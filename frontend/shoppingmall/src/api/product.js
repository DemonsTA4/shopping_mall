import request from '@/utils/request';
/** 
* 获取商品列表 
* * @param {Object} params - 查询参数 
* * @param {number} params.page - 页码 
* * @param {number} params.pageSize - 每页数量 
* * @param {number} params.categoryId - 分类ID 
* * @param {string} params.keyword - 搜索关键词 
* * @param {string} params.sort - 排序方式 (price, sales, rating) 
* * @param {string} params.order - 排序顺序 (asc, desc) 
* * @returns {Promise} 请求的Promise对象 
* */
export function getProducts(params) {
	return request({
		url: '/products',    
		method: 'get',    
		params  
		});
}

/** 
* 获取商品详情 
* * @param {number} id - 商品ID 
* * @returns {Promise} 请求的Promise对象 
* */
export function getProductDetail(id) {
	return request({
		url: `/products/${id}`,    
		method: 'get'  ,
		});
}

/** 
* 获取商品评论 
* * @param {number} id - 商品ID 
* * @param {Object} params - 查询参数 
* * @param {number} params.page - 页码 
* * @param {number} params.pageSize - 每页数量 
* * @returns {Promise} 请求的Promise对象 
* */
export function getProductComments(id, params) {
	return request({    
		url: `/products/${id}/comments`,    
		method: 'get',    
		params  
		});
}

/** 
* 添加商品评论 
* * @param {number} id - 商品ID 
* * @param {Object} data - 评论数据 
* * @param {string} data.content - 评论内容 
* * @param {number} data.rating - 评分 
* * @param {Array} data.images - 图片列表 
* * @returns {Promise} 请求的Promise对象 
* */
export function addComment(id, data) {
	return request({    
		url: `/products/${id}/comments`,    
		method: 'post',    
		data  
		});
}

/**
 * 切换商品收藏状态 (已修正)
 * @param {number} productId - 商品ID
 * @param {boolean} collectAction - true 表示要执行收藏操作, false 表示要执行取消收藏操作
 * @returns {Promise} 请求的Promise对象
 */
export function toggleCollect(productId, collectAction) {
  if (collectAction) {
    // 希望收藏商品 (newState is true)
    // 调用后端 POST /api/favorites/{productId}
    return request({
      url: `/favorites/${productId}`, // 修正了 URL 路径
      method: 'post'                // 修正了 HTTP 方法
      // 通常 POST 请求用于创建资源或执行动作，不需要在 body 中再传 isCollect
    });
  } else {
    // 希望取消收藏商品 (newState is false)
    // 调用后端 DELETE /api/favorites/{productId}
    return request({
      url: `/favorites/${productId}`, // 修正了 URL 路径
      method: 'delete'              // 修正了 HTTP 方法
    });
  }
}

/**
 * 检查商品是否已被当前用户收藏
 * @param {number} productId - 商品ID
 * @returns {Promise} 返回一个包含布尔值的 Promise (在 Result.data 中)
 */
export function getFavoriteStatus(productId) {
  return request({
    url: `/favorites/${productId}/status`, // 假设您的 request 工具处理了 /api 前缀
    method: 'get'
  });
}

/** 
 ** 获取商品分类 
 ** @returns {Promise} 请求的Promise对象 
 **/
export function getCategories() {
	return request({
		url: '/categories',    
		method: 'get'  ,
	});
}

/** 
* 创建商品 
* * @param {Object} data - 商品数据 
* * @returns {Promise} 请求的Promise对象 
* */export function createProduct(data) {
	return request({
		url: '/products',    
		method: 'post',    
		data  
	});
}

/** 
* 更新商品 
* * @param {number} id - 商品ID 
* * @param {Object} data - 商品数据 
* * @returns {Promise} 请求的Promise对象 
* */
export function updateProduct(id, data) {
	return request({
		url: `/products/${id}`,    
		method: 'put',    
		data  
	});
}

/** 
* 更新商品状态 
* * @param {number} id - 商品ID 
* * @param {Object} data - 状态数据 
* * @param {number} data.status - 商品状态(0:下架, 1:上架) 
* * @returns {Promise} 请求的Promise对象 
* */
export function updateProductStatus(id, data) {  
	return request({    
		url: `/products/${id}/status`,    
		method: 'put',    
		data  
	});
}

/** 
* 删除商品 
* * @param {number} id - 商品ID 
* * @returns {Promise} 请求的Promise对象 
* */
export function deleteProduct(id) {
	return request({
		url: `/products/${id}`,    
		method: 'delete'  ,
	});
}

/** 
* 上传商品图片 
* * @param {FormData} data - 包含图片文件的FormData对象 
* * @returns {Promise} 请求的Promise对象 
* */
export function uploadProductImage(data) {
	return request({    
		url: '/upload/product',    
		method: 'post',    
		headers: {
			'Content-Type': 'multipart/form-data'    ,
		},    
		data  
	});
}







/**
 * 获取首页推荐商品
 * @returns {Promise} 请求的Promise对象
 */
export function getRecommendProducts(count = 8) {
  return request({
    url: '/products',
    method: 'get',
	params: { // 通过 params 对象传递查询参数
	      type: 'featured', // 我们约定的参数，用于标识获取特色商品
	      pageSize: count   // 控制返回的商品数量
	      // 如果特色商品也需要特定的排序，可以在这里添加 sort 和 order 参数
	      // sort: 'your_featured_sort_field', // 例如：按销量、更新时间等
	      // order: 'desc'
	    }
  });
}

/**
 * 获取首页轮播图
 * @returns {Promise} 请求的Promise对象
 */
export function getBanners() {
  return request({
    url: '/banners',
    method: 'get'
  });
}



/**
 * 获取用户收藏的商品列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise} 请求的Promise对象
 */
export function getCollectedProducts(params) {
  return request({
    url: '/user/collected',
    method: 'get',
    params
  });
} 