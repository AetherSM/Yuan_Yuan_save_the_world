package com.example.demo.service.impl;

import com.example.demo.mapper.ComplaintMapper;
import com.example.demo.pojo.entity.Complaint;
import com.example.demo.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        Complaint c = complaintMapper.findById(complaintId);
        if (c != null) {
            c.setStatus(2);
            c.setResult(result);
            complaintMapper.update(c);
        }
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
}
