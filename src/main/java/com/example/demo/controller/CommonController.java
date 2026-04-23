package com.example.demo.controller;

import com.example.demo.constant.MessageConstant;
import com.example.demo.pojo.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/common")
@Tag(name = "通用接口", description = "文件上传等通用功能")
@Slf4j
@CrossOrigin
public class CommonController {

    /**
     * 文件上传
     * @param file 待上传的文件
     * @return 文件访问路径
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传", description = "上传文件并返回本地访问路径")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        log.info("文件上传：{}", file.getOriginalFilename());

        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀名
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 构造新文件名 (UUID)
            String objectName = UUID.randomUUID().toString() + extension;

            // 获取项目根目录下的 uploads/products 目录
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + File.separator + "uploads" + File.separator + "products" + File.separator;
            
            // 确保目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            file.transferTo(new File(uploadDir + objectName));

            // 返回文件的网络访问路径
            String filePath = "/uploads/products/" + objectName;
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
