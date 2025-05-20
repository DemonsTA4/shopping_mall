package org.example.shoppingmall.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.dto.CommentDto;
import org.example.shoppingmall.entity.Comment;
import org.example.shoppingmall.entity.OrderItem;
import org.example.shoppingmall.entity.Product;
import org.example.shoppingmall.entity.User;
import org.example.shoppingmall.repository.CommentRepository;
import org.example.shoppingmall.repository.OrderItemRepository;
import org.example.shoppingmall.repository.ProductRepository;
import org.example.shoppingmall.repository.UserRepository;
import org.example.shoppingmall.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils; // 导入 CollectionUtils
import org.springframework.util.StringUtils;   // 导入 StringUtils

import java.time.LocalDateTime;
import java.util.ArrayList; // 导入 ArrayList
import java.util.Arrays;    // 导入 Arrays
import java.util.List;
import java.util.Objects;   // 导入 Objects
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    // --- 统一的实体转DTO辅助方法 ---
    private CommentDto convertEntityToDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDto dto = new CommentDto();
        // 复制基本属性，排除需要特殊处理的关联对象和特殊字段
        BeanUtils.copyProperties(comment, dto, "user", "product", "orderItem", "images", "createdAt", "updatedAt", "visible");

        // 手动映射ID (假设实体ID是Long, DTO中ID是Integer，需要转换)
        // 如果ID类型一致（例如都是Long），则不需要转换
        if (comment.getId() != null) {
            dto.setId(comment.getId()); // 假设CommentDto.id是Integer
        }

        if (comment.getUser() != null) {
            if (comment.getUser().getId() != null) {
                dto.setUserId(comment.getUser().getId()); // User.id是Long, CommentDto.userId是Long
            }
            dto.setUsername(comment.getUser().getUsername());
            dto.setUserAvatar(comment.getUser().getAvatar());
        }
        if (comment.getProduct() != null) {
            if (comment.getProduct().getId() != null) {
                dto.setProductId(comment.getProduct().getId()); // Product.id是Long, CommentDto.productId是Integer
            }
            dto.setProductName(comment.getProduct().getName()); // 使用实体关联的Product信息
            dto.setProductImage(comment.getProduct().getImageUrl());
        }
        if (comment.getOrderItem() != null && comment.getOrderItem().getId() != null) {
            dto.setOrderItemId(comment.getOrderItem().getId()); // OrderItem.id是Long
        }

        // 处理 images: Comment.images (String) -> CommentDto.images (List<String>)
        if (StringUtils.hasText(comment.getImages())) {
            dto.setImages(Arrays.asList(comment.getImages().split(";"))); // 假设用分号分隔
        } else {
            dto.setImages(new ArrayList<>());
        }

        dto.setCreateTime(comment.getCreatedAt());
        dto.setUpdateTime(comment.getUpdatedAt());
        dto.setIsAnonymous(comment.getIsAnonymous());
        dto.setVisible(comment.getVisible()); // 设置DTO的visible字段

        // 处理回复信息
        dto.setReplyContent(comment.getReplyContent());
        dto.setReplyTime(comment.getReplyTime());
        // if (comment.getReplier() != null) { ... } // 如果有回复者实体

        return dto;
    }


    @Override
    @Transactional
    public CommentDto createComment(Long userId, CommentDto commentDto) { // userId 参数已是 Long
        User user = userRepository.findById(userId) // 直接使用 Long
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));

        Product product = productRepository.findById(commentDto.getProductId()) // DTO.productId是Integer, Product.id是Long
                .orElseThrow(() -> new EntityNotFoundException("商品不存在，ID: " + commentDto.getProductId()));

        OrderItem orderItem = null;
        if (commentDto.getOrderItemId() != null) { // DTO.orderItemId是Long
            orderItem = orderItemRepository.findById(commentDto.getOrderItemId())
                    .orElseThrow(() -> new EntityNotFoundException("订单项不存在，ID: " + commentDto.getOrderItemId()));

            // 假设 Order.Buyer.Id 是 Long
            if (!Objects.equals(orderItem.getOrder().getBuyer().getId(), userId)) {
                throw new AccessDeniedException("无权评论此订单项");
            }

            if (commentRepository.existsByOrderItem_Id(commentDto.getOrderItemId())) {
                throw new IllegalStateException("该订单项已经评论过了");
            }
        }

        Comment comment = new Comment();
        // BeanUtils.copyProperties(commentDto, comment); // 不再完全依赖，改为手动设置
        comment.setUser(user);
        comment.setProduct(product);
        if (orderItem != null) {
            comment.setOrderItem(orderItem);
        }
        comment.setRating(commentDto.getRating());
        comment.setContent(commentDto.getContent());

        // DTO.visible (Boolean) -> Entity.visible (Boolean)
        if (commentDto.getVisible() != null) {
            comment.setVisible(commentDto.getVisible());
        } else {
            comment.setVisible(true); // 默认可见
        }

        // DTO.images (List<String>) -> Entity.images (String)
        if (commentDto.getImages() != null && !commentDto.getImages().isEmpty()) {
            comment.setImages(String.join(";", commentDto.getImages())); // 正确调用 commentDto.getImages()
        } else {
            comment.setImages(null);
        }

        comment.setIsAnonymous(commentDto.getIsAnonymous() != null ? commentDto.getIsAnonymous() : false);

        Comment savedComment = commentRepository.save(comment);
        return convertEntityToDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Integer commentId, CommentDto commentDto) { // userId 参数已是 Long
        Comment comment = commentRepository.findById(commentId.longValue()) // commentId 参数是 Integer, Comment.id 是 Long
                .orElseThrow(() -> new EntityNotFoundException("评论不存在，ID: " + commentId));

        if (!Objects.equals(comment.getUser().getId(), userId)) {
            throw new AccessDeniedException("无权修改此评论");
        }

        comment.setRating(commentDto.getRating());
        comment.setContent(commentDto.getContent());

        // DTO.images (List<String>) -> Entity.images (String)
        if (commentDto.getImages() != null) {
            comment.setImages(String.join(";", commentDto.getImages())); // 正确调用 commentDto.getImages()
        } else {
            comment.setImages(null); // 如果希望清除图片
        }
        if (commentDto.getIsAnonymous() != null) {
            comment.setIsAnonymous(commentDto.getIsAnonymous());
        }
        if (commentDto.getVisible() != null) { // 允许更新visible状态
            comment.setVisible(commentDto.getVisible());
        }


        Comment updatedComment = commentRepository.save(comment);
        return convertEntityToDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Integer commentId) { // userId 参数已是 Long
        Comment comment = commentRepository.findById(commentId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("评论不存在，ID: " + commentId));

        if (!Objects.equals(comment.getUser().getId(), userId)) {
            // TODO: 管理员权限检查逻辑
            throw new AccessDeniedException("无权删除此评论");
        }
        commentRepository.deleteById(commentId.longValue());
    }

    @Override
    @Transactional
    public CommentDto replyToComment(Integer sellerId, Integer commentId, String replyContent) {
        // 假设 sellerId 对应 User.id (Long), 而参数是 Integer
        User seller = userRepository.findById(sellerId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("回复的卖家用户不存在, ID: " + sellerId));

        Comment comment = commentRepository.findById(commentId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("评论不存在，ID: " + commentId));

        // TODO: 实际的卖家权限验证逻辑 (例如，检查 comment.getProduct().getSeller().getId() 是否等于 sellerId)
        // Product product = comment.getProduct();
        // if (product.getSeller() == null || !Objects.equals(product.getSeller().getId(), seller.getId())) {
        //     throw new AccessDeniedException("无权回复此商品的评论");
        // }


        comment.setReplyContent(replyContent);
        comment.setReplyTime(LocalDateTime.now());
        // comment.setReplier(seller); // 如果Comment实体有 Replier 字段 (User类型)

        Comment updatedComment = commentRepository.save(comment);
        CommentDto dto = convertEntityToDto(updatedComment);
        // 如果 convertEntityToDto 没有处理回复者信息，在这里补充
        if (dto != null) { // 确保dto不为null
            dto.setReplierId(seller.getId()); // 假设DTO.replierId是Integer
            dto.setReplierName(seller.getUsername());
        }
        return dto;
    }

    @Override
    public CommentDto getCommentById(Integer commentId) {
        Comment comment = commentRepository.findById(commentId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("评论不存在，ID: " + commentId));
        return convertEntityToDto(comment);
    }

    @Override
    public Page<CommentDto> getProductComments(Integer productId, Pageable pageable) {
        if (!productRepository.existsById(productId.longValue())) {
            throw new EntityNotFoundException("商品不存在，ID: " + productId);
        }
        // 假设 CommentRepository 方法期望 Long productId
        return commentRepository.findByProduct_IdAndVisibleTrue(productId.longValue(), pageable)
                .map(this::convertEntityToDto);
    }

    @Override
    public Page<CommentDto> getUserComments(Long userId, Pageable pageable) { // userId 参数已是 Long
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("用户不存在，ID: " + userId);
        }
        // 假设 CommentRepository 方法期望 Long userId
        return commentRepository.findByUser_IdAndVisibleTrue(userId, pageable)
                .map(this::convertEntityToDto);
    }

    @Override
    public List<CommentDto> getLatestProductComments(Integer productId) {
        if (!productRepository.existsById(productId.longValue())) {
            throw new EntityNotFoundException("商品不存在，ID: " + productId);
        }
        // 假设 CommentRepository 方法期望 Long productId
        return commentRepository.findTop5ByProduct_IdAndVisibleTrueOrderByCreatedAtDesc(productId.longValue())
                .stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Double getProductAverageRating(Integer productId) {
        if (!productRepository.existsById(productId.longValue())) {
            throw new EntityNotFoundException("商品不存在，ID: " + productId);
        }
        // 假设 CommentRepository 方法期望 Long productId
        Double avgRating = commentRepository.getAverageRatingByProductId(productId.longValue());
        return avgRating == null ? 0.0 : avgRating;
    }

    @Override
    public boolean hasUserCommentedOnOrderItem(Long userId, Integer orderItemId) { // userId 参数已是 Long
        // 假设 OrderItem.id 是 Long, orderItemId 参数是 Integer
        OrderItem orderItem = orderItemRepository.findById(orderItemId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("订单项不存在，ID: " + orderItemId));

        if (!Objects.equals(orderItem.getOrder().getBuyer().getId(), userId)) {
            throw new AccessDeniedException("无权查询此订单项");
        }
        return commentRepository.existsByOrderItem_Id(orderItemId.longValue());
    }
}