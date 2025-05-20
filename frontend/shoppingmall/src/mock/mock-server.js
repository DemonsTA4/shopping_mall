import Mock from 'mockjs'

// 开启控制台打印
Mock.XHR.prototype.withCredentials = true;

// 配置mock的响应格式
Mock.setup({
  timeout: '200-500'
});

const productsData = Mock.mock({
  'list|20': [{
    'id|+1': 1,
    'name': '@ctitle(5, 10)',
    'description': '@cparagraph(1, 3)',
    'price|100-2000.2': 1, 
    'originalPrice|1-3': '@price',
    'sales|100-1000': 1,
    'rating|3.5-5.0': 1,
    'imageUrl': 'https://picsum.photos/400/400?random=@integer(1, 100)',
    'categoryId|1-10': 1
  }]
})

const recommendData = Mock.mock({
  'list|8': [{
    'id|+1': 1,
    'name': '@ctitle(5, 10)',
    'description': '@cparagraph(1, 3)',
    'price|100-2000.2': 1,
    'originalPrice|1-3': '@price', 
    'sales|100-1000': 1,
    'rate|3.5-5.0': 1,
    'imageUrl': 'https://picsum.photos/400/400?random=@integer(1, 100)'
  }]
})

const bannersData = [
  { id: 1, title: '618大促', imageUrl: 'https://picsum.photos/1200/400?random=1' },
  { id: 2, title: '新品发布', imageUrl: 'https://picsum.photos/1200/400?random=2' },
  { id: 3, title: '限时优惠', imageUrl: 'https://picsum.photos/1200/400?random=3' },
  { id: 4, title: '品牌专场', imageUrl: 'https://picsum.photos/1200/400?random=4' }
]

const categoriesData = [
  { id: 1, name: '手机数码', icon: 'Iphone' },
  { id: 2, name: '电脑办公', icon: 'Monitor' },
  { id: 3, name: '家用电器', icon: 'HomeFilled' },
  { id: 4, name: '服装鞋包', icon: 'ShoppingBag' },
  { id: 5, name: '美妆护肤', icon: 'Star' },
  { id: 6, name: '母婴玩具', icon: 'School' },
  { id: 7, name: '运动户外', icon: 'Basketball' },
  { id: 8, name: '家居百货', icon: 'House' }
]

// 设置Mock拦截请求
export function setupMockServer() {
  console.log('Mock服务已启动');

  // 处理401错误的API v1路径
  Mock.mock('/api/v1/', 'get', () => {
    return {
      code: 200,
      message: 'success',
      data: { apiVersion: 'v1' }
    }
  });

  // 获取推荐商品
  Mock.mock('/api/products/recommend', 'get', () => {
    console.log('Mock: 返回推荐商品数据');
    return {
      code: 200,
      message: 'success',
      data: recommendData.list
    }
  });

  // 获取轮播图
  Mock.mock('/api/banners', 'get', () => {
    console.log('Mock: 返回轮播图数据');
    return {
      code: 200,
      message: 'success',
      data: bannersData
    }
  });

  // 获取分类
  Mock.mock('/api/categories', 'get', () => {
    console.log('Mock: 返回分类数据');
    return {
      code: 200,
      message: 'success',
      data: categoriesData
    }
  });

  // 获取商品列表 - 使用字符串匹配
  Mock.mock(/^\/api\/products(\?.*)?$/, 'get', (options) => {
    console.log('Mock: 返回商品列表数据', options);
    return {
      code: 200,
      message: 'success',
      data: {
        list: productsData.list,
        total: 100,
        page: 1,
        pageSize: 20
      }
    }
  });

  // 获取商品详情
  Mock.mock(/^\/api\/products\/\d+$/, 'get', (options) => {
    const id = parseInt(options.url.match(/\/products\/(\d+)/)[1]);
    console.log('Mock: 返回商品详情数据', id);
    return {
      code: 200,
      message: 'success',
      data: {
        id,
        name: `商品${id}`,
        description: '这是一个详细的商品描述，包含商品的各种详细信息和特点。',
        price: (Math.random() * 1000 + 100).toFixed(2),
        originalPrice: (Math.random() * 1500 + 200).toFixed(2),
        sales: Math.floor(Math.random() * 1000),
        rating: (Math.random() * 3 + 2).toFixed(1),
        stock: Math.floor(Math.random() * 500),
        brand: '示例品牌',
        categoryId: Math.floor(Math.random() * 10) + 1,
        imageUrl: `https://picsum.photos/400/400?random=${id}`,
        images: [
          `https://picsum.photos/400/400?random=${id}`,
          `https://picsum.photos/400/400?random=${id+1}`,
          `https://picsum.photos/400/400?random=${id+2}`
        ]
      }
    }
  });
} 