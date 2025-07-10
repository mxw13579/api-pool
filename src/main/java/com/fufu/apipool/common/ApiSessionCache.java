package com.fufu.apipool.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fufu.apipool.common.constant.ApiUrlEnum;
import com.fufu.apipool.domain.newapi.LoginRequest;
import com.fufu.apipool.domain.newapi.R;
import com.fufu.apipool.domain.newapi.User;
import com.fufu.apipool.entity.PoolEntity;
import com.fufu.apipool.service.PoolService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.HttpCookie;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * api session缓存
 * @author lizelin
 */
@Slf4j
@Component
public class ApiSessionCache {
    @Autowired
    private PoolService poolService;
    /**
     * Session缓存
     */
    private final ConcurrentMap<Long, LoginSession> SESSION_CACHE = new ConcurrentHashMap<>();
    /**
     * Pool缓存
     */
    private final ConcurrentMap<Long, PoolEntity> POOL_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取登录session
     * @param poolId poolId
     * @return 登录session
     */
    public LoginSession getSession(Long poolId) {
        return SESSION_CACHE.computeIfAbsent(poolId, id -> {
            PoolEntity poolEntity = POOL_CACHE.get(id);
            if (poolEntity == null) {
                poolEntity = poolService.selectById(id);
                if (poolEntity != null) {
                    POOL_CACHE.put(id, poolEntity);
                } else {
                    log.error("PoolEntity not found for id: " + id);
                    throw new RuntimeException("PoolEntity not found for id: " + id);
                }
            }
            return login(poolEntity);
        });
    }

    /**
     * 登录
     * @param poolEntity pool对象
     * @return 登录 session
     */
    private static LoginSession login(PoolEntity poolEntity) {
        try {
            LoginRequest request = new LoginRequest();
            request.setUsername(poolEntity.getUsername());
            request.setPassword(poolEntity.getPassword());
            HttpRequest postRequest = HttpRequest.post(poolEntity.getEndpoint() + ApiUrlEnum.LOGIN.getUrl());
            postRequest.body(JSON.toJSONString(request));
            HttpResponse loginResponse = postRequest.execute();
            List<HttpCookie> cookies = loginResponse.getCookies();
            R<User> newApiR = JSON.parseObject(loginResponse.body(), new TypeReference<R<User>>() {});
            User user = newApiR.getData();
            return new LoginSession(cookies,poolEntity.getEndpoint(),user);
        } catch (Exception e) {
            log.error("Login failed for poolId: " + poolEntity.getId(), e);
            throw new RuntimeException("Login failed for poolId: " + poolEntity.getId(), e);
        }
    }


    /**
     * 登录 session
     */
    @Getter
    public static class LoginSession {
        private final List<HttpCookie> cookies;
        private final String endpoint;
        private final User user;
        public LoginSession(List<HttpCookie> cookies,String endpoint, User user) {
            this.cookies = cookies;
            this.endpoint = endpoint;
            this.user = user;
        }
    }
}
