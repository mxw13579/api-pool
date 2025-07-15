package com.fufu.apipool.service.impl;

import com.fufu.apipool.entity.ProxyEntity;
import com.fufu.apipool.service.PoolProxyRelationService;
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

    @Autowired
    private PoolProxyRelationService poolProxyRelationService;

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

        if (activeList.isEmpty()) {
            log.warn("没有可用的激活代理，轮询索引将从0开始。");
            return;
        }

        // 2. 获取最后使用的代理ID
        Long lastUsedProxyId = poolProxyRelationService.findLastUsedProxyId();
        int startIndex = 0;

        if (lastUsedProxyId != null) {
            // 3. 在当前激活列表中查找该代理的索引
            for (int i = 0; i < activeList.size(); i++) {
                if (activeList.get(i).getId().equals(lastUsedProxyId)) {
                    // 4. 如果找到，将起始索引设置为下一个位置，并处理边界情况
                    startIndex = (i + 1) % activeList.size();
                    log.info("找到最后使用的代理ID: {}, 轮询将从下一个代理开始，索引: {}", lastUsedProxyId, startIndex);
                    break;
                }
            }
            if (startIndex == 0) {
                log.info("最后使用的代理ID: {} 不在当前激活列表中，轮询将从头开始。", lastUsedProxyId);
            }
        } else {
            log.info("未找到任何代理使用记录，轮询将从头开始。");
        }

        // 5. 设置轮询计数器的初始值
        this.roundRobinIndex.set(startIndex);
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
        List<ProxyEntity> allProxies = proxyService.selectAllEntities();

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
