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

import java.util.ArrayList;
import java.util.Collections;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;

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
    private final ErrorLogService errorLogService;
    private final ChannelService channelService;

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
        if (r.getData() == null) {
            throw new RuntimeException("更新渠道失败：API未返回渠道信息");
        }
        ChannelEntity channelEntity = new ChannelEntity(r.getData(), poolId);
        channelService.updateChannel(channelEntity);
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
        pathVars.put("model", "gemini-2.5-pro");
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
            if (!proxyUrl.startsWith("socks5") || !proxyUrl.startsWith("http")) {
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

        Channel createdChannel = r.getData();
        if (createdChannel == null) {
            log.warn("API添加渠道成功，但未返回渠道信息，使用本地数据进行补偿。PoolId: {}, ChannelName: {}", poolId, channel.getName());
            createdChannel = channel; // 使用我们发送的channel对象
        }

        ChannelEntity channelEntity = new ChannelEntity(createdChannel, poolId);

        channelService.addChannel(channelEntity);

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
        channelService.deleteChannel(poolId, channelId);
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
    /**
     * 渠道分组
     */
    private enum ChannelGroup {
        ACTIVE, // 激活
        RETRYABLE, // 可重试
        PRISTINE, // 闲置（未失败过）
        PERMANENTLY_FAILED // 永久失败（超过最大重试次数）
    }

    /**
     * 渠道测试执行结果的封装
     * @param isSuccess 是否成功
     * @param errorMessage 错误信息（如果失败）
     */
    private record TestExecutionResult(boolean isSuccess, String errorMessage) {}

    /**
     * 渠道测试结果的封装
     * @param channel 渠道对象
     * @param isSuccess 是否成功
     * @param originalGroup 测试前的原始分组
     * @param errorMessage 错误信息（如果失败）
     */
    private record ChannelTestResult(Channel channel, boolean isSuccess, ChannelGroup originalGroup, String errorMessage) {}

    /**
     * 重构后的核心监控逻辑。
     * 将所有测试任务并行化，然后统一处理结果，以避免因单个任务缓慢而阻塞整个流程。
     */
    private void doMonitorPool(Long poolId) {
        PoolEntity pool = poolMapper.selectById(poolId);
        if (pool == null) {
            log.error("监控任务失败：找不到ID为 {} 的号池。", poolId);
            return;
        }

        // 1. 获取并按使用额度排序所有渠道
        List<Channel> allChannels = getChannelsByPoolId(poolId).stream()
                .sorted(Comparator.comparingInt(Channel::getUsedQuota).reversed())
                .collect(Collectors.toList());

        int maxRetries = pool.getMaxMonitorRetries() == null ? 5 : pool.getMaxMonitorRetries();
        int minActive = pool.getMinActiveChannels() == null ? 1 : pool.getMinActiveChannels();

        // 2. 将渠道分类
        Map<ChannelGroup, List<Channel>> groupedChannels = categorizeChannels(allChannels, maxRetries);
        log.info("号池[{}]: 监控开始. 激活: {}, 待重试: {}, 闲置: {}, 永久失败: {}.",
                pool.getName(),
                groupedChannels.getOrDefault(ChannelGroup.ACTIVE, Collections.emptyList()).size(),
                groupedChannels.getOrDefault(ChannelGroup.RETRYABLE, Collections.emptyList()).size(),
                groupedChannels.getOrDefault(ChannelGroup.PRISTINE, Collections.emptyList()).size(),
                groupedChannels.getOrDefault(ChannelGroup.PERMANENTLY_FAILED, Collections.emptyList()).size());

        // 3. 第一阶段：并发测试核心渠道（激活、待重试）
        List<CompletableFuture<ChannelTestResult>> coreTestFutures = new ArrayList<>();
        addTestTasks(coreTestFutures, groupedChannels.getOrDefault(ChannelGroup.ACTIVE, Collections.emptyList()), pool.getId(), ChannelGroup.ACTIVE, true);
        addTestTasks(coreTestFutures, groupedChannels.getOrDefault(ChannelGroup.RETRYABLE, Collections.emptyList()), pool.getId(), ChannelGroup.RETRYABLE, false);

        CompletableFuture.allOf(coreTestFutures.toArray(new CompletableFuture[0])).join();
        List<ChannelTestResult> finalResults = coreTestFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // 4. 评估是否需要补充渠道
        long survivingActiveCount = finalResults.stream()
                .filter(r -> r.originalGroup() == ChannelGroup.ACTIVE && r.isSuccess())
                .count();

        int needed = minActive - (int) survivingActiveCount;

        // 5. 第二阶段：如果需要，则并发测试闲置渠道
        if (needed > 0) {
            log.info("号池[{}]: 核心渠道测试后存活 {} 个，少于要求的 {} 个，需要补充 {} 个。开始测试闲置渠道...",
                    pool.getName(), survivingActiveCount, minActive, needed);

            List<Channel> pristineChannels = groupedChannels.getOrDefault(ChannelGroup.PRISTINE, Collections.emptyList());
            if (!pristineChannels.isEmpty()) {
                List<CompletableFuture<ChannelTestResult>> pristineTestFutures = new ArrayList<>();
                addTestTasks(pristineTestFutures, pristineChannels, pool.getId(), ChannelGroup.PRISTINE, false);

                CompletableFuture.allOf(pristineTestFutures.toArray(new CompletableFuture[0])).join();
                List<ChannelTestResult> pristineResults = pristineTestFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
                finalResults.addAll(pristineResults); // 将第二阶段结果合并
            } else {
                log.warn("号池[{}]: 需要补充渠道，但已无闲置渠道可供测试。", pool.getName());
            }
        } else {
            log.info("号池[{}]: 核心渠道测试后存活 {} 个，满足最低要求 {} 个，无需测试闲置渠道。",
                    pool.getName(), survivingActiveCount, minActive);
        }

        // 6. 集中处理所有已执行的测试结果
        processTestResults(finalResults, pool, minActive);
    }

    /**
     * 根据渠道状态和名称中的重试次数对渠道进行分类。
     */
    private Map<ChannelGroup, List<Channel>> categorizeChannels(List<Channel> channels, int maxRetries) {
        return channels.stream().collect(Collectors.groupingBy(channel -> {
            if (channel.getStatus() == ChannelStatus.ENABLED) {
                return ChannelGroup.ACTIVE;
            }
            NameParseResult result = parseChannelName(channel.getName());
            int retries = result.retries();
            if (retries == 0) {
                return ChannelGroup.PRISTINE;
            } else if (retries < maxRetries) {
                return ChannelGroup.RETRYABLE;
            } else {
                return ChannelGroup.PERMANENTLY_FAILED;
            }
        }));
    }

    /**
     * 为指定分组的渠道创建异步测试任务。
     */
    private void addTestTasks(List<CompletableFuture<ChannelTestResult>> futures, List<Channel> channels,
                              Long poolId, ChannelGroup group, boolean withRetries) {
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
     * 统一处理所有渠道的测试结果，执行禁用或激活操作。
     */
    private void processTestResults(List<ChannelTestResult> results, PoolEntity pool, int minActive) {
        Map<Boolean, List<ChannelTestResult>> partitionedBySuccess = results.stream()
                .collect(Collectors.partitioningBy(ChannelTestResult::isSuccess));

        List<ChannelTestResult> successes = partitionedBySuccess.getOrDefault(true, Collections.emptyList());
        List<ChannelTestResult> failures = partitionedBySuccess.getOrDefault(false, Collections.emptyList());

        // 1. 处理所有失败的测试
        for (ChannelTestResult failure : failures) {
            Channel channel = failure.channel();
            NameParseResult nameResult = parseChannelName(channel.getName());
            int nextRetryCount = nameResult.retries() + 1;
            channel.setName(nameResult.originalName() + " -监控失败- " + nextRetryCount + "次");
            if (failure.originalGroup() == ChannelGroup.ACTIVE) {
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
            errorLog.setErrorMessage(CharSequenceUtil.isNotBlank(failure.errorMessage()) ? failure.errorMessage() : "监控测试失败");
            errorLogService.logError(errorLog);

            safeUpdateChannel(pool.getId(), channel);
        }

        // 2. 确定当前仍然存活的激活渠道数量
        long survivingActiveCount = successes.stream()
                .filter(r -> r.originalGroup() == ChannelGroup.ACTIVE)
                .count();

        // 3. 确定需要激活的新渠道数量
        int needed = minActive - (int) survivingActiveCount;
        if (needed <= 0) {
            log.info("号池[{}]: 激活渠道数量 {} >= 最小要求 {}，无需激活新渠道。监控任务执行完毕。", pool.getName(), survivingActiveCount, minActive);
            return;
        }

        // 4. 从测试成功的候选中筛选并激活
        List<Channel> activationCandidates = successes.stream()
                .filter(r -> r.originalGroup() == ChannelGroup.RETRYABLE || r.originalGroup() == ChannelGroup.PRISTINE)
                .map(ChannelTestResult::channel)
                .collect(Collectors.toList());

        if (activationCandidates.isEmpty()) {
            log.warn("号池[{}]: 需要激活 {} 个渠道，但没有可用的候选渠道。监控任务执行完毕。", pool.getName(), needed);
            return;
        }

        log.info("号池[{}]: 当前激活 {} < 最小要求 {}, 需激活 {}个。开始从 {} 个合格候选者中激活...",
                pool.getName(), survivingActiveCount, minActive, needed, activationCandidates.size());

        activationCandidates.stream()
                .limit(needed)
                .forEach(channelToActivate -> {
                    NameParseResult nameResult = parseChannelName(channelToActivate.getName());
                    channelToActivate.setName(nameResult.originalName()); // 恢复原始名称
                    channelToActivate.setStatus(ChannelStatus.ENABLED);
                    safeUpdateChannel(pool.getId(), channelToActivate);
                    log.info("号池[{}]: 已成功激活渠道 [{}({})], 当前使用额度{}.", pool.getName(), channelToActivate.getName(), channelToActivate.getId(), (channelToActivate.getUsedQuota() / 500000));
                });

        log.info("号池[{}]: 监控任务执行完毕。", pool.getName());
    }

        private TestExecutionResult testChannelWithRetries(Long poolId, Long channelId, int retries, long delayMillis) {
        String lastExceptionMessage = "";
        for (int i = 0; i <= retries; i++) {
            try {
                testChannelByPoolId(poolId, channelId);
                return new TestExecutionResult(true, null); // 成功
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
        return new TestExecutionResult(false, lastExceptionMessage); // 所有尝试都失败了
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

    @Override
    public long testLatency(Long id) {
        PoolEntity poolEntity = selectById(id);
        String endpoint = poolEntity.getEndpoint();
        if (endpoint == null || endpoint.isEmpty()) {
            return -1L;
        }

        String host;
        try {
            // 使用 URI 解析，自动去除协议和端口
            java.net.URI uri = endpoint.contains("://") ? new java.net.URI(endpoint) : new java.net.URI("http://" + endpoint);
            host = uri.getHost();
            if (host == null) {
                // 如果没有协议，getHost 可能为 null，尝试直接用 endpoint
                host = endpoint.contains(":") ? endpoint.substring(0, endpoint.indexOf(":")) : endpoint;
            }
        } catch (Exception e) {
            log.error("Invalid endpoint URI: " + endpoint, e);
            return -1L;
        }

        try {
            // 判断操作系统，设置不同的 ping 命令参数
            String os = System.getProperty("os.name").toLowerCase();
            List<String> command = new ArrayList<>();
            command.add("ping");
            if (os.contains("win")) {
                command.add("-n");
            } else {
                command.add("-c");
            }
            command.add("5");
            command.add(host);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                List<Double> latencies = new ArrayList<>();
                // 兼容 Windows 和 Linux 的延迟正则
                Pattern pattern = Pattern.compile("(time|时间|ʱ��)[=<]?([\\d.]+) ?ms", Pattern.CASE_INSENSITIVE);
                while ((line = reader.readLine()) != null) {
                    log.info("ping日志: {}", line); // 打印每一行日志
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        // group(2) 是延迟数值
                        latencies.add(Double.parseDouble(matcher.group(2)));
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0 && !latencies.isEmpty()) {
                    double average = latencies.stream().mapToDouble(a -> a).average().orElse(-1.0);
                    return (long) average;
                } else {
                    return -1L;
                }
            }
        } catch (Exception e) {
            log.error("Ping failed for address: " + endpoint, e);
            return -1L;
        }
    }


    @Override
    public Map<String, Object> getStatistics(Long id) {
        List<ChannelEntity> channels = channelService.getChannelsByPoolId(id);
        Map<String, Object> statistics = new HashMap<>();

        LocalDate today = LocalDate.now();
        long todayStart = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long yesterdayStart = today.minusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long monthStart = today.withDayOfMonth(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        long todayCount = channels.stream().filter(c -> c.getCreatedAt() != null && c.getCreatedAt() >= todayStart).count();
        long yesterdayCount = channels.stream().filter(c -> c.getCreatedAt() != null && c.getCreatedAt() >= yesterdayStart && c.getCreatedAt() < todayStart).count();
        long weekCount = channels.stream().filter(c -> c.getCreatedAt() != null && c.getCreatedAt() >= weekStart).count();
        long monthCount = channels.stream().filter(c -> c.getCreatedAt() != null && c.getCreatedAt() >= monthStart).count();
        long totalCount = channels.size();

        Map<String, Long> accountStats = new HashMap<>();
        accountStats.put("today", todayCount);
        accountStats.put("yesterday", yesterdayCount);
        accountStats.put("thisWeek", weekCount);
        accountStats.put("thisMonth", monthCount);
        accountStats.put("total", totalCount);

        statistics.put("accountStats", accountStats);
        return statistics;
    }

    /**
     * 获取号池错误日志
     * @param id 号池ID
     * @return 错误日志
     */
    @Override
    public List<ErrorLogEntity> getErrorLogs(Long id) {
        return errorLogService.getErrorsByPoolId(id);
    }
}
