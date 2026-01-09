package com.devvv.user.web.controller;

import com.devvv.commons.common.response.Result;
import com.devvv.user.web.models.form.UploadForm;
import com.devvv.user.web.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by WangSJ on 2024/12/05
 */
@Tag(name = "001-文件处理")
@RestController
@RequestMapping("/api/user/file")
public class FileController {

    @Resource
    private FileService fileService;

    @Operation(summary = "1-上传文件")
    @PostMapping("/uploadFile")
    public Result<String> uploadFile(UploadForm form) {
        return Result.success(fileService.uploadFile(form));
    }

    @Operation(summary = "2-上传临时文件，到期后会自动删除")
    @PostMapping("/uploadTempFile")
    public Result<String> uploadTempFile(UploadForm form) {
        return Result.success(fileService.uploadTempFile(form));
    }
}
