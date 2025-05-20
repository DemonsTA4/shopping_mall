package org.example.shoppingmall.dto; // 确保包名与您的项目结构一致

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品搜索条件 数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDto {

    /**
     * 搜索关键词 (可以用于商品名称、描述等字段的模糊匹配)
     * 对应 ProductMapper#advancedSearch 中的 'keyword' 参数
     * 和 ProductMapper#selectByNameContaining 中的 'name' 参数
     */
    private String keyword;

    /**
     * 分类ID
     * 对应 ProductMapper#advancedSearch 和 ProductMapper#selectByCategoryId 中的 'categoryId' 参数
     */
    private Integer categoryId;

    /**
     * 卖家ID (如果需要按卖家筛选商品)
     * 对应 ProductMapper#selectBySellerId 中的 'sellerId' 参数
     * 即使 advancedSearch 中没有直接列出，作为一个通用的搜索DTO包含它也是合理的
     */
    private Integer sellerId;

    /**
     * 最低价格
     * 对应 ProductMapper#advancedSearch 中的 'minPrice' 参数
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     * 对应 ProductMapper#advancedSearch 中的 'maxPrice' 参数
     */
    private BigDecimal maxPrice;

    /**
     * 商品状态 (例如: 0=下架, 1=上架)
     * 对应 ProductMapper#selectByIsActiveTrue 方法的隐含条件
     * 以及 Product 实体中的 'status' 字段
     */
    private Integer status;

    // 其他可能的搜索条件可以根据需要添加，例如：
    // private String brand; // 品牌
    // private List<String> tags; // 标签
    // private Boolean hasStock; // 是否有库存

    // 注意：分页参数 (offset, limit) 和排序参数 (orderBy, orderDirection)
    // 在您的 ProductMapper#search 方法中是作为独立参数传递的，
    // 所以它们不一定需要包含在这个 ProductSearchDto 中。
    // 但如果希望将所有与查询相关的参数都封装起来，也可以将它们加入。
    // 为保持与您 Mapper 方法签名的一致性，这里暂时不添加分页和排序字段。
}