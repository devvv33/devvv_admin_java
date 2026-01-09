package com.devvv.commons.token.dao;

import cn.dev33.satoken.json.SaJsonTemplate;
import cn.dev33.satoken.util.SaFoxUtil;
import com.alibaba.fastjson2.JSON;

/**
 * Create by WangSJ on 2025/04/29
 *
 * 拷贝自:
 *         <dependency>
 *             <groupId>cn.dev33</groupId>
 *             <artifactId>sa-token-fastjson</artifactId>
 *             <version>1.42.0</version>
 *         </dependency>
 * 修改内容：
 *   将 fastjson 改为 fastjson2
 */
public class SaJsonTemplateForFastjson2 implements SaJsonTemplate {

    /**
     * 序列化：对象 -> json 字符串
     */
    @Override
    public String objectToJson(Object obj) {
        if(SaFoxUtil.isEmpty(obj)) {
            return null;
        }
        return JSON.toJSONString(obj);
    }

    /**
     * 反序列化：json 字符串 → 对象
     */
    @Override
    public<T> T jsonToObject(String jsonStr, Class<T> type) {
        if(SaFoxUtil.isEmpty(jsonStr)) {
            return null;
        }
        return JSON.parseObject(jsonStr, type);
    }

}

