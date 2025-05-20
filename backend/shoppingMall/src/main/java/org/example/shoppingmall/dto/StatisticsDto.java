package org.example.shoppingmall.dto; // 请将包名替换为您的实际项目路径

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // 导入Builder注解

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计数据 数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // 添加Lombok的Builder注解，方便链式创建对象
public class StatisticsDto {

    // --- 概览和详细统计都可能包含的字段 ---
    private Integer totalOrders;        // 总订单数
    private Integer totalProducts;      // 总商品数
    private Integer totalUsers;         // 总用户数
    private Integer totalCategories;    // 总分类数
    private BigDecimal totalRevenue;    // 总收入

    private LocalDate startDate;        // 统计数据的开始日期 (对于概览，可能是过去30天；对于详细，是指定日期)
    private LocalDate endDate;          // 统计数据的结束日期 (对于概览，是当前日期；对于详细，是指定日期)


    // --- 仅在详细统计中可能包含的字段 ---
    /**
     * 每日订单数量统计
     * Key: 日期字符串 (例如 "YYYY-MM-DD")
     * Value: 当日订单数量
     */
    private Map<String, Integer> dailyOrderCounts;

    /**
     * 每日收入统计
     * Key: 日期字符串 (例如 "YYYY-MM-DD")
     * Value: 当日收入金额
     */
    private Map<String, BigDecimal> dailyRevenue;

    /**
     * 热门用户统计列表 (例如按消费金额排序)
     */
    private List<UserStatsDto> topUsers;

    // (可以根据需要添加其他统计维度，例如热门商品、分类销售占比等)
    // private List<ProductStatsDto> topProducts;
    // private Map<String, BigDecimal> categorySalesPercentage;


    /**
     * 嵌套静态类，用于表示单个用户的统计信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserStatsDto {
        private Long userId;            // 用户ID
        private String username;        // 用户名
        private Integer orderCount;     // 该用户的订单总数
        private BigDecimal totalSpent;  // 该用户的总消费金额
    }

    /**
     * 示例：嵌套静态类，用于表示单个商品的统计信息 (如果需要)
     */
    // @Data
    // @NoArgsConstructor
    // @AllArgsConstructor
    // @Builder
    // public static class ProductStatsDto {
    //     private Long productId;         // 商品ID
    //     private String productName;     // 商品名称
    //     private Integer totalSalesCount; // 商品总销量
    //     private BigDecimal totalSalesAmount; // 商品总销售额
    // }
}