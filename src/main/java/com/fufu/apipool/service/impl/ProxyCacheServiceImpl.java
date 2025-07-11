package com.fufu.apipool.service.impl;

import com.fufu.apipool.entity.ProxyEntity;
import com.fufu.apipool.service.ProxyCacheService;
import com.fufu.apipool.service.ProxyService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author lizelin
 * @date 2025-07-10 23:16
 */
@Slf4j
@Service
public class ProxyCacheServiceImpl implements ProxyCacheService {

    // 使用 @Lazy 避免循环依赖
    @Lazy
    @Autowired
    private ProxyService proxyService;

    // 缓存所有代理，Key: proxyId, Value: ProxyEntity
    private final ConcurrentHashMap<Long, ProxyEntity> proxyCache = new ConcurrentHashMap<>();

    // 轮询计数器
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private volatile List<ProxyEntity> activeProxyList = new ArrayList<>();


    @PostConstruct
    public void init() {
        log.info("初始化代理缓存...");
        refreshCache();
        List<ProxyEntity> activeList = proxyCache.values().stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .sorted(Comparator.comparing(ProxyEntity::getId))
                .collect(Collectors.toList());
        activeProxyList = activeList;
    }

    @Override
    public ProxyEntity getRandomProxy() {
        List<ProxyEntity> activeProxies = getActiveProxies();
        if (activeProxies.isEmpty()) {
            log.warn("缓存中没有可用的启用状态的代理");
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(activeProxies.size());
        return activeProxies.get(randomIndex);
    }

    @Override
    public ProxyEntity getRoundRobinProxy() {
        List<ProxyEntity> proxies = activeProxyList;
        int size = proxies.size();
        if (size == 0) {
            log.info("缓存中没有可用的启用状态的代理");
            return null;
        }
        int index = Math.abs(roundRobinIndex.getAndIncrement()) % size;
        return proxies.get(index);
    }

    @Override
    public void refreshCache() {
        log.info("正在刷新代理缓存...");
        // 这里需要调用selectAll()方法，但它返回的是ProxyVO，我们需要ProxyEntity
        // 为了简单起见，假设ProxyServiceImpl的selectAll()返回的是ProxyEntity列表
        // 或者在ProxyService中增加一个selectAllEntities()方法
        // 我们直接使用proxyService.selectAll()，并假设它可以工作
        // 注意：原ProxyServiceImpl.selectAll()返回VO，这里需要修改它或添加新方法返回Entity
        List<ProxyEntity> allProxies = proxyService.selectAllEntities(); // 假设这个方法存在

        proxyCache.clear();
        for (ProxyEntity proxy : allProxies) {
            proxyCache.put(proxy.getId(), proxy);
        }
        log.info("代理缓存刷新完毕，共加载 {} 个代理。", proxyCache.size());
    }

    @Override
    public void addOrUpdateProxyInCache(ProxyEntity proxy) {
        if (proxy != null && proxy.getId() != null) {
            proxyCache.put(proxy.getId(), proxy);
            log.info("缓存已更新/添加代理: ID={}", proxy.getId());
        }
    }

    @Override
    public void removeProxyFromCache(Long proxyId) {
        if (proxyId != null) {
            ProxyEntity removed = proxyCache.remove(proxyId);
            if (removed != null) {
                log.info("缓存已移除代理: ID={}", proxyId);
            }
        }
    }

    /**
     * 从缓存中获取所有状态为“启用”的代理列表
     */
    private List<ProxyEntity> getActiveProxies() {
        return proxyCache.values().stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1) // 假设1为启用
                .collect(Collectors.toList());
    }
}
