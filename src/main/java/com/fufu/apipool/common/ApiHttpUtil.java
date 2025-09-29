package com.fufu.apipool.common;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import com.fufu.apipool.common.constant.ApiUrlEnum;
import com.fufu.apipool.common.exception.ApiAuthException;
import com.fufu.apipool.domain.newapi.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ApiHttpUtil {

    @Autowired
    private RetryableApiCall retryableApiCall;

    /**
     * 发送API请求（支持认证失效自动重试）
     * @param poolId poolId
     * @param apiUrlEnum url枚举
     * @param pathVars 路径变量（如{id}），可为null
     * @param query 查询参数，可为null
     * @param body 请求体，可为null
     * @return String
     */
    public String send(Long poolId, ApiUrlEnum apiUrlEnum, Map<String, Object> pathVars, Map<String, Object> query, Object body) {
        return retryableApiCall.executeWithRetry(
            poolId,
            () -> doSendRequest(poolId, apiUrlEnum, pathVars, query, body),
            () -> refreshSession(poolId)
        );
    }

    /**
     * 执行实际的HTTP请求
     */
    private String doSendRequest(Long poolId, ApiUrlEnum apiUrlEnum, Map<String, Object> pathVars, Map<String, Object> query, Object body) {
        // 获取session和endpoint
        ApiSessionCache.LoginSession session = SpringUtil.getBean(ApiSessionCache.class).getSession(poolId);
        String endpoint = session.getEndpoint();
        List<HttpCookie> cookies = session.getCookies();
        User user = session.getUser();

        // 拼接url
        String apiPath = apiUrlEnum.getUrl();
        if (pathVars != null && !pathVars.isEmpty()) {
            for (Map.Entry<String, Object> entry : pathVars.entrySet()) {
                apiPath = apiPath.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }
        String fullUrl = endpoint + apiPath;

        // 拼接query参数
        if (query != null && !query.isEmpty()) {
            StringBuilder sb = new StringBuilder(fullUrl);
            sb.append(fullUrl.contains("?") ? "&" : "?");
            query.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            fullUrl = sb.substring(0, sb.length() - 1);
        }

        // 构建请求
        HttpRequest request = HttpRequest.of(fullUrl)
                .method(Method.valueOf(apiUrlEnum.getMethod())).timeout(60000);

        if (!cookies.isEmpty()) {
            request.cookie(cookies.get(0));
        }
        if (user != null) {
            request.header("New-Api-User", String.valueOf(user.getId()));
        }
        if (body != null && !"GET".equalsIgnoreCase(apiUrlEnum.getMethod())) {
            request.body(JSON.toJSONString(body));
        }

        // 执行请求
        HttpResponse response = request.execute();

        // 检查认证状态
        int statusCode = response.getStatus();
        if (statusCode == 401 || statusCode == 403) {
            throw new ApiAuthException("API认证失效: HTTP " + statusCode, statusCode, String.valueOf(poolId));
        }

        if (!response.isOk()) {
            log.warn("API请求失败，poolId: {}, url: {}, status: {}", poolId, fullUrl, statusCode);
            throw new ApiAuthException("API请求失败: HTTP " + statusCode, statusCode, String.valueOf(poolId));
        }

        return response.body();
    }

    /**
     * 刷新指定号池的会话
     */
    private void refreshSession(Long poolId) {
        log.info("刷新号池 {} 的认证会话", poolId);
        ApiSessionCache sessionCache = SpringUtil.getBean(ApiSessionCache.class);
        sessionCache.refreshSession(poolId);
    }
}
