package com.fufu.apipool.service.impl;

import com.fufu.apipool.common.BaseEntity;
import com.fufu.apipool.common.util.SqlSecurityUtil;
import com.fufu.apipool.domain.dto.PageRequest;
import com.fufu.apipool.domain.dto.ProxyBatchesEntity;
import com.fufu.apipool.domain.vo.PageResult;
import com.fufu.apipool.entity.ProxyEntity;
import com.fufu.apipool.mapper.ProxyMapper;
import com.fufu.apipool.service.PoolProxyRelationService;
import com.fufu.apipool.service.ProxyCacheService;
import com.fufu.apipool.service.ProxyService;
import com.fufu.apipool.domain.vo.ProxyVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代理服务实现类
 * 提供代理的增删改查操作
 * @author lizelin
 */
@Slf4j
@Service

public class ProxyServiceImpl implements ProxyService {

    @Autowired@Lazy
    private ProxyMapper proxyMapper;
    @Autowired@Lazy
    private PoolProxyRelationService poolProxyRelationService;
    @Autowired@Lazy
    private ProxyCacheService proxyCacheService;

    /**
     * 根据ID查询代理
     * @param id 代理ID
     * @return ProxyVO 代理视图对象
     */
    @Override
    public ProxyVO selectById(Long id) {
        ProxyEntity entity = proxyMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        // 转换为VO
        ProxyVO vo = ProxyVO.fromEntity(entity);
        // 查询并设置绑定数量
        int count = poolProxyRelationService.getRelationsByProxyId(id).size();
        vo.setBindCount(count);
        return vo;
    }

    /**
     * 分页查询所有代理
     * @param pageRequest 分页请求参数
     * @return PageResult<ProxyVO> 分页结果
     */
    @Override
    public PageResult<ProxyVO> selectPage(PageRequest pageRequest) {
        // 设置分页参数 - 使用SQL安全工具类防止注入
        String safeOrderClause = SqlSecurityUtil.buildSafeOrderClause(
            pageRequest.getOrderBy(),
            pageRequest.getOrderDirection(),
            SqlSecurityUtil.PROXY_ALLOWED_COLUMNS,
            "proxy"
        );

        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize(), safeOrderClause);

        // 查询数据
        List<ProxyEntity> entities = proxyMapper.selectAll();
        PageInfo<ProxyEntity> pageInfo = new PageInfo<>(entities);

        if (CollectionUtils.isEmpty(entities)) {
            return PageResult.of(Collections.emptyList(), pageInfo.getTotal(),
                                pageRequest.getPageNum(), pageRequest.getPageSize());
        }

        // 1. 提取所有代理ID
        List<Long> proxyIds = entities.stream()
                .map(BaseEntity::getId)
                .collect(Collectors.toList());

        // 2. 一次性批量查询所有代理的绑定数量
        Map<Long, Integer> countMap = poolProxyRelationService.getBindCountsForProxies(proxyIds);

        // 3. 组装VO列表
        List<ProxyVO> voList = entities.stream().map(entity -> {
            ProxyVO vo = ProxyVO.fromEntity(entity);
            vo.setBindCount(countMap.getOrDefault(entity.getId(), 0));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, pageInfo.getTotal(),
                           pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    /**
     * 查询所有代理（批量优化）
     * @return List<ProxyVO> 代理视图对象列表
     */
    @Override
    public List<ProxyVO> selectAll() {
        List<ProxyEntity> entities = proxyMapper.selectAll();
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        // 1. 提取所有代理ID
        List<Long> proxyIds = entities.stream()
                .map(BaseEntity::getId)
                .collect(Collectors.toList());

        // 2. 一次性批量查询所有代理的绑定数量
        Map<Long, Integer> countMap = poolProxyRelationService.getBindCountsForProxies(proxyIds);

        // 3. 组装VO列表
        return entities.stream().map(entity -> {
            ProxyVO vo = ProxyVO.fromEntity(entity);
            // 从Map中获取绑定数量，如果不存在则默认为0
            vo.setBindCount(countMap.getOrDefault(entity.getId(), 0));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 新增代理
     * @param proxyEntity 代理实体
     * @return int 插入结果
     */
    @Override
    public int insert(ProxyEntity proxyEntity) {
        int result = proxyMapper.insert(proxyEntity);
        if (result > 0) {
            Long id = proxyMapper.getLastInsertId();
            proxyEntity.setId(id);
            proxyCacheService.addOrUpdateProxyInCache(proxyEntity);
        }
        return result;
    }

    /**
     * 更新代理
     * @param proxyEntity 代理实体
     * @return int 更新结果
     */
    @Override
    public int update(ProxyEntity proxyEntity) {
        int result = proxyMapper.update(proxyEntity);
        if (result > 0) {
            proxyCacheService.addOrUpdateProxyInCache(proxyEntity);
        }
        return result;
    }

    /**
     * 根据ID删除代理
     * @param id 代理ID
     * @return int 删除结果
     */
    @Override
    public int deleteById(Long id) {
//        // 查询该代理被哪些池绑定
//        List<Long> poolIds = poolProxyRelationService.getRelationsByProxyId(id)
//                .stream()
//                .map(relation -> relation.getPoolId())
//                .collect(Collectors.toList());
//        if (!poolIds.isEmpty()) {
//            // 拼接池号
//            String poolStr = poolIds.stream().map(String::valueOf).collect(Collectors.joining(","));
//            throw new RuntimeException("该代理已被池[" + poolStr + "]绑定，无法删除！");
//        }
        int result = proxyMapper.deleteById(id);
        // 删除成功后，移除缓存
        if (result > 0) {
            proxyCacheService.removeProxyFromCache(id);
        }
        return result;
    }

    /**
     * 查询所有代理（原始方法）
     * @return List<ProxyEntity> 代理实体列表
     */
    @Override
    public List<ProxyEntity> selectAllEntities() {
        return proxyMapper.selectAll();
    }

    /**
     * 批量添加代理
     * @param proxyBatches 代理批次实体
     * @return int 添加结果
     */
    @Override
    public int addBatches(ProxyBatchesEntity proxyBatches) {
        String proxyUrlBatches = proxyBatches.getProxyUrlBatches();
        if (proxyUrlBatches == null || proxyUrlBatches.trim().isEmpty()) {
            return 0;
        }
        String[] proxyUrls = proxyUrlBatches.split("\n");
        String prefixName = proxyBatches.getPrefixName();
        int successCount = 0;
        int failCount = 0;
        int i = 1;
        for (String proxyUrl : proxyUrls) {
            proxyUrl = proxyUrl.trim();
            if (proxyUrl.isEmpty()) continue;
            ProxyEntity proxyEntity = new ProxyEntity();
            proxyEntity.setName(prefixName + "-" + i);
            proxyEntity.setProxyUrl(proxyUrl);
            proxyEntity.setStatus(proxyBatches.getStatus());
            proxyEntity.setSource(proxyBatches.getSource());
            proxyEntity.setAddress(proxyBatches.getAddress());
            try {
                int result = proxyMapper.insert(proxyEntity);
                if (result > 0) {
                    Long id = proxyMapper.getLastInsertId();
                    proxyEntity.setId(id);
                    proxyCacheService.addOrUpdateProxyInCache(proxyEntity);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("添加代理失败：{}，{}", proxyUrl, e.getMessage());
                failCount++;
            }
            i++;
        }
        log.info("批量添加代理完成，成功：{}，失败：{}", successCount, failCount);
        return successCount;
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        int result = proxyMapper.deleteByIds(ids);
        if (result > 0) {
            ids.forEach(proxyCacheService::removeProxyFromCache);
        }
        return result;
    }

}

