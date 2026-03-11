package com.example.demo.service;

import com.example.demo.pojo.entity.RoleApplication;

import java.util.List;

public interface RoleApplicationService {

    RoleApplication apply(Long userId, Integer targetType, String reason);

    List<RoleApplication> listMy(Long userId);

    List<RoleApplication> adminSearch(Long userId, Integer targetType, Integer status);

    void approve(Long appId, Long handlerId, String remark);

    void reject(Long appId, Long handlerId, String remark);
}

