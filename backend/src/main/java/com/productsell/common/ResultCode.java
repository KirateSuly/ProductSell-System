package com.productsell.common;

import lombok.Getter;

/**
 * 统一状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器内部错误"),
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    LOGIN_LOCKED(1005, "操作频繁，请 15 分钟后再试"),
    ADMIN_CANNOT_DISABLE(1006, "系统管理员不可停用"),
    ADMIN_CANNOT_DELETE(1007, "系统管理员不可删除"),

    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_CODE_EXISTS(2002, "商品编号已存在"),
    STOCK_INSUFFICIENT(2003, "库存不足"),

    CUSTOMER_NOT_FOUND(3001, "客户不存在"),

    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态异常"),
    FILE_UPLOAD_ERROR(5001, "文件上传失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
