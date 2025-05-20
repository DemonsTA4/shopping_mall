<template>
  <div class="product-card" @click="viewDetail">
    <div class="product-image">
      <img :src="product.imageUrl" :alt="product.name" />
      <div v-if="product.discount" class="discount-badge">
        {{ product.discount }}折
      </div>
    </div>
    
    <div class="product-info">
      <h3 class="product-name">{{ product.name }}</h3>
      
      <div class="product-price">
        <span class="current-price">¥{{ product.price }}</span>
        <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
      </div>
      
      <div class="product-meta">
        <span v-if="product.sales">已售{{ product.sales }}件</span>
        <div class="product-rating" v-if="product.rate">
          <el-rate 
            v-model="product.rate" 
            disabled 
            text-color="#ff9900"
            size="small"
            :max="5"
            :allow-half="true"
            :show-score="true"
            score-template="{value}"
          />
        </div>
      </div>
      
      <div v-if="showActions" class="product-actions">
        <el-button 
          type="primary" 
          size="small" 
          :loading="addingToCart"
          @click.stop="addToCart"
        >
          <el-icon><ShoppingCart /></el-icon>
          加入购物车
        </el-button>
        
        <el-button 
          :type="isCollected ? 'danger' : 'default'" 
          size="small" 
          circle
          @click.stop="collectProduct"
        >
          <el-icon><Star /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'; // <-- 引入 onMounted 和 watch
import { useRouter } from 'vue-router';
import { useCartStore } from '@/store/modules/cart';
import { useUserStore } from '@/store/modules/user';
import { ElMessage } from 'element-plus';
// 假设 getFavoriteStatus 也从 @/api/product (或新的 @/api/favorite) 导出
import { toggleCollect, getFavoriteStatus } from '@/api/product';

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
});

const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();
// const isCollected = ref(props.product.isCollected || false); // <-- 旧的初始化方式，将被替换
const isCollected = ref(false); // <-- 新的默认初始值，将在 onMounted 中更新
const addingToCart = ref(false);

const isLoggedIn = computed(() => userStore.isLogin)

// 新增：获取并设置初始收藏状态的函数
const fetchInitialFavoriteStatus = async () => {
  // 在函数开始时打印日志，帮助追踪执行情况和关键变量状态
  console.log(`ProductCard (ID: ${props.product.id}): fetchInitialFavoriteStatus called. isLoggedIn: ${isLoggedIn.value}`);

  if (!isLoggedIn.value || !props.product || !props.product.id) {
    isCollected.value = false;
    if (!isLoggedIn.value) {
      console.log(`ProductCard (ID: ${props.product.id}): User not logged in or product ID missing, isCollected set to false.`);
    }
    return;
  }
  try {
      const response = await getFavoriteStatus(props.product.id); // response 是 {code: ..., message: ..., data: true/false}
  
      // ▼▼▼▼▼ 修改这部分逻辑 ▼▼▼▼▼
      if (response && response.code === 200 && typeof response.data === 'boolean') {
        // 确保响应存在，业务码成功 (例如200)，并且 response.data 是一个布尔值
        isCollected.value = response.data; // 直接使用 response.data 作为收藏状态
        console.log(`ProductCard (ID: ${props.product.id}): API success, isCollected set to ${isCollected.value} from API`);
      } else {
        // 如果 response.code 不是 200，或者 response.data 不是布尔值，或者 response 本身不存在
        console.warn(`ProductCard (ID: ${props.product.id}): 获取收藏状态失败或数据格式不正确 (Code: ${response?.code}, Data Type: ${typeof response?.data}). Response:`, response);
        isCollected.value = false;
      }
	}catch (error) {
    console.error(`ProductCard (ID: ${props.product.id}): 请求收藏状态失败:`, error);
    isCollected.value = false;
  }
};

onMounted(() => {
  // onMounted 时尝试获取一次，如果此时未登录，watch 会处理后续登录的情况
  fetchInitialFavoriteStatus();
});

// 监听商品ID或登录状态的变化
watch(
  [() => props.product.id, isLoggedIn], // 监听这两个值的变化
  ([newProductId, currentIsLoggedIn], [oldProductId, oldIsLoggedIn]) => {
    console.log(`ProductCard (ID: ${newProductId || oldProductId}): Watch triggered. ProductID changed: ${newProductId !== oldProductId}, LoginStatus changed: ${currentIsLoggedIn !== oldIsLoggedIn}. Current Login: ${currentIsLoggedIn}`);

    if (currentIsLoggedIn && newProductId) {
      // 如果是登录状态刚变为true，或者商品ID变了（且已登录）
      if ((currentIsLoggedIn && !oldIsLoggedIn) || (newProductId !== oldProductId)) {
        fetchInitialFavoriteStatus();
      }
    } else if (!currentIsLoggedIn) {
      // 如果用户登出，则将收藏状态设为false
      isCollected.value = false;
      console.log(`ProductCard (ID: ${newProductId || oldProductId}): User logged out, isCollected set to false.`);
    }
  },
  { immediate: false } // onMounted 会处理初始加载，所以这里 immediate 可以是 false
                      // 如果 Pinia store 的 isLogin 状态初始化很快且可靠，也可考虑 immediate: true 并去掉 onMounted 中的直接调用
);

// 查看商品详情 (您的代码)
const viewDetail = () => {
  if (!props.product || typeof props.product !== 'object') {
    console.error("viewDetail: props.product 无效或未定义。", props.product);
    return;
  }
  const productId = props.product.id;
  if (productId === undefined || productId === null || String(productId).trim() === "") {
    console.error("viewDetail: props.product.id 无效或未定义。", productId);
    return;
  }
  const numericId = parseInt(productId);
  if (isNaN(numericId) || numericId <= 0) {
    console.error("viewDetail: props.product.id 不是一个有效的正整数ID。", productId, "转换后:", numericId);
    return;
  }
  router.push(`/product/${numericId}`);
};

// 添加到购物车
const addToCart = (e) => {
  e.stopPropagation(); // 阻止事件冒泡

  // 1. 检查登录状态
  if (!userStore.isLogin) { // 如果 userStore.isLogin 为 false
    ElMessage.warning('请先登录'); // 弹出提示
    router.push('/login');        // 跳转到登录页
    return;                       // 函数提前结束
  }

  // 2. 设置按钮加载状态
  addingToCart.value = true;

  try {
    // 3. 调用 cartStore 的 addToCart action
    cartStore.addToCart(props.product); // 关键点：这个 action 的内部实现决定是否有API请求
    ElMessage.success('已加入购物车');   // 如果上面没有抛出错误，则显示成功消息
  } catch (error) {
    ElMessage.error('添加失败，请重试'); // 如果 cartStore.addToCart() 抛出错误，则显示失败消息
  } finally {
    // 4. 清除按钮加载状态
    addingToCart.value = false;
  }
};

// 收藏商品 (您的代码)
const collectProduct = async (e) => {
  e.stopPropagation();
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  try {
    const newState = !isCollected.value;
    await toggleCollect(props.product.id, newState);
    isCollected.value = newState;
    ElMessage.success(newState ? '收藏成功' : '已取消收藏');
  } catch (error) {
    ElMessage.error('操作失败，请重试');
    // 发生错误时，可以考虑重新获取一次服务器状态以确保UI一致性
    // await fetchInitialFavoriteStatus();
  }
};
</script>
<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.product-card {
  width: 100%;
  background-color: #fff;
  border-radius: $border-radius-base;
  box-shadow: $box-shadow-light;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: $box-shadow-dark;
  }
  
  .product-image {
    width: 100%;
    height: 0;
    padding-bottom: 100%;
    position: relative;
    overflow: hidden;
    
    img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s;
    }
    
    &:hover img {
      transform: scale(1.05);
    }
    
    .discount-badge {
      position: absolute;
      top: 10px;
      right: 10px;
      background-color: $danger-color;
      color: #fff;
      padding: 2px 8px;
      border-radius: $border-radius-small;
      font-size: $font-size-small;
      font-weight: bold;
    }
  }
  
  .product-info {
    padding: 12px;
  }
  
  .product-name {
    margin: 0 0 10px;
    font-size: $font-size-base;
    color: $text-primary;
    height: 40px;
    line-height: 20px;
    @include multi-ellipsis(2);
  }
  
  .product-price {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    
    .current-price {
      font-size: $font-size-large;
      font-weight: bold;
      color: $danger-color;
      margin-right: 8px;
    }
    
    .original-price {
      font-size: $font-size-small;
      color: $text-secondary;
      text-decoration: line-through;
    }
  }
  
  .product-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    font-size: $font-size-small;
    color: $text-secondary;
    
    .product-rating {
      display: flex;
      align-items: center;
    }
  }
  
  .product-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 10px;
    border-top: 1px solid $border-lighter;
  }
}

@include respond-to(sm) {
  .product-card {
    .product-actions {
      flex-direction: column;
      gap: 10px;
      
      .el-button {
        width: 100%;
      }
    }
  }
}
</style>