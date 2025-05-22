package org.example.shoppingmall.entity; // 请替换为您的实际包路径

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString; // 导入ToString以处理双向关联
import lombok.EqualsAndHashCode; // 导入EqualsAndHashCode以处理双向关联

import jakarta.persistence.*; // 导入JPA注解
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 地址实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity // 启用JPA实体注解
@Table(name = "addresses") // 对应的数据库表名
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 地址ID

    /**
     * 关联到用户实体
     * 使用 @ManyToOne 表示多个地址可以属于一个用户
     */
    @ManyToOne(fetch = FetchType.LAZY) // LAZY表示延迟加载，提高性能
    @JoinColumn(name = "user_id", nullable = false) // 定义外键列
    @ToString.Exclude // 如果User实体也关联回Address，避免toString循环
    @EqualsAndHashCode.Exclude // 如果User实体也关联回Address，避免equals/hashCode循环
    private User user; // <--- 修改为直接关联 User 对象

    // private Long userId; // <--- 原来的 userId 字段可以移除或注释掉

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 50)
    private String province;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String district;

    @Column(name = "detailed_address", nullable = false, length = 255)
    private String detailedAddress;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "is_default", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDefault = false; // Lombok会生成 isDefault() (或getIsDefault()) 和 setDefault()

    @Column(length = 20)
    private String tag;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    // Lombok @Data 会为 User user 生成 getUser() 和 setUser(User user)
    // Lombok @Data 会为 Boolean isDefault 生成 isDefault()/getIsDefault() 和 setDefault(Boolean isDefault)

    // 如果需要，可以保留或调整之前的构造函数，确保它们与新的字段定义兼容
    // 例如，如果之前的构造函数使用了 userId (Long)，现在应该使用 User 对象
    public Address(User user, String receiverName, String receiverPhone, String province, String city, String district, String detailedAddress, Boolean isDefault, String tag) {
        this.user = user;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailedAddress = detailedAddress;
        this.isDefault = isDefault;
        this.tag = tag;
    }
}
