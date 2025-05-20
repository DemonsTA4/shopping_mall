package org.example.shoppingmall.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.dto.CouponDto;
import org.example.shoppingmall.entity.Coupon;
import org.example.shoppingmall.entity.User;
import org.example.shoppingmall.repository.CouponRepository;
import org.example.shoppingmall.repository.UserRepository;
import org.example.shoppingmall.service.CouponService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CouponDto createCoupon(CouponDto couponDto) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponDto, coupon);
        coupon.setUsedCount(0); // 确保新创建的优惠券使用数为0
        coupon.setIsActive(true); // 默认为激活状态
        
        Coupon savedCoupon = couponRepository.save(coupon);
        
        CouponDto savedDto = new CouponDto();
        BeanUtils.copyProperties(savedCoupon, savedDto);
        return savedDto;
    }

    @Override
    @Transactional
    public CouponDto updateCoupon(Integer id, CouponDto couponDto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("优惠券不存在，ID: " + id));
        
        // 保留原有的users集合和使用数
        BeanUtils.copyProperties(couponDto, coupon, "id", "users", "usedCount");
        
        Coupon updatedCoupon = couponRepository.save(coupon);
        
        CouponDto updatedDto = new CouponDto();
        BeanUtils.copyProperties(updatedCoupon, updatedDto);
        return updatedDto;
    }

    @Override
    @Transactional
    public void deleteCoupon(Integer id) {
        if (!couponRepository.existsById(id)) {
            throw new EntityNotFoundException("优惠券不存在，ID: " + id);
        }
        couponRepository.deleteById(id);
    }

    @Override
    public CouponDto getCouponById(Integer id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("优惠券不存在，ID: " + id));
        
        CouponDto couponDto = new CouponDto();
        BeanUtils.copyProperties(coupon, couponDto);
        return couponDto;
    }

    @Override
    public Page<CouponDto> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable)
                .map(coupon -> {
                    CouponDto dto = new CouponDto();
                    BeanUtils.copyProperties(coupon, dto);
                    return dto;
                });
    }

    @Override
    public List<CouponDto> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findAvailableCoupons(now)
                .stream()
                .map(coupon -> {
                    CouponDto dto = new CouponDto();
                    BeanUtils.copyProperties(coupon, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponDto> getAvailableCouponsByAmount(BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findAvailableCouponsByAmount(now, amount)
                .stream()
                .map(coupon -> {
                    CouponDto dto = new CouponDto();
                    BeanUtils.copyProperties(coupon, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponDto> getUserCoupons(Long userId) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("用户不存在，ID: " + userId);
        }
        
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findUserCoupons(userId, now)
                .stream()
                .map(coupon -> {
                    CouponDto dto = new CouponDto();
                    BeanUtils.copyProperties(coupon, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponDto> getUserAvailableCouponsByAmount(Long userId, BigDecimal amount) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("用户不存在，ID: " + userId);
        }
        
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findUserCouponsByAmount(userId, now, amount)
                .stream()
                .map(coupon -> {
                    CouponDto dto = new CouponDto();
                    BeanUtils.copyProperties(coupon, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignCouponToUser(Integer couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("优惠券不存在，ID: " + couponId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));
        
        // 检查优惠券是否可用
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.getIsActive() || now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new IllegalStateException("优惠券当前不可用");
        }
        
        // 检查优惠券数量是否已经用完
        if (coupon.getUsedCount() >= coupon.getTotalCount()) {
            throw new IllegalStateException("优惠券已经被领完");
        }
        
        // 将优惠券分配给用户
        user.addCoupon(coupon);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void useCoupon(Integer couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("优惠券不存在，ID: " + couponId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));
        
        // 检查用户是否拥有此优惠券
        if (user.getCoupons().stream().noneMatch(c -> c.getId().equals(couponId))) {
            throw new IllegalStateException("用户没有此优惠券");
        }
        
        // 检查优惠券是否可用
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.getIsActive() || now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new IllegalStateException("优惠券当前不可用");
        }
        
        // 增加使用数量
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        
        // 从用户的优惠券列表中移除
        user.removeCoupon(coupon);
        
        couponRepository.save(coupon);
        userRepository.save(user);
    }
} 