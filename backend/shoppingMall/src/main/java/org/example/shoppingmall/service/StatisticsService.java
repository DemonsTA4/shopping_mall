package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.StatisticsDto;

import java.time.LocalDate;

/**
 * 统计数据服务接口
 */
public interface StatisticsService {
    
    /**
     * 获取系统概览统计数据
     * @return 统计数据DTO
     */
    StatisticsDto getOverviewStatistics();
    
    /**
     * 获取指定日期范围内的详细统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param topCount 返回热门商品和活跃用户的数量
     * @return 统计数据DTO
     */
    StatisticsDto getDetailedStatistics(LocalDate startDate, LocalDate endDate, int topCount);
    
    /**
     * 刷新统计数据缓存
     */
    void refreshStatisticsCache();
} 