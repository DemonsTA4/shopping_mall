import request from '@/utils/request';

/**
 * 提交订单
 * @param {Object} data - 订单数据
 * @param {number} data.addressId - 收货地址ID
 * @param {string} data.paymentMethod - 支付方式
 * @param {string} data.remark - 订单备注
 * @returns {Promise} 请求的Promise对象
 */
export function submitOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  });
}

/**
 * 获取订单列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} params.status - 订单状态(0:全部, 1:待付款, 2:待发货, 3:待收货, 4:待评价)
 * @returns {Promise} 请求的Promise对象
 */
export function getOrders(params) {
  return request({
    url: '/orders',
    method: 'get',
    params
  });
}

/**
 * 获取订单详情
 * @param {number} id - 订单ID
 * @returns {Promise} 请求的Promise对象
 */
export function getOrderDetail(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  });
}

/**
 * 取消订单
 * @param {number} id - 订单ID
 * @param {Object} data - 取消原因
 * @param {string} data.reason - 取消原因
 * @returns {Promise} 请求的Promise对象
 */
export function cancelOrder(id, data) {
  return request({
    url: `/orders/${id}/cancel`,
    method: 'put',
    data
  });
}

/**
 * 确认收货
 * @param {number} id - 订单ID
 * @returns {Promise} 请求的Promise对象
 */
export function confirmReceipt(id) {
  return request({
    url: `/orders/${id}/confirm`,
    method: 'put'
  });
}

/**
 * 删除订单
 * @param {number} id - 订单ID
 * @returns {Promise} 请求的Promise对象
 */
export function deleteOrder(id) {
  return request({
    url: `/orders/${id}`,
    method: 'delete'
  });
}

/**
 * 申请退款
 * @param {number} id - 订单ID
 * @param {Object} data - 退款数据
 * @param {string} data.reason - 退款原因
 * @param {string} data.description - 问题描述
 * @returns {Promise} 请求的Promise对象
 */
export function applyRefund(id, data) {
  return request({
    url: `/orders/${id}/refund`,
    method: 'post',
    data
  });
}

/** * 支付订单 * @param {number} id - 订单ID * @param {Object} data - 支付数据 * @param {string} data.paymentMethod - 支付方式 * @returns {Promise} 请求的Promise对象 */export function payOrder(id, data) {  return request({    url: `/orders/${id}/pay`,    method: 'post',    data  });} /** * 获取订单物流信息 * @param {number} id - 订单ID * @returns {Promise} 请求的Promise对象 */export function getOrderLogistics(id) {  return request({    url: `/orders/${id}/logistics`,    method: 'get'  });} 