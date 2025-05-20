package org.example.shoppingmall.util; // 确保包名正确

import org.example.shoppingmall.exception.UnauthorizedException;
import org.example.shoppingmall.security.CustomUserDetails; // ★★★ 导入你的 CustomUserDetails 类 ★★★
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            logger.debug("Attempting to get current user ID. Principal type: {}", (principal != null ? principal.getClass().getName() : "null"));

            if (principal instanceof CustomUserDetails) { // ★★★ 检查 principal 是否是 CustomUserDetails 的实例 ★★★
                CustomUserDetails customUserDetails = (CustomUserDetails) principal;
                Long userId = customUserDetails.getId(); // ★★★ 调用 CustomUserDetails 的 getId() 方法 ★★★
                logger.debug("Successfully retrieved userId: {} from CustomUserDetails.", userId);
                return userId;
            } else if (principal != null && "anonymousUser".equals(principal.toString())) {
                logger.warn("Current principal is 'anonymousUser'. No authenticated user ID available.");
            } else if (principal != null) {
                logger.warn("Principal is of an unexpected type: {}. Expected CustomUserDetails.", principal.getClass().getName());
            } else {
                logger.warn("Principal is null even though authentication is not null and is authenticated.");
            }
        } else {
            logger.warn("No authentication information found in SecurityContextHolder or user is not authenticated.");
        }

        // 如果以上条件都不满足，或者 principal 不是 CustomUserDetails 类型，则抛出异常
        throw new UnauthorizedException("User not authenticated or user ID could not be determined from principal.");
    }

    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }
}