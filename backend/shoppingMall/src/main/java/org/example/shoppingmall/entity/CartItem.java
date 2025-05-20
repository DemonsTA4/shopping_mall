package org.example.shoppingmall.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items", uniqueConstraints = {
        // 一个购物车内，同一个商品通常只有一条记录，通过数量表示
        @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 该购物车项所属的购物车。
     * 使用 @ManyToOne 表示多个购物车项可以属于同一个购物车。
     * 'cart_id' 将是 cart_items 表中指向 carts 表的外键。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false) // 外键列名通常是 cart_id
    @ToString.Exclude // 避免在Cart和CartItem之间因双向关联产生toString死循环
    private Cart cart; // <--- 修改为直接关联 Cart 实体

    /**
     * 该购物车项对应的商品。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    /**
     * 该商品在购物车中的数量。
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * 该购物车项是否被选中 (例如，用于结算)。
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean selected = true;

    /**
     * 记录创建时间。
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 记录最后更新时间。
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Lombok @Data 会为 private Cart cart; 字段自动生成:
    // public Cart getCart()
    // public void setCart(Cart cart)
    // 同样会为其他字段生成相应的 getter 和 setter。
}
