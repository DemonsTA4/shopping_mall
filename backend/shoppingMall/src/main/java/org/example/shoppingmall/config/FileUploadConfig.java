package org.example.shoppingmall.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j; // Import Slf4j for logging
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.MultipartConfigElement;
import java.io.File;
import java.nio.file.Paths;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
@Slf4j // Add this annotation for Lombok to create the 'log' instance
public class FileUploadConfig {

    // Default value will be used if 'file.upload-dir' is not in application.properties
    private String uploadDir = "./uploads"; // e.g., ./uploads or /var/www/myapp/uploads

    // Default value will be used if 'file.url-path-segment' is not in application.properties
    // This should match the segment used in MvcConfig for resource handling
    private String urlPathSegment = "/static/images"; // e.g., /static/images or /uploads_public



    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // Values from application.properties will override these if spring.servlet.multipart.* is set
        // However, it's common to define them here if not using the spring.servlet.multipart.* properties
        // Or, if spring.servlet.multipart.* is used, this bean might not be strictly necessary
        // unless further customization of MultipartConfigElement is needed beyond what properties offer.
        // For now, assuming these are the desired limits if not specified by spring.servlet.multipart.*
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(100)); // Changed from 20MB to 100MB to match your properties
        return factory.createMultipartConfig();
    }

    // StandardServletMultipartResolver is typically auto-configured by Spring Boot.
    // This bean definition is usually not needed unless you need to customize the resolver.
    // @Bean
    // public StandardServletMultipartResolver multipartResolver() {
    //     return new StandardServletMultipartResolver();
    // }
}
