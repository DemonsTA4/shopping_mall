package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.shoppingmall.entity.Role;

@Mapper
public interface RoleMapper {
    
    Role findByName(String name);
    
    void insert(Role role);
    
    boolean existsByName(String name);
} 