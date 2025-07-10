package com.fufu.apipool.controller;

import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.entity.PoolProxyRelationEntity;
import com.fufu.apipool.service.PoolProxyRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 号池与代理关联关系控制器
 *
 * 提供关联关系的增删查接口
 *
 * @author lizelin
 */
@RestController
@RequestMapping("/api/pool-proxy-relation")
public class PoolProxyRelationController {

    @Autowired
    private PoolProxyRelationService relationService;

    /**
     * 新增关联关系
     * @param relation 关联关系实体
     * @return 是否成功
     */
    @PostMapping("/add")
    public Result<Boolean> addRelation(@RequestBody PoolProxyRelationEntity relation) {
        return ResultUtil.getSuccessResult(relationService.addRelation(relation));
    }

    /**
     * 根据号池ID查询所有关联
     * @param poolId 号池ID
     * @return 关联关系列表
     */
    @GetMapping("/by-pool/{poolId}")
    public Result<List<PoolProxyRelationEntity>> getRelationsByPoolId(@PathVariable Long poolId) {
        return ResultUtil.getSuccessResult(relationService.getRelationsByPoolId(poolId));
    }

    /**
     * 根据代理ID查询所有关联
     * @param proxyId 代理ID
     * @return 关联关系列表
     */
    @GetMapping("/by-proxy/{proxyId}")
    public Result<List<PoolProxyRelationEntity>> getRelationsByProxyId(@PathVariable Long proxyId) {
        return ResultUtil.getSuccessResult(relationService.getRelationsByProxyId(proxyId));
    }

    /**
     * 删除关联关系
     * @param id 关联关系ID
     * @return 是否成功
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteRelation(@PathVariable Long id) {
        return ResultUtil.getSuccessResult(relationService.deleteRelation(id));
    }

    /**
     * 分页查询所有未被软删除的关联关系
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页结果（包含列表和总数）
     */
    @GetMapping("/list")
    public Map<String, Object> getAllRelations(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<PoolProxyRelationEntity> list = relationService.getAllRelations(pageNum, pageSize);
        int total = relationService.getAllRelationsCount();
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }
}
