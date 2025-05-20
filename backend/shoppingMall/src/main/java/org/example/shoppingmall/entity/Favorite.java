package org.example.shoppingmall.entity; // 请替换为您的实际包路径

import jakarta.persistence.*; // JPA注解
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // 用于自动设置创建时间

import java.time.LocalDateTime;

/**
 * 商品收藏实体类
 */
@Entity
@Table(name = "favorites", uniqueConstraints = {
        // 添加组合唯一约束，确保一个用户对一个商品只能收藏一次
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
@Data // Lombok: 自动生成 getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: 自动生成无参构造函数
@AllArgsConstructor // Lombok: 自动生成全参构造函数
public class Favorite {

    /**
     * 收藏记录的唯一标识符 (主键)
     * Repository 定义的主键类型是 Integer
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 数据库自增策略
    private Integer id;

    /**
     * 收藏该商品的用户
     * 多对一关系：多个收藏记录可以属于同一个用户
     * Repository 中的 findByUserId 方法暗示了与User实体的关联，
     * Spring Data JPA 会自动将 findByUserId 映射为 findByUser_Id
     */
    @ManyToOne(fetch = FetchType.LAZY) // LAZY表示延迟加载，提高性能
    @JoinColumn(name = "user_id", nullable = false) // 外键列，不能为空
    private User user; // 假设您有一个名为 User 的实体类

    /**
     * 被收藏的商品
     * 多对一关系：多个收藏记录可以指向同一个商品
     * Repository 中的 findByUserIdAndProductId 和 countByProductId 方法暗示了与Product实体的关联
     * Spring Data JPA 会自动将 findBy...ProductId 映射为 findBy...Product_Id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // 外键列，不能为空
    private Product product; // 假设您有一个名为 Product 的实体类

    /**
     * 收藏时间
     * 使用 @CreationTimestamp 注解，在实体创建时自动设置为当前时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // 构造函数 (如果需要自定义，例如不包含id和createdAt)
    public Favorite(User user, Product product) {
        this.user = user;
        this.product = product;
    }
}