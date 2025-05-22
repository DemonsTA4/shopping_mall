<template>
  <div class="checkout-container">
    <h1 class="page-title">订单结算</h1>
    
    <el-card v-if="!loading">
      <!-- 收货地址选择 -->
      <div class="section address-section">
        <div class="section-header">
          <h2>收货地址</h2>
          <el-button type="primary" text @click="showAddAddressDialog">
            + 添加新地址
          </el-button>
        </div>
        
        <div v-if="addressList.length === 0" class="empty-address">
          <el-empty description="暂无收货地址" />
        </div>
        
        <div v-else class="address-list">
          <el-radio-group v-model="selectedAddressId" @change="onAddressSelectionChange" >
            <div 
              v-for="address in addressList" 
              :key="address.id" 
              class="address-item"
              :class="{ active: selectedAddressId === address.id }"
            >
              <el-radio :label="address.id">
                <div class="address-info">
                  <div class="address-header">
                    <span class="name">{{ address.name }}</span>
                    <span class="phone">{{ address.phone }}</span>
                    <el-tag v-if="address.isDefault" size="small" type="danger">默认</el-tag>
                  </div>
                  <div class="address-detail">
                    {{ formatAddress(address) }}
                  </div>
                </div>
              </el-radio>
              
              <div class="address-actions">
                <el-button type="primary" text size="small" @click="editAddress(address)">
                  修改
                </el-button>
                <el-button type="danger" text size="small" @click="deleteAddressConfirm(address)">
                  删除
                </el-button>
              </div>
            </div>
          </el-radio-group>
        </div>
      </div>
      
      <!-- 商品信息 -->
      <div class="section order-items-section">
        <div class="section-header">
          <h2>商品信息</h2>
        </div>
        
        <div class="order-items">
          <div class="table-header">
            <div class="col product-col">商品</div>
            <div class="col price-col">单价</div>
            <div class="col quantity-col">数量</div>
            <div class="col subtotal-col">小计</div>
          </div>
          
          <div class="table-body">
            <div 
              v-for="item in selectedItems" 
              :key="item.id" 
              class="order-item"
            >
              <div class="col product-col">
                <div class="product-info">
                  <el-image :src="item.productImage" class="product-image" />
                  <div class="product-name">{{ item.name }}</div>
                </div>
              </div>
              <div class="col price-col">¥{{ item.price }}</div>
              <div class="col quantity-col">{{ item.quantity }}</div>
              <div class="col subtotal-col">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 订单备注 -->
      <div class="section remark-section">
        <div class="section-header">
          <h2>订单备注</h2>
        </div>
        
        <div class="remark-input">
          <el-input 
            v-model="orderRemark" 
            type="textarea" 
            placeholder="请输入订单备注信息（选填）" 
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>
      
      <!-- 支付方式 -->
      <div class="section payment-section">
        <div class="section-header">
          <h2>支付方式</h2>
        </div>
        
        <div class="payment-methods">
          <el-radio-group v-model="paymentMethod">
            <el-radio label="alipay">
              <div class="payment-method-item">
                <i class="payment-icon alipay-icon"></i>
                <span>支付宝</span>
              </div>
            </el-radio>
            <el-radio label="wechat">
              <div class="payment-method-item">
                <i class="payment-icon wechat-icon"></i>
                <span>微信支付</span>
              </div>
            </el-radio>
            <el-radio label="credit">
              <div class="payment-method-item">
                <i class="payment-icon credit-icon"></i>
                <span>信用卡</span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <!-- 价格信息 -->
      <div class="price-summary">
        <div class="price-item">
          <span class="label">商品总价：</span>
          <span class="value">¥{{ totalPrice }}</span>
        </div>
        <div class="price-item">
          <span class="label">运费：</span>
          <span class="value">¥{{ shippingFee }}</span>
        </div>
        <div class="price-item total">
          <span class="label">应付总额：</span>
          <span class="value">¥{{ orderTotal }}</span>
        </div>
      </div>
      
      <!-- 提交订单 -->
      <div class="submit-order">
        <el-button 
          type="primary" 
          size="large" 
          :loading="submitting" 
          :disabled="!canSubmit"
          @click="handleSubmitOrder"
        >
          提交订单
        </el-button>
      </div>
    </el-card>
    
    <!-- 添加/编辑地址对话框 -->
    <el-dialog 
      v-model="addressDialog.visible" 
      :title="addressDialog.isEdit ? '编辑地址' : '添加地址'"
      width="500px"
    >
      <el-form 
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择所在地区"
          />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detail">
          <el-input 
            v-model="addressForm.detail" 
            type="textarea" 
            :rows="3"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        
        <el-form-item label="邮政编码" prop="zipCode">
          <el-input v-model="addressForm.zipCode" placeholder="请输入邮政编码" />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认收货地址</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="addressDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="addressDialog.loading">
          保存
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 提交成功对话框 -->
    <el-dialog
      v-model="successDialog.visible"
      title="订单提交成功"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="success-info">
        <el-icon class="success-icon" color="#67C23A" :size="60"><CircleCheck /></el-icon>
        <p class="success-text">订单已提交成功，请尽快完成支付</p>
        <p class="order-number">订单号：{{ successDialog.orderNo }}</p>
      </div>
      
      <template #footer>
        <el-button @click="goToOrderList">查看订单</el-button>
        <el-button type="primary" @click="goToPay">
          去支付 (¥{{ successDialog.amountToPay }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/store/modules/cart';
import { useUserStore } from '@/store/modules/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { submitOrder } from '@/api/order';
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/user';
import { regionData } from '@/utils/region-data';
import { formatUtcToLocal } from '@/utils/datetime';

const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();
const loading = ref(true);
const submitting = ref(false);
const addressList = ref([]);
const selectedAddressId = ref('');
const orderRemark = ref('');
const paymentMethod = ref('alipay');
const shippingFee = ref(0);
const regionOptions = regionData;
const dataReady = ref(false);

// 表单相关
const addressFormRef = ref(null);
const addressDialog = reactive({
  visible: false,
  isEdit: false,
  loading: false,
  editId: null
});

const addressForm = reactive({
  name: '',
  phone: '',
  region: [],
  detail: '',
  zipCode: '',
  isDefault: false
});

const addressRules = {
  name: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  zipCode: [
    { pattern: /^\d{6}$/, message: '请输入正确的邮政编码', trigger: 'blur' }
  ]
};

// 提交成功对话框
const successDialog = reactive({
  visible: false,
  orderNo: '',
  amountToPay: '0.00'
});

// 计算属性：选中的商品列表
const selectedItems = computed(() => {
  if (!cartStore.hasLoadedOnce && !dataReady.value) return []; // 防止在数据未加载时计算
  return cartStore.cartItems.filter(item => item.selected).map(item => ({
      ...item,
      price: parseFloat(item.price) || 0, // 再次确保price是数字
      quantity: parseInt(item.quantity) || 0 // 确保quantity是数字
  }));
});

const initializeCheckout = async () => {
  if (!userStore.isLogin) { /* ... 跳转 ... */ return; }

  // 如果 cartStore 还没有从服务器加载过数据，则等待或触发一次
  if (!cartStore.hasLoadedOnce) {
    console.log('[Checkout.vue] Cart data not loaded yet from store, attempting to fetch...');
    await cartStore.fetchCart(); // 确保购物车数据已加载
  }

  // 现在可以安全使用 cartStore.cartItems
  console.log('[Checkout.vue] Cart items from store for checkout:', JSON.parse(JSON.stringify(cartStore.cartItems)));

  if (selectedItems.value.length === 0) { // selectedItems 依赖 cartStore.cartItems
    ElMessage.warning('请先选择要结算的商品');
    router.push('/cart');
    return;
  }

  await fetchAddressList(); // 获取地址列表
  dataReady.value = true; // 标记数据准备完毕
};

// 计算属性：商品总价
const totalPrice = computed(() => {
  if (!dataReady.value) return '0.00'; // 等待数据准备好
  return selectedItems.value
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
    .toFixed(2);
});

// 计算属性：订单总价
const orderTotal = computed(() => {
  return (parseFloat(totalPrice.value) + shippingFee.value).toFixed(2);
});

// 计算属性：是否可以提交订单
const canSubmit = computed(() => {
  return selectedItems.value.length > 0 && selectedAddressId.value;
});

const fetchAddressList = async () => {
  console.log('[fetchAddressList] Function called.'); // 1. 确认函数被调用
  try {
    const res = await getAddressList();
    console.log('[fetchAddressList] API response (res):', JSON.parse(JSON.stringify(res))); // 2. ★★★ 打印完整的API响应 ★★★

    // 假设您的后端Result结构是 { code: 200, message: "...", data: [...] }
    if (res && res.code === 200 && Array.isArray(res.data)) {
      console.log('[fetchAddressList] API success. Data received:', JSON.parse(JSON.stringify(res.data))); // 3. 打印API返回的地址数组

      // 在这里，您可以检查 res.data 中的每个对象是否包含所有必需的字段
      // 并且字段名是否与模板中使用的匹配。
      // 例如，如果模板用 address.receiverName，确保 res.data 的每个元素都有 receiverName
      addressList.value = res.data; // 直接赋值，假设字段名已匹配

      console.log('[fetchAddressList] addressList.value updated:', JSON.parse(JSON.stringify(addressList.value))); // 4. ★★★ 打印更新后的本地 addressList.value ★★★

      const defaultAddress = addressList.value.find(item => item.isDefault === true); // 确保比较的是布尔值 true
      if (defaultAddress) {
        selectedAddressId.value = defaultAddress.id;
        console.log('[fetchAddressList] Default address found and selected:', defaultAddress.id);
      } else if (addressList.value.length > 0) {
        selectedAddressId.value = addressList.value[0].id;
        console.log('[fetchAddressList] No default address, first address selected:', addressList.value[0].id);
      } else {
        selectedAddressId.value = null; // 如果列表为空，清除选中ID
        console.log('[fetchAddressList] Address list is empty, selectedAddressId cleared.');
      }
    } else {
      // 处理API返回非200业务码或数据格式不正确的情况
      console.warn('[fetchAddressList] API call did not return successful data or data is not an array. Response:', res);
      ElMessage.error(res?.message || '获取地址列表数据格式不正确');
      addressList.value = []; // 清空或保持旧数据，根据需求
      selectedAddressId.value = null;
    }
  } catch (error) {
    console.error('获取地址列表失败 (exception):', error);
    ElMessage.error(error.message || '获取地址列表失败，请重试');
    addressList.value = []; // 清空或保持旧数据
    selectedAddressId.value = null;
  }
};

// 格式化地址
const formatAddress = (address) => {
  return `${address.province || ''} ${address.city || ''} ${address.district || ''} ${address.detail || ''}`;
};

// 显示添加地址对话框
const showAddAddressDialog = () => {
  resetAddressForm();
  addressDialog.isEdit = false;
  addressDialog.editId = null;
  addressDialog.visible = true;
};

// 编辑地址
const editAddress = (address) => {
  resetAddressForm();
  addressDialog.isEdit = true;
  addressDialog.editId = address.id;
  
  // 填充表单数据
  addressForm.name = address.name;
  addressForm.phone = address.phone;
  addressForm.region = [address.province, address.city, address.district].filter(Boolean);
  addressForm.detail = address.detail;
  addressForm.zipCode = address.zipCode;
  addressForm.isDefault = address.isDefault;
  
  addressDialog.visible = true;
};

// 重置地址表单
const resetAddressForm = () => {
  if (addressFormRef.value) {
    addressFormRef.value.resetFields();
  }
  
  Object.assign(addressForm, {
    name: '',
    phone: '',
    region: [],
    detail: '',
    zipCode: '',
    isDefault: false
  });
};

// 删除地址确认
const deleteAddressConfirm = async (address) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该收货地址吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await deleteAddress(address.id);
    ElMessage.success('删除成功');
    fetchAddressList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error);
      ElMessage.error('删除失败，请重试');
    }
  }
};

// 在 Checkout.vue 或类似组件的 <script setup> 中

const saveAddress = async () => {
  if (!addressFormRef.value) return;

  try {
    // 使用 validate 的回调形式或 .then().catch() 来处理校验结果
    // 这样可以避免校验失败的 fields 对象被当成 Error 抛到最外层 catch
    const valid = await addressFormRef.value.validate().catch((validationErrors) => {
      console.log('表单校验失败详情:', validationErrors); // validationErrors 就是 {name: [...]} 这样的对象
      ElMessage.error('请检查表单，部分信息填写不正确！');
      return false; // 返回 false 表示校验失败
    });

    if (!valid) { // 如果 valid 是 false (即校验失败)
      return; // 校验失败，直接返回，不执行后续API调用
    }

    // --- 如果执行到这里，说明表单校验通过 ---
    addressDialog.loading = true;

    const [province, city, district] = addressForm.region || []; // 给 region 一个默认空数组防止解构错误
    const payload = {
      receiverName: addressForm.name,
      receiverPhone: addressForm.phone,
      province: province || null,
      city: city || null,
      district: district || null,
      detailedAddress: addressForm.detail,
      postalCode: addressForm.zipCode || null,
      isDefault: addressForm.isDefault,
      // id: addressDialog.isEdit ? addressDialog.editId : undefined // 如果是创建，不传id
    };
    if (addressDialog.isEdit && addressDialog.editId) {
        payload.id = addressDialog.editId;
    }


    console.log('准备发送给后端的地址数据 (payload):', payload);

    let apiResponse;
    if (addressDialog.isEdit) {
      apiResponse = await updateAddress(addressDialog.editId, payload); // 假设 updateAddress 是您的API函数
    } else {
      apiResponse = await addAddress(payload); // 假设 addAddress 是您的API函数
    }

    // 检查后端业务层是否真的成功
    // 假设您的 Result.success() 会设置 code 为 200
    // 并且 addAddress/updateAddress 返回的是 Result<AddressDto> 结构
    if (apiResponse && apiResponse.code === 200) {
      ElMessage.success(addressDialog.isEdit ? '更新地址成功' : '添加地址成功');
      addressDialog.visible = false;
      fetchAddressList(); // 刷新地址列表
    } else {
      // 后端返回了非200的业务码，或者没有返回期望的结构
      ElMessage.error(apiResponse?.message || (addressDialog.isEdit ? '更新失败' : '添加失败，请检查数据'));
    }

  } catch (apiCallError) {
    // 这个 catch 现在主要捕获 API 调用本身发生的网络错误或 request.js 中 reject 的错误
    console.error('保存地址时API调用或网络层面失败:', apiCallError);
    let errorMessage = addressDialog.isEdit ? '更新地址失败，请重试' : '添加地址失败，请重试';
    if (apiCallError && apiCallError.message) { // 优先使用错误对象中的 message
        errorMessage = apiCallError.message;
    }
    ElMessage.error(errorMessage);
  } finally {
    addressDialog.loading = false;
  }
};

const handleSubmitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址');
    return;
  }

  // selectedItems 是一个 computed 属性，它已经处理了过滤和数据转换
  if (selectedItems.value.length === 0) {
    ElMessage.warning('购物车中没有选中的商品或选中的商品数据无效');
    // 可以在这里加一个 console.log(cartStore.cartItems) 来检查原始购物车数据
    return;
  }

  // 1. 根据 selectedAddressId 从 addressList 中找到选中的地址对象
  const selectedFullAddress = addressList.value.find(
    (addr) => addr.id === selectedAddressId.value
  );

  if (!selectedFullAddress) {
    ElMessage.error('未能获取到选中的地址详细信息，请刷新重试');
    return;
  }

  try {
    submitting.value = true;

    // 2. 准备订单项 (items)
    //    确保 selectedItems.value 中的每个 item 结构符合后端 OrderItemRequestDto
    //    通常至少包含 productId 和 quantity。
    //    selectedItems computed 属性已经做了初步的 map，这里直接用。
    const orderItemsPayload = selectedItems.value.map(item => ({
      productId: item.productId || item.id, // 根据你的商品对象实际的ID字段名调整
      quantity: item.quantity
      // 如果后端 OrderItemRequestDto 还需要其他字段（如当时的价格），也需要在这里添加
    }));

    if (orderItemsPayload.length === 0) {
        ElMessage.warning('选中的商品列表为空，无法提交订单。');
        submitting.value = false;
        return;
    }
	
	// ...
	    let paymentMethodCode = 1; // 默认为1 (支付宝)
	    if (paymentMethod.value === 'alipay') {
	      paymentMethodCode = 1;
	    } else if (paymentMethod.value === 'wechat') {
	      paymentMethodCode = 2;
	    } else if (paymentMethod.value === 'credit') {
	      paymentMethodCode = 3;
	    }

    // 3. 构建符合后端 OrderCreateRequestDto 的数据对象
    const data = {
      items: orderItemsPayload, // ★★★ 添加订单项列表

      // ★★★ 使用从 addressList 找到的详细地址信息
      // 注意：这里的字段名需要和你后端 OrderCreateRequestDto 中的完全一致
      shippingAddress: `${selectedFullAddress.province || ''}${selectedFullAddress.city || ''}${selectedFullAddress.district || ''}${selectedFullAddress.detail || ''}`,
      receiverName: selectedFullAddress.name,
      receiverPhone: selectedFullAddress.phone,
      receiverProvince: selectedFullAddress.province || null, // 如果字段允许null
      receiverCity: selectedFullAddress.city || null,
      receiverDistrict: selectedFullAddress.district || null,
      receiverPostalCode: selectedFullAddress.zipCode || null,

      paymentMethod: paymentMethodCode, // ★★★ 确保 paymentMethod 是 Integer 类型 (假设后端是Integer)
                                                          // 如果你的 paymentMethod.value 是字符串如 "alipay", 
                                                          // 而后端 OrderCreateRequestDto 的 paymentMethod 是 Integer (例如 1代表支付宝, 2代表微信)
                                                          // 你需要在这里进行转换。
                                                          // 如果后端 paymentMethod 也是字符串，则直接用 paymentMethod.value
      notes: orderRemark.value // ★★★ 字段名从 remark 改为 notes (与你的 DTO 匹配)
    };

    console.log('Submitting order with data:', JSON.stringify(data, null, 2));

    // 提交订单 (submitOrder 是你封装的 API 调用)
    const res = await submitOrder(data);

    if (res && res.code === 200) { // 假设您的API响应结构包含 code
        // ★★★ 修改处：直接使用 orderTotal.value ★★★
        // 假设 orderTotal 是在 <script setup> 中定义的 computed 或 ref
        const finalOrderTotalForDisplay = orderTotal.value; 
        // ★★★ 在清空购物车前，保存当时的订单总额 ★★★
    
        // ★★★ 修改处：直接使用 selectedItems.value ★★★
        // 假设 selectedItems 是在 <script setup> 中定义的 computed 或 ref
        const itemIdsToRemove = selectedItems.value.map(item => item.id); 
    
        // ★★★ 修改处：直接使用 cartStore ★★★
        // 确保 cartStore.removeItemsByIds 函数已在您的 cartStore 中定义
        // 假设 cartStore 是通过 const cartStore = useCartStore(); 在 <script setup> 中定义的
        if (typeof cartStore.removeItemsByIds === 'function') { 
            cartStore.removeItemsByIds(itemIdsToRemove);
        } else {
            console.error("cartStore.removeItemsByIds is not a function! Cart items not cleared from frontend state.");
            // 即使这里报错，订单可能已在后端创建成功，只是前端购物车未清理
        }
    
        // ★★★ 修改处：直接使用 successDialog (假设它是 reactive 对象) ★★★
        successDialog.orderNo = res.data.orderNo || `ORDER${Date.now()}`;
        successDialog.amountToPay = finalOrderTotalForDisplay; // ★★★ 将保存的金额赋给对话框的一个新属性 ★★★
        successDialog.visible = true;
    } else {
      // API 返回了业务错误
      ElMessage.error(res?.message || '提交订单失败，请稍后再试');
    }

  } catch (error) {
    console.error('提交订单失败 (exception):', error);
    ElMessage.error(error?.response?.data?.message || error?.message || '提交订单时发生错误，请重试');
  } finally {
    submitting.value = false;
  }
};

const initializePageData = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    loading.value = false;
    return;
  }

  // 确保 cartStore 的数据已加载
  // 方式1: 如果 cartStore.fetchCart() 返回 Promise 并且 App.vue 等待了它，
  // 或者 cartStore 有一个 isReady / hasLoadedOnce 状态
  if (!cartStore.hasLoadedOnce) { // 假设 cartStore 有这个状态
    console.log('[Checkout.vue] Cart not yet loaded by store, awaiting fetchCart...');
    await cartStore.fetchCart(); // 再次确保，或者等待
    console.log('[Checkout.vue] cartStore.fetchCart() completed.');
  }
  
  // 打印此时的 cartItems 以便调试
  console.log('[Checkout.vue] cartStore.cartItems after ensuring load:', JSON.parse(JSON.stringify(cartStore.cartItems)));
  console.log('[Checkout.vue] selectedItems based on current store:', JSON.parse(JSON.stringify(selectedItems.value)));


  if (selectedItems.value.length === 0) {
    ElMessage.warning('购物车中没有选中的商品或购物车为空，请重新选择。');
    router.push('/cart');
    loading.value = false;
    return;
  }

  await fetchAddressList(); // 获取地址列表
  loading.value = false; // 所有数据加载完毕
};


// 去支付
const goToPay = () => {
  router.push(`/order/pay/${successDialog.orderNo}`);
  successDialog.visible = false;
};

// 查看订单列表
const goToOrderList = () => {
  router.push('/order/list');
  successDialog.visible = false;
};

const onAddressSelectionChange = (newlySelectedId) => {
  console.log('Address selection changed. New selectedAddressId:', newlySelectedId);
  console.log('Type of newlySelectedId:', typeof newlySelectedId);
  console.log('Current selectedAddressId.value (after v-model update, might be async):', selectedAddressId.value);
};

onMounted(() => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品');
    router.push('/cart');
    return;
  }
  
  console.log('[Checkout.vue] Component mounted.');
  fetchAddressList();
  initializeCheckout();
  initializePageData();
});

watch(selectedAddressId, (newValue, oldValue) => {
  console.log(`selectedAddressId changed from ${oldValue} to ${newValue}, type: ${typeof newValue}`);
});

</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.checkout-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .section {
    margin-bottom: $spacing-large;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid $border-lighter;
      padding-bottom: $spacing-small;
      margin-bottom: $spacing-base;
      
      h2 {
        font-size: $font-size-large;
        font-weight: bold;
        margin: 0;
      }
    }
  }
  
  .address-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-base;
    
    .address-item {
      border: 1px solid $border-lighter;
      border-radius: $border-radius-base;
      padding: $spacing-base;
      transition: all 0.3s;
      display: flex;
      flex-direction: column;
      
      &.active {
        border-color: $primary-color;
        background-color: rgba($primary-color, 0.05);
      }
      
      .address-info {
        flex: 1;
        
        .address-header {
          display: flex;
          align-items: center;
          margin-bottom: $spacing-small;
          
          .name {
            font-weight: bold;
            margin-right: $spacing-small;
          }
          
          .phone {
            color: $text-secondary;
            margin-right: $spacing-small;
          }
        }
        
        .address-detail {
          color: $text-regular;
          line-height: 1.5;
        }
      }
      
      .address-actions {
        margin-top: $spacing-small;
        display: flex;
        justify-content: flex-end;
        gap: $spacing-small;
      }
    }
  }
  
  .order-items {
    .table-header {
      display: flex;
      background-color: #f5f7fa;
      padding: $spacing-small $spacing-base;
      font-weight: bold;
    }
    
    .order-item {
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
    
    .subtotal-col {
      width: 100px;
      justify-content: center;
      font-weight: bold;
      color: $danger-color;
    }
  }
  
  .payment-methods {
    .payment-method-item {
      display: flex;
      align-items: center;
      
      .payment-icon {
        width: 24px;
        height: 24px;
        margin-right: $spacing-small;
        background-size: contain;
        background-repeat: no-repeat;
        background-position: center;
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
    }
  }
  
  .price-summary {
    text-align: right;
    padding: $spacing-base;
    background-color: #f5f7fa;
    border-radius: $border-radius-base;
    
    .price-item {
      margin-bottom: $spacing-small;
      
      &.total {
        font-size: $font-size-large;
        font-weight: bold;
        margin-top: $spacing-base;
      }
      
      .label {
        margin-right: $spacing-small;
      }
      
      .value {
        color: $danger-color;
      }
    }
  }
  
  .submit-order {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-large;
  }
  
  .success-info {
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
    
    .order-number {
      color: $text-secondary;
    }
  }
}

@include respond-to(md) {
  .checkout-container {
    .address-list {
      grid-template-columns: repeat(1, 1fr);
    }
  }
}

@include respond-to(sm) {
  .checkout-container {
    .order-items {
      .table-header {
        display: none;
      }
      
      .order-item {
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