package org.example.shoppingmall.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDto {

    private Long id;

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal productPrice;

    private Integer quantity;

    private BigDecimal totalPrice;

    private String productSpecs;
}