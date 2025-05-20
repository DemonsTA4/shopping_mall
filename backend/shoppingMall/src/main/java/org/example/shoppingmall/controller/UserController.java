package org.example.shoppingmall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.common.Result;
import org.example.shoppingmall.common.ResultCode;
import org.example.shoppingmall.dto.PasswordUpdateDTO;
import org.example.shoppingmall.dto.UserDTO;
import org.example.shoppingmall.dto.UserLoginDTO;
import org.example.shoppingmall.dto.UserRegisterDTO;
import org.example.shoppingmall.dto.UserUpdateDTO;
import org.example.shoppingmall.entity.Role;
import org.example.shoppingmall.entity.User;
import org.example.shoppingmall.security.CustomUserDetails;
import org.example.shoppingmall.service.UserService;
import org.example.shoppingmall.util.SecurityUtils; // Assuming this utility class exists
import org.example.shoppingmall.exception.UnauthorizedException; // Assuming this custom exception exists
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // 添加 Logger

    /**
     * Handles user registration requests.
     * @param registerDTO DTO containing registration information.
     * @return Result object монастырский UserDTO upon successful registration.
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        logger.info("====== UserController: Received registration request for username: {} ======", registerDTO.getUsername()); // 添加这行日志
        try {
            UserDTO createdUser = userService.register(registerDTO);
            logger.info("====== UserController: User registration successful for username: {} ======", registerDTO.getUsername());
            return Result.success(createdUser);
        } catch (Exception e) {
            logger.error("====== UserController: User registration failed for username: {} - Error: {} ======",
                    (registerDTO != null ? registerDTO.getUsername() : "UNKNOWN"), // 防御性编程，DTO 可能为 null
                    e.getMessage(), e);

            // 使用 ResultCode.ERROR
            return Result.error(ResultCode.ERROR);

            // 或者，如果您在 ResultCode 中定义了更具体的注册失败错误码，例如:
            // return Result.error(ResultCode.REGISTRATION_ERROR);
        }
    }

    /**
     * Handles user login requests.
     * @param loginDTO DTO containing login credentials.
     * @return Result object containing a JWT token upon successful login.
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid UserLoginDTO loginDTO) { // loginDTO 现在包含 rememberMe
        logger.info("UserController - Login request received for user: {}, rememberMe: {}", loginDTO.getUsername(), loginDTO.getRememberMe());

        // ★ 调用接收 UserLoginDTO 的 userService.login 方法 ★
        String token = userService.login(loginDTO);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDTO userDto = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User userEntity = userDetails.getUserEntity();
            userDto = convertUserToUserDTO(userEntity); // 确保 convertUserToUserDTO 存在且正确
            logger.info("UserController - 登录成功，为用户 {} 返回 UserDTO (包含role: {})", userDto.getUsername(), userDto.getRole());
        } else {
            logger.warn("UserController - 登录成功后，无法从 SecurityContextHolder 获取 CustomUserDetails 来构建 UserDTO");
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        if (userDto != null) {
            responseData.put("user", userDto);
        }

        return Result.success("登录成功", responseData);
    }


    /**
     * Retrieves information for the currently authenticated user.
     * @return Result object containing UserDTO of the current user.
     */
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo() {
        // The userService.getCurrentUser method is expected to retrieve user details
        // based on the current security context (e.g., from JWT token).
        return Result.success(userService.getCurrentUser());
    }

    /**
     * Handles requests to update user information.
     * @param updateDTO DTO containing the user information to be updated.
     * @return Result object containing the updated UserDTO.
     */
    @PutMapping("/update")
    public Result<UserDTO> updateUserInfo(@RequestBody @Valid UserUpdateDTO updateDTO) {
        // The userService.updateUserInfo method handles updating the current user's
        // information based on the provided DTO.
        return Result.success(userService.updateUserInfo(updateDTO));
    }

    /**
     * Handles requests to change the user's password.
     * @param passwordUpdateDTO DTO containing old and new password information.
     * @return Result object indicating success or failure.
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Valid PasswordUpdateDTO passwordUpdateDTO) {
        // The userService.updatePassword method handles password change logic,
        // including validating the old password.
        userService.updatePassword(passwordUpdateDTO);
        return Result.success(); // Typically no data is returned for a successful password change
    }

    /**
     * Handles requests to upload a user avatar.
     * @param file The MultipartFile رحمت الله عليه.
     * @return Result object containing the URL of the uploaded avatar.
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // The userService.uploadAvatar method handles saving the file
        // and returning its accessible URL.
        // IOException from file saving should be handled, possibly by a global exception handler
        // or within the service, converting it to a custom business exception.
        return Result.success(userService.uploadAvatar(file));
    }

    private UserDTO convertUserToUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        // 使用 BeanUtils 或手动复制属性
        org.springframework.beans.BeanUtils.copyProperties(user, dto, "password", "roles"); // 假设不返回密码和原始角色对象

        // 手动处理角色到 UserDTO.role (Integer) 的映射
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            boolean isSeller = user.getRoles().stream()
                    .anyMatch(roleEntity -> "ROLE_SELLER".equals(roleEntity.getName()));
            if (isSeller) {
                dto.setRole(1); // 商家
            } else {
                boolean isBuyer = user.getRoles().stream()
                        .anyMatch(roleEntity -> "ROLE_BUYER".equals(roleEntity.getName()));
                if (isBuyer) {
                    dto.setRole(0); // 假设前端买家用0 （之前讨论是2，这里统一为0，请根据前端实际情况调整）
                } else {
                    dto.setRole(0); // 其他默认为0
                }
            }
        } else {
            dto.setRole(0); // 无角色默认为0
        }
        return dto;
    }

}
