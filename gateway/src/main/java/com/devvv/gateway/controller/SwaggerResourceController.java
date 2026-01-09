package com.devvv.gateway.controller;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by WangSJ on 2024/07/03
 *
 * Swgger的接口聚合处理
 */
@RestController
@Profile({"dev", "test"})
// 只在 Nacos 启用时生效
@ConditionalOnProperty(name = "spring.cloud.nacos.discovery.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerResourceController {

    @Value("${spring.application.name}")
    private String currServerName;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;
    @Resource
    private NacosServiceManager nacosServiceManager;

    /**
     * 查询接口分组
     * 遍历所有微服务，查询其/v3/api-docs/swagger-config接口，获取swagger分组信息，最终聚合到一起返回给ui
     */
    @GetMapping("/v3/api-docs/swagger-config")
    public Mono<Map<String, Object>> getGroups() throws Exception {
        String group = nacosDiscoveryProperties.getGroup();
        ListView<String> serverList = nacosServiceManager.getNamingService().getServicesOfServer(1, 9999, group);
        JSONArray urls = new JSONArray();
        // 遍历所有注册的微服务, 查询其/v3/api-docs/swagger-config接口，获取swagger分组信息
        for (String serverName : serverList.getData()) {
            // 排除当前服务
            if (ObjectUtil.equal(currServerName, serverName)) {
                continue;
            }
            try {
                List<Instance> instanceList = nacosServiceManager.getNamingService().getAllInstances(serverName, group);
                if (instanceList == null || instanceList.isEmpty()) {
                    continue;
                }
                Instance instance = instanceList.get(0);
                String url = StrUtil.format("http://{}:{}/v3/api-docs/swagger-config", instance.getIp(), instance.getPort());
                String jsonStr = HttpUtil.get(url);
                Opt.ofTry(() -> JSONObject.parse(jsonStr))
                        .map(json -> json.getJSONArray("urls"))
                        // 替换返回的url，将原url编码，并拼接上服务名，向后传递
                        .peek(jsonArr -> jsonArr.forEach(obj -> {
                            if (obj instanceof JSONObject json && StrUtil.isNotBlank(json.getString("url"))) {
                                json.put("url", StrUtil.format("/v3/api-docs/{}/{}", serverName, HexUtil.encodeHexStr(json.getString("url"))));
                            }
                        }))
                        .ifPresent(urls::addAll);
            } catch (Exception ignore) {
            }
        }

        // 排序
        // [{"name":"管理WEB","url":"/v3/api-docs/cms-web"}]
        urls.sort(Comparator.comparing(v -> ((JSONObject) v).getString("name")));
        // 模板
        String str = """
                {
                    "configUrl": "/v3/api-docs/swagger-config",
                    "oauth2RedirectUrl": "http://localhost:8888/swagger-ui/oauth2-redirect.html",
                    "urls": [],
                    "validatorUrl": ""
                }
                """;
        JSONObject result = JSONObject.parseObject(str);
        result.put("urls", urls);
        return Mono.just(result);
    }


    /**
     * 根据分组，查询接口详情
     * 查询指定服务的/v3/api-docs/{分组名} 接口，获取其接口详情
     */
    @GetMapping("/v3/api-docs/{serverName}/{targetUrl}")
    public Map<String, Object> getApiByGroup(@PathVariable("serverName") String targetServerName, @PathVariable("targetUrl") String targetUrl, ServerHttpRequest request) throws Exception {
        // 排除当前服务
        if (ObjectUtil.equal(currServerName, targetServerName)) {
            return null;
        }
        String group = nacosDiscoveryProperties.getGroup();
        String baseUrl = request.getURI().getScheme() + "://" + request.getURI().getAuthority();

        try {
            // 代理查询实际微服务
            List<Instance> instanceList = nacosServiceManager.getNamingService().getAllInstances(targetServerName, group);
            if (instanceList == null || instanceList.isEmpty()) {
                return null;
            }
            targetUrl = HexUtil.decodeHexStr(targetUrl);
            Instance instance = instanceList.get(0);
            // 转换为实际的地址 http://localhost:8088/v3/api-docs/cms-web
            String url = StrUtil.format("http://{}:{}{}", instance.getIp(), instance.getPort(), targetUrl);
            String jsonStr = HttpUtil.get(url);
            JSONObject jsonObject = Opt.ofTry(() -> JSONObject.parse(jsonStr))
                    .peek(json -> json.getJSONArray("servers").forEach(obj -> {
                        // 将各微服务的端口替换为gateway的端口, 统一由gateway转发请求
                        if (obj instanceof JSONObject j && StrUtil.isNotBlank(j.getString("url"))) {
                            j.put("url", baseUrl);
                        }
                    }))
                    .orElse(null);

            // 排序-按描述字符串排序
            if (jsonObject != null) {
                JSONObject paths = jsonObject.getJSONObject("paths");
                LinkedHashMap<String, Object> sortedPaths = paths.entrySet().stream()
                        .sorted((e1, e2) -> {
                            JSONObject val1 = (JSONObject) ((JSONObject) e1.getValue()).entrySet().stream().findAny().get().getValue();
                            String tag1 = ObjectUtil.toString(val1.getByPath("tags[0]"));
                            String summary1 = ObjectUtil.toString(val1.getString("summary"));

                            JSONObject val2 = (JSONObject) ((JSONObject) e2.getValue()).entrySet().stream().findAny().get().getValue();
                            String tag2 = ObjectUtil.toString(val2.getByPath("tags[0]"));
                            String summary2 = ObjectUtil.toString(val2.getString("summary"));

                            int compareByTag = tag1.compareTo(tag2);
                            if (compareByTag != 0) {
                                return compareByTag;
                            }
                            if (summary1 == null && summary2 == null) {
                                return e1.getKey().compareTo(e2.getKey());
                            }
                            if (summary1 == null) {
                                return -1;
                            } else if (summary2 == null) {
                                return 1;
                            } else {
                                return summary1.compareTo(summary2);
                            }
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                jsonObject.put("paths", sortedPaths);
            }
            return jsonObject;
        } catch (Exception ignore) {
            return null;
        }
    }
}
