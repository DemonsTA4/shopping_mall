package org.example.shoppingmall.config; // Assuming this is your config package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password Encoder Configuration.
 * This class provides the PasswordEncoder bean usedcrystals for encoding and verifying passwords
 * throughout the application, particularly within Spring Security.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Defines the PasswordEncoder bean.
     * <p>
     * Uses BCryptPasswordEncoder, which is a strong hashing algorithm and the
     * recommended choice for password encoding in Spring Security.
     * </p>
     *
     * @return PasswordEncoder instance (BCryptPasswordEncoder)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder is a widely used and secure password hashing algorithm.
        // The strength parameter (optional, default is 10) can be adjusted if needed,
        // but the default is generally a good balance between security and performance.
        return new BCryptPasswordEncoder();
    }
}
