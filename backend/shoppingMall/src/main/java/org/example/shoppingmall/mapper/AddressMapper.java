package org.example.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shoppingmall.entity.Address;

import java.util.List;

@Mapper
public interface AddressMapper {
    
    // 基础CRUD操作
    int insert(Address address);
    
    int update(Address address);
    
    int deleteById(Integer id);
    
    Address selectById(Integer id);
    
    List<Address> selectAll();
    
    // 根据用户ID查询地址
    List<Address> selectByUserId(Long userId);
    
    // 根据用户ID查询默认地址
    Address selectDefaultByUserId(Long userId);
    
    // 取消该用户所有地址的默认状态
    int updateResetDefaultByUserId(Long userId);
    
    // 设置地址为默认
    int updateSetDefault(@Param("addressId") Integer addressId, @Param("userId") Long userId);
    
    // 检查地址是否属于指定用户
    int countByIdAndUserId(@Param("id") Integer id, @Param("userId") Long userId);
} 