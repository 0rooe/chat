package com.chatapp.user.controller;

import com.chatapp.user.dto.UserResponseDto;
import com.chatapp.user.model.User;
import com.chatapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("获取所有用户");
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("通过ID获取用户: {}", id);
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        log.info("通过用户名获取用户: {}", username);
        UserResponseDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(
            @RequestParam String query,
            @RequestParam(required = false) Long currentUserId) {
        log.info("搜索用户，查询条件: {}, 当前用户ID: {}", query, currentUserId);
        List<UserResponseDto> users = userService.searchUsers(query, currentUserId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserResponseDto userDto) {
        log.info("更新用户信息: {}", id);
        UserResponseDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponseDto> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> statusRequest) {
        User.UserStatus status = User.UserStatus.valueOf(statusRequest.get("status"));
        log.info("更新用户状态: {}, 新状态: {}", id, status);
        UserResponseDto updatedUser = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<UserResponseDto> updateUserStatusByParam(@PathVariable Long id, @RequestParam String status) {
        User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
        log.info("更新用户状态(参数方式): {}, 新状态: {}", id, userStatus);
        UserResponseDto updatedUser = userService.updateUserStatus(id, userStatus);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        log.info("检查用户名是否存在: {}", username);
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/{userId}/basic")
    public ResponseEntity<UserResponseDto> getUserBasicInfo(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-ID", required = false) Long requestUserId) {
        log.info("【调试】获取用户基本信息, 用户ID: {}, 请求者ID: {}", userId, requestUserId);
        try {
            UserResponseDto user = userService.getUserById(userId);
            log.info("【调试】成功获取到用户信息: id={}, username={}, nickname={}, status={}", 
                    user.getId(), user.getUsername(), user.getNickname(), user.getStatus());
            
            log.info("【调试】返回用户基本信息: {}", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("【错误】获取用户信息异常: {}, 异常类型: {}", e.getMessage(), e.getClass().getName(), e);
            // 返回默认信息
            UserResponseDto defaultUser = new UserResponseDto();
            defaultUser.setId(userId);
            defaultUser.setUsername("unknown");
            defaultUser.setNickname("未知用户");
            defaultUser.setAvatar("");
            defaultUser.setStatus(User.UserStatus.OFFLINE);
            
            log.info("【调试】返回默认用户信息: {}", defaultUser);
            return ResponseEntity.ok(defaultUser);
        }
    }
} 