package org.example.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shoppingmall.common.ResultCode;
import org.example.shoppingmall.dto.CartItemDTO;
import org.example.shoppingmall.entity.Cart;
import org.example.shoppingmall.entity.CartItem;
import org.example.shoppingmall.entity.Product;
import org.example.shoppingmall.entity.User;
import org.example.shoppingmall.exception.ApiException;
import org.example.shoppingmall.repository.CartItemRepository;
import org.example.shoppingmall.repository.CartRepository; // 新增 CartRepository
import org.example.shoppingmall.repository.ProductRepository;
import org.example.shoppingmall.repository.UserRepository;
import org.example.shoppingmall.service.CartService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository; // 新增
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * 获取或创建当前用户的购物车。
     * @param user 当前用户
     * @return 用户的购物车实体
     */
    @Transactional
    public Cart getOrCreateUserCart(User user) {
        // 假设 CartRepository 的 findByUserId 方法名是正确的，并且 User.id 是 Long
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("No cart found for user {}, creating a new one.", user.getUsername());
                    Cart newCart = new Cart(user); // 使用接受 User 的构造函数
                    return cartRepository.save(newCart);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartCount() {
        User user = getCurrentUser();
        Optional<Cart> cartOptional = cartRepository.findByUserId(user.getId());
        return cartOptional.map(cart -> cart.getItems().size()).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartList() {
        User user = getCurrentUser();
        Optional<Cart> cartOptional = cartRepository.findByUserId(user.getId());
        if (cartOptional.isEmpty() || CollectionUtils.isEmpty(cartOptional.get().getItems())) {
            return new ArrayList<>();
        }
        return cartOptional.get().getItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItemDTO addToCart(Long productId, Integer quantity) {
        User user = getCurrentUser();
        Cart cart = getOrCreateUserCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ResultCode.PRODUCT_NOT_EXISTS));

        if (product.getStock() < quantity) {
            throw new ApiException(ResultCode.STOCK_NOT_ENOUGH);
        }

        // 检查购物车中是否已存在该商品
        Optional<CartItem> existingItemOptional = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        CartItem cartItem;
        if (existingItemOptional.isPresent()) {
            cartItem = existingItemOptional.get();
            // 更新数量，确保不超过库存
            int newQuantity = cartItem.getQuantity() + quantity;
            if (product.getStock() < newQuantity && quantity > 0) { // 再次检查总数是否超库存
                throw new ApiException(ResultCode.STOCK_NOT_ENOUGH, "添加后总数量将超过库存");
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setSelected(true);
        } else { // 创建新的 CartItem
            cartItem = new CartItem();
            // cartItem.setCart(cart); // 会由 cart.addItem(cartItem) 设置
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setSelected(true);
            // cartItem.setUser(user); // <--- 移除这一行
            cart.addItem(cartItem);
        }

        // 保存Cart实体，会级联保存或更新CartItem
        cartRepository.save(cart);

        // 需要返回新添加或更新的CartItem的DTO，需要从保存后的cart中找到它
        // 为了简单，我们假设cartItem对象在save(cart)后ID已更新 (如果它是新创建的)
        // 或者重新从cart.getItems()中查找一次以确保获取的是持久化状态的对象
        CartItem savedOrUpdatedItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(cartItem); // 回退到内存中的对象，如果上面查找失败（理论上不应）

        return convertToDTO(savedOrUpdatedItem);
    }

    @Override
    @Transactional
    public CartItemDTO updateCartItemQuantity(Long cartItemId, Integer quantity) {
        User user = getCurrentUser();
        Cart cart = getOrCreateUserCart(user);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ResultCode.CART_ITEM_NOT_EXISTS, "购物车项不存在或不属于当前用户"));

        // 确保 cartItem 确实属于当前用户的 cart (虽然上面的查找已经隐含了)
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApiException(ResultCode.CART_ITEM_NOT_EXISTS, "购物车项不属于当前用户的购物车");
        }

        Product product = cartItem.getProduct();
        if (quantity <= 0) { // 如果数量小于等于0，则视为删除
            cart.removeItem(cartItem); // 使用Cart实体中的removeItem方法
        } else {
            if (product.getStock() < quantity) {
                throw new ApiException(ResultCode.STOCK_NOT_ENOUGH);
            }
            cartItem.setQuantity(quantity);
        }

        cartRepository.save(cart); // 保存Cart会级联更新CartItem或处理orphanRemoval

        // 如果cartItem被移除了，这里返回什么？服务接口定义返回CartItemDTO
        // 如果数量<=0导致移除，可能应该返回null或修改服务接口
        // 当前实现下，如果移除了，cartItem对象还在，但不再在cart.items里
        // 为了安全，如果移除了，我们应该返回一个表示移除的DTO或修改接口
        if (quantity <= 0) {
            //  返回一个表示已删除的DTO，或者根据业务需求调整
            CartItemDTO removedDto = new CartItemDTO();
            BeanUtils.copyProperties(cartItem, removedDto, "id"); // 复制一些信息
            removedDto.setQuantity(0); // 表示数量为0
            return removedDto; // 或者修改接口，此方法不返回DTO或抛特定异常
        }

        return convertToDTO(cartItem);
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartItemId) {
        User user = getCurrentUser();
        Cart cart = getOrCreateUserCart(user);

        CartItem cartItemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ResultCode.CART_ITEM_NOT_EXISTS, "购物车项不存在或不属于当前用户"));

        cart.removeItem(cartItemToRemove); // 使用Cart实体中的removeItem方法
        cartRepository.save(cart); // orphanRemoval=true 会删除数据库中的CartItem
    }

    @Override
    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateUserCart(user);

        cart.getItems().clear(); // 清空Cart实体中的items列表
        cartRepository.save(cart); // orphanRemoval=true 会删除所有关联的CartItem
        // 或者直接调用 cartItemRepository.deleteByCart_Id(cart.getId());
        // 但通过操作Cart实体集合更符合JPA的领域模型驱动方式
    }

    @Override
    @Transactional
    public CartItemDTO updateCartItemSelected(Long cartItemId, Boolean selected) {
        User user = getCurrentUser();
        Cart cart = getOrCreateUserCart(user);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ResultCode.CART_ITEM_NOT_EXISTS, "购物车项不存在或不属于当前用户"));

        cartItem.setSelected(selected);
        cartRepository.save(cart); // 保存Cart会级联更新CartItem

        return convertToDTO(cartItem);
    }

    @Override
    @Transactional
    public List<CartItemDTO> updateAllCartItemsSelected(Boolean selected) {
        User user = getCurrentUser();
        Cart cart = getOrCreateUserCart(user);

        if (CollectionUtils.isEmpty(cart.getItems())) {
            return new ArrayList<>();
        }

        cart.getItems().forEach(item -> item.setSelected(selected));
        cartRepository.save(cart); // 保存Cart会级联更新所有CartItem

        return cart.getItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ApiException(ResultCode.UNAUTHORIZED, "用户未认证");
        }
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ResultCode.USER_NOT_EXISTS, "当前登录用户不存在: " + username));
    }

    private CartItemDTO convertToDTO(CartItem cartItem) {
        if (cartItem == null) return null;
        CartItemDTO dto = new CartItemDTO();
        // 注意：BeanUtils.copyProperties(cartItem, dto) 可能会因为CartItem中没有直接的productId等字段而出错
        // 我们需要手动从 cartItem.getProduct() 获取信息

        dto.setId(cartItem.getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setSelected(cartItem.getSelected());

        Product product = cartItem.getProduct();
        if (product != null) {
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setProductImage(product.getImageUrl());
            dto.setProductPrice(product.getPrice());
            dto.setProductStock(product.getStock());
            dto.setProductStatus(product.getStatus());
        }

        return dto;
    }
}
