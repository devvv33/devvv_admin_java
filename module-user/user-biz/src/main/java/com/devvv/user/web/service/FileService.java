package com.devvv.user.web.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.user.web.models.form.UploadForm;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Create by WangSJ on 2024/12/05
 */
@Slf4j
@Service
public class FileService {

    @Resource
    private FileStorageService fileStorageService;

    private static final Set<String> IMAGE_EXTENSIONS = Sets.newHashSet("jpg", "jpeg", "png", "gif", "bmp");

    /**
     * 上传文件
     */
    public String uploadFile(UploadForm form) {
        Assert.notNull(form);
        Assert.notNull(form.getFile(), "文件不能为空");
        Long userId = BusiContextUtil.getUserId();

        // 检查是否为图片
        Boolean isImage = Opt.ofBlankAble(form.getFile().getOriginalFilename())
                .map(FileUtil::extName)
                .map(String::toLowerCase)
                .map(IMAGE_EXTENSIONS::contains)
                .orElse(false);

        // 图片上传
        FileInfo info = fileStorageService.of(form.getFile())
                .setPath(StrUtil.format("user/{}/",userId))
                .image(isImage, img -> img.scale(0.8F).outputQuality(0.6F))     // 图片压缩
                .upload();
        return info.getUrl();
    }

    /**
     * 上传临时文件
     */
    public String uploadTempFile(UploadForm form) {
        Assert.notNull(form);
        Assert.notNull(form.getFile(), "文件不能为空");
        Long userId = BusiContextUtil.getUserId();

        // 检查是否为图片
        Boolean isImage = Opt.ofBlankAble(form.getFile().getOriginalFilename())
                .map(FileUtil::extName)
                .map(String::toLowerCase)
                .map(IMAGE_EXTENSIONS::contains)
                .orElse(false);

        // 图片上传
        FileInfo info = fileStorageService.of(form.getFile())
                .setPath(StrUtil.format("tmp/{}/", userId))
                .image(isImage, img -> img.scale(0.8F).outputQuality(0.6F))     // 图片压缩
                .upload();
        return info.getUrl();
    }
}
