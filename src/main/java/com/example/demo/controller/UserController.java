package com.example.demo.controller;

import com.example.demo.pojo.dto.LoginDTO;
import com.example.demo.pojo.dto.LoginResponseDTO;
import com.example.demo.pojo.dto.UserDTO;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "用户认证", description = "用户注册、登录及信息管理接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @return 成功与否
     */
    @GetMapping("/send-code")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送注册/登录验证码")
    public Result<String> sendCode(@RequestParam String email) {
        try {
            userService.sendVerificationCode(email);
            return Result.success("验证码已发送，请查收");
        } catch (Exception e) {
            return Result.error("发送失败：" + e.getMessage());
        }
    }

    /**
     * 注册用户接口
     * @param userDTO 用户注册信息
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public Result<UserEntity> register(@RequestBody UserDTO userDTO) {
        try {
            UserEntity user = userService.register(userDTO);
            return Result.success(user);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询用户列表接口
     * @return 所有用户列表
     */
    @GetMapping("/query")
    @Operation(summary = "查询用户列表", description = "获取所有用户列表（不含密码）")
    public Result<List<UserEntity>> query() {
        try {
            List<UserEntity> users = userService.findAllUsers();
            // 移除密码信息
            users.forEach(user -> user.setPassword(null));
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据手机号查询用户接口
     * @param phone 手机号
     * @return 用户信息
     */
    @GetMapping("/query/{phone}")
    @Operation(summary = "根据手机号查询", description = "根据手机号获取用户信息")
    public Result<UserEntity> queryByPhone(@PathVariable String phone) {
        try {
            UserEntity user = userService.findByPhone(phone);
            if (user == null) {
                return Result.error("用户不存在");
            }
            user.setPassword(null); // 不返回密码
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录接口
     * @param loginDTO 登录信息（账号密码）
     * @return 登录响应（包含Token）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并获取Token")
    public Result<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        try {
            LoginResponseDTO response = userService.login(loginDTO);
            if (response == null) {
                return Result.error("登录失败：返回数据为空");
            }
            if (response.getToken() == null || response.getUser() == null) {
                return Result.error("登录失败：token或用户信息为空");
            }
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户信息接口（需要JWT认证）
     * @param userId 从Token中解析出的用户ID
     * @return 当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的详细信息")
    public Result<UserEntity> profile(@RequestAttribute("userId") Long userId) {
        try {
            UserEntity user = userService.findById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            user.setPassword(null); // 不返回密码
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 修改用户信息接口
     * @param userDTO 用户更新信息
     * @return 更新后的用户信息
     */
    @PutMapping("/update")
    @Operation(summary = "修改用户信息", description = "更新当前用户的个人信息")
    public Result<UserEntity> update(UserDTO userDTO) {
        return Result.success(new UserEntity());
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "退出登录并清理客户端令牌")
    public Result<String> logout(HttpServletRequest request) {
        return Result.success("已退出");
    }

}
