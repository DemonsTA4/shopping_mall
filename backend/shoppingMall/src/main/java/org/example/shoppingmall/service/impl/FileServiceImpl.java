package org.example.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shoppingmall.common.ResultCode;
import org.example.shoppingmall.config.FileUploadConfig; // 确保你有这个配置类或者直接使用 @Value
import org.example.shoppingmall.exception.ApiException;
import org.example.shoppingmall.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream; // 需要导入
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; // 如果用 Files.copy from InputStream
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileUploadConfig fileUploadConfig; // 假设这个类提供了 getUploadDir() 和 getUrlPrefix() 或类似方法

    // 现有的 uploadFile 方法 (处理 MultipartFile)
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ResultCode.VALIDATION_ERROR.getCode(), "上传的文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (!isValidFileType(extension)) {
            log.warn("尝试上传不支持的文件类型: {}", extension);
            throw new ApiException(ResultCode.VALIDATION_ERROR.getCode(), "不支持的文件类型: " + extension);
        }

        String newFilename = UUID.randomUUID().toString() + "." + extension;

        // 使用 FileUploadConfig 获取基础上传目录
        String baseUploadDir = fileUploadConfig.getUploadDir(); // 例如: ./uploads/images
        Path uploadPath = Paths.get(baseUploadDir, folder).toAbsolutePath().normalize(); // 例如: /abs/path/to/uploads/images/avatar

        try {
            Files.createDirectories(uploadPath); // 确保目录存在
            Path targetPath = uploadPath.resolve(newFilename);

            // 使用 try-with-resources确保 InputStream 关闭
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 构建并返回文件访问URL
            // 假设 FileUploadConfig 也提供了一个方法来获取URL的基础路径段
            // 或者直接硬编码，但更好的方式是通过配置
            // 例如: /static/images/avatar/filename.jpg
            String urlPathSegment = fileUploadConfig.getUrlPathSegment(); // 例如: /static/images 或 /uploads
            if (urlPathSegment == null) {
                urlPathSegment = "/uploads"; // 提供一个默认值以防万一
                log.warn("FileUploadConfig.urlPathSegment 未配置，使用默认值: /uploads");
            }

            // 确保 folder 和 newFilename 前面有斜杠，并且 pathSegment 后面没有斜杠 (如果它不是根路径)
            String normalizedFolder = folder == null || folder.isEmpty() ? "" : (folder.startsWith("/") ? folder : "/" + folder);
            String normalizedFilename = newFilename.startsWith("/") ? newFilename : "/" + newFilename;
            String finalUrlPathSegment = urlPathSegment.endsWith("/") ? urlPathSegment.substring(0, urlPathSegment.length() -1) : urlPathSegment;


            log.info("文件上传成功: {}, 存储路径: {}, 访问URL段: {}", originalFilename, targetPath, finalUrlPathSegment + normalizedFolder + normalizedFilename);
            return finalUrlPathSegment + normalizedFolder + normalizedFilename;

        } catch (IOException e) {
            log.error("文件上传失败: {}", originalFilename, e);
            throw new ApiException(ResultCode.UPLOAD_FAILED);
        }
    }

    // --- 新增的 uploadFile 方法 (处理 byte[]) ---
    @Override
    public String uploadFile(byte[] fileBytes, String filenameWithExtension, String folder) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new ApiException(ResultCode.VALIDATION_ERROR.getCode(), "上传的文件字节不能为空");
        }

        String extension = StringUtils.getFilenameExtension(filenameWithExtension);

        if (!isValidFileType(extension)) {
            log.warn("尝试上传不支持的文件类型 (字节数组): {}", extension);
            throw new ApiException(ResultCode.VALIDATION_ERROR.getCode(), "不支持的文件类型: " + extension);
        }

        // 这里可以直接使用传入的 filenameWithExtension，因为它应该已经是唯一的了
        // (在 UserService 中由 UUID 生成)
        String newFilename = filenameWithExtension;

        String baseUploadDir = fileUploadConfig.getUploadDir();
        Path uploadPath = Paths.get(baseUploadDir, folder).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath); // 确保目录存在
            Path targetPath = uploadPath.resolve(newFilename);
            Files.write(targetPath, fileBytes); // 直接写入字节数组

            // 构建并返回文件访问URL (与上面的方法逻辑一致)
            String urlPathSegment = fileUploadConfig.getUrlPathSegment();
            if (urlPathSegment == null) {
                urlPathSegment = "/uploads";
                log.warn("FileUploadConfig.urlPathSegment 未配置，使用默认值: /uploads");
            }
            String normalizedFolder = folder == null || folder.isEmpty() ? "" : (folder.startsWith("/") ? folder : "/" + folder);
            String normalizedFilename = newFilename.startsWith("/") ? newFilename : "/" + newFilename;
            String finalUrlPathSegment = urlPathSegment.endsWith("/") ? urlPathSegment.substring(0, urlPathSegment.length() -1) : urlPathSegment;

            log.info("字节数组文件上传成功, 文件名: {}, 存储路径: {}, 访问URL段: {}", newFilename, targetPath, finalUrlPathSegment + normalizedFolder + normalizedFilename);
            return finalUrlPathSegment + normalizedFolder + normalizedFilename;

        } catch (IOException e) {
            log.error("字节数组文件上传失败, 文件名: {}", newFilename, e);
            throw new ApiException(ResultCode.UPLOAD_FAILED);
        }
    }
    // --- 结束新增方法 ---


    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("尝试删除空文件URL");
            return false;
        }

        // 从URL中提取相对路径 (相对于URL前缀)
        // 例如，如果 fileUrl 是 /static/images/avatar/file.jpg
        // 并且 urlPathSegment 是 /static/images
        // 那么我们需要得到 avatar/file.jpg
        String urlPathSegment = fileUploadConfig.getUrlPathSegment();
        if (urlPathSegment == null) {
            urlPathSegment = "/uploads"; // 保持与uploadFile一致的默认逻辑
        }

        String relativePath;
        if (fileUrl.startsWith(urlPathSegment)) {
            relativePath = fileUrl.substring(urlPathSegment.length());
        } else {
            // 如果 URL 不包含预期的前缀，可能是一个外部URL或格式错误
            log.warn("文件URL {} 不包含预期的路径段 {}", fileUrl, urlPathSegment);
            // 根据你的策略，这里可以直接返回false或尝试基于其他规则解析
            // 为了安全，如果URL格式不符，最好不要尝试删除未知路径
            return false;
        }

        // 构建完整的文件系统路径
        Path path = Paths.get(fileUploadConfig.getUploadDir(), relativePath).toAbsolutePath().normalize();

        try {
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("文件删除成功: {}", path);
            } else {
                log.warn("尝试删除文件但文件不存在: {}", path);
            }
            return deleted;
        } catch (IOException e) {
            log.error("文件删除失败: {}", path, e);
            return false;
        }
    }

    private boolean isValidFileType(String extension) {
        if (extension == null) {
            return false;
        }
        // 允许的文件类型 (可以从配置中读取或硬编码)
        String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "webp"};
        for (String type : allowedTypes) {
            if (type.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}