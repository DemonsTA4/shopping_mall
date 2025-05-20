<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '@/api/user';
import { ElMessage } from 'element-plus';
import { User, Lock, Iphone, Message } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import { countryLocaleMap } from '@/i18n';

const router = useRouter();
const loading = ref(false);
const { t, locale } = useI18n();

// 表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  role: 0, // 默认为买家
  country: '', // 添加国家字段
  agreement: false
});

// 国家选项
const countryOptions = computed(() => [
  { value: 'CN', label: t('countries.china'), phonePattern: /^1[3-9]\d{9}$/, prefix: '+86', placeholder: t('validation.phonePlaceholder.CN') },
  { value: 'US', label: t('countries.usa'), phonePattern: /^\d{10}$/, prefix: '+1', placeholder: t('validation.phonePlaceholder.US') },
  { value: 'GB', label: t('countries.uk'), phonePattern: /^\d{10}$/, prefix: '+44', placeholder: t('validation.phonePlaceholder.GB') },
  { value: 'JP', label: t('countries.japan'), phonePattern: /^\d{10,11}$/, prefix: '+81', placeholder: t('validation.phonePlaceholder.JP') },
  { value: 'KR', label: t('countries.korea'), phonePattern: /^\d{9,10}$/, prefix: '+82', placeholder: t('validation.phonePlaceholder.KR') }
]);

// 当前选中的国家信息
const currentCountry = computed(() => {
  return countryOptions.value.find(c => c.value === registerForm.country) || countryOptions.value[0];
});

// 处理手机号码输入
const handlePhoneInput = (value) => {
  // 移除所有非数字字符
  registerForm.phone = value.replace(/\D/g, '');
};

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error(t('validation.required', { field: t('user.password') })));
  } else {
    if (registerForm.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error(t('validation.required', { field: t('user.confirmPassword') })));
  } else if (value !== registerForm.password) {
    callback(new Error(t('validation.passwordMismatch')));
  } else {
    callback();
  }
};

const validatePhone = (rule, value, callback) => {
  if (!registerForm.country) {
    callback(new Error(t('validation.required', { field: t('user.country') })));
    return;
  }
  
  const country = countryOptions.value.find(c => c.value === registerForm.country);
  if (!country) {
    callback(new Error(t('validation.invalidCountry')));
    return;
  }

  if (value && !country.phonePattern.test(value)) {
    callback(new Error(t('validation.invalidPhone', { country: country.label })));
  } else {
    callback();
  }
};

const validateEmail = (rule, value, callback) => {
  const pattern = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
  if (value && !pattern.test(value)) {
    callback(new Error('请输入正确的邮箱格式'));
  } else {
    callback();
  }
};

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error(t('validation.agreementRequired')));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: t('validation.required', { field: t('user.username') }), trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  country: [
    { required: true, message: t('validation.required', { field: t('user.country') }), trigger: 'change' }
  ],
  phone: [
    { required: true, message: t('validation.required', { field: t('user.phone') }), trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('validation.required', { field: t('user.email') }), trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  role: [
    { required: true, message: t('validation.required', { field: t('user.role') }), trigger: 'change' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
};

const formRef = ref(null);

// 处理国家选择
const handleCountryChange = (value) => {
  const newLocale = countryLocaleMap[value];
  if (newLocale) {
    locale.value = newLocale;
    localStorage.setItem('locale', newLocale);
  }
};

// 注册方法
const handleRegister = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    
    try {
      // 从表单中提取需要的数据
      const { username, password, phone, email, role, country } = registerForm;
      const response = await register({
        username,
        password,
        phone,
        email,
        role,
        country
      });
      
      if (response.code === 200) {
        ElMessage.success('注册成功，请登录');
        router.push('/login');
      }
    } catch (error) {
      console.error('注册失败:', error);
      ElMessage.error(error.message || '注册失败，请稍后重试');
    }
  } catch (error) {
    console.error('表单验证失败:', error);
  } finally {
    loading.value = false;
  }
};

// 去登录页
const goToLogin = () => {
  router.push('/login');
};

// 角色选项
const roleOptions = [
  { label: t('user.buyer'), value: 0 },
  { label: t('user.seller'), value: 1 }
];
</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>{{ t('common.register') }}</h2>
      </div>
      
      <el-form 
        ref="formRef"
        :model="registerForm"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item :label="t('user.username')" prop="username">
          <el-input 
            v-model="registerForm.username"
            :placeholder="t('validation.required', { field: t('user.username') })"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item :label="t('user.password')" prop="password">
          <el-input 
            v-model="registerForm.password"
            type="password"
            :placeholder="t('validation.required', { field: t('user.password') })"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item :label="t('user.confirmPassword')" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword"
            type="password"
            :placeholder="t('validation.required', { field: t('user.confirmPassword') })"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item :label="t('user.country')" prop="country">
          <el-select 
            v-model="registerForm.country" 
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
        
        <el-form-item :label="t('user.phone')" prop="phone">
          <el-input 
            v-model="registerForm.phone"
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
        
        <el-form-item :label="t('user.email')" prop="email">
          <el-input 
            v-model="registerForm.email"
            :placeholder="t('validation.required', { field: t('user.email') })"
            :prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item :label="t('user.role')" prop="role">
          <el-radio-group v-model="registerForm.role">
            <el-radio 
              v-for="option in roleOptions" 
              :key="option.value" 
              :value="option.value"
            >
              {{ option.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            {{ t('user.agreement') }} <el-link type="primary" href="javascript:void(0)">{{ t('user.userAgreement') }}</el-link>
          </el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            class="register-button"
            native-type="submit"
          >
            {{ t('user.registerNow') }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-link">
        <span>{{ t('user.hasAccount') }}</span>
        <el-link type="primary" @click="goToLogin">{{ t('user.loginNow') }}</el-link>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 180px);
  background-color: #f5f5f5;
  padding: 20px;
}

.register-box {
  width: 450px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    font-size: 24px;
    color: #333;
    margin: 0;
  }
}

.register-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  
  span {
    color: #666;
  }
}

:deep(.el-form-item) {
  margin-bottom: 22px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

:deep(.el-radio-group) {
  display: flex;
  gap: 30px;
}

:deep(.el-checkbox) {
  height: auto;
  line-height: 1.5;
  
  .el-checkbox__label {
    white-space: normal;
  }
}

:deep(.el-input-group__prepend) {
  padding: 0 12px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  background-color: var(--el-fill-color-light);
}

@media (max-width: 480px) {
  .register-box {
    width: 100%;
    padding: 20px;
  }
  
  :deep(.el-radio-group) {
    flex-direction: column;
    gap: 15px;
  }
}
</style> 