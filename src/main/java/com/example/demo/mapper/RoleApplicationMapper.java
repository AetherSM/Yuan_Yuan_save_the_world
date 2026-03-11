package com.example.demo.mapper;

import com.example.demo.pojo.entity.RoleApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleApplicationMapper {

    void insert(RoleApplication app);

    RoleApplication findById(@Param("id") Long id);

    List<RoleApplication> listByUser(@Param("userId") Long userId);

    List<RoleApplication> search(@Param("userId") Long userId,
                                 @Param("targetType") Integer targetType,
                                 @Param("status") Integer status);

    void updateStatus(RoleApplication app);
}

