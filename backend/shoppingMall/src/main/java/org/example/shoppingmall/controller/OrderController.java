package org.example.shoppingmall.controller;

import jakarta.validation.Valid;
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.dto.OrderCreateRequestDto;
import org.example.shoppingmall.dto.OrderItemResponseDto;
import org.example.shoppingmall.dto.OrderResponseDto;
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
    public OrderController(OrderService orderService, ProductService productService, UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @PostMapping
    public Result<OrderResponseDto> createOrder(@Valid @RequestBody OrderCreateRequestDto orderRequest) {
        Long currentUserId = getCurrentUserId();
        // User currentUser = userService.getUserById(currentUserId); // Not strictly needed if OrderResponseDto from service is complete

        // The temporaryOrderItems list is built here and passed to the service.
        // The service layer is responsible for creating persistent OrderItem entities.
        // This assumes OrderCreateRequestDto.ItemDto has getProductId(), getQuantity(), getProductSpecs().
        // And ProductService.getProductEntityById() returns a Product entity.
        List<OrderItem> temporaryOrderItems = orderRequest.getItems().stream()
                .map(itemDto -> {
                    OrderItem orderItem = new OrderItem();
                    // It's generally better if ProductService returns a Product DTO浅
                    // or if OrderService takes Product IDs and handles fetching.
                    // For now, assuming getProductEntityById returns the Product entity.
                    orderItem.setProduct(productService.getProductEntityById(itemDto.getProductId()));
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setProductSpecs(itemDto.getProductSpecs());
                    // Price and subtotal should be calculated and set in the service layer
                    // based on the actual product price at the time of order creation.
                    return orderItem;
                })
                .collect(Collectors.toList());

        // OrderService.createOrder is expected to return a fully populated OrderResponseDto
        OrderResponseDto responseDto = orderService.createOrder(currentUserId, temporaryOrderItems, orderRequest.getShippingAddress());

        // If the responseDto from service doesn't include username, and it's needed:
        // if (responseDto != null && responseDto.getUsername() == null) {
        //    User currentUser = userService.getUserById(currentUserId);
        //    responseDto.setUsername(currentUser.getUsername());
        // }

        return Result.success(responseDto);
    }

    @GetMapping
    public Result<Page<OrderResponseDto>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort, // Ensure Order entity has 'createdAt' field for sorting
            @RequestParam(defaultValue = "desc") String direction) {

        Long currentUserId = getCurrentUserId();

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // OrderService.getUserOrders is expected to return Page<OrderResponseDto>
        Page<OrderResponseDto> responsePage = orderService.getUserOrders(currentUserId, pageable);

        return Result.success(responsePage);
    }

    @GetMapping("/{id}")
    public Result<OrderResponseDto> getOrderById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        // OrderService.getOrderDetails is expected to return OrderResponseDto
        OrderResponseDto dto = orderService.getOrderDetails(id);

        if (dto == null) { // Service might throw EntityNotFoundException, or return null
            return Result.notFound("订单不存在");
        }

        // Validate that the current user is the owner of the order
        if (!dto.getUserId().equals(currentUserId)) {
            return Result.forbidden("无权访问此订单");
        }

        return Result.success(dto);
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderResponseDto> cancelOrder(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        // OrderService.cancelOrder is expected to handle validation and return OrderResponseDto
        OrderResponseDto cancelledOrderDto = orderService.cancelOrder(id, currentUserId);
        return Result.success(cancelledOrderDto);
    }

    @PutMapping("/{id}/status")
    public Result<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam("status") OrderStatus status) { // Spring can convert string to Enum automatically
        // Long currentUserId = getCurrentUserId(); // Uncomment if service needs userId for auth

        // OrderService.updateOrderStatus is expected to handle validation (if any, e.g. admin role)
        // and return OrderResponseDto.
        // If user-specific auth is needed for this operation and not handled by method security,
        // the service method might need the currentUserId.
        OrderResponseDto updatedOrderDto = orderService.updateOrderStatus(id, status);

        return Result.success(updatedOrderDto);
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
