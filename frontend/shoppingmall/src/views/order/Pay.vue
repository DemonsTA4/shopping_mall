<template>
  <div class="order-pay-container">
    <h1 class="page-title">订单支付</h1>
    
    <div class="payment-card" v-if="!loading">
      <!-- 订单信息 -->
      <div class="order-info-section">
        <div class="order-no">订单号：{{ order.orderNo }}</div>
        <div class="amount">支付金额：<span class="price">¥{{ order.totalAmount }}</span></div>
        <div class="expire-time" v-if="expireTime > 0">
          支付剩余时间：{{ formatTime(expireTime) }}
        </div>
      </div>
      
      <!-- 支付方式 -->
      <div class="payment-method-section">
        <h2 class="section-title">选择支付方式</h2>
        
        <el-radio-group v-model="paymentMethod" class="payment-methods">
          <el-radio label="alipay">
            <div class="payment-method-item">
              <i class="payment-icon alipay-icon"></i>
              <div class="method-info">
                <div class="method-name">支付宝</div>
                <div class="method-desc">推荐有支付宝账户的用户使用</div>
              </div>
            </div>
          </el-radio>
          
          <el-radio label="wechat">
            <div class="payment-method-item">
              <i class="payment-icon wechat-icon"></i>
              <div class="method-info">
                <div class="method-name">微信支付</div>
                <div class="method-desc">推荐安装微信的用户使用</div>
              </div>
            </div>
          </el-radio>
          
          <el-radio label="credit">
            <div class="payment-method-item">
              <i class="payment-icon credit-icon"></i>
              <div class="method-info">
                <div class="method-name">信用卡</div>
                <div class="method-desc">支持Visa、MasterCard、JCB、UnionPay</div>
              </div>
            </div>
          </el-radio>
        </el-radio-group>
      </div>
      
      <!-- 支付按钮 -->
      <div class="payment-action">
        <el-button 
          type="primary" 
          size="large" 
          :loading="paying" 
          @click="payOrder"
        >
          立即支付
        </el-button>
      </div>
      
      <!-- 支付提示 -->
      <div class="payment-tips">
        <p>tips: 为保证您的购物体验，请在 {{ expireHours }}小时内完成支付，超时订单将自动取消。</p>
      </div>
    </div>
    
    <!-- 二维码支付弹窗 -->
    <el-dialog
      v-model="qrDialog.visible"
      title="扫码支付"
      width="400px"
      center
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="qr-pay-container">
        <div class="payment-method">
          <i :class="['payment-icon', `${paymentMethod}-icon`]"></i>
          <span class="method-name">{{ getPaymentMethodName(paymentMethod) }}</span>
        </div>
        
        <div class="amount">¥{{ order.totalAmount }}</div>
        
        <div class="qr-code">
          <img :src="qrDialog.qrUrl" alt="支付二维码" />
          <p class="tip">请使用{{ getPaymentMethodName(paymentMethod) }}扫一扫</p>
        </div>
        
        <div class="payment-status">
          <p v-if="!qrDialog.paymentSuccess">
            <el-icon class="loading-icon"><Loading /></el-icon>
            正在等待支付结果...
          </p>
          <p v-else class="success">
            <el-icon class="success-icon"><CircleCheck /></el-icon>
            支付成功！
          </p>
        </div>
        
        <div class="dialog-footer">
          <el-button @click="cancelPayment">取消支付</el-button>
          <el-button 
            v-if="qrDialog.paymentSuccess" 
            type="primary" 
            @click="goToOrderDetail"
          >
            查看订单
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrderDetail, payOrder as payOrderApi } from '@/api/order';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const paying = ref(false);
const order = ref({});
const paymentMethod = ref('alipay');
const expireHours = 24;
const expireTime = ref(0);
const timer = ref(null);

// 订单支付二维码对话框
const qrDialog = reactive({
  visible: false,
  qrUrl: '',
  paymentSuccess: false,
  queryTimer: null
});

// 获取订单详情
const fetchOrderDetail = async () => {
  const orderNo = route.params.orderNo;
  if (!orderNo) {
    ElMessage.error('订单号不能为空');
    router.push('/order/list');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await getOrderDetail({ orderNo });
    order.value = res.data || {};
    
    // 如果订单状态不是待支付，跳转到订单详情页
    if (order.value.status !== 1) {
      ElMessage.warning('该订单状态不是待支付状态');
      router.push(`/order/detail/${order.value.id}`);
      return;
    }
    
    // 计算剩余支付时间
    if (order.value.createTime) {
      const createTime = new Date(order.value.createTime).getTime();
      const expireTimestamp = createTime + expireHours * 60 * 60 * 1000;
      const now = Date.now();
      expireTime.value = Math.max(0, Math.floor((expireTimestamp - now) / 1000));
      
      if (expireTime.value > 0) {
        startCountdown();
      }
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败，请重试');
    
    // 使用模拟数据
    mockOrderDetail();
  } finally {
    loading.value = false;
  }
};

// 模拟订单详情
const mockOrderDetail = () => {
  order.value = {
    id: 1,
    orderNo: route.params.orderNo || `ORDER${Date.now()}`,
    status: 1,
    totalAmount: '1299.00',
    createTime: new Date(Date.now() - 30 * 60 * 1000).toISOString()
  };
  
  // 设置剩余支付时间为23小时30分钟
  expireTime.value = expireHours * 60 * 60 - 30 * 60;
  startCountdown();
};

// 开始倒计时
const startCountdown = () => {
  if (timer.value) clearInterval(timer.value);
  
  timer.value = setInterval(() => {
    if (expireTime.value > 0) {
      expireTime.value--;
    } else {
      clearInterval(timer.value);
      ElMessage.warning('支付超时，订单已自动取消');
      router.push('/order/list');
    }
  }, 1000);
};

// 格式化剩余时间
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 支付订单
const payOrder = async () => {
  if (!paymentMethod.value) {
    ElMessage.warning('请选择支付方式');
    return;
  }
  
  paying.value = true;
  
  try {
    // 调用支付接口
    const res = await payOrderApi(order.value.id, {
      paymentMethod: paymentMethod.value
    });
    
    // 显示支付二维码
    qrDialog.visible = true;
    qrDialog.paymentSuccess = false;
    qrDialog.qrUrl = res.data?.qrUrl || `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${order.value.orderNo}`;
    
    // 模拟查询支付结果
    mockQueryPaymentResult();
  } catch (error) {
    console.error('支付请求失败:', error);
    ElMessage.error('支付请求失败，请重试');
  } finally {
    paying.value = false;
  }
};

// 模拟查询支付结果
const mockQueryPaymentResult = () => {
  if (qrDialog.queryTimer) clearTimeout(qrDialog.queryTimer);
  
  // 5秒后模拟支付成功
  qrDialog.queryTimer = setTimeout(() => {
    qrDialog.paymentSuccess = true;
  }, 5000);
};

// 取消支付
const cancelPayment = async () => {
  try {
    await ElMessageBox.confirm(
      '您确定要取消支付吗？',
      '取消支付',
      {
        confirmButtonText: '确定',
        cancelButtonText: '继续支付',
        type: 'warning'
      }
    );
    
    qrDialog.visible = false;
    if (qrDialog.queryTimer) clearTimeout(qrDialog.queryTimer);
    router.push('/order/list');
  } catch (error) {
    // 用户选择继续支付，不做处理
  }
};

// 获取支付方式名称
const getPaymentMethodName = (method) => {
  const methodMap = {
    'alipay': '支付宝',
    'wechat': '微信',
    'credit': '银行卡'
  };
  
  return methodMap[method] || '未知支付方式';
};

// 跳转到订单详情
const goToOrderDetail = () => {
  qrDialog.visible = false;
  if (qrDialog.queryTimer) clearTimeout(qrDialog.queryTimer);
  router.push(`/order/detail/${order.value.id}`);
};

onMounted(() => {
  fetchOrderDetail();
});

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value);
  if (qrDialog.queryTimer) clearTimeout(qrDialog.queryTimer);
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.order-pay-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .payment-card {
    background-color: #fff;
    border-radius: $border-radius-base;
    box-shadow: $box-shadow-light;
    padding: $spacing-large;
    
    .order-info-section {
      text-align: center;
      padding: $spacing-large 0;
      margin-bottom: $spacing-large;
      border-bottom: 1px solid $border-lighter;
      
      .order-no {
        color: $text-secondary;
        margin-bottom: $spacing-small;
      }
      
      .amount {
        margin-bottom: $spacing-base;
        font-size: $font-size-large;
        
        .price {
          font-size: 24px;
          font-weight: bold;
          color: $danger-color;
        }
      }
      
      .expire-time {
        color: $warning-color;
        font-size: $font-size-small;
      }
    }
    
    .payment-method-section {
      margin-bottom: $spacing-large;
      
      .section-title {
        font-size: $font-size-large;
        margin-bottom: $spacing-base;
      }
      
      .payment-methods {
        display: flex;
        flex-direction: column;
        gap: $spacing-base;
        width: 100%;
        
        .payment-method-item {
          display: flex;
          align-items: center;
          padding: $spacing-base;
          border: 1px solid $border-lighter;
          border-radius: $border-radius-base;
          transition: all 0.3s;
          
          .payment-icon {
            width: 40px;
            height: 40px;
            margin-right: $spacing-base;
            background-size: contain;
            background-repeat: no-repeat;
            background-position: center;
          }
          
          .method-info {
            .method-name {
              font-weight: bold;
              margin-bottom: $spacing-small;
            }
            
            .method-desc {
              font-size: $font-size-small;
              color: $text-secondary;
            }
          }
        }
      }
    }
    
    .payment-action {
      display: flex;
      justify-content: center;
      margin-bottom: $spacing-base;
      
      .el-button {
        width: 200px;
      }
    }
    
    .payment-tips {
      text-align: center;
      color: $text-secondary;
      font-size: $font-size-small;
    }
  }
}

.qr-pay-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .payment-method {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-base;
    
    .payment-icon {
      width: 24px;
      height: 24px;
      margin-right: $spacing-small;
      background-size: contain;
      background-repeat: no-repeat;
      background-position: center;
    }
    
    .method-name {
      font-weight: bold;
    }
  }
  
  .amount {
    font-size: 24px;
    font-weight: bold;
    color: $danger-color;
    margin-bottom: $spacing-base;
  }
  
  .qr-code {
    margin-bottom: $spacing-base;
    text-align: center;
    
    img {
      width: 200px;
      height: 200px;
      margin-bottom: $spacing-small;
    }
    
    .tip {
      color: $text-secondary;
      font-size: $font-size-small;
    }
  }
  
  .payment-status {
    margin-bottom: $spacing-base;
    
    p {
      display: flex;
      align-items: center;
      justify-content: center;
      
      .loading-icon {
        margin-right: $spacing-small;
        font-size: 18px;
        animation: rotate 1s linear infinite;
      }
      
      .success-icon {
        margin-right: $spacing-small;
        font-size: 18px;
        color: $success-color;
      }
      
      &.success {
        color: $success-color;
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: center;
    gap: $spacing-base;
  }
}

.alipay-icon {
  background-image: url('@/assets/images/alipay.png');
}

.wechat-icon {
  background-image: url('@/assets/images/wechat.png');
}

.credit-icon {
  background-image: url('@/assets/images/credit.png');
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 