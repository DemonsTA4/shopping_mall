package org.example.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequestDto {
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemRequestDto> items;
    
    @NotBlank(message = "收货地址不能为空")
    @Size(max = 500, message = "收货地址长度不能超过500个字符")
    private String shippingAddress;
    
    private String receiverName;
    
    private String receiverPhone;
    
    private String receiverProvince;
    
    private String receiverCity;
    
    private String receiverDistrict;
    
    private String receiverPostalCode;
    
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String notes;
    
    private Integer paymentMethod; // 支付方式
} 