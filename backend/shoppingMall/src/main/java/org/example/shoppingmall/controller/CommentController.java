package org.example.shoppingmall.controller; // 请替换为您的实际 controller 包路径

import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.dto.CommentDto;
import org.example.shoppingmall.service.CommentService;
// 假设您有统一的返回结果类，例如 Result.java 或直接使用 ResponseEntity
// import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.security.CustomUserDetails; // 假设用于获取当前用户信息

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.access.prepost.PreAuthorize; // 如果需要方法级别的权限控制
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // 用于校验 CommentDto

import java.util.List;
import java.util.Map; // 用于简单的回复请求体

@RestController
@RequestMapping("/api") // API 的基础路径
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取指定商品的评论列表 (分页)
     * 这将处理 GET /api/products/{productId}/comments
     */
    @GetMapping("/products/{productId}/comments")
    public ResponseEntity<Page<CommentDto>> getProductComments(
            @PathVariable Integer productId, // 与 CommentService 接口参数类型保持一致
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) { // Spring Data JPA 自动处理分页参数
        Page<CommentDto> comments = commentService.getProductComments(productId, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * 创建新评论
     * 假设用户必须登录才能评论
     * 请求体中应包含 productId, rating, content 等
     */
    @PostMapping("/comments")
    // @PreAuthorize("isAuthenticated()") // 示例：要求用户已认证
    public ResponseEntity<CommentDto> createComment(
            @AuthenticationPrincipal CustomUserDetails currentUser, // 获取当前登录用户信息
            @Valid @RequestBody CommentDto commentDto) {

        if (currentUser == null) {
            // 根据您的安全配置，这里可能不会到达，因为 @PreAuthorize 或安全过滤器会先拦截
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // CommentService 的 createComment 需要 userId，从 currentUser 获取
        CommentDto createdComment = commentService.createComment(currentUser.getId(), commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * 获取指定评论的详情
     */
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable Integer commentId) {
        CommentDto comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    /**
     * 更新用户自己的评论
     * 用户只能更新自己的评论
     */
    @PutMapping("/comments/{commentId}")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> updateComment(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentDto commentDto) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CommentDto updatedComment = commentService.updateComment(currentUser.getId(), commentId, commentDto);
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * 用户删除自己的评论
     */
    @DeleteMapping("/comments/{commentId}")
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Integer commentId) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        commentService.deleteComment(currentUser.getId(), commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 卖家回复评论
     * 假设回复者 (例如卖家或管理员) 的ID通过认证信息获取或有特定权限
     */
    @PostMapping("/comments/{commentId}/reply")
    // @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')") // 示例：要求特定角色
    public ResponseEntity<CommentDto> replyToComment(
            @AuthenticationPrincipal CustomUserDetails currentUser, // 假设回复者也是一个User
            @PathVariable Integer commentId,
            @RequestBody Map<String, String> payload) { // 简单接收一个包含 "replyContent" 的 JSON

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String replyContent = payload.get("replyContent");
        if (replyContent == null || replyContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // 或者返回更具体的错误信息
        }

        // CommentService 的 replyToComment 需要 sellerId (Integer)
        // 假设 CustomUserDetails.getId() 返回 Long，需要转换或调整service层参数类型
        // 这里我们假设 currentUser.getId() 返回的就是 Integer 类型的 sellerId，或者您在service层处理类型
        // 如果currentUser.getId()是Long，而service需要Integer，需要进行转换和错误处理
        // Integer sellerId = currentUser.getId().intValue(); // 注意 Long to Integer 可能的精度损失，确保ID范围合适

        // 鉴于您Service层 replyToComment 的 sellerId 是 Integer，这里需要注意类型
        // 如果 CustomUserDetails.getId() 是 Long，您可能需要调整 Service 层接口
        // 或者，如果回复权限不是基于当前用户，而是基于请求中的某个特定 sellerId，那认证方式会不同
        // 这里暂时假设 currentUser.getId() 可以安全转为 Integer 或 Service层能处理Long
        Integer sellerIdAsInteger;
        try {
            sellerIdAsInteger = currentUser.getId().intValue(); // 这是一个潜在的类型问题，如果用户ID很大
        } catch (NumberFormatException e) {
            // 处理ID过大无法转为Integer的情况
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 或者更具体的错误
        }

        CommentDto repliedComment = commentService.replyToComment(sellerIdAsInteger, commentId, replyContent);
        return ResponseEntity.ok(repliedComment);
    }

    /**
     * 获取当前登录用户的所有评论 (分页)
     */
    @GetMapping("/users/me/comments") // 或者 /api/my-comments
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CommentDto>> getCurrentUserComments(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Page<CommentDto> comments = commentService.getUserComments(currentUser.getId(), pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * (可选) 获取指定用户的评论列表 (分页) - 如果需要公开查看他人评论
     */
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<Page<CommentDto>> getUserCommentsById(
            @PathVariable Long userId, // 与 CommentService 接口参数类型保持一致
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<CommentDto> comments = commentService.getUserComments(userId, pageable);
        return ResponseEntity.ok(comments);
    }


    // 其他 CommentService 中的方法也可以类似地暴露为API端点
    // 例如：getLatestProductComments, getProductAverageRating, hasUserCommentedOnOrderItem
    // 这里就不一一列举了，您可以根据需要添加。

}