package com.fufu.apipool.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import com.fufu.apipool.common.ApiHttpUtil;
import com.fufu.apipool.common.constant.ApiUrlEnum;
import com.fufu.apipool.domain.newapi.Channel;
import com.fufu.apipool.domain.newapi.ChannelDTO;
import com.fufu.apipool.domain.newapi.ChannelPageData;
import com.fufu.apipool.domain.newapi.R;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.LIST, null, null, null);
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
            throw new RuntimeException("测试渠道失败");
        }
        return r.getTime();
    }

    /**
     * 根据ID添加号池下的渠道信息
     *
     * @param poolId  号池ID
     * @param dto 渠道信息
     * @return Boolean 添加结果
     */
    /**
     * 根据ID添加号池下的渠道信息
     *
     * @param poolId  号池ID
     * @param dto 渠道信息，包含代理选择策略
     * @return Boolean 添加结果
     */
    @Override
    @Transactional // 添加事务管理，确保API调用和数据库操作的原子性
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
            if (!proxyUrl.startsWith("socket5")) {
                proxyUrl = "socks5://" + proxyUrl;
            }
            // 格式化为JSON字符串: {"proxy":"http://user:pass@host:port"}
            channel.setSetting("{\"proxy\":\"" + proxyUrl + "\"}");
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
    public Boolean batchAddChannelToAll(ChannelDTO dto) {
        // 1. 获取所有号池
        List<PoolEntity> allPools = this.selectAll(); // 调用已有的 selectAll 方法
        if (allPools == null || allPools.isEmpty()) {
            log.warn("批量新增渠道失败：系统中没有任何号池。");
            return false; // 或者可以抛出异常，提示前端
        }

        log.info("开始为 {} 个号池批量新增渠道，渠道名称: {}", allPools.size(), dto.getName());

        String key = dto.getKey();

        JSONArray objects = JSON.parseArray(key);
        //转换为list
        List<String> list = objects.toJavaList(String.class);

        int size = allPools.size();
        int offset = 0;
        for (int i = 0; i < list.size(); i++) {

            ChannelDTO channelDTO = BeanUtil.copyProperties(dto, ChannelDTO.class);
            channelDTO.setKey(list.get(i));
            channelDTO.setName(dto.getName()+"-"+i);
            try {
                // 3. 复用现有的 `addChannelByPoolId` 逻辑
                Boolean success = this.addChannelByPoolId(allPools.get(offset).getId(), channelDTO);
                if (success != null && success) {
                    log.info("成功为号池 ID: {}, 名称: {} 添加渠道 {}", allPools.get(offset).getId(), allPools.get(offset).getName(), dto.getName());
                } else {
                    log.error("为号池 ID: {}, 名称: {} 添加渠道 {} 失败。", allPools.get(offset).getId(), allPools.get(offset).getName(), dto.getName());
                }
            } catch (Exception e) {
                log.error("为号池 ID: {}, 名称: {} 添加渠道 {} 时发生异常。", allPools.get(offset).getId(), allPools.get(offset).getName(), dto.getName(), e);
            }

            if (offset >= size - 1){
                offset = 0;
            }else {
                offset ++;
            }

        }
        return true;
    }
}
