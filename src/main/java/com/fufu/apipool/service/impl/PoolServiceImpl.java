package com.fufu.apipool.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import com.fufu.apipool.common.ApiHttpUtil;
import com.fufu.apipool.common.constant.ApiUrlEnum;
import com.fufu.apipool.domain.newapi.Channel;
import com.fufu.apipool.domain.newapi.ChannelPageData;
import com.fufu.apipool.domain.newapi.R;
import com.fufu.apipool.entity.PoolEntity;
import com.fufu.apipool.mapper.PoolMapper;
import com.fufu.apipool.service.PoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param channel 渠道信息
     * @return Boolean 添加结果
     */
    @Override
    public Boolean addChannelByPoolId(Long poolId, Channel channel) {
        String send = apiHttpUtil.send(poolId, ApiUrlEnum.ADD, null, null, channel);
        R r = JSON.parseObject(send, R.class);
        if (!r.getSuccess().equals(true)) {
            log.error("添加渠道失败：{}", r);
            throw new RuntimeException("测试渠道失败");
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
}
