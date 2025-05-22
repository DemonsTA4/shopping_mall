// src/store/modules/cart.js
import { defineStore } from 'pinia';
import { useUserStore } from './user';
import { ElMessage } from 'element-plus'; // ElMessage 通常在组件中使用，store中可以只log或抛出错误

// 导入时使用 API 文件中实际导出的名称
import {
    addToCart,   // 从 @/api/cart.js 导入 addToCart
    getCartList,
	updateCartItem,
	removeCartItem,
	clearCart as clearCartApiService,
	toggleSelectCartItem, 
	selectAllCartItems,// 从 @/api/cart.js 导入 getCartList
} from '@/api/cart';

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartItems: [],
    isLoading: false,
	hasLoadedOnce: false,
  }),
  getters: {
    // 假设您有这些getters，基于您上上次提供的cart.js store
    totalItems: (state) => state.cartItems.reduce((sum, item) => sum + item.quantity, 0),
    totalPrice: (state) => state.cartItems.reduce((sum, item) => sum + (parseFloat(item.price) || 0) * (parseInt(item.quantity) || 0), 0),
    cartItemCount: (state) => state.cartItems.length
  },
  actions: {
    async fetchCart() {
      const userStore = useUserStore();
      if (!userStore.isLogin) {
        // 用户未登录，尝试从localStorage加载并进行映射 (因为localStorage中也可能存的是productPrice)
        const localCartItems = JSON.parse(localStorage.getItem('cartItems') || '[]');
        this.cartItems = localCartItems.map(localItem => ({
          ...localItem, // 保留localStorage中的其他字段如selected, quantity等
          name: localItem.productName || localItem.name,         // 统一为 name
          price: parseFloat(localItem.productPrice || localItem.price) || 0, // 统一为 price，并确保是数字
          // 确保 quantity 也是数字
          quantity: parseInt(localItem.quantity) || 0,
        }));
        this.hasLoadedOnce = true; // 标记尝试加载过（即使从localStorage）
        return;
      }
  
      this.isLoading = true;
      try {
        const response = await getCartList(); // 调用API
        console.log('[cartStore fetchCart] API Response:', JSON.parse(JSON.stringify(response)));
  
        if (response && response.code === 200 && Array.isArray(response.data)) {
          console.log('[cartStore fetchCart] API success. Data received:', JSON.parse(JSON.stringify(response.data)));
  
          // ▼▼▼▼▼ 在这里进行数据映射 ▼▼▼▼▼
          this.cartItems = response.data.map(apiItem => ({
            id: apiItem.id,
            productId: apiItem.productId,
            quantity: parseInt(apiItem.quantity) || 0, // 确保是数字
            selected: apiItem.selected === undefined ? true : Boolean(apiItem.selected), // 处理selected，确保是布尔值，例如默认为true
            
            name: apiItem.productName,         // 将 productName 映射到 name
            productName: apiItem.productName,  // 也可以同时保留 productName
            
            productImage: apiItem.productImage,
            
            price: parseFloat(apiItem.productPrice) || 0, // 将 productPrice 映射到 price，并确保是数字
            productPrice: parseFloat(apiItem.productPrice) || 0, // 同时保留 productPrice，如果其他地方需要
  
            productStock: apiItem.productStock,
            productStatus: apiItem.productStatus
            // ... 根据您的 CartItemDTO 结构和前端组件的需要添加或调整其他字段
          }));
          // ▲▲▲▲▲ 数据映射结束 ▲▲▲▲▲
  
          console.log('[cartStore fetchCart] Mapped cartItems state:', JSON.parse(JSON.stringify(this.cartItems)));
          this.saveCart(); // 保存映射后的数据到localStorage
          this.hasLoadedOnce = true; // 标记已从服务器成功加载
        } else {
          console.warn('[cartStore fetchCart] API call did not return successful data or data is not an array. Response:', response);
          // API失败或数据格式不对，尝试从 localStorage 加载并映射
          const localCartItems = JSON.parse(localStorage.getItem('cartItems') || '[]');
          this.cartItems = localCartItems.map(localItem => ({
              ...localItem,
              name: localItem.productName || localItem.name,
              price: parseFloat(localItem.productPrice || localItem.price) || 0,
              quantity: parseInt(localItem.quantity) || 0,
          }));
          this.hasLoadedOnce = true;
        }
      } catch (error) {
        console.error('Failed to fetch cart from server (exception):', error);
        // API调用异常，也尝试从 localStorage 加载并映射
        const localCartItems = JSON.parse(localStorage.getItem('cartItems') || '[]');
        this.cartItems = localCartItems.map(localItem => ({
          ...localItem,
          name: localItem.productName || localItem.name,
          price: parseFloat(localItem.productPrice || localItem.price) || 0,
          quantity: parseInt(localItem.quantity) || 0,
        }));
        this.hasLoadedOnce = true;
      } finally {
        this.isLoading = false;
      }
    },

    async addToCart(product) {
      console.log('[cartStore] addToCart action called with product:', product.id);
      const userStore = useUserStore();

      const existingClientItem = this.cartItems.find(item => item.id === product.id);
      if (existingClientItem) {
        existingClientItem.quantity++;
      } else {
        this.cartItems.push({ ...product, quantity: 1, selected: true });
      }
      this.saveCart();

      if (!userStore.isLogin) {
        console.warn('[cartStore] User not logged in. Cart changes saved to localStorage only.');
        return;
      }

      try {
        console.log('[cartStore] Attempting to sync cart addition with server for product ID:', product.id);
        // ▼▼▼ 使用导入的 addToCart ▼▼▼
        const response = await addToCart({ productId: product.id, quantity: 1 });
        // ▲▲▲ 调用修正 ▲▲▲
        console.log('[cartStore] Product addition synced with server. Response:', response);
        // 成功后，考虑是否需要调用 this.fetchCart() 来获取最新的整个购物车状态
        // await this.fetchCart();
      } catch (error) {
        console.error('[cartStore] Failed to sync cart addition with server:', error);
        // 注意：如果API调用失败，您可能需要回滚客户端的乐观更新
        // (例如，如果商品刚才在客户端被添加或数量增加，现在需要撤销这个操作)
        // 这是一个更复杂的同步逻辑。
        // ElMessage.error('同步购物车失败'); // 提示可以在组件层面做
        throw error;
      }
    },
	
	async updateQuantity(cartItemId, newQuantity) {
	    const userStore = useUserStore();
	    console.log(`[cartStore] updateQuantity action. Item ID: ${cartItemId}, New Qty: ${newQuantity}`);
	
	    // 找到客户端的项，以便在API失败时可能需要回滚
	    const clientItemIndex = this.cartItems.findIndex(i => i.id === cartItemId);
	    const originalClientQuantity = clientItemIndex !== -1 ? this.cartItems[clientItemIndex].quantity : null;
	
	    // 乐观更新客户端状态 (可选，但如果做，则需考虑回滚)
	    if (clientItemIndex !== -1) {
	      if (newQuantity <= 0) {
	        this.cartItems.splice(clientItemIndex, 1); // 数量为0则移除
	      } else {
	        this.cartItems[clientItemIndex].quantity = newQuantity;
	      }
	      this.saveCart(); // 更新 localStorage
	    }
	
	    if (!userStore.isLogin) {
	      console.warn('[cartStore] User not logged in. Quantity update to localStorage only.');
	      if (newQuantity <= 0 && clientItemIndex === -1) { // 如果未登录且尝试将一个本地不存在的项数量改为0，则无需操作
	          return;
	      }
	      // 如果未登录，操作已在localStorage完成，直接成功或根据业务决定是否提示
	      // ElMessage.success('数量已在本地更新'); // 示例提示
	      return;
	    }
	
	    try {
	      const response = await updateCartItem({ id: cartItemId, quantity: newQuantity }); // 调用API
	      console.log('[cartStore] Quantity update synced with server. Response:', response);
	
	      // API 调用成功后，后端通常会返回更新后的购物车项或整个购物车
	      // 用后端返回的权威数据更新store是最佳实践
	      // 例如，如果后端返回更新后的项:
	      if (response && response.code === 200 && response.data) {
	        const updatedItemFromServer = response.data; // 这是 CartItemDTO
	        const index = this.cartItems.findIndex(item => item.id === updatedItemFromServer.id);
	        if (index !== -1) {
	          if (updatedItemFromServer.quantity > 0) { // 假设后端会正确处理数量<=0时是否移除项
	            // 将API返回的 productPrice 映射为 price
	            const storeItem = {
	                ...updatedItemFromServer,
	                price: updatedItemFromServer.productPrice 
	            };
	            this.cartItems.splice(index, 1, storeItem);
	          } else { // 如果后端表示该项已删除（例如返回数量为0或不返回该项）
	            this.cartItems.splice(index, 1);
	          }
	        } else if (updatedItemFromServer.quantity > 0) { // 如果本地没有但后端返回了（不太可能在此流程）
	            const storeItem = {
	                ...updatedItemFromServer,
	                price: updatedItemFromServer.productPrice
	            };
	            this.cartItems.push(storeItem);
	        }
	        this.saveCart();
	      } else if (response && response.code !== 200) { 
	        // 业务失败，例如库存不足 (code: 1302)
	        // 需要回滚之前的乐观更新
	        if (originalClientQuantity !== null && clientItemIndex !== -1) { // 确保我们有原始数量并且项还存在（或刚被乐观移除）
	            if (newQuantity <= 0) { // 之前是乐观删除了
	                // 需要重新从服务器获取整个购物车，因为不确定这个item是否真的应该被删除或恢复到什么数量
	                 await this.fetchCart(); // 或者根据业务逻辑决定如何恢复
	            } else {
	                 this.cartItems[clientItemIndex].quantity = originalClientQuantity; // 恢复数量
	            }
	        } else {
	            // 如果本地没有这个项，但API调用失败，可能也需要刷新整个购物车
	            await this.fetchCart();
	        }
	        this.saveCart();
	        throw new Error(response.message || '更新数量失败'); // 抛出业务错误给组件
	      } else {
	        // API成功，但返回数据结构意外，也刷新一下
	        await this.fetchCart();
	      }
	    } catch (error) {
	      console.error(`[cartStore] Failed to sync quantity update for item ID ${cartItemId}:`, error.message);
	      // API 调用彻底失败 (网络错误，或上面 re-throw 的业务错误)
	      // 回滚客户端的乐观更新
	      if (originalClientQuantity !== null && clientItemIndex !== -1) {
	         if (newQuantity <= 0 && this.cartItems.findIndex(i => i.id === cartItemId) === -1) { // 之前是乐观删除了但现在需要恢复
	            // 这里比较复杂，因为不知道原始商品信息了，最好是重新fetch
	            await this.fetchCart();
	         } else if (this.cartItems[clientItemIndex]) { // 确保项还存在
	            this.cartItems[clientItemIndex].quantity = originalClientQuantity;
	         }
	      } else if (originalClientQuantity === null && clientItemIndex !== -1 && newQuantity > 0) { // 本地新增的项，但同步失败
	          this.cartItems.splice(clientItemIndex, 1); // 移除这个乐观添加的项
	      } else {
	          // 其他复杂情况，统一刷新购物车
	          await this.fetchCart();
	      }
	      this.saveCart();
	      throw error; // 重新抛出错误，让组件捕获并提示用户
	    }
	  },
	
	async toggleSelect(itemId) {
	      const userStore = useUserStore();
	      console.log(`[cartStore] toggleSelect action called for item ID: ${itemId}`);
	
	      const itemIndex = this.cartItems.findIndex(i => i.id === itemId);
	      if (itemIndex === -1) {
	        console.error(`[cartStore] Item with ID ${itemId} not found in cartItems.`);
	        // 可以考虑抛出错误或返回一个表示失败的状态
	        throw new Error(`购物车中未找到ID为 ${itemId} 的商品`);
	      }
	
	      const itemToToggle = this.cartItems[itemIndex];
	      const newSelectedState = !itemToToggle.selected;
	
	      // 1. 乐观更新客户端状态
	      itemToToggle.selected = newSelectedState;
	      this.saveCart(); // 更新 localStorage
	
	      if (!userStore.isLogin) {
	        console.warn('[cartStore] User not logged in. Toggle select change saved to localStorage only.');
	        return; // 未登录则不调用后端API，直接返回 (或者根据业务返回一个值)
	      }
	
	      // 2. 调用后端API同步状态
	      try {
	        console.log(`[cartStore] Attempting to sync select state for item ID ${itemId} to ${newSelectedState}`);
	        // API 函数 toggleSelectCartItem(id, data) 期望第二个参数是 { selected: boolean }
	        const response = await toggleSelectCartItem(itemId, { selected: newSelectedState });
	        console.log('[cartStore] Select state synced with server. Response:', response);
	
	        if (response && response.code === 200) {
	          // API 成功，如果后端返回了更新后的项，可以用它来更新 store 以确保数据一致性
	          // (尤其是如果后端有其他业务逻辑可能影响该项)
	          if (response.data) { // 假设 response.data 是更新后的 CartItemDTO
	             const updatedItemFromServer = {
	                 ...response.data,
	                 price: parseFloat(response.data.productPrice || response.data.price) || 0,
	                 name: response.data.productName || response.data.name
	                 // 其他必要的字段映射
	             };
	             this.cartItems.splice(itemIndex, 1, updatedItemFromServer);
	             this.saveCart(); // 再次保存到localStorage，因为API返回的可能更权威
	          }
	          // 如果不依赖API返回的具体项，乐观更新可能已足够，但最好与服务器同步
	          // 或者，可以考虑在所有写操作成功后统一调用 this.fetchCart()
	          // await this.fetchCart();
	        } else {
	          // 业务错误 (例如后端返回 code !== 200)
	          console.error('[cartStore] API business error toggling select state:', response);
	          itemToToggle.selected = !newSelectedState; // 回滚乐观更新
	          this.saveCart();
	          throw new Error(response?.message || '更新选择状态失败');
	        }
	      } catch (error) {
	        console.error(`[cartStore] Failed to sync select state for item ID ${itemId}:`, error);
	        // 网络错误或上面抛出的业务错误，回滚乐观更新
	        itemToToggle.selected = !newSelectedState;
	        this.saveCart();
	        throw error; // 重新抛出错误，让组件的catch处理UI提示
	      }
	    },
		
	async clearCart() {
	    const userStore = useUserStore();
	    const originalCartItems = JSON.parse(JSON.stringify(this.cartItems));
	
	    this.cartItems = []; // 乐观更新
	    this.saveCart(); // 如果有 saveCart 到 localStorage
	
	    if (!userStore.isLogin) return;
	
	    try {
	      const response = await clearCartApiService(); // 调用 API
	      if (!(response && response.code === 200)) {
	        this.cartItems = originalCartItems; // API 失败，回滚
	        this.saveCart();
	        throw new Error(response?.message || '服务端清空购物车失败');
	      }
	    } catch (error) {
	      this.cartItems = originalCartItems; // 异常，回滚
	      this.saveCart();
	      throw error;
	    }
	},
	
	// ★★★ 在这里添加 removeItemsByIds action ★★★
	    /**
	     * 根据提供的ID数组从购物车中移除多个商品项
	     * @param {Array<string|number>} itemIdsToRemove - 需要移除的购物车项ID的数组
	     */
	async removeItemsByIds(itemIdsToRemove) {
		const userStore = useUserStore(); // 如果需要根据登录状态做不同处理
	    console.log('[CartStore] removeItemsByIds called with IDs:', itemIdsToRemove);
		
		if (!Array.isArray(itemIdsToRemove)) {
		    console.error('[CartStore] removeItemsByIds: itemIdsToRemove 必须是一个数组');
		    return;
		    }
		
		// 1. 更新前端 Pinia store 中的 cartItems 状态
		this.cartItems = this.cartItems.filter(item => !itemIdsToRemove.includes(item.id));
		  
		// 2. 更新 localStorage
		this.saveCart(); 
		
		      // 3. 可选：与后端同步
		      //    这取决于您的业务逻辑。订单成功后，这些购物车项在后端可能已经自动清除了，
		      //    或者后端提供了批量删除购物车项的API。
		      //    如果您有一个批量删除的API:
		      //    if (userStore.isLogin) {
		      //        try {
		      //            // 假设您有一个名为 batchRemoveCartItems 的API函数
		      //            // await batchRemoveCartItems(itemIdsToRemove); 
		      //            console.log('[CartStore] Successfully synced item removals with server.');
		      //        } catch (error) {
		      //            console.error('[CartStore] Failed to sync item removals with server:', error);
		      //            // 这里可能需要错误处理或回滚之前的本地状态修改（如果适用）
		      //        }
		      //    }
		      //    或者，如果您的后端 `removeCartItem` API 一次只能删一个，但您仍希望后端同步：
		      //    if (userStore.isLogin) {
		      //        itemIdsToRemove.forEach(async (itemId) => {
		      //            try {
		      //                await removeCartItem(itemId); // 循环调用单个删除API
		      //            } catch (error) {
		      //                console.error(`[CartStore] Failed to remove item ${itemId} from server:`, error);
		      //            }
		      //        });
		      //    }
		      //    对于订单成功后清空已购买商品，通常前端本地移除即可，因为这些商品已转化为订单项。
		      //    是否需要后端同步，取决于您的购物车在后端是如何设计的。
	},


    saveCart() {
      localStorage.setItem('cartItems', JSON.stringify(this.cartItems));
    },

    // 您之前localStorage版本中的其他action，如果也需要与后端同步，都需要类似地修改：
    // removeFromCart(productId) { ... 调用API ... then update local ... }
    // updateQuantity(productId, quantity) { ... 调用API ... then update local ... }
    // clearCart() { ... 调用API ... then update local ... }
    // 等等
  }
});