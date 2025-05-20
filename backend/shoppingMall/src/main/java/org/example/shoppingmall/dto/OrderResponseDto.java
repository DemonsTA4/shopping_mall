package org.example.shoppingmall.dto;

import lombok.Data;
import org.example.shoppingmall.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {

    private Long id;

    private String orderNo;

    private Long userId;

    private String username;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal freightAmount;

    private Integer payType;

    private OrderStatus status;

    private String deliveryCompany;

    private String deliverySn;

    private String receiverName;

    private String receiverPhone;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverAddress;

    private String receiverPostalCode;

    private String note;

    private Integer confirmStatus;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime receiveTime;

    private LocalDateTime commentTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<OrderItemResponseDto> items;
}