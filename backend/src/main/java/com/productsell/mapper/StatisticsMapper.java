package com.productsell.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    /** 概览：总数统计 */
    Map<String, Object> selectOverview(@Param("startDate") String startDate,
                                       @Param("endDate") String endDate);

    /** 销售趋势：按月统计 */
    List<Map<String, Object>> selectSalesTrend(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /** 热销商品 Top N */
    List<Map<String, Object>> selectTopProducts(@Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("limit") Integer limit);

    /** 分类销售占比 */
    List<Map<String, Object>> selectCategorySales(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    /** 订单状态分布 */
    List<Map<String, Object>> selectOrderStatusDistribution(@Param("startDate") String startDate,
                                                            @Param("endDate") String endDate);

    /** AI 助手用：低库存商品（库存小于阈值） */
    List<Map<String, Object>> selectLowStockProducts(@Param("threshold") int threshold);

    /** AI 助手用：最近 N 笔订单（含客户名） */
    List<Map<String, Object>> selectRecentOrders(@Param("limit") int limit);

    /** AI 助手用：各状态商品数量统计 */
    Map<String, Object> selectProductStatusCount();

    /** AI 助手用：最近新增客户 */
    List<Map<String, Object>> selectRecentCustomers(@Param("limit") int limit);
}
