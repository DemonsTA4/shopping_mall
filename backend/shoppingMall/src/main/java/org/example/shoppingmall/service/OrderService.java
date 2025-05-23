package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.OrderCreateRequestDto; // 如果创建订单用DTO
import org.example.shoppingmall.dto.OrderResponseDto;
import org.example.shoppingmall.dto.PaymentInitiationResponseDto;
import org.example.shoppingmall.dto.PaymentStatusDto;
import org.example.shoppingmall.entity.Order;
import org.example.shoppingmall.entity.OrderItem; // 假设创建时仍用此临时对象列表
import org.example.shoppingmall.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface OrderService {

    // 实际createOrder的参数items应该是临时的，包含productId和quantity的对象列表，或者DTO
    // 这里暂时保留 List<OrderItem> items，但service内部创建新的OrderItem持久化对象
    OrderResponseDto createOrder(Long userId, OrderCreateRequestDto orderRequestDto);

    OrderResponseDto getOrderDetails(Long id);

    Page<OrderResponseDto> getUserOrders(Long userId, Integer status, Pageable pageable);

    OrderResponseDto updateOrderStatus(String orderId, OrderStatus status);

    OrderResponseDto cancelOrder(String orderId, Long userId); // 添加userId进行权限校验

    OrderResponseDto getOrderDetailsByOrderNo(String orderNo, Long userId);

    PaymentInitiationResponseDto initiatePayment(Long orderId, Long userId, String paymentMethod);

    PaymentStatusDto getOrderPaymentStatus(Long orderId /*, Long userIdIfNeeded */);

    // getUserOrdersWithDetails 方法可以被上面的 getUserOrders(Long userId, Pageable pageable) 替代，
    // 因为 Repository 中的 findByBuyerIdOrderByCreatedAtDesc 已经通过 @EntityGraph 加载了详情。
    // 如果仍需保留，确保其实现逻辑和返回类型正确。
    // Page<Order> getUserOrdersWithDetails(Long userId, Pageable pageable); // 原方法，考虑是否还需要或修改返回DTO
}