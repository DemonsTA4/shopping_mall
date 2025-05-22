package org.example.shoppingmall.controller;

import jakarta.validation.Valid;
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.dto.OrderCreateRequestDto;
import org.example.shoppingmall.dto.OrderItemResponseDto;
import org.example.shoppingmall.dto.OrderResponseDto;
import org.example.shoppingmall.dto.OrderPaymentRequestDto;
import org.example.shoppingmall.dto.PaymentInitiationResponseDto;
import org.example.shoppingmall.dto.PaymentStatusDto;
// Order entity is not directly used if service returns DTOs
// import org.example.shoppingmall.entity.Order;
import org.example.shoppingmall.entity.OrderItem; // Used for temporary list creation
import org.example.shoppingmall.entity.OrderStatus;
import org.example.shoppingmall.entity.User; // Used to get username for createOrder, can be avoided if DTO from service is complete
import org.example.shoppingmall.exception.UnauthorizedException;
import org.example.shoppingmall.service.OrderService;
import org.example.shoppingmall.service.ProductService;
import org.example.shoppingmall.service.UserService;
import org.example.shoppingmall.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    // 构造函数也可能需要调整，如果 ProductService 不再需要在这里注入
    public OrderController(OrderService orderService , ProductService productService, UserService userService ) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @PostMapping
    public Result<OrderResponseDto> createOrder(@Valid @RequestBody OrderCreateRequestDto orderRequest) {
        logger.info("进入 OrderController.createOrder 方法");
        logger.info("接收到的 OrderCreateRequestDto 对象: {}", orderRequest); // Lombok @Data 会生成 toString()
        logger.info("DTO 中的 shippingAddress 值: {}", orderRequest.getShippingAddress());
        logger.info("DTO 中的 items 是否为空: {}", orderRequest.getItems() == null ? "是" : "否，数量: " + orderRequest.getItems().size());
        logger.info("DTO 中的 notes 值: {}", orderRequest.getNotes());
        logger.info("DTO 中的 paymentMethod 值: {}", orderRequest.getPaymentMethod());
        if (orderRequest.getShippingAddress() == null) {
            logger.error("严重错误：shippingAddress 在 OrderCreateRequestDto 中为 null！");
            // 这里可以根据情况决定是否提前返回错误，或者让后续的 @NotBlank 生效
        }
        Long currentUserId = getCurrentUserId();
        // User currentUser = userService.getUserById(currentUserId); // Not strictly needed if OrderResponseDto from service is complete

        OrderResponseDto responseDto = orderService.createOrder(currentUserId, orderRequest);
        return Result.success(responseDto);
    }

    @GetMapping
    public Result<Page<OrderResponseDto>> getUserOrders(
            @RequestParam(defaultValue = "1") int page, // 1. 修改默认值为 "1"，更符合前端习惯
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortProperty, // 改个更明确的名字
            @RequestParam(defaultValue = "desc") String sortDirectionString,
            @RequestParam(required = false) Integer status) {

        Long currentUserId = getCurrentUserId();
        logger.info("Controller: getUserOrders - userId: {}, pageFromFrontend: {}, size: {}, status: {}, sort: {}, direction: {}",
                currentUserId, page, size, status, sortProperty, sortDirectionString);


        Sort.Direction direction = sortDirectionString.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortInfo = Sort.by(direction, sortProperty);

        // 2. 将前端1-based的页码转换为后端0-based的页码
        int pageForJpa = page > 0 ? page - 1 : 0; // 确保 page 大于 0 才减 1，否则从0开始

        Pageable pageable = PageRequest.of(pageForJpa, size, sortInfo);
        logger.info("Controller: Calling orderService.getUserOrders with pageable: pageNumber={}, pageSize={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());


        Page<OrderResponseDto> responsePage = orderService.getUserOrders(currentUserId, status, pageable);

        logger.info("Controller: Service returned responsePage - totalElements: {}, totalPages: {}, currentPage(0-indexed): {}, numberOfElements: {}",
                responsePage.getTotalElements(), responsePage.getTotalPages(), responsePage.getNumber(), responsePage.getNumberOfElements());
        if (responsePage.hasContent() && !responsePage.getContent().isEmpty()) { // 添加 !responsePage.getContent().isEmpty() 检查
            logger.info("Controller: First order in response content - OrderNo: {}, CreateTime: {}",
                    responsePage.getContent().get(0).getOrderNo(), responsePage.getContent().get(0).getCreateTime());
        } else {
            logger.info("Controller: Service returned no content for this page.");
        }

        return Result.success(responsePage);
    }

    @GetMapping("/id/{id}")
    public Result<OrderResponseDto> getOrderById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        OrderResponseDto dto = orderService.getOrderDetails(id); // 假设Service有此方法
        if (dto == null) {
            return Result.notFound("订单不存在");
        }
        if (!dto.getUserId().equals(currentUserId)) {
            return Result.forbidden("无权访问此订单");
        }
        return Result.success(dto);
    }

    @GetMapping("/{orderNo}")
    public Result<OrderResponseDto> getOrderByOrderNo(@PathVariable String orderNo) {
        Long currentUserId = getCurrentUserId();
        OrderResponseDto dto = orderService.getOrderDetailsByOrderNo(orderNo, currentUserId); // 假设Service有此方法
        if (dto == null) {
            return Result.notFound("订单不存在 (订单号: " + orderNo + ")");
        }
        if (dto.getUserId() == null || !dto.getUserId().equals(currentUserId)) {
            return Result.forbidden("无权访问此订单");
        }
        return Result.success(dto);
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderResponseDto> cancelOrder(@PathVariable String id) {
        Long currentUserId = getCurrentUserId();
        // OrderService.cancelOrder is expected to handle validation and return OrderResponseDto
        OrderResponseDto cancelledOrderDto = orderService.cancelOrder(id, currentUserId);
        return Result.success(cancelledOrderDto);
    }

    @PutMapping("/{id}/status")
    public Result<OrderResponseDto> updateOrderStatus(
            @PathVariable String id,
            @RequestParam("status") OrderStatus status) { // Spring can convert string to Enum automatically
        // Long currentUserId = getCurrentUserId(); // Uncomment if service needs userId for auth

        // OrderService.updateOrderStatus is expected to handle validation (if any, e.g. admin role)
        // and return OrderResponseDto.
        // If user-specific auth is needed for this operation and not handled by method security,
        // the service method might need the currentUserId.
        OrderResponseDto updatedOrderDto = orderService.updateOrderStatus(id, status);

        return Result.success(updatedOrderDto);
    }

    /**
     * 处理订单支付请求
     * @param id 订单的数字主键ID (从路径中获取)
     * @param paymentRequest 包含支付方式等信息的请求体
     * @return 包含支付二维码URL等信息的响应
     */
    @PostMapping("/{id}/pay") // <--- 匹配前端的 POST /api/orders/{id}/pay 请求
    public Result<PaymentInitiationResponseDto> initiatePayment(
            @PathVariable Long id,
            @RequestBody OrderPaymentRequestDto paymentRequest) { // <--- 从请求体获取支付方式

        Long currentUserId = getCurrentUserId(); // 获取当前用户ID，用于权限验证或记录

        // 调用Service层处理支付初始化逻辑
        // paymentMethod 从 paymentRequest.getPaymentMethod() 获取
        PaymentInitiationResponseDto paymentResponse = orderService.initiatePayment(id, currentUserId, paymentRequest.getPaymentMethod());
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        // 您需要在 OrderService 中创建 initiatePayment 方法

        return Result.success(paymentResponse);
    }

    // Helper method to get current user ID
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            // ★★★ 在这里添加日志，记录原始异常 e ★★★
            // 使用你的日志框架，例如 SLF4J:
            // import org.slf4j.Logger;
            // import org.slf4j.LoggerFactory;
            logger.warn("SecurityUtils.getCurrentUserId() failed. Original exception: ", e);

            // 或者简单打印到控制台 (不推荐用于生产环境)
            //System.err.println("SecurityUtils.getCurrentUserId() failed. Original exception type: " + e.getClass().getName() + ", message: " + e.getMessage());
            //e.printStackTrace(); // 打印完整的堆栈跟踪

            throw new UnauthorizedException("用户未登录或认证令牌无效");
        }
    }
}
