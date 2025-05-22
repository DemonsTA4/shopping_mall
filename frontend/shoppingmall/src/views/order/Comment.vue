<template>
  <div class="order-comment-container">
    <h1 class="page-title">商品评价</h1>
    
    <el-card v-if="!loading">
      <div class="comment-header">
        <div class="order-info">
          <span class="label">订单号：</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
      </div>
      
      <div class="comment-form">
        <div 
          v-for="item in order.items" 
          :key="item.id" 
          class="product-comment-item"
        >
          <div class="product-info">
            <el-image 
              :src="item.imageUrl" 
              :alt="item.name"
              class="product-image"
              @click="goToProduct(item.productId)"
            />
            <div class="product-detail">
              <div class="product-name" @click="goToProduct(item.productId)">
                {{ item.name }}
              </div>
              <div class="product-price">¥{{ item.price }} x {{ item.quantity }}</div>
            </div>
          </div>
          
          <div class="comment-content">
            <el-form 
              :ref="formRefs[item.id]" 
              :model="commentForms[item.id]" 
              label-position="top"
            >
              <el-form-item label="商品评分" prop="rating">
                <el-rate 
                  v-model="commentForms[item.id].rating" 
                  allow-half 
                  show-text 
                  :texts="['很差', '较差', '一般', '较好', '很好']"
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                />
              </el-form-item>
              
              <el-form-item label="评价内容" prop="content">
                <el-input 
                  v-model="commentForms[item.id].content" 
                  type="textarea" 
                  :rows="4"
                  placeholder="请分享您对该商品的使用体验和感受..."
                />
              </el-form-item>
              
              <el-form-item label="上传图片">
                <el-upload
                  action="#"
                  list-type="picture-card"
                  :auto-upload="false"
                  :limit="5"
                  @change="handleImageChange($event, item.id)"
                  @remove="handleImageRemove($event, item.id)"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              
              <el-form-item label="匿名评价">
                <el-switch v-model="commentForms[item.id].anonymous" />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
      
      <div class="form-actions">
        <el-button @click="goToOrderList">返回订单列表</el-button>
        <el-button type="primary" :loading="submitting" @click="submitComments">提交评价</el-button>
      </div>
    </el-card>
    
    <!-- 提交成功对话框 -->
    <el-dialog
      v-model="successDialog.visible"
      title="评价成功"
      width="400px"
      :before-close="handleCloseSuccessDialog"
    >
      <div class="success-content">
        <el-icon class="success-icon" color="#67C23A" :size="60"><CircleCheck /></el-icon>
        <p class="success-text">您的评价已提交成功，感谢您的反馈！</p>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="goToOrderList">返回订单列表</el-button>
          <el-button type="primary" @click="goToHome">继续购物</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getOrderDetailByIdApi } from '@/api/order';
import { addComment } from '@/api/product';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const submitting = ref(false);
const order = ref({});
const commentForms = ref({});
const formRefs = ref({});
const imageList = ref({});

// 成功对话框
const successDialog = reactive({
  visible: false
});

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
    const res = await getOrderDetailByIdApi(orderId);
    order.value = res.data || {};
    
    // 初始化评论表单
    if (order.value.items && order.value.items.length > 0) {
      order.value.items.forEach(item => {
        commentForms.value[item.id] = {
          productId: item.productId,
          orderId: order.value.id,
          rating: 5,
          content: '',
          anonymous: false
        };
        
        imageList.value[item.id] = [];
      });
    }
    
    // 如果订单状态不是待评价，跳转到订单详情页
    if (order.value.status !== 4) {
      ElMessage.warning('该订单不是待评价状态');
      router.push(`/order/detail/${order.value.id}`);
      return;
    }
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
  const items = [
    {
      id: 1001,
      productId: 101,
      name: '测试商品1',
      price: 599.00,
      quantity: 1,
      imageUrl: 'https://picsum.photos/id/1/100/100'
    },
    {
      id: 1002,
      productId: 102,
      name: '测试商品2',
      price: 600.00,
      quantity: 1,
      imageUrl: 'https://picsum.photos/id/2/100/100'
    }
  ];
  
  order.value = {
    id: orderId,
    orderNo: `ORDER${Date.now()}`,
    status: 4,
    createTime: '2023-05-15 14:30:25',
    items
  };
  
  // 初始化评论表单
  items.forEach(item => {
    commentForms.value[item.id] = {
      productId: item.productId,
      orderId: order.value.id,
      rating: 5,
      content: '',
      anonymous: false
    };
    
    imageList.value[item.id] = [];
  });
};

// 处理图片上传
const handleImageChange = (file, itemId) => {
  // 在实际项目中，这里应该上传图片到服务器，然后获取URL
  // 这里简单模拟
  const reader = new FileReader();
  reader.readAsDataURL(file.raw);
  reader.onload = () => {
    if (!imageList.value[itemId]) {
      imageList.value[itemId] = [];
    }
    imageList.value[itemId].push(reader.result);
  };
};

// 处理图片移除
const handleImageRemove = (file, itemId) => {
  const index = imageList.value[itemId].indexOf(file.url);
  if (index !== -1) {
    imageList.value[itemId].splice(index, 1);
  }
};

// 提交评价
const submitComments = async () => {
  // 验证评分和内容
  for (const itemId in commentForms.value) {
    const form = commentForms.value[itemId];
    if (!form.rating) {
      ElMessage.warning('请为所有商品评分');
      return;
    }
    
    if (!form.content.trim()) {
      ElMessage.warning('请填写评价内容');
      return;
    }
  }
  
  submitting.value = true;
  
  try {
    const promises = Object.keys(commentForms.value).map(itemId => {
      const form = commentForms.value[itemId];
      const images = imageList.value[itemId] || [];
      
      return addComment(form.productId, {
        orderId: form.orderId,
        content: form.content,
        rating: form.rating,
        images: images,
        anonymous: form.anonymous
      });
    });
    
    await Promise.all(promises);
    
    // 显示成功对话框
    successDialog.visible = true;
  } catch (error) {
    console.error('提交评价失败:', error);
    ElMessage.error('提交评价失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/product/${productId}`);
};

// 跳转到订单列表
const goToOrderList = () => {
  router.push('/order/list');
};

// 跳转到首页
const goToHome = () => {
  router.push('/');
};

// 关闭成功对话框
const handleCloseSuccessDialog = () => {
  successDialog.visible = false;
  goToOrderList();
};

onMounted(() => {
  fetchOrderDetail();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.order-comment-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .comment-header {
    margin-bottom: $spacing-large;
    padding-bottom: $spacing-base;
    border-bottom: 1px solid $border-lighter;
    
    .order-info {
      .label {
        color: $text-secondary;
      }
      
      .value {
        font-weight: bold;
      }
    }
  }
  
  .product-comment-item {
    margin-bottom: $spacing-large;
    padding-bottom: $spacing-large;
    border-bottom: 1px solid $border-lighter;
    
    &:last-child {
      margin-bottom: 0;
      padding-bottom: 0;
      border-bottom: none;
    }
    
    .product-info {
      display: flex;
      margin-bottom: $spacing-base;
      
      .product-image {
        width: 80px;
        height: 80px;
        border-radius: $border-radius-small;
        margin-right: $spacing-base;
        cursor: pointer;
      }
      
      .product-detail {
        flex: 1;
        
        .product-name {
          margin-bottom: $spacing-small;
          color: $text-primary;
          cursor: pointer;
          
          &:hover {
            color: $primary-color;
          }
        }
        
        .product-price {
          color: $text-secondary;
        }
      }
    }
    
    .comment-content {
      background-color: #f8f8f8;
      border-radius: $border-radius-base;
      padding: $spacing-base;
    }
  }
  
  .form-actions {
    display: flex;
    justify-content: center;
    gap: $spacing-base;
    margin-top: $spacing-large;
  }
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-large 0;
  
  .success-icon {
    margin-bottom: $spacing-base;
  }
  
  .success-text {
    font-size: $font-size-large;
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: $spacing-base;
}
</style> 