package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 如果需要自定义查询
import org.springframework.data.repository.query.Param; // 如果自定义查询有参数
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> { // 假设 Category ID 是 Long

    /**
     * 根据名称查找分类。
     * @param name 分类名称
     * @return 包含分类的Optional对象
     */
    Optional<Category> findByName(String name);

    /**
     * 检查是否存在指定名称的分类。
     * @param name 分类名称
     * @return 如果存在则返回true，否则返回false
     */
    boolean existsByName(String name);

    /**
     * 根据状态查询分类列表。
     * 例如，可以传入 1 来获取所有启用的分类。
     * @param status 分类的状态 (例如: 1 代表启用, 0 代表禁用)
     * @return 符合状态的分类列表
     */
    List<Category> findByStatus(Integer status);

    /**
     * 根据状态查询分类列表，并按 'sort' 字段升序排序。
     * 例如，可以传入 1 来获取所有启用的分类，并按 sort 字段排序。
     * 注意：Category 实体中的排序字段名为 'sort'。
     * @param status 分类的状态 (例如: 1 代表启用)
     * @return 符合状态且已排序的分类列表
     */
    List<Category> findByStatusOrderBySortAsc(Integer status);

    // 如果您希望保持类似 "Active" 的语义，并且总是查询 status = 1 的情况，
    // 并且按 sort 升序，可以考虑使用 @Query 注解自定义一个方法：
    /**
     * 查询所有启用的分类 (status=1)，并按 'sort' 字段升序排序。
     * @return 启用的、已排序的分类列表
     */
    @Query("SELECT c FROM Category c WHERE c.status = 1 ORDER BY c.sort ASC")
    List<Category> findAllActiveOrderBySortAsc();


    // --- 以下是您提供的其他方法，其中一些可能也需要根据 Category 实体调整 ---
    // 如果 Category 实体没有 parentId 或 level 字段，以下派生查询会失败：
    // List<Category> findByParentIdIsNull();
    // List<Category> findByParentId(Long parentId);
    // List<Category> findByLevel(Integer level);

    // 这个自定义查询假设 Category 实体有 parentId 字段
    // @Query("SELECT c FROM Category c WHERE c.parentId IS NULL")
    // List<Category> findAllRootCategories();

    // 这个自定义查询也有问题：
    // 1. 假设 Category 实体有 parentId 字段。
    // 2. c.getIsActive = true 不是有效的JPQL语法，应该用 c.status = 1 (如果 status=1 代表 active)。
    // 3. ORDER BY c.sortOrder 假设字段名为 sortOrder，而实体中是 sort。
    // @Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.status = 1 ORDER BY c.sort ASC")
    // List<Category> findActiveSubcategoriesByParentId(@Param("parentId") Long parentId); // 假设 parentId 是 Long
}
