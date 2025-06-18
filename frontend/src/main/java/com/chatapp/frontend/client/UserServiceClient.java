package com.chatapp.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.chatapp.frontend.dto.UserRegistrationDto;
import com.chatapp.frontend.dto.LoginRequest;
import com.chatapp.frontend.dto.ApiResponse;
import com.chatapp.frontend.dto.JwtResponseDto;
import com.chatapp.frontend.dto.UserResponseDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping(value = "/api/v1/auth/register", consumes = "application/json", produces = "application/json")
    ResponseEntity<ApiResponse> registerUser(@RequestBody UserRegistrationDto registrationDto);
    
    @PostMapping(value = "/api/v1/auth/login", consumes = "application/json", produces = "application/json")
    ResponseEntity<JwtResponseDto> loginUser(@RequestBody LoginRequest loginRequest);
    
    @GetMapping(value = "/api/v1/users/search", produces = "application/json")
    ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String query, @RequestParam Long currentUserId, @RequestHeader("Authorization") String authorization);
    
    @GetMapping(value = "/api/v1/users/{userId}/basic", produces = "application/json")
    ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId);
    
    @GetMapping(value = "/api/v1/users/{userId}", produces = "application/json")
    ResponseEntity<UserResponseDto> getUserByIdWithAuth(@PathVariable Long userId, @RequestHeader("Authorization") String authorization);
    
    @PostMapping(value = "/api/v1/users/{userId}/status", produces = "application/json")
    ResponseEntity<UserResponseDto> updateUserStatus(@PathVariable Long userId, @RequestParam String status);
    
    @PostMapping(value = "/api/v1/users/{userId}/status", produces = "application/json")
    ResponseEntity<UserResponseDto> updateUserStatusWithAuth(@PathVariable Long userId, @RequestParam String status, @RequestHeader("Authorization") String authorization);
} 