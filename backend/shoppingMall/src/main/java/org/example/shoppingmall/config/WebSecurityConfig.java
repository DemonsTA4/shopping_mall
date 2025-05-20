package org.example.shoppingmall.config;

import org.example.shoppingmall.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private JwtRequestFilter jwtAuthenticationFilter;

    // 在您的 SecurityConfig 或 WebSecurityConfig 中
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF
                .authorizeHttpRequests(authorize -> authorize
                        // --- 1. 静态资源放行 ---
                        .requestMatchers("/static/images/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/favicon.ico", "/error").permitAll()

                        // --- 2. 公开的 API 接口 ---
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                        .requestMatchers("/api/products", "/api/products/**").permitAll() // 商品列表和详情通常公开
                        .requestMatchers("/api/categories", "/api/categories/**").permitAll()
                        .requestMatchers("/api/banners", "/api/banners/**").permitAll()
                        .requestMatchers( // Swagger UI
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // --- 3. 需要认证的 API 接口 ---
                        .requestMatchers("/api/user/**").authenticated() // 例如获取用户信息、修改密码等
                        .requestMatchers("/api/cart/**").authenticated() // 购物车相关操作

                        // ★★★ 新增：收藏相关接口需要认证 ★★★
                        .requestMatchers(HttpMethod.POST, "/api/favorites/{productId}").authenticated()    // 添加收藏
                        .requestMatchers(HttpMethod.DELETE, "/api/favorites/{productId}").authenticated() // 取消收藏
                        .requestMatchers(HttpMethod.GET, "/api/favorites/{productId}/status").authenticated() // 检查收藏状态
                        .requestMatchers(HttpMethod.GET, "/api/favorites").authenticated()                 // 获取用户收藏列表
                        // 如果您将 getProductFavoriteCount 放在 ProductController 并希望它公开：
                        // .requestMatchers(HttpMethod.GET, "/api/products/{productId}/favorite-count").permitAll()
                        // 如果也需要认证：
                        // .requestMatchers(HttpMethod.GET, "/api/products/{productId}/favorite-count").authenticated()


                        // --- 4. 其他所有未明确配置的请求，默认需要认证 ---
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话管理
                );

        // 添加 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 确保 jwtAuthenticationFilter 是您正确的JWT过滤器Bean

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}