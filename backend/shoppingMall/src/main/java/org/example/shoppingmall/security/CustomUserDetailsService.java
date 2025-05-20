package org.example.shoppingmall.security;

import org.example.shoppingmall.entity.User;
import org.example.shoppingmall.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // 用于权限列表

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        logger.debug("尝试加载用户: {}", principal);
        try {
            User user = userRepository.findByPrincipal(principal)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with principal: " + principal));
            
            logger.debug("用户加载成功: {}", principal);
            return new CustomUserDetails(user);
        } catch (UsernameNotFoundException e) {
            logger.error("找不到用户: {}", principal);
            throw e;
        } catch (Exception e) {
            logger.error("加载用户时出错: {}", e.getMessage());
            throw new UsernameNotFoundException("Error loading user: " + e.getMessage(), e);
        }
    }
}