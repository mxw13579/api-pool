package com.fufu.apipool.service.impl;

import com.fufu.apipool.domain.dto.ProxyBindCountDTO;
import com.fufu.apipool.entity.PoolProxyRelationEntity;
import com.fufu.apipool.mapper.PoolProxyRelationMapper;
import com.fufu.apipool.service.PoolProxyRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 号池与代理关联关系服务实现类
 *
 * @author lizelin
 */
@Service
public class PoolProxyRelationServiceImpl implements PoolProxyRelationService {

    @Autowired
    private PoolProxyRelationMapper poolProxyRelationMapper;

    @Override
    public boolean addRelation(PoolProxyRelationEntity relation) {
        return poolProxyRelationMapper.insert(relation) > 0;
    }

    @Override
    public List<PoolProxyRelationEntity> getRelationsByPoolId(Long poolId) {
        return poolProxyRelationMapper.selectByPoolId(poolId);
    }

    @Override
    public List<PoolProxyRelationEntity> getRelationsByProxyId(Long proxyId) {
        return poolProxyRelationMapper.selectByProxyId(proxyId);
    }

    @Override
    public boolean deleteRelation(Long id) {
        return poolProxyRelationMapper.deleteById(id) > 0;
    }

    @Override
    public List<PoolProxyRelationEntity> getAllRelations(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return poolProxyRelationMapper.selectAll(offset, pageSize);
    }

    @Override
    public int getAllRelationsCount() {
        return poolProxyRelationMapper.countAll();
    }

    /**
     * 批量获取多个代理的绑定数量
     * @param proxyIds 代理ID列表
     * @return Map，key为代理ID，value为绑定数量
     */
    @Override
    public Map<Long, Integer> getBindCountsForProxies(List<Long> proxyIds) {
        if (CollectionUtils.isEmpty(proxyIds)) {
            return Collections.emptyMap();
        }
        List<ProxyBindCountDTO> counts = poolProxyRelationMapper.countByProxyIds(proxyIds);
        return counts.stream()
                .collect(Collectors.toMap(ProxyBindCountDTO::getProxyId, ProxyBindCountDTO::getCount));
    }

}
