<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCartStore } from '@/store/modules/cart';
import { useUserStore } from '@/store/modules/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getProductDetail, getProductComments, addComment, toggleCollect } from '@/api/product';
import { addToCart } from '@/api/cart';
import ProductCard from '@/components/business/ProductCard.vue';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();

const productId = computed(() => route.params.id);
const loading = ref(true);
const product = ref({});
const comments = ref([]);
const recommendProducts = ref([]);
const isCollected = ref(false);
const quantity = ref(1);
const currentTab = ref('detail');
const commentPagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

// 表单数据
const commentForm = reactive({
  content: '',
  rating: 5,
  images: []
});

// 表单验证规则
const commentRules = {
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ]
};

const commentFormRef = ref(null);

// 获取商品详情
const fetchProductDetail = async () => {
  // 1. 获取并校验 productId
  const currentProductIdValue = productIdFromRoute.value; // 或者如果您直接用一个 ref productId，那就是 productId.value

  if (currentProductIdValue === undefined || currentProductIdValue === null || String(currentProductIdValue).toLowerCase() === "undefined") {
    console.error("商品详情页：无效的商品ID（来自路由或ref）:", currentProductIdValue);
    ElMessage.error("无法加载商品详情：商品ID无效。");
    loading.value = false; // 确保在出错时停止加载状态
    // 这里可以考虑跳转到404页面或商品列表页
    // router.push('/products');
    return; // 提前退出，不执行API调用
  }

  const numericProductId = parseInt(currentProductIdValue);

  if (isNaN(numericProductId) || numericProductId <= 0) {
    console.error("商品详情页：商品ID不是一个有效的正整数:", currentProductIdValue, "转换后:", numericProductId);
    ElMessage.error("无法加载商品详情：商品ID格式不正确。");
    loading.value = false;
    // router.push('/products');
    return; // 提前退出
  }

  // 2. 只有当 numericProductId 有效时，才继续
  loading.value = true;
  try {
    const res = await getProductDetail(numericProductId); // 使用校验和转换后的 numericProductId
    
    if (res && res.data) { // 确保 res 和 res.data 存在
        product.value = res.data || {};
        isCollected.value = res.data.isCollected || false; // 假设后端返回 isCollected

        // 获取相关推荐 (假设后端商品详情返回 recommendIds)
        if (res.data.recommendIds && res.data.recommendIds.length > 0) {
            // fetchRecommendProducts(res.data.recommendIds); // 您需要实现这个函数
            console.log("需要获取相关推荐，IDs:", res.data.recommendIds);
        } else {
            // mockRecommendProducts(); // 或者加载通用的推荐
            console.log("没有特定推荐ID，可以加载通用推荐或模拟数据");
        }
    } else {
        console.error('获取商品详情失败: API未返回有效数据结构', res);
        ElMessage.error('获取商品详情数据格式错误，请重试');
        mockProductDetail(); // 使用模拟数据
    }

  } catch (error) {
    console.error('获取商品详情API调用失败:', error);
    ElMessage.error('获取商品详情失败，请重试');
    mockProductDetail(); // 使用模拟数据
  } finally {
    loading.value = false;
  }
};


// 获取商品评论
const fetchComments = async () => {
  try {
    const res = await getProductComments(productId.value, {
      page: commentPagination.page,
      pageSize: commentPagination.pageSize
    });
    
    comments.value = res.data.list || [];
    commentPagination.total = res.data.total || 0;
  } catch (error) {
    console.error('获取商品评论失败:', error);
    
    // 使用模拟数据
    mockComments();
  }
};

// 获取推荐商品
const fetchRecommendProducts = async (ids) => {
  try {
    // 在实际项目中，可能需要根据 ids 批量获取商品
    recommendProducts.value = ids.map(id => ({
      id,
      name: `推荐商品 ${id}`,
      price: Math.floor(Math.random() * 1000 + 100),
      originalPrice: Math.floor(Math.random() * 1500 + 200),
      imageUrl: `https://picsum.photos/id/${id % 100}/400/400`,
      sales: Math.floor(Math.random() * 1000),
      rate: (Math.random() * 2 + 3).toFixed(1)
    }));
  } catch (error) {
    console.error('获取推荐商品失败:', error);
    mockRecommendProducts();
  }
};

// 切换收藏状态
const toggleCollectProduct = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  try {
    const newState = !isCollected.value;
    await toggleCollect(productId.value, newState);
    isCollected.value = newState;
    ElMessage.success(newState ? '收藏成功' : '已取消收藏');
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

// 添加到购物车
const handleAddToCart = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  try {
    // 添加到本地购物车
    cartStore.addToCart({
      ...product.value,
      quantity: quantity.value
    });
    
    // 同步到服务端
    await addToCart({
      productId: productId.value,
      quantity: quantity.value
    });
    
    ElMessage.success('已加入购物车');
  } catch (error) {
    console.error('添加购物车失败:', error);
    ElMessage.error('添加失败，请重试');
  }
};

// 立即购买
const handleBuyNow = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  // 添加到购物车并跳转到结算页
  cartStore.addToCart({
    ...product.value,
    quantity: quantity.value,
    selected: true
  });
  
  router.push('/checkout');
};

// 提交评论
const submitComment = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  if (!commentFormRef.value) return;
  
  try {
    await commentFormRef.value.validate();
    
    await addComment(productId.value, {
      content: commentForm.content,
      rating: commentForm.rating,
      images: commentForm.images
    });
    
    ElMessage.success('评论成功');
    
    // 重置表单
    commentForm.content = '';
    commentForm.rating = 5;
    commentForm.images = [];
    
    // 刷新评论列表
    commentPagination.page = 1;
    fetchComments();
  } catch (error) {
    console.error('提交评论失败:', error);
    ElMessage.error('提交评论失败，请重试');
  }
};

// 分页切换
const handleCommentPageChange = (page) => {
  commentPagination.page = page;
  fetchComments();
};

// 模拟数据：商品详情
const mockProductDetail = () => {
  product.value = {
    id: productId.value,
    name: `测试商品 ${productId.value}`,
    price: 999,
    originalPrice: 1299,
    discount: '7.7',
    stock: 999,
    sales: 888,
    rate: 4.8,
    description: '这是一个很棒的商品，具有出色的性能和精美的外观设计。',
    specs: '规格：默认规格',
    imageUrl: `https://picsum.photos/id/${productId.value}/800/800`,
    images: [
      `https://picsum.photos/id/${Number(productId.value) + 1}/800/800`,
      `https://picsum.photos/id/${Number(productId.value) + 2}/800/800`,
      `https://picsum.photos/id/${Number(productId.value) + 3}/800/800`,
      `https://picsum.photos/id/${Number(productId.value) + 4}/800/800`
    ],
    detail: `
      <div style="text-align:center;">
        <h2>商品详情</h2>
        <p>这是一个高质量的商品，拥有众多出色特性：</p>
        <ul style="text-align:left;">
          <li>高性能</li>
          <li>时尚外观</li>
          <li>耐用材质</li>
          <li>易于使用</li>
          <li>物超所值</li>
        </ul>
        <img src="https://picsum.photos/id/${Number(productId.value) + 10}/800/400" alt="商品详情图1" />
        <img src="https://picsum.photos/id/${Number(productId.value) + 11}/800/400" alt="商品详情图2" />
        <img src="https://picsum.photos/id/${Number(productId.value) + 12}/800/400" alt="商品详情图3" />
      </div>
    `
  };
};

onMounted(() => {
  fetchProductDetail();
  fetchComments();
});
</script>

<template>
  <div class="product-detail-container">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-product">
          <div class="skeleton-left">
            <el-skeleton-item variant="image" style="width: 100%; height: 400px;" />
          </div>
          <div class="skeleton-right">
            <el-skeleton-item variant="h3" style="width: 50%; height: 24px;" />
            <el-skeleton-item variant="text" style="margin-top: 16px; width: 80%;" />
            <el-skeleton-item variant="text" style="margin-top: 16px; width: 30%;" />
            <el-skeleton-item variant="text" style="margin-top: 16px; width: 50%;" />
            <el-skeleton-item variant="text" style="margin-top: 16px; width: 40%;" />
            <div style="margin-top: 16px; display: flex; justify-content: space-between;">
              <el-skeleton-item variant="button" style="width: 30%; height: 40px;" />
              <el-skeleton-item variant="button" style="width: 30%; height: 40px;" />
            </div>
          </div>
        </div>
      </template>
      
      <div v-if="!loading" class="product-detail">
        <!-- 商品基本信息 -->
        <div class="product-info-section">
          <!-- 商品图片 -->
          <div class="product-gallery">
            <el-carousel indicator-position="outside" height="400px">
              <el-carousel-item v-for="(img, index) in product.images || [product.imageUrl]" :key="index">
                <img :src="img" :alt="`${product.name} - 图片${index + 1}`" />
              </el-carousel-item>
            </el-carousel>
          </div>
          
          <!-- 商品信息 -->
          <div class="product-info">
            <h1 class="product-name">{{ product.name }}</h1>
            
            <div class="product-price-info">
              <div class="price-box">
                <span class="current-price">¥{{ product.price }}</span>
                <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
                <span v-if="product.discount" class="discount-badge">{{ product.discount }}折</span>
              </div>
              <div class="product-stats">
                <span class="stat-item">销量: {{ product.sales || 0 }}</span>
                <span class="stat-item">评分: {{ product.rate || 0 }}</span>
              </div>
            </div>
            
            <div class="product-meta">
              <div class="meta-item">
                <span class="meta-label">库存:</span>
                <span class="meta-value">{{ product.stock || '暂无库存' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">规格:</span>
                <span class="meta-value">{{ product.specs || '默认规格' }}</span>
              </div>
            </div>
            
            <div class="product-description">
              <p>{{ product.description }}</p>
            </div>
            
            <!-- 数量选择 -->
            <div class="quantity-selector">
              <span class="quantity-label">数量:</span>
              <el-input-number 
                v-model="quantity" 
                :min="1" 
                :max="product.stock || 99"
                size="large"
              />
            </div>
            
            <!-- 操作按钮 -->
            <div class="product-actions">
              <el-button 
                type="primary" 
                size="large" 
                @click="handleAddToCart"
              >
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              
              <el-button 
                type="danger" 
                size="large" 
                @click="handleBuyNow"
              >
                <el-icon><ShoppingBag /></el-icon>
                立即购买
              </el-button>
              
              <el-button 
                :type="isCollected ? 'danger' : 'default'" 
                size="large" 
                circle
                @click="toggleCollectProduct"
              >
                <el-icon><Star /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 商品详情选项卡 -->
        <div class="product-tabs">
          <el-tabs v-model="currentTab" type="border-card">
            <el-tab-pane label="商品详情" name="detail">
              <div class="product-detail-content" v-html="product.detail"></div>
            </el-tab-pane>
            
            <el-tab-pane :label="`商品评价(${commentPagination.total})`" name="comments">
              <!-- 评论表单 -->
              <div class="comment-form">
                <h3>发表评论</h3>
                <el-form 
                  ref="commentFormRef"
                  :model="commentForm"
                  :rules="commentRules"
                  label-position="top"
                >
                  <el-form-item label="评分" prop="rating">
                    <el-rate 
                      v-model="commentForm.rating" 
                      :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                      :max="5"
                      show-text
                      :texts="['很差', '较差', '一般', '较好', '很好']"
                    />
                  </el-form-item>
                  
                  <el-form-item label="评论内容" prop="content">
                    <el-input 
                      v-model="commentForm.content" 
                      type="textarea" 
                      :rows="3"
                      placeholder="请输入您对商品的评价..."
                    />
                  </el-form-item>
                  
                  <el-form-item label="上传图片">
                    <el-upload
                      action="#"
                      list-type="picture-card"
                      :auto-upload="false"
                      :limit="5"
                      :on-change="(file) => commentForm.images.push(file.url)"
                      :on-remove="(file) => commentForm.images = commentForm.images.filter(url => url !== file.url)"
                      accept=".jpg,.jpeg,.png,.gif"
                    >
                      <el-icon><Plus /></el-icon>
                    </el-upload>
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" @click="submitComment">提交评论</el-button>
                  </el-form-item>
                </el-form>
              </div>
              
              <!-- 评论列表 -->
              <div class="comment-list">
                <el-empty v-if="comments.length === 0" description="暂无评论" />
                
                <div v-else class="comment-items">
                  <div 
                    v-for="comment in comments" 
                    :key="comment.id" 
                    class="comment-item"
                  >
                    <div class="comment-user">
                      <el-avatar :src="comment.avatar" :size="40">{{ comment.username.substring(0, 1) }}</el-avatar>
                      <div class="user-info">
                        <div class="username">{{ comment.username }}</div>
                        <div class="comment-time">{{ comment.createdAt }}</div>
                      </div>
                    </div>
                    
                    <div class="comment-content">
                      <div class="rating">
                        <el-rate 
                          v-model="comment.rating" 
                          disabled 
                          text-color="#ff9900"
                        />
                      </div>
                      <div class="text">{{ comment.content }}</div>
                      
                      <div v-if="comment.images && comment.images.length > 0" class="comment-images">
                        <el-image 
                          v-for="(img, index) in comment.images" 
                          :key="index"
                          :src="img"
                          :preview-src-list="comment.images"
                          fit="cover"
                          :initial-index="index"
                          class="comment-image"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- 评论分页 -->
                <div class="comment-pagination">
                  <el-pagination
                    v-model:current-page="commentPagination.page"
                    :page-size="commentPagination.pageSize"
                    layout="prev, pager, next"
                    :total="commentPagination.total"
                    @current-change="handleCommentPageChange"
                    background
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <!-- 相关推荐 -->
        <div class="related-products" v-if="recommendProducts.length > 0">
          <h2 class="section-title">相关推荐</h2>
          <div class="product-list">
            <div 
              v-for="item in recommendProducts" 
              :key="item.id" 
              class="product-item"
            >
              <ProductCard :product="item" />
            </div>
          </div>
        </div>
      </div>
    </el-skeleton>
  </div>
</template>

<style lang="scss" scoped>
.product-detail-container {
  min-height: 500px;
}

.skeleton-product {
  display: flex;
  gap: $spacing-large;
  
  .skeleton-left {
    flex: 1;
  }
  
  .skeleton-right {
    flex: 1;
  }
}

.product-detail {
  min-height: 500px;
}

.product-info-section {
  display: flex;
  gap: $spacing-large;
  margin-bottom: $spacing-large;
  
  .product-gallery {
    width: 50%;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }
  
  .product-info {
    width: 50%;
    
    .product-name {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: $spacing-base;
      color: $text-primary;
    }
    
    .product-price-info {
      background-color: #FEF0F0;
      padding: $spacing-base;
      border-radius: $border-radius-base;
      margin-bottom: $spacing-base;
      
      .price-box {
        display: flex;
        align-items: center;
        margin-bottom: $spacing-small;
        
        .current-price {
          font-size: 28px;
          font-weight: bold;
          color: $danger-color;
          margin-right: $spacing-small;
        }
        
        .original-price {
          font-size: $font-size-base;
          color: $text-secondary;
          text-decoration: line-through;
          margin-right: $spacing-small;
        }
        
        .discount-badge {
          padding: 2px 8px;
          background-color: $danger-color;
          color: #fff;
          font-size: $font-size-small;
          border-radius: $border-radius-small;
        }
      }
      
      .product-stats {
        display: flex;
        gap: $spacing-large;
        
        .stat-item {
          color: $text-secondary;
        }
      }
    }
    
    .product-meta {
      margin-bottom: $spacing-base;
      
      .meta-item {
        display: flex;
        margin-bottom: $spacing-small;
        
        .meta-label {
          width: 80px;
          color: $text-secondary;
        }
        
        .meta-value {
          color: $text-regular;
        }
      }
    }
    
    .product-description {
      margin-bottom: $spacing-large;
      color: $text-regular;
      font-size: $font-size-base;
      line-height: 1.6;
    }
    
    .quantity-selector {
      display: flex;
      align-items: center;
      margin-bottom: $spacing-large;
      
      .quantity-label {
        margin-right: $spacing-base;
      }
    }
    
    .product-actions {
      display: flex;
      gap: $spacing-base;
    }
  }
}

.product-tabs {
  margin-bottom: $spacing-large;
  
  .product-detail-content {
    padding: $spacing-base;
    
    img {
      max-width: 100%;
      display: block;
      margin: $spacing-base auto;
    }
  }
}

.comment-form {
  background-color: #f9f9f9;
  padding: $spacing-base;
  border-radius: $border-radius-base;
  margin-bottom: $spacing-large;
  
  h3 {
    margin-bottom: $spacing-base;
    font-size: $font-size-large;
  }
}

.comment-list {
  .comment-items {
    margin-bottom: $spacing-large;
    
    .comment-item {
      padding: $spacing-base 0;
      border-bottom: 1px solid $border-lighter;
      
      .comment-user {
        display: flex;
        align-items: center;
        margin-bottom: $spacing-small;
        
        .user-info {
          margin-left: $spacing-small;
          
          .username {
            font-weight: bold;
            color: $text-primary;
          }
          
          .comment-time {
            font-size: $font-size-small;
            color: $text-secondary;
          }
        }
      }
      
      .comment-content {
        padding-left: 48px;
        
        .rating {
          margin-bottom: $spacing-small;
        }
        
        .text {
          margin-bottom: $spacing-small;
          line-height: 1.6;
        }
        
        .comment-images {
          display: flex;
          flex-wrap: wrap;
          gap: $spacing-small;
          
          .comment-image {
            width: 80px;
            height: 80px;
            border-radius: $border-radius-small;
            cursor: pointer;
          }
        }
      }
    }
  }
  
  .comment-pagination {
    display: flex;
    justify-content: center;
    margin-top: $spacing-large;
  }
}

.related-products {
  margin-bottom: $spacing-large;
  
  .section-title {
    font-size: $font-size-large;
    margin-bottom: $spacing-base;
    padding-bottom: $spacing-small;
    border-bottom: 1px solid $border-lighter;
  }
  
  .product-list {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: $spacing-base;
    
    .product-item {
      margin-bottom: $spacing-base;
    }
  }
}

@include respond-to(md) {
  .product-info-section {
    flex-direction: column;
    
    .product-gallery,
    .product-info {
      width: 100%;
    }
  }
  
  .related-products .product-list {
    grid-template-columns: repeat(4, 1fr);
  }
}

@include respond-to(sm) {
  .product-info .product-actions {
    flex-wrap: wrap;
    
    .el-button {
      flex: 1;
    }
  }
  
  .related-products .product-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@include respond-to(xs) {
  .related-products .product-list {
    grid-template-columns: repeat(1, 1fr);
  }
}
</style> 