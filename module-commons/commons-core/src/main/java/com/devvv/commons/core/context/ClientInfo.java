package com.devvv.commons.core.context;

import com.alibaba.fastjson2.annotation.JSONField;
import com.devvv.commons.common.enums.PackageType;
import com.devvv.commons.common.enums.type.ClientType;
import com.devvv.commons.common.json.FastJosn2IDEnumReader;
import com.devvv.commons.common.json.FastJosn2IDEnumWriter;
import lombok.Data;

/**
 * Create by WangSJ on 2024/01/16
 */
@Data
public class ClientInfo {

    /**
     * ASE秘钥
     * 1、客户端自行生成AES密钥，此密钥用于加密x-inf的数据 和 请求体数据
     *    服务端返回数据时，也将用此秘钥加密后返回
     *    AES算法: AES/ECB/PKCS5Padding，加密后转为Base64字符串
     * 2、客户端用RSA公钥，再将AES秘钥加密，生成密文字符串
     *    RSA算法: RSA/ECB/PKCS1Padding，加密后转为Base64字符串
     * 3、在密文字符串前，再拼接上客户端标识，然后整体作为值传输
     */
    public static final String X_ARG = "x-arg";
    /**
     * 请求头信息
     * x-info: t=1640000000&n=123456&l=1000&p=BF&c=IOS&v=1.0.0&nt=wifi&os=IOS&ov=14.7&ua=iPhone&di=123456&fa=123456&ai=123456&ei=123456&oa=123456
     * 正式环境，要对x-info整体value串，进行AES加密传输
     */
    public static final String X_INF = "x-inf";

    private transient byte[] aesKey;

    @JSONField(name = "t")
    private Long clientTimestamp;           // 客户端时间
    @JSONField(name = "n")
    private String nonce;                   // 随机数（要求必须是加密的AesKey）
    @JSONField(name = "c")
    private String channelCode;

    @JSONField(name = "p")
    private PackageType packageType;            // app包体
    @JSONField(name = "i", deserializeUsing = FastJosn2IDEnumReader.class, serializeUsing = FastJosn2IDEnumWriter.class)
    private ClientType clientType;          // app客户端类型
    @JSONField(name = "v")
    private String clientVersion;           // app的客户端版本号


    @JSONField(name = "nt")
    private String netType;             // 网络类型 2G、3G、4G、5G、wifi、网线
    @JSONField(name = "os")
    private String os;                  // 操作系统 Android、IOS、Windows、MacOS、Linux 等
    @JSONField(name = "ov")
    private String osVersion;           // 操作系统版本号
    @JSONField(name = "di")
    private String deviceId;            // 自定义设备id
    @JSONField(name = "fa")
    private String deviceIdfa;          // IOS设备标识，<10 直接获取；>=10 用户关闭时获取到的是000；系统大版本升级（如11到12）IDFA也会发生变化
    @JSONField(name = "ai")
    private String deviceAndroidId;     // Android版本=8.0 可获取
    @JSONField(name = "ei")
    private String deviceImei;          // Android版本<6.0 直接获取；6.0<=Android版本<=9.0 需授权获取；Android版本>=10.0 几乎不可获取
    @JSONField(name = "oa")
    private String deviceOaid;          // MSA制定的匿名设备标识符，通常Android版本>=10.0可获取

}