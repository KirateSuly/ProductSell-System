package com.productsell.dto;

import lombok.Data;

/**
 * AI 聊天响应 DTO
 * <p>
 * 后端返回给前端的消息格式。
 */
@Data
public class ChatResponse {

    /** AI 回复内容（markdown 格式） */
    private String reply;

    /** 是否因无关问题被拒绝回答 */
    private boolean refused;
}
