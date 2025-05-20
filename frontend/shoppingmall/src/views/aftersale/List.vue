<template>
  <div class="aftersale-list-container">
    <h1 class="page-title">售后服务</h1>
    
    <!-- 售后状态选项卡 -->
    <el-tabs v-model="activeStatus" @tab-click="handleStatusChange">
      <el-tab-pane label="全部" name="0"></el-tab-pane>
      <el-tab-pane label="处理中" name="1"></el-tab-pane>
      <el-tab-pane label="已完成" name="2"></el-tab-pane>
      <el-tab-pane label="已取消" name="3"></el-tab-pane>
    </el-tabs>
    
    <!-- 售后列表 -->
    <el-card v-if="!loading">
      <div v-if="aftersaleList.length === 0" class="empty-list">
        <el-empty description="暂无售后记录" />
      </div>
      
      <div v-else class="aftersale-list">
        <div 
          v-for="item in aftersaleList" 
          :key="item.id" 
          class="aftersale-item"
        >
          <div class="aftersale-header">
            <div class="aftersale-info">
              <span class="aftersale-no">售后单号：{{ item.aftersaleNo }}</span>
              <span class="create-time">申请时间：{{ item.createTime }}</span>
            </div>
            <div class="aftersale-status">
              <el-tag :type="getStatusType(item.status)">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
          </div>
          
          <div class="aftersale-content">
            <div class="aftersale-type">
              <span class="label">售后类型：</span>
              <span class="value">{{ getTypeText(item.type) }}</span>
            </div>
            
            <div class="products">
              <div
                v-for="product in item.products"
                :key="product.itemId"
                class="product-item"
              >
                <el-image 
                  :src="product.imageUrl" 
                  :alt="product.name"
                  class="product-image"
                  @click="goToProductDetail(product.productId)"
                />
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
            
            <div class="aftersale-reason">
              <span class="label">售后原因：</span>
              <span class="value">{{ item.reason }}</span>
            </div>
          </div>
          
          <div class="aftersale-footer">
            <div class="aftersale-actions">
              <el-button 
                type="primary" 
                plain
                size="small"
                @click="goToAftersaleDetail(item.id)"
              >
                查看详情
              </el-button>
              
              <el-button 
                v-if="item.status === 1" 
                type="danger" 
                plain
                size="small"
                @click="cancelAftersale(item)"
              >
                取消申请
              </el-button>
              
              <el-button 
                v-if="item.status === 2 && item.type === 2" 
                type="warning" 
                plain
                size="small"
                @click="fillReturnInfo(item)"
              >
                填写退货信息
              </el-button>
              
              <el-button 
                v-if="item.status === 4" 
                type="success" 
                plain
                size="small"
                @click="confirmComplete(item)"
              >
                确认完成
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
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getAftersaleList, 
  cancelAftersale as cancelAftersaleApi, 
  submitReturnLogistics, 
  confirmAftersaleComplete 
} from '@/api/aftersale';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const activeStatus = ref('0');
const aftersaleList = ref([]);
const total = ref(0);
const returnFormRef = ref(null);

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  status: 0
});

// 退货信息对话框
const returnDialog = reactive({
  visible: false,
  loading: false,
  aftersaleId: null,
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

// 获取售后列表
const fetchAftersaleList = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await getAftersaleList(queryParams);
    aftersaleList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error('获取售后列表失败:', error);
    ElMessage.error('获取售后列表失败，请重试');
    
    // 使用模拟数据
    mockAftersaleList();
  } finally {
    loading.value = false;
  }
};

// 模拟售后数据
const mockAftersaleList = () => {
  const list = [];
  const status = parseInt(activeStatus.value);
  
  for (let i = 1; i <= queryParams.pageSize; i++) {
    // 如果选择了特定状态，只显示该状态的售后
    const aftersaleStatus = status === 0 ? (i % 5) + 1 : status;
    const aftersaleType = (i % 4) + 1;
    
    list.push({
      id: i,
      aftersaleNo: `ASV${Date.now()}${i}`,
      status: aftersaleStatus,
      type: aftersaleType,
      reason: '商品质量问题',
      createTime: new Date(Date.now() - i * 86400000).toISOString().split('T')[0],
      products: [
        {
          itemId: i * 100 + 1,
          productId: i * 10 + 1,
          name: `测试商品 ${i * 10 + 1}`,
          price: (Math.random() * 100 + 50).toFixed(2),
          quantity: Math.floor(Math.random() * 3) + 1,
          imageUrl: `https://picsum.photos/id/${i * 10 + 1}/100/100`
        }
      ]
    });
  }
  
  aftersaleList.value = list;
  total.value = 100;
};

// 处理状态变化
const handleStatusChange = () => {
  queryParams.status = parseInt(activeStatus.value);
  queryParams.page = 1;
  fetchAftersaleList();
};

// 处理页码变化
const handleCurrentChange = (page) => {
  queryParams.page = page;
  fetchAftersaleList();
};

// 处理每页数量变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size;
  queryParams.page = 1;
  fetchAftersaleList();
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

// 跳转到售后详情
const goToAftersaleDetail = (id) => {
  router.push(`/aftersale/detail/${id}`);
};

// 取消售后
const cancelAftersale = async (item) => {
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
    
    await cancelAftersaleApi(item.id);
    ElMessage.success('售后申请已取消');
    fetchAftersaleList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消售后失败:', error);
      ElMessage.error('操作失败，请重试');
    }
  }
};

// 填写退货信息
const fillReturnInfo = (item) => {
  returnDialog.aftersaleId = item.id;
  
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
    
    await submitReturnLogistics(returnDialog.aftersaleId, {
      company: returnDialog.form.company,
      trackingNo: returnDialog.form.trackingNo,
      remark: returnDialog.form.remark
    });
    
    ElMessage.success('退货信息提交成功');
    returnDialog.visible = false;
    fetchAftersaleList();
  } catch (error) {
    console.error('提交退货信息失败:', error);
    ElMessage.error('提交失败，请重试');
  } finally {
    returnDialog.loading = false;
  }
};

// 确认完成
const confirmComplete = async (item) => {
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
    
    await confirmAftersaleComplete(item.id);
    ElMessage.success('操作成功');
    fetchAftersaleList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认完成失败:', error);
      ElMessage.error('操作失败，请重试');
    }
  }
};

onMounted(() => {
  fetchAftersaleList();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.aftersale-list-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .empty-list {
    padding: 50px 0;
    display: flex;
    justify-content: center;
  }
  
  .aftersale-list {
    .aftersale-item {
      margin-bottom: $spacing-large;
      padding: $spacing-base;
      border: 1px solid $border-lighter;
      border-radius: $border-radius-base;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .aftersale-header {
        display: flex;
        justify-content: space-between;
        padding-bottom: $spacing-small;
        border-bottom: 1px solid $border-lighter;
        margin-bottom: $spacing-base;
        
        .aftersale-info {
          .aftersale-no {
            margin-right: $spacing-base;
            font-weight: bold;
          }
          
          .create-time {
            color: $text-secondary;
          }
        }
      }
      
      .aftersale-content {
        margin-bottom: $spacing-base;
        
        .aftersale-type {
          margin-bottom: $spacing-small;
          
          .label {
            color: $text-secondary;
          }
          
          .value {
            font-weight: bold;
          }
        }
        
        .products {
          margin: $spacing-base 0;
          
          .product-item {
            display: flex;
            padding: $spacing-small 0;
            
            .product-image {
              width: 80px;
              height: 80px;
              margin-right: $spacing-base;
              border-radius: $border-radius-small;
              cursor: pointer;
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
                }
                
                .quantity {
                  color: $text-secondary;
                }
              }
            }
          }
        }
        
        .aftersale-reason {
          .label {
            color: $text-secondary;
          }
        }
      }
      
      .aftersale-footer {
        display: flex;
        justify-content: flex-end;
        padding-top: $spacing-base;
        border-top: 1px solid $border-lighter;
        
        .aftersale-actions {
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
}

@include respond-to(md) {
  .aftersale-list-container {
    .aftersale-list {
      .aftersale-item {
        .aftersale-header {
          flex-direction: column;
          
          .aftersale-status {
            margin-top: $spacing-small;
          }
        }
        
        .aftersale-footer {
          .aftersale-actions {
            flex-wrap: wrap;
            
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