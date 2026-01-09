package com.devvv.commons.feign.config;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by WangSJ on 2024/06/26
 */
@Slf4j
public class FastJson2FeignMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

    public FastJson2FeignMessageConverter() {
        super();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.TEXT_XML);
        mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
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
            return JSONObject.parseObject(requestBody, clazz);
        } catch (IOException e) {
            throw new HttpMessageNotReadableException("接口参数读取异常！", e);
        }

    }
    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        try {
            InputStream in = inputMessage.getBody();
            String requestBody = IoUtil.read(in, StandardCharsets.UTF_8);
            return JSONObject.parseObject(requestBody, type);
        } catch (IOException e) {
            throw new HttpMessageNotReadableException("接口参数读取异常！", e);
        }
    }
    // endregion


    // region -------------------------------------------- 写出消息 --------------------------------------------
    @Override
    protected void writeInternal(Object obj, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            String jsonStr = JSONObject.toJSONString(obj);
            IoUtil.write(outputMessage.getBody(), true, jsonStr.getBytes(StandardCharsets.UTF_8));
        } catch (Throwable e) {
            log.error("接口结果写出异常！ obj:{}", obj, e);
            throw e;
        }
    }
    // endregion
}
