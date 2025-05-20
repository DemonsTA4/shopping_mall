package org.example.shoppingmall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.common.ResultCode;
import org.example.shoppingmall.dto.PageResult;
import org.example.shoppingmall.dto.ProductDTO;
import org.example.shoppingmall.security.CustomUserDetails;
import org.example.shoppingmall.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<PageResult<ProductDTO>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String type) {
        return Result.success(productService.getProducts(keyword, categoryId, pageNum, pageSize, sort, order, minPrice, maxPrice, type));
    }

    @GetMapping("/{id}")
    public Result<ProductDTO> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')") // 实际检查 "ROLE_SELLER"
    public Result<ProductDTO> createProduct(
            @RequestPart("productData") @Valid ProductDTO productDTO,
            @RequestPart(name = "mainImageFile", required = true) MultipartFile mainImageFile,
            @RequestPart(name = "otherImageFiles", required = false) List<MultipartFile> otherImageFiles,
            @AuthenticationPrincipal CustomUserDetails currentUser) { // ★★★ 注入当前认证用户 ★★★

        if (currentUser == null) {
            // 理论上 @PreAuthorize 已经处理了未认证的情况，但可以加一层保险
            return Result.error(ResultCode.USER_NOT_EXISTS);
        }

        Long sellerId = currentUser.getSellerId(); // ★★★ 从 CustomUserDetails 获取 sellerId ★★★

        if (sellerId == null) {
            // 这意味着用户有 ROLE_SELLER，但 getSellerId() 返回了 null
            // 这通常不应该发生，除非 CustomUserDetails.getSellerId() 逻辑有问题
            // 或者用户虽然有SELLER角色，但不符合成为卖家的其他条件（如果getSellerId有更复杂逻辑）
            return Result.error(ResultCode.USER_NOT_EXISTS);
        }

        // ★★★ 调用 ProductService 的方法，并传递 sellerId ★★★
        return Result.success(productService.createProduct(productDTO, mainImageFile, otherImageFiles, sellerId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public Result<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody @Valid ProductDTO productDTO) {
        return Result.success(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('SELLER')")
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        return Result.success(productService.uploadProductImage(file));
    }
} 