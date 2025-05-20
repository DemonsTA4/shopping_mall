package org.example.shoppingmall.entity;// package org.example.shoppingmall.entity;
import jakarta.persistence.*;
import lombok.Data;
// ...
@Data
@Entity
@Table(name = "sellers") // 商家表
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 这个就是 seller_id

    @Column(name = "seller_name", nullable = false)
    private String sellerName;

    // 其他商家信息，如店铺描述、联系方式、地址等

    @OneToOne // 一个商家档案对应一个用户账户
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true) // 外键关联到 User 表的 id
    private User user;

    // ... getters and setters ...
}