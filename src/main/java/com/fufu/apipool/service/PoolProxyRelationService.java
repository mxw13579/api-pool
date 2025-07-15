package com.fufu.apipool.service;

import com.fufu.apipool.entity.PoolProxyRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 号池与代理关联关系服务接口
 *
 * @author lizelin
 */
public interface PoolProxyRelationService {
    /**
     * 新增关联关系
     * @param relation 关联关系实体
     * @return 是否成功
     */
    boolean addRelation(PoolProxyRelationEntity relation);

    /**
     * 根据号池ID查询所有关联
     * @param poolId 号池ID
     * @return 关联关系列表
     */
    List<PoolProxyRelationEntity> getRelationsByPoolId(Long poolId);

    /**
     * 根据代理ID查询所有关联
     * @param proxyId 代理ID
     * @return 关联关系列表
     */
    List<PoolProxyRelationEntity> getRelationsByProxyId(Long proxyId);

    /**
     * 删除关联关系
     * @param id 关联关系ID
     * @return 是否成功
     */
    boolean deleteRelation(Long id);

    /**
     * 分页查询所有未被软删除的关联关系
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @return 关联关系列表
     */
    List<PoolProxyRelationEntity> getAllRelations(int pageNum, int pageSize);

    /**
     * 查询所有未被软删除的关联关系总数
     * @return 总数
     */
    int getAllRelationsCount();

    /**
     * 批量获取多个代理的绑定数量
     * @param proxyIds 代理ID列表
     * @return Map，key为代理ID，value为绑定数量
     */
    Map<Long, Integer> getBindCountsForProxies(List<Long> proxyIds);

    /**
     * 查询最后使用的代理ID
     *
     */
    Long findLastUsedProxyId();
}
