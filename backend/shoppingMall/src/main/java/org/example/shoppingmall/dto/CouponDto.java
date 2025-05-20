package org.example.shoppingmall.dto; // 请替换为您的实际包路径

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.shoppingmall.entity.CouponType; // 导入CouponType枚举

import jakarta.validation.constraints.*; // 用于输入校验
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {

    private Integer id;

    @NotBlank(message = "优惠券名称不能为空")
    @Size(max = 100, message = "优惠券名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "优惠券码不能为空")
    @Size(max = 50, message = "优惠券码长度不能超过50个字符")
    private String code; // 优惠券码，用户兑换或系统发放时使用

    @Size(max = 255, message = "描述信息长度不能超过255个字符")
    private String description; // 描述

    @NotNull(message = "优惠券类型不能为空")
    private CouponType type; // 类型: PERCENTAGE, FIXED_AMOUNT

    @NotNull(message = "折扣值不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "折扣值必须大于0") // 对于固定金额或百分比的数值部分
    private BigDecimal discountValue; // 折扣值 (例如: 10.00 表示10元或10%)

    @NotNull(message = "最低购买金额不能为空")
    @DecimalMin(value = "0.0", message = "最低购买金额不能为负")
    private BigDecimal minPurchaseAmount; // 最低消费金额才能使用

    private BigDecimal maxDiscountAmount; // 最大可抵扣金额 (主要用于百分比折扣券)

    @NotNull(message = "生效时间不能为空")
    private LocalDateTime startTime; // 生效时间

    @NotNull(message = "失效时间不能为空")
    private LocalDateTime endTime; // 失效时间

    @NotNull(message = "总数量不能为空")
    @Min(value = 0, message = "总数量不能为负") // 0表示无限量，但在实现中通常用一个很大的数或null表示
    private Integer totalCount; // 发放总数量

    private Integer usedCount; // 已使用/已领取数量 (通常在创建DTO时不填写，由后端设置)

    private Boolean isActive; // 是否激活 (通常在创建DTO时不填写，由后端设置)

    // DTO 通常不直接包含 Set<User> users 这样的集合，以避免循环依赖和数据冗余
    // 用户与优惠券的关联通常通过 UserCoupon 中间表或 User 实体中的优惠券列表来体现
}