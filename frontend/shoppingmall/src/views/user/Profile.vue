<template>
  <div class="user-profile-container">
    <h1 class="page-title">个人中心</h1>
    
    <el-row :gutter="20">
      <!-- 左侧菜单 -->
      <el-col :span="6" :xs="24" :sm="8" :md="6">
        <el-card class="menu-card">
          <div class="user-info">
            <el-avatar :size="80" :src="userInfo.avatar">
              {{ userInfo.nickname?.substring(0, 1) || userInfo.username?.substring(0, 1) }}
            </el-avatar>
            <div class="user-name">{{ userInfo.nickname || userInfo.username }}</div>
          </div>
          
          <el-menu
            :default-active="activeMenu"
            class="profile-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>个人资料</span>
            </el-menu-item>
            <el-menu-item index="address">
              <el-icon><Location /></el-icon>
              <span>收货地址</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>账号安全</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <!-- 右侧内容 -->
      <el-col :span="18" :xs="24" :sm="16" :md="18">
        <el-card v-if="!loading">
          <!-- 个人资料 -->
          <div v-if="activeMenu === 'profile'" class="profile-section">
            <h2 class="section-title">个人资料</h2>
            
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
              
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
              </el-form-item>
              
              <el-form-item :label="t('user.country')" prop="country">
                <el-select 
                  v-model="profileForm.country" 
                  :placeholder="t('validation.required', { field: t('user.country') })"
                  @change="handleCountryChange"
                >
                  <el-option 
                    v-for="option in countryOptions" 
                    :key="option.value" 
                    :label="option.label" 
                    :value="option.value"
                  ></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="手机号码" prop="phone">
                <el-input 
                  v-model="profileForm.phone"
                  :placeholder="currentCountry.placeholder"
                  @input="handlePhoneInput"
                >
                  <template #prepend>
                    <div style="min-width: 60px; text-align: center;">
                      {{ currentCountry.prefix }}
                    </div>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                  <el-radio :label="0">保密</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="生日" prop="birthday">
                <el-date-picker
                  v-model="profileForm.birthday"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              
              <el-form-item label="头像">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                  :on-change="handleAvatarChange"
                >
                  <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="updateProfile" :loading="updating">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 收货地址管理 -->
          <div v-else-if="activeMenu === 'address'" class="address-section">
            <div class="section-header">
              <h2 class="section-title">收货地址</h2>
              <el-button type="primary" @click="showAddAddressDialog">
                + 新增收货地址
              </el-button>
            </div>
            
            <div v-if="addressList.length === 0" class="empty-address">
              <el-empty description="暂无收货地址" />
            </div>
            
            <div v-else class="address-list">
              <div 
                v-for="address in addressList" 
                :key="address.id" 
                class="address-item"
              >
                <div class="address-content">
                  <div class="address-header">
                    <div class="recipient-info">
                      <span class="name">{{ address.name }}</span>
                      <span class="phone">{{ address.phone }}</span>
                    </div>
                    <el-tag v-if="address.isDefault" type="danger" size="small">
                      默认地址
                    </el-tag>
                  </div>
                  
                  <div class="address-detail">
                    {{ formatAddress(address) }}
                  </div>
                </div>
                
                <div class="address-actions">
                  <el-button 
                    v-if="!address.isDefault" 
                    type="primary" 
                    link 
                    @click="setDefaultAddress(address.id)"
                  >
                    设为默认
                  </el-button>
                  <el-button type="primary" link @click="editAddress(address)">
                    编辑
                  </el-button>
                  <el-button type="danger" link @click="deleteAddressConfirm(address)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 账号安全 -->
          <div v-else-if="activeMenu === 'security'" class="security-section">
            <h2 class="section-title">账号安全</h2>
            
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="password-form"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input 
                  v-model="passwordForm.oldPassword" 
                  type="password" 
                  placeholder="请输入当前密码" 
                  show-password
                />
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  placeholder="请输入新密码" 
                  show-password
                />
              </el-form-item>
              
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  placeholder="请再次输入新密码" 
                  show-password
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="updatePassword" :loading="passwordUpdating">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 添加/编辑地址对话框 -->
    <el-dialog 
      v-model="addressDialog.visible" 
      :title="addressDialog.isEdit ? '编辑地址' : '添加新地址'"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getUserInfo, 
  updateUserInfo, 
  changePassword,
  getAddressList, 
  addAddress, 
  updateAddress, 
  deleteAddress, 
  setDefaultAddress as setDefaultAddressApi
} from '@/api/user';
import { regionData } from '@/utils/region-data';
import { useI18n } from 'vue-i18n';
import { countryLocaleMap } from '@/i18n';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const updating = ref(false);
const passwordUpdating = ref(false);
const activeMenu = ref('profile');
const userInfo = ref({});
const addressList = ref([]);
const regionOptions = regionData;

const { t, locale } = useI18n();

// 个人资料表单
const profileFormRef = ref(null);
const profileForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  birthday: '',
  avatar: '',
  country: ''
});

// 国家选项
const countryOptions = [
  { value: 'CN', label: '中国', phonePattern: /^1[3-9]\d{9}$/, prefix: '+86', placeholder: '请输入11位手机号码' },
  { value: 'US', label: '美国', phonePattern: /^\d{10}$/, prefix: '+1', placeholder: '请输入10位手机号码' },
  { value: 'GB', label: '英国', phonePattern: /^\d{10}$/, prefix: '+44', placeholder: '请输入10位手机号码' },
  { value: 'JP', label: '日本', phonePattern: /^\d{10,11}$/, prefix: '+81', placeholder: '请输入10-11位手机号码' },
  { value: 'KR', label: '韩国', phonePattern: /^\d{9,10}$/, prefix: '+82', placeholder: '请输入9-10位手机号码' }
];

// 当前选中的国家信息
const currentCountry = computed(() => {
  return countryOptions.find(c => c.value === profileForm.country) || countryOptions[0];
});

// 处理手机号码输入
const handlePhoneInput = (value) => {
  // 移除所有非数字字符
  profileForm.phone = value.replace(/\D/g, '');
};

// 个人资料表单验证规则
const profileRules = {
  nickname: [
    { max: 20, message: '昵称不能超过20个字符', trigger: 'blur' }
  ],
  country: [
    { required: true, message: '请选择国家', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!profileForm.country) {
          callback(new Error('请先选择国家'));
          return;
        }
        
        const country = countryOptions.find(c => c.value === profileForm.country);
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
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
};

// 密码表单
const passwordFormRef = ref(null);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur' 
    }
  ]
};

// 地址相关
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
    { 
      validator: (rule, value, callback) => {
        if (!profileForm.country) {
          callback(new Error('请先在个人资料中设置国家信息'));
          return;
        }
        
        const country = countryOptions.find(c => c.value === profileForm.country);
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

// 获取用户信息
const fetchUserInfo = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await getUserInfo();
    userInfo.value = res.data || {};
    
    // 填充表单
    Object.assign(profileForm, {
      username: userInfo.value.username || '',
      nickname: userInfo.value.nickname || '',
      phone: userInfo.value.phone || '',
      email: userInfo.value.email || '',
      gender: userInfo.value.gender || 0,
      birthday: userInfo.value.birthday || '',
      avatar: userInfo.value.avatar || '',
      country: userInfo.value.country || ''
    });
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败，请重试');
    
    // 使用模拟数据
    mockUserInfo();
  } finally {
    loading.value = false;
  }
};

// 模拟用户数据
const mockUserInfo = () => {
  userInfo.value = {
    id: 1,
    username: 'testuser',
    nickname: '测试用户',
    phone: '13800138000',
    email: 'test@example.com',
    gender: 1,
    birthday: '1990-01-01',
    avatar: 'https://picsum.photos/id/1005/200/200',
    country: 'CN'
  };
  
  Object.assign(profileForm, userInfo.value);
};

// 获取地址列表
const fetchAddressList = async () => {
  try {
    const res = await getAddressList();
    addressList.value = res.data || [];
  } catch (error) {
    console.error('获取地址列表失败:', error);
    ElMessage.error('获取地址列表失败，请重试');
    
    // 使用模拟数据
    mockAddressList();
  }
};

// 模拟地址数据
const mockAddressList = () => {
  addressList.value = [
    {
      id: 1,
      name: '张三',
      phone: '13800138000',
      province: '浙江省',
      city: '杭州市',
      district: '西湖区',
      detail: '古荡街道1号',
      zipCode: '310000',
      isDefault: true
    },
    {
      id: 2,
      name: '李四',
      phone: '13900139000',
      province: '广东省',
      city: '深圳市',
      district: '南山区',
      detail: '科技园10号',
      zipCode: '518000',
      isDefault: false
    }
  ];
};

// 处理菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index;
  
  if (index === 'address') {
    fetchAddressList();
  }
};

// 更新个人资料
const updateProfile = async () => {
  if (!profileFormRef.value) return;
  
  try {
    await profileFormRef.value.validate();
    
    updating.value = true;
    
    await updateUserInfo(profileForm);
    
    ElMessage.success('个人资料更新成功');
    fetchUserInfo();
  } catch (error) {
    console.error('更新个人资料失败:', error);
    ElMessage.error('更新失败，请重试');
  } finally {
    updating.value = false;
  }
};

// 更新密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return;
  
  try {
    await passwordFormRef.value.validate();
    
    passwordUpdating.value = true;
    
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    
    ElMessage.success('密码修改成功，请重新登录');
    
    // 清空表单
    passwordFormRef.value.resetFields();
    
    // 退出登录
    userStore.logout();
    router.push('/login');
  } catch (error) {
    console.error('修改密码失败:', error);
    ElMessage.error('修改失败，请检查当前密码是否正确');
  } finally {
    passwordUpdating.value = false;
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

// 设置默认地址
const setDefaultAddress = async (id) => {
  try {
    await setDefaultAddressApi(id);
    ElMessage.success('默认地址设置成功');
    fetchAddressList();
  } catch (error) {
    console.error('设置默认地址失败:', error);
    ElMessage.error('设置失败，请重试');
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

// 处理头像上传
const handleAvatarChange = (file) => {
  // 在实际项目中，这里应该上传图片到服务器，然后获取URL
  // 这里简单模拟
  const reader = new FileReader();
  reader.readAsDataURL(file.raw);
  reader.onload = () => {
    profileForm.avatar = reader.result;
  };
};

// 处理国家选择
const handleCountryChange = (value) => {
  const newLocale = countryLocaleMap[value];
  if (newLocale) {
    locale.value = newLocale;
    localStorage.setItem('locale', newLocale);
  }
};

onMounted(() => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  fetchUserInfo();
});
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.user-profile-container {
  padding-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: $spacing-large;
  }
  
  .menu-card {
    margin-bottom: $spacing-base;
    
    .user-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: $spacing-large 0;
      border-bottom: 1px solid $border-lighter;
      
      .user-name {
        margin-top: $spacing-base;
        font-size: $font-size-large;
        font-weight: bold;
      }
    }
    
    .profile-menu {
      border-right: none;
    }
  }
  
  .section-title {
    margin-top: 0;
    margin-bottom: $spacing-large;
    padding-bottom: $spacing-small;
    border-bottom: 1px solid $border-lighter;
    font-size: $font-size-large;
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-large;
    
    .section-title {
      margin: 0;
      border-bottom: none;
    }
  }
  
  .profile-form, .password-form {
    max-width: 500px;
  }
  
  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed $border-base;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
      
      &:hover {
        border-color: $primary-color;
      }
    }
    
    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100px;
      height: 100px;
      text-align: center;
      line-height: 100px;
    }
    
    .avatar {
      width: 100px;
      height: 100px;
      display: block;
    }
  }
  
  .empty-address {
    padding: 50px 0;
    display: flex;
    justify-content: center;
  }
  
  .address-list {
    .address-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: $spacing-base;
      border: 1px solid $border-lighter;
      border-radius: $border-radius-base;
      margin-bottom: $spacing-base;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .address-content {
        flex: 1;
        
        .address-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: $spacing-small;
          
          .recipient-info {
            .name {
              font-weight: bold;
              margin-right: $spacing-small;
            }
            
            .phone {
              color: $text-secondary;
            }
          }
        }
        
        .address-detail {
          color: $text-regular;
        }
      }
      
      .address-actions {
        display: flex;
        gap: $spacing-small;
      }
    }
  }
}

@include respond-to(md) {
  .user-profile-container {
    .el-row {
      display: flex;
      flex-direction: column;
      
      .el-col {
        margin-bottom: $spacing-base;
      }
    }
  }
}

@include respond-to(sm) {
  .user-profile-container {
    .address-list {
      .address-item {
        flex-direction: column;
        align-items: flex-start;
        
        .address-content {
          margin-bottom: $spacing-base;
          width: 100%;
        }
        
        .address-actions {
          width: 100%;
          justify-content: flex-end;
        }
      }
    }
  }
}

:deep(.el-input-group__prepend) {
  padding: 0 12px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  background-color: var(--el-fill-color-light);
}
</style> 