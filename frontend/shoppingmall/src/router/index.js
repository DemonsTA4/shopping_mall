import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
import Home from '../views/home/index.vue'
import Login from '../views/user/Login.vue'
import Register from '../views/user/Register.vue'
import ProductList from '../views/product/List.vue'
import ProductDetail from '../views/product/Detail.vue'
import Cart from '../views/cart/Index.vue'
import Checkout from '../views/order/Checkout.vue'
import OrderList from '../views/order/List.vue'
import OrderDetail from '../views/order/Detail.vue'
import OrderPay from '../views/order/Pay.vue'
import OrderComment from '../views/order/Comment.vue'
import UserProfile from '../views/user/Profile.vue'
import UserFavorites from '../views/user/Favorites.vue'
import AftersaleApply from '../views/aftersale/Apply.vue'
import AftersaleList from '../views/aftersale/List.vue'
import AftersaleDetail from '../views/aftersale/Detail.vue'
import SellerDashboard from '../views/seller/Dashboard.vue'
import SellerProducts from '../views/seller/Products.vue'
import SellerProductEdit from '../views/seller/ProductEdit.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetail
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: { requiresAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
    meta: { requiresAuth: true }
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: OrderList,
    meta: { requiresAuth: true }
  },
  {
    path: '/order/detail/:id',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/order/pay/:orderNo',
    name: 'OrderPay',
    component: OrderPay,
    meta: { requiresAuth: true }
  },
  {
    path: '/order/comment/:id',
    name: 'OrderComment',
    component: OrderComment,
    meta: { requiresAuth: true }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true }
  },
  {
    path: '/user/favorites',
    name: 'UserFavorites',
    component: UserFavorites,
    meta: { requiresAuth: true }
  },
  {
    path: '/aftersale/apply/:orderId',
    name: 'AftersaleApply',
    component: AftersaleApply,
    meta: { requiresAuth: true }
  },
  {
    path: '/aftersale/list',
    name: 'AftersaleList',
    component: AftersaleList,
    meta: { requiresAuth: true }
  },
  {
    path: '/aftersale/detail/:id',
    name: 'AftersaleDetail',
    component: AftersaleDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/seller',
    name: 'SellerDashboard',
    component: SellerDashboard,
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/seller/products',
    name: 'SellerProducts',
    component: SellerProducts,
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/seller/product/add',
    name: 'SellerProductAdd',
    component: SellerProductEdit,
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/seller/product/edit/:id',
    name: 'SellerProductEdit',
    component: SellerProductEdit,
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, role: 2 }
  },
  // 错误页面
  {
    path: '/error',
    name: 'Error',
    component: () => import('../views/error/Index.vue')
  },
  // 通配符路由，捕获所有未匹配的路由
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = parseInt(localStorage.getItem('userRole') || '0')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.role && to.meta.role !== userRole) {
    next('/')
  } else {
    next()
  }
})

export default router