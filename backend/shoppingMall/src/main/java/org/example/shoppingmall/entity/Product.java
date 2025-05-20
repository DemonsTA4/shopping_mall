package org.example.shoppingmall.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString; // 导入ToString
import lombok.EqualsAndHashCode; // 导入EqualsAndHashCode

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet; // 导入 HashSet
import java.util.Set;     // 导入 Set

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude // 如果Category也反向关联Product
    @EqualsAndHashCode.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id") // 假设有Brand实体和关联
    @ToString.Exclude // 如果Brand也反向关联Product
    @EqualsAndHashCode.Exclude
    private Brand brand; // 品牌关联

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id") // 假设有User实体作为Seller和关联
    @ToString.Exclude // 如果User也反向关联Product
    @EqualsAndHashCode.Exclude
    private User seller; // 卖家关联

    private Long Sellerid;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false)
    private Integer stock;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(columnDefinition = "JSON")
    private String images; // 存储多张图片的JSON字符串或特定格式字符串

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(columnDefinition = "JSON")
    private String params; // 商品参数JSON

    @Column(columnDefinition = "JSON")
    private String specs; // 商品规格JSON

    @Column(name = "sales", columnDefinition = "INT DEFAULT 0")
    private Integer sales = 0;

    // --- 新增与 Comment 实体的 OneToMany 关联 ---
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // 避免与Comment的双向关联导致toString问题
    @EqualsAndHashCode.Exclude // 避免与Comment的双向关联导致equals/hashCode问题
    private Set<Comment> comments = new HashSet<>(); // <--- 商品拥有的评论集合

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "is_Featured", nullable = false,columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isFeatured = false;

    // 便捷方法添加评论 (如果需要在Product实体端管理)
    public void addComment(Comment comment) {
        if (comment != null) {
            this.comments.add(comment);
            comment.setProduct(this); // 维护双向关联
        }
    }

    public void removeComment(Comment comment) {
        if (comment != null) {
            this.comments.remove(comment);
            comment.setProduct(null); // 解除关联
        }
    }
}
