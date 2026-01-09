package com.devvv.user.web.models.form;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Create by WangSJ on 2024/12/05
 */
@Data
public class UploadForm {

    private MultipartFile file;
}
