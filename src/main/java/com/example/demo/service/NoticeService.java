package com.example.demo.service;

import com.example.demo.pojo.dto.NoticeDTO;
import com.example.demo.pojo.entity.NoticeEntity;
import java.util.List;

public interface NoticeService {
    /**
     * 发布公告
     */
    void publish(NoticeDTO noticeDTO, Long adminId);

    /**
     * 更新公告
     */
    void update(NoticeDTO noticeDTO);

    /**
     * 删除公告
     */
    void delete(Long noticeId);

    /**
     * 获取所有公告 (管理端)
     */
    List<NoticeEntity> getAllNotices();

    /**
     * 获取可见公告 (用户端)
     */
    List<NoticeEntity> getVisibleNotices(Long userId, Integer userType);
}
