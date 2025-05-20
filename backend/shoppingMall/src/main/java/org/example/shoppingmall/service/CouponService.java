package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.CouponDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    CouponDto createCoupon(CouponDto couponDto);
    
    CouponDto updateCoupon(Integer id, CouponDto couponDto);
    
    void deleteCoupon(Integer id);
    
    CouponDto getCouponById(Integer id);
    
    Page<CouponDto> getAllCoupons(Pageable pageable);
    
    List<CouponDto> getAvailableCoupons();
    
    List<CouponDto> getAvailableCouponsByAmount(BigDecimal amount);
    
    List<CouponDto> getUserCoupons(Long userId);
    
    List<CouponDto> getUserAvailableCouponsByAmount(Long userId, BigDecimal amount);
    
    void assignCouponToUser(Integer couponId, Long userId);
    
    void useCoupon(Integer couponId, Long userId);
} 