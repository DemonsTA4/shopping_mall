/**
 * 模拟API响应数据
 * 当后端API无法访问时提供模拟数据
 */

// 从localStorage获取已注册用户数据，如果没有则初始化为空数组
const getRegisteredUsers = () => {
  const usersJson = localStorage.getItem('mockUsers');
  return usersJson ? JSON.parse(usersJson) : [];
};

// 保存用户数据到localStorage
const saveRegisteredUsers = (users) => {
  localStorage.setItem('mockUsers', JSON.stringify(users));
};

// 模拟分类数据
const mockCategories = {
  code: 200,
  message: 'success',
  data: [
    { id: 1, name: '手机数码', icon: 'phone-outline' },
    { id: 2, name: '电脑办公', icon: 'laptop-outline' },
    { id: 3, name: '家用电器', icon: 'tv-outline' },
    { id: 4, name: '家居家装', icon: 'home-outline' },
    { id: 5, name: '厨房用品', icon: 'restaurant-outline' },
    { id: 6, name: '服装鞋包', icon: 'shirt-outline' },
    { id: 7, name: '个护清洁', icon: 'water-outline' },
    { id: 8, name: '食品生鲜', icon: 'nutrition-outline' },
    { id: 9, name: '玩具乐器', icon: 'game-controller-outline' },
    { id: 10, name: '图书音像', icon: 'book-outline' }
  ]
};

// 模拟推荐商品数据
const mockRecommendProducts = {
  code: 200,
  message: 'success',
  data: Array.from({ length: 10 }, (_, i) => ({
    id: i + 1,
    name: `模拟推荐商品${i + 1}`,
    description: '这是一个模拟商品描述，用于测试展示',
    price: (Math.random() * 1000 + 100).toFixed(2),
    originalPrice: (Math.random() * 1500 + 200).toFixed(2),
    sales: Math.floor(Math.random() * 1000),
    rating: (Math.random() * 3 + 2).toFixed(1),
    imageUrl: `https://picsum.photos/id/${i + 20}/400/400`
  }))
};

// 模拟轮播图数据
const mockBanners = {
  code: 200,
  message: 'success',
  data: [
    {
      id: 1,
      title: '夏季大促',
      imageUrl: 'https://picsum.photos/id/1/1200/400',
      link: '/products?categoryId=6'
    },
    {
      id: 2,
      title: '新品发布',
      imageUrl: 'https://picsum.photos/id/2/1200/400',
      link: '/products?categoryId=1'
    },
    {
      id: 3,
      title: '限时折扣',
      imageUrl: 'https://picsum.photos/id/3/1200/400',
      link: '/products?categoryId=3'
    },
    {
      id: 4,
      title: '品牌专场',
      imageUrl: 'https://picsum.photos/id/4/1200/400',
      link: '/products?categoryId=2'
    },
    {
      id: 5,
      title: '会员特权',
      imageUrl: 'https://picsum.photos/id/5/1200/400',
      link: '/user/profile'
    }
  ]
};

// 模拟商品列表数据
const mockProducts = {
  code: 200,
  message: 'success',
  data: {
    list: Array.from({ length: 20 }, (_, i) => ({
      id: i + 1,
      name: `模拟商品${i + 1}`,
      description: '这是一个模拟商品描述，用于测试展示',
      price: (Math.random() * 1000 + 100).toFixed(2),
      originalPrice: (Math.random() * 1500 + 200).toFixed(2),
      sales: Math.floor(Math.random() * 1000),
      rating: (Math.random() * 3 + 2).toFixed(1),
      imageUrl: `https://picsum.photos/id/${i + 50}/400/400`,
      categoryId: Math.floor(Math.random() * 10) + 1
    })),
    total: 100,
    page: 1,
    pageSize: 20
  }
};

// 用户注册处理
function handleUserRegister(userData) {
  console.log('模拟注册用户:', userData);
  
  // 获取已注册用户
  const users = getRegisteredUsers();
  
  // 检查用户名是否已存在
  const existingUser = users.find(user => user.username === userData.username);
  if (existingUser) {
    return {
      code: 400,
      message: '用户名已存在'
    };
  }
  
  // 检查邮箱是否已存在
  const existingEmail = users.find(user => user.email === userData.email);
  if (existingEmail) {
    return {
      code: 400,
      message: '邮箱已被注册'
    };
  }
  
  // 生成新用户
  const newUser = {
    id: users.length + 1,
    username: userData.username,
    password: userData.password, // 实际应用中应该加密存储
    email: userData.email,
    phone: userData.phone,
    role: userData.role || 0,
    createTime: new Date().toISOString()
  };
  
  // 添加新用户并保存
  users.push(newUser);
  saveRegisteredUsers(users);
  
  console.log('用户注册成功:', newUser);
  
  // 返回成功响应
  return {
    code: 200,
    message: '注册成功',
    data: {
      id: newUser.id,
      username: newUser.username,
      email: newUser.email,
      role: newUser.role
    }
  };
}

// 用户登录处理
function handleUserLogin(userData) {
  console.log('模拟用户登录:', userData);
  
  // 获取已注册用户
  const users = getRegisteredUsers();
  
  // 查找匹配的用户
  const user = users.find(
    u => u.username === userData.username && u.password === userData.password
  );
  
  if (!user) {
    return {
      code: 401,
      message: '用户名或密码错误'
    };
  }
  
  // 生成模拟token
  const token = `mock-token-${user.id}-${Date.now()}`;
  
  return {
    code: 200,
    message: '登录成功',
    data: {
      token,
      user: {
        id: user.id,
        username: user.username,
        email: user.email,
        role: user.role
      }
    }
  };
}

// 根据请求URL返回对应的模拟数据
export function mockApiResponse(url, method, params) {
  console.log(`模拟API响应: ${method} ${url}`, params);
  
  // 用户注册
  if (url.includes('/user/register') && method.toLowerCase() === 'post') {
    return handleUserRegister(params);
  }
  
  // 用户登录
  if (url.includes('/user/login') && method.toLowerCase() === 'post') {
    return handleUserLogin(params);
  }
  
  if (url.includes('/categories')) {
    return mockCategories;
  }
  
  if (url.includes('/products/recommend')) {
    return mockRecommendProducts;
  }
  
  if (url.includes('/banners')) {
    return mockBanners;
  }
  
  if (url.includes('/products') && !url.includes('/recommend')) {
    // 对于详情页请求，生成单个商品数据
    if (url.match(/\/products\/\d+$/)) {
      const id = parseInt(url.split('/').pop());
      return {
        code: 200,
        message: 'success',
        data: {
          id,
          name: `模拟商品${id}`,
          description: '这是一个详细的模拟商品描述，包含商品的各种详细信息和特点。这个商品质量优良，价格合理，深受广大消费者喜爱。',
          price: (Math.random() * 1000 + 100).toFixed(2),
          originalPrice: (Math.random() * 1500 + 200).toFixed(2),
          sales: Math.floor(Math.random() * 1000),
          rating: (Math.random() * 3 + 2).toFixed(1),
          stock: Math.floor(Math.random() * 500),
          brand: '模拟品牌',
          categoryId: Math.floor(Math.random() * 10) + 1,
          tags: ['热销', '新品', '推荐'],
          attributes: [
            { name: '颜色', value: '黑色/白色/蓝色' },
            { name: '尺寸', value: 'S/M/L/XL' },
            { name: '材质', value: '优质材料' }
          ],
          images: Array.from({ length: 5 }, (_, i) => ({
            id: i + 1,
            url: `https://picsum.photos/id/${id * 10 + i}/800/800`
          }))
        }
      };
    }
    
    return mockProducts;
  }
  
  return null;
} 