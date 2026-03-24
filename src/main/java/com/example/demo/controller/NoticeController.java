package com.example.demo.controller;

import com.example.demo.context.BaseContext;
import com.example.demo.pojo.dto.NoticeDTO;
import com.example.demo.pojo.entity.NoticeEntity;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Tag(name = "系统公告接口")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @PostMapping("/admin/notices/publish")
    @Operation(summary = "发布公告(管理员)")
    public Result<String> publish(@RequestBody NoticeDTO noticeDTO) {
        Long adminId = BaseContext.getCurrentId();
        noticeService.publish(noticeDTO, adminId);
        return Result.success("发布成功");
    }

    @PutMapping("/admin/notices/update")
    @Operation(summary = "更新公告(管理员)")
    public Result<String> update(@RequestBody NoticeDTO noticeDTO) {
        noticeService.update(noticeDTO);
        return Result.success("更新成功");
    }

    @DeleteMapping("/admin/notices/{id}")
    @Operation(summary = "删除公告(管理员)")
    public Result<String> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/admin/notices/all")
    @Operation(summary = "获取所有公告(管理员)")
    public Result<List<NoticeEntity>> getAllForAdmin() {
        return Result.success(noticeService.getAllNotices());
    }

    @GetMapping("/api/notices/visible")
    @Operation(summary = "获取可见公告(用户/跑腿/商家)")
    public Result<List<NoticeEntity>> getVisibleNotices(@RequestParam Integer userType) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(noticeService.getVisibleNotices(userId, userType));
    }
}
