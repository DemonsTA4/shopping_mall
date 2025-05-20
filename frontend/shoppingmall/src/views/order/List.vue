<template>
  <div class="order-list-container">
    <h1 class="page-title">我的订单</h1>
    
    <!-- 订单状态选项卡 -->
    <el-tabs v-model="activeStatus" @tab-click="handleStatusChange">
      <el-tab-pane label="全部订单" name="0"></el-tab-pane>
      <el-tab-pane label="待付款" name="1"></el-tab-pane>
      <el-tab-pane label="待发货" name="2"></el-tab-pane>
      <el-tab-pane label="待收货" name="3"></el-tab-pane>
      <el-tab-pane label="待评价" name="4"></el-tab-pane>
    </el-tabs>
    
    <!-- 订单列表 -->
    <el-card v-if="!loading">
      <div v-if="orderList.length === 0" class="empty-orders">
        <el-empty description="暂无相关订单" />
      </div>
      
      <div v-else class="order-list">
        <div 
          v-for="order in orderList" 
          :key="order.id" 
          class="order-item"
        >
          <div class="order-header">
            <div class="order-info">
              <span class="order-time">{{ order.createTime }}</span>
              <span class="order-no">订单号：{{ order.orderNo }}</span>
            </div>
            <div class="order-status">
              <el-tag :type="getStatusType(order.status)">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>
          
          <div class="order-products">
            <div 
              v-for="item in order.items" 
              :key="item.id" 
              class="product-item"
              @click="goToProductDetail(item.productId)"
            >
              <el-image 
                :src="item.imageUrl" 
                :alt="item.name"
                class="product-image"
              />
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-price">
                  <span class="price">¥{{ item.price }}</span>
                  <span class="quantity">x {{ item.quantity }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-amount">
              <span class="label">实付款：</span>
              <span class="amount">¥{{ order.totalAmount }}</span>
            </div>
            
            <div class="order-actions">
              <el-button 
                v-if="order.status === 1" 
                type="primary" 
                size="small"
                @click="goToPay(order)"
              >
                立即付款
              </el-button>
              
              <el-button 
                v-if="order.status === 1" 
                type="danger" 
                plain
                size="small"
                @click="cancelOrder(order)"
              >
                取消订单
              </el-button>
              
              <el-button 
                v-if="order.status === 3" 
                type="success" 
                size="small"
                @click="confirmReceipt(order)"
              >
                确认收货
              </el-button>
              
              <el-button 
                v-if="order.status === 4" 
                type="primary" 
                plain
                size="small"
                @click="goToComment(order)"
              >
                去评价
              </el-button>
              
              <el-button 
                v-if="order.status === 2 || order.status === 3" 
                type="warning" 
                plain
                size="small"
                @click="showLogistics(order)"
              >
                查看物流
              </el-button>
              
              <el-button 
                v-if="[2, 3, 4, 5].includes(order.status)" 
                type="danger" 
                plain
                size="small"
                @click="applyAftersale(order)"
              >
                申请售后
              </el-button>
              
              <el-button 
                type="info" 
                plain
                size="small"
                @click="goToOrderDetail(order)"
              >
                订单详情
              </el-button>
              
              <el-button 
                v-if="order.status === 5 || order.status === 6" 
                type="danger" 
                plain
                size="small"
                @click="deleteOrder(order)"
              >
                删除订单
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>
    
    <!-- 取消订单对话框 -->
    <el-dialog
      v-model="cancelDialog.visible"
      title="取消订单"
      width="500px"
    >
      <div class="cancel-form">
        <p>您确定要取消该订单吗？取消后，订单将无法恢复。</p>
        <el-form ref="cancelFormRef" :model="cancelDialog.form" label-width="80px">
          <el-form-item label="取消原因" prop="reason">
            <el-select v-model="cancelDialog.form.reason" placeholder="请选择取消原因" style="width: 100%">
              <el-option label="我不想买了" value="我不想买了" />
              <el-option label="信息填写错误，重新拍" value="信息填写错误，重新拍" />
              <el-option label="商品降价" value="商品降价" />
              <el-option label="商品缺货" value="商品缺货" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="cancelDialog.visible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmCancelOrder" 
          :loading="cancelDialog.loading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 退款申请对话框 -->
    <el-dialog
      v-model="refundDialog.visible"
      title="申请退款"
      width="500px"
    >
      <div class="refund-form">
        <el-form ref="refundFormRef" :model="refundDialog.form" label-width="80px">
          <el-form-item label="退款原因" prop="reason">
            <el-select v-model="refundDialog.form.reason" placeholder="请选择退款原因" style="width: 100%">
              <el-option label="不想要了" value="不想要了" />
              <el-option label="商品有问题" value="商品有问题" />
              <el-option label="商品与描述不符" value="商品与描述不符" />
              <el-option label="质量问题" value="质量问题" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="问题描述" prop="description">
            <el-input 
              v-model="refundDialog.form.description" 
              type="textarea" 
              :rows="4"
              placeholder="请详细描述您遇到的问题"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="refundDialog.visible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmRefund" 
          :loading="refundDialog.loading"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 物流信息对话框 -->
    <el-dialog
      v-model="logisticsDialog.visible"
      title="物流信息"
      width="600px"
    >
      <div class="logistics-info">
        <div class="logistics-header">
          <div class="info-item">
            <span class="label">物流公司：</span>
            <span class="value">{{ logisticsDialog.data.company }}</span>
          </div>
          <div class="info-item">
            <span class="label">物流单号：</span>
            <span class="value">{{ logisticsDialog.data.trackingNo }}</span>
            <el-button type="primary" link @click="copyTrackingNumber(logisticsDialog.data.trackingNo)">复制</el-button>
          </div>
          <div class="info-item" v-if="logisticsDialog.data.sendTime">
            <span class="label">发货时间：</span>
            <span class="value">{{ logisticsDialog.data.sendTime }}</span>
          </div>
        </div>
        
        <div class="logistics-status" v-if="logisticsDialog.data.status">
          <div class="current-status">
            <el-tag :type="getLogisticsStatusType(logisticsDialog.data.status)">
              {{ getLogisticsStatusText(logisticsDialog.data.status) }}
            </el-tag>
          </div>
          <div class="status-desc">
            {{ getLogisticsDesc(logisticsDialog.data.status) }}
          </div>
        </div>
        
        <div class="logistics-map" v-if="logisticsDialog.data.location">
          <h3 class="sub-title">当前位置</h3>
          <div>
            {{ logisticsDialog.data.location.province }} 
            {{ logisticsDialog.data.location.city }} 
            {{ logisticsDialog.data.location.district }}
          </div>
        </div>
        
        <div class="logistics-timeline">
          <h3 class="sub-title">物流跟踪</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in logisticsDialog.data.traces"
              :key="index"
              :timestamp="item.time"
              :type="index === 0 ? 'primary' : ''"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <div class="dialog-footer">
          <el-button @click="refreshLogistics">刷新物流</el-button>
          <el-button type="primary" @click="goToLogisticsWebsite(logisticsDialog.data)">
            前往物流官网查询
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrders, cancelOrder as cancelOrderApi, confirmReceipt as confirmReceiptApi, deleteOrder as deleteOrderApi, applyRefund as applyRefundApi } from '@/api/order';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const activeStatus = ref('0');
const orderList = ref([]);
const total = ref(0);

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  status: 0
});

// 取消订单对话框
const cancelDialog = reactive({
  visible: false,
  loading: false,
  orderId: null,
  form: {
    reason: ''
  }
});

// 退款对话框
const refundDialog = reactive({
  visible: false,
  loading: false,
  orderId: null,
  form: {
    reason: '',
    description: ''
  }
});

// 物流信息对话框
const logisticsDialog = reactive({
  visible: false,
  data: {
    company: '',
    trackingNo: '',
    traces: []
  }
});

// 获取订单列表
const fetchOrderList = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await getOrders(queryParams);
    orderList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error('获取订单列表失败:', error);
    ElMessage.error('获取订单列表失败，请重试');
    
    // 使用模拟数据
    mockOrderList();
  } finally {
    loading.value = false;
  }
};

// 模拟订单数据
const mockOrderList = () => {
  const list = [];
  const status = parseInt(activeStatus.value);
  
  for (let i = 1; i <= queryParams.pageSize; i++) {
    // 如果选择了特定状态，只显示该状态的订单
    const orderStatus = status === 0 ? (i % 6) + 1 : status;
    
    list.push({
      id: i,
      orderNo: `ORDER${Date.now()}${i}`,
      status: orderStatus,
      totalAmount: (Math.random() * 1000 + 100).toFixed(2),
      createTime: new Date(Date.now() - i * 86400000).toISOString().split('T')[0],
      items: [
        {
          id: i * 100 + 1,
          productId: i * 10 + 1,
          name: `测试商品 ${i * 10 + 1}`,
          price: (Math.random() * 100 + 50).toFixed(2),
          quantity: Math.floor(Math.random() * 3) + 1,
          imageUrl: `https://picsum.photos/id/${i * 10 + 1}/100/100`
        },
        {
          id: i * 100 + 2,
          productId: i * 10 + 2,
          name: `测试商品 ${i * 10 + 2}`,
          price: (Math.random() * 200 + 100).toFixed(2),
          quantity: Math.floor(Math.random() * 2) + 1,
          imageUrl: `https://picsum.photos/id/${i * 10 + 2}/100/100`
        }
      ]
    });
  }
  
  orderList.value = list;
  total.value = 100;
};

// 处理状态变化
const handleStatusChange = () => {
  queryParams.status = parseInt(activeStatus.value);
  queryParams.page = 1;
  fetchOrderList();
};

// 处理页码变化
const handleCurrentChange = (page) => {
  queryParams.page = page;
  fetchOrderList();
};

// 处理每页数量变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size;
  queryParams.page = 1;
  fetchOrderList();
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

// 跳转到商品详情
const goToProductDetail = (productId) => {
  router.push(`/product/${productId}`);
};

// 跳转到订单详情
const goToOrderDetail = (order) => {
  router.push(`/order/detail/${order.id}`);
};

// 跳转到支付页面
const goToPay = (order) => {
  router.push(`/order/pay/${order.orderNo}`);
};

// 跳转到评价页面
const goToComment = (order) => {
  router.push(`/order/comment/${order.id}`);
};

// 取消订单
const cancelOrder = (order) => {
  cancelDialog.orderId = order.id;
  cancelDialog.form.reason = '';
  cancelDialog.visible = true;
};

// 确认取消订单
const confirmCancelOrder = async () => {
  if (!cancelDialog.form.reason) {
    ElMessage.warning('请选择取消原因');
    return;
  }
  
  try {
    cancelDialog.loading = true;
    
    await cancelOrderApi(cancelDialog.orderId, {
      reason: cancelDialog.form.reason
    });
    
    ElMessage.success('订单已取消');
    cancelDialog.visible = false;
    fetchOrderList();
  } catch (error) {
    console.error('取消订单失败:', error);
    ElMessage.error('取消失败，请重试');
  } finally {
    cancelDialog.loading = false;
  }
};

// 确认收货
const confirmReceipt = async (order) => {
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
    
    await confirmReceiptApi(order.id);
    ElMessage.success('确认收货成功');
    fetchOrderList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error);
      ElMessage.error('操作失败，请重试');
    }
  }
};

// 删除订单
const deleteOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      '您确定要删除该订单吗？删除后不可恢复。',
      '删除订单',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await deleteOrderApi(order.id);
    ElMessage.success('删除成功');
    fetchOrderList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error);
      ElMessage.error('删除失败，请重试');
    }
  }
};

// 申请退款
const applyRefund = (order) => {
  refundDialog.orderId = order.id;
  refundDialog.form.reason = '';
  refundDialog.form.description = '';
  refundDialog.visible = true;
};

// 确认申请退款
const confirmRefund = async () => {
  if (!refundDialog.form.reason) {
    ElMessage.warning('请选择退款原因');
    return;
  }
  
  try {
    refundDialog.loading = true;
    
    await applyRefundApi(refundDialog.orderId, {
      reason: refundDialog.form.reason,
      description: refundDialog.form.description
    });
    
    ElMessage.success('退款申请已提交');
    refundDialog.visible = false;
    fetchOrderList();
  } catch (error) {
    console.error('申请退款失败:', error);
    ElMessage.error('申请失败，请重试');
  } finally {
    refundDialog.loading = false;
  }
};

// 查看物流
const showLogistics = (order) => {
  // 如果订单没有物流信息，先获取一次
  if (!order.logistics) {
    getOrderLogistics(order.id).then(res => {
      logisticsDialog.data = res.data || mockLogisticsData();
      logisticsDialog.visible = true;
    }).catch(() => {
      // 失败时使用模拟数据
      logisticsDialog.data = mockLogisticsData();
      logisticsDialog.visible = true;
    });
  } else {
    logisticsDialog.data = order.logistics || mockLogisticsData();
    logisticsDialog.visible = true;
  }
};

// 模拟物流数据
const mockLogisticsData = () => {
  return {
    company: '顺丰速运',
    trackingNo: `SF${Math.floor(Math.random() * 10000000000)}`,
    status: Math.floor(Math.random() * 4) + 1,
    sendTime: '2023-05-16 10:20:35',
    location: {
      province: '浙江省',
      city: '杭州市',
      district: '西湖区'
    },
    traces: [
      {
        time: '2023-05-20 18:30:00',
        content: '已签收，签收人：本人'
      },
      {
        time: '2023-05-20 11:20:00',
        content: '【杭州市】快递员正在派送途中'
      },
      {
        time: '2023-05-20 08:30:00',
        content: '【杭州市】已到达杭州西湖区转运中心'
      },
      {
        time: '2023-05-19 20:15:00',
        content: '【上海市】已从上海发出，下一站杭州市'
      },
      {
        time: '2023-05-19 18:00:00',
        content: '【上海市】已到达上海转运中心'
      },
      {
        time: '2023-05-19 15:20:00',
        content: '【上海市】已揽收'
      }
    ]
  };
};

// 申请售后
const applyAftersale = (order) => {
  router.push(`/aftersale/apply/${order.id}`);
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

// 刷新物流信息
const refreshLogistics = async () => {
  try {
    // 实际项目中这里应该调用API获取最新物流信息
    // 这里仅做模拟
    const newTraces = [
      {
        time: new Date().toLocaleString(),
        content: '物流信息已更新'
      },
      ...logisticsDialog.data.traces
    ];
    
    logisticsDialog.data.traces = newTraces;
    ElMessage.success('物流信息已更新');
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
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  fetchOrderList();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.order-list-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .empty-orders {
    padding: 50px 0;
    display: flex;
    justify-content: center;
  }
  
  .order-list {
    .order-item {
      margin-bottom: $spacing-large;
      padding: $spacing-base;
      border: 1px solid $border-lighter;
      border-radius: $border-radius-base;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .order-header {
        display: flex;
        justify-content: space-between;
        padding-bottom: $spacing-small;
        border-bottom: 1px solid $border-lighter;
        margin-bottom: $spacing-base;
        
        .order-info {
          .order-time {
            margin-right: $spacing-base;
            color: $text-secondary;
          }
          
          .order-no {
            color: $text-regular;
          }
        }
      }
      
      .order-products {
        margin-bottom: $spacing-base;
        
        .product-item {
          display: flex;
          padding: $spacing-small 0;
          cursor: pointer;
          
          &:hover {
            background-color: #f5f7fa;
          }
          
          .product-image {
            width: 80px;
            height: 80px;
            margin-right: $spacing-base;
            border-radius: $border-radius-small;
          }
          
          .product-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            
            .product-name {
              font-size: $font-size-base;
              color: $text-primary;
              margin-bottom: $spacing-small;
            }
            
            .product-price {
              display: flex;
              justify-content: space-between;
              align-items: center;
              
              .price {
                color: $danger-color;
                font-weight: bold;
              }
              
              .quantity {
                color: $text-secondary;
              }
            }
          }
        }
      }
      
      .order-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: $spacing-base;
        border-top: 1px solid $border-lighter;
        
        .order-amount {
          .label {
            color: $text-regular;
          }
          
          .amount {
            color: $danger-color;
            font-size: $font-size-large;
            font-weight: bold;
          }
        }
        
        .order-actions {
          display: flex;
          gap: $spacing-small;
        }
      }
    }
  }
  
  .pagination-container {
    margin-top: $spacing-large;
    display: flex;
    justify-content: center;
  }
  
  .cancel-form, .refund-form {
    margin-bottom: $spacing-base;
  }
  
  .logistics-info {
    .logistics-header {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-base;
      margin-bottom: $spacing-base;
      
      .info-item {
        display: flex;
        align-items: center;
        
        .label {
          color: $text-secondary;
          margin-right: $spacing-small;
        }
        
        .value {
          font-weight: bold;
          margin-right: $spacing-small;
        }
      }
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
      margin-bottom: $spacing-small;
      font-weight: bold;
    }
    
    .logistics-map {
      margin-bottom: $spacing-base;
    }
    
    .logistics-timeline {
      max-height: 300px;
      overflow-y: auto;
      margin-bottom: $spacing-base;
    }
    
    .dialog-footer {
      display: flex;
      justify-content: center;
      gap: $spacing-base;
      margin-top: $spacing-base;
    }
  }
}

@include respond-to(sm) {
  .order-list-container {
    .order-list {
      .order-item {
        .order-header {
          flex-direction: column;
          
          .order-status {
            margin-top: $spacing-small;
          }
        }
        
        .order-footer {
          flex-direction: column;
          align-items: flex-start;
          
          .order-amount {
            margin-bottom: $spacing-base;
          }
          
          .order-actions {
            display: flex;
            flex-wrap: wrap;
            width: 100%;
            
            .el-button {
              margin-bottom: $spacing-small;
            }
          }
        }
      }
    }
  }
}
</style> 