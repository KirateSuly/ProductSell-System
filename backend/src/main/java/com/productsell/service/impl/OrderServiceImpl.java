package com.productsell.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.productsell.common.BusinessException;
import com.productsell.common.ResultCode;
import com.productsell.dto.OrderCreateDto;
import com.productsell.dto.OrderItemDto;
import com.productsell.entity.Customer;
import com.productsell.entity.Order;
import com.productsell.entity.OrderItem;
import com.productsell.entity.Product;
import com.productsell.mapper.CustomerMapper;
import com.productsell.mapper.OrderItemMapper;
import com.productsell.mapper.OrderMapper;
import com.productsell.mapper.ProductMapper;
import com.productsell.service.OrderService;
import com.productsell.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现 —— 核心业务逻辑
 * <p>
 * 订单操作全部使用 @Transactional 保证数据一致性：
 * <ul>
 *   <li>创建订单：生成订单号 → 校验商品和库存 → 扣减库存 → 计算金额 → 批量插入明细</li>
 *   <li>更新订单：恢复旧明细的库存 → 删除旧明细 → 重新处理新明细 → 更新总金额</li>
 *   <li>删除订单：恢复所有明细的库存 → 删除明细 → 删除订单</li>
 * </ul>
 * <p>
 * 注意：订单明细中的商品名称和价格以"下单时快照"方式存储，确保历史数据不受后续商品变更影响。
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CustomerMapper customerMapper;
    private final CodeGenerator codeGenerator;

    /**
     * 创建订单（含明细）
     * <p>
     * 事务流程：
     * 1. 校验客户存在
     * 2. 自动生成订单号，保存订单主体
     * 3. 遍历订单明细：校验商品存在、库存充足 → 构建 OrderItem（快照商品名称和价格）→ 扣减库存
     * 4. 汇总总金额，更新订单
     *
     * @param dto 订单创建请求（含客户ID、日期、商品明细列表）
     * @return 创建成功的订单实体
     */
    @Override
    @Transactional
    public Order createOrder(OrderCreateDto dto) {
        // 校验客户
        Customer customer = customerMapper.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }

        // 构建订单主体，初始状态为"待处理"
        Order order = new Order();
        order.setOrderNo(codeGenerator.generateOrderNo());
        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getCustomerName()); // 快照客户名称
        order.setOrderDate(dto.getOrderDate());
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setRemark(dto.getRemark());
        order.setOrderStatus("PENDING");

        // 先保存订单以获取自增主键 id，供明细使用
        save(order);

        // 处理订单明细：快照商品信息并扣减库存
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDto itemDto : dto.getItems()) {
            Product product = productMapper.selectById(itemDto.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_INSUFFICIENT);
            }

            // 创建订单明细，快照商品名称和价格
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setProductId(product.getId());
            item.setProductName(product.getProductName());   // 快照
            item.setProductPrice(product.getPrice());        // 快照
            item.setQuantity(itemDto.getQuantity());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            items.add(item);

            total = total.add(item.getSubtotal());

            // 扣减库存
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productMapper.updateById(product);
        }

        // 批量插入明细
        items.forEach(orderItemMapper::insert);

        // 更新订单总金额
        order.setTotalAmount(total);
        updateById(order);

        return order;
    }

    /**
     * 更新订单（含明细替换）
     * <p>
     * 仅允许修改"待处理"状态的订单。更新流程：
     * 1. 恢复旧明细对应的库存
     * 2. 删除旧明细
     * 3. 重新处理新明细（同创建逻辑）
     *
     * @param id  订单ID
     * @param dto 订单更新请求
     * @return 更新后的订单实体
     */
    @Override
    @Transactional
    public Order updateOrder(Long id, OrderCreateDto dto) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        // 只有"待处理"状态的订单允许修改
        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "只有待处理状态的订单可以修改");
        }

        // 第一步：恢复旧明细扣减的库存
        List<OrderItem> oldItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem oldItem : oldItems) {
            Product product = productMapper.selectById(oldItem.getProductId());
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + oldItem.getQuantity());
                productMapper.updateById(product);
            }
        }
        // 删除旧明细
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

        // 第二步：更新订单主体信息
        Customer customer = customerMapper.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }

        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getCustomerName());
        order.setOrderDate(dto.getOrderDate());
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setRemark(dto.getRemark());

        // 第三步：处理新明细
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDto itemDto : dto.getItems()) {
            Product product = productMapper.selectById(itemDto.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_INSUFFICIENT);
            }

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setProductId(product.getId());
            item.setProductName(product.getProductName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(itemDto.getQuantity());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            items.add(item);

            total = total.add(item.getSubtotal());
            // 扣减库存
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productMapper.updateById(product);
        }

        items.forEach(orderItemMapper::insert);
        order.setTotalAmount(total);
        updateById(order);

        return order;
    }

    /**
     * 删除订单（含明细）
     * <p>
     * 删除前恢复所有明细中已扣减的库存，然后级联删除明细和订单主体。
     *
     * @param id 订单ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteOrder(Long id) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 恢复库存：把该订单所有明细的商品数量加回去
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        // 先删明细，再删订单
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        return removeById(id);
    }

    /**
     * 更新订单状态
     * <p>
     * 状态流转: PENDING → SHIPPED → COMPLETED / CANCELLED
     *
     * @param id     订单ID
     * @param status 目标状态
     * @return 是否更新成功
     */
    @Override
    public boolean updateStatus(Long id, String status) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        order.setOrderStatus(status);
        return updateById(order);
    }
}
