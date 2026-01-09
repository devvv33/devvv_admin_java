package com.devvv.commons.core.context;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.devvv.commons.core.busicode.BusiCodeDefine;
import com.devvv.commons.core.session.AdminSessionInfo;
import com.devvv.commons.core.session.UserSessionInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

import java.util.Date;

/**
 * Create by WangSJ on 2024/06/26
 */
@Data
public class BusiContext {
    private transient HttpServletRequest request;
    private transient HttpServletResponse response;

    private String traceId;             // 请求链路id，用于日志
    private Long globalRequestId;       // 全局请求id，一般用于分布式锁
    private Date requestTime;
    private String requestURL;          // 完整url，包含主机名
    private String requestURI;          // 请求路径，不包含主机名
    private String clientIp;
    private String userAgent;

    private ClientInfo clientInfo;      // 客户端详细信息

    @JSONField(serialize = false)
    private String requestQuery;
    @JSONField(serialize = false)
    private String requestBody;
    @JSONField(serialize = false)
    private Object result;
    @JSONField(serialize = false)
    private String error;

    private JSONObject ext = new JSONObject();

    /**
     * 当前登录的用户信息
     */
    private Long adminId;
    private AdminSessionInfo adminSessionCopy;
    private Long userId;
    private UserSessionInfo userSessionCopy;

    private BusiCodeDefine busiCode;
    // 终止发送mq消息
    @JSONField(serialize = false)
    private Boolean breakSendMQ;

    public String toJsonStr(){
        return JSONObject.toJSONString(this);
    }
    // 全字段序列化，包括serialize = false的字段
    public String toFullJsonStr(){
        return JSONUtil.toJsonStr(this);
    }
}
