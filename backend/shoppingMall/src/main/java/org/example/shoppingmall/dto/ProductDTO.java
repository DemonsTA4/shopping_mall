package org.example.shoppingmall.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100个字符")
    private String name;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    private String categoryName;
    
    @Size(max = 500, message = "商品描述长度不能超过500个字符")
    private String description;
    
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;
    
    private BigDecimal originalPrice;
    
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于0")
    private Integer stock;
    
    private Integer status = 1; // 默认上架
    

    private String imageUrl;
    
    private List<String> images;
    
    private String detail;

    private Boolean isFeatured;
    
    private List<String> params;
    
    private List<String> specs;
    
    private Integer sales = 0;
    
    private LocalDateTime createTime;
} 