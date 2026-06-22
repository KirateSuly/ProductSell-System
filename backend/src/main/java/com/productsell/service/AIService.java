package com.productsell.service;

import com.productsell.dto.ChatRequest;
import com.productsell.dto.ChatResponse;

/**
 * AI 智能问答服务接口
 * <p>
 * 封装与 LLM 的交互，负责 Prompt 工程、实时数据注入、调用大模型并返回结果。
 */
public interface AIService {

    /**
     * 处理用户对话消息
     *
     * @param request 用户消息及对话历史
     * @return AI 回复（markdown 格式）
     */
    ChatResponse chat(ChatRequest request);
}
