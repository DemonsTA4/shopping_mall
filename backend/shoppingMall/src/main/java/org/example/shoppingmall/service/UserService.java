package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.*;
import org.example.shoppingmall.entity.User;import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册成功的用户信息
     */
    UserDTO register(UserRegisterDTO registerDTO);
    
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录成功的用户信息
     */
    String login(UserLoginDTO loginDTO);
    
    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    UserDTO getCurrentUser();
    
    /**
     * 更新用户信息
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    UserDTO updateUserInfo(UserUpdateDTO updateDTO);
    
    /**
     * 修改密码
     * @param passwordUpdateDTO 密码更新信息
     */
    void updatePassword(PasswordUpdateDTO passwordUpdateDTO);
    
        /**     * 上传头像     * @param file 头像文件     * @return 头像URL     */    String uploadAvatar(MultipartFile file);        /**     * 根据ID获取用户实体     * @param id 用户ID     * @return 用户实体     */    User getUserById(Long id);
}