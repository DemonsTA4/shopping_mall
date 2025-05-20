package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.Coupon;
import org.example.shoppingmall.entity.User; // 确保User实体已导入，如果需要直接关联查询
import org.springframework.data.domain.Page; // 如果有分页方法
import org.springframework.data.domain.Pageable; // 如果有分页方法
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // 确保导入Param
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> { // Coupon ID 是 Integer

    /**
     * 查找当前所有可用的优惠券。
     * 条件：激活状态，在有效期内，且未被领完/用完。
     * @param now 当前时间
     * @return 可用优惠券列表
     */
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
            "AND c.startTime <= :now " +
            "AND c.endTime >= :now " +
            "AND c.usedCount < c.totalCount")
    List<Coupon> findAvailableCoupons(@Param("now") LocalDateTime now);

    /**
     * 查找当前所有可用且满足指定订单金额的优惠券。
     * @param now 当前时间
     * @param amount 订单金额，用于比较优惠券的最低消费要求
     * @return 符合条件的可用优惠券列表
     */
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
            "AND c.startTime <= :now " +
            "AND c.endTime >= :now " +
            "AND c.usedCount < c.totalCount " +
            "AND c.minPurchaseAmount <= :amount") // <--- 修改: minAmount 改为 minPurchaseAmount
    List<Coupon> findAvailableCouponsByAmount(@Param("now") LocalDateTime now, @Param("amount") BigDecimal amount);

    /**
     * 查找指定用户当前可用的所有优惠券。
     * @param userId 用户ID (假设User实体的ID是Long)
     * @param now 当前时间
     * @return 用户可用的优惠券列表
     */
    @Query("SELECT c FROM Coupon c JOIN c.users u WHERE u.id = :userId " + // 假设Coupon实体有 Set<User> users; 关联
            "AND c.isActive = true " +
            "AND c.startTime <= :now " +
            "AND c.endTime >= :now")
    List<Coupon> findUserCoupons(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 查找指定用户当前可用且满足指定订单金额的优惠券。
     * @param userId 用户ID
     * @param now 当前时间
     * @param amount 订单金额
     * @return 用户符合条件的可用优惠券列表
     */
    @Query("SELECT c FROM Coupon c JOIN c.users u WHERE u.id = :userId " +
            "AND c.isActive = true " +
            "AND c.startTime <= :now " +
            "AND c.endTime >= :now " +
            "AND c.minPurchaseAmount <= :amount") // <--- 修改: minAmount 改为 minPurchaseAmount
    List<Coupon> findUserCouponsByAmount(@Param("userId") Long userId, @Param("now") LocalDateTime now, @Param("amount") BigDecimal amount);

    // 可以根据需要添加其他查询方法，例如：
    Optional<Coupon> findByCode(String code); // 根据优惠券码查找

    // 如果需要分页获取所有优惠券 (JpaRepository 已提供 findAll(Pageable pageable))
    // Page<Coupon> findAllByIsActiveTrue(Boolean isActive, Pageable pageable); // 查询特定激活状态的优惠券并分页
}
