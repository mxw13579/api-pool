package com.fufu.apipool.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fufu.apipool.common.constant.ApiUrlEnum;
import com.fufu.apipool.common.exception.ApiAuthException;
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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cache for remote API login sessions.
 */
@Slf4j
@Component
public class ApiSessionCache {

    @Autowired
    private PoolService poolService;

    private final ConcurrentMap<Long, LoginSession> SESSION_CACHE = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, PoolEntity> POOL_CACHE = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, ReentrantLock> LOGIN_LOCKS = new ConcurrentHashMap<>();

    /**
     * Get a cached login session or log in when absent.
     */
    public LoginSession getSession(Long poolId) {
        LoginSession cachedSession = SESSION_CACHE.get(poolId);
        if (cachedSession != null) {
            return cachedSession;
        }

        ReentrantLock lock = LOGIN_LOCKS.computeIfAbsent(poolId, id -> new ReentrantLock());
        lock.lock();
        try {
            cachedSession = SESSION_CACHE.get(poolId);
            if (cachedSession != null) {
                return cachedSession;
            }

            PoolEntity poolEntity = getPoolEntity(poolId);
            LoginSession newSession = performLogin(poolEntity);
            SESSION_CACHE.put(poolId, newSession);
            return newSession;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clear cached session and pool info for the given pool.
     */
    public void clearSession(Long poolId) {
        log.info("Clearing cached session for pool {}", poolId);
        SESSION_CACHE.remove(poolId);
        POOL_CACHE.remove(poolId);
    }

    /**
     * Force refresh of the session for the given pool.
     */
    public LoginSession refreshSession(Long poolId) {
        ReentrantLock lock = LOGIN_LOCKS.computeIfAbsent(poolId, id -> new ReentrantLock());
        lock.lock();
        try {
            log.info("Refreshing session for pool {}", poolId);
            SESSION_CACHE.remove(poolId);
            POOL_CACHE.remove(poolId);

            PoolEntity poolEntity = getPoolEntity(poolId);
            LoginSession newSession = performLogin(poolEntity);
            SESSION_CACHE.put(poolId, newSession);
            return newSession;
        } finally {
            lock.unlock();
        }
    }

    private PoolEntity getPoolEntity(Long poolId) {
        PoolEntity poolEntity = POOL_CACHE.get(poolId);
        if (poolEntity == null) {
            poolEntity = poolService.selectById(poolId);
            if (poolEntity != null) {
                POOL_CACHE.put(poolId, poolEntity);
            } else {
                log.error("PoolEntity not found for id: {}", poolId);
                throw new ApiAuthException("Pool not found: " + poolId);
            }
        }
        return poolEntity;
    }

    private LoginSession performLogin(PoolEntity poolEntity) {
        try {
            LoginRequest request = new LoginRequest();
            request.setUsername(poolEntity.getUsername());
            request.setPassword(poolEntity.getPassword());

            HttpRequest postRequest = HttpRequest.post(poolEntity.getEndpoint() + ApiUrlEnum.LOGIN.getUrl());
            postRequest.body(JSON.toJSONString(request));
            HttpResponse loginResponse = postRequest.execute();

            int status = loginResponse.getStatus();
            if (status == 401 || status == 403) {
                throw new ApiAuthException("Authentication failed: HTTP " + status,
                        status, String.valueOf(poolEntity.getId()));
            }

            if (!loginResponse.isOk()) {
                throw new ApiAuthException("Login request failed: HTTP " + status,
                        status, String.valueOf(poolEntity.getId()));
            }

            List<HttpCookie> cookies = loginResponse.getCookies();
            R<User> newApiResponse = JSON.parseObject(loginResponse.body(), new TypeReference<R<User>>() {});
            if (newApiResponse == null || newApiResponse.getData() == null) {
                throw new ApiAuthException("Login response parsing failed", 500,
                        String.valueOf(poolEntity.getId()));
            }

            User user = newApiResponse.getData();
            log.info("Pool {} logged in successfully, user: {}", poolEntity.getId(), user.getUsername());
            return new LoginSession(cookies, poolEntity.getEndpoint(), user);
        } catch (ApiAuthException authException) {
            throw authException;
        } catch (Exception ex) {
            log.error("Login failed for poolId {}", poolEntity.getId(), ex);
            throw new ApiAuthException("Login failed: " + ex.getMessage(), ex, 500,
                    String.valueOf(poolEntity.getId()));
        }
    }

    @Getter
    public static class LoginSession {
        private final List<HttpCookie> cookies;
        private final String endpoint;
        private final User user;

        public LoginSession(List<HttpCookie> cookies, String endpoint, User user) {
            this.cookies = cookies;
            this.endpoint = endpoint;
            this.user = user;
        }
    }
}
