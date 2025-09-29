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
     * 登录锁，防止并发登录
     */
    private final ConcurrentMap<Long, ReentrantLock> LOGIN_LOCKS = new ConcurrentHashMap<>();

    /**
     * 获取登录session（线程安全）
     * @param poolId poolId
     * @return 登录session
     */
    public LoginSession getSession(Long poolId) {
        // 先尝试从缓存获取
        LoginSession cachedSession = SESSION_CACHE.get(poolId);
        if (cachedSession != null) {
            return cachedSession;
        }

        // 获取登录锁
        ReentrantLock lock = LOGIN_LOCKS.computeIfAbsent(poolId, k -> new ReentrantLock());

        lock.lock();
        try {
            // 双重检查，防止并发登录
            cachedSession = SESSION_CACHE.get(poolId);
            if (cachedSession != null) {
                return cachedSession;
            }

            // 执行登录
            PoolEntity poolEntity = getPoolEntity(poolId);
            LoginSession newSession = performLogin(poolEntity);
            SESSION_CACHE.put(poolId, newSession);

            return newSession;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清除指定号池的会话缓存
     * @param poolId 号池ID
     */
    public void clearSession(Long poolId) {
        log.info("清除号池{}的会话缓存", poolId);
        SESSION_CACHE.remove(poolId);
    }

    /**
     * 刷新指定号池的会话
     * @param poolId 号池ID
     * @return 新的登录会话
     */
    public LoginSession refreshSession(Long poolId) {
        ReentrantLock lock = LOGIN_LOCKS.computeIfAbsent(poolId, k -> new ReentrantLock());

        lock.lock();
        try {
            log.info("刷新号池{}的会话", poolId);

            // 清除旧会话
            SESSION_CACHE.remove(poolId);

            // 重新登录
            PoolEntity poolEntity = getPoolEntity(poolId);
            LoginSession newSession = performLogin(poolEntity);
            SESSION_CACHE.put(poolId, newSession);

            return newSession;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取号池实体
     * @param poolId 号池ID
     * @return 号池实体
     */
    private PoolEntity getPoolEntity(Long poolId) {
        PoolEntity poolEntity = POOL_CACHE.get(poolId);
        if (poolEntity == null) {
            poolEntity = poolService.selectById(poolId);
            if (poolEntity != null) {
                POOL_CACHE.put(poolId, poolEntity);
            } else {
                log.error("PoolEntity not found for id: " + poolId);
                throw new ApiAuthException("号池不存在: " + poolId);
            }
        }
        return poolEntity;
    }

    /**
     * 执行登录操作
     * @param poolEntity pool对象
     * @return 登录session
     */
    private LoginSession performLogin(PoolEntity poolEntity) {
        try {
            LoginRequest request = new LoginRequest();
            request.setUsername(poolEntity.getUsername());
            request.setPassword(poolEntity.getPassword());

            HttpRequest postRequest = HttpRequest.post(poolEntity.getEndpoint() + ApiUrlEnum.LOGIN.getUrl());
            postRequest.body(JSON.toJSONString(request));
            HttpResponse loginResponse = postRequest.execute();

            // 检查HTTP状态码
            if (loginResponse.getStatus() == 401 || loginResponse.getStatus() == 403) {
                throw new ApiAuthException("认证失败: HTTP " + loginResponse.getStatus(),
                                         loginResponse.getStatus(), String.valueOf(poolEntity.getId()));
            }

            if (!loginResponse.isOk()) {
                throw new ApiAuthException("登录请求失败: HTTP " + loginResponse.getStatus(),
                                         loginResponse.getStatus(), String.valueOf(poolEntity.getId()));
            }

            List<HttpCookie> cookies = loginResponse.getCookies();
            R<User> newApiR = JSON.parseObject(loginResponse.body(), new TypeReference<R<User>>() {});

            if (newApiR == null || newApiR.getData() == null) {
                throw new ApiAuthException("登录响应解析失败", 500, String.valueOf(poolEntity.getId()));
            }

            User user = newApiR.getData();
            log.info("号池{}登录成功，用户: {}", poolEntity.getId(), user.getUsername());

            return new LoginSession(cookies, poolEntity.getEndpoint(), user);
        } catch (ApiAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login failed for poolId: " + poolEntity.getId(), e);
            throw new ApiAuthException("登录失败: " + e.getMessage(), e, 500, String.valueOf(poolEntity.getId()));
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
