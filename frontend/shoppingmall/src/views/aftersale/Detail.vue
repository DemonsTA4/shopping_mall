<template>
  <div class="aftersale-detail-container">
    <h1 class="page-title">售后详情</h1>
    
    <el-card v-if="!loading">
      <!-- 售后状态 -->
      <div class="status-section">
        <div class="status-info">
          <el-tag :type="getStatusType(aftersale.status)" size="large">
            {{ getStatusText(aftersale.status) }}
          </el-tag>
          <p class="status-desc">
            {{ getStatusDescription(aftersale.status, aftersale.type) }}
          </p>
        </div>
        
        <div class="aftersale-actions">
          <el-button 
            v-if="aftersale.status === 1" 
            type="danger" 
            plain
            @click="cancelAftersale"
          >
            取消申请
          </el-button>
          
          <el-button 
            v-if="aftersale.status === 2 && aftersale.type === 2" 
            type="warning" 
            plain
            @click="fillReturnInfo"
          >
            填写退货信息
          </el-button>
          
          <el-button 
            v-if="aftersale.status === 4" 
            type="success" 
            plain
            @click="confirmComplete"
          >
            确认完成
          </el-button>
        </div>
      </div>
      
      <!-- 售后进度 -->
      <div class="progress-section">
        <el-steps :active="getProgressStep()" finish-status="success">
          <el-step title="提交申请" :description="aftersale.createTime"></el-step>
          <el-step title="商家处理" :description="aftersale.processTime || ''"></el-step>
          <el-step title="处理中" :description="aftersale.handleTime || ''"></el-step>
          <el-step title="完成" :description="aftersale.finishTime || ''"></el-step>
        </el-steps>
      </div>
      
      <!-- 基本信息 -->
      <div class="section basic-section">
        <h2 class="section-title">基本信息</h2>
        <div class="info-item">
          <span class="label">售后单号：</span>
          <span class="value">{{ aftersale.aftersaleNo }}</span>
        </div>
        <div class="info-item">
          <span class="label">申请时间：</span>
          <span class="value">{{ aftersale.createTime }}</span>
        </div>
        <div class="info-item">
          <span class="label">售后类型：</span>
          <span class="value">{{ getTypeText(aftersale.type) }}</span>
        </div>
        <div class="info-item">
          <span class="label">售后原因：</span>
          <span class="value">{{ aftersale.reason }}</span>
        </div>
        <div class="info-item">
          <span class="label">问题描述：</span>
          <span class="value">{{ aftersale.description }}</span>
        </div>
      </div>
      
      <!-- 商品信息 -->
      <div class="section products-section">
        <h2 class="section-title">售后商品</h2>
        <div class="product-list">
          <div 
            v-for="product in aftersale.products" 
            :key="product.itemId" 
            class="product-item"
          >
            <div class="product-image">
              <el-image 
                :src="product.imageUrl" 
                :alt="product.name"
                @click="goToProductDetail(product.productId)"
              />
            </div>
            
            <div class="product-info">
              <div class="product-name" @click="goToProductDetail(product.productId)">
                {{ product.name }}
              </div>
              <div class="product-price">
                <span class="price">¥{{ product.price }}</span>
                <span class="quantity">x {{ product.quantity }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 物流信息 -->
      <div v-if="hasLogistics" class="section logistics-section">
        <h2 class="section-title">物流信息</h2>
        
        <div v-if="aftersale.logistics" class="logistics-info">
          <div class="info-item">
            <span class="label">物流公司：</span>
            <span class="value">{{ aftersale.logistics.company }}</span>
          </div>
          <div class="info-item">
            <span class="label">物流单号：</span>
            <span class="value">{{ aftersale.logistics.trackingNo }}</span>
          </div>
          <div class="info-item" v-if="aftersale.logistics.remark">
            <span class="label">备注：</span>
            <span class="value">{{ aftersale.logistics.remark }}</span>
          </div>
        </div>
        
        <div v-if="aftersale.logistics && aftersale.logistics.traces && aftersale.logistics.traces.length > 0" class="logistics-traces">
          <h3 class="sub-title">物流跟踪</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(trace, index) in aftersale.logistics.traces"
              :key="index"
              :timestamp="trace.time"
              :type="index === 0 ? 'primary' : ''"
            >
              {{ trace.content }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      
      <!-- 上传的图片 -->
      <div v-if="aftersale.images && aftersale.images.length > 0" class="section images-section">
        <h2 class="section-title">问题图片</h2>
        <div class="images-list">
          <el-image
            v-for="(image, index) in aftersale.images"
            :key="index"
            :src="image"
            fit="cover"
            class="evidence-image"
            :preview-src-list="aftersale.images"
            :initial-index="index"
          />
        </div>
      </div>
      
      <!-- 处理记录 -->
      <div v-if="aftersale.records && aftersale.records.length > 0" class="section records-section">
        <h2 class="section-title">处理记录</h2>
        <div class="records-list">
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in aftersale.records"
              :key="index"
              :timestamp="record.createTime"
              :type="getRecordType(record.type)"
            >
              <div class="record-content">
                <div class="record-operator">{{ record.operator }}</div>
                <div class="record-message">{{ record.message }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-card>
    
    <!-- 退货信息对话框 -->
    <el-dialog
      v-model="returnDialog.visible"
      title="填写退货信息"
      width="500px"
    >
      <el-form 
        ref="returnFormRef"
        :model="returnDialog.form"
        :rules="returnDialog.rules"
        label-width="100px"
      >
        <el-form-item label="物流公司" prop="company">
          <el-select v-model="returnDialog.form.company" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="EMS" value="EMS" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="物流单号" prop="trackingNo">
          <el-input v-model="returnDialog.form.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
        
        <el-form-item label="备注信息" prop="remark">
          <el-input 
            v-model="returnDialog.form.remark" 
            type="textarea" 
            :rows="3"
            placeholder="可填写备注信息（选填）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="returnDialog.visible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitReturnInfo" 
          :loading="returnDialog.loading"
        >
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getAftersaleDetail, 
  cancelAftersale as cancelAftersaleApi, 
  submitReturnLogistics, 
  confirmAftersaleComplete 
} from '@/api/aftersale';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const aftersale = ref({});
const returnFormRef = ref(null);

// 退货信息对话框
const returnDialog = reactive({
  visible: false,
  loading: false,
  form: {
    company: '',
    trackingNo: '',
    remark: ''
  },
  rules: {
    company: [
      { required: true, message: '请选择物流公司', trigger: 'change' }
    ],
    trackingNo: [
      { required: true, message: '请输入物流单号', trigger: 'blur' },
      { min: 5, message: '物流单号不能少于5个字符', trigger: 'blur' }
    ]
  }
});

// 是否有物流信息
const hasLogistics = computed(() => {
  return aftersale.value && 
    (aftersale.value.logistics || 
     [2, 3, 4].includes(aftersale.value.type));
});

// 获取售后详情
const fetchAftersaleDetail = async () => {
  const aftersaleId = route.params.id;
  if (!aftersaleId) {
    ElMessage.error('售后单ID不能为空');
    router.push('/aftersale/list');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await getAftersaleDetail(aftersaleId);
    aftersale.value = res.data || {};
  } catch (error) {
    console.error('获取售后详情失败:', error);
    ElMessage.error('获取售后详情失败，请重试');
    
    // 使用模拟数据
    mockAftersaleDetail(aftersaleId);
  } finally {
    loading.value = false;
  }
};

// 模拟售后详情数据
const mockAftersaleDetail = (id) => {
  const status = Math.floor(Math.random() * 5) + 1;
  const type = Math.floor(Math.random() * 4) + 1;
  
  aftersale.value = {
    id,
    aftersaleNo: `ASV${Date.now()}`,
    orderNo: `ORDER${Date.now()}`,
    status,
    type,
    reason: '商品质量问题',
    description: '收到商品后发现有明显的划痕，影响使用体验，希望能退换商品。',
    createTime: new Date(Date.now() - 5 * 86400000).toISOString().split('T')[0] + ' 10:30:25',
    processTime: status >= 2 ? new Date(Date.now() - 4 * 86400000).toISOString().split('T')[0] + ' 14:20:10' : '',
    handleTime: status >= 4 ? new Date(Date.now() - 2 * 86400000).toISOString().split('T')[0] + ' 09:15:40' : '',
    finishTime: status === 5 ? new Date(Date.now() - 1 * 86400000).toISOString().split('T')[0] + ' 16:45:30' : '',
    contactName: '张三',
    contactPhone: '13800138000',
    products: [
      {
        itemId: 1001,
        productId: 101,
        name: '测试商品1',
        price: 599.00,
        quantity: 1,
        imageUrl: 'https://picsum.photos/id/1/100/100'
      }
    ],
    images: [
      'https://picsum.photos/id/20/500/500',
      'https://picsum.photos/id/21/500/500',
      'https://picsum.photos/id/22/500/500'
    ],
    logistics: type === 2 && status >= 3 ? {
      company: '顺丰速运',
      trackingNo: `SF${Math.floor(Math.random() * 10000000000)}`,
      remark: '周一至周五上班时间在家',
      traces: [
        {
          time: new Date(Date.now() - 2 * 86400000).toISOString().split('T')[0] + ' 09:15:40',
          content: '快件已由顺丰速运揽收'
        },
        {
          time: new Date(Date.now() - 2 * 86400000).toISOString().split('T')[0] + ' 18:30:22',
          content: '快件已到达杭州转运中心'
        },
        {
          time: new Date(Date.now() - 1 * 86400000).toISOString().split('T')[0] + ' 08:45:10',
          content: '快件已从杭州转运中心发出，下一站上海转运中心'
        },
        {
          time: new Date(Date.now() - 1 * 86400000).toISOString().split('T')[0] + ' 16:45:30',
          content: '快件已到达上海转运中心'
        }
      ]
    } : null,
    records: [
      {
        id: 1,
        type: 'apply',
        operator: '用户',
        message: '提交售后申请',
        createTime: new Date(Date.now() - 5 * 86400000).toISOString().split('T')[0] + ' 10:30:25'
      },
      ...(status >= 2 ? [
        {
          id: 2,
          type: 'approve',
          operator: '客服',
          message: '审核通过，同意进行' + (type === 1 ? '退款' : type === 2 ? '退货退款' : type === 3 ? '换货' : '维修'),
          createTime: new Date(Date.now() - 4 * 86400000).toISOString().split('T')[0] + ' 14:20:10'
        }
      ] : []),
      ...(status >= 3 && type === 2 ? [
        {
          id: 3,
          type: 'info',
          operator: '用户',
          message: '已填写退货物流信息：顺丰速运 SF1234567890',
          createTime: new Date(Date.now() - 3 * 86400000).toISOString().split('T')[0] + ' 11:05:30'
        }
      ] : []),
      ...(status >= 4 ? [
        {
          id: 4,
          type: 'process',
          operator: '客服',
          message: type === 1 ? '退款处理中，预计1-3个工作日到账' : type === 2 ? '已收到退货，正在检查商品' : '商品已发出，请注意查收',
          createTime: new Date(Date.now() - 2 * 86400000).toISOString().split('T')[0] + ' 09:15:40'
        }
      ] : []),
      ...(status === 5 ? [
        {
          id: 5,
          type: 'complete',
          operator: '系统',
          message: type === 1 || type === 2 ? '退款已完成，金额已原路退回' : '商品已送达，售后服务完成',
          createTime: new Date(Date.now() - 1 * 86400000).toISOString().split('T')[0] + ' 16:45:30'
        }
      ] : [])
    ]
  };
};

// 获取状态文字
const getStatusText = (status) => {
  const statusMap = {
    1: '待处理',
    2: '已同意',
    3: '已拒绝',
    4: '处理中',
    5: '已完成',
    6: '已取消'
  };
  
  return statusMap[status] || '未知状态';
};

// 获取状态描述
const getStatusDescription = (status, type) => {
  if (status === 1) return '您的售后申请已提交，正在等待商家处理';
  if (status === 2) {
    if (type === 2) return '商家已同意您的申请，请尽快填写退货物流信息';
    return '商家已同意您的申请，正在处理中';
  }
  if (status === 3) return '很抱歉，商家拒绝了您的申请';
  if (status === 4) {
    if (type === 1) return '退款正在处理中，预计1-3个工作日到账';
    if (type === 2) return '已收到退货，正在检查商品';
    if (type === 3) return '换货商品正在配送中';
    return '商品正在维修中';
  }
  if (status === 5) {
    if (type === 1 || type === 2) return '退款已完成，金额已原路退回';
    return '售后服务已完成';
  }
  if (status === 6) return '您已取消此次售后申请';
  
  return '';
};

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'primary',
    5: 'info',
    6: 'info'
  };
  
  return typeMap[status] || 'info';
};

// 获取类型文字
const getTypeText = (type) => {
  const typeMap = {
    1: '退款',
    2: '退货退款',
    3: '换货',
    4: '维修'
  };
  
  return typeMap[type] || '未知类型';
};

// 获取记录类型
const getRecordType = (type) => {
  const typeMap = {
    'apply': 'primary',
    'approve': 'success',
    'reject': 'danger',
    'process': 'warning',
    'complete': 'success',
    'cancel': 'info',
    'info': ''
  };
  
  return typeMap[type] || '';
};

// 获取进度步骤
const getProgressStep = () => {
  const status = aftersale.value.status;
  
  if (status === 1) return 1;
  if (status === 2) return 2;
  if (status === 3) return 2;
  if (status === 4) return 3;
  if (status === 5) return 4;
  if (status === 6) return 1;
  
  return 1;
};

// 导入 Element Plus 的 ElMessage (如果您想用它来提示)
// import { ElMessage } from 'element-plus';
// import { useRouter } from 'vue-router'; // 假设 router 实例已通过 useRouter 获取

// 查看商品详情
const viewDetail = () => {
  // 1. 检查 props.product 是否存在且有效
  if (!props.product || typeof props.product !== 'object') {
    console.error("viewDetail: props.product 无效或未定义。", props.product);
    // ElMessage.error("无法查看商品详情：商品数据错误。");
    return; // 阻止导航
  }

  // 2. 检查 props.product.id 是否存在且有效
  const productId = props.product.id;
  if (productId === undefined || productId === null || String(productId).trim() === "") {
    console.error("viewDetail: props.product.id 无效或未定义。", productId);
    // ElMessage.error("无法查看商品详情：商品ID缺失。");
    return; // 阻止导航
  }

  // 3. (可选但推荐) 检查ID是否为有效数字或可转换为有效数字的字符串
  //    因为路由参数通常是字符串，但最好确保它代表一个数字。
  //    如果您的ID总是数字，这一步可以简化。
  const numericId = parseInt(productId);
  if (isNaN(numericId) || numericId <= 0) {
    console.error("viewDetail: props.product.id 不是一个有效的正整数ID。", productId, "转换后:", numericId);
    // ElMessage.error("无法查看商品详情：商品ID格式不正确。");
    return; // 阻止导航
  }

  // 4. 只有当ID有效时，才执行路由跳转
  //    使用 numericId (如果是数字ID) 或 productId (如果是字符串ID且后端能处理)
  router.push(`/product/${numericId}`); // 或者 router.push(`/product/${productId}`);
};

// 取消售后
const cancelAftersale = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该售后申请吗？',
      '取消售后',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await cancelAftersaleApi(aftersale.value.id);
    ElMessage.success('售后申请已取消');
    fetchAftersaleDetail();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消售后失败:', error);
      ElMessage.error('操作失败，请重试');
    }
  }
};

// 填写退货信息
const fillReturnInfo = () => {
  // 重置表单
  Object.assign(returnDialog.form, {
    company: '',
    trackingNo: '',
    remark: ''
  });
  
  returnDialog.visible = true;
};

// 提交退货信息
const submitReturnInfo = async () => {
  if (!returnFormRef.value) return;
  
  try {
    await returnFormRef.value.validate();
    
    returnDialog.loading = true;
    
    await submitReturnLogistics(aftersale.value.id, {
      company: returnDialog.form.company,
      trackingNo: returnDialog.form.trackingNo,
      remark: returnDialog.form.remark
    });
    
    ElMessage.success('退货信息提交成功');
    returnDialog.visible = false;
    fetchAftersaleDetail();
  } catch (error) {
    console.error('提交退货信息失败:', error);
    ElMessage.error('提交失败，请重试');
  } finally {
    returnDialog.loading = false;
  }
};

// 确认完成
const confirmComplete = async () => {
  try {
    await ElMessageBox.confirm(
      '确认售后服务已完成吗？',
      '确认完成',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    );
    
    await confirmAftersaleComplete(aftersale.value.id);
    ElMessage.success('操作成功');
    fetchAftersaleDetail();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认完成失败:', error);
      ElMessage.error('操作失败，请重试');
    }
  }
};

onMounted(() => {
  fetchAftersaleDetail();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.aftersale-detail-container {
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
    
    .aftersale-actions {
      display: flex;
      gap: $spacing-small;
    }
  }
  
  .progress-section {
    margin-bottom: $spacing-large;
    padding: $spacing-base;
    background-color: #fff;
    border-radius: $border-radius-base;
  }
  
  .section {
    margin-bottom: $spacing-large;
    
    .section-title {
      font-size: $font-size-large;
      margin-bottom: $spacing-base;
      padding-bottom: $spacing-small;
      border-bottom: 1px solid $border-lighter;
    }
    
    .sub-title {
      font-size: $font-size-medium;
      margin-bottom: $spacing-base;
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
    .product-item {
      display: flex;
      padding: $spacing-base;
      border: 1px solid $border-lighter;
      border-radius: $border-radius-base;
      margin-bottom: $spacing-base;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .product-image {
        width: 80px;
        height: 80px;
        margin-right: $spacing-base;
        overflow: hidden;
        border-radius: $border-radius-small;
        
        .el-image {
          width: 100%;
          height: 100%;
          cursor: pointer;
        }
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
          cursor: pointer;
          
          &:hover {
            color: $primary-color;
          }
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
  
  .images-list {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-base;
    
    .evidence-image {
      width: 120px;
      height: 120px;
      border-radius: $border-radius-small;
      cursor: pointer;
    }
  }
  
  .records-list {
    .record-content {
      .record-operator {
        font-weight: bold;
        margin-bottom: $spacing-small;
      }
      
      .record-message {
        color: $text-regular;
      }
    }
  }
}

@include respond-to(md) {
  .aftersale-detail-container {
    .status-section {
      flex-direction: column;
      align-items: flex-start;
      
      .status-info {
        margin-bottom: $spacing-base;
      }
    }
  }
}
</style> 