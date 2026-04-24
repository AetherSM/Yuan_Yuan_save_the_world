package com.example.demo.service.impl;

import com.example.demo.mapper.ComplaintMapper;
import com.example.demo.pojo.entity.Complaint;
import com.example.demo.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Override
    public void submitComplaint(Complaint complaint) {
        complaint.setStatus(0);
        complaintMapper.insert(complaint);
    }

    @Override
    public void resolveComplaint(Long complaintId, String result) {
        handleComplaint(complaintId, result, 2, "处理");
    }

    @Override
    public void rejectComplaint(Long complaintId, String result) {
        handleComplaint(complaintId, result, 3, "拒绝");
    }

    @Override
    public List<Complaint> listMyComplaints(Long userId) {
        return complaintMapper.listByUser(userId);
    }

    @Override
    public List<Complaint> listAllComplaints(Map<String, Object> params) {
        return complaintMapper.listAll(params);
    }

    @Override
    public List<Map<String, Object>> countComplaintsByStatus() {
        return complaintMapper.countByStatus();
    }

    private void handleComplaint(Long complaintId, String result, Integer targetStatus, String actionName) {
        Complaint complaint = complaintMapper.findById(complaintId);
        if (complaint == null) {
            throw new IllegalArgumentException("投诉不存在");
        }
        if (!StringUtils.hasText(result)) {
            throw new IllegalArgumentException("处理结果不能为空");
        }
        if (complaint.getStatus() != null && (complaint.getStatus() == 2 || complaint.getStatus() == 3)) {
            throw new IllegalStateException("投诉已处理，不能重复" + actionName);
        }
        complaint.setStatus(targetStatus);
        complaint.setResult(result.trim());
        complaintMapper.update(complaint);
    }
}
