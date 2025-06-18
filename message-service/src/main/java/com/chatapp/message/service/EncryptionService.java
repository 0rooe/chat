package com.chatapp.message.service;

/**
 * 加密服务接口
 */
public interface EncryptionService {
    
    /**
     * 使用RSA公钥加密内容
     * 
     * @param content 待加密内容
     * @param publicKey RSA公钥
     * @return 加密后的内容
     */
    String encrypt(String content, String publicKey);
    
    /**
     * 使用RSA私钥解密内容
     * 
     * @param encryptedContent 已加密内容
     * @param privateKey RSA私钥
     * @return 解密后的内容
     */
    String decrypt(String encryptedContent, String privateKey);
    
    /**
     * 生成RSA密钥对（包含公钥和私钥）
     * 
     * @return JSON格式的密钥对，包含publicKey和privateKey字段
     */
    String generateKeyPair();
} 