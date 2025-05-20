<script setup>
import { computed, ref, onErrorCaptured, watch, onMounted } from 'vue';
import { useRouter, useRoute, RouterLink, RouterView } from 'vue-router';
import { useUserStore } from './store/modules/user';
import { useCartStore } from './store/modules/cart';
import { useI18n } from 'vue-i18n';
import { ShoppingCart } from '@element-plus/icons-vue';
//import { getCartItemCount } from '@/api/cart';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const cartStore = useCartStore();
const { t } = useI18n();
//const error = ref(null);

onMounted(async () => {
  console.log('App.vue onMounted: 开始初始化用户认证状态...');
  await userStore.initializeAuth(); // ★★★ 关键调用 ★★★
  console.log('App.vue onMounted: 用户认证状态初始化完成。');
  if (userStore.isLogin) {
      console.log('App.vue onMounted: 用户已登录，开始获取购物车...');
      await cartStore.fetchCart(); // ★★★ 用户登录后，获取购物车数据填充store ★★★
      console.log('App.vue onMounted: 购物车获取完成。CartStore items:', JSON.parse(JSON.stringify(cartStore.cartItems)));
      console.log('App.vue onMounted: CartStore totalItems getter:', cartStore.totalItems);
    }
    console.log('[App.vue onMounted] 初始化后的 userStore.isLogin:', userStore.isLogin);
});

// 计算属性：是否显示头部和底部
const showHeaderFooter = computed(() => {
  return !route.path.includes('/admin') && !route.path.includes('/seller') && 
         !route.path.includes('/error') && !route.path.includes('/not-found');
});

const cartBadgeCount = computed(() => cartStore.totalItems);

// 购物车商品数量
const cartItemCount = ref(0);

const isSeller = computed(() => {
  console.log('------------------------------------');
  console.log('[isSeller Computed] userStore.isLogin:', userStore.isLogin);
  console.log('[isSeller Computed] userStore.userInfo (raw):', userStore.userInfo);
  // 使用JSON.stringify/parse来避免直接打印proxy对象导致信息不全
  console.log('[isSeller Computed] userStore.userInfo (JSON):', JSON.parse(JSON.stringify(userStore.userInfo))); 
  
  if (userStore.userInfo) {
    console.log('[isSeller Computed] userStore.userInfo.role:', userStore.userInfo.role);
    console.log('[isSeller Computed] typeof userStore.userInfo.role:', typeof userStore.userInfo.role);
    console.log('[isSeller Computed] userStore.userInfo.role === 1 (数字严格比较):', userStore.userInfo.role === 1);
    console.log('[isSeller Computed] userStore.userInfo.role == 1 (数字非严格比较):', userStore.userInfo.role == 1);
    console.log('[isSeller Computed] String(userStore.userInfo.role) === "1" (字符串比较):', String(userStore.userInfo.role) === "1");
  }
  
  const result = userStore.isLogin && userStore.userInfo && userStore.userInfo.role === 1;
  console.log('[isSeller Computed] 最终计算结果 isSeller:', result);
  console.log('------------------------------------');
  return result;
});

// 监听用户登录状态变化
watch(() => userStore.isLogin, async (isLogin, oldIsLogin) => {
  console.log(`[App.vue] userStore.isLogin changed from ${oldIsLogin} to ${isLogin}`);
  if (isLogin && !oldIsLogin) { // 用户从登出变为登录
    console.log('[App.vue] 用户已登录，重新获取购物车...');
    await cartStore.fetchCart();
    console.log('[App.vue] 购物车获取完成。CartStore items:', JSON.parse(JSON.stringify(cartStore.cartItems)));
    console.log('[App.vue] CartStore totalItems getter:', cartStore.totalItems);
  } else if (!isLogin && oldIsLogin) { // 用户从登录变为登出
    console.log('[App.vue] 用户已登出，清空本地购物车store状态 (如果需要)...');
    // cartStore.clearCartClientSide(); // 如果有这样一个纯客户端清空方法
    // 或者 fetchCart 在未登录时会从 localStorage 加载，如果localStorage也被清除了，cartItems会变空
    await cartStore.fetchCart(); // 重新调用 fetchCart，它会在未登录时加载localStorage或清空
  }
});

// 获取购物车商品数量
const fetchCartItemCount = async () => {
  try {
    const res = await getCartItemCount();
    cartItemCount.value = res.data || 0;
  } catch (error) {
    console.error('获取购物车数量失败:', error);
    cartItemCount.value = 0;
  }
};

// 在组件挂载时，如果用户已登录则获取购物车数量
onMounted(async () => {
  console.log('[App.vue onMounted] Starting...');
  await userStore.initializeAuth();
  console.log('[App.vue onMounted] After initializeAuth. userStore.isLogin:', userStore.isLogin);
  if (userStore.isLogin) {
    console.log('[App.vue onMounted] User is logged in, calling cartStore.fetchCart().');
    await cartStore.fetchCart();
  } else {
    console.log('[App.vue onMounted] User is NOT logged in, cartStore.fetchCart() NOT called here.');
  }
});

// 捕获子组件中的错误
onErrorCaptured((err, instance, info) => {
  console.error('应用错误:', err, info);
  error.value = err;
  // 跳转到错误页面
  router.push('/error');
  // 阻止错误继续传播
  return false;
});
</script>

<template>
  <div class="app-container">
    <!-- 全局顶部导航 -->
    <header v-if="showHeaderFooter" class="app-header">
      <div class="header-container">
        <div class="left">
          <RouterLink to="/" class="logo">
            {{ t('common.appName') }}
          </RouterLink>
          <nav class="main-nav">
            <RouterLink to="/">{{ t('common.home') }}</RouterLink>
            <RouterLink to="/products">{{ t('common.products') }}</RouterLink>
          </nav>
        </div>
        
        <div class="right">
          <template v-if="userStore.isLogin">
            <RouterLink to="/cart" class="cart-link">
              <el-badge :value="cartBadgeCount" :hidden="cartBadgeCount === 0">
                <el-icon><ShoppingCart /></el-icon>
                {{ t('common.cart') }}
              </el-badge>
            </RouterLink>
            <el-dropdown>
              <span class="user-menu">
                <el-avatar 
                  :size="32" 
                  :src="userStore.userInfo?.avatar"
                >{{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) }}</el-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <RouterLink to="/user/profile">{{ t('common.profile') }}</RouterLink>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <RouterLink to="/order/list">{{ t('common.orders') }}</RouterLink>
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="userStore.logout">
                    {{ t('common.logout') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <template v-else>
            <div class="auth-links">
              <RouterLink to="/login" class="nav-link">{{ t('common.login') }}</RouterLink>
              <RouterLink to="/register" class="nav-link register-link">{{ t('common.register') }}</RouterLink>
            </div>
          </template>
        </div>
      </div>
    </header>

    <!-- 页面主体内容 -->
    <main class="app-main">
      <Suspense>
        <template #default>
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </template>
        <template #fallback>
          <div class="loading-container">
            <el-skeleton style="width: 100%" animated :rows="10" />
          </div>
        </template>
      </Suspense>
    </main>

    <!-- 全局底部 -->
    <footer v-if="showHeaderFooter" class="app-footer">
      <div class="footer-container">
        <div class="footer-links">
          <div class="footer-column">
            <h3>{{ t('footer.aboutUs') }}</h3>
            <a href="#">{{ t('footer.companyInfo') }}</a>
            <a href="#">{{ t('footer.contactUs') }}</a>
            <a href="#">{{ t('footer.joinUs') }}</a>
          </div>
          <div class="footer-column">
            <h3>{{ t('footer.helpCenter') }}</h3>
            <a href="#">{{ t('footer.shoppingGuide') }}</a>
            <a href="#">{{ t('footer.payment') }}</a>
            <a href="#">{{ t('footer.afterSales') }}</a>
          </div>
          <div class="footer-column">
            <h3>{{ t('footer.merchantServices') }}</h3>
            <a href="#">{{ t('footer.merchantSettlement') }}</a>
            <router-link v-if="isSeller" to="/seller/products">{{ t('footer.merchantCenter') }}</router-link>
            <a href="#">{{ t('footer.marketingCenter') }}</a>
          </div>
        </div>
        <div class="copyright">
          © 2023 {{ t('common.appName') }} {{ t('footer.allRightsReserved') }}
        </div>
      </div>
    </footer>
  </div>
</template>

<style lang="scss">
/* 重置与基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  font-size: 14px;
  color: #333;
  background-color: #f5f5f5;
}

a {
  text-decoration: none;
  color: #333;
  &:hover {
    color: #409EFF;
  }
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 头部样式 */
.app-header {
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .left {
      display: flex;
      align-items: center;
      gap: 40px;

      .logo {
        font-size: 24px;
        font-weight: bold;
        color: #409EFF;
      }

      .main-nav {
        display: flex;
        gap: 30px;

        a {
          font-size: 16px;
          position: relative;
          
          &.router-link-active {
            color: #409EFF;
            
            &:after {
              content: '';
              position: absolute;
              bottom: -19px;
              left: 0;
              right: 0;
              height: 2px;
              background-color: #409EFF;
            }
          }
        }
      }
    }

    .right {
      display: flex;
      align-items: center;
      gap: 24px;

      .cart-link {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 8px 12px;
        border-radius: 4px;
        transition: background-color 0.3s;

        &:hover {
          background-color: rgba(64, 158, 255, 0.1);
        }

        .el-icon {
          font-size: 20px;
        }
      }

      .auth-links {
        display: flex;
        align-items: center;
        gap: 16px;

        .nav-link {
          padding: 8px 16px;
          border-radius: 4px;
          transition: all 0.3s;

          &:hover {
            background-color: rgba(64, 158, 255, 0.1);
          }

          &.register-link {
            color: #fff;
            background-color: #409EFF;

            &:hover {
              background-color: #66b1ff;
            }
          }
        }
      }

      .user-menu {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 4px;
        transition: background-color 0.3s;

        &:hover {
          background-color: rgba(64, 158, 255, 0.1);
        }

        .username {
          max-width: 100px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}

/* 主体内容 */
.app-main {
  flex: 1;
  margin: 20px auto;
  max-width: 1200px;
  width: 100%;
  padding: 0 20px;
}

/* 底部样式 */
.app-footer {
  background-color: #f8f8f8;
  padding: 40px 0;
  margin-top: 40px;
  
  .footer-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
  
  .footer-links {
    display: flex;
    justify-content: space-between;
    margin-bottom: 30px;
    
    .footer-column {
      display: flex;
      flex-direction: column;
      gap: 10px;
      
      h3 {
        font-size: 16px;
        margin-bottom: 15px;
        font-weight: bold;
      }
      
      a {
        color: #666;
        &:hover {
          color: #409EFF;
        }
      }
    }
  }
  
  .copyright {
    text-align: center;
    color: #999;
    padding-top: 20px;
    border-top: 1px solid #e0e0e0;
  }
}

/* 加载状态样式 */
.loading-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>
