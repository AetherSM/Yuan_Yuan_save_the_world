package com.example.demo.service.impl;

import com.example.demo.mapper.NoticeMapper;
import com.example.demo.pojo.dto.NoticeDTO;
import com.example.demo.pojo.entity.NoticeEntity;
import com.example.demo.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public void publish(NoticeDTO noticeDTO, Long adminId) {
        NoticeEntity notice = new NoticeEntity();
        notice.setTitle(noticeDTO.getTitle());
        notice.setContent(noticeDTO.getContent());
        notice.setVisibility(noticeDTO.getVisibility());
        notice.setTargetTypes(noticeDTO.getTargetTypes());
        notice.setTargetUsers(noticeDTO.getTargetUsers());
        notice.setAdminId(adminId);
        noticeMapper.insert(notice);
    }

    @Override
    public void update(NoticeDTO noticeDTO) {
        NoticeEntity notice = new NoticeEntity();
        notice.setNoticeId(noticeDTO.getNoticeId());
        notice.setTitle(noticeDTO.getTitle());
        notice.setContent(noticeDTO.getContent());
        notice.setVisibility(noticeDTO.getVisibility());
        notice.setTargetTypes(noticeDTO.getTargetTypes());
        notice.setTargetUsers(noticeDTO.getTargetUsers());
        noticeMapper.update(notice);
    }

    @Override
    public void delete(Long noticeId) {
        noticeMapper.deleteById(noticeId);
    }

    @Override
    public List<NoticeEntity> getAllNotices() {
        return noticeMapper.findAll();
    }

    @Override
    public List<NoticeEntity> getVisibleNotices(Long userId, Integer userType) {
        return noticeMapper.findVisibleNotices(userId, userType);
    }
}
