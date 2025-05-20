package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    
    // 基础CRUD操作
    void insert(User user);
    
    void update(User user);
    
    int deleteById(Integer id);
    
    User selectById(Integer id);
    
    List<User> selectAll();
    
    // 分页查询
    List<User> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    // 按用户名查询
    User selectByUsername(String username);
    
    // 按邮箱查询
    User selectByEmail(String email);
    
    // 按手机号查询
    User selectByPhone(String phone);
    
    // 按角色查询
    List<User> selectByRole(@Param("role") String role, 
                          @Param("offset") int offset, 
                          @Param("limit") int limit);
    
    // 计数
    int count();
    
    // 更新最后登录时间
    int updateLastLoginTime(@Param("id") Integer id, @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime);
    
    User findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
} 