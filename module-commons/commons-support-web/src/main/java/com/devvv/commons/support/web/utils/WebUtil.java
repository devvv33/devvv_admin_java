package com.devvv.commons.support.web.utils;

import com.alibaba.fastjson2.JSONObject;
import com.devvv.commons.core.context.BusiContextUtil;
import com.devvv.commons.core.context.ClientInfoUtil;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Create by WangSJ on 2024/01/15
 */
@Slf4j
public class WebUtil {


    /**
     * 获取客户端IP
     */
    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (ipAddress.contains(",")){
            for (String ip : ipAddress.split(",")) {
                return ip;
            }
        }
        return ipAddress;
    }

    /**
     * 获取所有请求头
     */
    public static Map<String, String> getHeaderMap(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headerMap.put(headerName.toLowerCase(), headerValue);
        }
        return headerMap;
    }


    /**
     * 向浏览器发送数据
     */
    public static void sendJsonMessage(ServletResponse response, Object msg) {
        if (response==null) {
            return;
        }
        String jsonMsg = "";
        if (msg != null) {
            jsonMsg = JSONObject.toJSONString(msg);
        }
        sendJsonMessage(response, jsonMsg);
    }

    /**
     * 向浏览器发送数据
     */
    public static void sendJsonMessage(ServletResponse response, String jsonStr) {
        if (response==null) {
            return;
        }
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // try {
        //     httpResponse.reset();
        // } catch (Exception e) {
        //     log.warn("HttpResponse 已经关闭，无法再次打开");
        //     return;
        // }

        // 允许跨域相关设置
        // httpResponse.setHeader("Access-Control-Max-Age", "3600");
        // httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET,PUT, OPTIONS, DELETE");//http请求方式
        // httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        // httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        // httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, x-access-token");

        // 禁用浏览器缓存
        httpResponse.setHeader("Cache-Control", "no-store");
        httpResponse.setHeader("Pragrma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);
        httpResponse.setStatus(HttpStatus.OK.value());

        httpResponse.setContentType("application/json; charset=utf-8");
        httpResponse.setCharacterEncoding("UTF-8");

        try {
            PrintWriter out = httpResponse.getWriter();
            // 加密
            jsonStr = ClientInfoUtil.encryptBody(jsonStr);
            out.print(jsonStr);
            out.flush();
        } catch (ClientAbortException e) {
            log.error("{} \t客户端已断开连接：{}", BusiContextUtil.getContext().getClientIp(), e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
