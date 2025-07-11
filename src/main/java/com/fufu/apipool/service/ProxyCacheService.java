package com.fufu.apipool.service;

import com.fufu.apipool.entity.ProxyEntity;

/**
 * @author lizelin
 * @date 2025-07-10 23:16
 */
public interface ProxyCacheService {

    /**
     * 从缓存中随机获取一个启用的代理
     * @return ProxyEntity 或 null (如果没有可用的代理)
     */
    ProxyEntity getRandomProxy();

    /**
     * 从缓存中按轮询方式获取一个启用的代理
     * @return ProxyEntity 或 null (如果没有可用的代理)
     */
    ProxyEntity getRoundRobinProxy();

    /**
     * 刷新整个代理缓存
     */
    void refreshCache();

    /**
     * 在缓存中添加或更新一个代理
     * @param proxy 代理实体
     */
    void addOrUpdateProxyInCache(ProxyEntity proxy);

    /**
     * 从缓存中移除一个代理
     * @param proxyId 代理ID
     */
    void removeProxyFromCache(Long proxyId);
}
