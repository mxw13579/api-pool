package com.fufu.apipool.service;

import com.fufu.apipool.domain.newapi.Channel;
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

    List<Channel> getChannelsByPoolId(Long poolId);
}
