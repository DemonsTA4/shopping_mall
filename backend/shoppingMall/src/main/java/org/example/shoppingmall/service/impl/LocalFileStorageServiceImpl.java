package org.example.shoppingmall.service.impl; // 请将包名替换为您的实际项目路径

import lombok.RequiredArgsConstructor; // 如果没有其他 final 字段通过构造函数注入，这个注解可以移除
import org.example.shoppingmall.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
// @RequiredArgsConstructor // 如果没有 final 字段需要构造函数注入，可以移除
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${file.upload-dir:./uploads/images}")
    private String uploadDirString;

    @Value("${file.upload-path-segment:/static/images}")
    private String uploadPathSegment;

    private Path fileStorageLocation;

    // 无参构造函数是必需的，如果类上有@RequiredArgsConstructor且没有其他构造函数，
    // 或者没有@RequiredArgsConstructor但有其他有参构造函数时，需要显式添加。
    // 由于这里没有 final 字段通过构造函数注入，@RequiredArgsConstructor 可以移除，
    // 或者保留它（如果将来会添加 final 字段）。为清晰起见，如果用不到，可以移除。
    public LocalFileStorageServiceImpl() {
        // 默认构造函数
    }

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDirString).toAbsolutePath().normalize();
            logger.info("文件存储位置初始化为: {}", this.fileStorageLocation);
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("无法创建文件上传目录。路径: {}", uploadDirString, ex);
            // 在实际应用中，这里抛出自定义异常或让应用启动失败可能更合适
            throw new RuntimeException("无法创建上传目录: " + uploadDirString, ex);
        }
    }

    private String getFileExtension(String originalFilename) {
        if (StringUtils.hasText(originalFilename)) {
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex >= 0) {
                return originalFilename.substring(lastDotIndex); // 包含点 "."
            }
        }
        return "";
    }

    /**
     * 实现接口中定义的 storeFile 方法。
     * 保存上传的文件并返回其可访问的URL。
     */
    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.warn("尝试存储一个空文件或null文件。");
            throw new IllegalArgumentException("上传的文件不能为空。");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        logger.debug("原始文件名: {}", originalFilename);

        // 校验文件名
        if (originalFilename == null || originalFilename.contains("..")) {
            logger.warn("尝试使用无效的文件名: {}", originalFilename);
            throw new IllegalArgumentException("文件名包含无效的路径序列: " + originalFilename);
        }

        // 生成唯一文件名
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        logger.debug("生成的唯一文件名: {}", uniqueFilename);

        try (InputStream inputStream = file.getInputStream()) {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("文件 {} 成功存储到路径: {}", uniqueFilename, targetLocation);
            return getFileUrl(uniqueFilename); // 返回构建好的URL
        } catch (IOException ex) {
            logger.error("无法存储文件 {}。请稍后再试。", uniqueFilename, ex);
            throw new IOException("无法存储文件 " + uniqueFilename + "。请稍后再试。", ex);
        }
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        if (!StringUtils.hasText(fileName)) {
            logger.warn("尝试删除文件名为空的文件。");
            throw new IllegalArgumentException("文件名不能为空。");
        }

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(targetLocation)) {
                Files.delete(targetLocation);
                logger.info("文件 {} 删除成功。", fileName);
            } else {
                logger.warn("尝试删除不存在的文件: {}", fileName);
                // 根据业务需求，文件不存在时可以选择不抛异常或抛特定异常
                // throw new FileNotFoundException("文件不存在，无法删除: " + fileName);
            }
        } catch (IOException ex) {
            logger.error("无法删除文件 {}: {}", fileName, ex.getMessage(), ex);
            throw new IOException("无法删除文件 " + fileName, ex);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }

        String cleanedUploadPathSegment = uploadPathSegment;
        if (!cleanedUploadPathSegment.startsWith("/")) {
            cleanedUploadPathSegment = "/" + cleanedUploadPathSegment;
        }
        if (cleanedUploadPathSegment.endsWith("/")) {
            cleanedUploadPathSegment = cleanedUploadPathSegment.substring(0, cleanedUploadPathSegment.length() - 1);
        }

        // contextPath 可能为空字符串，如果应用部署在根路径
        String fullPath = contextPath + cleanedUploadPathSegment + "/" + fileName;

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(fullPath)
                .toUriString();
    }
}
