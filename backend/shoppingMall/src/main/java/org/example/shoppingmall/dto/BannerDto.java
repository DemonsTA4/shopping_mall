package org.example.shoppingmall.dto; // 请将包名替换为您的实际项目路径

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank; // 用于校验
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.URL; // 用于URL校验

import java.time.LocalDateTime;

/**
 * Banner (广告横幅/轮播图) 数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerDto {

    private Integer id; // Banner ID

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title; // Banner 标题 (例如，用于alt文本或管理后台显示)

    @NotBlank(message = "图片URL不能为空")
    @URL(message = "图片URL格式不正确")
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    private String imageUrl; // Banner 图片的URL

    @URL(message = "跳转链接URL格式不正确") // 跳转链接可以为空，但如果提供，则必须是有效URL
    @Size(max = 500, message = "跳转链接URL长度不能超过500个字符")
    private String linkUrl; // 点击Banner后跳转的链接URL (可选)

    @NotBlank(message = "展示位置不能为空")
    @Size(max = 50, message = "展示位置长度不能超过50个字符")
    private String position; // Banner 的展示位置 (例如: "HOME_TOP", "CATEGORY_SIDEBAR")

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能为负数")
    private Integer sortOrder = 0; // 排序值，用于控制显示顺序，默认为0

    private Boolean isActive = true; // Banner 是否激活/启用，默认为true

    @Size(max = 255, message = "描述信息长度不能超过255个字符")
    private String description; // 简短描述 (可选)

    private LocalDateTime startDate; // Banner 开始生效时间 (可选)

    private LocalDateTime endDate; // Banner 失效时间 (可选)

    // 以下字段通常在查询结果中由后端填充，创建/更新时不需要客户端提供
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 最后更新时间

    // 构造函数示例 (用于创建时，不包含ID和时间戳)
    public BannerDto(String title, String imageUrl, String linkUrl, String position, Integer sortOrder, Boolean isActive, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.position = position;
        this.sortOrder = sortOrder;
        this.isActive = isActive;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}