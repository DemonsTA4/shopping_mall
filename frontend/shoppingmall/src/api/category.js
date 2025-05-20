import request from '@/utils/request';

/**
 * 获取分类列表
 * @returns {Promise} 请求的Promise对象
 */
export function getCategories() {
  return request({
    url: '/categories',
    method: 'get'
  });
}

/**
 * 创建分类
 * @param {Object} data - 分类数据
 * @param {string} data.name - 分类名称
 * @param {string} data.description - 分类描述
 * @param {number} data.sort - 排序值
 * @returns {Promise} 请求的Promise对象
 */
export function createCategory(data) {
  return request({
    url: '/categories',
    method: 'post',
    data
  });
}

/**
 * 更新分类
 * @param {number} id - 分类ID
 * @param {Object} data - 分类数据
 * @param {string} data.name - 分类名称
 * @param {string} data.description - 分类描述
 * @param {number} data.sort - 排序值
 * @returns {Promise} 请求的Promise对象
 */
export function updateCategory(id, data) {
  return request({
    url: `/categories/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除分类
 * @param {number} id - 分类ID
 * @returns {Promise} 请求的Promise对象
 */
export function deleteCategory(id) {
  return request({
    url: `/categories/${id}`,
    method: 'delete'
  });
}

/**
 * 获取分类详情
 * @param {number} id - 分类ID
 * @returns {Promise} 请求的Promise对象
 */
export function getCategoryDetail(id) {
  return request({
    url: `/categories/${id}`,
    method: 'get'
  });
}

/**
 * 更新分类状态
 * @param {number} id - 分类ID
 * @param {Object} data - 状态数据
 * @param {number} data.status - 状态(0:禁用, 1:启用)
 * @returns {Promise} 请求的Promise对象
 */
export function updateCategoryStatus(id, data) {
  return request({
    url: `/categories/${id}/status`,
    method: 'put',
    data
  });
}

/**
 * 上传分类图标
 * @param {FormData} data - 包含图片文件的FormData对象
 * @returns {Promise} 请求的Promise对象
 */
export function uploadCategoryIcon(data) {
  return request({
    url: '/upload/category',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  });
} 