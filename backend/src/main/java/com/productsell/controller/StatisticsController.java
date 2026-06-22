package com.productsell.controller;

import com.productsell.common.Result;
import com.productsell.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 销售统计接口
 * <p>
 * 提供仪表盘概览和销售图表数据。
 * 所有统计接口均支持可选的时间范围参数（startDate/endDate），
 * 不传则统计全部历史数据。
 * <p>
 * 图表类型映射：
 * <ul>
 *   <li>销售额趋势 → 双折线图（订单数 + 销售额）</li>
 *   <li>热销商品排行 → 柱状图（按销量降序）</li>
 *   <li>分类销售占比 → 饼图（环形）</li>
 *   <li>订单状态分布 → 饼图</li>
 * </ul>
 */
@Tag(name = "统计图表", description = "销售数据统计与图表数据接口")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取概览数据
     * <p>
     * 返回四个关键指标：在售商品数、客户总数、订单总数、总销售额。
     */
    @Operation(summary = "获取概览数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statisticsService.getOverview(startDate, endDate));
    }

    /**
     * 销售趋势（按月）
     * <p>
     * 返回按月份聚合的订单数和销售额，用于折线图展示。
     * 仅统计非取消状态的订单。
     */
    @Operation(summary = "销售趋势（按月）")
    @GetMapping("/sales-trend")
    public Result<?> salesTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statisticsService.getSalesTrend(startDate, endDate));
    }

    /**
     * 热销商品排行
     * <p>
     * 按销量降序返回 Top N 商品，默认 Top 10。
     */
    @Operation(summary = "热销商品排行")
    @GetMapping("/top-products")
    public Result<?> topProducts(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getTopProducts(startDate, endDate, limit));
    }

    /**
     * 分类销售占比
     * <p>
     * 按商品分类汇总销售额，用于饼图展示。
     */
    @Operation(summary = "分类销售占比")
    @GetMapping("/category-sales")
    public Result<?> categorySales(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statisticsService.getCategorySales(startDate, endDate));
    }

    /**
     * 订单状态分布
     * <p>
     * 按订单状态分组计数，用于饼图展示。
     */
    @Operation(summary = "订单状态分布")
    @GetMapping("/order-status")
    public Result<?> orderStatus(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statisticsService.getOrderStatusDistribution(startDate, endDate));
    }
}
