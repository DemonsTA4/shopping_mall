package org.example.shoppingmall.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
// import org.example.shoppingmall.entity.Coupon; // 确保 Coupon 已导入或存在
// import org.example.shoppingmall.entity.Role; // 确保 Role 已导入或存在

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 10)
    private String country;

    @Column(length = 255)
    private String avatar;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude // 避免循环引用
    @EqualsAndHashCode.Exclude
    private Seller sellerProfile; // 用户的商家档案

    public Seller getSellerProfile() {
        return sellerProfile;
    }

    // 原来的单一角色字段可以移除或保留作他用，或者迁移到新的角色系统
    // @Column(columnDefinition = "TINYINT DEFAULT 0")
    // private Integer role = 0; // 这个字段现在由下面的 roles 集合替代

    @Column(columnDefinition = "TINYINT DEFAULT 0") // 性别: 0=保密, 1=男, 2=女
    private Integer gender = 0;

    @Column
    private LocalDate birthday;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_coupons",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "coupon_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Coupon> coupons = new HashSet<>();

    // --- 新增与 Role 实体的多对多关联 ---
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // 通常角色信息需要急切加载
    @JoinTable(
            name = "user_roles", // 中间连接表的名称
            joinColumns = @JoinColumn(name = "user_id"),       // User表在中间表的外键
            inverseJoinColumns = @JoinColumn(name = "role_id")  // Role表在中间表的外键
    )
    @ToString.Exclude // 避免与Role的潜在双向关联导致toString问题
    @EqualsAndHashCode.Exclude // 同上
    private Set<Role> roles = new HashSet<>(); // <--- 用户拥有的角色集合

    // 便捷方法来管理与Coupon的双向关系
    public void addCoupon(Coupon coupon) {
        this.coupons.add(coupon);
        if (coupon != null && coupon.getUsers() != null) { // 确保 coupon 和 coupon.getUsers() 不为 null
            coupon.getUsers().add(this);
        }
    }

    public void removeCoupon(Coupon coupon) {
        this.coupons.remove(coupon);
        if (coupon != null && coupon.getUsers() != null) { // 确保 coupon 和 coupon.getUsers() 不为 null
            coupon.getUsers().remove(this);
        }
    }

    // 便捷方法来管理与Role的双向关系 (可选)
    public void addRole(Role role) {
        this.roles.add(role);
        // 如果 Role 实体中也有 Set<User> users 并用 mappedBy="roles"，则不需要下面这行
        // role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        // 如果 Role 实体中也有 Set<User> users 并用 mappedBy="roles"，则不需要下面这行
        // role.getUsers().remove(this);
    }
}
