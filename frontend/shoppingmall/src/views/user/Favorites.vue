<template>
  <div class="favorites-container">
    <h1 class="page-title">我的收藏</h1>
    
    <el-card v-if="!loading">
      <div v-if="favoriteList.length === 0" class="empty-favorites">
        <el-empty description="暂无收藏商品" />
        <el-button type="primary" @click="goToProductList">去购物</el-button>
      </div>
      
      <div v-else class="favorites-list">
        <div 
          v-for="item in favoriteList" 
          :key="item.id" 
          class="favorite-item"
        >
          <div class="product-card">
            <div class="product-image" @click="goToProductDetail(item.productId)">
              <el-image 
                :src="item.imageUrl" 
                :alt="item.name"
                fit="cover"
              />
            </div>
            
            <div class="product-info">
              <div class="product-name" @click="goToProductDetail(item.productId)">
                {{ item.name }}
              </div>
              
              <div class="product-price">
                <span class="current-price">¥{{ item.price }}</span>
                <span v-if="item.originalPrice" class="original-price">¥{{ item.originalPrice }}</span>
              </div>
              
              <div class="product-actions">
                <el-button type="danger" plain size="small" @click="removeFromFavorites(item.id)">
                  取消收藏
                </el-button>
                <el-button type="primary" size="small" @click="addToCart(item)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { useCartStore } from '@/store/modules/cart';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getFavorites, removeFavorite } from '@/api/user';

const router = useRouter();
const userStore = useUserStore();
const cartStore = useCartStore();
const loading = ref(false);
const favoriteList = ref([]);
const total = ref(0);

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 12
});

// 获取收藏列表
const fetchFavorites = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await getFavorites(queryParams);
    favoriteList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error('获取收藏列表失败:', error);
    ElMessage.error('获取收藏列表失败，请重试');
    
    // 使用模拟数据
    mockFavorites();
  } finally {
    loading.value = false;
  }
};

// a mock function for development
const mockFavorites = () => {
  const list = [];
  
  for (let i = 1; i <= 10; i++) {
    list.push({
      id: i,
      productId: i * 100,
      name: `商品${i}`,
      price: (Math.random() * 1000 + 100).toFixed(2),
      originalPrice: (Math.random() * 1500 + 500).toFixed(2),
      imageUrl: `https://picsum.photos/id/${i + 10}/300/300`,
      createTime: new Date().toISOString()
    });
  }
  
  favoriteList.value = list;
  total.value = 30;
};

// 取消收藏
const removeFromFavorites = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消收藏该商品吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await removeFavorite(id);
    ElMessage.success('取消收藏成功');
    fetchFavorites();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error);
      ElMessage.error('操作失败，请重试');
    }
  }
};

// 加入购物车
const addToCart = (item) => {
  cartStore.addToCart({
    id: item.productId,
    name: item.name,
    price: item.price,
    imageUrl: item.imageUrl,
    quantity: 1
  });
  
  ElMessage.success('已加入购物车');
};

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push(`/product/${productId}`);
};

// 跳转到商品列表页
const goToProductList = () => {
  router.push('/products');
};

// 处理页码变化
const handleCurrentChange = (page) => {
  queryParams.page = page;
  fetchFavorites();
};

// 处理每页数量变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size;
  queryParams.page = 1;
  fetchFavorites();
};

onMounted(() => {
  fetchFavorites();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.favorites-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .empty-favorites {
    padding: 50px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .el-button {
      margin-top: $spacing-base;
    }
  }
  
  .favorites-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: $spacing-base;
    
    .favorite-item {
      transition: transform 0.3s;
      
      &:hover {
        transform: translateY(-5px);
      }
      
      .product-card {
        height: 100%;
        border-radius: $border-radius-base;
        box-shadow: $box-shadow-light;
        overflow: hidden;
        
        .product-image {
          width: 100%;
          height: 200px;
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
        }
        
        .product-info {
          padding: $spacing-base;
          
          .product-name {
            margin-bottom: $spacing-small;
            font-size: $font-size-medium;
            font-weight: bold;
            cursor: pointer;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            height: 40px;
            
            &:hover {
              color: $primary-color;
            }
          }
          
          .product-price {
            margin-bottom: $spacing-base;
            
            .current-price {
              font-size: $font-size-large;
              font-weight: bold;
              color: $danger-color;
            }
            
            .original-price {
              margin-left: $spacing-small;
              font-size: $font-size-small;
              color: $text-secondary;
              text-decoration: line-through;
            }
          }
          
          .product-actions {
            display: flex;
            justify-content: space-between;
          }
        }
      }
    }
  }
  
  .pagination-container {
    margin-top: $spacing-large;
    display: flex;
    justify-content: center;
  }
}

@include respond-to(sm) {
  .favorites-container {
    .favorites-list {
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    }
  }
}
</style> 