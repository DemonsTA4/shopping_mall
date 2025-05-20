package org.example.shoppingmall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.dto.CartItemDTO;
import org.example.shoppingmall.dto.AddToCartDto; // <--- 1. 导入新的 DTO
import org.example.shoppingmall.dto.UpdateCartItemRequestDto;
import org.example.shoppingmall.dto.SelectCartItemRequestDto;
import org.example.shoppingmall.service.CartService;
// import org.example.shoppingmall.security.CustomUserDetails; // 如果需要获取当前用户ID
// import org.springframework.security.core.annotation.AuthenticationPrincipal; // 如果需要获取当前用户ID
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/count")
    public Result<Integer> getCartCount(/*@AuthenticationPrincipal CustomUserDetails currentUser*/) {
        // Long userId = currentUser.getId(); // 如果 cartService 需要 userId
        return Result.success(cartService.getCartCount(/*userId*/));
    }

    @GetMapping("/list")
    public Result<List<CartItemDTO>> getCartList(/*@AuthenticationPrincipal CustomUserDetails currentUser*/) {
        // Long userId = currentUser.getId(); // 如果 cartService 需要 userId
        return Result.success(cartService.getCartList(/*userId*/));
    }

    // ▼▼▼▼▼ 修改开始 ▼▼▼▼▼
    @PostMapping("/add")
    public Result<CartItemDTO> addToCart(
            // @AuthenticationPrincipal CustomUserDetails currentUser, // 如果Service层需要用户ID
            @Valid @RequestBody AddToCartDto addToCartRequest) { // <--- 2. 使用 @RequestBody 和新的 DTO

        Long productId = addToCartRequest.getProductId();
        Integer quantity = addToCartRequest.getQuantity();

        // Long userId = currentUser.getId(); // 如果 cartService 需要 userId

        // 假设您的 cartService.addToCart 方法签名是 (Long userId, Long productId, Integer quantity)
        // 或者您需要调整 service 方法或在这里适配
        // 为了匹配您之前的 cartService.addToCart(productId, quantity)，
        // 我们需要确保 CartService 也被相应调整，或者假设当前用户ID由Service内部处理（例如从安全上下文）
        // 如果 CartService.addToCart(Long productId, Integer quantity) 是针对当前登录用户的，
        // 那么 Service 内部需要一种方式获取当前用户ID。

        // 调用 CartService，假设它能处理当前用户的购物车
        // 如果您的 cartService.addToCart 签名是 (Long productId, Integer quantity)
        // 并且它能从 SecurityContextHolder 获取当前用户ID，则可以直接调用：
        CartItemDTO cartItem = cartService.addToCart(productId, quantity);

        // 如果您的 cartService.addToCart 签名是 (Long userId, Long productId, Integer quantity)
        // 您需要获取 userId，例如:
        // Long userId = getCurrentUserIdFromSecurityContext(); // 或者通过 @AuthenticationPrincipal
        // CartItemDTO cartItem = cartService.addToCart(userId, productId, quantity);

        return Result.success(cartItem);
    }
    // ▲▲▲▲▲ 修改结束 ▲▲▲▲▲

    @PutMapping("/update")
    public Result<CartItemDTO> updateCartItemQuantity(
            @Valid @RequestBody UpdateCartItemRequestDto updateRequest) { // <--- 使用 @RequestBody 和新的 DTO

        Long cartItemId = updateRequest.getId();
        Integer quantity = updateRequest.getQuantity();

        // 假设 cartService.updateCartItemQuantity 需要 cartItemId 和 quantity
        // 并且它能从 SecurityContextHolder 获取当前用户ID，或不需要用户ID直接操作（不推荐）
        CartItemDTO updatedItem = cartService.updateCartItemQuantity(cartItemId, quantity);
        return Result.success(updatedItem);
    }

    @DeleteMapping("/remove/{id}")
    public Result<Void> removeFromCart(
            // @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id) { // 假设这里的 'id' 是购物车项的ID
        // Long userId = currentUser.getId();
        cartService.removeFromCart(/*userId,*/ id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clearCart(/*@AuthenticationPrincipal CustomUserDetails currentUser*/) {
        // Long userId = currentUser.getId();
        cartService.clearCart(/*userId*/);
        return Result.success();
    }

    // 注意: /update 和 /remove/{id} 中的 'id' 参数是指购物车项本身的ID，
    // 而不是商品ID (productId)。前端调用时需要传递正确的ID。

    @PutMapping("/{id}/select")
    public Result<CartItemDTO> updateCartItemSelected(
            @PathVariable Long id, // 路径中的购物车项ID (Service层如果是Integer，记得转换)
            @Valid @RequestBody SelectCartItemRequestDto selectRequest) { // <--- 使用 @RequestBody 和新的 DTO

        Boolean selectedValue = selectRequest.getSelected();

        // 假设 cartService.updateCartItemSelected 需要 (Long cartItemId, Boolean selected)
        // 并且它能从 SecurityContextHolder 获取当前用户ID，或在其内部进行用户权限校验
        // 注意：如果 service 层 addressId 是 Integer，则需要 id.intValue()
        CartItemDTO updatedItem = cartService.updateCartItemSelected(id, selectedValue); // 假设service层id是Long
        return Result.success(updatedItem);
    }

    @PutMapping("/select-all")
    public Result<List<CartItemDTO>> updateAllCartItemsSelected(
            // @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Boolean selected) {
        // Long userId = currentUser.getId();
        return Result.success(cartService.updateAllCartItemsSelected(/*userId,*/ selected));
    }
}