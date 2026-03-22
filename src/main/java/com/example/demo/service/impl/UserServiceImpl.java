package com.example.demo.service.impl;

import com.example.demo.constant.JwtClaimsConstant;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.dto.LoginDTO;
import com.example.demo.pojo.dto.LoginResponseDTO;
import com.example.demo.pojo.dto.UserDTO;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.properties.JwtProperties;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import com.example.demo.utils.MD5Util;
import com.example.demo.utils.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String VERIFY_CODE_KEY_PREFIX = "user:verify_code:";
    private static final long EXPIRE_DURATION = 5; // 5分钟有效

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEntity register(UserDTO userDTO) {
        // 1. 参数验证
        if (!StringUtils.hasText(userDTO.getPhone())) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (!StringUtils.hasText(userDTO.getEmail())) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (!StringUtils.hasText(userDTO.getCode())) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        if (!StringUtils.hasText(userDTO.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!StringUtils.hasText(userDTO.getNickname())) {
            throw new IllegalArgumentException("昵称不能为空");
        }

        // 验证验证码 (使用 Redis)
        String redisKey = VERIFY_CODE_KEY_PREFIX + userDTO.getEmail();
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (storedCode == null) {
            throw new IllegalArgumentException("验证码已过期或未获取");
        }
        if (!storedCode.equals(userDTO.getCode())) {
            throw new IllegalArgumentException("验证码错误");
        }
        
        // 验证成功后删除验证码
        redisTemplate.delete(redisKey);
        
        // 验证手机号格式（简单验证，11位数字）
        if (!userDTO.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        
        // 验证密码长度
        if (userDTO.getPassword().length() < 6 || userDTO.getPassword().length() > 20) {
            throw new IllegalArgumentException("密码长度必须在6-20位之间");
        }

        // 2. 检查手机号是否已存在
        UserEntity existingUser = userMapper.findByPhone(userDTO.getPhone());
        if (existingUser != null) {
            throw new IllegalArgumentException("该手机号已被注册");
        }

        // 3. 密码加密（MD5）
        String encodedPassword = MD5Util.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        // 4. 设置默认值
        if (!StringUtils.hasText(userDTO.getAvatar())) {
            userDTO.setAvatar("https://via.placeholder.com/150"); // 默认头像
        }
        if (userDTO.getGender() == null) {
            userDTO.setGender(0); // 默认未知（0-未知，1-男，2-女）
        }
        
        // 5. 设置其他默认值
        LocalDateTime now = LocalDateTime.now();
        userDTO.setCreateTime(now);
        userDTO.setUpdateTime(now);
        if (userDTO.getDormitory() == null) {
            userDTO.setDormitory("");
        }
        if (userDTO.getUserType() == null) {
            userDTO.setUserType(1);
        }
        if (userDTO.getStatus() == null) {
            userDTO.setStatus(1);
        }
        if (userDTO.getBalance() == null) {
            userDTO.setBalance(BigDecimal.ZERO);
        }
        if (userDTO.getCreditScore() == null) {
            userDTO.setCreditScore(100);
        }

        // 6. 插入数据库
        userMapper.register(userDTO);
        
        // 7. 查询刚注册的用户信息（不包含密码）
        UserEntity newUser = userMapper.findByPhone(userDTO.getPhone());
        if (newUser != null) {
            newUser.setPassword(null); // 不返回密码
        }
        return newUser;
    }

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        // 1. 参数验证
        if (!StringUtils.hasText(loginDTO.getPhone())) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (!StringUtils.hasText(loginDTO.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // 2. 根据手机号查询用户
        UserEntity user = userMapper.findByPhone(loginDTO.getPhone());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在，请先注册");
        }

        // 3. 验证密码（MD5加密后比较）
        String encodedPassword = MD5Util.encode(loginDTO.getPassword());
        if (user.getPassword() == null || !encodedPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("手机号或密码错误");
        }

        // 验证角色
        if (loginDTO.getUserType() != null && !loginDTO.getUserType().equals(user.getUserType())) {
            throw new IllegalArgumentException("请选择正确的角色登录");
        }

        // 4. 生成JWT token
        String token;
        Map<String, Object> claims = new HashMap<>();
        /*把员工的id作为值,empId作为键,存储到jwt的声明中(claims)*/
        claims.put(JwtClaimsConstant.EMP_ID, user.getUserId());
        claims.put(JwtClaimsConstant.USERNAME,user.getNickname());
        try {
             token = JwtUtil.createJWT(
                    jwtProperties.getAdminSecretKey(),
                    jwtProperties.getAdminTtl(),
                    claims);
        } catch (Exception e) {
            throw new RuntimeException("生成token失败：" + e.getMessage(), e);
        }

        // 5. 创建用户信息副本（避免修改原对象）
        UserEntity userInfo = new UserEntity();
        userInfo.setUserId(user.getUserId());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setStudentId(user.getStudentId());
        userInfo.setSchool(user.getSchool());
        userInfo.setDormitory(user.getDormitory());
        userInfo.setBalance(user.getBalance());
        userInfo.setCreditScore(user.getCreditScore());
        userInfo.setUserType(user.getUserType());
        userInfo.setStatus(user.getStatus());
        userInfo.setCreateTime(user.getCreateTime());
        userInfo.setUpdateTime(user.getUpdateTime());
        // 不设置密码

        // 6. 返回登录响应
        return new LoginResponseDTO(token, userInfo);
    }

    @Override
    public UserEntity findByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public UserEntity findById(Long userId) {
        return userMapper.findById(userId);
    }

    @Override
    public List<UserEntity> findAllUsers() {
        return userMapper.findAllUsers();
    }

    @Override
    public List<UserEntity> searchUsers(String phone, String nickname, Integer userType, Integer status) {
        return userMapper.search(phone, nickname, userType, status);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        userMapper.updateStatus(userId, status);
    }

    @Override
    public void updateUserType(Long userId, Integer userType) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        Integer currentType = user.getUserType();
        // 不允许通过此接口直接修改管理员角色
        if (currentType != null && currentType == 0 && (userType == null || userType != 0)) {
            throw new IllegalArgumentException("不能直接修改管理员的角色，请通过审批流程处理");
        }
        // 不允许直接把普通用户升为管理员
        if ((currentType == null || currentType != 0) && userType != null && userType == 0) {
            throw new IllegalArgumentException("不能直接将用户设置为管理员，请通过角色申请审批");
        }
        userMapper.updateUserType(userId, userType);
    }

    @Override
    public void sendVerificationCode(String email) {
        // 1. 生成6位数字验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 2. 存储验证码 (使用 Redis，有效期 5 分钟)
        String redisKey = VERIFY_CODE_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, code, EXPIRE_DURATION, TimeUnit.MINUTES);
        
        // 3. 发送邮件
        String subject = "【校园购】注册验证码";
        String content = "您的验证码为：" + code + "，有效期5分钟。请勿泄露给他人。";
        mailUtil.sendTextMail(subject, content, Collections.singletonList(email));
    }
}
