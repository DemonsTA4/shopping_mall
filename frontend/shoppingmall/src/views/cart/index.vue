<template>
  <div class="cart-container">
    <div class="cart-header">
      <h1 class="page-title">我的购物车</h1>
      <el-button 
        v-if="cartItems.length > 0" 
        type="danger" 
        plain 
        size="small"
        @click="clearCartItems"
      >
        清空购物车
      </el-button>
    </div>
    
    <el-skeleton :loading="isLoading" animated :count="3">
      <template #template>
        <div class="skeleton-cart-item">
          <div style="display: flex; align-items: center;">
            <el-skeleton-item variant="circle" style="width: 20px; height: 20px; margin-right: 20px;" />
            <el-skeleton-item variant="image" style="width: 80px; height: 80px; margin-right: 20px;" />
            <el-skeleton-item variant="text" style="width: 200px;" />
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 20px;">
            <el-skeleton-item variant="text" style="width: 80px;" />
            <el-skeleton-item variant="text" style="width: 100px;" />
            <el-skeleton-item variant="text" style="width: 40px;" />
          </div>
        </div>
      </template>
      
      <div v-if="!isLoading">
        <div v-if="cartItems.length === 0" class="empty-cart">
          <el-empty description="购物车空空如也" />
          <el-button type="primary" @click="continueShopping">去逛逛</el-button>
        </div>
        
        <div v-else class="cart-content">
          <!-- 购物车表格 -->
          <div class="cart-table">
            <!-- 表头 -->
            <div class="cart-table-header">
              <div class="col col-check">
                <el-checkbox 
                  v-model="allSelected"
                  @change="toggleSelectAll"
                />
              </div>
              <div class="col col-product">商品信息</div>
              <div class="col col-price">单价</div>
              <div class="col col-quantity">数量</div>
              <div class="col col-total">小计</div>
              <div class="col col-action">操作</div>
            </div>
            
            <!-- 购物车列表 -->
            <div class="cart-table-body">
              <div 
                v-for="item in cartItems" 
                :key="item.id" 
                class="cart-item"
              >
                <div class="col col-check">
                  <el-checkbox 
                    v-model="item.selected"
                    @change="() => toggleSelect(item)"
                  />
                </div>
                
                <div class="col col-product">
                  <div class="product-info">
                    <el-image 
                      :src="item.productImage" 
                      :alt="item.productName"
                      class="product-image" 
                      @click="router.push(`/product/${item.id}`)"
                    />
                    <div class="product-name" @click="router.push(`/product/${item.id}`)">
                      {{ item.name }}
                    </div>
                  </div>
                </div>
                
                <div class="col col-price">
                  <div class="price">¥{{ item.price }}</div>
                </div>
                
                <div class="col col-quantity">
                  <el-input-number 
                    v-model="item.quantity" 
                    :min="1" 
                    :max="item.stock || 99"
                    size="small"
                    controls-position="right"
                    @change="(value) => updateQuantity(item, value)"
                  />
                </div>
                
                <div class="col col-total">
                  <div class="total-price">
                    ¥{{ (item.price * item.quantity).toFixed(2) }}
                  </div>
                </div>
                
                <div class="col col-action">
                  <el-button 
                    type="danger" 
                    size="small" 
                    text
                    @click="removeItem(item)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 购物车底部 -->
          <div class="cart-footer">
            <div class="footer-left">
              <el-checkbox 
                v-model="allSelected"
                @change="toggleSelectAll"
              >
                全选
              </el-checkbox>
              <el-button 
                type="danger" 
                plain 
                size="small"
                :disabled="!hasSelected"
                @click="clearCartItems"
              >
                清空购物车
              </el-button>
            </div>
            
            <div class="footer-right">
              <div class="checkout-info">
                <div class="selected-count">
                  已选择 <span class="count">{{ selectedCount }}</span> 件商品
                </div>
                <div class="total-amount">
                  合计：<span class="amount">¥{{ selectedTotal }}</span>
                </div>
              </div>
              
              <el-button 
                type="primary" 
                size="large"
                :disabled="!hasSelected"
                @click="goToCheckout"
              >
                去结算
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/store/modules/cart';
import { useUserStore } from '@/store/modules/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getCartList, updateCartItem, removeCartItem, clearCart, toggleSelectCartItem, selectAllCartItems } from '@/api/cart';
import { storeToRefs } from 'pinia';
import { Loading } from '@element-plus/icons-vue';

const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();
const { cartItems, isLoading } = storeToRefs(cartStore);
const allSelected = computed(() =>
  cartItems.value.length > 0 && cartItems.value.every(item => item.selected)
);
const loading = ref(false);

// 计算属性：已选择商品总数
const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).length;
});

// 计算属性：已选择商品总价
const selectedTotal = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
    .toFixed(2);
});

// 计算属性：是否有选中的商品
const hasSelected = computed(() => {
  return selectedCount.value > 0;
});

// 获取购物车列表
const fetchCartList = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loading.value = true;
  
  try {
	  //loading.value=true;
      await cartStore.fetchCart(); // 调用 store action
      // 数据会自动通过 storeToRefs 更新到组件的 cartItems
      // allSelected 的更新逻辑也应该依赖于 store 的 cartItems
      // 例如，将 allSelected 定义为 computed 属性
    } catch (error) {
      console.error('组件中调用 cartStore.fetchCart 失败:', error);
      ElMessage.error(error.message || '获取购物车列表失败，请重试');
    } finally {
      loading.value = false; // 如果 store action 控制 loading，这里也不需要
    }
};

const updateQuantity = async (item, newQuantity) => { // 参数名改为 newQuantity 以示区分
  const originalQuantity = item.quantity; // 保存原始数量以便回滚

  // 1. 立即在UI上反映更改 (可选的乐观更新，但要小心)
  // 如果store的action也会做乐观更新，这里可以不做，或者只更新本地组件的临时状态
  // item.quantity = newQuantity; // 直接修改item对象，如果它是本地组件的ref/reactive数据

  try {
    // 2. 调用 Pinia store action，让 store 负责与后端同步和状态管理
    // store action 内部会调用 updateCartItem API
    // 假设 cartStore.updateQuantity 会在API失败时抛出错误
    await cartStore.updateQuantity(item.id, newQuantity);

    // 如果 store action 成功并没有抛错，说明后端也更新成功了
    ElMessage.success('数量更新成功');
    
    // store action 成功后，通常会更新 store 内的 cartItems。
    // 如果这个组件的 cartItems 是直接从 store 获取并响应的，则UI会自动更新。
    // 如果不是，或者 store action 没有返回最新列表，则可能需要重新获取：
    // await fetchCartList(); // 如果 store action 没有处理好状态同步，则调用它

  } catch (error) {
    console.error('更新购物车商品数量失败 (来自组件):', error);
    
    // 3. API 调用失败，显示后端返回的业务错误信息
    ElMessage.error(error.message || '更新失败，请重试'); 
    
    // 4. 回滚UI上的乐观更新（如果做了的话）
    // item.quantity = originalQuantity; // 恢复原始数量
    
    // 5. 或者，更可靠的方式是让 store action 在失败时负责回滚，
    //    并确保组件总是从 store 获取最新状态，或者在这里调用 fetchCartList()
    //    以从服务器获取最准确的状态。
    await fetchCartList(); // 强制从服务器同步，覆盖掉错误的乐观更新
  }
};

// 删除购物车商品 (组件内)
const removeItem = async (item) => {
  try {
    await ElMessageBox.confirm(
      '确定要从购物车中删除该商品吗？',
      '提示',
      { /* ... */ }
    );

    // 核心：只调用 store action
    await cartStore.removeFromCart(item.id); // 假设这个 action 处理 API 和状态

    // store action 成功后，cartItems 应已更新。
    // 一般不需要手动 fetchCartList()。
    ElMessage.success('删除成功');
  } catch (error) {
    if (error !== 'cancel' && String(error).indexOf('cancel') === -1) {
      console.error('删除购物车商品失败 (组件):', error);
      ElMessage.error(error.message || '删除失败，请重试');
    }
  }
};

const clearCartItems = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空购物车吗？',
      '提示',
      { /* ... */ }
    );

    // 核心：只调用 store action。store action 会负责本地状态和后端API调用。
    await cartStore.clearCart(); // 这个 action 应该返回 Promise

    // cartStore.clearCart() 成功后，store 内的 cartItems 应该已经是空的了。
    // 组件的 cartItems (来自 storeToRefs) 会自动更新。
    // 一般不需要再手动调用 fetchCartList()，除非清空操作在后端有复杂副作用需要重新拉取。
    // if (/* 特殊情况需要刷新 */) {
    //   await fetchCartList();
    // }

    ElMessage.success('购物车已清空');
  } catch (error) {
    if (error !== 'cancel' && String(error).indexOf('cancel') === -1) {
      console.error('清空购物车失败 (组件):', error);
      ElMessage.error(error.message || '操作失败，请重试');
    }
  }
};

// 选择/取消选择单个商品
const toggleSelect = async (item) => {
  console.log('[CheckoutPage/CartPage] toggleSelect called for item ID:', item.id, 'Current selected state:', item.selected);
  try {
    // 直接调用 store action。store action 会负责更新自身状态和调用API。
    // item.selected 已经是当前的状态，所以 store action 内部会处理取反逻辑。
    // 或者，如果 store action 期望接收目标状态，可以传递 !item.selected
    // 根据我上一条建议的 store action `async toggleSelect(itemId)`，它内部会取反。
    await cartStore.toggleSelect(item.id);

    // 理论上，如果 store action 正确更新了 cartItems，
    // 并且组件中的 cartItems 和 allSelected 是响应式地依赖于 store 状态的，
    // UI 会自动更新，不需要在这里手动调用 fetchCartList()。
    // 但如果 store action 的设计是在API成功后调用其内部的 fetchCart()，那就更好了。

    // 更新本地 allSelected 状态 (如果它不是直接从 store getter 计算的)
    // 如果 allSelected 是 computed(() => cartStore.cartItems.every(i => i.selected)) 这种，就不需要手动更新
    allSelected.value = cartStore.cartItems.length > 0 && cartStore.cartItems.every(cartItem => cartItem.selected);
    // ElMessage.success('选择状态已更新'); // 可以考虑在 store action 成功时由 store 触发全局提示，或由组件提示

  } catch (error) {
    console.error('更新商品选择状态失败 (from component):', error);
    ElMessage.error(error.message || '操作失败，请重试');
    // 如果 store action 在 catch 中没有很好地回滚状态，或者为了保险，可以重新获取列表
    // await fetchCartList(); 
  }
};

// 全选/取消全选
const toggleSelectAll = async () => {
  const newState = !allSelected.value; // 计算出期望的新全选状态
  console.log('[CheckoutPage/CartPage] toggleSelectAll called. Attempting to set all to:', newState);
  try {
    // 调用 store action，传递期望的新状态
    await cartStore.toggleSelectAllStore(newState); // 假设 store 中的 action 名是 toggleSelectAllStore

    // 同上，UI应自动响应 store 状态的变化。
    // allSelected.value 理论上也应该因为 cartStore.cartItems 的变化而自动通过其 computed 属性更新。
    // 如果 allSelected 是本地 ref，并且不是 computed，则需要手动更新：
    // allSelected.value = newState; // 但这只是本地UI，最好是 store action 更新完 cartItems 后，allSelected能正确重新计算。

    // ElMessage.success(newState ? '已全选' : '已取消全选');

  } catch (error) {
    console.error('更新全选状态失败 (from component):', error);
    ElMessage.error(error.message || '操作失败，请重试');
    // 可能需要重新 fetchCartList 来同步状态
    // await fetchCartList();
  }
};

// 跳转到结算页面
const goToCheckout = () => {
  if (!hasSelected.value) {
    ElMessage.warning('请选择要结算的商品');
    return;
  }
  
  router.push('/checkout');
};

// 返回购物
const continueShopping = () => {
  router.push('/products');
};

onMounted(() => {
  fetchCartList();
});
</script>

<style lang="scss" scoped>
/* 重新导入 */
@import '@/assets/styles/variables.scss';
.cart-container {
  min-height: 500px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-large;
  
  .page-title {
    margin-bottom: 0;
  }
}

.skeleton-cart-item {
  display: flex;
  justify-content: space-between;
  padding: $spacing-base;
  margin-bottom: $spacing-base;
  background-color: #fff;
  border-radius: $border-radius-base;
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100px 0;
  background-color: #fff;
  border-radius: $border-radius-base;
  
  .el-button {
    margin-top: $spacing-large;
  }
}

.cart-content {
  background-color: #fff;
  border-radius: $border-radius-base;
  overflow: hidden;
  
  .cart-table {
    width: 100%;
    
    .cart-table-header {
      display: flex;
      align-items: center;
      padding: $spacing-base;
      background-color: #f5f7fa;
      border-bottom: 1px solid $border-light;
      font-weight: bold;
    }
    
    .cart-table-body {
      .cart-item {
        display: flex;
        align-items: center;
        padding: $spacing-base;
        border-bottom: 1px solid $border-lighter;
        
        &:last-child {
          border-bottom: none;
        }
      }
    }
    
    .col {
      padding: 0 $spacing-small;
    }
    
    .col-check {
      width: 50px;
      text-align: center;
    }
    
    .col-product {
      flex: 1;
      
      .product-info {
        display: flex;
        align-items: center;
        
        .product-image {
          width: 80px;
          height: 80px;
          margin-right: $spacing-base;
          cursor: pointer;
        }
        
        .product-name {
          color: $text-primary;
          cursor: pointer;
          
          &:hover {
            color: $primary-color;
          }
        }
      }
    }
    
    .col-price {
      width: 100px;
      text-align: center;
      
      .price {
        color: $text-primary;
      }
    }
    
    .col-quantity {
      width: 150px;
      text-align: center;
    }
    
    .col-total {
      width: 120px;
      text-align: center;
      
      .total-price {
        color: $danger-color;
        font-weight: bold;
      }
    }
    
    .col-action {
      width: 80px;
      text-align: center;
    }
  }
  
  .cart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-base $spacing-large;
    background-color: #f5f7fa;
    border-top: 1px solid $border-light;
    
    .footer-left {
      display: flex;
      align-items: center;
      gap: $spacing-base;
    }
    
    .footer-right {
      display: flex;
      align-items: center;
      
      .checkout-info {
        margin-right: $spacing-large;
        text-align: right;
        
        .selected-count {
          margin-bottom: $spacing-small;
          
          .count {
            color: $danger-color;
            font-weight: bold;
          }
        }
        
        .total-amount {
          .amount {
            color: $danger-color;
            font-size: $font-size-large;
            font-weight: bold;
          }
        }
      }
    }
  }
}

@include respond-to(md) {
  .cart-content {
    .cart-table {
      .col-price {
        width: 80px;
      }
      
      .col-quantity {
        width: 120px;
      }
      
      .col-total {
        width: 100px;
      }
    }
  }
}

@include respond-to(sm) {
  .cart-content {
    .cart-table {
      .cart-table-header {
        display: none;
      }
      
      .cart-item {
        flex-wrap: wrap;
        padding: $spacing-base $spacing-small;
        
        .col-check {
          width: 40px;
        }
        
        .col-product {
          flex: 1;
          width: calc(100% - 40px);
          margin-bottom: $spacing-base;
        }
        
        .col-price, .col-quantity, .col-total, .col-action {
          width: 25%;
          text-align: center;
        }
      }
    }
    
    .cart-footer {
      flex-direction: column;
      gap: $spacing-base;
      
      .footer-left, .footer-right {
        width: 100%;
      }
      
      .footer-right {
        justify-content: space-between;
      }
    }
  }
}

@include respond-to(xs) {
  .cart-content {
    .cart-table {
      .cart-item {
        .col-price, .col-quantity, .col-total, .col-action {
          width: 50%;
          padding: $spacing-small 0;
        }
      }
    }
    
    .cart-footer {
      .footer-right {
        flex-direction: column;
        gap: $spacing-base;
        
        .checkout-info {
          margin-right: 0;
          width: 100%;
          text-align: center;
        }
        
        .el-button {
          width: 100%;
        }
      }
    }
  }
}
</style>