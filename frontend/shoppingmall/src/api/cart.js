import request from '@/utils/request';

/**
 * 获取购物车商品数量
 * @returns {Promise} 请求的Promise对象
 */
export function getCartItemCount() {
  return request({
    url: '/cart/count',
    method: 'get'
  });
}

/**
 * 获取购物车列表
 * @returns {Promise} 请求的Promise对象
 */
export function getCartList() {
  return request({
    url: '/cart/list',
    method: 'get'
  });
}

/**
 * 添加商品到购物车
 * @param {Object} data - 购物车数据
 * @param {number} data.productId - 商品ID
 * @param {number} data.quantity - 数量
 * @returns {Promise} 请求的Promise对象
 */
export function addToCart(data) {
  return request({
    url: '/cart/add',
    method: 'post',
    data
  });
}

/**
 * 更新购物车商品数量
 * @param {Object} data - 更新数据
 * @param {number} data.id - 购物车项ID
 * @param {number} data.quantity - 新数量
 * @returns {Promise} 请求的Promise对象
 */
export function updateCartItem(data) {
  console.log('[api/cart.js] updateCartItem - payload:', data);
  console.log('[api/cart.js] updateCartItem - typeof payload:', typeof data);
  console.log('[api/cart.js] updateCartItem - payload instanceof URLSearchParams:', data instanceof URLSearchParams);
  console.log('[api/cart.js] updateCartItem - payload instanceof FormData:', data instanceof FormData);
  return request({
    url: '/cart/update',
    method: 'put',
    data
  });
}

/**
 * 删除购物车商品
 * @param {number} id - 购物车项ID
 * @returns {Promise} 请求的Promise对象
 */
export function removeCartItem(id) {
  return request({
    url: `/cart/remove/${id}`,
    method: 'delete'
  });
}

/**
 * 清空购物车
 * @returns {Promise} 请求的Promise对象
 */
export function clearCart() {
  return request({
    url: '/cart/clear',
    method: 'delete'
  });
}

/**
 * 选择/取消选择购物车商品
 * @param {number} id - 购物车商品ID
 * @param {Object} data - 更新数据
 * @param {boolean} data.selected - 是否选中
 * @returns {Promise} 请求的Promise对象
 */
export function toggleSelectCartItem(id, data) {
  return request({
    url: `/cart/${id}/select`,
    method: 'put',
    data
  });
}

/**
 * 全选/取消全选购物车商品
 * @param {Object} data - 更新数据
 * @param {boolean} data.selected - 是否全选
 * @returns {Promise} 请求的Promise对象
 */
export function selectAllCartItems(data) {
  return request({
    url: '/cart/select-all',
    method: 'put',
    data
  });
} 