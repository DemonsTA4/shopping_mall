package org.example.shoppingmall.entity; // 请替换为您的实际包路径

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code", unique = true),
        @Index(name = "idx_coupon_active_times", columnList = "isActive, startTime, endTime")
})
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name; // 优惠券名称

    @Column(nullable = false, unique = true, length = 50)
    private String code; // 优惠券码

    @Column(length = 255)
    private String description; // 描述

    @Enumerated(EnumType.STRING) // 将枚举存储为字符串
    @Column(nullable = false, length = 30)
    private CouponType type; // 类型

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue; // 折扣值

    @Column(name = "min_purchase_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount; // 最低消费金额

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount; // 最大可抵扣金额 (用于百分比券)

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // 生效时间

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // 失效时间

    @Column(name = "total_count", nullable = false)
    private Integer totalCount; // 发放总数量 (0 或特定值表示无限，但通常用Integer.MAX_VALUE)

    @Column(name = "used_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer usedCount = 0; // 已使用/已领取数量

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true; // 是否激活

    /**
     * 拥有此优惠券的用户集合。
     * 使用 @ManyToMany 来表示用户和优惠券之间的多对多关系。
     * 'mappedBy = "coupons"' 表示关系的维护由 User 实体的 'coupons' 字段负责。
     * 这意味着在 User 实体中，你需要有一个名为 'coupons' 的字段，并使用 @JoinTable 来定义中间表。
     */
    @ManyToMany(mappedBy = "coupons", fetch = FetchType.LAZY)
    @ToString.Exclude // 避免循环引用导致的栈溢出
    @EqualsAndHashCode.Exclude // 避免循环引用导致的栈溢出
    private Set<User> users = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}