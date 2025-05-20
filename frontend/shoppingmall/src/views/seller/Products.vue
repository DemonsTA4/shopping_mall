<template>
  <div class="seller-products">
    <h1 class="page-title">商品管理</h1>

    <!-- 功能按钮和搜索 -->
    <div class="action-bar">
      <el-button type="primary" @click="$router.push('/seller/product/add')">
        添加商品
      </el-button>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品名称或编号"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            搜索
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <div class="filter-item">
        <span class="label">商品分类:</span>
        <el-select v-model="filters.categoryId" placeholder="所有分类" clearable @change="fetchProducts">
          <el-option
            v-for="item in categories"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="label">商品状态:</span>
        <el-select v-model="filters.status" placeholder="所有状态" clearable @change="fetchProducts">
          <el-option label="已上架" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="label">价格区间:</span>
        <el-input-number v-model="filters.minPrice" :min="0" :step="10" placeholder="最低价" size="small" />
        <span class="separator">-</span>
        <el-input-number v-model="filters.maxPrice" :min="0" :step="10" placeholder="最高价" size="small" />
      </div>
      
      <div class="filter-actions">
        <el-button type="primary" @click="fetchProducts">筛选</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="product-list-card">
      <el-table
        v-loading="loading"
        :data="products"
        style="width: 100%"
        border
      >
        <el-table-column type="index" width="50" />
        
        <el-table-column prop="name" label="商品名称">
          <template #default="scope">
            <div class="product-info">
              <el-image :src="scope.row.imageUrl" class="product-image" />
              <span class="product-name">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="productNo" label="商品编号" width="150" />
        
        <el-table-column prop="price" label="价格" width="120">
          <template #default="scope">
            <span class="price">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="stock" label="库存" width="100" />
        
        <el-table-column prop="sales" label="销量" width="100" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180" />
        
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="editProduct(scope.row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="toggleProductStatus(scope.row)">
              {{ scope.row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" size="small" @click="deleteProductConfirm(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch} from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getCategories, getProducts, createProduct, deleteProduct } from '@/api/product';

const router = useRouter();
const loading = ref(false);
const products = ref([]);
const categories = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchKeyword = ref('');

// 筛选条件
const filters = reactive({
  categoryId: '',
  status: '',
  minPrice: null,
  maxPrice: null
});

const queryParams = ref({});

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategories();
    categories.value = res.data || [];
  } catch (error) {
    console.error('获取分类失败:', error);
    // 模拟数据
    categories.value = [
      { id: 1, name: '手机' },
      { id: 2, name: '电脑' },
      { id: 3, name: '家电' },
      { id: 4, name: '服装' },
      { id: 5, name: '鞋靴' }
    ];
  }
};

// 获取商品列表
const fetchProducts = async () => {
  console.log('DEBUG: [Products.vue] fetchProducts function STARTED.'); // ★ 1. 函数开始执行

  loading.value = true;
  const paramsForApi = {
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    keyword: searchKeyword.value || undefined,
    categoryId: filters.categoryId || undefined,
    status: filters.status !== '' ? filters.status : undefined,
    minPrice: filters.minPrice,
    maxPrice: filters.maxPrice
  };
  Object.keys(paramsForApi).forEach(key => {
    if (paramsForApi[key] === undefined || paramsForApi[key] === null || paramsForApi[key] === '') {
      delete paramsForApi[key];
    }
  });

  console.log('DEBUG: [Products.vue fetchProducts] API call PARAMS:', JSON.parse(JSON.stringify(paramsForApi))); // ★ 2. 打印API参数

  try {
    console.log('DEBUG: [Products.vue fetchProducts] BEFORE calling await getProducts.'); // ★ 3. 调用API之前
    const response = await getProducts(paramsForApi);
    console.log('DEBUG: [Products.vue fetchProducts] AFTER calling await getProducts. RAW RESPONSE:', response); // ★ 4. API调用之后，打印原始响应 ★★★ 这是我们最想看到的日志 ★★★

    if (response && response.code === 200 && response.data) {
      console.log('DEBUG: [Products.vue fetchProducts] Response structure seems OK. Extracting list.'); // ★ 5. 进入了if分支
      products.value = response.data.list || response.data.records || [];
      total.value = response.data.total || 0;
      console.log('DEBUG: [Products.vue fetchProducts] Extracted products:', products.value); // ★ 6. 打印提取后的商品列表
      console.log('DEBUG: [Products.vue fetchProducts] Extracted total:', total.value);
    } else {
      console.warn('DEBUG: [Products.vue fetchProducts] FAILED to extract list from response or response code not 200. Response:', response); // ★ 7. 进入了else分支
      products.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('DEBUG: [Products.vue fetchProducts] EXCEPTION caught:', error); // ★ 8. 捕获到异常
    console.error('DEBUG: [Products.vue fetchProducts] Error stack:', error.stack); // 打印错误堆栈
    products.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
    console.log('DEBUG: [Products.vue fetchProducts] fetchProducts function FINISHED.'); // ★ 9. 函数结束
  }
};

onMounted(() => {
  fetchCategories();
  console.log('DEBUG: [Products.vue] onMounted hook CALLED.');
  fetchProducts();
});

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchProducts();
};

// 重置筛选条件
const resetFilters = () => {
  searchKeyword.value = '';
  Object.keys(filters).forEach(key => {
    filters[key] = typeof filters[key] === 'number' ? null : '';
  });
  currentPage.value = 1;
  fetchProducts();
};

// 编辑商品
const editProduct = (product) => {
  router.push(`/seller/product/edit/${product.id}`);
};

// 切换商品状态
const toggleProductStatus = async (product) => {
  try {
    // 这里应该调用API
    // await updateProductStatus(product.id, { status: product.status === 1 ? 0 : 1 });
    
    // 更新本地数据
    product.status = product.status === 1 ? 0 : 1;
    
    ElMessage({
      type: 'success',
      message: `商品${product.status === 1 ? '上架' : '下架'}成功`
    });
  } catch (error) {
    console.error('修改商品状态失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

// 删除商品确认
const deleteProductConfirm = async (product) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品"${product.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    // 这里应该调用API
    // await deleteProduct(product.id);
    
    // 更新本地数据
    products.value = products.value.filter(p => p.id !== product.id);
    
    ElMessage({
      type: 'success',
      message: '删除成功'
    });
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error);
      ElMessage.error('删除失败，请重试');
    }
  }
};

// 分页相关
const handleSizeChange = () => {
  fetchProducts();
};

const handleCurrentChange = () => {
  fetchProducts();
};

onMounted(() => {
  fetchCategories();
  fetchProducts();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.seller-products {
  .action-bar {
    display: flex;
    justify-content: space-between;
    margin-bottom: $spacing-base;
    
    .search-input {
      width: 300px;
    }
  }
  
  .filter-card {
    margin-bottom: $spacing-base;
    
    .filter-item {
      display: inline-flex;
      align-items: center;
      margin-right: $spacing-large;
      margin-bottom: $spacing-small;
      
      .label {
        margin-right: $spacing-small;
      }
      
      .separator {
        margin: 0 $spacing-small;
      }
    }
    
    .filter-actions {
      display: inline-flex;
      margin-left: $spacing-base;
    }
  }
  
  .product-list-card {
    .product-info {
      display: flex;
      align-items: center;
      
      .product-image {
        width: 40px;
        height: 40px;
        margin-right: $spacing-small;
        object-fit: cover;
      }
      
      .product-name {
        @include text-ellipsis;
        max-width: 250px;
      }
    }
    
    .price {
      color: $danger-color;
      font-weight: bold;
    }
  }
  
  .pagination-container {
    margin-top: $spacing-large;
    text-align: right;
  }
}

@include respond-to(md) {
  .seller-products {
    .action-bar {
      flex-direction: column;
      
      .search-input {
        width: 100%;
        margin-top: $spacing-small;
      }
    }
    
    .filter-card {
      .filter-item {
        display: flex;
        width: 100%;
        margin-right: 0;
        margin-bottom: $spacing-small;
      }
      
      .filter-actions {
        display: flex;
        width: 100%;
        margin-left: 0;
        margin-top: $spacing-small;
        
        .el-button {
          flex: 1;
        }
      }
    }
  }
}
</style>