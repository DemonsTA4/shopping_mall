package org.example.shoppingmall.entity; // 请替换为您的实际包路径

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName; // 商品名称快照

    @Column(name = "product_image")
    private String productImage; // 商品图片快照

    @Column(name = "product_specs", length = 500) // 新增字段：商品规格快照
    private String productSpecs;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // 下单时单价快照

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal; // 小计金额

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void calculateSubtotal() {
        if (this.unitPrice != null && this.quantity != null && this.quantity > 0) {
            this.subtotal = this.unitPrice.multiply(new BigDecimal(this.quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    // 构造函数更新以包含 productSpecs
    // 假设 Product 实体本身不直接有 specs 字符串，而是从某个变体或选择中获取
    public OrderItem(Order order, Product product, Integer quantity, String productSpecs) {
        this.order = order;
        this.product = product;
        this.productName = product.getName();
        this.productImage = product.getImageUrl();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        this.productSpecs = productSpecs; // 设置规格
        calculateSubtotal();
    }

    // 如果不传递 order，或者在 Order.addItem 中设置 order
    public OrderItem(Product product, Integer quantity, String productSpecs) {
        if (product == null) {
            throw new IllegalArgumentException("订单项中的商品不能为空 (Product cannot be null for OrderItem)");
        }
        this.product = product;
        this.productName = product.getName();
        this.productImage = product.getImageUrl();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        this.productSpecs = productSpecs; // 设置规格
        calculateSubtotal();
    }
}