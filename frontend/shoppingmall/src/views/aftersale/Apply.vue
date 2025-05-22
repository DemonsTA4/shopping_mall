<template>
  <div class="aftersale-apply-container">
    <h1 class="page-title">申请售后</h1>
    
    <el-card v-if="!loading">
      <!-- 订单信息 -->
      <div class="order-info-section">
        <h2 class="section-title">订单信息</h2>
        <div class="order-basic-info">
          <div class="info-item">
            <span class="label">订单编号：</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">下单时间：</span>
            <span class="value">{{ order.createTime }}</span>
          </div>
        </div>
        
        <div class="product-list">
          <div class="table-header">
            <div class="col product-col">商品</div>
            <div class="col price-col">单价</div>
            <div class="col quantity-col">数量</div>
            <div class="col action-col">售后商品</div>
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
                    :src="item.productImage" 
                    :alt="item.productName"
                    class="product-image"
                  />
                  <div class="product-name">{{ item.name }}</div>
                </div>
              </div>
              <div class="col price-col">¥{{ item.price }}</div>
              <div class="col quantity-col">{{ item.quantity }}</div>
              <div class="col action-col">
                <el-checkbox 
                  v-model="item.selected" 
                  @change="handleItemSelect(item)"
                >
                  选择
                </el-checkbox>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 售后申请表单 -->
      <div class="aftersale-form" v-if="hasSelectedItems">
        <h2 class="section-title">售后信息</h2>
        
        <el-form 
          ref="formRef"
          :model="aftersaleForm"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item label="售后类型" prop="type">
            <el-radio-group v-model="aftersaleForm.type">
              <el-radio :label="1">退款</el-radio>
              <el-radio :label="2">退货退款</el-radio>
              <el-radio :label="3">换货</el-radio>
              <el-radio :label="4">维修</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="售后原因" prop="reason">
            <el-select v-model="aftersaleForm.reason" placeholder="请选择售后原因" style="width: 100%">
              <el-option v-for="item in reasonOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="问题描述" prop="description">
            <el-input 
              v-model="aftersaleForm.description" 
              type="textarea" 
              :rows="4"
              placeholder="请详细描述您遇到的问题..."
            />
          </el-form-item>
          
          <el-form-item label="上传凭证">
            <el-upload
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="5"
              @change="handleImageChange"
              @remove="handleImageRemove"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">可上传商品问题图片或聊天记录截图等</div>
          </el-form-item>
          
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="aftersaleForm.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>
          
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="aftersaleForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-form>
      </div>
      
      <div class="form-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button v-if="hasSelectedItems" type="primary" :loading="submitting" @click="submitAftersale">
          提交申请
        </el-button>
      </div>
    </el-card>
    
    <!-- 提交成功对话框 -->
    <el-dialog
      v-model="successDialog.visible"
      title="申请提交成功"
      width="400px"
      :before-close="handleCloseSuccessDialog"
    >
      <div class="success-content">
        <el-icon class="success-icon" color="#67C23A" :size="60"><CircleCheck /></el-icon>
        <p class="success-text">您的售后申请已提交成功！</p>
        <p class="success-number">售后单号：{{ successDialog.aftersaleNo }}</p>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="goToOrderList">返回订单列表</el-button>
          <el-button type="primary" @click="goToAftersaleList">查看售后记录</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getOrderDetailByIdApi } from '@/api/order';
import { getUserInfo } from '@/api/user';
import { submitAftersaleApplication } from '@/api/aftersale';
import { useUserStore } from '@/store/modules/user';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const submitting = ref(false);
const order = ref({});
const formRef = ref(null);
const imageList = ref([]);
const userStore = useUserStore();
const userInfo = ref({});

// 售后申请表单
const aftersaleForm = reactive({
  type: 1,
  reason: '',
  description: '',
  contactName: '',
  contactPhone: '',
  images: []
});

// 国家选项
const countryOptions = [
  { value: 'CN', label: '中国', phonePattern: /^1[3-9]\d{9}$/ },
  { value: 'US', label: '美国', phonePattern: /^\+?1?\d{10}$/ },
  { value: 'GB', label: '英国', phonePattern: /^\+?44\d{10}$/ },
  { value: 'JP', label: '日本', phonePattern: /^\+?81\d{9,10}$/ },
  { value: 'KR', label: '韩国', phonePattern: /^\+?82\d{9,10}$/ },
  // 可以根据需要添加更多国家
];

// 表单验证规则
const formRules = {
  type: [
    { required: true, message: '请选择售后类型', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请选择售后原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请填写问题描述', trigger: 'blur' },
    { min: 10, message: '描述不能少于10个字符', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!userInfo.value.country) {
          callback(new Error('请先在个人资料中设置国家信息'));
          return;
        }
        
        const country = countryOptions.find(c => c.value === userInfo.value.country);
        if (!country) {
          callback(new Error('无效的国家选择'));
          return;
        }

        if (value && !country.phonePattern.test(value)) {
          callback(new Error(`请输入正确的${country.label}手机号码格式`));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 售后原因选项
const reasonOptions = [
  { value: '质量问题', label: '质量问题' },
  { value: '功能异常', label: '功能异常' },
  { value: '商品损坏', label: '商品损坏' },
  { value: '尺寸不合适', label: '尺寸不合适' },
  { value: '颜色/款式与描述不符', label: '颜色/款式与描述不符' },
  { value: '收到商品与描述不符', label: '收到商品与描述不符' },
  { value: '不想要了', label: '不想要了' },
  { value: '其他', label: '其他' }
];

// 成功对话框
const successDialog = reactive({
  visible: false,
  aftersaleNo: ''
});

// 计算是否有选中的商品
const hasSelectedItems = computed(() => {
  if (!order.value.items) return false;
  return order.value.items.some(item => item.selected);
});

// 获取订单详情
const fetchOrderDetail = async () => {
  const orderId = route.params.orderId;
  if (!orderId) {
    ElMessage.error('订单ID不能为空');
    router.push('/order/list');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await getOrderDetailByIdApi(orderId);
    order.value = res.data || {};
    
    // 初始化商品选择状态
    if (order.value.items && order.value.items.length > 0) {
      order.value.items.forEach(item => {
        item.selected = false;
      });
    }
    
    // 如果订单状态不适合申请售后，跳转到订单详情页
    if (![2, 3, 4, 5].includes(order.value.status)) {
      ElMessage.warning('该订单状态不支持申请售后');
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
  order.value = {
    id: orderId,
    orderNo: `ORDER${Date.now()}`,
    status: 3,
    createTime: '2023-05-15 14:30:25',
    items: [
      {
        id: 1,
        productId: 101,
        name: '测试商品1',
        price: 599.00,
        quantity: 1,
        imageUrl: 'https://picsum.photos/id/1/100/100',
        selected: false
      },
      {
        id: 2,
        productId: 102,
        name: '测试商品2',
        price: 600.00,
        quantity: 2,
        imageUrl: 'https://picsum.photos/id/2/100/100',
        selected: false
      }
    ]
  };
};

// 处理商品选择
const handleItemSelect = (item) => {
  // 如果是退款（整单），则选中一个就选中所有
  if (aftersaleForm.type === 1 && item.selected) {
    order.value.items.forEach(i => {
      i.selected = true;
    });
  }
};

// 处理图片上传
const handleImageChange = (file) => {
  // 在实际项目中，这里应该上传图片到服务器，然后获取URL
  // 这里简单模拟
  const reader = new FileReader();
  reader.readAsDataURL(file.raw);
  reader.onload = () => {
    imageList.value.push(reader.result);
  };
};

// 处理图片移除
const handleImageRemove = (file) => {
  const index = imageList.value.indexOf(file.url);
  if (index !== -1) {
    imageList.value.splice(index, 1);
  }
};

// 提交售后申请
const submitAftersale = async () => {
  if (!hasSelectedItems.value) {
    ElMessage.warning('请选择需要售后的商品');
    return;
  }
  
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    submitting.value = true;
    
    // 获取选中的商品项
    const selectedItems = order.value.items
      .filter(item => item.selected)
      .map(item => ({
        itemId: item.id,
        productId: item.productId,
        productName: item.name,
        quantity: item.quantity
      }));
    
    const data = {
      orderId: order.value.id,
      orderNo: order.value.orderNo,
      type: aftersaleForm.type,
      reason: aftersaleForm.reason,
      description: aftersaleForm.description,
      contactName: aftersaleForm.contactName,
      contactPhone: aftersaleForm.contactPhone,
      images: imageList.value,
      items: selectedItems
    };
    
    const res = await submitAftersaleApplication(data);
    
    // 显示成功对话框
    successDialog.aftersaleNo = res.data?.aftersaleNo || `ASV${Date.now()}`;
    successDialog.visible = true;
  } catch (error) {
    console.error('提交售后申请失败:', error);
    ElMessage.error('提交申请失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 跳转到订单列表
const goToOrderList = () => {
  router.push('/order/list');
};

// 跳转到售后记录列表
const goToAftersaleList = () => {
  router.push('/aftersale/list');
};

// 关闭成功对话框
const handleCloseSuccessDialog = () => {
  successDialog.visible = false;
  goToAftersaleList();
};

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo();
    userInfo.value = res.data || {};
    
    // 自动填充联系人和电话
    aftersaleForm.contactName = userInfo.value.nickname || userInfo.value.username || '';
    aftersaleForm.contactPhone = userInfo.value.phone || '';
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败');
  }
};

onMounted(() => {
  fetchOrderDetail();
  fetchUserInfo();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.aftersale-apply-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .section-title {
    font-size: $font-size-large;
    margin-bottom: $spacing-base;
    padding-bottom: $spacing-small;
    border-bottom: 1px solid $border-lighter;
  }
  
  .order-info-section {
    margin-bottom: $spacing-large;
    
    .order-basic-info {
      display: flex;
      flex-wrap: wrap;
      margin-bottom: $spacing-base;
      
      .info-item {
        margin-right: $spacing-large;
        margin-bottom: $spacing-small;
        
        .label {
          color: $text-secondary;
          margin-right: $spacing-small;
        }
        
        .value {
          font-weight: bold;
        }
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
        }
        
        .product-name {
          flex: 1;
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
    
    .action-col {
      width: 80px;
      justify-content: center;
    }
  }
  
  .aftersale-form {
    margin-bottom: $spacing-large;
    
    .upload-tip {
      font-size: $font-size-small;
      color: $text-secondary;
      margin-top: $spacing-small;
    }
  }
  
  .form-actions {
    display: flex;
    justify-content: center;
    gap: $spacing-base;
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
      margin-bottom: $spacing-small;
    }
    
    .success-number {
      color: $text-secondary;
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: center;
    gap: $spacing-base;
  }
}

@include respond-to(sm) {
  .aftersale-apply-container {
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
        
        .price-col, .quantity-col, .action-col {
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