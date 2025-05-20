import { defineStore } from 'pinia';
import { login as apiLogin, getUserInfo as apiGetUserInfo } from '../../api/user'; // 重命名导入的 login，避免与 action 名冲突
// 假设 ElMessage 是全局可用的或已导入
// import { ElMessage } from 'element-plus';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: {}, // 初始为空对象，将由 initializeAuth 或 login action 填充
    isLogin: false // 初始为false，将由 initializeAuth 或 login action 更新
  }),
  actions: {
    /**
     * 应用初始化时或需要恢复登录状态时调用。
     * 如果存在token，则尝试从后端获取用户信息。
     */
    // 在 useUserStore 的 actions 中
    async initializeAuth() {
      const token = localStorage.getItem('token'); // 或者 sessionStorage
      if (token) {
        this.token = token;
        this.isLogin = true; // 先乐观设置
        try {
          console.log('[UserStore initializeAuth] 检测到token，尝试从API获取用户信息...');
          // 优先通过API获取最新、最完整的用户信息
          const res = await apiGetUserInfo(); // 假设 apiGetUserInfo 是您导入的API函数
          if (res.code === 200 && res.data) {
            this.userInfo = res.data; // ★ API返回的 res.data 中应包含正确的 role (数字)
            // this.isLogin = true; // 再次确认，虽然前面已设置
            if (res.data.role != null) {
                localStorage.setItem('userRole', res.data.role.toString()); // 同步localStorage中的角色
            }
            console.log('[UserStore initializeAuth] 成功从API恢复用户信息:', JSON.parse(JSON.stringify(this.userInfo)));
          } else {
            // API 获取用户信息失败，可能是 token 过期或无效
            console.warn('[UserStore initializeAuth] 从API获取用户信息失败 (API响应非200或无数据):', res.message);
            this.logout(); // 清除无效的登录状态
          }
        } catch (error) {
          // API 调用本身出错
          console.error('[UserStore initializeAuth] 调用apiGetUserInfo出错:', error);
          this.logout(); // 出错时也清除登录状态
        }
      } else {
        // 没有token，确保是未登录状态
        this.logout(); // 调用logout来清理可能存在的旧状态
        console.log('[UserStore initializeAuth] 未检测到token，用户未登录。');
      }
    },

    async login(userData) {
      try {
        const res = await apiLogin(userData); // 调用API模块的login函数
        if (res.code === 200 && res.data && res.data.token && res.data.user) {
          this.token = res.data.token;
          this.userInfo = res.data.user; // ★ 后端返回的user对象应包含正确的role (数字0或1)
          this.isLogin = true;
          localStorage.setItem('token', res.data.token);
          if (res.data.user.role != null) {
            localStorage.setItem('userRole', res.data.user.role.toString());
          } else {
            localStorage.removeItem('userRole');
          }
          console.log('[UserStore Login Action] 登录成功，更新store和localStorage:', JSON.parse(JSON.stringify(this.userInfo)));
          return Promise.resolve(res.data);
        } else {
          const errorMessage = res.message || '登录失败或响应数据格式不正确';
          if (typeof ElMessage !== 'undefined') ElMessage({ message: errorMessage, type: 'error' });
          else console.error(errorMessage);
          return Promise.reject(new Error(errorMessage));
        }
      } catch (error) {
        console.error("Pinia login action error:", error);
        return Promise.reject(error);
      }
    },

    logout() {
      this.token = '';
      this.userInfo = {};
      this.isLogin = false;
      localStorage.removeItem('token');
      localStorage.removeItem('userRole');
      console.log('[UserStore Logout Action] 用户已登出，清空store和localStorage。');
    }
  }
});