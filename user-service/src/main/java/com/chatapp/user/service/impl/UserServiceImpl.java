package com.chatapp.user.service.impl;

import com.chatapp.user.client.RelationshipServiceClient;
import com.chatapp.user.dto.JwtResponseDto;
import com.chatapp.user.dto.UserLoginRequestDto;
import com.chatapp.user.dto.UserRegisterRequestDto;
import com.chatapp.user.dto.UserResponseDto;
import com.chatapp.user.exception.ResourceNotFoundException;
import com.chatapp.user.exception.UserAlreadyExistsException;
import com.chatapp.user.model.User;
import com.chatapp.user.repository.UserRepository;
import com.chatapp.user.security.jwt.JwtUtils;
import com.chatapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RelationshipServiceClient relationshipServiceClient;

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRegisterRequestDto registerDto) {
        log.info("【调试】开始注册用户: {}", registerDto);
        
        // 检查用户名是否已存在
        boolean usernameExists = userRepository.existsByUsername(registerDto.getUsername());
        log.info("【调试】用户名是否存在: {}", usernameExists);
        if (usernameExists) {
            log.warn("【警告】用户名已被使用: {}", registerDto.getUsername());
            throw new UserAlreadyExistsException("用户名已被使用");
        }



        // 创建新用户
        User user = User.builder()
                .username(registerDto.getUsername())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .nickname(registerDto.getNickname())
                .status(User.UserStatus.OFFLINE)
                .createTime(LocalDateTime.now())
                .build();
        log.info("【调试】创建的用户对象: {}", user);

        User savedUser = userRepository.save(user);
        log.info("【调试】用户注册成功: {}", savedUser);
        
        UserResponseDto responseDto = UserResponseDto.fromUser(savedUser);
        log.info("【调试】返回的响应DTO: {}", responseDto);
        return responseDto;
    }

    @Override
    public JwtResponseDto authenticateUser(UserLoginRequestDto loginDto) {
        log.info("【调试】开始用户认证: {}", loginDto);
        
        // 先检查用户是否存在
        Optional<User> userCheck = userRepository.findByUsername(loginDto.getUsername());
        if (userCheck.isPresent()) {
            User user = userCheck.get();
            log.info("【调试】找到用户: {}, 密码长度: {}", user.getUsername(), user.getPassword().length());
            boolean passwordMatches = passwordEncoder.matches(loginDto.getPassword(), user.getPassword());
            log.info("【调试】密码匹配结果: {}", passwordMatches);
        } else {
            log.warn("【警告】用户不存在: {}", loginDto.getUsername());
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
            log.info("【调试】认证成功: {}", authentication.getName());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            log.info("【调试】生成的JWT令牌: {}", jwt);

            User user = userRepository.findByUsername(loginDto.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("未找到用户"));
            log.info("【调试】找到的用户: {}", user);
            
            // 更新最后登录时间和状态
            user.setLastLoginTime(LocalDateTime.now());
            user.setStatus(User.UserStatus.ONLINE);
            User updatedUser = userRepository.save(user);
            log.info("【调试】更新后的用户: {}", updatedUser);
            
            log.info("【调试】用户登录成功: {}", user.getUsername());

            JwtResponseDto response = JwtResponseDto.builder()
                    .token(jwt)
                    .id(user.getId())
                    .username(user.getUsername())
                    .build();
            log.info("【调试】返回的JWT响应: {}", response);
            return response;
        } catch (Exception e) {
            log.error("【错误】用户认证失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        log.info("【调试】根据ID查询用户: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的用户"));
        log.info("【调试】找到的用户: {}", user);
        UserResponseDto responseDto = UserResponseDto.fromUser(user);
        log.info("【调试】返回的用户DTO: {}", responseDto);
        return responseDto;
    }

    @Override
    public UserResponseDto getUserByUsername(String username) {
        log.info("【调试】根据用户名查询用户: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户名为" + username + "的用户"));
        log.info("【调试】找到的用户: {}", user);
        UserResponseDto responseDto = UserResponseDto.fromUser(user);
        log.info("【调试】返回的用户DTO: {}", responseDto);
        return responseDto;
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("【调试】查询所有用户");
        List<User> users = userRepository.findAll();
        log.info("【调试】找到的用户数量: {}", users.size());
        List<UserResponseDto> responseDtos = users.stream()
                .map(UserResponseDto::fromUser)
                .collect(Collectors.toList());
        log.info("【调试】返回的用户DTO列表大小: {}", responseDtos.size());
        return responseDtos;
    }

    @Override
    public List<UserResponseDto> searchUsers(String query, Long currentUserId) {
        log.info("【调试】搜索用户，查询条件: {}, 当前用户ID: {}", query, currentUserId);
        List<User> users = userRepository.searchUsers(query);
        log.info("【调试】找到的匹配用户数量: {}", users.size());
        
        List<UserResponseDto> responseDtos = users.stream()
                .map(UserResponseDto::fromUser)
                .collect(Collectors.toList());
        
        // 如果提供了当前用户ID，则查询好友关系状态
        if (currentUserId != null) {
            for (UserResponseDto userDto : responseDtos) {
                try {
                    // 排除自己
                    if (!userDto.getId().equals(currentUserId)) {
                        // 查询关系状态
                        Map<String, String> statusMap = relationshipServiceClient.getFriendshipStatus(currentUserId, userDto.getId());
                        String status = statusMap.get("status");
                        
                        if (status != null) {
                            userDto.setRelationshipStatus(status);
                        }
                    }
                } catch (HttpClientErrorException e) {
                    log.warn("获取好友关系状态失败: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("处理用户关系信息时出错: {}", e.getMessage(), e);
                }
            }
        }
        
        log.info("【调试】返回的搜索结果DTO列表大小: {}", responseDtos.size());
        return responseDtos;
    }

    @Override
    public List<UserResponseDto> searchUsers(String query) {
        // 调用带currentUserId参数的方法，但传入null，表示不检查关系状态
        return searchUsers(query, null);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserResponseDto userDto) {
        log.info("【调试】更新用户信息, ID: {}, DTO: {}", id, userDto);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的用户"));
        log.info("【调试】找到的用户: {}", user);
        
        if (userDto.getNickname() != null) {
            log.info("【调试】更新昵称: {} -> {}", user.getNickname(), userDto.getNickname());
            user.setNickname(userDto.getNickname());
        }
        
        if (userDto.getAvatar() != null) {
            log.info("【调试】更新头像: {} -> {}", user.getAvatar(), userDto.getAvatar());
            user.setAvatar(userDto.getAvatar());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("【调试】用户信息已更新: {}", updatedUser);
        
        UserResponseDto responseDto = UserResponseDto.fromUser(updatedUser);
        log.info("【调试】返回的用户DTO: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public UserResponseDto updateUserStatus(Long id, User.UserStatus status) {
        log.info("【调试】更新用户状态, ID: {}, 状态: {}", id, status);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的用户"));
        log.info("【调试】找到的用户: {}", user);
        
        log.info("【调试】更新状态: {} -> {}", user.getStatus(), status);
        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        log.info("【调试】用户状态已更新: {}", updatedUser);
        
        UserResponseDto responseDto = UserResponseDto.fromUser(updatedUser);
        log.info("【调试】返回的用户DTO: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("【调试】删除用户, ID: {}", id);
        boolean exists = userRepository.existsById(id);
        log.info("【调试】用户是否存在: {}", exists);
        
        if (!exists) {
            log.warn("【警告】未找到要删除的用户, ID: {}", id);
            throw new ResourceNotFoundException("未找到ID为" + id + "的用户");
        }
        
        userRepository.deleteById(id);
        log.info("【调试】用户已删除, ID: {}", id);
    }

    @Override
    public boolean existsByUsername(String username) {
        log.info("【调试】检查用户名是否存在: {}", username);
        boolean exists = userRepository.existsByUsername(username);
        log.info("【调试】用户名是否存在结果: {}", exists);
        return exists;
    }


} 