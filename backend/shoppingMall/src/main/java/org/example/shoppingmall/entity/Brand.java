package org.example.shoppingmall.entity; // 请将包名替换为您的实际项目路径

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
// import java.util.Set; // 如果需要反向关联到 Product

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "brands", indexes = {
        @Index(name = "idx_brand_name", columnList = "name", unique = true) // 品牌名称通常是唯一的
})
public class Brand {

    /**
     * 品牌ID (主键)
     * 假设使用Long类型，与项目中其他ID类型保持一致
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 数据库自增策略
    private Long id;

    /**
     * 品牌名称
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 品牌Logo图片的URL (可选)
     */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /**
     * 品牌描述或故事 (可选)
     */
    @Lob // 对于可能较长的文本
    @Column(name = "description")
    private String description;

    /**
     * 品牌官方网站URL (可选)
     */
    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    /**
     * 排序值 (可选，用于品牌列表展示排序)
     */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;

    /**
     * 品牌状态 (例如：是否激活/推荐等) (可选)
     * 0: 不可用, 1: 可用
     */
    @Column(name = "status", columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 如果需要从品牌反向查询所有属于该品牌的产品 (通常不建议在 Brand 实体中直接加载，除非确实需要)
    // @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    // private Set<Product> products = new HashSet<>();


    // 方便创建对象的构造函数 (可以根据需要添加更多)
    public Brand(String name, String logoUrl, String description) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
    }
}