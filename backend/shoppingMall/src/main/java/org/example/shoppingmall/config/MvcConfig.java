package org.example.shoppingmall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MvcConfig implements WebMvcConfigurer {

    private final FileUploadConfig fileUploadConfig; // 注入 FileUploadConfig

    // 通过构造函数注入
    public MvcConfig(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 从 FileUploadConfig 获取统一的配置值
        String configuredUrlPathSegment = fileUploadConfig.getUrlPathSegment();
        String configuredUploadDir = fileUploadConfig.getUploadDir();

        // 1. 构建浏览器访问的URL模式 (Path Pattern)
        String pathPattern = configuredUrlPathSegment;
        if (pathPattern == null || pathPattern.isEmpty()) { // 增加空值检查和默认值
            log.warn("file.url-path-segment 未配置或为空，使用默认值 '/static/uploads_fallback'");
            pathPattern = "/static/uploads_fallback"; // 提供一个明确的后备值
        }
        if (!pathPattern.startsWith("/")) {
            pathPattern = "/" + pathPattern;
        }
        if (pathPattern.length() > 1 && pathPattern.endsWith("/")) {
            pathPattern = pathPattern.substring(0, pathPattern.length() - 1);
        }
        pathPattern += "/**";

        // 2. 构建文件在服务器上的物理位置 (Resource Location)
        if (configuredUploadDir == null || configuredUploadDir.isEmpty()) { // 增加空值检查和默认值
            log.error("file.upload-dir 未配置或为空，无法配置静态资源物理位置！");
            // 在这种情况下，可能不应该添加资源处理器，或者使用一个非常明确的无效路径
            // 或者直接抛出配置异常
            return; // 或者抛异常
        }
        String absoluteUploadDir = Paths.get(configuredUploadDir).toAbsolutePath().normalize().toString();
        String resourceLocation = "file:" + absoluteUploadDir + (absoluteUploadDir.endsWith("/") ? "" : "/");

        log.info("MvcConfig - 配置静态资源处理器: URL模式 '{}' 将映射到物理位置 '{}'", pathPattern, resourceLocation);

        registry.addResourceHandler(pathPattern)
                .addResourceLocations(resourceLocation);
    }
}