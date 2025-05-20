import request from '@/utils/request';

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 请求的Promise对象
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  });
}

/**
 * 用户注册
 * @param {Object} data - 注册数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @param {string} data.country - 国家代码
 * @param {number} data.role - 用户角色(0:买家, 1:卖家)
 * @returns {Promise} 请求的Promise对象
 */
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  });
}

/**
 * 获取用户信息
 * @returns {Promise} 请求的Promise对象
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  });
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 * @param {string} data.nickname - 昵称
 * @param {string} data.phone - 手机号
 * @param {string} data.email - 邮箱
 * @param {string} data.country - 国家代码
 * @param {number} data.gender - 性别(0:保密 1:男 2:女)
 * @param {string} data.birthday - 生日
 * @param {string} data.avatar - 头像URL
 * @returns {Promise} 请求的Promise对象
 */
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  });
}

/**
 * 修改密码
 * @param {Object} data - 密码数据
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise} 请求的Promise对象
 */
export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  });
}

/**
 * 获取用户地址列表
 * @returns {Promise} 请求的Promise对象
 */
export function getAddressList() {
  return request({
    url: '/user/address',
    method: 'get'
  });
}

/**
 * 添加收货地址
 * @param {Object} data - 地址数据
 * @returns {Promise} 请求的Promise对象
 */
export function addAddress(data) {
  return request({
    url: '/user/address',
    method: 'post',
    data
  });
}

/**
 * 更新收货地址
 * @param {number} id - 地址ID
 * @param {Object} data - 地址数据
 * @returns {Promise} 请求的Promise对象
 */
export function updateAddress(id, data) {
  return request({
    url: `/user/address/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除收货地址
 * @param {number} id - 地址ID
 * @returns {Promise} 请求的Promise对象
 */
export function deleteAddress(id) {
  return request({
    url: `/user/address/${id}`,
    method: 'delete'
  });
}

/** * 设置默认收货地址 * @param {number} id - 地址ID * @returns {Promise} 请求的Promise对象 */export function setDefaultAddress(id) {  return request({    url: `/user/address/${id}/default`,    method: 'put'  });} /** * 获取收藏列表 * @param {Object} params - 查询参数 * @returns {Promise} 请求的Promise对象 */export function getFavorites(params) {  return request({    url: '/user/favorites',    method: 'get',    params  });}/** * 添加收藏 * @param {number} productId - 商品ID * @returns {Promise} 请求的Promise对象 */export function addFavorite(productId) {  return request({    url: '/user/favorites',    method: 'post',    data: { productId }  });}/** * 取消收藏 * @param {number} id - 收藏ID * @returns {Promise} 请求的Promise对象 */export function removeFavorite(id) {  return request({    url: `/user/favorites/${id}`,    method: 'delete'  });} 