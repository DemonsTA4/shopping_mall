package org.example.shoppingmall.dto; // 请将包名替换为您的实际项目路径

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;

    @NotNull(message = "商品ID不能为空")
    private Long productId;
    private String productName;
    private String productImage;

    private Long orderItemId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Long rating;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;

    private List<String> images; // Lombok会生成 getImages() 和 setImages()

    private Boolean isAnonymous = false;

    private Boolean visible = true; // 新增字段，用于控制评论是否立即可见，默认为true

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String replyContent;
    private LocalDateTime replyTime;
    private Long replierId;
    private String replierName;

    // 构造函数可以保持不变或按需调整
    public CommentDto(Long productId, Long orderItemId, Long rating, String content, List<String> images, Boolean isAnonymous, Boolean visible) {
        this.productId = productId;
        this.orderItemId = orderItemId;
        this.rating = rating;
        this.content = content;
        this.images = images;
        this.isAnonymous = isAnonymous;
        this.visible = visible; // 添加到构造函数
    }

    public CommentDto(Long rating, String content, List<String> images, Boolean isAnonymous, Boolean visible) {
        this.rating = rating;
        this.content = content;
        this.images = images;
        this.isAnonymous = isAnonymous;
        this.visible = visible; // 添加到构造函数
    }
    // 如果不希望在构造函数中包含所有字段，可以移除它们，依赖Lombok的AllArgsConstructor或手动创建需要的。
}