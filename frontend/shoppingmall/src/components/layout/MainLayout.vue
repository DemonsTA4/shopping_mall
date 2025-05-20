<template>
  <div class="main-layout">
    <header class="header">
      <div class="logo" @click="goToHome">购物平台</div>
      <div class="search-box">
        <el-input v-model="searchKeyword" placeholder="搜索商品" clearable @keyup.enter="search">
          <template #append>
            <el-button @click="search">
              <el-icon><search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/products">全部商品</router-link>
      </div>
      <div class="user-actions">
        <router-link to="/cart" class="cart-link">
          <el-badge :value="cartItemCount" :max="99" class="cart-badge">
            <el-icon><shopping-cart /></el-icon>
          </el-badge>
        </router-link>
        <template v-if="userStore.isLogin">
          <el-dropdown>
            <span class="user-dropdown">
              {{ userStore.userInfo.username }}
              <el-icon><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToUserCenter">个人中心</el-dropdown-item>
                <el-dropdown-item @click="goToOrders">我的订单</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </div>
    </header>

    <main class="main-content">
      <slot></slot>
    </main>

    <footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h3>关于我们</h3>
          <p>购物平台致力于为用户提供优质的购物体验。</p>
        </div>
        <div class="footer-section">
          <h3>客户服务</h3>
          <p>联系我们: support@example.com</p>
        </div>
        <div class="footer-section">
          <h3>支付方式</h3>
          <p>支持各种支付方式</p>
        </div>
      </div>
      <div class="copyright">
        © 2023 购物平台 版权所有
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ShoppingCart, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/modules/user'
import { useCartStore } from '../../store/modules/cart'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const { cartItemCount } = storeToRefs(cartStore)

const searchKeyword = ref('')

const search = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/products',
      query: { keyword: searchKeyword.value }
    })
  }
}

const goToHome = () => {
  router.push('/')
}

const goToUserCenter = () => {
  router.push('/user')
}

const goToOrders = () => {
  router.push('/orders')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-right: 30px;
  cursor: pointer;
}

.search-box {
  flex: 1;
  max-width: 500px;
  margin: 0 20px;
}

.nav-links {
  margin-left: 20px;
}

.nav-links a {
  margin: 0 10px;
  color: #333;
  text-decoration: none;
}

.nav-links a:hover {
  color: #409eff;
}

.user-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.user-actions a {
  margin: 0 10px;
  color: #333;
  text-decoration: none;
}

.user-actions a:hover {
  color: #409eff;
}

.cart-link {
  font-size: 20px;
  margin-right: 20px;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.main-content {
  flex: 1;
  background-color: #f5f5f5;
}

.footer {
  background-color: #f8f9fa;
  padding: 20px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
}

.footer-section {
  flex: 1;
  padding: 0 15px;
}

.footer-section h3 {
  margin-bottom: 10px;
}

.copyright {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}
</style>