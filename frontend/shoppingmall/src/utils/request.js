import axios from 'axios'
import { ElMessage } from 'element-plus'
// 注释掉模拟数据导入
// import { mockApiResponse } from './mock'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  withCredentials: true
})

// 调试信息
console.log('API基础URL:', service.defaults.baseURL);

// request.js - 请求拦截器
service.interceptors.request.use(
  config => {
    config.metadata = { startTime: new Date() };
    const token = localStorage.getItem('token');

    // ★★★ 重要日志：打印从 localStorage 获取的 token ★★★
    console.log('[Request Interceptor] Token from localStorage AT REQUEST TIME:', token);

    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      // ★★★ 重要日志：确认 Authorization header 被设置 ★★★
      console.log('[Request Interceptor] Authorization header SET with token.');
    } else {
      // ★★★ 重要日志：如果没找到 token ★★★
      console.warn('[Request Interceptor] No token found in localStorage. Authorization header NOT set.');
    }

    console.log(`发送请求: ${config.method.toUpperCase()} ${config.url}`, config.params || config.data || {});
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// request.js
// ... (请求拦截器和其他辅助函数保持不变) ...

// 响应拦截器
service.interceptors.response.use(
  response => {
    // ... (成功响应处理部分保持不变) ...
    const res = response.data;
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '业务处理失败', // 优先使用 res.message
        type: 'error',
        duration: 5 * 1000
      });
      if (res.code === 401) {
        handleUnauthorized();
      }
      // 为业务失败也创建一个带有业务信息的Error对象并reject
      const businessError = new Error(res.message || '业务处理失败');
      businessError.code = res.code; // 可以将业务code也附加上
      businessError.data = res.data; // 可以将完整data附加上
      return Promise.reject(businessError);
    } else {
      return res;
    }
  },
  error => {
    console.error('响应错误:', error);
    const errorDetails = getErrorDetails(error);
    console.error('错误详情:', errorDetails);

    let customErrorMessage = '请求发生未知错误，请稍后再试';
    let errorCode = null; // 用于存储业务错误码
    let errorData = null; // 用于存储业务数据

    if (error.response) {
      // 服务器返回了响应，但状态码是错误状态 (4xx, 5xx)
      // 尝试从 error.response.data 中获取后端定义的业务错误信息
      if (error.response.data && error.response.data.message) {
        customErrorMessage = error.response.data.message; // ★★★ 使用后端返回的业务消息 ★★★
        errorCode = error.response.data.code;         // ★★★ 保存后端业务码 ★★★
        errorData = error.response.data.data;         // ★★★ 保存后端业务数据 ★★★
      } else {
        // 如果响应体中没有 message，则根据 HTTP 状态码给一个通用提示
        switch (error.response.status) {
          case 400: customErrorMessage = '请求参数错误(400)'; break;
          case 401: handleUnauthorized(); customErrorMessage = '未授权，请重新登录(401)'; break; // handleUnauthorized会跳转
          case 403: customErrorMessage = '禁止访问(403)'; break;
          case 404: customErrorMessage = '请求的资源未找到(404)'; break;
          case 500: customErrorMessage = '服务器内部错误(500)'; break;
          default: customErrorMessage = `请求错误 (${error.response.status})`;
        }
      }
      // 统一弹出提示
      if (error.response.status !== 401) { // 401时 handleUnauthorized 可能会跳转，避免重复提示
          ElMessage({ message: customErrorMessage, type: 'error', duration: 5 * 1000 });
      }

    } else if (error.request) {
      // 请求已发出，但没有收到响应 (网络问题)
      customErrorMessage = '网络连接失败，请检查您的网络或稍后再试';
      if (error.message.includes('timeout')) {
        customErrorMessage = '请求超时，请稍后再试';
      }
      ElMessage({ message: customErrorMessage, type: 'error', duration: 5 * 1000 });
    } else {
      // 其他错误 (例如请求配置错误)
      customErrorMessage = `请求发起失败: ${error.message}`;
      ElMessage({ message: customErrorMessage, type: 'error', duration: 5 * 1000 });
    }

    // 创建一个新的Error对象，其message是更具体的错误信息
    const enhancedError = new Error(customErrorMessage);
    // 将原始axios错误对象的属性（如response, config, code(业务码)）附加到新错误对象上，供上层使用
    if (error.response) {
      enhancedError.response = error.response;
    }
    if (errorCode) { // 如果从后端响应中获取了业务错误码
        enhancedError.code = errorCode;
    }
    if (errorData) {
        enhancedError.data = errorData;
    }
    // 如果需要原始的axios错误代码 (如 'ERR_BAD_REQUEST')，也可以附加
    // enhancedError.axiosErrorCode = error.code;


    return Promise.reject(enhancedError); // ★★★ reject 带有具体业务消息的 Error 对象 ★★★
  }
);

// 计算请求耗时
function calculateRequestDuration(response) {
  const endTime = new Date();
  const startTime = response.config.metadata.startTime;
  return endTime - startTime;
}

// 获取详细错误信息
function getErrorDetails(error) {
  return {
    message: error.message,
    code: error.code,
    stack: error.stack,
    response: error.response ? {
      status: error.response.status,
      statusText: error.response.statusText,
      headers: error.response.headers,
      data: error.response.data
    } : null,
    request: error.request ? {
      url: error.config?.url,
      method: error.config?.method,
      baseURL: error.config?.baseURL,
      timeout: error.config?.timeout,
      headers: error.config?.headers
    } : null
  };
}

// 处理未授权错误
function handleUnauthorized() {
  localStorage.removeItem('token');
  localStorage.removeItem('userRole');
  window.location.href = '/login';
}

// 处理响应错误
function handleResponseError(response) {
  // 状态码处理
  switch (response.status) {
    case 400:
      ElMessage({message: '请求错误(400)', type: 'error'});
      break;
    case 401:
      handleUnauthorized();
      break;
    case 403:
      ElMessage({message: '拒绝访问(403)', type: 'error'});
      break;
    case 404:
      ElMessage({message: '请求出错(404)', type: 'error'});
      break;
    case 408:
      ElMessage({message: '请求超时(408)', type: 'error'});
      break;
    case 500:
      ElMessage({message: '服务器错误(500)', type: 'error'});
      break;
    case 501:
      ElMessage({message: '服务未实现(501)', type: 'error'});
      break;
    case 502:
      ElMessage({message: '网络错误(502)', type: 'error'});
      break;
    case 503:
      ElMessage({message: '服务不可用(503)', type: 'error'});
      break;
    case 504:
      ElMessage({message: '网络超时(504)', type: 'error'});
      break;
    default:
      ElMessage({
        message: response.data?.message || `连接出错(${response.status})!`,
        type: 'error'
      });
  }
}

// 处理网络错误
function handleNetworkError(error) {
  let message = '网络连接失败，请检查您的网络';
  if (error.message.includes('timeout')) {
    message = '请求超时，请稍后再试';
  }
  ElMessage({
    message,
    type: 'error',
    duration: 5 * 1000
  });
}

// 处理请求错误
function handleRequestError(error) {
  ElMessage({
    message: `请求配置错误: ${error.message}`,
    type: 'error',
    duration: 5 * 1000
  });
}

export default service;