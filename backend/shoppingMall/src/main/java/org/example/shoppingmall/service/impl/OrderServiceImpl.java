package org.example.shoppingmall.service.impl;

import org.example.shoppingmall.dto.OrderItemResponseDto;
import org.example.shoppingmall.dto.OrderResponseDto;
// 假设 OrderCreateRequestDto 和 OrderItemCreateDto (用于createOrder的items参数) 已定义
// import org.example.shoppingmall.dto.OrderCreateRequestDto;
// import org.example.shoppingmall.dto.OrderItemCreateDto;
import org.example.shoppingmall.entity.*;
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
    public OrderResponseDto createOrder(Long userId, List<OrderItem> temporaryOrderItems, String shippingAddressFullText) {
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在 (ID: " + userId + ")"));

        if (CollectionUtils.isEmpty(temporaryOrderItems)) {
            throw new RuntimeException("订单项不能为空");
        }

        Order newOrder = new Order();
        newOrder.setBuyer(buyer);
        newOrder.setOrderNo(generateOrderNo());
        newOrder.setStatus(OrderStatus.PENDING_PAYMENT);
        newOrder.setShippingAddressText(shippingAddressFullText); // 保存原始完整地址

        // 实际应用中，结构化的收货地址信息应作为参数传入或从用户默认地址获取
        // 这里做简化处理，假设 shippingAddressFullText 包含了一些关键信息
        // 或者您可以在Controller层解析好再传入
        if (StringUtils.hasText(shippingAddressFullText)) {
            newOrder.setReceiverAddress(shippingAddressFullText); // 简化，实际应拆分
            // newOrder.setReceiverName(buyer.getNickname() != null ? buyer.getNickname() : buyer.getUsername()); // 示例
            // newOrder.setReceiverPhone(buyer.getPhone()); // 示例
        } else {
            throw new RuntimeException("收货地址不能为空");
        }


        BigDecimal totalAmountCalculated = BigDecimal.ZERO;
        List<OrderItem> persistentOrderItems = new ArrayList<>();

        for (OrderItem tempItemInfo : temporaryOrderItems) {
            if (tempItemInfo.getProduct() == null || tempItemInfo.getProduct().getId() == null) {
                throw new RuntimeException("订单项中的商品ID缺失");
            }
            if (tempItemInfo.getQuantity() == null || tempItemInfo.getQuantity() <= 0) {
                throw new RuntimeException("订单项中的商品数量无效");
            }

            Product product = productRepository.findById(tempItemInfo.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("商品不存在 (ID: " + tempItemInfo.getProduct().getId() + ")"));

            if (product.getStock() < tempItemInfo.getQuantity()) { // 使用 product.getStock()
                throw new RuntimeException("商品 '" + product.getName() + "' 库存不足 (需求: " + tempItemInfo.getQuantity() + ", 现有: " + product.getStock() + ")");
            }

            product.setStock(product.getStock() - tempItemInfo.getQuantity()); // 使用 product.setStock()
            productRepository.save(product);

            OrderItem persistentOrderItem = new OrderItem();
            persistentOrderItem.setProduct(product);
            persistentOrderItem.setProductName(product.getName());
            persistentOrderItem.setProductImage(product.getImageUrl());
            persistentOrderItem.setUnitPrice(product.getPrice());
            persistentOrderItem.setQuantity(tempItemInfo.getQuantity());
            persistentOrderItem.calculateSubtotal();

            totalAmountCalculated = totalAmountCalculated.add(persistentOrderItem.getSubtotal());
            newOrder.addItem(persistentOrderItem); // addItem 会设置 persistentOrderItem.setOrder(newOrder)
        }

        newOrder.setTotalAmount(totalAmountCalculated);
        // 根据业务设置其他金额，如运费、实际支付金额
        newOrder.setFreightAmount(calculateFreight(newOrder)); // 假设有运费计算逻辑
        newOrder.setPayAmount(newOrder.getTotalAmount().add(newOrder.getFreightAmount())); // 示例：支付金额 = 商品总额 + 运费

        Order savedOrder = orderRepository.save(newOrder);

        // 此处可以添加逻辑，例如：如果订单来源于购物车，清空购物车中已下单的商品
        // clearCartItems(userId, temporaryOrderItems);

        return convertToOrderResponseDto(savedOrder);
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
    public OrderResponseDto getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId) // findById 已通过 @EntityGraph 加载关联
                .orElseThrow(() -> new RuntimeException("订单不存在 (ID: " + orderId + ")"));
        return convertToOrderResponseDto(order);
    }

    @Override
    public Page<OrderResponseDto> getUserOrders(Long userId, Pageable pageable) {
        // 确保用户存在，如果 userRepository.findById 会抛异常则不需要这一步
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在 (ID: " + userId + ")"));

        Page<Order> ordersPage = orderRepository.findByBuyerIdOrderByCreatedAtDesc(userId, pageable);
        return ordersPage.map(this::convertToOrderResponseDto);
    }

    @Override
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
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
    public OrderResponseDto cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, userId)
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
        if (buyer != null) {
            dto.setUserId(buyer.getId());
            dto.setUsername(buyer.getUsername());
        }

        dto.setTotalAmount(order.getTotalAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setFreightAmount(order.getFreightAmount());
        dto.setPayType(order.getPayType());
        dto.setStatus(order.getStatus());
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
}