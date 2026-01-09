

## 说明
    我们要求所有客户端对服务器的访问，都必须进行加密传输
    但是由于加密后无法抓包看到请求参数，联调比较麻烦，故此此功能仅在正式环境强制生效

## 流程
    1、 服务端为每个客户端准备RSA公私钥
``` java
    @Test
    public void getRsaKEY(){
        RSA rsa = new RSA("RSA/ECB/PKCS1Padding");
        String privateKeyBase64 = rsa.getPrivateKeyBase64();
        String publicKeyBase64 = rsa.getPublicKeyBase64();
        Console.log("public: {}", publicKeyBase64);
        Console.log("private: {}", privateKeyBase64);
    }
```

    2、 将私钥写入配置文件，将公钥给到对应客户端，客户端可将公钥写死在包体中（尽一切办法加密保存）
``` yaml
apiKey:
  WB:     # Web网页端
    public: xxx
    private: xxx
  AA:     # 安卓客户端
    public: xxx
    private: xxx
```

    3、 客户端每次请求时，请求头中都要携带x-arg和x-inf，且x-inf参数值必须要加密，body请求体也要加密
        1) x-arg （请求头）：
            此参数表示加密秘钥，生成步骤:
            1、客户端自行生成AES密钥（如:a892ce9b0b7747ed858f5f700eefb889），此密钥用于加密x-inf的数据 和 请求体数据
                服务端返回数据时，也将用此秘钥加密后返回
                AES算法： AES/ECB/PKCS5Padding
            2、客户端用RSA公钥，将AES秘钥加密，转为Base64字符串，生成密文字符串
                RSA算法:RSA/ECB/PKCS1Padding
                假设公钥为: MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCzrtHJa1M2tFs2gbaPSxJDCkM9+x8jM2IkGz65neOoKiyEQ4R7nHNXkubLyEj/pxXcv9SdKXqXy+3AIndvtj8UP20qwLuPEzqkcibZ1a62fAptUryeQJdclpZWATGlrn65Z/zeVHlqXo3pJe23Ggcqx9/6JlytizfDUSUgNlFrVwIDAQAB
                则密文为: VVcsix8HrKL5AJ94kEnoczoBJbj7IKEE/mXOYl/EB4FLDqFjj2NLXF6ya72HB4bzdgU9dDYj+1Z9ObGPQePkTDVqjfoFR5JWYY9LULovntAMMtvYkcz5e9AlR2HIJmQ5xXQ9O5GV2ax6kdVVYdtKIx3LMmRBRM0LRWnS639JumI=
            3、在密文字符串前，再拼接上客户端标识，然后整体作为值传输
                假设客户端是安卓客户端，客户端标识为: AA
                拼接后的字符串: x-arg: AAVVcsix8HrKL5AJ94kEnoczoBJbj7IKEE/mXOYl/EB4FLDqFjj2NLXF6ya72HB4bzdgU9dDYj+1Z9ObGPQePkTDVqjfoFR5JWYY9LULovntAMMtvYkcz5e9AlR2HIJmQ5xXQ9O5GV2ax6kdVVYdtKIx3LMmRBRM0LRWnS639JumI=
        2) x-inf （请求头）：
            此参数包含客户端基本信息，每一项参数key都要跟服务端对齐
            额外要求其中的n参数 就是加密用的aesKey
            示例 x-inf: p=BF&c=IOS&v=1.0.0&nt=wifi&os=IOS&ov=14.7&ua=iPhone&di=123456&fa=123456&ai=123456&ei=123456&oa=123456&t=1640000000&n=a892ce9b0b7747ed858f5f700eefb889&l=1000
            正式环境传输时，需要AES加密传输，假设AES密钥为: a892ce9b0b7747ed858f5f700eefb889 时，密文如下:
            x-inf: HgcMdCD/SCisqfA+zeQzBhiHPD1M+M8JvlR9xzjeK8TjfJQs4SmTxZpMWaQsdmTlnJ/16TU12FBZPYQjUa3N2rc9/iAVay+SPGxIRKl2Lbt2yYeXpu9Aw/9UPFdm9AfYdAsvpizVw+0PHB04wgKluzP8uLt33GIWj0axWKPnFAbP2VAUi62uy9nsr9n4pYK63tJsixxDZCCzc0cm519EKA==
        3) body （请求体）:
            业务约定，本项目对外接口统一使用POST方法，用json传参，那么请求体将都是json字符串；
            客户端需要在发送请求前，对json字符串进行AES加密，然后再发送到后端；
            注意：AES加密后，请求体从格式上看将不再是json，但是我们需要向后端请求的ContentType仍然必须是application/json，客户端注意调试处理
                假设请求体为: {"name":"张三","age":18}
                则密文为: SJpHK4PpRQ8cs3MrM0a/pNB93EqRN9BjyMTqU73BcdI=
            服务端返回时，也将会用AES加密后返回，客户端收到数据时，也需要用AES解密
                服务端响应: Ksa+sftipf7Y1x3gRK28Vnv3OTiBCHgVy6+4x3SVAEQgwIzeDFgWrNF0RpDVIowci8Q9zLhfdI+mQrvyrZBGHA==
                明文: {"code":200,"msg":"success","data":{"name":"张三","age":18}}

``` http request
### 客户端请求完整示例
POST http://localhost:8080/api/user/update
Content-Type: application/json
token: 7e598c2085da4d9aa72a23a63d6e6e08
x-arg: AAVVcsix8HrKL5AJ94kEnoczoBJbj7IKEE/mXOYl/EB4FLDqFjj2NLXF6ya72HB4bzdgU9dDYj+1Z9ObGPQePkTDVqjfoFR5JWYY9LULovntAMMtvYkcz5e9AlR2HIJmQ5xXQ9O5GV2ax6kdVVYdtKIx3LMmRBRM0LRWnS639JumI=
x-inf: HgcMdCD/SCisqfA+zeQzBhiHPD1M+M8JvlR9xzjeK8TjfJQs4SmTxZpMWaQsdmTlnJ/16TU12FBZPYQjUa3N2rc9/iAVay+SPGxIRKl2Lbt2yYeXpu9Aw/9UPFdm9AfYdAsvpizVw+0PHB04wgKlu5mJtUTzjE3lERIbaagk3FfzYaul8CPWWyfofN59HsvB

SJpHK4PpRQ8cs3MrM0a/pNB93EqRN9BjyMTqU73BcdI=
```

## 服务端流程
    1、 解析x-inf  （非强制加解密）
        ClientInfoParser.parseByHeader
        此时即支持明文也支持密文，当有x-arg请求头时，尝试解密；如果AES秘钥解析成功，将会被放置到BusiContext的ClientInfo属性中；
    2、 基于SaToken，对/api/**的接口，在正式环境 强制校验必须要有AES秘钥；（正式环境强制加解密）
        注意: 因为此校验是基于SaToken的，所以如果接口上有@SaIgnore注解，则此校验将失效；如果此时还需要校验，请自行处理，参看用户登录接口
        参看：com.devvv.commons.token.config.SaTokenConfiguration.addInterceptors
    3、 解析请求体前，检查如果Context中有AES秘钥，则对请求体进行AES解密 （非强制加解密）
        参看：MyHttpMessageConverter.readInternal
    4、 接口响应时，检查如果Context中有AES秘钥，则对响应体加密 （非强制加解密）
        参看：MyHttpMessageConverter.writeInternal
        
        


