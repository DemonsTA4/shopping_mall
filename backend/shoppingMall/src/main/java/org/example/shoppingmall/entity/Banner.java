package org.example.shoppingmall.entity; // 请替换为您的实际包路径

import jakarta.persistence.*; // JPA注解
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Banner (广告横幅/轮播图) 实体类
 */
@Entity
@Table(name = "banners")
@Data // Lombok: 自动生成 getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: 自动生成无参构造函数
@AllArgsConstructor // Lombok: 自动生成全参构造函数
public class Banner {

    /**
     * Banner 唯一标识符 (主键)
     * Repository 定义的主键类型是 Integer
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 数据库自增策略
    private Integer id;

    /**
     * Banner 标题 (可选)
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * Banner 图片的 URL
     * 通常是必需的
     */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * 点击 Banner 后跳转的链接 URL (可选)
     */
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /**
     * Banner 是否激活/启用
     * Repository 中的 findByIsActiveTrue... 方法暗示了此属性
     * 数据库中通常映射为布尔值或小整数 (0 或 1)
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true; // 默认为true

    /**
     * Banner 的排序值，值越小/大越靠前 (根据具体业务定义)
     * Repository 中的 ...OrderBySortOrder 方法暗示了此属性
     */
    @Column(name = "sort_order", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0; // 默认为0

    /**
     * Banner 的展示位置 (例如: "homepage_top", "category_sidebar", "product_detail_promo")
     * Repository 中的 findByPosition... 方法暗示了此属性
     */
    @Column(name = "position", length = 100)
    private String position;

    /**
     * Banner 的描述信息 (可选)
     */
    @Lob // 对于可能较长的文本，可以使用 @Lob
    @Column(name = "description")
    private String description;

    /**
     * Banner 开始展示时间 (可选)
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * Banner 结束展示时间 (可选)
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * 记录创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 记录最后更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // 方便创建对象的构造函数 (可以根据需要添加更多)
    public Banner(String title, String imageUrl, String linkUrl, Boolean isActive, Integer sortOrder, String position) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
        this.position = position;
    }
}