package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, CommentDto commentDto);
    
    CommentDto updateComment(Long userId, Integer commentId, CommentDto commentDto);
    
    void deleteComment(Long userId, Integer commentId);
    
    CommentDto replyToComment(Integer sellerId, Integer commentId, String replyContent);
    
    CommentDto getCommentById(Integer commentId);
    
    Page<CommentDto> getProductComments(Integer productId, Pageable pageable);
    
    Page<CommentDto> getUserComments(Long userId, Pageable pageable);
    
    List<CommentDto> getLatestProductComments(Integer productId);
    
    Double getProductAverageRating(Integer productId);
    
    boolean hasUserCommentedOnOrderItem(Long userId, Integer orderItemId);
} 