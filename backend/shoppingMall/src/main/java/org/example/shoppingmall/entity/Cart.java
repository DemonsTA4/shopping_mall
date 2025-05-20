package org.example.shoppingmall.entity; // 请将包名替换为您的实际项目路径

import jakarta.persistence.*; // JPA注解
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString; // 用于自定义ToString，避免循环引用
import lombok.EqualsAndHashCode; // 用于处理双向关联时的 equals 和 hashCode
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车实体类
 * <p>
 * 代表一个用户的购物车，它包含多个购物车项 (CartItem)。
 * 这个模型中，CartItem 通过自身的 'cart' 字段关联回这个Cart实体。
 * </p>
 */
@Entity
@Table(name = "carts")
@Data // Lombok: 自动生成 getters, setters, equals, hashCode, toString
@NoArgsConstructor // Lombok: 自动生成无参构造函数
@AllArgsConstructor // Lombok: 自动生成全参构造函数
public class Cart {

    /**
     * 购物车唯一标识符 (主键)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 数据库自增策略
    private Long id;

    /**
     * 购物车所属的用户。
     * 一个用户通常只有一个活跃的购物车，因此这里使用 OneToOne 关系。
     * 'user_id' 列将是唯一且不能为空的。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // 一个用户通常只有一个活动购物车，所以可以是 unique
    @ToString.Exclude
    private User user; // ★★★ 关联到 User 实体 ★★★

    /**
     * 购物车中的商品项列表。
     * 一个购物车可以包含多个不同的商品项 (CartItem)。
     * 'mappedBy = "cart"' 表示这个关联关系由 CartItem 实体中的 'cart' 字段来维护。
     * CascadeType.ALL 表示对购物车的操作（如保存、删除）会级联到其包含的商品项。
     * orphanRemoval = true 表示如果一个 CartItem 从这个 'items' 集合中被移除，
     * 并且不再被其他地方引用，它将被从数据库中删除。
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // 避免在toString中因Cart与CartItem的双向关联导致栈溢出
    @EqualsAndHashCode.Exclude // 同上
    private List<CartItem> items = new ArrayList<>(); // 初始化为空列表，避免空指针异常

    /**
     * 记录创建时间
     * 使用 @CreationTimestamp 注解，在实体首次持久化时自动设置为当前时间。
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 记录最后更新时间
     * 使用 @UpdateTimestamp 注解，在实体每次更新时自动设置为当前时间。
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 便捷方法，用于向购物车添加商品项。
     * 此方法同时维护了 Cart 和 CartItem 之间的双向关联。
     *
     * @param item 要添加的购物车项
     */
    public void addItem(CartItem item) {
        if (item != null) {
            items.add(item);
            item.setCart(this); // 关键：设置 CartItem 中的 Cart 引用，维护双向关系
        }
    }

    /**
     * 便捷方法，用于从购物车移除商品项。
     * 此方法同时维护了 Cart 和 CartItem 之间的双向关联。
     *
     * @param item 要移除的购物车项
     */
    public void removeItem(CartItem item) {
        if (item != null) {
            items.remove(item);
            item.setCart(null); // 解除 CartItem 与此 Cart 的关联
        }
    }

    /**
     * 构造函数，方便通过用户创建购物车。
     *
     * @param user 购物车所属的用户
     */
    public Cart(User user) {
        this.user = user;
    }
}
