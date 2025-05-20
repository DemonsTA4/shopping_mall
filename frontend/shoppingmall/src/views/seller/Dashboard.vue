<template>
  <div class="seller-dashboard">
    <h1 class="page-title">卖家中心</h1>
    
    <el-row :gutter="20">
      <!-- 统计卡片 -->
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
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayOrders }}</div>
          <div class="stat-title">今日订单</div>
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
        <el-button type="primary" @click="$router.push('/seller/product/add')">
          添加商品
        </el-button>
        <el-button @click="$router.push('/seller/products')">
          管理商品
        </el-button>
        <el-button @click="$router.push('/seller/orders')">
          订单管理
        </el-button>
      </div>
    </el-card>
    
    <!-- 最近订单 -->
    <el-card class="recent-orders-card">
      <template #header>
        <div class="card-header">
          <span>最近订单</span>
          <el-button text @click="$router.push('/seller/orders')">
            查看全部
          </el-button>
        </div>
      </template>
      
      <el-table :data="recentOrders" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column prop="customer" label="客户" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="viewOrder(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 统计数据
const stats = ref({
  totalProducts: 0,
  totalOrders: 0,
  totalSales: 0,
  todayOrders: 0
});

// 最近订单
const recentOrders = ref([]);

// 初始化数据
const initData = async () => {
  // 这里应该调用API获取数据
  // 现在使用模拟数据
  stats.value = {
    totalProducts: 120,
    totalOrders: 1589,
    totalSales: 125890,
    todayOrders: 12
  };
  
  recentOrders.value = [
    {
      id: 1001,
      orderNo: 'ORD20230412001',
      createTime: '2023-04-12 10:23:45',
      customer: '张三',
      amount: '¥299.00',
      status: 1,
      statusText: '待发货'
    },
    {
      id: 1002,
      orderNo: 'ORD20230412002',
      createTime: '2023-04-12 14:08:21',
      customer: '李四',
      amount: '¥1299.00',
      status: 2,
      statusText: '已发货'
    },
    {
      id: 1003,
      orderNo: 'ORD20230411005',
      createTime: '2023-04-11 16:45:33',
      customer: '王五',
      amount: '¥599.00',
      status: 3,
      statusText: '已完成'
    },
    {
      id: 1004,
      orderNo: 'ORD20230411003',
      createTime: '2023-04-11 09:12:05',
      customer: '赵六',
      amount: '¥1599.00',
      status: 4,
      statusText: '已取消'
    }
  ];
};

// 获取状态对应的标签类型
const getStatusType = (status) => {
  switch (status) {
    case 1: return 'warning';  // 待发货
    case 2: return 'primary';  // 已发货
    case 3: return 'success';  // 已完成
    case 4: return 'info';     // 已取消
    case 5: return 'danger';   // 退款/售后
    default: return 'info';
  }
};

// 查看订单详情
const viewOrder = (order) => {
  router.push(`/seller/order/${order.id}`);
};

onMounted(() => {
  initData();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.seller-dashboard {
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
  
  .action-card, .recent-orders-card {
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
  .seller-dashboard {
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