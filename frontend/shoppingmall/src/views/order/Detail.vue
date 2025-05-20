<template>
  <div class="order-detail-container">
    <h1 class="page-title">订单详情</h1>
    
    <el-card v-if="!loading">
      <!-- 订单状态 -->
      <div class="status-section">
        <div class="status-info">
          <el-tag :type="getStatusType(order.status)" size="large">
            {{ getStatusText(order.status) }}
          </el-tag>
          <p class="status-desc">
            {{ getStatusDescription(order.status) }}
          </p>
        </div>
        
        <div class="order-actions">
          <el-button 
            v-if="order.status === 1" 
            type="primary"
            @click="goToPay"
          >
            立即付款
          </el-button>
          
          <el-button 
            v-if="order.status === 1" 
            type="danger" 
            plain
            @click="cancelOrder"
          >
            取消订单
          </el-button>
          
          <el-button 
            v-if="order.status === 3" 
            type="success"
            @click="confirmReceipt"
          >
            确认收货
          </el-button>
        </div>
      </div>
      
      <!-- 收货信息 -->
      <div class="section">
        <h2 class="section-title">收货信息</h2>
        <div class="info-item">
          <span class="label">收货人：</span>
          <span class="value">{{ order.address?.name }}</span>
        </div>
        <div class="info-item">
          <span class="label">联系电话：</span>
          <span class="value">{{ order.address?.phone }}</span>
        </div>
        <div class="info-item">
          <span class="label">收货地址：</span>
          <span class="value">{{ formatAddress(order.address) }}</span>
        </div>
      </div>
      
      <!-- 订单信息 -->
      <div class="section">
        <h2 class="section-title">订单信息</h2>
        <div class="info-item">
          <span class="label">订单编号：</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ order.createTime }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付方式：</span>
          <span class="value">{{ getPaymentMethodText(order.paymentMethod) }}</span>
        </div>
        <div class="info-item" v-if="order.payTime">
          <span class="label">支付时间：</span>
          <span class="value">{{ order.payTime }}</span>
        </div>
        <div class="info-item" v-if="order.remark">
          <span class="label">订单备注：</span>
          <span class="value">{{ order.remark }}</span>
        </div>
      </div>
      
      <!-- 商品清单 -->
      <div class="section">
        <h2 class="section-title">商品清单</h2>
        <div class="product-list">
          <div class="table-header">
            <div class="col product-col">商品</div>
            <div class="col price-col">单价</div>
            <div class="col quantity-col">数量</div>
            <div class="col subtotal-col">小计</div>
          </div>
          
          <div class="table-body">
            <div 
              v-for="item in order.items" 
              :key="item.id" 
              class="product-item"
            >
              <div class="col product-col">
                <div class="product-info">
                  <el-image 
                    :src="item.imageUrl" 
                    :alt="item.name"
                    class="product-image"
                    @click="goToProduct(item.productId)"
                  />
                  <div class="product-name" @click="goToProduct(item.productId)">
                    {{ item.name }}
                  </div>
                </div>
              </div>
              <div class="col price-col">¥{{ item.price }}</div>
              <div class="col quantity-col">{{ item.quantity }}</div>
              <div class="col subtotal-col">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 金额信息 -->
      <div class="amount-section">
        <div class="amount-item">
          <span class="label">商品总价：</span>
          <span class="value">¥{{ order.totalPrice }}</span>
        </div>
        <div class="amount-item">
          <span class="label">运费：</span>
          <span class="value">¥{{ order.shippingFee }}</span>
        </div>
        <div class="amount-item total">
          <span class="label">实付款：</span>
          <span class="value">¥{{ order.totalAmount }}</span>
        </div>
      </div>
      
      <!-- 物流信息 -->
      <div v-if="order.status >= 2 && order.logistics" class="section logistics-section">
        <h2 class="section-title">物流信息</h2>
        <div class="logistics-info">
          <div class="info-item">
            <span class="label">物流公司：</span>
            <span class="value">{{ order.logistics.company }}</span>
          </div>
          <div class="info-item">
            <span class="label">物流单号：</span>
            <span class="value">{{ order.logistics.trackingNo }}</span>
            <el-button type="primary" link @click="copyTrackingNumber(order.logistics.trackingNo)">复制</el-button>
          </div>
          <div class="info-item" v-if="order.logistics.sendTime">
            <span class="label">发货时间：</span>
            <span class="value">{{ order.logistics.sendTime }}</span>
          </div>
        </div>
        
        <div class="logistics-status">
          <div class="current-status">
            <el-tag :type="getLogisticsStatusType(order.logistics.status)">
              {{ getLogisticsStatusText(order.logistics.status) }}
            </el-tag>
          </div>
          <div class="status-desc">
            {{ getLogisticsDesc(order.logistics.status) }}
          </div>
        </div>
        
        <div class="logistics-traces">
          <h3 class="sub-title">物流跟踪</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(trace, index) in order.logistics.traces"
              :key="index"
              :timestamp="trace.time"
              :type="index === 0 ? 'primary' : ''"
            >
              {{ trace.content }}
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <div class="logistics-map" v-if="order.logistics.location">
          <h3 class="sub-title">物流位置</h3>
          <div class="map-container">
            <!-- 在实际项目中，这里可以集成地图API显示物流当前位置 -->
            <div class="map-placeholder">
              <i class="el-icon-location"></i>
              <div>当前位置：{{ order.logistics.location.city }} {{ order.logistics.location.district }}</div>
            </div>
          </div>
        </div>
        
        <div class="logistics-actions">
          <el-button type="primary" plain @click="refreshLogistics">刷新物流</el-button>
          <el-button type="info" plain @click="goToLogisticsWebsite(order.logistics)">前往物流官网查询</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrderDetail, cancelOrder as cancelOrderApi, confirmReceipt as confirmReceiptApi } from '@/api/order';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const order = ref({});

// 获取订单详情
const fetchOrderDetail = async () => {
  const orderId = route.params.id;
  if (!orderId) {
    ElMessage.error('订单ID不能为空');
    router.push('/order/list');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await getOrderDetail(orderId);
    order.value = res.data || {};
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败，请重试');
    
    // 使用模拟数据
    mockOrderDetail(orderId);
  } finally {
    loading.value = false;
  }
};

// 模拟订单详情数据
const mockOrderDetail = (orderId) => {
  order.value = {
    id: orderId,
    orderNo: `ORDER${Date.now()}`,
    status: 2,
    createTime: '2023-05-15 14:30:25',
    payTime: '2023-05-15 14:35:10',
    paymentMethod: 'alipay',
    totalPrice: '1199.00',
    shippingFee: '0.00',
    totalAmount: '1199.00',
    remark: '请尽快发货，谢谢',
    address: {
      name: '张三',
      phone: '13800138000',
      province: '浙江省',
      city: '杭州市',
      district: '西湖区',
      detail: '古荡街道1号'
    },
    items: [
      {
        id: 1,
        productId: 101,
        name: '测试商品1',
        price: 599.00,
        quantity: 1,
        imageUrl: 'https://picsum.photos/id/1/100/100'
      },
      {
        id: 2,
        productId: 102,
        name: '测试商品2',
        price: 600.00,
        quantity: 1,
        imageUrl: 'https://picsum.photos/id/2/100/100'
      }
    ]
  };
  
  // 如果订单状态为待收货或待评价，添加物流信息
  if (order.value.status >= 2) {
    order.value.logistics = {
      company: '顺丰速运',
      trackingNo: `SF${Math.floor(Math.random() * 10000000000)}`,
      sendTime: '2023-05-16 10:20:35',
      status: order.value.status >= 4 ? 4 : order.value.status,
      location: {
        province: '浙江省',
        city: '杭州市',
        district: '西湖区'
      },
      traces: [
        {
          time: '2023-05-16 10:20:35',
          content: '【杭州市】快件已由商家发出，等待揽收'
        },
        {
          time: '2023-05-16 15:30:22',
          content: '【杭州市】快件已被顺丰速运揽收'
        },
        {
          time: '2023-05-16 18:45:10',
          content: '【杭州市】快件离开杭州中转中心，发往上海中转中心'
        },
        {
          time: '2023-05-17 02:30:45',
          content: '【上海市】快件到达上海中转中心'
        },
        {
          time: '2023-05-17 08:15:20',
          content: '【上海市】快件离开上海中转中心，发往上海徐汇区'
        },
        {
          time: '2023-05-17 10:40:10',
          content: '【上海市】快件到达上海徐汇区，准备派送'
        },
        {
          time: '2023-05-17 13:20:30',
          content: '【上海市】快递员正在派送途中，请保持电话畅通'
        }
      ]
    };
    
    // 如果订单已完成，添加签收信息
    if (order.value.status >= 4) {
      order.value.logistics.traces.unshift({
        time: '2023-05-17 16:30:00',
        content: '【上海市】快件已签收，签收人：本人，感谢您使用顺丰速运'
      });
    }
  }
};

// 获取状态文字
const getStatusText = (status) => {
  const statusMap = {
    1: '待付款',
    2: '待发货',
    3: '待收货',
    4: '待评价',
    5: '已完成',
    6: '已取消',
    7: '退款中',
    8: '已退款'
  };
  
  return statusMap[status] || '未知状态';
};

// 获取状态描述
const getStatusDescription = (status) => {
  const descMap = {
    1: '请在24小时内完成支付，超时订单将自动取消',
    2: '商家正在处理您的订单，请耐心等待',
    3: '商品已发出，请注意查收',
    4: '您已收到商品，期待您的评价',
    5: '订单已完成',
    6: '订单已取消',
    7: '退款申请处理中',
    8: '退款已完成'
  };
  
  return descMap[status] || '';
};

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'danger',
    2: 'warning',
    3: 'primary',
    4: 'success',
    5: 'info',
    6: 'info',
    7: 'warning',
    8: 'info'
  };
  
  return typeMap[status] || 'info';
};

// 获取支付方式文字
const getPaymentMethodText = (method) => {
  const methodMap = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'credit': '信用卡',
    'cash': '货到付款'
  };
  
  return methodMap[method] || '未知支付方式';
};

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '';
  return `${address.province || ''} ${address.city || ''} ${address.district || ''} ${address.detail || ''}`;
};

// 跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/product/${productId}`);
};

// 跳转到支付页面
const goToPay = () => {
  router.push(`/order/pay/${order.value.orderNo}`);
};

// 取消订单
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm(
      '您确定要取消该订单吗？取消后，订单将无法恢复。',
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await cancelOrderApi(order.value.id, { reason: '用户取消' });
    ElMessage.success('订单已取消');
    fetchOrderDetail();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error);
      ElMessage.error('取消失败，请重试');
    }
  }
};

// 确认收货
const confirmReceipt = async () => {
  try {
    await ElMessageBox.confirm(
      '您确定已收到商品吗？',
      '确认收货',
      {
        confirmButtonText: '确认收货',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await confirmReceiptApi(order.value.id);
    ElMessage.success('确认收货成功');
    fetchOrderDetail();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error);
      ElMessage.error('操作失败，请重试');
    }
  }
};

// 获取物流状态文字
const getLogisticsStatusText = (status) => {
  const statusMap = {
    1: '已发货',
    2: '运输中',
    3: '派送中',
    4: '已签收',
    5: '异常'
  };
  
  return statusMap[status] || '未知状态';
};

// 获取物流状态标签类型
const getLogisticsStatusType = (status) => {
  const typeMap = {
    1: 'info',
    2: 'warning',
    3: 'primary',
    4: 'success',
    5: 'danger'
  };
  
  return typeMap[status] || 'info';
};

// 获取物流状态描述
const getLogisticsDesc = (status) => {
  const descMap = {
    1: '商家已发货，等待物流公司揽收',
    2: '包裹正在运往目的地',
    3: '包裹已到达您的城市，正在派送',
    4: '包裹已成功签收',
    5: '物流异常，请联系客服'
  };
  
  return descMap[status] || '';
};

// 复制物流单号
const copyTrackingNumber = (number) => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(number)
      .then(() => {
        ElMessage.success('物流单号已复制到剪贴板');
      })
      .catch(() => {
        ElMessage.error('复制失败，请手动复制');
      });
  } else {
    ElMessage.error('您的浏览器不支持此功能，请手动复制');
  }
};

// 刷新物流信息
const refreshLogistics = async () => {
  try {
    const res = await getOrderLogistics(order.value.id);
    if (res.data && res.data.traces) {
      order.value.logistics = res.data;
      ElMessage.success('物流信息已更新');
    }
  } catch (error) {
    console.error('获取物流信息失败:', error);
    ElMessage.error('获取物流信息失败，请重试');
  }
};

// 前往物流官网
const goToLogisticsWebsite = (logistics) => {
  const websiteMap = {
    '顺丰速运': 'https://www.sf-express.com/cn/sc/dynamic_function/waybill/',
    '中通快递': 'https://www.zto.com/express/expressCheck.html',
    '圆通速递': 'https://www.yto.net.cn/homedoc/quick_search/waybill_info.htm',
    '韵达快递': 'https://www.yundaex.com/cn/gzcx.php',
    '申通快递': 'https://www.sto.cn/query/sto-express-no',
    'EMS': 'https://www.ems.com.cn/queryService.html',
    '京东物流': 'https://www.jdwl.com/order/search'
  };
  
  const url = websiteMap[logistics.company] || 'https://www.kuaidi100.com/';
  window.open(url, '_blank');
};

onMounted(() => {
  fetchOrderDetail();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.order-detail-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .status-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-base;
    background-color: #f8f8f8;
    border-radius: $border-radius-base;
    margin-bottom: $spacing-large;
    
    .status-info {
      .status-desc {
        margin-top: $spacing-small;
        color: $text-secondary;
      }
    }
    
    .order-actions {
      display: flex;
      gap: $spacing-small;
    }
  }
  
  .section {
    margin-bottom: $spacing-large;
    
    .section-title {
      font-size: $font-size-large;
      margin-bottom: $spacing-base;
      padding-bottom: $spacing-small;
      border-bottom: 1px solid $border-lighter;
    }
    
    .info-item {
      margin-bottom: $spacing-small;
      
      .label {
        color: $text-secondary;
        margin-right: $spacing-small;
      }
      
      .value {
        color: $text-primary;
      }
    }
  }
  
  .product-list {
    .table-header {
      display: flex;
      background-color: #f5f7fa;
      padding: $spacing-small $spacing-base;
      font-weight: bold;
    }
    
    .product-item {
      display: flex;
      padding: $spacing-base;
      border-bottom: 1px solid $border-lighter;
      
      &:last-child {
        border-bottom: none;
      }
    }
    
    .col {
      display: flex;
      align-items: center;
    }
    
    .product-col {
      flex: 1;
      
      .product-info {
        display: flex;
        align-items: center;
        
        .product-image {
          width: 60px;
          height: 60px;
          margin-right: $spacing-base;
          cursor: pointer;
        }
        
        .product-name {
          flex: 1;
          cursor: pointer;
          
          &:hover {
            color: $primary-color;
          }
        }
      }
    }
    
    .price-col {
      width: 100px;
      justify-content: center;
    }
    
    .quantity-col {
      width: 80px;
      justify-content: center;
    }
    
    .subtotal-col {
      width: 100px;
      justify-content: center;
      font-weight: bold;
    }
  }
  
  .amount-section {
    text-align: right;
    padding: $spacing-base;
    background-color: #f5f7fa;
    border-radius: $border-radius-base;
    
    .amount-item {
      margin-bottom: $spacing-small;
      
      &.total {
        margin-top: $spacing-base;
        font-size: $font-size-large;
        font-weight: bold;
      }
      
      .label {
        color: $text-regular;
      }
      
      .value {
        color: $danger-color;
        font-weight: bold;
      }
    }
  }
  
  .logistics-section {
    .logistics-info {
      margin-bottom: $spacing-base;
    }
    
    .logistics-status {
      margin-bottom: $spacing-base;
      padding: $spacing-base;
      background-color: #f8f8f8;
      border-radius: $border-radius-base;
      
      .current-status {
        margin-bottom: $spacing-small;
      }
      
      .status-desc {
        color: $text-secondary;
      }
    }
    
    .sub-title {
      font-size: $font-size-medium;
      margin-bottom: $spacing-base;
    }
    
    .logistics-traces {
      margin-bottom: $spacing-base;
    }
    
    .map-container {
      height: 200px;
      background-color: #f8f8f8;
      border-radius: $border-radius-base;
      margin-bottom: $spacing-base;
      display: flex;
      justify-content: center;
      align-items: center;
      
      .map-placeholder {
        text-align: center;
        color: $text-secondary;
        
        i {
          font-size: 48px;
          margin-bottom: $spacing-small;
        }
      }
    }
    
    .logistics-actions {
      display: flex;
      gap: $spacing-small;
    }
  }
}

@include respond-to(md) {
  .order-detail-container {
    .status-section {
      flex-direction: column;
      align-items: flex-start;
      
      .status-info {
        margin-bottom: $spacing-base;
      }
    }
  }
}

@include respond-to(sm) {
  .order-detail-container {
    .product-list {
      .table-header {
        display: none;
      }
      
      .product-item {
        flex-wrap: wrap;
        
        .product-col {
          width: 100%;
          margin-bottom: $spacing-small;
        }
        
        .price-col, .quantity-col, .subtotal-col {
          width: 33.33%;
          justify-content: flex-start;
          
          &::before {
            content: attr(data-label);
            margin-right: $spacing-small;
            color: $text-secondary;
          }
        }
      }
    }
  }
}
</style> 