package org.example.shoppingmall.repository; // 确保包名与你的项目结构一致

import org.example.shoppingmall.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> { // 第二个泛型参数是 Role 实体 ID 的类型

    /**
     * 根据角色名称查找角色。
     * 角色名称应该是唯一的。
     *
     * @param name 角色名称 (例如 "ROLE_BUYER")
     * @return 包含角色的 Optional，如果找不到则为空
     */
    Optional<Role> findByName(String name);

    /**
     * 检查具有给定名称的角色是否存在。
     *
     * @param name 角色名称
     * @return 如果存在则为 true，否则为 false
     */
    boolean existsByName(String name);

    // 你可以根据需要添加其他自定义的查询方法
}