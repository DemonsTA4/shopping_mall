<template>
  <div class="home-container">
    <!-- 加载中 -->
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div style="padding: 20px">
          <el-skeleton-item variant="image" style="width: 100%; height: 300px" />
          <div style="display: flex; justify-content: space-between; margin-top: 20px">
            <el-skeleton-item v-for="i in 8" :key="i" variant="image" style="width: 12%; height: 100px" />
          </div>
          <div style="margin-top: 20px">
            <el-skeleton-item variant="h3" style="width: 50%" />
            <div style="display: flex; flex-wrap: wrap; margin-top: 20px">
              <div v-for="i in 8" :key="i" style="width: 25%; padding: 10px; box-sizing: border-box">
                <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
                <el-skeleton-item variant="text" style="margin-top: 10px; width: 80%" />
                <el-skeleton-item variant="text" style="margin-top: 10px; width: 60%" />
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 实际内容 -->
      <div>
        <!-- 轮播图 -->
        <div class="banner-container">
          <el-carousel height="400px" :interval="5000" arrow="always" indicator-position="outside">
            <el-carousel-item v-for="item in banners" :key="item.id">
              <div class="banner-item" @click="goToProductDetail(item.id)">
                <img :src="item.imageUrl" :alt="item.title" />
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>

        <!-- 分类导航 -->
        <div class="category-container">
          <div class="category-item" v-for="item in categories" :key="item.id" @click="goToCategory(item.id)">
            <el-icon size="24"><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
          </div>
        </div>

        <!-- 推荐商品 -->
        <div class="recommend-container">
          <div class="section-title">
            <h2>推荐商品</h2>
            <router-link to="/products">查看全部</router-link>
          </div>
          <div class="product-grid">
            <div v-for="item in recommendProducts" :key="item.id" class="product-item" @click="goToProductDetail(item.id)">
              <div class="product-image">
                <img :src="item.imageUrl" :alt="item.name" />
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ item.name }}</h3>
                <div class="product-price">
                  <span class="current-price">¥{{ item.price }}</span>
                  <span class="original-price">¥{{ item.originalPrice }}</span>
                </div>
                <div class="product-meta">
                  <span>已售{{ item.sales }}件</span>
                  <span>评分{{ item.rate }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getBanners, getRecommendProducts, getCategories } from '@/api/product';

const router = useRouter();
const banners = ref([]);
const recommendProducts = ref([]);
const categories = ref([]);
const loading = ref(false);

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true;
    // 同时请求多个接口
    const [bannersRes, productsRes, categoriesRes] = await Promise.all([
      getBanners(),
      getRecommendProducts(),
      getCategories()
    ]);

    // ★★★ 添加日志来检查 productsRes 的结构 ★★★
    console.log('API调用后的 productsRes:', productsRes); 

    // 假设您的 `request` 封装返回的是后端最外层 data 字段的内容
    // 即 productsRes 的结构是 { list: [...], total: N, ... }
    if (productsRes && Array.isArray(productsRes.list)) {
      console.log('提取到的推荐商品列表 (productsRes.list):', productsRes.list);
      recommendProducts.value = productsRes.list;
    } 
    // 另一种常见情况：如果 productsRes 是后端返回的完整JSON { code: ..., message: ..., data: { list: ...}}
    // 并且您的 request 封装没有自动提取最外层的 data
    else if (productsRes && productsRes.data && Array.isArray(productsRes.data.list)) {
      console.log('提取到的推荐商品列表 (productsRes.data.list):', productsRes.data.list);
      recommendProducts.value = productsRes.data.list;
    } 
    else {
      console.warn('未能从 productsRes 中提取到商品列表，productsRes 的值为:', productsRes);
      recommendProducts.value = []; // 获取失败或数据格式不对，则设置为空数组
    }
    
    // 对 bannersRes 和 categoriesRes 也可能需要类似的判断和赋值逻辑
    // 例如，如果它们也遵循 { list: [...] } 或 { data: { list: [...] } } 结构
    if (bannersRes && Array.isArray(bannersRes.data)) { // 假设 bannersRes.data 直接是数组
         banners.value = bannersRes.data;
    } else if (bannersRes && bannersRes.data && Array.isArray(bannersRes.data.list)) { // 或者 bannersRes.data.list 是数组
         banners.value = bannersRes.data.list;
    } else {
         banners.value = [];
    }

    if (categoriesRes && Array.isArray(categoriesRes.data)) { // 假设 categoriesRes.data 直接是数组
         categories.value = categoriesRes.data;
    } else if (categoriesRes && categoriesRes.data && Array.isArray(categoriesRes.data.list)) { // 或者 categoriesRes.data.list 是数组
         categories.value = categoriesRes.data.list;
    } else {
         categories.value = [];
    }

  } catch (error) {
    console.error('获取首页数据失败:', error);
    recommendProducts.value = []; // 出错时也设置为空数组
    banners.value = [];
    categories.value = [];
  } finally {
    loading.value = false;
  }
};

// 商品详情页
const goToProductDetail = (id) => {
  router.push(`/product/${id}`);
};

// 商品分类页
const goToCategory = (id) => {
  router.push({
    path: '/products',
    query: { categoryId: id }
  });
};

// 模拟数据（当后端接口未准备好时使用）
const mockData = () => {
  // 模拟轮播图数据
  banners.value = [
    { id: 1, title: '618大促', imageUrl: 'https://img.alicdn.com/imgextra/i4/O1CN019Bd4Uo27zssuRYnZI_!!6000000007866-0-tps-2880-1070.jpg' },
    { id: 2, title: '新品发布', imageUrl: 'https://img.alicdn.com/imgextra/i4/O1CN01bGzRz324pdpt49rE0_!!6000000007440-0-tps-2880-1070.jpg' },
    { id: 3, title: '限时优惠', imageUrl: 'https://img.alicdn.com/imgextra/i2/O1CN01VtABSK1CCPdRQiFvp_!!6000000000052-0-tps-2880-1070.jpg' }
  ];
  
  // 模拟分类数据
  categories.value = [
    { id: 1, name: '手机数码', icon: 'Iphone' },
    { id: 2, name: '电脑办公', icon: 'Monitor' },
    { id: 3, name: '家用电器', icon: 'HomeFilled' },
    { id: 4, name: '服装鞋包', icon: 'ShoppingBag' },
    { id: 5, name: '美妆护肤', icon: 'Star' },
    { id: 6, name: '母婴玩具', icon: 'School' },
    { id: 7, name: '运动户外', icon: 'Basketball' },
    { id: 8, name: '家居百货', icon: 'House' }
  ];
  
  // 模拟推荐商品数据
  recommendProducts.value = Array(8).fill().map((_, index) => ({
    id: index + 1,
    name: `商品${index + 1}`,
    price: Math.floor(Math.random() * 1000 + 100),
    originalPrice: Math.floor(Math.random() * 1500 + 200),
    imageUrl: `https://picsum.photos/id/${20 + index}/400/400`,
    sales: Math.floor(Math.random() * 1000),
    rate: (Math.random() * 2 + 3).toFixed(1),
    description: '这是一个很好的商品'
  }));
};

onMounted(() => {
  // 尝试从API获取数据，如果失败则使用模拟数据
  fetchData().catch(() => mockData());
});
</script>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
  padding-bottom: 30px;
}

.banner-container {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  .banner-item {
    width: 100%;
    height: 100%;
    cursor: pointer;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.category-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  .category-item {
    width: 12.5%;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 15px 0;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-5px);
      color: #409EFF;
    }
    
    .el-icon {
      margin-bottom: 10px;
      font-size: 26px;
      color: #409EFF;
    }
    
    span {
      font-size: 14px;
    }
  }
}

.recommend-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #f0f0f0;
    
    h2 {
      font-size: 20px;
      font-weight: bold;
      color: #333;
      margin: 0;
    }
    
    a {
      color: #409EFF;
      font-size: 14px;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
  
  .product-grid {
    display: flex;
    flex-wrap: wrap;
    margin: 0 -10px;
    
    .product-item {
      width: 25%;
      padding: 10px;
      box-sizing: border-box;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      }
      
      .product-image {
        width: 100%;
        height: 0;
        padding-bottom: 100%;
        position: relative;
        border-radius: 4px;
        overflow: hidden;
        background-color: #f5f5f5;
        margin-bottom: 10px;
        
        img {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .product-info {
        padding: 10px;
      }
      
      .product-name {
        font-size: 14px;
        margin: 0 0 10px;
        line-height: 1.4;
        height: 40px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .product-price {
        display: flex;
        align-items: center;
        margin-bottom: 6px;
        
        .current-price {
          font-size: 16px;
          font-weight: bold;
          color: #f56c6c;
          margin-right: 8px;
        }
        
        .original-price {
          font-size: 12px;
          color: #999;
          text-decoration: line-through;
        }
      }
      
      .product-meta {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #999;
      }
    }
  }
}

@media (max-width: 768px) {
  .category-container .category-item {
    width: 25%;
  }
  
  .recommend-container .product-grid .product-item {
    width: 50%;
  }
}
</style>