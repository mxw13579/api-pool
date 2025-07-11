package com.fufu.apipool.mapper;

import com.fufu.apipool.domain.dto.ProxyBindCountDTO;
import com.fufu.apipool.entity.PoolProxyRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 号池与代理关联关系Mapper接口
 *
 * @author lizelin
 */
@Mapper
public interface PoolProxyRelationMapper {
    /**
     * 新增关联关系
     * @param relation 关联关系实体
     * @return 影响行数
     */
    int insert(PoolProxyRelationEntity relation);

    /**
     * 根据号池ID查询所有关联
     * @param poolId 号池ID
     * @return 关联关系列表
     */
    List<PoolProxyRelationEntity> selectByPoolId(@Param("poolId") Long poolId);

    /**
     * 根据代理ID查询所有关联
     * @param proxyId 代理ID
     * @return 关联关系列表
     */
    List<PoolProxyRelationEntity> selectByProxyId(@Param("proxyId") Long proxyId);

    /**
     * 删除关联关系
     * @param id 关联关系ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
    /**
     * 分页查询所有未被软删除的关联关系
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 关联关系列表
     */
    List<PoolProxyRelationEntity> selectAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询所有未被软删除的关联关系总数
     * @return 总数
     */
    int countAll();

    /**
     * [新增] 根据一批代理ID，批量查询每个代理的绑定数量
     * @param proxyIds 代理ID列表
     * @return 包含代理ID和绑定数量的列表
     */
    List<ProxyBindCountDTO> countByProxyIds(@Param("proxyIds") List<Long> proxyIds);
}
