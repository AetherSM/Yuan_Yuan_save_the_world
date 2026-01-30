package com.example.demo.service;

import com.example.demo.pojo.dto.LoginDTO;
import com.example.demo.pojo.dto.LoginResponseDTO;
import com.example.demo.pojo.dto.UserDTO;
import com.example.demo.pojo.entity.UserEntity;

import java.util.List;

public interface UserService {
    /**
     * 用户注册
     * @param userDTO 用户注册信息
     * @return 注册后的用户信息
     */
    UserEntity register(UserDTO userDTO);

    /**
     * 用户登录
     * @param loginDTO 登录信息（手机号和密码）
     * @return 登录响应（包含token和用户信息）
     */
    LoginResponseDTO login(LoginDTO loginDTO);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    UserEntity findByPhone(String phone);

    /**
     * 根据用户ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    UserEntity findById(Long userId);

    /**
     * 查询所有用户列表
     * @return 用户列表
     */
    List<UserEntity> findAllUsers();

    List<UserEntity> searchUsers(String phone, String nickname, Integer userType, Integer status);

    void updateUserStatus(Long userId, Integer status);

    void updateUserType(Long userId, Integer userType);
}
