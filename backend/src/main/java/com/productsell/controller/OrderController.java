package com.productsell.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.productsell.common.BusinessException;
import com.productsell.common.Result;
import com.productsell.common.ResultCode;
import com.productsell.dto.OrderCreateDto;
import com.productsell.dto.OrderQueryDto;
import com.productsell.dto.PageResult;
import com.productsell.entity.Order;
import com.productsell.entity.OrderItem;
import com.productsell.mapper.OrderItemMapper;
import com.productsell.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理接口
 * <p>
 * 提供订单的增删改查和状态管理功能。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>订单创建/更新均在 {@link com.productsell.service.impl.OrderServiceImpl} 中以事务方式处理</li>
 *   <li>订单明细中的商品名称和价格为下单时快照，确保历史数据不变</li>
 *   <li>状态管理支持运行中变更（PENDING → SHIPPED → COMPLETED / CANCELLED）</li>
 *   <li>查询支持按订单号、客户、状态、日期范围多条件筛选</li>
 * </ul>
 */
@Tag(name = "订单管理", description = "订单增删改查及状态管理")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemMapper orderItemMapper;

    /**
     * 分页查询订单
     * <p>
     * 支持多条件组合筛选：订单号（模糊）、客户ID、状态、日期范围。
     * 按创建时间倒序排列。
     */
    @Operation(summary = "分页查询订单")
    @GetMapping
    public Result<PageResult<Order>> list(OrderQueryDto query) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getOrderNo())) {
            wrapper.like(Order::getOrderNo, query.getOrderNo());
        }
        if (query.getCustomerId() != null) {
            wrapper.eq(Order::getCustomerId, query.getCustomerId());
        }
        if (StringUtils.hasText(query.getOrderStatus())) {
            wrapper.eq(Order::getOrderStatus, query.getOrderStatus());
        }
        // 日期范围查询
        if (StringUtils.hasText(query.getStartDate())) {
            wrapper.ge(Order::getOrderDate, query.getStartDate());
        }
        if (StringUtils.hasText(query.getEndDate())) {
            wrapper.le(Order::getOrderDate, query.getEndDate());
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = orderService.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.success(PageResult.from(result));
    }

    /**
     * 获取订单详情
     * <p>
     * 返回订单主体信息和完整的商品明细列表，用于订单详情弹窗展示。
     */
    @Operation(summary = "获取订单详情（含明细）")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        // 查询关联的订单明细
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        return Result.success(Map.of("order", order, "items", items));
    }

    /**
     * 创建订单
     * <p>
     * 接收订单主体和明细列表，由 Service 层在事务中完成：
     * 生成订单号 → 校验库存 → 快照商品信息 → 扣减库存 → 计算总金额。
     */
    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Order> create(@Valid @RequestBody OrderCreateDto dto) {
        Order order = orderService.createOrder(dto);
        return Result.success("创建成功", order);
    }

    /**
     * 更新订单
     * <p>
     * 仅允许修改"待处理"状态的订单。修改时先恢复旧明细的库存，再按新明细重新处理。
     */
    @Operation(summary = "更新订单")
    @PutMapping("/{id}")
    public Result<Order> update(@PathVariable Long id, @Valid @RequestBody OrderCreateDto dto) {
        Order order = orderService.updateOrder(id, dto);
        return Result.success("更新成功", order);
    }

    /**
     * 删除订单
     * <p>
     * 删除时自动恢复已扣减的商品库存。
     */
    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success("删除成功", null);
    }

    /**
     * 更新订单状态
     * <p>
     * 状态值：PENDING（待处理）、SHIPPED（已发货）、COMPLETED（已完成）、CANCELLED（已取消）。
     */
    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateStatus(id, body.get("status"));
        return Result.success("状态更新成功", null);
    }
}
