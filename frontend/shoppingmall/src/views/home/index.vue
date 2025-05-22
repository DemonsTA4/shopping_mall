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
          <div 
            class="category-item" 
            v-for="item in categories" 
            :key="item.id" 
            @click="goToCategory(item.id)"
          >
            <el-image 
              :src="getCategoryIcon(item.name)"
              :alt="item.name" 
              fit="contain" 
              class="category-icon-img" >
              <template #error> <div class="image-slot-icon">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
              <template #placeholder> <div class="image-slot-icon">
                  <el-icon><Loading /></el-icon>
                </div>
              </template>
            </el-image>
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
// 只导入 getRecommendProducts，因为 banners 和 categories 已经是本地数据了
import { getRecommendProducts } from '@/api/product';

// 导入分类图标
import foodIcon from '@/assets/images/food.png';
import computerIcon from '@/assets/images/computer.png';
import clothingIcon from '@/assets/images/clothing.png';
import earphoneIcon from '@/assets/images/earphone.png'; // 电子产品
import phoneIcon from '@/assets/images/phone.png';
import defaultCategoryIcon from '@/assets/images/default-category.png';

// 导入本地轮播图图片 (请确保这些文件名在您的 /assets/images/ 文件夹中是正确的)
import bannerImage1 from '@/assets/images/one.png'; // 示例文件名，请替换
import bannerImage2 from '@/assets/images/two.png';   // 示例文件名，请替换
import bannerImage3 from '@/assets/images/three.png';// 示例文件名，请替换
// 如果有更多轮播图图片，请在这里继续导入

const router = useRouter();
const recommendProducts = ref([]);
const loading = ref(false); // 用于 el-skeleton 的加载状态

// 本地定义的轮播图数据
const banners = ref([
  { id: 'banner_1', imageUrl: bannerImage1, title: '风景一' }, // id 可以自定义, title 用于 alt 属性
  { id: 'banner_2', imageUrl: bannerImage2, title: '动漫图' },
  { id: 'banner_3', imageUrl: bannerImage3, title: '森林景' },
]);

// 本地定义的分类导航数据
const categories = ref([
  { id: 'cat_electronics', name: '电子产品' }, // id 保持唯一
  { id: 'cat_clothing', name: '服装' },
  { id: 'cat_food', name: '食品' },
  { id: 'cat_phone', name: '手机' },
  { id: 'cat_computer', name: '电脑' },
]);

const categoryIconMap = {
  '电子产品': earphoneIcon,
  '服装': clothingIcon,
  '食品': foodIcon,
  '手机': phoneIcon,
  '电脑': computerIcon,
};

function getCategoryIcon(categoryName) {
  const iconSrc = categoryIconMap?.[categoryName];
  return iconSrc || defaultCategoryIcon;
}

// 获取推荐商品数据的函数 (名称已统一为 fetchData)
const fetchData = async () => {
  try {
    loading.value = true; // 开始加载，显示骨架屏
    const productsRes = await getRecommendProducts();

    // console.log('API调用后的 productsRes:', productsRes); // 调试日志

    if (productsRes && Array.isArray(productsRes.list)) {
      // console.log('提取到的推荐商品列表 (productsRes.list):', productsRes.list); // 调试日志
      recommendProducts.value = productsRes.list;
    } else if (productsRes && productsRes.data && Array.isArray(productsRes.data.list)) {
      // console.log('提取到的推荐商品列表 (productsRes.data.list):', productsRes.data.list); // 调试日志
      recommendProducts.value = productsRes.data.list;
    } else {
      console.warn('未能从 productsRes 中提取到商品列表，productsRes 的值为:', productsRes);
      recommendProducts.value = [];
    }
  } catch (error) {
    console.error('获取推荐商品数据失败:', error);
    recommendProducts.value = []; // 出错时也设置为空数组
  } finally {
    loading.value = false; // 加载完成，隐藏骨架屏
  }
};

// 点击轮播图或商品图跳转到详情页的函数
const goToProductDetail = (itemId) => {
  // itemId 可能是轮播图的 id (如 'banner_1') 或商品的 id (通常是数字或不同的字符串格式)
  console.log('点击项目，ID:', itemId);

  // 根据 itemId 的类型或前缀判断是轮播图还是商品
  if (typeof itemId === 'string' && itemId.startsWith('banner_')) {
    // 这是轮播图的点击事件
    // 您可以根据不同的 banner id 执行不同的操作，例如：
    // if (itemId === 'banner_1') { router.push('/promo/landscape-special'); }
    // else if (itemId === 'banner_2') { router.push('/collection/anime-goods'); }
    // 如果轮播图不用于导航，可以留空或移除这里的逻辑
    console.log(`轮播图 ${itemId} 被点击。如果需要，请定义具体的跳转逻辑。`);
  } else if (itemId) { // 假设商品 ID 不是以 'banner_' 开头
    // 这是商品的点击事件
    router.push(`/product/${itemId}`);
  }
};

// 点击分类导航跳转到分类商品列表页
const goToCategory = (categoryId) => {
  router.push({
    path: '/products',
    query: { categoryId: categoryId }
  });
};

// 组件挂载后获取数据
onMounted(() => {
  fetchData(); // 调用 fetchData 函数获取推荐商品
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
  
  .category-icon-img {
    width: 90px;  /* 根据需要调整图标大小 */
    height: 102px; /* 根据需要调整图标大小 */
    margin-bottom: 8px; /* 图标和文字之间的间距 */
    object-fit: contain; /* 确保图片等比缩放并完整显示 */
  }
  .image-slot-icon { /* el-image 加载失败或加载中占位符的样式 */
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background: #f5f7fa;
    color: #c0c4cc;
    font-size: 20px; /* 根据图标大小调整 */
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