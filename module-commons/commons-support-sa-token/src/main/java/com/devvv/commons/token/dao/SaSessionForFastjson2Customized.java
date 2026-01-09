package com.devvv.commons.token.dao;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.util.SaFoxUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

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
public class SaSessionForFastjson2Customized extends SaSession {

    private static final long serialVersionUID = -7600983549653130681L;

    /**
     * 构建一个 SaSession 对象
     */
    public SaSessionForFastjson2Customized() {
        super();
    }

    /**
     * 构建一个 SaSession 对象
     * @param id Session 的 id
     */
    public SaSessionForFastjson2Customized(String id) {
        super(id);
    }

    /**
     * 取值 (指定转换类型)
     * @param <T> 泛型
     * @param key key
     * @param cs 指定转换类型
     * @return 值
     */
    @Override
    public <T> T getModel(String key, Class<T> cs) {
        // 如果是想取出为基础类型
        Object value = get(key);
        if(SaFoxUtil.isBasicType(cs)) {
            return SaFoxUtil.getValueByType(value, cs);
        }
        // 为空提前返回
        if(valueIsNull(value)) {
            return null;
        }
        // 如果是 JSONObject 类型直接转，否则先转为 String 再转
        if(value instanceof JSONObject jo) {
            return jo.toJavaObject(cs);
        } else {
            return JSON.parseObject(value.toString(), cs);
        }
    }

}