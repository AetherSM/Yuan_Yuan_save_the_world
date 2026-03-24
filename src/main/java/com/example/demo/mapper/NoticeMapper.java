package com.example.demo.mapper;

import com.example.demo.pojo.dto.NoticeDTO;
import com.example.demo.pojo.entity.NoticeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NoticeMapper {
    /**
     * 管理端: 获取所有公告
     */
    List<NoticeEntity> findAll();

    /**
     * 用户端: 根据条件获取可见公告
     * @param userId 用户ID
     * @param userType 用户类型
     */
    List<NoticeEntity> findVisibleNotices(@Param("userId") Long userId, @Param("userType") Integer userType);

    /**
     * 保存新公告
     */
    void insert(NoticeEntity notice);

    /**
     * 更新公告
     */
    void update(NoticeEntity notice);

    /**
     * 删除公告
     */
    void deleteById(Long noticeId);
}
