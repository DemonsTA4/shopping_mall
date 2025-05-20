package org.example.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.config.CacheConfig;
import org.example.shoppingmall.dto.StatisticsDto;
import org.example.shoppingmall.repository.CategoryRepository;
import org.example.shoppingmall.repository.OrderRepository;
import org.example.shoppingmall.repository.ProductRepository;
import org.example.shoppingmall.repository.UserRepository;
import org.example.shoppingmall.service.StatisticsService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Cacheable(value = CacheConfig.STATISTICS_CACHE, key = "'overview'")
    @Transactional(readOnly = true)
    public StatisticsDto getOverviewStatistics() {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        
        // 转换为LocalDateTime
        LocalDateTime startOfThirtyDaysAgo = thirtyDaysAgo.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);
        
        // 获取基本统计数据
        Integer totalOrders = orderRepository.countOrdersBetweenDates(startOfThirtyDaysAgo, endOfToday);
        BigDecimal totalRevenue = orderRepository.sumOrderAmountBetweenDates(startOfThirtyDaysAgo, endOfToday);
        
        // 创建统计DTO
        return StatisticsDto.builder()
                .totalOrders(totalOrders)
                .totalProducts((int) productRepository.count())
                .totalUsers((int) userRepository.count())
                .totalCategories((int) categoryRepository.count())
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .startDate(thirtyDaysAgo)
                .endDate(today)
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.STATISTICS_CACHE, key = "'detailed_' + #startDate + '_' + #endDate + '_' + #topCount")
    @Transactional(readOnly = true)
    public StatisticsDto getDetailedStatistics(LocalDate startDate, LocalDate endDate, int topCount) {
        // 转换为LocalDateTime
        LocalDateTime startOfStartDate = startDate.atStartOfDay();
        LocalDateTime endOfEndDate = endDate.atTime(LocalTime.MAX);
        
        // 获取基本统计数据
        Integer totalOrders = orderRepository.countOrdersBetweenDates(startOfStartDate, endOfEndDate);
        BigDecimal totalRevenue = orderRepository.sumOrderAmountBetweenDates(startOfStartDate, endOfEndDate);
        
        // 获取每日订单统计
        List<Object[]> dailyOrderStats = orderRepository.countOrdersByDate(startOfStartDate, endOfEndDate);
        Map<String, Integer> dailyOrderCounts = new HashMap<>();
        for (Object[] stat : dailyOrderStats) {
            if (stat[0] != null && stat[1] != null) {
                String date = stat[0].toString();
                Integer count = Integer.valueOf(stat[1].toString());
                dailyOrderCounts.put(date, count);
            }
        }
        
        // 获取每日收入统计
        List<Object[]> dailyRevenueStats = orderRepository.sumOrderAmountByDate(startOfStartDate, endOfEndDate);
        Map<String, BigDecimal> dailyRevenue = new HashMap<>();
        for (Object[] stat : dailyRevenueStats) {
            if (stat[0] != null && stat[1] != null) {
                String date = stat[0].toString();
                BigDecimal amount = new BigDecimal(stat[1].toString());
                dailyRevenue.put(date, amount);
            }
        }
        
        // 获取热门用户统计
        List<Object[]> topUserStats = orderRepository.sumOrderAmountByUser(startOfStartDate, endOfEndDate, PageRequest.of(0, topCount));
        List<StatisticsDto.UserStatsDto> topUsers = new ArrayList<>();
        for (Object[] stat : topUserStats) {
            if (stat[0] != null && stat[1] != null) {
                Long userId = Long.valueOf(stat[0].toString());
                BigDecimal totalSpent = new BigDecimal(stat[1].toString());
                
                // 为了获取用户名，需要从userRepository获取
                userRepository.findById(userId).ifPresent(user -> {
                    // 获取用户订单数
                    Long orderCountLong = orderRepository.countByBuyerId(userId); // 调用新的计数方法
                    Integer orderCount = (orderCountLong != null) ? orderCountLong.intValue() : 0; // 转换为 Integer
                    
                    StatisticsDto.UserStatsDto userStats = new StatisticsDto.UserStatsDto(
                            userId,
                            user.getUsername(),
                            orderCount,
                            totalSpent
                    );
                    topUsers.add(userStats);
                });
            }
        }
        
        // 构建并返回详细统计DTO
        return StatisticsDto.builder()
                .totalOrders(totalOrders)
                .totalProducts((int) productRepository.count())
                .totalUsers((int) userRepository.count())
                .totalCategories((int) categoryRepository.count())
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .startDate(startDate)
                .endDate(endDate)
                .dailyOrderCounts(dailyOrderCounts)
                .dailyRevenue(dailyRevenue)
                .topUsers(topUsers)
                .build();
    }

    @Override
    @CacheEvict(value = CacheConfig.STATISTICS_CACHE, allEntries = true)
    public void refreshStatisticsCache() {
        // 方法实现为空，目的是通过@CacheEvict注解清除统计缓存
    }
} 