package com.productsell.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 使用 @RestControllerAdvice 拦截所有 Controller 抛出的异常，
 * 统一转换为 {@link Result} 格式的 JSON 响应，避免异常信息直接暴露给前端。
 * <p>
 * 异常处理优先级（从具体到通用）：
 * <ol>
 *   <li>BusinessException → 业务异常，返回自定义状态码和消息</li>
 *   <li>AuthenticationException → 认证异常，返回 401</li>
 *   <li>AccessDeniedException → 权限异常，返回 403</li>
 *   <li>BindException → 参数校验失败，返回 400 + 字段错误信息</li>
 *   <li>Exception → 兜底，返回 500</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     * <p>
     * 继承自 RuntimeException，由业务代码主动抛出。
     * 返回异常中携带的自定义状态码和消息。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 认证异常（未登录或 Token 无效）
     * <p>
     * HTTP 401 状态码触发前端自动跳转登录页。
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常: {}", e.getMessage());
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    /**
     * 权限异常（已登录但角色不匹配）
     * <p>
     * 返回 403 告知用户无权限执行此操作。
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }

    /**
     * 参数校验异常（@Valid 验证失败，GET 请求 / 表单绑定）
     * <p>
     * 提取字段级别的校验错误信息拼接后返回。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 参数校验异常（@Valid + @RequestBody 验证失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 未知异常兜底处理
     * <p>
     * 记录完整堆栈日志，返回通用的 "服务器内部错误" 消息，避免敏感信息泄露。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.ERROR);
    }
}
