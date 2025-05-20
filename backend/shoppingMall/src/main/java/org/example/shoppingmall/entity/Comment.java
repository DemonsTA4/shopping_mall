package org.example.shoppingmall.entity; // 请将包名替换为您的实际项目路径

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
// 假设 Product 和 User 实体类已存在于此包或已正确导入
// import org.example.shoppingmall.entity.Product;
// import org.example.shoppingmall.entity.User;
// import org.example.shoppingmall.entity.OrderItem;


@Data // Lombok: 自动生成 getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: 自动生成无参构造函数
@AllArgsConstructor // Lombok: 自动生成全参构造函数
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_product_user", columnList = "product_id, user_id"),
        @Index(name = "idx_comment_order_item", columnList = "order_item_id", unique = true) // 一个订单项通常只允许一条评论
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 评论ID，统一使用Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user; // 发表评论的用户实体

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product; // 被评论的商品实体

    @OneToOne(fetch = FetchType.LAZY) // 一个订单项对应一条评论
    @JoinColumn(name = "order_item_id", unique = true) // 确保唯一性，如果评论必须基于订单项
    @ToString.Exclude
    private OrderItem orderItem; // 关联的订单项 (可选, 但根据您的 ServiceImpl 逻辑是需要的)

    @Column(nullable = false)
    private Long rating; // 评分 (例如: 1-5 星)

    @Lob // 对于较长的文本内容
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 评论内容

    /**
     * 评论图片 (存储多张图片的URL, 以特定分隔符分隔的字符串或JSON字符串)
     * 如果是JSON字符串，查询和展示时需要处理。
     * 如果希望更规范地存储多图，可以使用 @ElementCollection 或创建 CommentImage 实体建立一对多关系。
     * 为简单起见，这里仍用 String，具体实现时需注意转换。
     */
    @Column(name = "images", length = 1000)
    private String images;

    @Column(name = "is_anonymous", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAnonymous = false; // 是否匿名评论

    @Column(name = "is_visible", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean visible = true; // 评论是否可见/已审核通过 (代替之前的status字段)

    // --- 卖家回复相关字段 ---
    @Lob
    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent; // 卖家回复内容

    @Column(name = "reply_time")
    private LocalDateTime replyTime; // 卖家回复时间

    // 假设回复者是User实体中的某个特定角色的用户，例如卖家或管理员
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "replier_id")
    // @ToString.Exclude
    // private User replier; // 回复者实体 (可选，如果需要记录回复者信息)


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Lombok 的 @Data 注解会自动为您生成以下方法:
    // getId(), setId()
    // getUser(), setUser(User user)
    // getProduct(), setProduct(Product product)
    // getOrderItem(), setOrderItem(OrderItem orderItem)
    // getRating(), setRating(Integer rating)
    // getContent(), setContent(String content)
    // getImages(), setImages(String images)
    // getIsAnonymous(), setIsAnonymous(Boolean isAnonymous) (或 isAnonymous() / setAnonymous())
    // isVisible(), setVisible(Boolean visible) (Lombok 对 isXxx 的布尔字段的 getter 通常是 isXxx())
    // getReplyContent(), setReplyContent(String replyContent)
    // getReplyTime(), setReplyTime(LocalDateTime replyTime)
    // getCreatedAt(), setCreatedAt()
    // getUpdatedAt(), setUpdatedAt()
}