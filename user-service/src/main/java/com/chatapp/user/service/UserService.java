package com.chatapp.user.service;

import com.chatapp.user.dto.JwtResponseDto;
import com.chatapp.user.dto.UserLoginRequestDto;
import com.chatapp.user.dto.UserRegisterRequestDto;
import com.chatapp.user.dto.UserResponseDto;
import com.chatapp.user.model.User;

import java.util.List;

public interface UserService {
    UserResponseDto registerUser(UserRegisterRequestDto registerDto);
    JwtResponseDto authenticateUser(UserLoginRequestDto loginDto);
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByUsername(String username);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(Long id, UserResponseDto userDto);
    UserResponseDto updateUserStatus(Long id, User.UserStatus status);
    void deleteUser(Long id);
    boolean existsByUsername(String username);
    List<UserResponseDto> searchUsers(String query);
    List<UserResponseDto> searchUsers(String query, Long currentUserId);
} 