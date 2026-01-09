package com.devvv.commons.core.context;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSONObject;
import com.devvv.commons.common.enums.type.ClientType;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.core.config.ApplicationInfo;
import com.devvv.commons.core.config.redis.RedisKey;
import com.devvv.commons.core.config.redis.key.LimitKeyDefine;
import com.devvv.commons.core.config.redis.template.LimitRedisTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create by WangSJ on 2024/07/09
 */
@Slf4j
@Component
public class ClientInfoUtil {

    private static final Map<ClientType, Pair<PrivateKey, PublicKey>> RSA_KEY_MAP = new ConcurrentHashMap<>();
    public static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static LimitRedisTemplate limitRedisTemplate;
    @Autowired(required = false)
    private void setLimitRedisTemplate(LimitRedisTemplate limitRedisTemplate) {
        ClientInfoUtil.limitRedisTemplate = limitRedisTemplate;
    }

    /**
     * 解析请求头
     */
    public static ClientInfo parseByHeader(HttpServletRequest request) {
        String x_inf = request.getHeader(ClientInfo.X_INF);
        if (StrUtil.isBlank(x_inf)) {
            return null;
        }
        // 1、解析秘钥
        String x_arg = request.getHeader(ClientInfo.X_ARG);
        Pair<ClientType, byte[]> aesKey = getAesKey(x_arg);
        if (aesKey != null) {
            x_inf = SecureUtil.aes(aesKey.getValue()).decryptStr(x_inf);
        }
        // 2、解析请求头
        Map<CharSequence, CharSequence> queryMap = UrlQuery.of(x_inf, StandardCharsets.UTF_8).getQueryMap();
        ClientInfo clientInfo = new JSONObject(queryMap).to(ClientInfo.class);

        // 将AES秘钥存储到CientInfo中
        if (aesKey != null) {
            // 检查RSA使用的秘钥，与客户端类型是否匹配
            Assert.equals(aesKey.getKey(), clientInfo.getClientType(), () -> new BusiException(ErrorCode.CRYPT_KEY_ERR, StrUtil.format("客户端（{}）非法使用了RSA公钥（{}）", clientInfo.getClientType().getId(), aesKey.getKey().getId()), "非法请求"));
            clientInfo.setAesKey(aesKey.getValue());
        }
        return clientInfo;
    }

    /**
     * 检查客户端请求
     */
    public static void checkClient(){
        ClientInfo clientInfo = BusiContextUtil.getContext().getClientInfo();
        Assert.notNull(clientInfo, () -> new BusiException(ErrorCode.X_INFO, "缺少请求头x-inf", "请求参数有误！"));
        // 非正式环境，可以不用检测
        if ("_ignore".equals(clientInfo.getNonce()) && !ApplicationInfo.isProdEnv()) {
            return;
        }

        // 必须要有AesKey，即必须要加密请求
        if (ApplicationInfo.isProdEnv() || clientInfo.getAesKey() != null) {
            Assert.notNull(clientInfo.getAesKey(), () -> new BusiException(ErrorCode.X_ARG, "缺少请求头x-arg", "请求参数有误！"));
            // Assert.equals(new String(clientInfo.getAesKey()), clientInfo.getNonce(), () -> new BusiException(ErrorCode.X_ARG, "加密的AesKey和info中的key不一致", "请求参数有误！"));
        }

        // 客户端和服务端时间
        long diffTime = Math.abs(System.currentTimeMillis() - clientInfo.getClientTimestamp());
        Assert.isTrue(diffTime <= 300_000, () -> new BusiException(ErrorCode.CLIENT_TIME_ERR, "客户端时间相差超过阈值", "客户端异常"));

        // 60s内，重复请求检查，防止重放攻击
        if (limitRedisTemplate == null) {
            log.error("limitRedis 未配置，请及时检查处理！");
        } else {
            RedisKey apiLimitRedisKey = RedisKey.create(LimitKeyDefine.Api, BusiContextUtil.getContext().getClientIp(), clientInfo.getNonce());
            if (!limitRedisTemplate.setIfAbsent(apiLimitRedisKey, "1")) {
                throw new BusiException(ErrorCode.REPEATED_REQUEST, "nonce重复请求", "重复请求");
            }
        }

        // 客户端指纹检测
    }

    /**
     * 解析请求体
     */
    public static String decryptBody(String bodyStr) {
        if (bodyStr == null) {
            return null;
        }
        return Opt.of(BusiContextUtil.getContext())
                .map(BusiContext::getClientInfo)
                .map(ClientInfo::getAesKey)
                .map(SecureUtil::aes)
                .map(aes -> aes.decryptStr(bodyStr))
                .orElse(bodyStr);
    }

    /**
     * 加密请求体
     */
    public static String encryptBody(String bodyStr) {
        if (bodyStr == null) {
            return null;
        }
        return Opt.of(BusiContextUtil.getContext())
                .map(BusiContext::getClientInfo)
                .map(ClientInfo::getAesKey)
                .map(SecureUtil::aes)
                .map(aes -> aes.encryptBase64(bodyStr))
                .orElse(bodyStr);
    }


    private static Pair<ClientType, byte[]> getAesKey(String x_arg) {
        if (x_arg == null || x_arg.length() <= 2) {
            return null;
        }
        // 前2位是客户端类型
        String clientTypeId = x_arg.substring(0, 2);
        ClientType clientType = EnumUtil.getBy(ClientType.class, c -> c.getId().equalsIgnoreCase(clientTypeId));
        Assert.notNull(clientType, () -> new BusiException(ErrorCode.CRYPT_KEY_ERR, StrUtil.format("x-arg前缀错误:{}", x_arg), "非法请求"));

        // 获取RSA公私钥
        RSA rsa = getRsa(clientType);
        Assert.notNull(rsa, () -> new BusiException(ErrorCode.CRYPT_KEY_ERR, StrUtil.format("未配置客户端RSA公私钥({})", clientTypeId), "非法请求"));

        // 解密AES秘钥
        byte[] aesKey = Opt.ofTry(() -> rsa.decrypt(x_arg.substring(2), KeyType.PrivateKey))
                .orElseThrow(() -> new BusiException(ErrorCode.CRYPT_KEY_ERR, StrUtil.format("x-arg解密失败，客户端公钥可能不正确！({})", clientTypeId), "非法请求"));
        return Pair.of(clientType, aesKey);
    }

    private static RSA getRsa(ClientType clientType) {
        Pair<PrivateKey, PublicKey> key = RSA_KEY_MAP.get(clientType);
        if (key != null) {
            return new RSA(RSA_ALGORITHM, key.getKey(), key.getValue());
        }
        // 从配置文件中获取RSA私钥
        Object privateKey = SpringUtil.getProperty(StrUtil.format("apiKey.{}.private", clientType.getId()));
        Object publicKey = SpringUtil.getProperty(StrUtil.format("apiKey.{}.public", clientType.getId()));
        if (privateKey == null) {
            log.warn("接口秘钥未配置！ clientType:{}  {}", clientType, clientType.getId());
            return null;
        }
        PrivateKey prv = KeyUtil.generatePrivateKey(RSA_ALGORITHM, SecureUtil.decode(privateKey.toString()));
        PublicKey pub = KeyUtil.generatePublicKey(RSA_ALGORITHM, SecureUtil.decode(publicKey.toString()));
        key = Pair.of(prv, pub);
        RSA_KEY_MAP.put(clientType, key);
        return new RSA(RSA_ALGORITHM, key.getKey(), key.getValue());
    }

}
