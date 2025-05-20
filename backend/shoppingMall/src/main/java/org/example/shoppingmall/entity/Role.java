package org.example.shoppingmall.entity; // 请替换为您的实际包路径

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // 导入
import lombok.ToString;       // 导入

import java.util.Set; // 导入

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 角色ID，建议使用Long

    @Column(nullable = false, unique = true, length = 50)
    private String name; // 角色名称, 例如 "USER", "ADMIN", "SELLER"

    // 如果需要从Role实体反向查找拥有此角色的所有用户 (通常不常用，且可能导致性能问题)
    // @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    // @ToString.Exclude
    // @EqualsAndHashCode.Exclude
    // private Set<User> users;

    public Role(String name) {
        this.name = name;
    }
}
