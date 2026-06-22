package com.productsell.service.impl;

import com.productsell.common.BusinessException;
import com.productsell.dto.ChatRequest;
import com.productsell.dto.ChatResponse;
import com.productsell.mapper.StatisticsMapper;
import com.productsell.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 智能问答服务实现（Spring AI 方案）
 * <p>
 * 核心逻辑：
 * <ol>
 *   <li>从数据库查询实时业务数据（概览、库存、订单、热销、客户）</li>
 *   <li>构建 System Prompt：角色锁定 + 规则约束 + 实时数据注入</li>
 *   <li>拼接对话历史 + 当前用户消息</li>
 *   <li>通过 Spring AI 的 ChatModel 调用大模型 API</li>
 *   <li>封装返回 ChatResponse</li>
 * </ol>
 * <p>
 * 防 AI 乱答多层防护：
 * <ul>
 *   <li>第一层：System Prompt 角色锁定，严格限定业务范围</li>
 *   <li>第二层：只注入真实 DB 数据，不给 AI "想象空间"</li>
 *   <li>第三层：application.yml 中 temperature=0.3 + max-tokens=1500</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    private final ChatModel chatModel;
    private final StatisticsMapper statisticsMapper;

    /** 日期格式化 */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public ChatResponse chat(ChatRequest request) {
        // 1. 查询实时业务数据
        Map<String, Object> bizData = buildBusinessData();

        // 2. 构建 System Prompt（角色 + 规则 + 实时数据）
        String systemContent = buildSystemPrompt(bizData);

        // 3. 拼接消息列表
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemContent));

        // 拼接对话历史（最近几轮）
        if (request.getHistory() != null) {
            for (Map<String, String> h : request.getHistory()) {
                String role = h.get("role");
                String content = h.get("content");
                if ("user".equals(role)) {
                    messages.add(new UserMessage(content));
                } else if ("assistant".equals(role)) {
                    messages.add(new AssistantMessage(content));
                }
            }
        }

        // 当前用户消息
        messages.add(new UserMessage(request.getMessage()));

        // 4. 调用大模型
        String reply;
        try {
            var prompt = new Prompt(messages);
            var aiResponse = chatModel.call(prompt);
            reply = aiResponse.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("AI 模型调用失败", e);
            throw new BusinessException("AI 服务调用失败: " + e.getMessage());
        }

        // 5. 封装返回
        ChatResponse result = new ChatResponse();
        result.setReply(reply);
        result.setRefused(false);
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 构建 System Prompt
     * <p>
     * 包含：角色定义 + 核心规则 + 能力范围 + 格式要求 + 实时数据
     */
    private String buildSystemPrompt(Map<String, Object> bizData) {
        return """
                你是一个专业的进销存管理系统顾问，只能回答与销售、库存、商品、订单、客户管理相关的问题。

                ## 核心规则（必须严格遵守）
                1. 你只服务于"商品进销存管理系统"的业务咨询。
                2. 如果用户询问与进销存业务无关的问题（如天气、闲聊、编程、娱乐、政治等），回复："抱歉，我是销售进销存顾问，只能回答商品管理、库存、订单、客户、销售统计等相关问题。请提出业务相关的问题。"
                3. 所有数据必须基于下方【当前系统数据】中的真实数字进行分析。
                4. 如果用户询问的数据在【当前系统数据】中不存在，回复："暂无该统计数据"。
                5. 严格禁止虚构或编造任何金额、数量、日期、人名等数据。
                6. 回答中涉及金额时，必须标注货币单位（元/人民币）。

                ## 你能做的事情
                - 查询和分析销售统计（营收、订单量、趋势）
                - 分析库存状况（在售/停售、库存不足预警）
                - 查询订单状态分布
                - 分析客户数据
                - 热销商品排行分析
                - 指导用户操作系统功能（如何添加商品、如何创建订单等）

                ## 回答格式要求
                - 用清晰的标题分段（如：### 营收概况、### 库存分析）
                - 数字用表格或列表形式展示，标注清楚指标名称、数值、单位、对比
                - 分析类回答给出简要的趋势解读或建议

                """ + bizData.get("dataText");
    }

    /**
     * 从数据库查询实时业务数据
     * <p>
     * 查询内容：概览统计、库存状况、订单概况、热销商品、客户概况。
     * 将查询结果拼接为 System Prompt 末尾的「当前系统数据」文本块。
     *
     * @return 包含 dataText 的 Map
     */
    private Map<String, Object> buildBusinessData() {
        // 概览统计（不限时间）
        Map<String, Object> overview = statisticsMapper.selectOverview(null, null);

        // 商品状态统计
        Map<String, Object> productStatus = statisticsMapper.selectProductStatusCount();

        // 低库存商品（库存 < 10）
        List<Map<String, Object>> lowStockList = statisticsMapper.selectLowStockProducts(10);

        // 订单状态分布
        List<Map<String, Object>> orderStatusList = statisticsMapper.selectOrderStatusDistribution(null, null);

        // 最近 5 笔订单
        List<Map<String, Object>> recentOrders = statisticsMapper.selectRecentOrders(5);

        // 热销商品 Top 5
        List<Map<String, Object>> topProducts = statisticsMapper.selectTopProducts(null, null, 5);

        // 最近新增客户（5 位）
        List<Map<String, Object>> recentCustomers = statisticsMapper.selectRecentCustomers(5);

        // ---------- 拼接数据文本 ----------
        StringBuilder sb = new StringBuilder();
        sb.append("## 当前系统数据（实时）\n\n");

        // 概览
        sb.append("### 概览指标\n");
        sb.append(formatOverview(overview));

        // 库存
        sb.append("### 库存概况\n");
        sb.append(formatProductStatus(productStatus));
        sb.append(formatLowStock(lowStockList));

        // 订单
        sb.append("### 订单概况\n");
        sb.append(formatOrderStatus(orderStatusList));
        sb.append(formatRecentOrders(recentOrders));

        // 热销
        sb.append("### 热销商品 Top 5\n");
        sb.append(formatTopProducts(topProducts));

        // 客户
        sb.append("### 客户概况\n");
        sb.append(formatRecentCustomers(recentCustomers));

        return Map.of("dataText", sb.toString());
    }

    // ==================== 格式化辅助方法 ====================

    private String formatOverview(Map<String, Object> data) {
        if (data == null) return "";
        return String.format("- 在售商品数：%s\n- 客户总数：%s\n- 订单总数：%s\n- 总销售额：%s 元\n\n",
                data.getOrDefault("totalProducts", 0),
                data.getOrDefault("totalCustomers", 0),
                data.getOrDefault("totalOrders", 0),
                formatAmount(data.get("totalRevenue")));
    }

    private String formatProductStatus(Map<String, Object> data) {
        if (data == null) return "";
        return String.format("- 在售商品：%s 种\n- 停售商品：%s 种\n\n",
                data.getOrDefault("activeProducts", 0),
                data.getOrDefault("inactiveProducts", 0));
    }

    private String formatLowStock(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "- 低库存预警（库存<10）：无\n\n";
        String items = list.stream()
                .map(m -> m.get("productName") + ":" + m.get("stockQuantity") + "件")
                .collect(Collectors.joining("、"));
        return "- 低库存预警（库存<10）：" + items + "\n\n";
    }

    private String formatOrderStatus(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> m : list) {
            String status = String.valueOf(m.getOrDefault("status", "未知"));
            Object count = m.getOrDefault("count", 0);
            sb.append(String.format("- %s：%s 单\n", translateOrderStatus(status), count));
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatRecentOrders(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "- 最近订单：暂无\n\n";
        StringBuilder sb = new StringBuilder("- 最近 5 笔订单：\n");
        for (Map<String, Object> m : list) {
            String dateStr = "";
            if (m.get("orderDate") != null) {
                try {
                    dateStr = DATE_FMT.format(java.time.LocalDateTime.parse(m.get("orderDate").toString().replace("T", " ").substring(0, 19), DATE_FMT));
                } catch (Exception e) {
                    dateStr = m.get("orderDate").toString();
                }
            }
            sb.append(String.format("  - 订单 %s | %s | %s 元 | %s | %s\n",
                    m.getOrDefault("orderNo", ""),
                    m.getOrDefault("customerName", ""),
                    formatAmount(m.get("totalAmount")),
                    m.getOrDefault("orderStatus", ""),
                    dateStr));
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatTopProducts(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "- 暂无热销数据\n\n";
        StringBuilder sb = new StringBuilder();
        sb.append("| 商品名称 | 销量 | 销售额(元) |\n");
        sb.append("|----------|------|------------|\n");
        for (Map<String, Object> m : list) {
            sb.append(String.format("| %s | %s | %s |\n",
                    m.getOrDefault("productName", ""),
                    m.getOrDefault("totalQuantity", 0),
                    formatAmount(m.get("totalAmount"))));
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatRecentCustomers(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return "- 最近新增客户：暂无\n";
        String items = list.stream()
                .map(m -> String.valueOf(m.get("customerName")))
                .collect(Collectors.joining("、"));
        return "- 总客户数：见概览\n- 最近新增客户：" + items + "\n";
    }

    /** 格式化金额：BigDecimal 转两位小数，null 转为 0.00 */
    private String formatAmount(Object amount) {
        if (amount == null) return "0.00";
        if (amount instanceof BigDecimal bd) {
            return bd.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        }
        try {
            return new BigDecimal(amount.toString()).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            return amount.toString();
        }
    }

    /** 翻译订单状态（中文展示） */
    private String translateOrderStatus(String status) {
        return switch (status) {
            case "PENDING" -> "待处理";
            case "SHIPPED" -> "已发货";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }
}
