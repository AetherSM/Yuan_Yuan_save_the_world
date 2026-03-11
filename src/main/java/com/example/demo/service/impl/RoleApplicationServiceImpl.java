package com.example.demo.service.impl;

import com.example.demo.mapper.RoleApplicationMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.RoleApplication;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.service.RoleApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleApplicationServiceImpl implements RoleApplicationService {

    @Autowired
    private RoleApplicationMapper roleApplicationMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleApplication apply(Long userId, Integer targetType, String reason) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (targetType == null) {
            throw new IllegalArgumentException("申请角色不能为空");
        }
        if (!StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("申请理由不能为空");
        }
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (user.getUserType() != null && user.getUserType() == 0) {
            throw new IllegalArgumentException("管理员无需申请角色变更");
        }

        RoleApplication app = new RoleApplication();
        app.setUserId(userId);
        app.setCurrentType(user.getUserType());
        app.setTargetType(targetType);
        app.setReason(reason);
        app.setStatus(0);
        app.setCreateTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());
        roleApplicationMapper.insert(app);
        return app;
    }

    @Override
    public List<RoleApplication> listMy(Long userId) {
        return roleApplicationMapper.listByUser(userId);
    }

    @Override
    public List<RoleApplication> adminSearch(Long userId, Integer targetType, Integer status) {
        return roleApplicationMapper.search(userId, targetType, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long appId, Long handlerId, String remark) {
        RoleApplication app = requireApp(appId);
        if (app.getStatus() != null && app.getStatus() != 0) {
            throw new IllegalArgumentException("该申请已处理");
        }
        UserEntity user = userMapper.findById(app.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 通过审批直接修改角色（这里允许升/降）
        userMapper.updateUserType(app.getUserId(), app.getTargetType());

        app.setStatus(1);
        app.setHandlerId(handlerId);
        app.setHandleRemark(StringUtils.hasText(remark) ? remark : "审批通过");
        app.setUpdateTime(LocalDateTime.now());
        roleApplicationMapper.updateStatus(app);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long appId, Long handlerId, String remark) {
        RoleApplication app = requireApp(appId);
        if (app.getStatus() != null && app.getStatus() != 0) {
            throw new IllegalArgumentException("该申请已处理");
        }
        app.setStatus(2);
        app.setHandlerId(handlerId);
        app.setHandleRemark(StringUtils.hasText(remark) ? remark : "审批拒绝");
        app.setUpdateTime(LocalDateTime.now());
        roleApplicationMapper.updateStatus(app);
    }

    private RoleApplication requireApp(Long id) {
        RoleApplication app = roleApplicationMapper.findById(id);
        if (app == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        return app;
    }
}

