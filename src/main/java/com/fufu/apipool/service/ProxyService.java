package com.fufu.apipool.service;

import com.fufu.apipool.domain.dto.PageRequest;
import com.fufu.apipool.domain.dto.ProxyBatchesEntity;
import com.fufu.apipool.domain.vo.PageResult;
import com.fufu.apipool.domain.vo.ProxyVO;
import com.fufu.apipool.entity.ProxyEntity;

import java.util.List;

/**
 * 代理服务接口
 * 提供代理的增删改查操作
 * @author lizelin
 */
public interface ProxyService {

    /**
     * 根据ID查询代理
     * @param id 代理ID
     * @return 代理实体
     */
    ProxyVO selectById(Long id);

    /**
     * 分页查询代理列表
     * @param pageRequest 分页请求参数
     * @return 分页结果
     */
    PageResult<ProxyVO> selectPage(PageRequest pageRequest);

    /**
     * 查询所有代理
     * @return 代理列表
     */
    List<ProxyVO > selectAll();

    /**
     * 新增代理
     * @param proxyEntity 代理实体
     * @return 插入行数
     */
    int insert(ProxyEntity proxyEntity);

    /**
     * 更新代理
     * @param proxyEntity 代理实体
     * @return 更新行数
     */
    int update(ProxyEntity proxyEntity);

    /**
     * 根据ID删除代理
     * @param id 代理ID
     * @return 删除行数
     */
    int deleteById(Long id);

    /**
     * 新增：查询所有代理实体
     * @return List<ProxyEntity> 代理实体列表
     */
    List<ProxyEntity> selectAllEntities();

    /**
     * 新增：批量添加代理
     * @param proxyBatches 代理批次实体
     * @return 添加行数
     */
    int addBatches(ProxyBatchesEntity proxyBatches);

    /**
     * 批量删除代理
     * @param ids 代理ID列表
     * @return 删除行数
     */
    int deleteByIds(List<Long> ids);
}
