package com.fufu.apipool.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import com.fufu.apipool.common.ApiHttpUtil;
import com.fufu.apipool.common.constant.ApiUrlEnum;
import com.fufu.apipool.common.constant.ChannelStatus;
import com.fufu.apipool.common.constant.ChannelType;
import com.fufu.apipool.domain.newapi.*;
import com.fufu.apipool.entity.PoolEntity;
import com.fufu.apipool.entity.PoolProxyRelationEntity;
import com.fufu.apipool.entity.ProxyEntity;
import com.fufu.apipool.mapper.PoolMapper;
import com.fufu.apipool.service.PoolProxyRelationService;
import com.fufu.apipool.service.PoolService;
import com.fufu.apipool.service.ProxyCacheService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 号池服务实现类
 * 提供号池的增删改查操作
 * @author lizelin
 */
@Slf4j
@Service
@AllArgsConstructor
public class PoolServiceImpl implements PoolService {

    private final PoolMapper poolMapper;
    @Lazy
    @Autowired
    private ApiHttpUtil apiHttpUtil;
    private final ProxyCacheService proxyCacheService;
    private final PoolProxyRelationService poolProxyRelationService;

    private final ExecutorService monitoringExecutor;
    private final ConcurrentHashMap<Long, Lock> poolLocks = new ConcurrentHashMap<>();

    private static final Pattern FAILED_PATTERN = Pattern.compile("^(.*) -监控失败- (\\d+)次$");
    private static final Pattern RECOVERED_PATTERN = Pattern.compile("^(.*) -重试\\d+次后恢复$");

    /**
     * 根据ID查询号池
     * @param id 号池ID
     * @return PoolEntity 号池实体
     */
    @Override
    public PoolEntity selectById(Long id) {
        return poolMapper.selectById(id);
    }

    /**
     * 查询所有号池
     * @return List<PoolEntity> 号池列表
     */
    @Override
    public List<PoolEntity> selectAll() {
        return poolMapper.selectAll();
    }

    /**
     * 新增号池
     * @param poolEntity 号池实体
     * @return int 插入结果
     */
    @Override
    public int insert(PoolEntity poolEntity) {
        return poolMapper.insert(poolEntity);
    }

    /**
     * 更新号池
     * @param poolEntity 号池实体
     * @return int 更新结果
     */
    @Override
    public int update(PoolEntity poolEntity) {
        return poolMapper.update(poolEntity);
    }

    /**
     * 根据ID删除号池
     * @param id 号池ID
     * @return int 删除结果
     */
    @Override
    public int deleteById(Long id) {
        return poolMapper.deleteById(id);
    }

    /**
     * 根据ID查询号池下的渠道列表
     * @param poolId 号池ID
     * @return List<Channel> 渠道列表
     */
    @Override
    public List<Channel> getChannelsByPoolId(Long poolId) {
        //?p=1&page_size=50&id_sort=true&tag_mode=false
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("p", 1);
        pathVars.put("page_size", 99999);
        pathVars.put("id_sort", false);
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.LIST, null, pathVars, null);
        R<ChannelPageData<Channel>> r = JSON.parseObject(
                send,
                new TypeReference<R<ChannelPageData<Channel>>>() {},
                JSONReader.Feature.SupportSmartMatch
        );
        return r.getData().getItems();
    }

    /**
     * 根据ID更新号池下的渠道信息
     * @param poolId 号池ID
     * @param channel 渠道信息
     * @return Boolean 更新结果
     */
    @Override
    public Boolean updateChannelByPoolId(Long poolId, Channel channel) {
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.EDIT, null, null, channel);
        R<Channel> r = JSON.parseObject(
                send,
                new TypeReference<R<Channel>>() {}
        );
        if (!r.getSuccess().equals(true)) {
            throw new RuntimeException("更新渠道失败");
        }
        return true;
    }


    /**
     * 根据ID测试号池下的渠道信息
     * @param poolId 号池ID
     * @param channelId 渠道信息
     * @return long 测试结果
     */
    @Override
    public long testChannelByPoolId(Long poolId, Long channelId) {
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("channelId", channelId);
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.TEST, pathVars, null, null);
        R r = JSON.parseObject(send, R.class);
        if (!r.getSuccess().equals(true)) {
            log.error("测试渠道失败: {}", r);
            throw new RuntimeException("测试渠道失败:"+r.getMessage());
        }
        return r.getTime();
    }

    /**
     * 根据ID添加号池下的渠道信息
     *
     * @param poolId  号池ID
     * @param dto 渠道信息，包含代理选择策略
     * @return Boolean 添加结果
     */
    @Override
    @Transactional
    public Boolean addChannelByPoolId(Long poolId, ChannelDTO dto) {
        Channel channel = BeanUtil.copyProperties(dto, Channel.class);
        Integer proxyStrategy = dto.getProxy(); // 0: 随机, 1: 轮询

        // 1. 获取代理
        ProxyEntity selectedProxy;
        if (Objects.equals(proxyStrategy, 0)) {
            selectedProxy = proxyCacheService.getRandomProxy();
            log.info("为号池ID {} 添加渠道，使用随机策略选择代理。", poolId);
        } else if (Objects.equals(proxyStrategy, 1)) {
            selectedProxy = proxyCacheService.getRoundRobinProxy();
            log.info("为号池ID {} 添加渠道，使用轮询策略选择代理。", poolId);
        } else {
            // 如果策略值无效或未提供，则不使用代理
            selectedProxy = null;
            log.info("为号池ID {} 添加渠道，未指定有效的代理策略，不使用代理。", poolId);
        }

        if (selectedProxy == null && proxyStrategy != null) {
            throw new RuntimeException("根据策略未能获取到任何可用的代理，请检查代理列表及其状态。");
        }

        // 2. 设置渠道的代理信息
        if (selectedProxy != null) {
            String proxyUrl = selectedProxy.getProxyUrl();
            //proxyUrl 如果不是 socket5开头则添加
            if (!proxyUrl.startsWith("socks5")) {
                proxyUrl = "socks5://" + proxyUrl;
            }
            // 格式化为JSON字符串: {"proxy":"http://user:pass@host:port"}
            channel.setSetting("{\"proxy\":\"" + proxyUrl + "\"}");
            dto.setSetting(proxyUrl);
            log.info("选定代理: ID={}, URL={}", selectedProxy.getId(), proxyUrl);
        }

        // 3. 调用外部API添加渠道
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.ADD, null, null, channel);
        R<Channel> r = JSON.parseObject(send, new TypeReference<R<Channel>>() {});
        if (!r.getSuccess().equals(true)) {
            log.error("添加渠道失败，API响应: {}", send);
            // 这里可以解析r.getMessage()以提供更具体的错误信息
            throw new RuntimeException("调用API添加渠道失败: " + r.getMessage());
        }

        log.info("API添加渠道成功。");
        // API返回的Channel对象可能包含由API生成的ID，但我们这里不需要

        // 4. 如果成功使用了代理，则添加绑定关系
        if (selectedProxy != null) {
            PoolProxyRelationEntity relation = new PoolProxyRelationEntity();
            relation.setPoolId(poolId);
            relation.setProxyId(selectedProxy.getId());

            boolean relationAdded = poolProxyRelationService.addRelation(relation);
            if (!relationAdded) {
                // 如果数据库操作失败，因为方法有@Transactional注解，整个操作会回滚。
                // 但外部API调用无法回滚，这是一个分布式事务问题。
                // 在当前场景下，抛出异常让事务回滚是合理的处理。
                log.error("添加号池-代理绑定关系失败! PoolId: {}, ProxyId: {}", poolId, selectedProxy.getId());
                throw new RuntimeException("数据库操作失败：添加号池与代理的绑定关系时出错。");
            }
            log.info("成功添加号池-代理绑定关系: PoolId={}, ProxyId={}", poolId, selectedProxy.getId());
        }

        return true;
    }

    /**
     * 根据ID查询号池下的渠道信息
     *
     * @param poolId    号池ID
     * @param channelId 渠道ID
     * @return Channel 渠道信息
     */
    @Override
    public Channel getChannelByPoolId(Long poolId, Long channelId) {
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("channelId", channelId);
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.DETAIL, pathVars, null, null);
        R<Channel> r = JSON.parseObject(
                send,
                new TypeReference<R<Channel>>() {}
        );
        return r.getData();
    }

    /**
     * 根据号池ID和渠道ID删除渠道信息
     * @param poolId 号池ID
     * @param channelId 渠道信息
     * @return Boolean 删除结果
     */
    @Override
    public Boolean deleteChannelByPoolId(Long poolId, Long channelId) {
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("channelId", channelId);
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.DELETE, pathVars, null, null);
        R<Channel> r = JSON.parseObject(
                send,
                new TypeReference<R<Channel>>() {}
        );
        if (!r.getSuccess().equals(true)) {
            throw new RuntimeException("删除渠道失败");
        }
        return true;
    }

    /**
     * 批量为所有号池新增渠道。
     * "轮询"在此处理解为遍历所有号池，并为每个号池执行添加操作。
     * @param dto 渠道数据传输对象
     * @return 如果所有操作都成功，则返回 true，否则返回 false。
     */
    @Override
    public List<String> batchAddChannelToAll(ChannelDTO dto) {
        // 1. 获取所有号池
        List<PoolEntity> allPools = this.selectAll();
        if (allPools == null || allPools.isEmpty()) {
            log.warn("批量新增渠道失败：系统中没有任何号池。");
            throw new RuntimeException("批量新增渠道失败：系统中没有任何号池。");
        }

        log.info("开始为 {} 个号池批量新增渠道，渠道名称: {}", allPools.size(), dto.getName());

        String key = dto.getKey();

        JSONArray objects = JSON.parseArray(key);
        // 转换为list
        List<String> list = objects.toJavaList(String.class);

        int size = allPools.size();
        int offset = 0;
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {

            ChannelDTO channelDTO = BeanUtil.copyProperties(dto, ChannelDTO.class);
            channelDTO.setKey(list.get(i));
            channelDTO.setName(dto.getName() + "-" + i);
            try {
                if (channelDTO.getType().equals(ChannelType.VERTEX_AI)) {
                    String vertexAiKey  = channelDTO.getKey();
                    GoogleServiceAccount googleServiceAccount = JSON.parseObject(vertexAiKey, GoogleServiceAccount.class);
                    String projectId = googleServiceAccount.getProject_id();
                    channelDTO.setName(projectId);
                }

                // 3. 复用现有的 `addChannelByPoolId` 逻辑
                Boolean success = this.addChannelByPoolId(allPools.get(offset).getId(), channelDTO);
                if (success != null && success) {
                    // 写入成功信息
                    String msg = String.format("成功为号池 ID: %s, 名称: %s 添加渠道 %s 成功,代理为 %s",
                            allPools.get(offset).getId(), allPools.get(offset).getName(), channelDTO.getName(),channelDTO.getSetting());
                    ret.add(msg);
                    log.info(msg);
                } else {
                    // 写入失败信息
                    String msg = String.format("为号池 ID: %s, 名称: %s 添加渠道 %s 失败。",
                            allPools.get(offset).getId(), allPools.get(offset).getName(), channelDTO.getName());
                    ret.add(msg);
                    log.error(msg);
                }
            } catch (Exception e) {
                // 写入异常信息
                String msg = String.format("为号池 ID: %s, 名称: %s 添加渠道 %s 时发生异常：%s",
                        allPools.get(offset).getId(), allPools.get(offset).getName(), channelDTO.getName(), e.getMessage());
                ret.add(msg);
                log.error(msg, e);
            }

            if (offset >= size - 1) {
                offset = 0;
            } else {
                offset++;
            }
        }
        return ret;
    }

    private record NameParseResult(String originalName, int retries) {}

    private NameParseResult parseChannelName(String name) {
        if (name == null) {
            return new NameParseResult("", 0);
        }
        Matcher failedMatcher = FAILED_PATTERN.matcher(name);
        if (failedMatcher.matches()) {
            return new NameParseResult(failedMatcher.group(1).trim(), Integer.parseInt(failedMatcher.group(2)));
        }

        Matcher recoveredMatcher = RECOVERED_PATTERN.matcher(name);
        if (recoveredMatcher.matches()) {
            return new NameParseResult(recoveredMatcher.group(1).trim(), 0);
        }

        return new NameParseResult(name, 0);
    }

    private void safeUpdateChannel(Long poolId, Channel channel) {
        try {
            updateChannelByPoolId(poolId, channel);
        } catch (Exception e) {
            // BUG FIX 3: 捕获更新失败的异常，防止中断监控流程
            log.error("CRITICAL: 更新号池[ID:{}]的渠道[{}({})]状态失败！数据库与内存状态可能不一致。",
                    poolId, channel.getName(), channel.getId(), e);
        }
    }

    @Override
    public void monitorPool(Long poolId) {
        Lock lock = poolLocks.computeIfAbsent(poolId, k -> new ReentrantLock());
        if (!lock.tryLock()) {
            log.warn("号池[ID:{}] 的监控任务已在运行，本次调度跳过。", poolId);
            return;
        }
        try {
            doMonitorPool(poolId);
        } finally {
            lock.unlock();
        }
    }

    private void doMonitorPool(Long poolId) {
        PoolEntity pool = poolMapper.selectById(poolId);
        if (pool == null) {
            log.error("监控任务失败：找不到ID为 {} 的号池。", poolId);
            return;
        }

        List<Channel> allChannels = getChannelsByPoolId(poolId);
        int maxRetries = pool.getMaxMonitorRetries() == null ? 5 : pool.getMaxMonitorRetries();
        int minActive = pool.getMinActiveChannels() == null ? 1 : pool.getMinActiveChannels();

        // 1. 分类渠道
        List<Channel> activeChannels = new ArrayList<>();
        List<Channel> retryableFailedChannels = new ArrayList<>();
        List<Channel> pristineInactiveChannels = new ArrayList<>();

        // 按使用额度降序排序，优先测试额度高的渠道
        allChannels = allChannels.stream()
                .sorted(Comparator.comparingInt(Channel::getUsedQuota).reversed())
                .toList();

        for (Channel channel : allChannels) {
            if (channel.getStatus() == ChannelStatus.ENABLED) {
                activeChannels.add(channel);
            } else {
                NameParseResult result = parseChannelName(channel.getName());
                if (result.retries() > 0 && result.retries() < maxRetries) {
                    retryableFailedChannels.add(channel);
                } else if (result.retries() == 0) {
                    pristineInactiveChannels.add(channel);
                }
            }
        }

        log.info("号池[{}]: 监控开始. 激活: {}, 待重试: {}, 闲置: {}.",
                pool.getName(), activeChannels.size(), retryableFailedChannels.size(), pristineInactiveChannels.size());

        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        AtomicInteger failureCount = new AtomicInteger(0);
        // 使用线程安全的队列收集从“待重试”状态恢复的渠道
        ConcurrentLinkedQueue<Channel> recoveredChannels = new ConcurrentLinkedQueue<>();

        // 2. 检测激活渠道
        for (Channel channel : activeChannels) {
            tasks.add(CompletableFuture.runAsync(() -> {
                // 尝试1次主测试 + 2次重试，总共3次
                boolean isAlive = testChannelWithRetries(pool.getId(), channel.getId(), 2, 1000L);

                if (isAlive) {
                    log.info("号池[{}]: 激活渠道[{}({})]检测正常, 当前使用额度{}.", pool.getName(), channel.getName(), channel.getId(), (channel.getUsedQuota() / 500000));
                } else {
                    // 经过重试后仍然失败
                    failureCount.incrementAndGet();
                    NameParseResult result = parseChannelName(channel.getName());
                    // 标记为失败1次
                    channel.setName(result.originalName() + " -监控失败- 1次");
                    channel.setStatus(ChannelStatus.MANUALLY_DISABLED);
                    safeUpdateChannel(pool.getId(), channel);
                    log.warn("号池[{}]: 激活渠道[{}({})]多次检测失败，已禁用。", pool.getName(), result.originalName(), channel.getId());
                }
            }, monitoringExecutor));
        }

        // 3. 检测待重试渠道
        for (Channel channel : retryableFailedChannels) {
            tasks.add(CompletableFuture.runAsync(() -> {
                try {
                    testChannelByPoolId(pool.getId(), channel.getId());
                    // 如果成功，不直接激活，而是标记为“已恢复”，并加入预备队列
                    NameParseResult result = parseChannelName(channel.getName());
                    channel.setName(result.originalName() + " -重试" + result.retries() + "次后恢复");
                    channel.setStatus(ChannelStatus.MANUALLY_DISABLED); // 保持非激活状态
                    safeUpdateChannel(pool.getId(), channel);
                    recoveredChannels.add(channel); // 添加到恢复队列
                    log.info("号池[{}]: 渠道[{}({})]重试成功，已移至待激活列表。", pool.getName(), channel.getName(), channel.getId());
                } catch (Exception e) {
                    // 如果重试仍然失败，增加失败次数
                    NameParseResult result = parseChannelName(channel.getName());
                    channel.setName(result.originalName() + " -监控失败- " + (result.retries() + 1) + "次");
                    safeUpdateChannel(pool.getId(), channel);
                    log.warn("号池[{}]: 待重试渠道[{}({})]再次检测失败。", pool.getName(), result.originalName(), channel.getId());
                }
            }, monitoringExecutor));
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        // 4. 判断是否需要激活新渠道
        // 重新获取最新状态来计算激活数量
        List<Channel> updatedChannels = getChannelsByPoolId(poolId);
        long currentActiveCount = updatedChannels.stream()
                .filter(c -> c.getStatus() == ChannelStatus.ENABLED)
                .count();
        int needed = minActive - (int) currentActiveCount;

        if (needed > 0) {
            // 5. 准备待激活列表 (将恢复的渠道放在最前面，优先激活)
            List<Channel> activationCandidates = new ArrayList<>();
            // 恢复的渠道已按额度排序，直接添加
            activationCandidates.addAll(new ArrayList<>(recoveredChannels));
            // 闲置渠道也已按额度排序，追加在后面
            activationCandidates.addAll(pristineInactiveChannels);

            if (!activationCandidates.isEmpty()) {
                log.info("号池[{}]: 当前激活 {} < 最小要求 {}, 需激活 {}个。开始从 {} 个候选渠道中检测...",
                        pool.getName(), currentActiveCount, minActive, needed, activationCandidates.size());

                ConcurrentLinkedQueue<Channel> successfulInactiveChannels = new ConcurrentLinkedQueue<>();
                List<CompletableFuture<Void>> activationTestTasks = new ArrayList<>();

                for(Channel channel : activationCandidates) {
                    activationTestTasks.add(CompletableFuture.runAsync(() -> {
                        try {
                            testChannelByPoolId(pool.getId(), channel.getId());
                            successfulInactiveChannels.add(channel);
                        } catch(Exception e) {
                            NameParseResult result = parseChannelName(channel.getName());
                            channel.setName(result.originalName() + " -监控失败- 1次");
                            channel.setStatus(ChannelStatus.MANUALLY_DISABLED);
                            safeUpdateChannel(pool.getId(), channel);
                            log.warn("号池[{}]: 候选渠道[{}({})]激活测试失败。", pool.getName(), result.originalName(), channel.getId());
                        }
                    }, monitoringExecutor));
                }
                CompletableFuture.allOf(activationTestTasks.toArray(new CompletableFuture[0])).join();

                // 按ID排序以保证激活顺序的确定性
                List<Channel> sortedSuccessChannels = successfulInactiveChannels.stream()
                        .sorted(Comparator.comparing(Channel::getId))
                        .toList();

                if (!sortedSuccessChannels.isEmpty()) {
                    sortedSuccessChannels.stream().limit(needed).forEach(channelToActivate -> {
                        // 激活时可能需要重置名称
                        NameParseResult result = parseChannelName(channelToActivate.getName());
                        if (result.originalName() != null && !result.originalName().equals(channelToActivate.getName())) {
                            channelToActivate.setName(result.originalName());
                        }
                        channelToActivate.setStatus(ChannelStatus.ENABLED);
                        safeUpdateChannel(pool.getId(), channelToActivate);
                        log.info("号池[{}]: 已成功激活渠道 [{}({})], 当前使用额度{}.", pool.getName(), channelToActivate.getName(), channelToActivate.getId(), (channelToActivate.getUsedQuota() / 500000));
                    });
                }
            }
        }
        log.info("号池[{}]: 监控任务执行完毕。", pool.getName());
    }

    /**
     * 带重试机制的渠道测试方法。
     * @param poolId 池ID
     * @param channelId 渠道ID
     * @param retries 重试次数 (例如，传入2代表首次失败后再重试2次)
     * @param delayMillis 重试间隔（毫秒）
     * @return 测试成功返回 true, 否则返回 false
     */
    private boolean testChannelWithRetries(Long poolId, Long channelId, int retries, long delayMillis) {
        for (int i = 0; i <= retries; i++) {
            try {
                testChannelByPoolId(poolId, channelId);
                return true; // 成功则直接返回
            } catch (Exception e) {
                if (i < retries) {
                    log.warn("渠道[{}]检测失败，将在 {}ms 后进行第 {}/{} 次重试...", channelId, delayMillis, i + 1, retries);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMillis);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        log.error("渠道[{}]重试等待时被中断。", channelId);
                        return false; // 等待被中断，直接返回失败
                    }
                } else {
                    log.error("渠道[{}]经过 {} 次重试后仍检测失败。", channelId, retries, e);
                }
            }
        }
        return false; // 所有尝试都失败了
    }

}
