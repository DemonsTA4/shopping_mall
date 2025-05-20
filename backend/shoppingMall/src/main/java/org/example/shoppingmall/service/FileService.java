package org.example.shoppingmall.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件 (通过 MultipartFile)
     * @param file 文件
     * @param folder 存储文件夹
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 上传文件 (通过字节数组，例如从Base64解码后得到)
     * @param fileBytes 文件的字节数组
     * @param filenameWithExtension 包含扩展名的文件名 (例如你生成的新文件名)
     * @param folder 存储文件夹
     * @return 文件访问URL
     */
    String uploadFile(byte[] fileBytes, String filenameWithExtension, String folder); // 新增的方法

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}