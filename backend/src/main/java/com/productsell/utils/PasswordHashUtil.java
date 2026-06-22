package com.productsell.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 密码哈希工具
 * <p>
 * 前端传输前对密码进行 SHA-256 脱敏处理，避免明文在网络上传输。
 * 服务端收到 SHA-256 哈希值后，再使用 BCrypt 加密存储。
 * 数据库最终存储格式：BCrypt(SHA-256(明文))。
 */
public class PasswordHashUtil {

    private PasswordHashUtil() {}

    /**
     * 对明文密码进行 SHA-256 哈希，返回十六进制字符串。
     * 与前端 CryptoJS.SHA256() 输出一致。
     */
    public static String sha256(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 计算失败", e);
        }
    }
}
