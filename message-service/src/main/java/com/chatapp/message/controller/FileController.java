package com.chatapp.message.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    static {
        // 创建上传目录
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            log.error("创建上传目录失败", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-ID") Long userId) {
        
        if (file.isEmpty()) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "文件不能为空");
            return ResponseEntity.badRequest().body(errorMap);
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "文件大小不能超过50MB");
            return ResponseEntity.badRequest().body(errorMap);
        }
        
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path targetPath = Paths.get(UPLOAD_DIR + filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 构建文件信息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("filename", filename);
            fileInfo.put("originalName", originalFilename);
            fileInfo.put("size", file.getSize());
            fileInfo.put("contentType", file.getContentType());
            fileInfo.put("url", "/api/v1/files/download/" + filename);
            fileInfo.put("uploadTime", System.currentTimeMillis());
            fileInfo.put("uploaderId", userId);
            
            log.info("用户 {} 上传文件成功: {}", userId, filename);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("success", true);
            resultMap.put("message", "文件上传成功");
            resultMap.put("file", fileInfo);
            return ResponseEntity.ok(resultMap);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "文件上传失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMap);
        }
    }
    
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] data = Files.readAllBytes(filePath);
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(data);
                    
        } catch (IOException e) {
            log.error("文件下载失败: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/image/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] data = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .body(data);
                    
        } catch (IOException e) {
            log.error("图片获取失败: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable String filename,
            @RequestHeader("X-User-ID") Long userId) {
        
        try {
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            
            if (!Files.exists(filePath)) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("error", "文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap);
            }
            
            Files.delete(filePath);
            log.info("用户 {} 删除文件: {}", userId, filename);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("success", true);
            resultMap.put("message", "文件删除成功");
            return ResponseEntity.ok(resultMap);
            
        } catch (IOException e) {
            log.error("文件删除失败: {}", filename, e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "文件删除失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMap);
        }
    }
} 