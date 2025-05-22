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
import { ref, onMounted, onUnmounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrderDetailByOrderNoApi, payOrder as payOrderApi , getPaymentStatus} from '@/api/order';

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
  const orderNoString = route.params.orderNo; // 从路由获取 :orderNo 参数
    if (orderNoString) {
      loading.value = true;
      try {
        const res = await getOrderDetailByOrderNoApi(orderNoString); // 调用通过字符串订单号获取的API
        order.value = res.data || {}; // 确保API返回的数据结构是 { data: orderObject }

		// 检查订单是否存在或是否获取成功
		if (!order.value || Object.keys(order.value).length === 0) {
			ElMessage.error('未能获取到订单详情，请检查订单号或稍后再试');
			// 可以选择跳转到列表页或显示错误信息
			// router.push('/order/list');
			loading.value = false;
			return;
			}

			// 如果订单状态不是待支付，跳转到订单详情页
			if (order.value.status !== 1) { // 假设 status: 1 代表待支付
			ElMessage.warning('该订单状态不是待支付状态');
			router.push(`/order/detail/${order.value.id}`); // 确保 order.value.id 存在
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
			} else if (expireTime.value === 0 && order.value.status === 1) {
				// 如果从API获取时就已经超时，但状态还是待支付（理论上后端应更新状态）
				ElMessage.warning('支付已超时，订单可能已自动取消');
				// router.push('/order/list'); // 可以考虑跳转
			}
		}
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败，请刷新页面或稍后再试');
    // 此处不再调用 mockOrderDetail()
    // 您可以根据需要决定如何处理错误，例如显示一个错误提示，或者允许用户重试
    // router.push('/order/list'); // 或者跳转到错误页/列表页
  } finally {
    loading.value = false;
  }
}else {
    console.error('路由中未找到订单号');
    // ... 错误处理 ...
  }
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

//格式化支付金额
const formatPrice = (amount) => {
  return Number(amount).toFixed(2);
};


// 支付订单
const payOrder = async () => {
  if (!paymentMethod.value) {
    ElMessage.warning('请选择支付方式');
    return;
  }

  paying.value = true;

  try {
    // 调用支付接口 (这部分是真实的)
    const res = await payOrderApi(order.value.id, { // 使用 order.value.id (Long)
      paymentMethod: paymentMethod.value
    });

    // 显示支付二维码 (这部分是真实的)
    qrDialog.visible = true;
    qrDialog.paymentSuccess = false;
    qrDialog.qrUrl = res.data?.qrUrl || `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${order.value.orderNo}`;

    // 模拟查询支付结果 <--- 修改这里
    // mockQueryPaymentResult(); // 删除或注释掉这行模拟调用

    // 开始轮询真实的支付状态 <--- 添加这行
    if (qrDialog.visible) { // 确保弹窗可见时才开始轮询
        pollPaymentStatus();
    }

  } catch (error) {
    console.error('支付请求失败:', error);
    ElMessage.error('支付请求失败，请重试');
    // 如果支付API本身失败，可能需要关闭二维码弹窗
    // qrDialog.visible = false;
  } finally {
    paying.value = false;
  }
};
// 真实轮询逻辑示意 (这个函数本身是真实的)
const pollPaymentStatus = () => {
  // 如果之前的计时器还在，先清除，以防重复轮询
  if (qrDialog.queryTimer) {
    clearInterval(qrDialog.queryTimer);
  }

  qrDialog.queryTimer = setInterval(async () => {
    // 检查弹窗是否还可见，如果用户关闭了弹窗，应停止轮询
    if (!qrDialog.visible) {
        clearInterval(qrDialog.queryTimer);
        return;
    }

    try {
        const res = await getPaymentStatus(order.value.id); // 调用获取支付状态的API
        // 假设API成功时，res.data 包含一个表示支付状态的字段，例如 status
        // 并且当支付成功时，这个状态是 'paid' (或后端定义的其他表示成功的状态字符串/代码)
        if (res.data && res.data.status === 'paid') {
            clearInterval(qrDialog.queryTimer);
            qrDialog.paymentSuccess = true;
            ElMessage.success('支付成功！');
            // 可以在这里根据业务需求做一些跳转，比如跳转到订单详情或支付成功页
            // goToOrderDetail(); // 例如
        }
        // 如果 res.data.status 是其他状态 (如 'pending', 'failed')，则轮询会继续
        // 您也可以在这里处理支付失败的情况，并提示用户
    } catch (error) {
        console.error('查询支付状态失败:', error);
        // 根据错误类型决定是否停止轮询或提示用户
        // clearInterval(qrDialog.queryTimer); // 例如，如果发生严重错误，停止轮询
        // ElMessage.error('查询支付状态失败，请稍后再试');
    }
  }, 3000); // 每3秒查询一次
};


// 取消支付
const cancelPayment = async () => {
  try {
    // ... (MessageBox 确认) ...
    qrDialog.visible = false;
    if (qrDialog.queryTimer) clearInterval(qrDialog.queryTimer); // 使用 clearInterval
    router.push('/order/list');
  } catch (error) {
    // ...
  }
};

// 跳转到订单详情
const goToOrderDetail = () => {
  qrDialog.visible = false;
  if (qrDialog.queryTimer) clearInterval(qrDialog.queryTimer); // 使用 clearInterval
  router.push(`/order/detail/${order.value.id}`);
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
        margin-bottom: $spacing-large;
      }
      
      .payment-methods {
        display: flex;
        flex-direction: column;
        gap: 37px !important;
		height: 230px;
        //gap: $spacing-base;
		width: 100%;
		align-items: center;
		
		::v-deep(.el-radio) {
		  display: flex !important;
		  //display: block;
		  align-items: center;
		  width: 350px;
		  height: 70px !important;
		  justify-content: flex-start;
		  box-sizing: border-box;
		  
		  .el-radio__input { // 这对应 <span class="el-radio__input...">
            flex-shrink: 0 !important; // 防止它被压缩
            // 通常 Element UI 会处理好这部分的样式和间距
			margin: 0 auto !important;
			display: none !important;
          }
		}
		
		
		.el-radio__label { // 对应 <span class="el-radio__label">
		      width: 330px !important;  // <--- 设置固定宽度
		      height: 70px !important;  // <--- 设置固定高度
		      display: block !important; // 确保它可以正确应用宽高，并作为块级容器
		      // flex-grow: 1; // 因为宽度固定了，不再需要 flex-grow
		      // width: 0;     // 同上
		      box-sizing: border-box !important; // 加上这个，如果 el-radio__label 将来可能有自己的 padding 或 border
		      // padding: 0; // 确保它没有内边距，让 .payment-method-item 完全填充
			  padding: 0 !important;
		}

          
        
        .payment-method-item {
		  margin-top: 40px;
          display: flex;
          align-items: center;
          padding: $spacing-base;
          border: 1px solid $border-light;
          border-radius: $border-radius-base;
          transition: all 0.3s;
		  height: 70px;
		  width: 330px;
		  //margin: 0 auto;
		  box-sizing: border-box;
          
          .payment-icon {
            width: 40px;
            height: 40px;
            margin-right: $spacing-base;
            background-size: contain;
            background-repeat: no-repeat;
            background-position: center;
			flex-shrink: 0;
          }
          
          .method-info {
			  display: flex;
			  flex-direction: column;
			  justify-content: center;
			  flex-grow: 1;
			  
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