import request from '@/utils/request';

/**
 * 创建售后申请
 * @param {Object} data - 售后申请数据
 * @param {number} data.orderId - 订单ID
 * @param {number} data.type - 售后类型(1:退款, 2:退货退款, 3:换货)
 * @param {string} data.reason - 申请原因
 * @param {string} data.description - 问题描述
 * @param {Array} data.images - 图片列表
 * @returns {Promise} 请求的Promise对象
 */
export function createAftersale(data) {
  return request({
    url: '/aftersales',
    method: 'post',
    data
  });
}

/**
 * 获取售后申请列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} params.status - 状态(0:全部, 1:待处理, 2:处理中, 3:已完成, 4:已拒绝)
 * @returns {Promise} 请求的Promise对象
 */
export function getAftersaleList(params) {
  return request({
    url: '/aftersales',
    method: 'get',
    params
  });
}

/**
 * 获取售后申请详情
 * @param {number} id - 售后申请ID
 * @returns {Promise} 请求的Promise对象
 */
export function getAftersaleDetail(id) {
  return request({
    url: `/aftersales/${id}`,
    method: 'get'
  });
}

/**
 * 取消售后申请
 * @param {number} id - 售后申请ID
 * @returns {Promise} 请求的Promise对象
 */
export function cancelAftersale(id) {
  return request({
    url: `/aftersales/${id}/cancel`,
    method: 'put'
  });
}

/**
 * 提交物流信息(退货)
 * @param {number} id - 售后申请ID
 * @param {Object} data - 物流信息
 * @param {string} data.company - 物流公司
 * @param {string} data.trackingNo - 物流单号
 * @returns {Promise} 请求的Promise对象
 */
export function submitShippingInfo(id, data) {
  return request({
    url: `/aftersales/${id}/shipping`,
    method: 'put',
    data
  });
}

/**
 * 确认收货(换货)
 * @param {number} id - 售后申请ID
 * @returns {Promise} 请求的Promise对象
 */
export function confirmReceived(id) {
  return request({
    url: `/aftersales/${id}/confirm`,
    method: 'put'
  });
}

/**
 * 获取订单可申请售后的商品
 * @param {number} orderId - 订单ID
 * @returns {Promise} 请求的Promise对象
 */
export function getOrderItemsForAftersale(orderId) {
  return request({
    url: `/orders/${orderId}/aftersale-items`,
    method: 'get'
  });
}

/**
 * 提交售后申请(与createAftersale功能相同，用于兼容性)
 * @param {Object} data - 售后申请数据
 * @param {number} data.orderId - 订单ID
 * @param {number} data.type - 售后类型(1:退款, 2:退货退款, 3:换货)
 * @param {string} data.reason - 申请原因
 * @param {string} data.description - 问题描述
 * @param {Array} data.images - 图片列表
 * @returns {Promise} 请求的Promise对象
 */
export function submitAftersaleApplication(data) {
  return createAftersale(data);
}

/**
 * 确认售后完成
 * @param {number} id - 售后申请ID
 * @returns {Promise} 请求的Promise对象
 */
export function confirmAftersaleComplete(id) {
  return request({
    url: `/aftersales/${id}/complete`,
    method: 'put'
  });
}

/**
 * 提交退货物流信息(兼容性函数)
 * @param {number} id - 售后申请ID
 * @param {Object} data - 物流信息
 * @param {string} data.company - 物流公司
 * @param {string} data.trackingNo - 物流单号
 * @returns {Promise} 请求的Promise对象
 */
export function submitReturnLogistics(id, data) {
  return submitShippingInfo(id, data);
} 