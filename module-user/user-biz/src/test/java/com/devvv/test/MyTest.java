package com.devvv.test;

import cn.hutool.core.lang.Console;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Create by WangSJ on 2024/07/01
 */
@Slf4j
public class MyTest {
    @Test
    public void getRsaKEY(){
        RSA rsa = new RSA("RSA/ECB/PKCS1Padding");
        String privateKeyBase64 = rsa.getPrivateKeyBase64();
        String publicKeyBase64 = rsa.getPublicKeyBase64();
        Console.log("public: {}", publicKeyBase64);
        Console.log("private: {}", privateKeyBase64);
    }

    @Test
    public void test(){

    }


}
