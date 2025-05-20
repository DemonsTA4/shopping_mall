package org.example.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.dto.BannerDto;
import org.example.shoppingmall.entity.Banner;
import org.example.shoppingmall.repository.BannerRepository;
import org.example.shoppingmall.service.BannerService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    
    private static final String BANNER_CACHE = "bannerCache";
    private static final String ALL_BANNERS_CACHE_KEY = "'all'";
    private static final String HOME_BANNERS_CACHE_KEY = "'home'";
    private static final String POSITION_BANNERS_CACHE_KEY = "'position:'";

    @Override
    @Transactional
    @CacheEvict(value = BANNER_CACHE, allEntries = true)
    public BannerDto createBanner(BannerDto bannerDto) {
        Banner banner = new Banner();
        BeanUtils.copyProperties(bannerDto, banner);
        Banner savedBanner = bannerRepository.save(banner);
        BeanUtils.copyProperties(savedBanner, bannerDto);
        bannerDto.setId(savedBanner.getId());
        return bannerDto;
    }

    @Override
    @Transactional
    @CacheEvict(value = BANNER_CACHE, allEntries = true)
    public BannerDto updateBanner(Integer id, BannerDto bannerDto) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found with id: " + id));
        
        BeanUtils.copyProperties(bannerDto, banner);
        banner.setId(id); // 确保ID不变
        
        Banner updatedBanner = bannerRepository.save(banner);
        BeanUtils.copyProperties(updatedBanner, bannerDto);
        return bannerDto;
    }

    @Override
    @Transactional
    @CacheEvict(value = BANNER_CACHE, allEntries = true)
    public void deleteBanner(Integer id) {
        bannerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BannerDto getBannerById(Integer id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found with id: " + id));
        
        BannerDto bannerDto = new BannerDto();
        BeanUtils.copyProperties(banner, bannerDto);
        return bannerDto;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = BANNER_CACHE, key = ALL_BANNERS_CACHE_KEY)
    public List<BannerDto> getAllActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortOrder().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = BANNER_CACHE, key = POSITION_BANNERS_CACHE_KEY + " + #position")
    public List<BannerDto> getActiveBannersByPosition(String position) {
        return bannerRepository.findByPositionAndIsActiveTrueOrderBySortOrder(position).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BannerDto> getAllBanners(Pageable pageable) {
        return bannerRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = BANNER_CACHE, key = HOME_BANNERS_CACHE_KEY)
    public List<BannerDto> getHomeBanners() {
        return bannerRepository.findTop5ByPositionAndIsActiveTrueOrderBySortOrder("home").stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private BannerDto convertToDto(Banner banner) {
        BannerDto dto = new BannerDto();
        BeanUtils.copyProperties(banner, dto);
        return dto;
    }
} 