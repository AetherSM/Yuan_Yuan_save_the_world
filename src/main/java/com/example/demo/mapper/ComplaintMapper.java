package com.example.demo.mapper;

import com.example.demo.pojo.entity.Complaint;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface ComplaintMapper {
    void insert(Complaint complaint);
    void update(Complaint complaint);
    Complaint findById(Long complaintId);
    List<Complaint> listByUser(Long userId);
    List<Complaint> listAll(Map<String, Object> params);
    
    @MapKey("status")
    List<Map<String, Object>> countByStatus();
}
