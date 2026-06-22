package com.productsell.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天请求 DTO
 * <p>
 * 前端发送给后端的消息格式，包含当前消息和对话历史。
 * 对话历史用于维持多轮对话上下文。
 */
@Data
public class ChatRequest {

    /** 用户当前消息 */
    private String message;

    /** 对话历史 [{role: "user"|"assistant", content: "..."}] */
    private List<Map<String, String>> history;
}
