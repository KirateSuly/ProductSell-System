package com.productsell.controller;

import com.productsell.common.Result;
import com.productsell.dto.ChatRequest;
import com.productsell.dto.ChatResponse;
import com.productsell.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 智能问答控制器
 * <p>
 * 提供内嵌 AI 助手的对话接口。
 * <p>
 * 安全性：/api/ai/** 不在 SecurityConfig 白名单中，
 * 自动被 Spring Security + JWT 认证拦截，无需额外配置。
 */
@Tag(name = "AI 智能问答", description = "内嵌 AI 助手对话接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @Operation(summary = "发送对话消息", description = "向 AI 销售顾问发送消息，获取基于实时业务数据的回复")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        return Result.success(aiService.chat(request));
    }
}
