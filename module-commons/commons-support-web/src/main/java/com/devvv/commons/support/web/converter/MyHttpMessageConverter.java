package com.devvv.commons.support.web.converter;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.context.ClientInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by WangSJ on 2024/01/16
 */
@Slf4j
public class MyHttpMessageConverter extends AbstractHttpMessageConverter<Object>  {

    public MyHttpMessageConverter() {
        super();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        mediaTypes.add(new MediaType("application", "*+json"));
        mediaTypes.add(MediaType.TEXT_XML);
        mediaTypes.add(MediaType.TEXT_PLAIN);
        mediaTypes.add(MediaType.TEXT_HTML);
        this.setSupportedMediaTypes(mediaTypes);
    }


    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    // region -------------------------------------------- 读取消息 --------------------------------------------
    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        try {
            InputStream in = inputMessage.getBody();
            String requestBody = IoUtil.read(in, StandardCharsets.UTF_8);
            // 解密
            requestBody = ClientInfoUtil.decryptBody(requestBody);
            BusiContextHolder.setRequestBody(requestBody);
            return JSONObject.parseObject(requestBody, clazz);
        } catch (IOException | JSONException e) {
            log.error("参数解析失败！ body:{}  targetClass:{}", BusiContextUtil.getContext().getRequestBody(), clazz);
            throw new BusiException("参数解析失败！", "参数有误");
        }

    }
    // endregion


    // region -------------------------------------------- 写出消息 --------------------------------------------
    @Override
    protected void writeInternal(Object obj, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        BusiContextHolder.setResult(obj);
        try {
            String jsonStr = JSONObject.toJSONString(obj, JSONWriter.Feature.WriteLongAsString);
            // 加密
            jsonStr = ClientInfoUtil.encryptBody(jsonStr);
            IoUtil.write(outputMessage.getBody(), true, jsonStr.getBytes(StandardCharsets.UTF_8));
        } catch (Throwable e) {
            log.error("接口结果写出异常！ obj:{}", obj, e);
        }
    }
    // endregion
}
