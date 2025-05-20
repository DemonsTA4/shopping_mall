package org.example.shoppingmall.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    /**
     * 保存文件并返回访问URL
     *
     * @param file 要保存的文件
     * @return 文件的访问URL
     */
    String storeFile(MultipartFile file) throws IOException;

    /**
     * 删除文件
     *
     * @param fileName 文件名
     */
    void deleteFile(String fileName) throws IOException;

    /**
     * 获取文件的完整访问URL
     *
     * @param fileName 文件名
     * @return 完整的访问URL
     */
    String getFileUrl(String fileName);
} 