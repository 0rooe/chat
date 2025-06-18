package com.chatapp.message.controller;

import com.chatapp.message.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/encryption")
@RequiredArgsConstructor
@Slf4j
public class EncryptionController {

    private final EncryptionService encryptionService;

    @GetMapping("/keys")
    public ResponseEntity<String> generateKeyPair() {
        log.info("生成新的密钥对");
        String keyPair = encryptionService.generateKeyPair();
        return ResponseEntity.ok(keyPair);
    }
} 