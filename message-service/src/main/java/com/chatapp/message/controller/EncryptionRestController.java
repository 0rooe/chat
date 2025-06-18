package com.chatapp.message.controller;

import com.chatapp.message.service.EncryptionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/encryption")
@RequiredArgsConstructor
@Slf4j
public class EncryptionRestController {

    private final EncryptionService encryptionService;

    @PostMapping("/encrypt")
    public ResponseEntity<String> encrypt(@RequestBody EncryptRequest request) {
        log.info("加密内容请求");
        String encryptedContent = encryptionService.encrypt(request.getContent(), request.getPublicKey());
        return ResponseEntity.ok(encryptedContent);
    }

    @PostMapping("/decrypt")
    public ResponseEntity<String> decrypt(@RequestBody DecryptRequest request) {
        log.info("解密内容请求");
        String decryptedContent = encryptionService.decrypt(request.getContent(), request.getPrivateKey());
        return ResponseEntity.ok(decryptedContent);
    }
    
    @Data
    public static class EncryptRequest {
        private String content;
        private String publicKey;
    }
    
    @Data
    public static class DecryptRequest {
        private String content;
        private String privateKey;
    }
} 