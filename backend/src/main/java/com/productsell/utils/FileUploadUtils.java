package com.productsell.utils;

import com.productsell.common.BusinessException;
import com.productsell.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传工具类
 * <p>
 * 负责商品图片的上传存储和删除清理。
 * <p>
 * 安全策略：
 * <ul>
 *   <li>仅允许图片格式（JPG/PNG/GIF/WebP）</li>
 *   <li>文件大小限制 5MB</li>
 *   <li>文件名使用 UUID 重命名，防止路径遍历和重名冲突</li>
 *   <li>文件存储在 uploads/products/ 目录下</li>
 * </ul>
 * <p>
 * 图片通过 {@link com.productsell.config.WebMvcConfig} 中的资源映射
 * 以 HTTP URL 方式对外提供访问。
 */
@Slf4j
@Component
public class FileUploadUtils {

    /** 允许的图片 MIME 类型 */
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    ));

    /** 单文件最大 5MB */
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    /**
     * 上传商品图片
     * <p>
     * 1. 校验文件非空、大小、MIME 类型
     * 2. UUID 重命名后保存到 uploads/products/ 目录
     * 3. 返回 HTTP 可访问的相对路径
     *
     * @param file 上传的图片文件
     * @return 图片访问 URL（如 /uploads/products/a1b2c3d4.jpg）
     * @throws BusinessException 文件校验失败或 IO 异常时抛出
     */
    public String uploadProductImage(MultipartFile file) {
        // 基础校验
        if (file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件大小不能超过 5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("仅支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        try {
            // 提取原始文件扩展名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            // UUID 重命名，防止冲突和路径遍历
            String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 确保目录存在
            Path dir = Paths.get(uploadPath, "products");
            Files.createDirectories(dir);
            Path target = dir.resolve(newFileName);
            file.transferTo(target.toFile());

            String url = "/uploads/products/" + newFileName;
            log.info("图片上传成功: {}", url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * 根据图片 URL 删除文件
     * <p>
     * 仅在 URL 以 /uploads/ 开头时执行删除，防止误删。
     * 删除失败只记录日志，不抛出异常（best-effort 清理）。
     *
     * @param imageUrl 图片访问路径（如 /uploads/products/xxx.jpg）
     */
    public void deleteFile(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
            return; // 安全校验：只处理 uploads 目录下的文件
        }
        try {
            Path filePath = Paths.get(uploadPath, imageUrl.substring("/uploads/".length()));
            Files.deleteIfExists(filePath);
            log.info("已删除文件: {}", imageUrl);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", imageUrl, e); // best-effort，不抛异常
        }
    }
}
