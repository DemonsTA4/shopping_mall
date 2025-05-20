<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
   price: [
      { required: true, message: '请输入销售价格', trigger: 'blur' },
      { type: 'number', message: '价格必须是数字', trigger: 'blur' }, // 确保是数字
      { validator: (rule, value, callback) => { // 自定义校验器模拟 @DecimalMin
          if (value === null || value === undefined || value === '') { // 如果允许为空（但required=true使其不为空）
              callback(); // 如果允许为空，则不校验最小值；但这里 required=true
          } else if (typeof value === 'number' && value > 0) { // @DecimalMin("0.01")
              callback();
          } else {
              callback(new Error('商品价格必须大于0'));
          }
          }, trigger: 'blur' }
    ],
    stock: [
      { required: true, message: '请输入库存数量', trigger: 'blur' },
      { type: 'number', message: '库存必须是数字', trigger: 'blur' }, // 确保是数字
      { validator: (rule, value, callback) => { // 自定义校验器模拟 @Min(0)
          if (value === null || value === undefined || value === '') {
              callback();
          } else if (typeof value === 'number' && value >= 0) {
              callback();
          } else {
              callback(new Error('商品库存不能小于0'));
          }
          }, trigger: 'blur' }
    ],
};

const formRef = ref(null);

// 登录方法
const handleLogin = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    
    try {
      await userStore.login(loginForm);
      ElMessage.success('登录成功');
      router.push('/');
    } catch (error) {
      console.error('登录失败:', error);
      ElMessage.error(error.message || '登录失败，请检查用户名和密码');
    }
  } catch (error) {
    console.error('表单验证失败:', error);
  } finally {
    loading.value = false;
  }
};

// 去注册页
const goToRegister = () => {
  router.push('/register');
};
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>用户登录</h2>
      </div>
      
      <el-form 
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <div class="form-options">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          <el-link type="primary" href="javascript:void(0)">忘记密码？</el-link>
        </div>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-link">
        <span>还没有账号？</span>
        <el-link type="primary" @click="goToRegister">立即注册</el-link>
      </div>
      
      <div class="other-login">
        <div class="divider">
          <span>其他方式登录</span>
        </div>
        <div class="social-login">
          <el-button circle>
            <el-icon><Position /></el-icon>
          </el-button>
          <el-button circle>
            <el-icon><Service /></el-icon>
          </el-button>
          <el-button circle>
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 180px);
  background-color: #f5f5f5;
  padding: 20px;
}

.login-box {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    font-size: 24px;
    color: #333;
    margin: 0;
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  
  span {
    color: #666;
  }
}

.other-login {
  margin-top: 30px;
  
  .divider {
    display: flex;
    align-items: center;
    color: #999;
    font-size: 14px;
    margin-bottom: 20px;
    
    &::before,
    &::after {
      content: '';
      flex: 1;
      height: 1px;
      background-color: #eee;
    }
    
    span {
      padding: 0 15px;
    }
  }
  
  .social-login {
    display: flex;
    justify-content: center;
    gap: 20px;
  }
}

@media (max-width: 480px) {
  .login-box {
    width: 100%;
    padding: 20px;
  }
}
</style> 