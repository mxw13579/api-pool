package com.fufu.apipool.service.impl;

import com.fufu.apipool.entity.PoolProxyRelationEntity;
import com.fufu.apipool.mapper.PoolProxyRelationMapper;
import com.fufu.apipool.service.PoolProxyRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 号池与代理关联关系服务实现类
 *
 * @author lizelin
 */
@Service
public class PoolProxyRelationServiceImpl implements PoolProxyRelationService {

    @Autowired
    private PoolProxyRelationMapper relationMapper;

    @Override
    public boolean addRelation(PoolProxyRelationEntity relation) {
        return relationMapper.insert(relation) > 0;
    }

    @Override
    public List<PoolProxyRelationEntity> getRelationsByPoolId(Long poolId) {
        return relationMapper.selectByPoolId(poolId);
    }

    @Override
    public List<PoolProxyRelationEntity> getRelationsByProxyId(Long proxyId) {
        return relationMapper.selectByProxyId(proxyId);
    }

    @Override
    public boolean deleteRelation(Long id) {
        return relationMapper.deleteById(id) > 0;
    }

    @Override
    public List<PoolProxyRelationEntity> getAllRelations(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return relationMapper.selectAll(offset, pageSize);
    }

    @Override
    public int getAllRelationsCount() {
        return relationMapper.countAll();
    }
}
