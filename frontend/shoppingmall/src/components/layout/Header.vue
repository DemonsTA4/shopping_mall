<template>
  <header class="app-header">
    <div class="container">
      <div class="header-left">
        <router-link to="/" class="logo">
          <img src="@/assets/images/logo.png" alt="商城logo" />
          <h1>购物商城</h1>
        </router-link>
        
        <nav class="main-nav">
          <router-link to="/">首页</router-link>
          <router-link to="/products">全部商品</router-link>
          <router-link to="/products?category=new">新品上市</router-link>
          <router-link to="/products?category=hot">热卖商品</router-link>
        </nav>
      </div>
      
      <div class="header-right">
        <div class="search-box">
          <el-input 
            v-model="searchText" 
            placeholder="搜索商品" 
            clearable
            @keyup.enter="onSearch"
          >
            <template #append>
              <el-button icon="el-icon-search" @click="onSearch"></el-button>
            </template>
          </el-input>
        </div>
        
        <div class="user-actions">
          <router-link to="/cart" class="action-item cart-link">
            <el-badge :value="cartCount" :hidden="cartCount === 0">
              <i class="el-icon-shopping-cart-2"></i>
            </el-badge>
            <span>购物车</span>
          </router-link>
          
          <router-link to="/user/favorites" class="action-item">
            <i class="el-icon-star-off"></i>
            <span>收藏夹</span>
          </router-link>
          
          <template v-if="isLogin">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="action-item user-dropdown">
                <el-avatar :size="32" :src="userAvatar">{{ userInitial }}</el-avatar>
                <span>{{ nickname }}</span>
                <i class="el-icon-arrow-down"></i>
              </div>
              
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="aftersale">售后服务</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <template v-else>
            <router-link to="/login" class="action-item">
              <i class="el-icon-user"></i>
              <span>登录</span>
            </router-link>
            
            <router-link to="/register" class="action-item">
              <i class="el-icon-plus"></i>
              <span>注册</span>
            </router-link>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { useCartStore } from '@/store/modules/cart';

const router = useRouter();
const userStore = useUserStore();
const cartStore = useCartStore();
const searchText = ref('');

// 用户状态
const isLogin = computed(() => userStore.isLogin);
const nickname = computed(() => userStore.userInfo.nickname || userStore.userInfo.username || '用户');
const userAvatar = computed(() => userStore.userInfo.avatar || '');
const userInitial = computed(() => {
  const name = nickname.value;
  return name ? name.substring(0, 1).toUpperCase() : 'U';
});

// 购物车数量
const cartCount = computed(() => cartStore.cartItems.length);

// 搜索
const onSearch = () => {
  if (searchText.value.trim()) {
    router.push(`/products?keyword=${encodeURIComponent(searchText.value.trim())}`);
  }
};

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/user/profile');
  } else if (command === 'orders') {
    router.push('/order/list');
  } else if (command === 'favorites') {
    router.push('/user/favorites');
  } else if (command === 'aftersale') {
    router.push('/aftersale/list');
  } else if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  }
};

onMounted(() => {
  // 初始化时加载购物车数据
  cartStore.loadCart();
});
</script>

<style lang="scss" scoped>
.app-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  
  .container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-base 0;
  }
  
  .header-left {
    display: flex;
    align-items: center;
    
    .logo {
      display: flex;
      align-items: center;
      margin-right: $spacing-large;
      text-decoration: none;
      
      img {
        width: 40px;
        height: 40px;
        margin-right: $spacing-small;
      }
      
      h1 {
        font-size: $font-size-large;
        color: $primary-color;
        margin: 0;
      }
    }
    
    .main-nav {
      display: flex;
      gap: $spacing-large;
      
      a {
        color: $text-regular;
        text-decoration: none;
        font-size: $font-size-medium;
        transition: color 0.3s;
        
        &:hover, &.router-link-active {
          color: $primary-color;
        }
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    
    .search-box {
      width: 300px;
      margin-right: $spacing-large;
    }
    
    .user-actions {
      display: flex;
      align-items: center;
      gap: $spacing-base;
      
      .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        color: $text-regular;
        text-decoration: none;
        font-size: $font-size-small;
        padding: 0 $spacing-small;
        
        i {
          font-size: 20px;
          margin-bottom: 2px;
        }
        
        &:hover {
          color: $primary-color;
        }
      }
      
      .cart-link {
        position: relative;
      }
      
      .user-dropdown {
        flex-direction: row;
        gap: $spacing-small;
        cursor: pointer;
      }
    }
  }
}

@include respond-to(md) {
  .app-header {
    .container {
      flex-direction: column;
      
      .header-left {
        margin-bottom: $spacing-small;
      }
      
      .header-right {
        width: 100%;
        
        .search-box {
          width: 100%;
          margin-bottom: $spacing-small;
        }
        
        .user-actions {
          width: 100%;
          justify-content: space-around;
        }
      }
    }
  }
}

@include respond-to(sm) {
  .app-header {
    .header-left {
      .main-nav {
        display: none;
      }
    }
  }
}
</style> 