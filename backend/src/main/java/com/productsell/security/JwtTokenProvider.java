package com.productsell.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT Token 工具类
 * <p>
 * 负责 JWT 的生成、解析和验证。
 * 使用 HMAC-SHA 算法签名，密钥通过 Base64 解码获得。
 * <p>
 * Token 内容：
 * <ul>
 *   <li>sub: 用户名</li>
 *   <li>role: 用户角色（ADMIN/USER）</li>
 *   <li>iat: 签发时间</li>
 *   <li>exp: 过期时间（默认 24 小时）</li>
 * </ul>
 *
 * @see JwtAuthenticationFilter Token 验证的调用方
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expiration;
    private final long rememberMeExpiration;

    /**
     * 从配置文件注入 JWT 密钥和过期时间
     *
     * @param secret               Base64 编码的密钥（jwt.secret）
     * @param expiration           过期时间毫秒数（jwt.expiration，默认 24 小时）
     * @param rememberMeExpiration 记住我过期时间毫秒数（jwt.remember-me-expiration，默认 7 天）
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration,
                            @Value("${jwt.remember-me-expiration}") long rememberMeExpiration) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
        this.rememberMeExpiration = rememberMeExpiration;
    }

    /**
     * 生成 JWT Token（使用默认过期时间）
     *
     * @param username 用户名（存入 sub 字段）
     * @param role     角色（存入 role 自定义声明）
     * @return 签名的 JWT 字符串
     */
    public String generateToken(String username, String role) {
        return generateToken(username, role, expiration);
    }

    /**
     * 生成 JWT Token（指定过期时间）
     *
     * @param username       用户名
     * @param role           角色
     * @param expirationMs   自定义过期时间（毫秒），通常来自 remember-me 配置
     * @return 签名的 JWT 字符串
     */
    public String generateToken(String username, String role, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 获取默认过期时间（供 AuthController 判断是否使用 remember-me）
     */
    public long getDefaultExpiration() {
        return expiration;
    }

    /**
     * 获取"记住我"过期时间（7 天）
     */
    public long getRememberMeExpiration() {
        return rememberMeExpiration;
    }

    /**
     * 从 Token 中提取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 Token 中提取角色
     */
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 验证 Token 是否有效
     * <p>
     * 验证内容：签名正确性、是否过期、格式是否合法。
     *
     * @param token JWT 字符串
     * @return true 有效，false 无效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("无效的 JWT 签名");
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已过期");
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的 JWT Token");
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token 为空");
        }
        return false;
    }

    /**
     * 解析 Token 的 Claims
     * <p>
     * 使用 jjwt 库的 verifyWith 进行签名验证后提取载荷。
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
