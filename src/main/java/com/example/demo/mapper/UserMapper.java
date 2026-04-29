package com.example.demo.mapper;


import com.example.demo.pojo.dto.UserDTO;
import com.example.demo.pojo.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 用户注册
     */
    void register(UserDTO userDTO);

    /**
     * 根据手机号查询用户
     */
    UserEntity findByPhone(String phone);

    /**
     * 根据用户ID查询用户
     */
    UserEntity findById(Long userId);

    /**
     * 查询所有用户列表
     */
    List<UserEntity> findAllUsers();

    /**
     * 更新余额
     */
    void updateBalance(Long userId, java.math.BigDecimal newBalance);

    List<UserEntity> search(String phone, String nickname, Integer userType, Integer status);

    List<UserEntity> searchByKeyword(String keyword, Integer userType, Integer status);

    void updateStatus(Long userId, Integer status);

    void updateUserType(Long userId, Integer userType);
}
