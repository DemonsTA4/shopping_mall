<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getProducts, getCategories } from '@/api/product';
import ProductCard from '@/components/business/ProductCard.vue';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const products = ref([]);
const categories = ref([]);
const total = ref(0);
const currentCategory = ref(null);

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 12,
  categoryId: route.query.categoryId || '',
  keyword: route.query.keyword || '',
  sort: route.query.sort || 'default',
  order: route.query.order || 'desc',
  minPrice: route.query.minPrice || '',
  maxPrice: route.query.maxPrice || ''
});

// 计算属性：排序选项
const sortOptions = computed(() => {
  return [
    { label: '默认排序', value: 'default' },
    { label: '价格升序', value: 'price,asc' },
    { label: '价格降序', value: 'price,desc' },
    { label: '销量优先', value: 'sales,desc' },
    { label: '好评优先', value: 'rating,desc' }
  ];
});

// 处理排序变化
const handleSortChange = (value) => {
  const [sort, order] = value.split(',');
  queryParams.sort = sort;
  queryParams.order = order || 'desc';
  fetchProducts();
};

// 获取商品列表
// Products.vue fetchProducts
const fetchProducts = async () => {
  console.log('DEBUG: [List.vue] fetchProducts function STARTED.');
  loading.value = true;

  const paramsForApi = {
    pageNum: queryParams.page,        // ★ 使用 queryParams.page
    pageSize: queryParams.pageSize,   // ★ 使用 queryParams.pageSize
    keyword: queryParams.keyword || undefined,
    categoryId: queryParams.categoryId || undefined,
    sort: queryParams.sort,           // ★ 确保 queryParams 中有 sort
    order: queryParams.order,         // ★ 确保 queryParams 中有 order
    minPrice: queryParams.minPrice ? parseFloat(queryParams.minPrice) : undefined,
    maxPrice: queryParams.maxPrice ? parseFloat(queryParams.maxPrice) : undefined,
    // 如果后端 API 需要 'type' 参数来区分普通商品列表和推荐商品，您需要从 queryParams 或其他地方获取
    // type: queryParams.type || 'all_products' // 示例，假设默认类型是 'all_products'
  };

  // 清理 undefined, null, 或空字符串的参数
  Object.keys(paramsForApi).forEach(key => {
    if (paramsForApi[key] === undefined || paramsForApi[key] === null || paramsForApi[key] === '') {
      delete paramsForApi[key];
    }
  });

  console.log('DEBUG: [List.vue] Calling getProducts with params:', JSON.parse(JSON.stringify(paramsForApi)));
  
  try {
    // ... (构建 paramsForApi 的代码) ...
    console.log('DEBUG: [List.vue] Calling getProducts with params:', JSON.parse(JSON.stringify(paramsForApi)));
    
    const apiResult = await getProducts(paramsForApi);
  
    // ★★★★★ 这是最关键的日志，现在我们知道它的结构了 ★★★★★
    console.log('DEBUG: [List.vue] Raw apiResult from getProducts:', JSON.parse(JSON.stringify(apiResult))); 
  
    // ★★★ 根据最新的日志，apiResult 的结构是 { code: 200, message: "成功", data: { list: Array, total: N, ... } } ★★★
    if (apiResult && apiResult.code === 200 && apiResult.data && typeof apiResult.data === 'object' && Array.isArray(apiResult.data.list) && typeof apiResult.data.total !== 'undefined') {
      console.log('DEBUG: [List.vue] API call successful and response.data is an object with list and total.'); // 进入了正确的分支
  
      products.value = apiResult.data.list; // ★ 从 apiResult.data.list 获取商品数组 ★
      total.value = apiResult.data.total;   // ★ 从 apiResult.data.total 获取总记录数 ★
      
      // 如果需要，也可以更新其他分页信息
      // if (typeof apiResult.data.pageNum !== 'undefined') {
      //   queryParams.page = apiResult.data.pageNum; // 或者您用于存储当前页的 ref
      // }
  
      console.log('DEBUG: [List.vue] Updated products.value:', JSON.parse(JSON.stringify(products.value)));
      console.log('DEBUG: [List.vue] Updated total.value:', total.value);
  
      if (products.value.length === 0) { // 如果列表为空，但API调用是成功的
          ElMessage.info('暂无符合条件的商品');
      }
  
    } else {
      console.error('DEBUG: [List.vue] API call failed or response structure incorrect. API Response:', JSON.parse(JSON.stringify(apiResult)));
      products.value = [];
      total.value = 0;
      ElMessage.error(apiResult?.message || '获取商品数据失败，请重试');
    }
  } catch (error) {
    console.error('DEBUG: [List.vue] Exception during fetchProducts:', error);
    console.error('DEBUG: [List.vue] Error stack for exception:', error.stack);
    products.value = [];
    total.value = 0;
    ElMessage.error('获取商品数据异常，请重试');
  } finally {
    loading.value = false;
    console.log('DEBUG: [List.vue] fetchProducts FINISHED.');
  }
};

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategories();
	console.log('DEBUG: Raw response from getProducts:', res);
    categories.value = [
      { id: '', name: '全部分类' },
      ...(res.data || [])
    ];
    
    // 设置当前选中的分类
    if (queryParams.categoryId) {
      currentCategory.value = categories.value.find(
        item => item.id.toString() === queryParams.categoryId.toString()
      );
    } else {
      currentCategory.value = categories.value[0];
    }
  } catch (error) {
    console.error('获取分类失败:', error);
  }
};

// 切换分类
const handleCategoryChange = (category) => {
  currentCategory.value = category;
  queryParams.categoryId = category.id;
  queryParams.page = 1;
  fetchProducts();
};

// 搜索商品
const handleSearch = () => {
  queryParams.page = 1;
  fetchProducts();
};

// 重置搜索条件
const resetFilters = () => {
  Object.assign(queryParams, {
    keyword: '',
    minPrice: '',
    maxPrice: '',
    sort: 'default',
    order: 'desc'
  });
  handleSearch();
};

// 分页变化
const handlePageChange = (page) => {
  queryParams.page = page;
  fetchProducts();
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.page = 1;
  queryParams.pageSize = size;
  fetchProducts();
};

// 监听路由参数变化
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.categoryId !== queryParams.categoryId ||
        newQuery.keyword !== queryParams.keyword) {
      queryParams.categoryId = newQuery.categoryId || '';
      queryParams.keyword = newQuery.keyword || '';
      queryParams.page = parseInt(newQuery.page) || 1;
      
      if (categories.value.length > 0) {
        currentCategory.value = categories.value.find(
          item => item.id.toString() === queryParams.categoryId.toString()
        ) || categories.value[0];
      }
      
      fetchProducts();
    }
  }
);

onMounted(() => {
  fetchCategories();
  fetchProducts();
});
</script>

<template>
  <div class="product-list-container">
    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="category-list">
        <div 
          v-for="item in categories" 
          :key="item.id" 
          class="category-item"
          :class="{ active: currentCategory && item.id === currentCategory.id }"
          @click="handleCategoryChange(item)"
        >
          {{ item.name }}
        </div>
      </div>
    </div>
    
    <!-- 过滤和搜索 -->
    <div class="filter-section">
      <div class="filter-form">
        <el-form :model="queryParams" inline>
          <el-form-item label="关键词">
            <el-input 
              v-model="queryParams.keyword" 
              placeholder="输入商品名称关键词"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          
          <el-form-item label="价格区间">
            <div class="price-range">
              <el-input-number 
                v-model="queryParams.minPrice" 
                :min="0" 
                :step="10" 
                controls-position="right"
                placeholder="最低价"
                size="small"
                style="width: 120px"
              />
              <span class="range-separator">-</span>
              <el-input-number 
                v-model="queryParams.maxPrice" 
                :min="0" 
                :step="10" 
                controls-position="right"
                placeholder="最高价"
                size="small"
                style="width: 120px"
              />
            </div>
          </el-form-item>
          
          <el-form-item label="排序">
            <el-select 
              v-model="queryParams.sort" 
              placeholder="默认排序"
              size="small"
              @change="handleSortChange"
            >
              <el-option 
                v-for="item in sortOptions" 
                :key="item.value" 
                :label="item.label" 
                :value="item.value" 
              />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    
    <!-- 商品列表 -->
    <div class="product-list-wrapper">
      <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
      
      <el-skeleton v-if="loading" :count="8" animated>
        <template #template>
          <div class="skeleton-card">
            <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
            <div style="padding: 14px">
              <el-skeleton-item variant="h3" style="width: 80%" />
              <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 16px;">
                <el-skeleton-item variant="text" style="width: 30%" />
                <el-skeleton-item variant="text" style="width: 20%" />
              </div>
            </div>
          </div>
        </template>
      </el-skeleton>
      
      <div v-else class="product-list">
        <div v-for="product in products" :key="product.id" class="product-item">
          <ProductCard :product="product" />
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.product-list-container {
  min-height: 500px;
}

.category-nav {
  background-color: #fff;
  border-radius: $border-radius-base;
  box-shadow: $box-shadow-light;
  margin-bottom: $spacing-base;
  padding: $spacing-base;
  
  .category-list {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-base;
    
    .category-item {
      padding: $spacing-small $spacing-base;
      font-size: $font-size-base;
      cursor: pointer;
      border-radius: $border-radius-base;
      transition: all 0.3s;
      
      &:hover {
        color: $primary-color;
      }
      
      &.active {
        background-color: $primary-color;
        color: #fff;
      }
    }
  }
}

.filter-section {
  background-color: #fff;
  border-radius: $border-radius-base;
  box-shadow: $box-shadow-light;
  margin-bottom: $spacing-base;
  padding: $spacing-base;
  
  .filter-form {
    .price-range {
      display: flex;
      align-items: center;
      
      .range-separator {
        margin: 0 $spacing-small;
      }
    }
  }
}

.product-list-wrapper {
  min-height: 500px;
  
  .product-list {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-base;
    
    .product-item {
      margin-bottom: $spacing-base;
    }
  }
}

.skeleton-card {
  width: 100%;
  border-radius: $border-radius-base;
  overflow: hidden;
  box-shadow: $box-shadow-light;
  margin-bottom: $spacing-base;
}

.pagination-wrapper {
  margin-top: $spacing-large;
  display: flex;
  justify-content: center;
}

@include respond-to(md) {
  .product-list-wrapper .product-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

@include respond-to(sm) {
  .product-list-wrapper .product-list {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-section .filter-form {
    .el-form-item {
      margin-bottom: $spacing-base;
    }
  }
}

@include respond-to(xs) {
  .product-list-wrapper .product-list {
    grid-template-columns: repeat(1, 1fr);
  }
  
  .filter-section .filter-form {
    .el-form {
      display: flex;
      flex-direction: column;
      
      .el-form-item {
        margin-right: 0;
      }
    }
  }
}
</style>