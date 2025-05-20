package org.example.shoppingmall.security;

import org.example.shoppingmall.entity.Role;
import org.example.shoppingmall.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final User user;
    private static final String SELLER_ROLE_NAME = "ROLE_SELLER";

    public CustomUserDetails(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
    }

    /**
     * 返回当前用户的ID。
     * 这是自定义的方法，方便在应用中获取用户ID。
     * @return 用户ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * 返回底层的 User 实体对象。
     * 如果需要 User 实体中的其他自定义属性，可以通过这个方法获取。
     * @return User 实体
     */
    public User getUserEntity() {
        return user;
    }

    /**
     * 返回分配给用户的权限集合。
     * Spring Security 会使用这些权限来进行授权决策。
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            // logger.warn("用户 {} 没有任何角色，将返回空权限集合。", getUsername()); // 可以添加日志
            return Set.of();
        }
        // logger.debug("为用户 {} 转换角色为权限:", getUsername()); // 可以添加日志
        return roles.stream()
                .map(role -> {
                    String roleName = role.getName(); // ★★★ 获取角色的名称，例如 "ROLE_BUYER", "ROLE_SELLER" ★★★
                    // logger.debug("  - 从Role实体获取的角色名: {}", roleName); // 可以添加日志
                    return new SimpleGrantedAuthority(roleName); // ★★★ 使用角色名称创建权限 ★★★
                })
                .collect(Collectors.toSet());
    }

    /**
     * 返回用于验证用户身份的密码。
     * @return 用户的密码（通常是哈希过的）
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 返回用于验证用户身份的用户名。
     * 在 Spring Security 的上下文中，这通常是登录时使用的唯一标识。
     * 可以是实际的用户名、邮箱或者其他唯一字段。
     * @return 用户名
     */
    @Override
    public String getUsername() {
        // 你可以选择返回 user.getUsername(), user.getEmail(), 或者其他在登录时用作 principal 的字段
        // 为了与 CustomUserDetailsService 中使用 principal 查询保持一致，
        // 通常这里返回 User 实体中作为主要登录标识的字段。
        // 假设你主要用 username 登录，或者 findByPrincipal 最常匹配到 username
        return user.getUsername();
    }

    /**
     * 指示用户的帐户是否已过期。过期的帐户无法进行身份验证。
     * @return 如果用户的帐户有效（即未过期），则为 true；如果不再有效（即已过期），则为 false
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // 默认为 true，你可以根据 User 实体的属性来决定
        // 例如: return user.getAccountExpirationDate() == null || user.getAccountExpirationDate().isAfter(LocalDateTime.now());
    }

    /**
     * 指示用户是被锁定还是未锁定。锁定的用户无法进行身份验证。
     * @return 如果用户未锁定，则为 true；如果用户已锁定，则为 false
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // 默认为 true，你可以根据 User 实体的属性来决定
        // 例如: return !user.isLocked();
    }

    /**
     * 指示用户的凭据（密码）是否已过期。过期的凭据会阻止身份验证。
     * @return 如果用户的凭据有效（即未过期），则为 true；如果不再有效（即已过期），则为 false
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 默认为 true，你可以根据 User 实体的属性来决定
        // 例如: return user.getCredentialsExpirationDate() == null || user.getCredentialsExpirationDate().isAfter(LocalDateTime.now());
    }

    /**
     * 指示用户是启用还是禁用。禁用的用户无法进行身份验证。
     * @return 如果用户已启用，则为 true；如果用户已禁用，则为 false
     */
    @Override
    public boolean isEnabled() {
        // 假设你的 User 实体有一个 isEnabled() 或 getEnabled() 方法
        // return user.isEnabled();
        return true; // 默认为 true，如果 User 实体没有 enable 状态，或者默认所有查到的用户都可用
    }

    // 为了调试方便，可以覆盖 equals 和 hashCode (可选)
    // 但主要确保它们在 UserDetails 层面的一致性，如果需要的话。
    // 通常，UserDetails 的比较是基于 username 的。

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        // UserDetails 通常基于 username 进行比较
        return getUsername() != null ? getUsername().equals(that.getUsername()) : that.getUsername() == null;
    }

    @Override
    public int hashCode() {
        return getUsername() != null ? getUsername().hashCode() : 0;
    }

    public Long getSellerId() {
        if (this.user == null) {
            return null;
        }
        // getAuthorities() 方法会返回基于 user.getRoles().getName() 的权限集合
        // 例如，如果 Role.name 是 "ROLE_SELLER", getAuthorities() 会包含一个 GrantedAuthority，其 getAuthority() 返回 "ROLE_SELLER"
        boolean isSeller = getAuthorities().stream()
                .anyMatch(grantedAuthority -> SELLER_ROLE_NAME.equals(grantedAuthority.getAuthority()));
        if (isSeller) {
            return this.user.getId(); // 如果是商家，返回用户的ID作为seller_id
        }
        return null; // 如果不是商家，返回null
    }
}