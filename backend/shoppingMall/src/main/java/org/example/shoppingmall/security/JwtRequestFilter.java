package org.example.shoppingmall.security; // 确保包名正确

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
// 注意：如果你使用的 io.jsonwebtoken 版本较新，SignatureException 可能在 io.jsonwebtoken.security.SignatureException
import io.jsonwebtoken.security.SignatureException; // 或者 io.jsonwebtoken.SignatureException，取决于你的库版本
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull; // 推荐对关键参数使用 @NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component // 确保只有一个JWT过滤器是 @Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil; // 确保这是你项目中统一的JWT工具类

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // 1. 检查 "Authorization" 请求头是否存在并且以 "Bearer " 开头
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);

            // 检查提取的令牌是否为空白
            if (jwtToken.isBlank()) {
                logger.error("收到的JWT令牌为空白字符串。路径: {}", request.getRequestURI());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "JWT令牌不能为空");
                return; // 终止请求
            }

            try {
                username = jwtUtil.extractUsername(jwtToken);
                logger.debug("从JWT Token中提取用户名: {}", username);

                // 2. 如果用户名存在且当前没有认证信息，则进行认证
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        logger.debug("用户 {} 通过JWT成功认证。权限: {}", username, userDetails.getAuthorities());
                    } else {
                        logger.warn("JWT Token对用户 {} 验证失败。", username);
                        // 可选：如果严格要求，即使是验证失败也应发送401并返回
                        // sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token验证失败");
                        // return;
                        SecurityContextHolder.clearContext(); // 清除可能存在的无效上下文
                    }
                }
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token已过期: {}, 路径: {}", e.getMessage(), request.getRequestURI());
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "JWT Token已过期，请重新登录");
                return; // 终止请求
            } catch (SignatureException e) {
                logger.error("JWT签名验证失败: {}, 路径: {}", e.getMessage(), request.getRequestURI());
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "无效的JWT签名");
                return; // 终止请求
            } catch (MalformedJwtException e) {
                logger.error("无效的JWT Token格式: {}, 路径: {}", e.getMessage(), request.getRequestURI());
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "无效的JWT Token格式");
                return; // 终止请求
            } catch (IllegalArgumentException e) { // 通常由jwtUtil内部逻辑问题导致
                logger.error("JWT参数错误或断言失败: {}, 路径: {}", e.getMessage(), request.getRequestURI());
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "无法解析JWT Token");
                return; // 终止请求
            } catch (UsernameNotFoundException e) {
                logger.warn("根据JWT中的用户名 '{}' 未找到用户, 路径: {}", username, request.getRequestURI());
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "用户不存在或认证信息无效");
                return; // 终止请求
            } catch (Exception e) { // 捕获所有其他潜在异常
                logger.error("处理JWT Token时发生未知错误: {}, 路径: {}", e.getMessage(), request.getRequestURI(), e);
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "认证处理错误");
                return; // 终止请求
            }
        } else {
            logger.debug("路径 {} 请求头中无JWT Token或格式不正确。", request.getRequestURI());
            // 对于没有提供Token的请求，我们不设置认证信息。
            // 后续的Spring Security规则（如 in HttpSecurity 或 @PreAuthorize）将决定是否允许访问。
        }

        // 3. 无论认证是否成功（只要没有提前返回错误），都继续过滤器链
        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8"); // 确保UTF-8编码

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", System.currentTimeMillis());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);
        // errorDetails.put("path", request.getRequestURI()); // 如果需要包含路径信息，需要将request传入

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}