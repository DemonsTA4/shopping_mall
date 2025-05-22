package org.example.shoppingmall.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.shoppingmall.controller.OrderController;
import org.example.shoppingmall.dto.*;
// 假设 OrderCreateRequestDto 和 OrderItemCreateDto (用于createOrder的items参数) 已定义
// import org.example.shoppingmall.dto.OrderCreateRequestDto;
// import org.example.shoppingmall.dto.OrderItemCreateDto;
import org.example.shoppingmall.dto.OrderItemResponseDto;
import org.example.shoppingmall.dto.OrderCreateRequestDto;
import org.example.shoppingmall.entity.*;
import org.example.shoppingmall.exception.UnauthorizedException;
import org.example.shoppingmall.repository.OrderRepository;
import org.example.shoppingmall.repository.ProductRepository;
import org.example.shoppingmall.repository.UserRepository;
import org.example.shoppingmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils; // 用于检查字符串
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    // ... 您的 @Autowired 依赖注入 ...
    // 考虑注入 CartService 或 CartItemRepository 用于清空购物车等操作

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public OrderResponseDto createOrder(Long userId, OrderCreateRequestDto orderRequestDto) { // 接收 DTO
        logger.info("Service层接收到的 OrderCreateRequestDto: {}", orderRequestDto); // 确认DTO已传入
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在 (ID: " + userId + ")"));

        if (CollectionUtils.isEmpty(orderRequestDto.getItems())) { // 从DTO获取items
            throw new RuntimeException("订单项不能为空");
        }
        if (!StringUtils.hasText(orderRequestDto.getShippingAddress())) { // 从DTO获取shippingAddress
            throw new RuntimeException("收货地址不能为空 (来自Service校验)");
        }

        Order newOrder = new Order();
        newOrder.setBuyer(buyer);
        newOrder.setOrderNo(generateOrderNo());
        newOrder.setStatus(OrderStatus.PENDING_PAYMENT);

        // 从 DTO 设置所有相关字段
        newOrder.setShippingAddress(orderRequestDto.getShippingAddress());
        newOrder.setShippingAddressText(orderRequestDto.getShippingAddress()); // 如果也需要
        newOrder.setReceiverAddress(orderRequestDto.getShippingAddress());   // 如果也需要，或从DTO其他字段获取

        newOrder.setReceiverName(orderRequestDto.getReceiverName());
        newOrder.setReceiverPhone(orderRequestDto.getReceiverPhone());
        newOrder.setReceiverProvince(orderRequestDto.getReceiverProvince());
        newOrder.setReceiverCity(orderRequestDto.getReceiverCity());
        newOrder.setReceiverDistrict(orderRequestDto.getReceiverDistrict());
        newOrder.setReceiverPostalCode(orderRequestDto.getReceiverPostalCode()); // 这个在DTO中可以为null
        newOrder.setNote(orderRequestDto.getNotes());                         // 这个在DTO中可以为null
        if (orderRequestDto.getPaymentMethod() != null) { // 假设DTO中 paymentMethod 是 Integer
            newOrder.setPayType(orderRequestDto.getPaymentMethod());
        }

        BigDecimal totalAmountCalculated = BigDecimal.ZERO;
        for (OrderItemRequestDto itemDto : orderRequestDto.getItems()) {
            if (itemDto.getProductId() == null) { // 假设 OrderItemRequestDto 中有 getProductId()
                throw new RuntimeException("订单项中的商品ID缺失");
            }
            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) { // 假设有 getQuantity()
                throw new RuntimeException("订单项中的商品数量无效");
            }

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在 (ID: " + itemDto.getProductId() + ")"));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("商品 '" + product.getName() + "' 库存不足 (需求: " + itemDto.getQuantity() + ", 现有: " + product.getStock() + ")");
            }

            product.setStock(product.getStock() - itemDto.getQuantity()); // 使用 product.setStock()
            productRepository.save(product);

            OrderItem persistentOrderItem = new OrderItem();
            persistentOrderItem.setProduct(product);
            persistentOrderItem.setProductName(product.getName()); // 或从 itemDto 获取快照 (如果DTO有)
            persistentOrderItem.setProductImage(product.getImageUrl()); // 或从 itemDto 获取快照
            persistentOrderItem.setUnitPrice(product.getPrice()); // 或从 itemDto 获取快照
            persistentOrderItem.setQuantity(itemDto.getQuantity());
            persistentOrderItem.setProductSpecs(itemDto.getProductSpecs()); // 从 itemDto 获取
            persistentOrderItem.calculateSubtotal();

            totalAmountCalculated = totalAmountCalculated.add(persistentOrderItem.getSubtotal());
            newOrder.addItem(persistentOrderItem);
        }

        newOrder.setTotalAmount(totalAmountCalculated);
        newOrder.setFreightAmount(calculateFreight(newOrder));
        newOrder.setPayAmount(newOrder.getTotalAmount().add(newOrder.getFreightAmount()));

        Order savedOrder = orderRepository.save(newOrder);
        return convertToOrderResponseDto(savedOrder);
    }

    @Override
    public OrderResponseDto getOrderDetailsByOrderNo(String orderNo, Long userId) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在 (订单号: " + orderNo + ")"));
        // 权限校验
        if (order.getBuyer() == null || !order.getBuyer().getId().equals(userId)) {
            throw new UnauthorizedException("无权访问此订单");
        }
        return convertToOrderResponseDto(order);
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private BigDecimal calculateFreight(Order order) {
        // 示例运费计算逻辑，例如满额包邮，或根据地区计算
        if (order.getTotalAmount().compareTo(new BigDecimal("99")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("10.00"); // 默认10元运费
    }

    // private void clearCartItems(Long userId, List<OrderItem> orderedItems) {
    // 假设CartItem有userId和productId字段
    // for (OrderItem item : orderedItems) {
    // cartItemRepository.deleteByUserIdAndProductId(userId, item.getProduct().getId());
    // }
    // }


    @Override
    public OrderResponseDto getOrderDetails(Long id) {
        Order order = orderRepository.findById(id) // findById 已通过 @EntityGraph 加载关联
                .orElseThrow(() -> new RuntimeException("订单不存在 (ID: " + id + ")"));
        return convertToOrderResponseDto(order);
    }

    @Override
    public Page<OrderResponseDto> getUserOrders(Long userId, Integer status, Pageable pageable) {
        Page<Order> ordersPage;
        if (status != null && status != 0) { // 假设 status=0 或 null 代表查询全部
            // 将前端的数字状态转换为后端的 OrderStatus 枚举
            OrderStatus orderStatusEnum = mapIntegerToOrderStatus(status); // 您需要实现这个映射方法
            if (orderStatusEnum != null) {
                // 调用 Repository 中按用户ID、状态和分页查询的方法
                // 您需要在 OrderRepository 中定义 findByBuyerIdAndStatus 方法
                ordersPage = orderRepository.findByBuyerIdAndStatus(userId, orderStatusEnum, pageable);
            } else {
                // 如果状态无效，则查询该用户的所有订单（或抛出错误）
                ordersPage = orderRepository.findByBuyerIdOrderByCreatedAtDesc(userId, pageable); // 假设您有这个按用户ID查询所有订单的方法
            }
        } else {
            // 查询该用户的所有订单
            ordersPage = orderRepository.findByBuyerIdOrderByCreatedAtDesc(userId, pageable);
        }
        return ordersPage.map(this::convertToOrderResponseDto); // 假设有 DTO 转换方法
    }

    @Override
    public OrderResponseDto updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findByOrderNo(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在 (ID: " + orderId + ")"));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new RuntimeException("订单已终结 (完成/取消/已退款)，无法更改状态至 " + status);
        }

        // 示例：更严格的状态流转控制
        // switch (order.getStatus()) {
        //     case PENDING_PAYMENT:
        //         if (status != OrderStatus.AWAITING_SHIPMENT && status != OrderStatus.CANCELLED) {
        //             throw new RuntimeException("待付款订单只能变更为处理中或已取消");
        //         }
        //         break;
        //     // ... more cases
        // }

        order.setStatus(status);
        if (status == OrderStatus.AWAITING_SHIPMENT && order.getPayTime() == null) { // 假设 AWAITING_SHIPMENT 表示已付款
            order.setPayTime(LocalDateTime.now());
        } else if (status == OrderStatus.SHIPPED && order.getDeliveryTime() == null) {
            // 通常发货时还需要填写物流信息
            // if (!StringUtils.hasText(order.getDeliverySn()) || !StringUtils.hasText(order.getDeliveryCompany())) {
            //     throw new RuntimeException("发货前请填写物流公司和单号");
            // }
            order.setDeliveryTime(LocalDateTime.now());
        } else if (status == OrderStatus.COMPLETED && order.getReceiveTime() == null) { // 代表用户确认收货
            order.setReceiveTime(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToOrderResponseDto(updatedOrder);
    }

    @Override
    public OrderResponseDto cancelOrder(String orderId, Long userId) {
        Order order = orderRepository.findByOrderNoAndBuyerId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("订单不存在或无权操作 (OrderID: " + orderId + ")"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.AWAITING_SHIPMENT) {
            throw new RuntimeException("订单状态为 " + order.getStatus() + "，不符合取消条件");
        }

        if (!CollectionUtils.isEmpty(order.getItems())) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct(); // 已经是急加载的，或者再次查询确保最新
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        return convertToOrderResponseDto(cancelledOrder);
    }


    private OrderResponseDto convertToOrderResponseDto(Order order) {
        if (order == null) {
            return null;
        }
        OrderResponseDto dto = new OrderResponseDto();

        // ... (订单级别字段的映射 - id, orderNo, user, amounts, status, 等)
        // (这部分与之前完整代码响应中的保持一致)
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());

        User buyer = order.getBuyer();

        if (order.getStatus() != null) {
            dto.setStatus(order.getStatus().getCode()); // <--- 直接调用枚举的 getCode() 方法
        } else {
            dto.setStatus(0); // 或者您定义的代表null或未知状态的数字
        }

        if (buyer != null) {
            dto.setUserId(buyer.getId());
            dto.setUsername(buyer.getUsername());
        }

        dto.setTotalAmount(order.getTotalAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setFreightAmount(order.getFreightAmount());
        dto.setPayType(order.getPayType());
        dto.setConfirmStatus(order.getConfirmStatus());

        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverProvince(order.getReceiverProvince());
        dto.setReceiverCity(order.getReceiverCity());
        dto.setReceiverDistrict(order.getReceiverDistrict());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setReceiverPostalCode(order.getReceiverPostalCode());

        dto.setNote(order.getNote());

        dto.setDeliveryCompany(order.getDeliveryCompany());
        dto.setDeliverySn(order.getDeliverySn());

        dto.setPayTime(order.getPayTime());
        dto.setDeliveryTime(order.getDeliveryTime());
        dto.setReceiveTime(order.getReceiveTime());
        dto.setCommentTime(order.getCommentTime());
        dto.setCreateTime(order.getCreatedAt()); // DTO.createTime 映射到 Order.createdAt
        dto.setUpdateTime(order.getUpdatedAt()); // DTO.updateTime 映射到 Order.updatedAt


        // 更新映射 OrderItems 的部分：
        if (!CollectionUtils.isEmpty(order.getItems())) {
            List<OrderItemResponseDto> itemDtos = order.getItems().stream()
                    .map(item -> {
                        if (item == null) return null;
                        OrderItemResponseDto itemDto = new OrderItemResponseDto();

                        itemDto.setId(item.getId());

                        Product product = item.getProduct();
                        if (product != null) {
                            itemDto.setProductId(product.getId());
                        }
                        // 使用 OrderItem 中的快照数据
                        itemDto.setProductName(item.getProductName());
                        itemDto.setProductImage(item.getProductImage());
                        itemDto.setProductPrice(item.getUnitPrice()); // OrderItem 中的 unitPrice 对应 DTO 中的 productPrice
                        itemDto.setProductSpecs(item.getProductSpecs()); // 映射新增的规格字段

                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setTotalPrice(item.getSubtotal()); // OrderItem 中的 subtotal 对应 DTO 中的 totalPrice

                        return itemDto;
                    })
                    .filter(Objects::nonNull) // 过滤掉转换 item 为 null 时可能产生的 null 值
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        } else {
            dto.setItems(new ArrayList<>()); // 如果没有订单项，设置为空列表
        }
        return dto;
    }

    @Override
    public PaymentInitiationResponseDto initiatePayment(Long orderId, Long userId, String paymentMethod) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, userId) // 验证订单存在且属于该用户
                .orElseThrow(() -> new RuntimeException("订单不存在或无权操作 (ID: " + orderId + ")"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException("订单状态不是待付款，无法支付");
        }

        // --- 这里是调用实际支付网关或生成支付信息的逻辑 ---
        // 1. 根据 paymentMethod (如 "alipay", "wechat") 选择相应的支付渠道。
        // 2. 调用支付渠道的API，传递订单信息（如订单号 order.getOrderNo(), 金额 order.getPayAmount() 等）。
        // 3. 支付渠道API会返回支付凭证，例如支付宝或微信支付的二维码URL。
        // 4. 更新订单状态（例如，如果需要，可以先更新为“支付处理中”等，但这取决于您的流程）。
        // 5. 构建 PaymentInitiationResponseDto 并返回。

        // 示例：假设您有一个 PaymentGatewayService
        // String qrCodeUrl = paymentGatewayService.generatePaymentQrCode(order, paymentMethod);

        // 简化示例，实际应从支付服务获取
        String qrCodeUrl = "https://example.com/pay/qr?orderNo=" + order.getOrderNo() + "&method=" + paymentMethod;
        // order.setPayType(mapPaymentMethodToInteger(paymentMethod)); // 如果需要记录支付方式的数字代码
        // orderRepository.save(order); // 如果有状态或支付方式更新

        PaymentInitiationResponseDto responseDto = new PaymentInitiationResponseDto();
        responseDto.setQrUrl(qrCodeUrl);
        responseDto.setOrderNo(order.getOrderNo());

        return responseDto;
    }

    @Override
    public PaymentStatusDto getOrderPaymentStatus(Long orderId /*, Long userIdIfNeeded */) {
        // 1. (可选) 权限校验：检查当前用户是否有权查询此订单的支付状态

        // 2. 查询订单的支付状态
        //    这可能涉及到查询您自己的 Order 表中的支付状态字段，
        //    或者调用第三方支付API查询实际支付结果。
        //    这里是一个非常简化的示例：
        Order order = orderRepository.findById(orderId)
                .orElse(null); // 或者抛出异常

        if (order == null) {
            // 如果您在这里处理订单不存在的情况，Controller那边就可以简化
            throw new EntityNotFoundException("订单不存在 (ID: " + orderId + ")");
        }

        // 假设您的 Order 实体有一个直接表示支付是否成功的字段或状态
        PaymentStatusDto statusDto = new PaymentStatusDto();
        if (order.getStatus() == OrderStatus.PROCESSING || // 例如：已付款，处理中/等待发货
                order.getStatus() == OrderStatus.AWAITING_SHIPMENT ||
                order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.COMPLETED ||
                order.getStatus() == OrderStatus.AWAITING_REVIEW ) {
            statusDto.setStatus("paid"); // 假设这些都算作前端轮询可以停止的“已支付”状态
        } else if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            statusDto.setStatus("pending"); // 或 "unpaid"
        } else if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.FAILED) {
            statusDto.setStatus("failed"); // 或 "cancelled"
        } else {
            statusDto.setStatus("unknown"); // 其他未知状态
        }

        logger.info("订单 {} 的内部状态为: {}, 映射为支付状态: {}", orderId, order.getStatus(), statusDto.getStatus());
        return statusDto;
    }

    private OrderStatus mapIntegerToOrderStatus(Integer statusInt) {
        if (statusInt == null) return null;
        // 这个映射需要与您前端的定义一致
        // (0:全部 (已在Service逻辑中处理), 1:待付款, 2:待发货, 3:待收货, 4:待评价, 5:已完成, 6:已取消)
        switch (statusInt) {
            case 1: return OrderStatus.PENDING_PAYMENT;
            case 2: return OrderStatus.AWAITING_SHIPMENT; // 或 PROCESSING
            case 3: return OrderStatus.SHIPPED; // 或 DELIVERED
            case 4: // "待评价" 的逻辑可能是在订单完成后，这里需要您根据业务定义
                // 如果您的 OrderStatus 枚举有 AWAITING_REVIEW
                // return OrderStatus.AWAITING_REVIEW;
                // 否则，如果“待评价”是 COMPLETED 状态的一个后续动作，这里可能不直接按状态筛选
                // 或者您的 COMPLETED 状态就包含了待评价的含义
                return OrderStatus.COMPLETED; // 示例，需要您确认
            case 5: return OrderStatus.COMPLETED;
            case 6: return OrderStatus.CANCELLED;
            // ... 其他状态映射
            default: return null; // 或抛出无效状态异常
        }
    }

}