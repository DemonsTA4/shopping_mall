<template>
  <div class="admin-dashboard">
    <h1 class="page-title">管理员控制台</h1>
    
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-title">用户总数</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalProducts }}</div>
          <div class="stat-title">商品总数</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalOrders }}</div>
          <div class="stat-title">订单总数</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-value">¥{{ stats.totalSales }}</div>
          <div class="stat-title">销售总额</div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快捷操作 -->
    <el-card class="action-card">
      <template #header>
        <div class="card-header">
          <span>快捷操作</span>
        </div>
      </template>
      
      <div class="action-buttons">
        <el-button type="primary" @click="$router.push('/admin/users')">
          用户管理
        </el-button>
        <el-button @click="$router.push('/admin/products')">
          商品管理
        </el-button>
        <el-button @click="$router.push('/admin/orders')">
          订单管理
        </el-button>
        <el-button @click="$router.push('/admin/categories')">
          分类管理
        </el-button>
        <el-button @click="$router.push('/admin/settings')">
          系统设置
        </el-button>
      </div>
    </el-card>
    
    <!-- 系统状态 -->
    <el-card class="system-card">
      <template #header>
        <div class="card-header">
          <span>系统状态</span>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="服务器状态" label-align="center">
          <el-tag type="success">运行中</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="系统版本" label-align="center">
          v1.0.0
        </el-descriptions-item>
        <el-descriptions-item label="今日注册用户" label-align="center">
          {{ stats.todayUsers }}
        </el-descriptions-item>
        <el-descriptions-item label="今日订单数" label-align="center">
          {{ stats.todayOrders }}
        </el-descriptions-item>
        <el-descriptions-item label="待处理售后" label-align="center">
          {{ stats.pendingAftersales }}
        </el-descriptions-item>
        <el-descriptions-item label="待发货订单" label-align="center">
          {{ stats.pendingShipments }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 最近注册用户 -->
    <el-card class="recent-users-card">
      <template #header>
        <div class="card-header">
          <span>最近注册用户</span>
          <el-button text @click="$router.push('/admin/users')">
            查看全部
          </el-button>
        </div>
      </template>
      
      <el-table :data="recentUsers" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === 0 ? 'info' : (scope.row.role === 1 ? 'warning' : 'success')">
              {{ scope.row.role === 0 ? '普通用户' : (scope.row.role === 1 ? '卖家' : '管理员') }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 统计数据
const stats = ref({
  totalUsers: 0,
  totalProducts: 0,
  totalOrders: 0,
  totalSales: 0,
  todayUsers: 0,
  todayOrders: 0,
  pendingAftersales: 0,
  pendingShipments: 0
});

// 最近注册用户
const recentUsers = ref([]);

// 初始化数据
const initData = async () => {
  // 这里应该调用API获取数据
  // 现在使用模拟数据
  stats.value = {
    totalUsers: 1286,
    totalProducts: 3589,
    totalOrders: 12576,
    totalSales: 982560,
    todayUsers: 23,
    todayOrders: 156,
    pendingAftersales: 12,
    pendingShipments: 45
  };
  
  recentUsers.value = [
    {
      id: 1001,
      username: 'zhangsan',
      email: 'zhangsan@example.com',
      registerTime: '2023-04-12 10:23:45',
      role: 0
    },
    {
      id: 1002,
      username: 'lisi',
      email: 'lisi@example.com',
      registerTime: '2023-04-12 14:08:21',
      role: 1
    },
    {
      id: 1003,
      username: 'wangwu',
      email: 'wangwu@example.com',
      registerTime: '2023-04-11 16:45:33',
      role: 0
    },
    {
      id: 1004,
      username: 'zhaoliu',
      email: 'zhaoliu@example.com',
      registerTime: '2023-04-11 09:12:05',
      role: 0
    },
    {
      id: 1005,
      username: 'adminuser',
      email: 'admin@example.com',
      registerTime: '2023-04-10 11:32:18',
      role: 2
    }
  ];
};

onMounted(() => {
  initData();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.admin-dashboard {
  .stat-card {
    margin-bottom: $spacing-base;
    text-align: center;
    
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: $primary-color;
      margin-bottom: $spacing-small;
    }
    
    .stat-title {
      font-size: $font-size-base;
      color: $text-secondary;
    }
  }
  
  .action-card, .system-card, .recent-users-card {
    margin-bottom: $spacing-large;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .action-buttons {
    display: flex;
    gap: $spacing-base;
    flex-wrap: wrap;
  }
}

@include respond-to(sm) {
  .admin-dashboard {
    .stat-card {
      margin-bottom: $spacing-base;
    }
    
    .action-buttons {
      flex-direction: column;
      
      .el-button {
        width: 100%;
        margin-left: 0;
      }
    }
  }
}
</style> 