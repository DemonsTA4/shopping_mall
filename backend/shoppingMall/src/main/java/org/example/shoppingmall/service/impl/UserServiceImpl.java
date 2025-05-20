package org.example.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shoppingmall.common.ResultCode;
import org.example.shoppingmall.dto.PasswordUpdateDTO; // 假设这个DTO已定义
import org.example.shoppingmall.dto.UserDTO;
import org.example.shoppingmall.dto.UserLoginDTO;
import org.example.shoppingmall.dto.UserRegisterDTO;
import org.example.shoppingmall.dto.UserUpdateDTO;   // 假设这个DTO已定义
import org.example.shoppingmall.entity.User;
import org.example.shoppingmall.entity.Role; // 引入 Role 实体
import org.example.shoppingmall.exception.ApiException;
import org.example.shoppingmall.repository.RoleRepository; // 引入 RoleRepository
import org.example.shoppingmall.repository.UserRepository;
import org.example.shoppingmall.service.FileService;
import org.example.shoppingmall.service.UserService;
import org.example.shoppingmall.util.JwtTokenUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // 假设您在uploadAvatar中使用

import java.util.Base64; // 从您的 updateUserInfo 方法中看到
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;    // 从您的 updateUserInfo 方法中看到

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    //private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final FileService fileService;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDTO register(UserRegisterDTO registerDTO) {
        log.info("UserServiceImpl - 注册请求，用户名: {}, DTO中的角色代码: {}", registerDTO.getUsername(), registerDTO.getRole());

        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new ApiException(ResultCode.USER_EXISTS);
        }
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new ApiException("邮箱已被注册");
        }

        User user = new User();
        // 从 DTO 复制基本属性。注意：如果 User 实体中也有一个名为 role 的 Integer 字段，
        // BeanUtils 可能会尝试复制它，但我们希望通过 Role 实体来管理角色，所以后续会覆盖或设置 roles 集合。
        // 最好是 User 实体中没有名为 role 的 Integer 字段，而是只有 roles 集合。
        // 为清晰起见，我们手动复制非角色字段。
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setCountry(registerDTO.getCountry());
        // 根据 UserRegisterDTO 的定义，它没有 nickname, avatar 等字段，所以这里不需要复制。
        // User 实体中这些字段在创建时可以是 null，或有数据库默认值，或后续更新。

        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(registerDTO.getUsername()); // 默认昵称与用户名相同
        }
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        // user.setStatus(1); // 可以在这里设置用户默认状态，例如：1代表激活

        // --- 处理角色 ---
        Integer roleCodeFromDTO = registerDTO.getRole();
        String targetRoleName;

        if (roleCodeFromDTO != null && roleCodeFromDTO == 1) { // 1 代表卖家
            targetRoleName = "ROLE_SELLER"; // 与您数据库中定义的卖家角色名一致
            log.info("为用户 {} 分配卖家角色 (ROLE_SELLER)", registerDTO.getUsername());
        } else { // 0 或其他情况 (包括null，因为DTO中role默认是0) 默认为买家
            targetRoleName = "ROLE_BUYER";  // 与您数据库中定义的买家角色名一致
            log.info("为用户 {} 分配买家角色 (ROLE_BUYER)", registerDTO.getUsername());
        }

        Role userRoleEntity = roleRepository.findByName(targetRoleName)
                .orElseGet(() -> {
                    log.warn("角色 '{}' 在数据库中不存在，将创建新角色。", targetRoleName);
                    Role newRole = new Role(targetRoleName); // 假设 Role 实体有这样的构造函数
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRoleEntity);
        user.setRoles(roles); // 确保 User 实体有 setRoles(Set<Role> roles) 方法

        user = userRepository.save(user);
        log.info("用户 {} (ID: {}) 已成功保存到数据库，角色为: {}", user.getUsername(), user.getId(), targetRoleName);

        // 将保存后的 User 实体转换回 UserDTO 以返回给前端
        return convertUserToUserDTO(user); // 使用下面定义的转换方法
    }
    private UserDTO convertUserToUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        // 使用 BeanUtils 复制大部分兼容的属性
        // 注意：BeanUtils.copyProperties(source, target, "ignoredProperty1", "ignoredProperty2");
        // 如果 User 实体中没有名为 role 的 Integer 字段，那么 roles 集合不会被复制到 dto.role
        // 所以我们在这里不复制 roles，后续手动处理
        BeanUtils.copyProperties(user, dto, "roles", "password"); // 不复制角色集合和密码

        // 手动处理角色转换
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            // 假设用户只有一个主要角色，或者我们取第一个角色来决定DTO中的role代码
            Role primaryRole = user.getRoles().iterator().next();
            String roleName = primaryRole.getName(); // 获取角色名，例如 "ROLE_SELLER"

            if ("ROLE_SELLER".equalsIgnoreCase(roleName)) {
                dto.setRole(1); // 1 代表卖家
            } else if ("ROLE_BUYER".equalsIgnoreCase(roleName)) {
                dto.setRole(0); // 0 代表买家
            } else {
                // 对于其他角色（如ROLE_ADMIN），如果DTO中没有特定代码，可以设为默认或特定值
                log.warn("用户 {} 的角色 {} 没有在DTO中明确映射，默认为0 (买家)", user.getUsername(), roleName);
                dto.setRole(0); // 默认为买家
            }
        } else {
            // 如果用户没有任何角色，在DTO中默认为买家
            log.warn("用户 {} 没有任何角色，DTO中角色将默认为0 (买家)", user.getUsername());
            dto.setRole(0);
        }
        // 确保 UserDTO 中有 setRole(Integer role) 方法

        return dto;
    }

    @Override
    public String login(UserLoginDTO loginDTO) { // ★ 确保参数是 UserLoginDTO ★
        try {
            log.info("UserServiceImpl - 尝试为用户 {} 登录，记住我: {}", loginDTO.getUsername(), loginDTO.getRememberMe());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 从 DTO 获取 rememberMe 标志，如果为null则默认为false
            boolean rememberMeFlag = loginDTO.getRememberMe() != null && loginDTO.getRememberMe();

            log.info("UserServiceImpl - 为用户 {} 生成Token，rememberMe标志: {}", userDetails.getUsername(), rememberMeFlag);
            return jwtTokenUtil.generateToken(userDetails, rememberMeFlag); // ★ 传递 rememberMeFlag ★

        } catch (AuthenticationException e) {
            log.warn("UserServiceImpl - 用户 {} 登录认证失败: {}", loginDTO.getUsername(), e.getMessage());
            throw new ApiException(ResultCode.LOGIN_ERROR);
        } catch (Exception e) {
            log.error("UserServiceImpl - 登录过程中发生意外错误 for user {}:", loginDTO.getUsername(), e);
            throw new ApiException(ResultCode.ERROR, "登录服务暂时不可用，请稍后再试");
        }
    }

    @Override
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            log.warn("Attempt to get current user without authentication.");
            throw new ApiException(ResultCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ResultCode.USER_NOT_EXISTS, "用户 " + username + " 不存在"));

        // ★★★ 调用正确的转换方法 ★★★
        return convertUserToUserDTO(user);
    }

    @Override
    @Transactional // 非常重要，因为涉及到文件操作和数据库操作
    public UserDTO updateUserInfo(UserUpdateDTO updateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            log.warn("Attempt to update user info without authentication.");
            throw new ApiException(ResultCode.UNAUTHORIZED);
        }
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ResultCode.USER_NOT_EXISTS, "用户 " + username + " 不存在"));

        // 更新其他用户信息
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getEmail() != null) {
            if (!user.getEmail().equals(updateDTO.getEmail()) && userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new ApiException("更新失败：邮箱已被其他用户注册");
            }
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getCountry() != null) {
            user.setCountry(updateDTO.getCountry());
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        if (updateDTO.getBirthday() != null) {
            user.setBirthday(updateDTO.getBirthday());
        }

        // --- 修改：处理头像更新 (Base64 字符串) ---
        String newAvatarData = updateDTO.getAvatar();
        if (newAvatarData != null && !newAvatarData.isEmpty()) {
            if (newAvatarData.startsWith("data:image")) { // 判断是否为Base64数据URI
                try {
                    // 1. 解析Base64字符串 (例如: data:image/jpeg;base64,/9j/...)
                    String[] parts = newAvatarData.split(",");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("无效的Base64数据URI格式");
                    }
                    String metadata = parts[0]; // "data:image/jpeg;base64"
                    String base64Content = parts[1];
                    byte[] imageBytes = Base64.getDecoder().decode(base64Content);

                    // 2. 提取MIME类型并确定文件扩展名
                    String mimeType = metadata.substring(metadata.indexOf(":") + 1, metadata.indexOf(";")); // "image/jpeg"
                    String extension;
                    switch (mimeType.toLowerCase()) {
                        case "image/jpeg":
                        case "image/jpg":
                            extension = ".jpg";
                            break;
                        case "image/png":
                            extension = ".png";
                            break;
                        case "image/gif":
                            extension = ".gif";
                            break;
                        default:
                            log.warn("用户 {} 上传了不支持的头像图片类型: {}", username, mimeType);
                            throw new ApiException(ResultCode.VALIDATION_ERROR, "不支持的图片类型: " + mimeType);
                    }

                    // 3. 生成一个包含原始信息和唯一性的文件名
                    //    例如：avatar_当前用户名_uuid.jpg
                    String originalFilenameWithExtension = "avatar_" + username.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + UUID.randomUUID().toString() + extension;

                    // 4. 使用 FileService 上传文件
                    //    假设 FileService 已经有 uploadFile(byte[] bytes, String originalFilenameWithExtension, String subDirectory)
                    String avatarUrl = fileService.uploadFile(imageBytes, originalFilenameWithExtension, "avatar"); // "avatar" 是存储头像的子目录

                    // 5. 更新用户实体中的头像URL
                    user.setAvatar(avatarUrl);
                    log.info("用户 {} 的头像已通过Base64数据更新为: {}", username, avatarUrl);

                } // 在 UserServiceImpl.java 的 updateUserInfo 方法中
// ...
                catch (IllegalArgumentException e) {
                    log.error("用户 {} 的Base64头像数据处理失败: {}", username, e.getMessage());
                    // 使用枚举中定义的 VALIDATION_ERROR
                    throw new ApiException(ResultCode.VALIDATION_ERROR, "无效的头像数据: " + e.getMessage());
                } catch (Exception e) { // 捕获 FileService 可能抛出的其他IO等异常
                    log.error("用户 {} 的头像文件保存失败: {}",username, e.getMessage(), e);
                    // 使用枚举中定义的 UPLOAD_FAILED，因为它更具体
                    throw new ApiException(ResultCode.UPLOAD_FAILED, "头像上传处理失败，请稍后再试。");
                }
// ...
            } else {
                // 如果不是 Base64 数据 URI，但又有值，这可能是一个外部URL
                // 根据业务需求决定是否允许用户直接设置外部URL作为头像
                // 为简单起见，如果不是预期的Base64格式，我们可能应该抛出错误或忽略它
                // 或者，如果允许外部URL，可以直接赋值：user.setAvatar(newAvatarData);
                log.warn("用户 {} 尝试更新头像，但提供的数据不是有效的Base64图片URI: {}", username, newAvatarData.substring(0, Math.min(newAvatarData.length(), 50)) + "...");
                // 可以选择抛出异常，或者如果业务允许直接设置URL，则取消注释下一行
                // throw new ApiException(ResultCode.VALIDATE_FAILED, "头像数据格式不正确，请重新上传图片。");
                user.setAvatar(newAvatarData); // 如果允许直接设置URL（需谨慎，可能引入安全风险或外部依赖）
            }
        } else if (newAvatarData != null && newAvatarData.isEmpty()){
            // 如果前端传递了一个明确的空字符串，可能表示用户想删除头像
            // user.setAvatar(null); // 取决于业务逻辑是否允许删除头像
        }
        // 如果 updateDTO.getAvatar() 是 null，则不更新头像字段，保持原有值
        // --- 结束修改 ---

        user = userRepository.save(user);

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            log.warn("Attempt to update password without authentication.");
            throw new ApiException(ResultCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ResultCode.USER_NOT_EXISTS, "用户 " + username + " 不存在"));

        if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
            throw new ApiException(ResultCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            log.warn("Attempt to upload avatar without authentication.");
            throw new ApiException(ResultCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ResultCode.USER_NOT_EXISTS, "用户 " + username + " 不存在"));

        String avatarUrl = fileService.uploadFile(file, "avatar");
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        log.info("用户 {} 通过uploadAvatar接口更新了头像: {}", username, avatarUrl);
        return avatarUrl;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.USER_NOT_EXISTS));
    }
}