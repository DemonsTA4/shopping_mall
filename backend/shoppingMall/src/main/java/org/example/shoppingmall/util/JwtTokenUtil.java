package org.example.shoppingmall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // jjwt 0.11.x+
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j // 添加 Slf4j 日志
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.default}") // 默认（短）有效期，单位：毫秒
    private Long defaultExpirationTimeMs;

    @Value("${jwt.expiration.rememberMe}") // “记住我”状态下的（长）有效期，单位：毫秒
    private Long rememberMeExpirationTimeMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes); // 根据密钥长度自动选择合适的 HMAC SHA 变体
    }

    /**
     * 从UserDetails生成JWT Token（支持“记住我”功能）。
     *
     * @param userDetails 用户信息
     * @param rememberMe  是否启用“记住我”以使用长有效期
     * @return 生成的JWT Token
     */
    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        // 将用户的角色/权限信息添加到claims中，键为 "roles"
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles); // 这样在解析Token时，如果需要，也可以获取角色信息

        return createToken(claims, userDetails.getUsername(), rememberMe);
    }

    /**
     * 从UserDetails生成JWT Token（默认使用短有效期）。
     *
     * @param userDetails 用户信息
     * @return 生成的JWT Token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, false); // 默认不“记住我”
    }

    /**
     * 创建JWT Token的私有辅助方法。
     */
    private String createToken(Map<String, Object> claims, String subject, boolean rememberMe) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long expirationTimeToUse = rememberMe ? rememberMeExpirationTimeMs : defaultExpirationTimeMs;
        Date expiryDate = new Date(nowMillis + expirationTimeToUse);

        log.debug("创建Token: subject={}, rememberMe={}, issueAt={}, expiresAt={}", subject, rememberMe, now, expiryDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // subject通常是用户名
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // 推荐使用HS512，如果密钥长度足够 (例如64字节的Base64编码密钥)
                // 如果您的密钥是为HS256准备的(例如32字节的Base64编码密钥)，请使用 SignatureAlgorithm.HS256
                .compact();
    }

    /**
     * 从JWT Token中提取用户名。
     *
     * @param token JWT Token
     * @return 用户名，如果Token无效或无法解析则返回null
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从JWT Token中提取过期时间。
     *
     * @param token JWT Token
     * @return 过期时间，如果Token无效或无法解析则返回null
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从JWT Token中提取指定的Claim。
     *
     * @param token          JWT Token
     * @param claimsResolver 解析Claim的函数
     * @return Claim的值，如果Token无效或无法解析则可能返回null (取决于claimsResolver)
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claimsResolver.apply(claims);
        }
        return null;
    }

    /**
     * 解析JWT Token以获取所有的Claims。
     *
     * @param token JWT Token
     * @return Claims对象，如果解析失败则返回null
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT签名验证失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims字符串为空或无效: {}", e.getMessage());
        } catch (Exception e) { // 捕获其他可能的解析异常
            log.error("解析JWT Token时发生未知错误: {}", e.getMessage(), e);
        }
        return null; // 如果有任何异常，返回null
    }

    /**
     * 检查JWT Token是否已过期。
     *
     * @param token JWT Token
     * @return 如果已过期或Token无效则返回true，否则返回false
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        // 如果expiration为null (例如token解析失败)，也视为已过期
        return expiration == null || expiration.before(new Date());
    }

    /**
     * 验证JWT Token是否有效（用户名匹配且未过期）。
     *
     * @param token       JWT Token
     * @param userDetails 用于验证的用户信息
     * @return 如果Token有效则返回true，否则返回false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("validateToken失败：userDetails为null");
            return false;
        }
        final String usernameFromToken = extractUsername(token);
        if (usernameFromToken == null) {
            log.warn("validateToken失败：无法从Token中提取用户名");
            return false;
        }
        return (usernameFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}