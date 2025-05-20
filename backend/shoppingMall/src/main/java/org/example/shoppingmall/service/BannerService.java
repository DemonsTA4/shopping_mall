package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.BannerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BannerService {

    BannerDto createBanner(BannerDto bannerDto);
    
    BannerDto updateBanner(Integer id, BannerDto bannerDto);
    
    void deleteBanner(Integer id);
    
    BannerDto getBannerById(Integer id);
    
    List<BannerDto> getAllActiveBanners();
    
    List<BannerDto> getActiveBannersByPosition(String position);
    
    Page<BannerDto> getAllBanners(Pageable pageable);
    
    List<BannerDto> getHomeBanners();
} 