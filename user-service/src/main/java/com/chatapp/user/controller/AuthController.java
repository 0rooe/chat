package com.chatapp.user.controller;

import com.chatapp.user.dto.JwtResponseDto;
import com.chatapp.user.dto.UserLoginRequestDto;
import com.chatapp.user.dto.UserRegisterRequestDto;
import com.chatapp.user.dto.UserResponseDto;
import com.chatapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegisterRequestDto registerDto) {
        log.info("【调试】注册新用户: {}", registerDto);
        try {
            UserResponseDto user = userService.registerUser(registerDto);
            log.info("【调试】用户注册成功: {}", user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("【错误】用户注册失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> loginUser(@Valid @RequestBody UserLoginRequestDto loginDto) {
        log.info("【调试】用户登录: {}", loginDto);
        try {
            JwtResponseDto response = userService.authenticateUser(loginDto);
            log.info("【调试】用户登录成功，返回响应: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【错误】用户登录失败: {}", e.getMessage(), e);
            throw e;
        }
    }
} 