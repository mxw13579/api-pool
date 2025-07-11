package com.fufu.apipool.service;

import com.fufu.apipool.domain.newapi.Channel;
import com.fufu.apipool.domain.newapi.ChannelDTO;
import com.fufu.apipool.entity.PoolEntity;

import java.util.List;

/**
 * 号池服务接口
 * 提供号池的增删改查操作
 * @author lizelin
 */
public interface PoolService {

    /**
     * 根据ID查询号池
     * @param id 号池ID
     * @return 号池实体
     */
    PoolEntity selectById(Long id);

    /**
     * 查询所有号池
     * @return 号池列表
     */
    List<PoolEntity> selectAll();

    /**
     * 新增号池
     * @param poolEntity 号池实体
     * @return 插入行数
     */
    int insert(PoolEntity poolEntity);

    /**
     * 更新号池
     * @param poolEntity 号池实体
     * @return 更新行数
     */
    int update(PoolEntity poolEntity);

    /**
     * 根据ID删除号池
     * @param id 号池ID
     * @return 删除行数
     */
    int deleteById(Long id);

    /**
     * 根据号池ID查询渠道
     * @param poolId 号池ID
     * @return 渠道列表
     */
    List<Channel> getChannelsByPoolId(Long poolId);

    /**
     * 根据号池ID更新渠道
     * @param poolId 号池ID
     * @param channel 渠道实体
     * @return 更新行数
     */
    Boolean updateChannelByPoolId(Long poolId, Channel channel);

    /**
     * 根据ID测试号池下的渠道信息
     * @param poolId 号池ID
     * @param channelId 渠道id
     * @return long 测试结果
     */
    long testChannelByPoolId(Long poolId, Long channelId);

    /**
     * 根据号池ID添加渠道信息
     *
     * @param poolId  号池ID
     * @param dto
     * @return 添加结果
     */
    Boolean addChannelByPoolId(Long poolId, ChannelDTO dto);

    /**
     * 根据 号池ID和渠道ID查询渠道信息
     * @param poolId 号池ID
     * @param channelId 渠道ID
     * @return 渠道信息
     */
    Channel getChannelByPoolId(Long poolId, Long channelId);

    /**
     * 根据号池ID和渠道ID删除渠道信息
     * @param poolId 号池ID
     * @param channelId 渠道信息
     * @return 删除结果
     */
    Boolean deleteChannelByPoolId(Long poolId, Long channelId);

    /**
     * 批量添加渠道信息到所有号池
     * @param dto 渠道信息
     * @return 添加结果
     */
    List<String> batchAddChannelToAll(ChannelDTO dto);
}
