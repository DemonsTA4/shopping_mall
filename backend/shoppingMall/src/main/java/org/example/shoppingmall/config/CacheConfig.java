package org.example.shoppingmall.config; // 请将包名替换为您的实际项目路径

/**
 * 缓存配置常量类
 * <p>
 * 用于定义项目中所有缓存区域的名称。
 * 这样可以方便地在 @Cacheable, @CacheEvict, @CachePut 等注解中引用，
 * 避免硬编码字符串，提高代码的可维护性和一致性。
 */
public final class CacheConfig {

    /**
     * 私有构造函数，防止实例化该工具类。
     */
    private CacheConfig() {
        // 这个类不应该被实例化
    }

    /**
     * 统计信息相关的缓存名称。
     * <p>
     * 在 {@link org.example.shoppingmall.service.impl.StatisticsServiceImpl} 中使用。
     * 例如：
     * <ul>
     * <li><code>@Cacheable(value = CacheConfig.STATISTICS_CACHE, key = "'overview'")</code></li>
     * <li><code>@Cacheable(value = CacheConfig.STATISTICS_CACHE, key = "'detailed_' + #startDate + '_' + #endDate + '_' + #topCount")</code></li>
     * <li><code>@CacheEvict(value = CacheConfig.STATISTICS_CACHE, allEntries = true)</code></li>
     * </ul>
     */
    public static final String STATISTICS_CACHE = "statisticsCache";

    /**
     * 示例：产品信息相关的缓存名称。
     * (您可以根据需要添加其他缓存区域的常量)
     */
    // public static final String PRODUCTS_CACHE = "productsCache";

    /**
     * 示例：用户信息相关的缓存名称。
     */
    // public static final String USERS_CACHE = "usersCache";

    /**
     * 示例：分类信息相关的缓存名称。
     */
    // public static final String CATEGORIES_CACHE = "categoriesCache";

    // ... 可以根据您的应用需求添加更多缓存名称常量
}