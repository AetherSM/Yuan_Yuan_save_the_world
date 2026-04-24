package com.example.demo.controller;

import com.example.demo.pojo.entity.Complaint;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员投诉管理控制器
 */
@Tag(name = "管理员-投诉管理", description = "管理员对投诉进行管理的接口")
@RestController
@RequestMapping("/admin/complaints")
public class AdminComplaintController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * 查看所有投诉
     * @param status 投诉状态
     * @param orderType 订单类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 投诉列表
     */
    @Operation(summary = "查看所有投诉", description = "管理员查看平台所有投诉记录")
    @GetMapping
    public Result<List<Complaint>> listAllComplaints(
            @Parameter(description = "投诉状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "订单类型") @RequestParam(required = false) Integer orderType,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("orderType", orderType);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        List<Complaint> complaints = complaintService.listAllComplaints(params);
        return Result.success(complaints);
    }

    /**
     * 统计投诉状态
     * @return 状态统计
     */
    @Operation(summary = "统计投诉状态", description = "统计不同状态的投诉数量")
    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> countComplaintsByStatus() {
        List<Map<String, Object>> stats = complaintService.countComplaintsByStatus();
        return Result.success(stats);
    }

    /**
     * 处理投诉
     * @param complaintId 投诉ID
     * @param result 处理结果
     * @return 操作结果
     */
    @Operation(summary = "处理投诉", description = "管理员处理用户投诉")
    @PostMapping("/{id}/resolve")
    public Result<String> resolveComplaint(
            @PathVariable("id") Long complaintId,
            @Parameter(description = "处理结果/回复内容", required = true) @RequestParam String result) {
        complaintService.resolveComplaint(complaintId, result);
        return Result.success("处理完成");
    }

    /**
     * 拒绝投诉
     * @param complaintId 投诉ID
     * @param result 拒绝说明
     * @return 操作结果
     */
    @Operation(summary = "拒绝投诉", description = "管理员拒绝投诉并给出说明")
    @PostMapping("/{id}/reject")
    public Result<String> rejectComplaint(
            @PathVariable("id") Long complaintId,
            @Parameter(description = "拒绝说明", required = true) @RequestParam String result) {
        complaintService.rejectComplaint(complaintId, result);
        return Result.success("已拒绝投诉");
    }
}
