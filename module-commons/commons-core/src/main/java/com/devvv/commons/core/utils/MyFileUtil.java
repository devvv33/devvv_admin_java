package com.devvv.commons.core.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import com.devvv.commons.common.utils.MyOpt;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * Create by WangSJ on 2025/04/16
 */
public class MyFileUtil {

    public static final String accessPath = SpringUtil.getProperty("file.access_path", "/cmsFile");
    public static final String uploadDir = SpringUtil.getProperty("file.upload_dir", "upload");

    /**
     * 保存文件
     */
    public static String saveUploadFile(InputStream inputStream, String suffix) {
        Pair<File, String> filePath = buildFilePath(suffix);
        FileUtil.writeFromStream(inputStream, filePath.getKey());
        return filePath.getValue();
    }

    /**
     * base64格式文件
     */
    public static String saveBase64File(String base64Str, String suffix) {
        byte[] bytes = MyOpt.ofBlankAble(base64Str)
                .tryMap(str -> {
                    if (str.startsWith("data:")) {
                        int index = str.indexOf("base64,");
                        if (index > 0) {
                            return str.substring(index + 7);
                        }
                    }
                    return str;
                })
                .tryMap(Base64::decode)
                .orElse(null);
        if (bytes == null) {
            return null;
        }
        Pair<File, String> filePath = buildFilePath(suffix);
        FileUtil.writeBytes(bytes, filePath.getKey());
        return filePath.getValue();
    }


    /**
     * 根据网络路径，下载文件，并返回可访问路径
     */
    public static String saveDownloadFile(String url) {
        if (StrUtil.isBlank(url) || !url.startsWith("http")) {
            return null;
        }
        // 构建子目录路径，例如：2504/uuid/
        String localFileDir = StrUtil.format(DateUtil.format(new Date(), "/yyMM/dd/HHmmss_{}/"), IdUtil.fastSimpleUUID());
        String basePath = System.getProperty("user.dir") + File.separator + uploadDir;
        File localDir = new File(basePath, localFileDir);
        localDir.mkdirs();
        // 下载时，使用实际文件名
        return Opt.ofTry(() -> HttpUtil.downloadFileFromUrl(url, localDir))
                .map(File::getAbsolutePath)
                .map(path -> path.substring(basePath.length()))
                .map(path -> path.replace("\\", "/"))
                .map(path -> accessPath + path)
                .orElse(null);
    }


    /**
     * 构建上传文件的 文件路径
     * @return
     *  key: 本地文件对象，可以直接写入文件、可以获取本地文件路径
     *  value: 文件访问路径
     */
    public static Pair<File, String> buildFilePath(String suffix) {
        String localFileName = StrUtil.format(DateUtil.format(new Date(), "/yyMM/dd/HHmmss_{}.{}"), IdUtil.fastSimpleUUID(), suffix);
        String basePath = System.getProperty("user.dir") + File.separator + uploadDir;
        File localFile = new File(basePath, localFileName);
        FileUtil.mkParentDirs(localFile);
        return Pair.of(localFile, accessPath + localFileName);
    }

}
