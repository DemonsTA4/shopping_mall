package org.example.shoppingmall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.dto.AddressDto;
import org.example.shoppingmall.service.AddressService;
import org.example.shoppingmall.security.CustomUserDetails; // 假设您用这个
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
// 导入自定义异常，例如
import org.example.shoppingmall.exception.UnauthorizedException; // 如果需要

@RestController
@RequestMapping("/api/user/address") // 确保这个基础路径与前端API调用匹配
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // 辅助方法从Principal获取用户ID (如果需要，或者直接在每个方法中使用currentUser.getId())
    private Long getUserIdFromPrincipal(CustomUserDetails currentUser) {
        if (currentUser == null) {
            // 在实际应用中，如果 @AuthenticationPrincipal 为 null，
            // 通常意味着请求未认证，Spring Security 的过滤器会在此之前就拦截。
            // 但作为防御性编程，可以保留检查。
            throw new UnauthorizedException("用户未认证或无法获取用户信息");
        }
        return currentUser.getId(); // 假设 CustomUserDetails 有 getId() 返回 Long
    }

    @GetMapping
    public Result<List<AddressDto>> getAddressList(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long currentUserId = getUserIdFromPrincipal(currentUser);
        List<AddressDto> addresses = addressService.getUserAddresses(currentUserId);
        return Result.success(addresses);
    }

    @PostMapping
    public Result<AddressDto> addAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                         @RequestBody @Valid AddressDto addressDtoFromRequest) {
        Long currentUserId = getUserIdFromPrincipal(currentUser);
        addressDtoFromRequest.setId(null); // 确保创建时ID为空
        AddressDto newAddress = addressService.createAddress(currentUserId, addressDtoFromRequest);
        return Result.success(newAddress);
    }

    @GetMapping("/{id}")
    public Result<AddressDto> getAddressById(@AuthenticationPrincipal CustomUserDetails currentUser,
                                             @PathVariable Long id) { // 路径变量是 Long
        Long currentUserId = getUserIdFromPrincipal(currentUser);
        // 调用 Service 时，将 Long id 转换为 Integer
        AddressDto address = addressService.getAddressById(currentUserId, id.intValue()); // ★★★ 类型转换 ★★★
        return Result.success(address);
    }

    @PutMapping("/{id}")
    public Result<AddressDto> updateAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                            @PathVariable Long id, // 路径变量是 Long
                                            @RequestBody @Valid AddressDto addressDTO) {
        Long currentUserId = getUserIdFromPrincipal(currentUser);
        // 调用 Service 时，将 Long id 转换为 Integer
        AddressDto updatedAddress = addressService.updateAddress(currentUserId, id.intValue(), addressDTO); // ★★★ 类型转换 ★★★
        return Result.success(updatedAddress);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                      @PathVariable Long id) { // 路径变量是 Long
        Long currentUserId = getUserIdFromPrincipal(currentUser);
        // 调用 Service 时，将 Long id 转换为 Integer
        addressService.deleteAddress(currentUserId, id.intValue()); // ★★★ 类型转换 ★★★
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                          @PathVariable Long id) { // 路径变量是 Long
        Long currentUserId = getUserIdFromPrincipal(currentUser);
        // 调用 Service 时，将 Long id 转换为 Integer
        addressService.setDefaultAddress(currentUserId, id.intValue()); // ★★★ 类型转换 ★★★
        return Result.success();
    }
}