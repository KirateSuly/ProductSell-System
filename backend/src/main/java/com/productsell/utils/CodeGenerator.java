package com.productsell.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 业务编号自动生成器
 * <p>
 * 编号格式：前缀 + 年月日(yyyyMMdd) + 3 位自增序号(001-999)
 * <p>
 * 生成策略：
 * <ol>
 *   <li>查询当天已有的最大编号</li>
 *   <li>提取末尾序号并 +1</li>
 *   <li>若当天尚无记录，从 001 开始</li>
 * </ol>
 * <p>
 * 线程安全：使用 synchronized 关键字，适用于单机部署。
 * 未来扩展到多实例时，可改用数据库乐观锁或 Redis 原子递增。
 * <p>
 * 示例：
 * <ul>
 *   <li>客户编号：KH20260526001</li>
 *   <li>订单号：DD20260526001</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CodeGenerator {

    private final com.productsell.mapper.CustomerMapper customerMapper;
    private final com.productsell.mapper.OrderMapper orderMapper;

    /**
     * 生成客户编号
     * <p>
     * 格式：KH + yyyyMMdd + 序号（如 KH20260526001）
     *
     * @return 唯一的客户编号
     */
    public synchronized String generateCustomerCode() {
        return generateCode("KH", customerMapper::selectMaxCodeByPrefix);
    }

    /**
     * 生成订单号
     * <p>
     * 格式：DD + yyyyMMdd + 序号（如 DD20260526001）
     *
     * @return 唯一的订单号
     */
    public synchronized String generateOrderNo() {
        return generateCode("DD", orderMapper::selectMaxCodeByPrefix);
    }

    /**
     * 通用编号生成逻辑
     *
     * @param prefix  业务前缀（KH/DD/CP）
     * @param fetcher 查询当天最大编号的函数
     * @return 生成的完整编号
     */
    private String generateCode(String prefix, MaxCodeFetcher fetcher) {
        // 当天前缀：如 "KH20260526"
        String todayPrefix = prefix + new SimpleDateFormat("yyyyMMdd").format(new Date());
        // 查询数据库当天最大编号
        String maxCode = fetcher.fetch(todayPrefix);
        int sequence = 1;
        if (maxCode != null && maxCode.length() >= todayPrefix.length()) {
            try {
                // 提取末尾序号并 +1
                String seqPart = maxCode.substring(todayPrefix.length());
                sequence = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException ignored) {
                // 异常时从 1 开始（兼容脏数据）
            }
        }
        return todayPrefix + String.format("%03d", sequence);
    }

    /**
     * 函数式接口：查询指定前缀的最大编号
     */
    @FunctionalInterface
    private interface MaxCodeFetcher {
        String fetch(String prefix);
    }
}
