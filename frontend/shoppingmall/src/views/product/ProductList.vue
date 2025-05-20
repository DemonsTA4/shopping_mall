<template>
  <div class="product-list-container">
    <!-- 分类导航 -->
    <div class="category-nav">
      <el-menu
        :default-active="searchForm.categoryId || 'all'"
        mode="horizontal"
        @select="handleCategorySelect"
      >
        <el-menu-item index="all">全部分类</el-menu-item>
        <el-menu-item 
          v-for="category in categories" 
          :key="category.id" 
          :index="category.id.toString()"
        >
          {{ category.name }}
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <el-form :model="searchForm" @submit.prevent="handleSearch" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            :placeholder="t('product.searchPlaceholder')"
            clearable
            @clear="handleSearch"
          >
            <template #append>
              <el-button type="primary" @click="handleSearch">
                {{ t('common.search') }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-input-number
            v-model="searchForm.minPrice"
            :placeholder="t('product.minPrice')"
            :min="0"
            :precision="2"
            :step="10"
          />
          <span class="price-separator">-</span>
          <el-input-number
            v-model="searchForm.maxPrice"
            :placeholder="t('product.maxPrice')"
            :min="0"
            :precision="2"
            :step="10"
          />
        </el-form-item>

        <el-form-item>
          <el-select v-model="searchForm.sort" @change="handleSearch">
            <el-option
              v-for="option in sortOptions"
              :key="option.value"
              :label="t(`product.sort.${option.label}`)"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <!-- 商品列表 -->
    <div v-loading="loading" class="product-list">
      <el-empty v-if="!loading && products.length === 0" :description="t('product.noData')" />
      <div v-else class="product-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <el-card :body-style="{ padding: '0px' }" shadow="hover">
            <div class="product-image" @click="goToDetail(product.id)">
              <el-image :src="product.imageUrl" fit="cover" lazy>
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="product-info">
              <h3 class="product-name" @click="goToDetail(product.id)">{{ product.name }}</h3>
              <div class="product-price">
                <span class="current-price">¥{{ product.price.toFixed(2) }}</span>
                <span v-if="product.originalPrice" class="original-price">
                  ¥{{ product.originalPrice.toFixed(2) }}
                </span>
              </div>
              <div class="product-footer">
                <span class="sales">{{ t('product.sales', { count: product.sales }) }}</span>
                <el-button 
                  type="primary" 
                  size="small"
                  :icon="ShoppingCart"
                  @click="handleAddToCart(product)"
                >
                  {{ t('product.addToCart') }}
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ElMessage } from 'element-plus';
import { Picture, ShoppingCart } from '@element-plus/icons-vue';
import { searchProducts } from '@/api/product';
import { addToCart, removeCartItem } from '@/api/cart';
import { useUserStore } from '@/store/modules/user';
import { getCategories } from '@/api/category';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: '',
  minPrice: null,
  maxPrice: null,
  sort: 'default',
  order: 'desc'
});

// 排序选项
const sortOptions = [
  { label: 'default', value: 'default' },
  { label: 'priceAsc', value: 'price_asc' },
  { label: 'priceDesc', value: 'price_desc' },
  { label: 'salesDesc', value: 'sales_desc' }
];

// 列表数据
const loading = ref(false);
const products = ref([]);
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);

// 分类列表
const categories = ref([]);

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategories();
    categories.value = res.data || [];
  } catch (error) {
    console.error('获取分类列表失败:', error);
    ElMessage.error('获取分类列表失败');
  }
};

// 处理分类选择
const handleCategorySelect = (index) => {
  searchForm.categoryId = index === 'all' ? '' : index;
  currentPage.value = 1; // 重置页码
  updateUrlAndFetch();
};

// 监听路由参数变化
watch(
  () => route.query,
  (newQuery) => {
    // 从URL参数更新搜索条件
    searchForm.keyword = newQuery.keyword || '';
    searchForm.categoryId = newQuery.categoryId || '';
    searchForm.minPrice = newQuery.minPrice ? Number(newQuery.minPrice) : null;
    searchForm.maxPrice = newQuery.maxPrice ? Number(newQuery.maxPrice) : null;
    searchForm.sort = newQuery.sort || 'default';
    searchForm.order = newQuery.order || 'desc';
    currentPage.value = Number(newQuery.page) || 1;
    pageSize.value = Number(newQuery.pageSize) || 12;

    // 执行搜索
    fetchProducts();
  },
  { immediate: true }
);

// 获取商品列表
// Products.vue fetchProducts
const fetchProducts = async () => {
  loading.value = true;

  // ★★★ 直接使用 queryParams 构建 API 参数 ★★★
  const paramsForApi = {
    pageNum: queryParams.page, // 使用 queryParams.page
    pageSize: queryParams.pageSize,
    keyword: queryParams.keyword || undefined,
    categoryId: queryParams.categoryId || undefined,
    sort: queryParams.sort,
    order: queryParams.order,
    // minPrice 和 maxPrice 如果是空字符串，后端需要能正确处理，或者在这里转换为数字/undefined
    minPrice: queryParams.minPrice ? parseFloat(queryParams.minPrice) : undefined,
    maxPrice: queryParams.maxPrice ? parseFloat(queryParams.maxPrice) : undefined
    // 如果您还有 status 等其他筛选条件，也从 queryParams 中获取
  };

  // 清理 undefined, null, 或空字符串的参数 (这个逻辑可以保留)
  Object.keys(paramsForApi).forEach(key => {
    if (paramsForApi[key] === undefined || paramsForApi[key] === null || paramsForApi[key] === '') {
      delete paramsForApi[key];
    }
  });

  console.log('[Products.vue fetchProducts] 准备调用API，参数:', JSON.parse(JSON.stringify(paramsForApi)));
  // ... 后续 try/catch 块 ..
  try {
    const response = await getProducts(paramsForApi); // 使用导入的 getProducts 函数

    console.log('[Products.vue fetchProducts] API响应:', response); // ★ 我们之前看到的日志输出是这个 response ★

    // 根据您之前 image_212228.png 的日志，response 的结构是：
    // { code: 200, message: '成功', data: Array(5) }
    // 其中 response.data 直接就是商品对象的数组

    if (response && response.code === 200 && Array.isArray(response.data)) { // ★ 条件调整：检查 response.data 是不是数组 ★
      products.value = response.data; // ★ 直接使用 response.data 作为商品列表 ★
      
      // ★★★ 核心问题：total 丢失了 ★★★
      // 后端返回的这个 response 结构中，没有包含 total (总商品数) 信息。
      // 您需要与后端确认，分页接口 /products 如何返回总记录数。
      // 假设后端暂时无法修改，前端可以做临时处理，但这会影响分页的准确性。
      total.value = response.data.length; // 临时方案：将当前页的商品数量作为 total (不准确，分页会出问题)
      // 或者，如果后端能在响应头中返回总数 (例如 X-Total-Count)，可以在 axios 拦截器中获取。
      // 或者，如果API约定了如果这是最后一页，会有特殊标记。
      // 最好的方案是后端在响应体中明确返回 total。

      console.log('从API获取的商品列表:', products.value);
      console.log('商品总数 (可能不准确，依赖后端返回):', total.value);
    } else {
      console.error('从API获取商品列表失败或响应结构不正确:', response?.message || '未知错误', '完整响应:', response);
      products.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('调用API获取商品列表时发生异常:', error);
    products.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1; // 重置页码
  updateUrlAndFetch();
};

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page;
  updateUrlAndFetch();
};

// 处理每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1; // 重置页码
  updateUrlAndFetch();
};

// 更新URL并获取数据
const updateUrlAndFetch = () => {
  // 构建查询参数
  const query = {
    ...searchForm,
    page: currentPage.value,
    pageSize: pageSize.value
  };

  // 移除空值
  Object.keys(query).forEach(key => {
    if (query[key] === '' || query[key] === null || query[key] === undefined) {
      delete query[key];
    }
  });

  // 更新URL
  router.push({ query });
};

// 跳转到商品详情
const goToDetail = (id) => {
  router.push(`/product/${id}`);
};

// 添加到购物车
const handleAddToCart = async (product) => {
  if (!userStore.isLogin) {
    ElMessage.warning(t('common.pleaseLogin'));
    router.push('/login');
    return;
  }

  try {
    await addToCart({
      productId: product.id,
      quantity: 1
    });
    ElMessage.success(t('product.addToCartSuccess'));
  } catch (error) {
    console.error('添加到购物车失败:', error);
    ElMessage.error(t('product.addToCartError'));
  }
};

onMounted(() => {
  fetchCategories();
  // 初始化时已经通过路由参数监听触发了搜索
});
</script>

<style lang="scss" scoped>
.product-list-container {
  padding: 20px;

  .category-nav {
    background: #fff;
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    :deep(.el-menu) {
      border-bottom: none;
      padding: 0 20px;
    }

    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      
      &.is-active {
        font-weight: bold;
      }
    }
  }

  .search-filter-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .search-form {
      display: flex;
      gap: 20px;
      align-items: flex-start;

      .el-form-item {
        margin-bottom: 0;
        flex: 1;

        &:first-child {
          flex: 2;
        }

        .price-separator {
          margin: 0 10px;
        }
      }
    }
  }

  .product-list {
    min-height: 400px;

    .product-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 20px;
    }

    .product-card {
      .product-image {
        height: 250px;
        overflow: hidden;
        cursor: pointer;

        .el-image {
          width: 100%;
          height: 100%;
          transition: transform 0.3s;

          &:hover {
            transform: scale(1.05);
          }
        }

        .image-error {
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f5f7fa;
          color: #909399;
          font-size: 30px;
        }
      }

      .product-info {
        padding: 14px;

        .product-name {
          margin: 0 0 10px;
          font-size: 16px;
          line-height: 1.4;
          height: 44px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          cursor: pointer;

          &:hover {
            color: var(--el-color-primary);
          }
        }

        .product-price {
          margin-bottom: 10px;

          .current-price {
            color: #ff4400;
            font-size: 20px;
            font-weight: bold;
            margin-right: 8px;
          }

          .original-price {
            color: #999;
            text-decoration: line-through;
            font-size: 14px;
          }
        }

        .product-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .sales {
            color: #999;
            font-size: 14px;
          }
        }
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .product-list-container {
    .search-filter-section {
      .search-form {
        flex-direction: column;
        gap: 10px;

        .el-form-item {
          width: 100%;
        }
      }
    }
  }
}
</style> 