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
import com.fufu.apipool.entity.ErrorLogEntity;
import com.fufu.apipool.entity.PoolEntity;
import com.fufu.apipool.entity.PoolProxyRelationEntity;
import com.fufu.apipool.entity.ProxyEntity;
import com.fufu.apipool.mapper.PoolMapper;
import com.fufu.apipool.service.PoolProxyRelationService;
import com.fufu.apipool.service.PoolService;
import com.fufu.apipool.service.ProxyCacheService;
import com.fufu.apipool.entity.ChannelEntity;
import com.fufu.apipool.service.ChannelService;
import com.fufu.apipool.service.ErrorLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 号池服务实现类
 * 保持原功能，优化可读性、健壮性与性能
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
    private final ErrorLogService errorLogService;
    private final ChannelService channelService;

    private final ExecutorService monitoringExecutor;
    private final ConcurrentHashMap<Long, Lock> poolLocks = new ConcurrentHashMap<>();

    private static final Pattern FAILED_PATTERN = Pattern.compile("^(.*) -监控失败- (\\d+)次$");
    private static final Pattern RECOVERED_PATTERN = Pattern.compile("^(.*) -重试\\d+次后恢复$");
    private static final Pattern PING_LATENCY_PATTERN = Pattern.compile("(time|时间)[=<]?([\\d.]+) ?ms", Pattern.CASE_INSENSITIVE);
    private static final JSONReader.Feature[] JSON_FEATURES = new JSONReader.Feature[]{JSONReader.Feature.SupportSmartMatch};
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final int DEFAULT_MIN_ACTIVE = 1;
    private static final int PING_COUNT = 5;

    /**
     * 根据ID查询号池实体
     * @param id 号池ID
     * @return PoolEntity 实体对象
     */
    @Override
    public PoolEntity selectById(Long id) {
        return poolMapper.selectById(id);
    }

    /**
     * 查询所有号池
     * @return 号池实体列表
     */
    @Override
    public List<PoolEntity> selectAll() {
        return poolMapper.selectAll();
    }

    /**
     * 新增号池
     * @param poolEntity 号池实体
     * @return 插入行数
     */
    @Override
    public int insert(PoolEntity poolEntity) {
        return poolMapper.insert(poolEntity);
    }

    /**
     * 更新号池
     * @param poolEntity 号池实体
     * @return 更新行数
     */
    @Override
    public int update(PoolEntity poolEntity) {
        return poolMapper.update(poolEntity);
    }

    /**
     * 根据ID删除号池
     * @param id 号池ID
     * @return 删除行数
     */
    @Override
    public int deleteById(Long id) {
        return poolMapper.deleteById(id);
    }

    /**
     * 获取指定号池下的所有渠道
     * @param poolId 号池ID
     * @return 渠道列表
     */
    @Override
    public List<Channel> getChannelsByPoolId(Long poolId) {
        Map<String, Object> pathVars = new HashMap<>(4);
        pathVars.put("p", 1);
        pathVars.put("page_size", 99999);
        pathVars.put("id_sort", Boolean.FALSE);
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.LIST, null, pathVars, null);
        R<ChannelPageData<Channel>> r = JSON.parseObject(send, new TypeReference<R<ChannelPageData<Channel>>>() {}, JSON_FEATURES);
        ensureSuccess(r, "获取渠道列表失败");
        ChannelPageData<Channel> data = r.getData();
        return data == null ? Collections.emptyList() : Optional.ofNullable(data.getItems()).orElse(Collections.emptyList());
    }

    /**
     * 更新指定号池下的渠道信息
     * @param poolId 号池ID
     * @param channel 渠道对象
     * @return 是否成功
     */
    @Override
    public Boolean updateChannelByPoolId(Long poolId, Channel channel) {
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.EDIT, null, null, channel);
        R<Channel> r = JSON.parseObject(send, new TypeReference<R<Channel>>() {}, JSON_FEATURES);
        ensureSuccess(r, "更新渠道失败");
        Channel updated = r.getData();
        if (updated == null) {
            throw new RuntimeException("更新渠道失败：API未返回渠道信息");
        }
        channelService.updateChannel(new ChannelEntity(updated, poolId));
        return true;
    }

    /**
     * 测试指定号池下渠道的可用性
     * @param poolId 号池ID
     * @param channelId 渠道ID
     * @return 响应时间（毫秒）
     */
    @Override
    public long testChannelByPoolId(Long poolId, Long channelId) {
        Map<String, Object> pathVars = new HashMap<>(4);
        pathVars.put("channelId", channelId);
        pathVars.put("model", "gemini-2.5-pro");
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.TEST, pathVars, null, null);
        R<?> r = JSON.parseObject(send, new TypeReference<R<?>>() {}, JSON_FEATURES);
        ensureSuccess(r, "测试渠道失败");
        return r.getTime();
    }

    /**
     * 为指定号池添加渠道（带事务）
     * @param poolId 号池ID
     * @param dto 渠道DTO
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean addChannelByPoolId(Long poolId, ChannelDTO dto) {
        Channel channel = BeanUtil.copyProperties(dto, Channel.class);
        Integer proxyStrategy = dto.getProxy(); // 0: 随机, 1: 轮询

        ProxyEntity selectedProxy = null;
        if (Objects.equals(proxyStrategy, 0)) {
            selectedProxy = proxyCacheService.getRandomProxy();
            log.info("为号池ID {} 添加渠道，使用随机策略选择代理。", poolId);
        } else if (Objects.equals(proxyStrategy, 1)) {
            selectedProxy = proxyCacheService.getRoundRobinProxy();
            log.info("为号池ID {} 添加渠道，使用轮询策略选择代理。", poolId);
        } else if (proxyStrategy != null) {
            // 非空但不支持的策略，按原逻辑视为错误
            selectedProxy = null;
        }

        if (selectedProxy == null && proxyStrategy != null) {
            throw new RuntimeException("根据策略未能获取到任何可用的代理，请检查代理列表及其状态。");
        }

      Map<String, Object> setting = new HashMap<>(2);


      if (selectedProxy != null) {
            String proxyUrl = normalizeProxyUrl(selectedProxy.getProxyUrl());
            // 统一用对象序列化，避免手拼 JSON
            setting.put("proxy", proxyUrl);
            log.info("选定代理: ID={}, URL={}", selectedProxy.getId(), proxyUrl);
        }

      setting.put("force_format", false);
      setting.put("thinking_to_content", false);
      setting.put("pass_through_body_enabled", false);
      setting.put("system_prompt_override", false);
      setting.put("system_prompt", "");

      String settingJson = JSON.toJSONString(setting);
      channel.setSetting(settingJson);
      dto.setSetting((String) setting.get("proxy"));


        NewChannel newChannel = new NewChannel();
        newChannel.setChannel(channel);

        String send = apiHttpUtil.send(poolId, ApiUrlEnum.ADD, null, null, newChannel);
        R<Channel> r = JSON.parseObject(send, new TypeReference<R<Channel>>() {}, JSON_FEATURES);
//        ensureSuccess(r, "调用API添加渠道失败");

        Channel createdChannel = channel;
        channelService.addChannel(new ChannelEntity(createdChannel, poolId));

        if (selectedProxy != null) {
            PoolProxyRelationEntity relation = new PoolProxyRelationEntity();
            relation.setPoolId(poolId);
            relation.setProxyId(selectedProxy.getId());
            if (!poolProxyRelationService.addRelation(relation)) {
                log.error("添加号池-代理绑定关系失败! PoolId: {}, ProxyId: {}", poolId, selectedProxy.getId());
                throw new RuntimeException("数据库操作失败：添加号池与代理的绑定关系时出错。");
            }
            log.info("成功添加号池-代理绑定关系: PoolId={}, ProxyId={}", poolId, selectedProxy.getId());
        }
        return true;
    }

    /**
     * 获取指定号池下的单个渠道详情
     * @param poolId 号池ID
     * @param channelId 渠道ID
     * @return 渠道对象
     */
    @Override
    public Channel getChannelByPoolId(Long poolId, Long channelId) {
        Map<String, Object> pathVars = new HashMap<>(2);
        pathVars.put("channelId", channelId);
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.DETAIL, pathVars, null, null);
        R<Channel> r = JSON.parseObject(send, new TypeReference<R<Channel>>() {}, JSON_FEATURES);
        ensureSuccess(r, "获取渠道详情失败");
        return r.getData();
    }

    /**
     * 删除指定号池下的渠道
     * @param poolId 号池ID
     * @param channelId 渠道ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteChannelByPoolId(Long poolId, Long channelId) {
        Map<String, Object> pathVars = new HashMap<>(2);
        pathVars.put("channelId", channelId);
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.DELETE, pathVars, null, null);
        R<Channel> r = JSON.parseObject(send, new TypeReference<R<Channel>>() {}, JSON_FEATURES);
        ensureSuccess(r, "删除渠道失败");
        channelService.deleteChannel(poolId, channelId);
        return true;
    }

    /**
     * 批量为所有号池添加渠道
     * @param dto 渠道DTO
     * @return 添加结果列表
     */
    @Override
    public List<String> batchAddChannelToAll(ChannelDTO dto) {
        List<PoolEntity> allPools = this.selectAll();
        if (allPools == null || allPools.isEmpty()) {
            log.warn("批量新增渠道失败：系统中没有任何号池。");
            throw new RuntimeException("批量新增渠道失败：系统中没有任何号池。");
        }

        log.info("开始为 {} 个号池批量新增渠道，渠道名称: {}", allPools.size(), dto.getName());

        JSONArray arr = JSON.parseArray(dto.getKey());
        List<String> keys = arr == null ? Collections.emptyList() : arr.toJavaList(String.class);
        if (keys.isEmpty()) {
            throw new RuntimeException("批量新增渠道失败：提供的 key 列表为空");
        }

        int poolSize = allPools.size();
        int offset = 0;
        List<String> ret = new ArrayList<>(keys.size());

        for (int i = 0; i < keys.size(); i++) {
            ChannelDTO channelDTO = BeanUtil.copyProperties(dto, ChannelDTO.class);
            channelDTO.setKey(keys.get(i));
            channelDTO.setName(dto.getName() + "-" + i);
            try {
                if (Objects.equals(channelDTO.getType(), ChannelType.VERTEX_AI)) {
                    GoogleServiceAccount google = JSON.parseObject(channelDTO.getKey(), GoogleServiceAccount.class, JSON_FEATURES);
                    if (google != null && CharSequenceUtil.isNotBlank(google.getProject_id())) {
                        channelDTO.setName(google.getProject_id());
                    }
                }

                PoolEntity pool = allPools.get(offset);
                Boolean success = this.addChannelByPoolId(pool.getId(), channelDTO);
                if (Boolean.TRUE.equals(success)) {
                    String msg = String.format("成功为号池 ID: %s, 名称: %s 添加渠道 %s 成功,代理为 %s",
                            pool.getId(), pool.getName(), channelDTO.getName(), channelDTO.getSetting());
                    ret.add(msg);
                    log.info(msg);
                } else {
                    String msg = String.format("为号池 ID: %s, 名称: %s 添加渠道 %s 失败。",
                            pool.getId(), pool.getName(), channelDTO.getName());
                    ret.add(msg);
                    log.error(msg);
                }
            } catch (Exception e) {
                PoolEntity pool = allPools.get(offset);
                String msg = String.format("为号池 ID: %s, 名称: %s 添加渠道 %s 时发生异常：%s",
                        pool.getId(), pool.getName(), channelDTO.getName(), e.getMessage());
                ret.add(msg);
                log.error(msg, e);
            }
            offset = (offset >= poolSize - 1) ? 0 : (offset + 1);
        }
        return ret;
    }

    /**
     * 渠道名称解析结果
     * originalName: 原始名称
     * retries: 重试次数
     */
    private record NameParseResult(String originalName, int retries) {}

    /**
     * 解析渠道名称，提取原始名称和重试次数
     * @param name 渠道名称
     * @return 解析结果
     */
    private NameParseResult parseChannelName(String name) {
        if (name == null) return new NameParseResult("", 0);
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

    /**
     * 渠道分组枚举
     */
    private enum ChannelGroup {
        ACTIVE,             // 激活
        RETRYABLE,          // 可重试
        PRISTINE,           // 闲置
        PERMANENTLY_FAILED  // 永久失败
    }

    /**
     * 单次测试执行结果
     * isSuccess: 是否成功
     * errorMessage: 错误信息
     */
    private record TestExecutionResult(boolean isSuccess, String errorMessage) {}

    /**
     * 渠道测试结果
     * channel: 渠道对象
     * isSuccess: 是否成功
     * originalGroup: 原始分组
     * errorMessage: 错误信息
     */
    private record ChannelTestResult(Channel channel, boolean isSuccess, ChannelGroup originalGroup, String errorMessage) {}

    /**
     * 执行号池监控任务（核心逻辑）
     * @param poolId 号池ID
     */
    private void doMonitorPool(Long poolId) {
        PoolEntity pool = poolMapper.selectById(poolId);
        if (pool == null) {
            log.error("监控任务失败：找不到ID为 {} 的号池。", poolId);
            return;
        }

        List<Channel> allChannels = getChannelsByPoolId(poolId).stream()
                .sorted(Comparator.comparingInt(Channel::getUsedQuota).reversed())
                .collect(Collectors.toList());

        int maxRetries = pool.getMaxMonitorRetries() == null ? DEFAULT_MAX_RETRIES : pool.getMaxMonitorRetries();
        int minActive = pool.getMinActiveChannels() == null ? DEFAULT_MIN_ACTIVE : pool.getMinActiveChannels();

        Map<ChannelGroup, List<Channel>> groupedChannels = categorizeChannels(allChannels, maxRetries);
        log.info("号池[{}]: 监控开始. 激活: {}, 待重试: {}, 闲置: {}, 永久失败: {}.",
                pool.getName(),
                groupedChannels.getOrDefault(ChannelGroup.ACTIVE, Collections.emptyList()).size(),
                groupedChannels.getOrDefault(ChannelGroup.RETRYABLE, Collections.emptyList()).size(),
                groupedChannels.getOrDefault(ChannelGroup.PRISTINE, Collections.emptyList()).size(),
                groupedChannels.getOrDefault(ChannelGroup.PERMANENTLY_FAILED, Collections.emptyList()).size());

        List<CompletableFuture<ChannelTestResult>> futures = new ArrayList<>();
        addTestTasks(futures, groupedChannels.getOrDefault(ChannelGroup.ACTIVE, Collections.emptyList()), pool.getId(), ChannelGroup.ACTIVE, true);
        addTestTasks(futures, groupedChannels.getOrDefault(ChannelGroup.RETRYABLE, Collections.emptyList()), pool.getId(), ChannelGroup.RETRYABLE, false);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        List<ChannelTestResult> finalResults = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

        long survivingActiveCount = finalResults.stream()
                .filter(r -> r.originalGroup() == ChannelGroup.ACTIVE && r.isSuccess())
                .count();

        int needed = minActive - (int) survivingActiveCount;
        if (needed > 0) {
            log.info("号池[{}]: 核心渠道测试后存活 {} 个，少于要求的 {} 个，需要补充 {} 个。开始测试闲置渠道...",
                    pool.getName(), survivingActiveCount, minActive, needed);

            List<Channel> pristineChannels = groupedChannels.getOrDefault(ChannelGroup.PRISTINE, Collections.emptyList());
            if (!pristineChannels.isEmpty()) {
                List<CompletableFuture<ChannelTestResult>> pristineFutures = new ArrayList<>(pristineChannels.size());
                addTestTasks(pristineFutures, pristineChannels, pool.getId(), ChannelGroup.PRISTINE, false);
                CompletableFuture.allOf(pristineFutures.toArray(new CompletableFuture[0])).join();
                pristineFutures.forEach(f -> finalResults.add(f.join()));
            } else {
                log.warn("号池[{}]: 需要补充渠道，但已无闲置渠道可供测试。", pool.getName());
            }
        } else {
            log.info("号池[{}]: 核心渠道测试后存活 {} 个，满足最低要求 {} 个，无需测试闲置渠道。",
                    pool.getName(), survivingActiveCount, minActive);
        }

        processTestResults(finalResults, pool, minActive);
    }

    /**
     * 渠道分组
     * @param channels 渠道列表
     * @param maxRetries 最大重试次数
     * @return 分组结果
     */
    private Map<ChannelGroup, List<Channel>> categorizeChannels(List<Channel> channels, int maxRetries) {
        Map<ChannelGroup, List<Channel>> map = new EnumMap<>(ChannelGroup.class);
        for (Channel channel : channels) {
            ChannelGroup group;
            if (channel.getStatus() == ChannelStatus.ENABLED) {
                group = ChannelGroup.ACTIVE;
            } else {
                NameParseResult result = parseChannelName(channel.getName());
                int retries = result.retries();
                if (retries == 0) group = ChannelGroup.PRISTINE;
                else if (retries < maxRetries) group = ChannelGroup.RETRYABLE;
                else group = ChannelGroup.PERMANENTLY_FAILED;
            }
            map.computeIfAbsent(group, k -> new ArrayList<>()).add(channel);
        }
        return map;
    }

    /**
     * 添加异步测试任务
     * @param futures 任务列表
     * @param channels 待测渠道
     * @param poolId 号池ID
     * @param group 分组
     * @param withRetries 是否带重试
     */
    private void addTestTasks(List<CompletableFuture<ChannelTestResult>> futures, List<Channel> channels,
                              Long poolId, ChannelGroup group, boolean withRetries) {
        if (channels == null || channels.isEmpty()) return;
        for (Channel channel : channels) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                TestExecutionResult result;
                if (withRetries) {
                    result = testChannelWithRetries(poolId, channel.getId(), 2, 1000L);
                } else {
                    try {
                        testChannelByPoolId(poolId, channel.getId());
                        result = new TestExecutionResult(true, null);
                    } catch (Exception e) {
                        result = new TestExecutionResult(false, e.getMessage());
                    }
                }
                return new ChannelTestResult(channel, result.isSuccess(), group, result.errorMessage());
            }, monitoringExecutor));
        }
    }

    /**
     * 处理所有测试结果，自动禁用/激活渠道
     * @param results 测试结果
     * @param pool 号池实体
     * @param minActive 最小激活数
     */
    private void processTestResults(List<ChannelTestResult> results, PoolEntity pool, int minActive) {
        if (results == null || results.isEmpty()) {
            log.info("号池[{}]: 无测试结果，监控任务结束。", pool.getName());
            return;
        }

        // 单次遍历：统计与处理失败
        long survivingActiveCount = 0;
        List<Channel> activationCandidates = new ArrayList<>();
        for (ChannelTestResult r : results) {
            if (r.isSuccess()) {
                if (r.originalGroup() == ChannelGroup.ACTIVE) {
                    survivingActiveCount++;
                } else if (r.originalGroup() == ChannelGroup.RETRYABLE || r.originalGroup() == ChannelGroup.PRISTINE) {
                    activationCandidates.add(r.channel());
                }
            } else {
                Channel channel = r.channel();
                NameParseResult nameResult = parseChannelName(channel.getName());
                int nextRetryCount = nameResult.retries() + 1;
                channel.setName(nameResult.originalName() + " -监控失败- " + nextRetryCount + "次");
                if (r.originalGroup() == ChannelGroup.ACTIVE) {
                    channel.setStatus(ChannelStatus.MANUALLY_DISABLED);
                    log.warn("号池[{}]: 激活渠道[{}({})]检测失败，已禁用。", pool.getName(), nameResult.originalName(), channel.getId());
                } else {
                    log.warn("号池[{}]: 候选渠道[{}({})]测试失败。", pool.getName(), nameResult.originalName(), channel.getId());
                }

                ErrorLogEntity errorLog = new ErrorLogEntity();
                errorLog.setPoolId(pool.getId());
                errorLog.setPoolName(pool.getName());
                errorLog.setChannelId(channel.getId());
                errorLog.setChannelName(nameResult.originalName());
                errorLog.setErrorMessage(CharSequenceUtil.isNotBlank(r.errorMessage()) ? r.errorMessage() : "监控测试失败");
                errorLogService.logError(errorLog);

                safeUpdateChannel(pool.getId(), channel);
            }
        }

        int needed = minActive - (int) survivingActiveCount;
        if (needed <= 0) {
            log.info("号池[{}]: 激活渠道数量 {} >= 最小要求 {}，无需激活新渠道。监控任务执行完毕。", pool.getName(), survivingActiveCount, minActive);
            return;
        }

        if (activationCandidates.isEmpty()) {
            log.warn("号池[{}]: 需要激活 {} 个渠道，但没有可用的候选渠道。监控任务执行完毕。", pool.getName(), needed);
            return;
        }

        activationCandidates.stream()
                .limit(needed)
                .forEach(channelToActivate -> {
                    NameParseResult nameResult = parseChannelName(channelToActivate.getName());
                    channelToActivate.setName(nameResult.originalName());
                    channelToActivate.setStatus(ChannelStatus.ENABLED);
                    safeUpdateChannel(pool.getId(), channelToActivate);
                    log.info("号池[{}]: 已成功激活渠道 [{}({})], 当前使用额度{}.", pool.getName(),
                            channelToActivate.getName(), channelToActivate.getId(), (channelToActivate.getUsedQuota() / 500000));
                });

        log.info("号池[{}]: 监控任务执行完毕。", pool.getName());
    }

    /**
     * 带重试的渠道测试
     * @param poolId 号池ID
     * @param channelId 渠道ID
     * @param retries 重试次数
     * @param delayMillis 重试间隔（毫秒）
     * @return 测试结果
     */
    private TestExecutionResult testChannelWithRetries(Long poolId, Long channelId, int retries, long delayMillis) {
        String lastExceptionMessage = "";
        for (int i = 0; i <= retries; i++) {
            try {
                testChannelByPoolId(poolId, channelId);
                return new TestExecutionResult(true, null);
            } catch (Exception e) {
                lastExceptionMessage = e.getMessage();
                if (i < retries) {
                    log.warn("渠道[{}]检测失败，将在 {}ms 后进行第 {}/{} 次重试...", channelId, delayMillis, i + 1, retries);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMillis);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        log.error("渠道[{}]重试等待时被中断。", channelId);
                        return new TestExecutionResult(false, "Interrupted during retry wait");
                    }
                } else {
                    log.error("渠道[{}]经过 {} 次重试后仍检测失败。", channelId, retries, e);
                }
            }
        }
        return new TestExecutionResult(false, lastExceptionMessage);
    }

    /**
     * 安全更新渠道信息（异常自动捕获并记录日志）
     * @param poolId 号池ID
     * @param channel 渠道对象
     */
    private void safeUpdateChannel(Long poolId, Channel channel) {
        try {
            updateChannelByPoolId(poolId, channel);
        } catch (Exception e) {
            log.error("CRITICAL: 更新号池[ID:{}]的渠道[{}({})]状态失败！数据库与内存状态可能不一致。",
                    poolId, channel.getName(), channel.getId(), e);
        }
    }

    /**
     * 号池监控入口（加锁防并发）
     * @param poolId 号池ID
     */
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

    /**
     * 测试号池网络延迟（ping）
     * @param id 号池ID
     * @return 平均延迟（毫秒），失败返回-1
     */
    @Override
    public long testLatency(Long id) {
        PoolEntity poolEntity = selectById(id);
        String endpoint = poolEntity == null ? null : poolEntity.getEndpoint();
        if (CharSequenceUtil.isBlank(endpoint)) {
            return -1L;
        }

        String host;
        try {
            URI uri = endpoint.contains("://") ? new URI(endpoint) : new URI("http://" + endpoint);
            host = uri.getHost();
            if (host == null) {
                host = endpoint.contains(":") ? endpoint.substring(0, endpoint.indexOf(":")) : endpoint;
            }
        } catch (Exception e) {
            log.error("Invalid endpoint URI: {}", endpoint, e);
            return -1L;
        }

        try {
            List<String> command = buildPingCommand(host);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            List<Double> latencies = new ArrayList<>(PING_COUNT);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = PING_LATENCY_PATTERN.matcher(line);
                    if (matcher.find()) {
                        latencies.add(Double.parseDouble(matcher.group(2)));
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0 && !latencies.isEmpty()) {
                double average = latencies.stream().mapToDouble(a -> a).average().orElse(-1.0);
                return (long) average;
            }
            return -1L;
        } catch (Exception e) {
            log.error("Ping failed for address: {}", endpoint, e);
            return -1L;
        }
    }

    /**
     * 构建ping命令
     * @param host 主机名或IP
     * @return 命令参数列表
     */
    private List<String> buildPingCommand(String host) {
        String os = System.getProperty("os.name").toLowerCase();
        List<String> command = new ArrayList<>(4);
        command.add("ping");
        command.add(os.contains("win") ? "-n" : "-c");
        command.add(String.valueOf(PING_COUNT));
        command.add(host);
        return command;
    }

    /**
     * 获取号池统计信息
     * @param id 号池ID
     * @return 统计数据Map
     */
    @Override
    public Map<String, Object> getStatistics(Long id) {
        List<ChannelEntity> channels = channelService.getChannelsByPoolId(id);
        Map<String, Object> statistics = new HashMap<>(2);

        LocalDate today = LocalDate.now();
        long todayStart = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long yesterdayStart = today.minusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long monthStart = today.withDayOfMonth(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        // 先过滤一次 null，减少多次判空
        List<Long> created = channels.stream()
                .map(ChannelEntity::getCreatedAt)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long todayCount = created.stream().filter(t -> t >= todayStart).count();
        long yesterdayCount = created.stream().filter(t -> t >= yesterdayStart && t < todayStart).count();
        long weekCount = created.stream().filter(t -> t >= weekStart).count();
        long monthCount = created.stream().filter(t -> t >= monthStart).count();
        long totalCount = channels.size();

        Map<String, Long> accountStats = new HashMap<>(5);
        accountStats.put("today", todayCount);
        accountStats.put("yesterday", yesterdayCount);
        accountStats.put("thisWeek", weekCount);
        accountStats.put("thisMonth", monthCount);
        accountStats.put("total", totalCount);

        statistics.put("accountStats", accountStats);
        return statistics;
    }

    /**
     * 获取号池下的错误日志
     * @param id 号池ID
     * @return 错误日志列表
     */
    @Override
    public List<ErrorLogEntity> getErrorLogs(Long id) {
        return errorLogService.getErrorsByPoolId(id);
    }

    /**
     * 规范化代理URL，自动补全协议前缀
     * @param proxyUrl 原始代理地址
     * @return 规范化后的代理地址
     */
    private static String normalizeProxyUrl(String proxyUrl) {
        if (CharSequenceUtil.isBlank(proxyUrl)) return proxyUrl;
        String lower = proxyUrl.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("socks5://")) {
            return proxyUrl;
        }
        // 原逻辑使用 or 导致始终套前缀，这里修正：默认 socks5
        return "socks5://" + proxyUrl;
    }

    /**
     * 校验API调用结果是否成功，否则抛出异常
     * @param r 返回结果
     * @param defaultMsg 默认错误信息
     * @param <T> 泛型
     */
    private static <T> void ensureSuccess(R<T> r, String defaultMsg) {
        if (r == null || !Boolean.TRUE.equals(r.getSuccess())) {
            String msg = r == null ? defaultMsg : (CharSequenceUtil.isBlank(r.getMessage()) ? defaultMsg : r.getMessage());
            throw new RuntimeException(msg);
        }
    }
}
