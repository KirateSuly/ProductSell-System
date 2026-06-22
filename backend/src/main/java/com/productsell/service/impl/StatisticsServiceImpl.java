package com.productsell.service.impl;

import com.productsell.mapper.StatisticsMapper;
import com.productsell.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    @Override
    public Map<String, Object> getOverview(String startDate, String endDate) {
        return statisticsMapper.selectOverview(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getSalesTrend(String startDate, String endDate) {
        return statisticsMapper.selectSalesTrend(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getTopProducts(String startDate, String endDate, Integer limit) {
        return statisticsMapper.selectTopProducts(startDate, endDate, limit != null ? limit : 5);
    }

    @Override
    public List<Map<String, Object>> getCategorySales(String startDate, String endDate) {
        return statisticsMapper.selectCategorySales(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getOrderStatusDistribution(String startDate, String endDate) {
        return statisticsMapper.selectOrderStatusDistribution(startDate, endDate);
    }
}
