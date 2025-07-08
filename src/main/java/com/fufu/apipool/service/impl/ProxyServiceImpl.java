package com.fufu.apipool.service.impl;

import com.fufu.apipool.entity.ProxyEntity;
import com.fufu.apipool.mapper.ProxyMapper;
import com.fufu.apipool.service.ProxyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代理服务实现类
 * 提供代理的增删改查操作
 * @author lizelin
 */
@Slf4j
@Service
@AllArgsConstructor
public class ProxyServiceImpl implements ProxyService {

    private final ProxyMapper proxyMapper;

    /**
     * 根据ID查询代理
     * @param id 代理ID
     * @return ProxyEntity 代理实体
     */
    @Override
    public ProxyEntity selectById(Long id) {
        return proxyMapper.selectById(id);
    }

    /**
     * 查询所有代理
     * @return List<ProxyEntity> 代理列表
     */
    @Override
    public List<ProxyEntity> selectAll() {
        return proxyMapper.selectAll();
    }

    /**
     * 新增代理
     * @param proxyEntity 代理实体
     * @return int 插入结果
     */
    @Override
    public int insert(ProxyEntity proxyEntity) {
        return proxyMapper.insert(proxyEntity);
    }

    /**
     * 更新代理
     * @param proxyEntity 代理实体
     * @return int 更新结果
     */
    @Override
    public int update(ProxyEntity proxyEntity) {
        return proxyMapper.update(proxyEntity);
    }

    /**
     * 根据ID删除代理
     * @param id 代理ID
     * @return int 删除结果
     */
    @Override
    public int deleteById(Long id) {
        return proxyMapper.deleteById(id);
    }
}
