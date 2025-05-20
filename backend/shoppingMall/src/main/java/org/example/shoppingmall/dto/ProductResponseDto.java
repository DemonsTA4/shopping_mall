package org.example.shoppingmall.dto; // 请将包名替换为您的实际项目路径

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.shoppingmall.entity.Category; // 假设 Category 是一个实体类

import java.math.BigDecimal;
// 如果有其他复杂类型，也需要导入

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private Long id; // 商品ID (假设 Product 实体的 ID 是 Long 类型)
    private String name; // 商品名称
    private BigDecimal price; // 商品价格
    private String imageUrl; // 商品主图片URL

    // 以下字段是根据您代码中的 dto.setCategory(product.getCategory()) 等推断的
    // 您需要确保 Product 实体中有对应的字段和getter方法，并且类型匹配

    private CategoryInfo category; // 商品分类信息 (下面定义一个嵌套DTO或直接用Category实体)
    // 如果 Category 是一个复杂的实体，通常会为其创建一个简化的 CategoryInfoDto

    private BrandInfo brand; // 商品品牌信息 (下面定义一个嵌套DTO或假设有一个 Brand 实体/DTO)
    // 如果 Brand 只是一个字符串，则类型为 String

    private Integer stockQuantity; // 商品库存数量

    // 其他您可能希望在商品列表中或商品详情中展示的字段：
    private String description; // 商品描述 (可选)
    private BigDecimal originalPrice; // 商品原价 (可选)
    private Integer sales; // 商品销量 (可选)
    private Double averageRating; // 商品平均评分 (可选, 可能需要额外计算)
    private Boolean isActive; // 商品是否上架/激活 (可选)
    // private List<String> tags; // 商品标签 (可选)


    // --------------------------------------------------------------------
    // 嵌套 DTO 用于更清晰地表示关联信息 (如果 Category 和 Brand 是复杂对象)
    // --------------------------------------------------------------------

    /**
     * 嵌套静态类，用于表示简化的分类信息
     * 如果 Product.getCategory() 返回的是 Category 实体，
     * 并且您不想在 ProductResponseDto 中暴露整个 Category 实体，
     * 可以在转换时手动创建 CategoryInfo。
     * 如果 getCategory() 返回的就是一个简单的 String 或 Integer (分类ID)，则不需要这个。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id; // 分类ID (假设 Category 实体的 ID 是 Long)
        private String name; // 分类名称
        // 可以添加其他需要的分类信息，如 icon 等
    }

    /**
     * 嵌套静态类，用于表示简化的品牌信息
     * 同上，如果 Product.getBrand() 返回的是 Brand 实体。
     * 如果 getBrand() 返回的就是品牌名称字符串，则 ProductResponseDto.brand 应为 String 类型。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandInfo {
        private Long id; // 品牌ID (假设 Brand 实体的 ID 是 Long)
        private String name; // 品牌名称
        private String logoUrl; // 品牌Logo (可选)
    }

    // 如果您在 FavoriteServiceImpl 的转换逻辑中是这样做的：
    // dto.setCategory(new CategoryInfo(product.getCategory().getId(), product.getCategory().getName()));
    // dto.setBrand(new BrandInfo(product.getBrand().getId(), product.getBrand().getName(), product.getBrand().getLogoUrl()));
    // 那么这里的嵌套DTO定义就是合适的。

    // 如果 product.getCategory() 直接返回 String (分类名) 或者 Integer (分类ID)
    // 那么 ProductResponseDto 中的 category 字段类型也应相应调整，例如：
    // private String categoryName;
    // private Integer categoryId;
}