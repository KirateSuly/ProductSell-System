package com.productsell.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getOverview(String startDate, String endDate);

    List<Map<String, Object>> getSalesTrend(String startDate, String endDate);

    List<Map<String, Object>> getTopProducts(String startDate, String endDate, Integer limit);

    List<Map<String, Object>> getCategorySales(String startDate, String endDate);

    List<Map<String, Object>> getOrderStatusDistribution(String startDate, String endDate);
}
