package com.fufu.apipool.controller;

import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.domain.dto.ProxyBatchesEntity;
import com.fufu.apipool.domain.vo.ProxyVO;
import com.fufu.apipool.entity.ProxyEntity;
import com.fufu.apipool.service.ProxyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代理控制器
 * 提供代理相关的REST接口
 * @author lizelin
 */
@Slf4j
@RestController
@RequestMapping("/api/proxy")
@AllArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;

    /**
     * 查询所有代理
     *
     * @return Result对象，包含代理列表
     */
    @GetMapping("/list")
    public Result<List<ProxyVO>> list() {
        List<ProxyVO> list = proxyService.selectAll();
        return ResultUtil.getSuccessResult(list);
    }

    /**
     * 根据ID查询代理
     *
     * @param id 代理ID
     * @return Result对象，包含代理信息
     */
    @GetMapping("/{id}")
    public Result<ProxyVO> getById(@PathVariable Long id) {
        ProxyVO entity = proxyService.selectById(id);
        return ResultUtil.getSuccessResult(entity);
    }

    /**
     * 新增代理
     *
     * @param proxyEntity 代理实体
     * @return Result对象，包含插入行数
     */
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody ProxyEntity proxyEntity) {
        int count = proxyService.insert(proxyEntity);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * 批量新增代理
     *
     * @param proxyBatches 代理实体
     * @return Result对象，包含插入行数
     */
    @PostMapping("/add-batches")
    public Result<Integer> addBatches(@RequestBody ProxyBatchesEntity proxyBatches) {
        int count = proxyService.addBatches(proxyBatches);
        return ResultUtil.getSuccessResult(count);
    }


    /**
     * 更新代理
     *
     * @param proxyEntity 代理实体
     * @return Result对象，包含更新行数
     */
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody ProxyEntity proxyEntity) {
        int count = proxyService.update(proxyEntity);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * 删除代理
     *
     * @param id 代理ID
     * @return Result对象，包含删除行数
     */
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable Long id) {
        int count = proxyService.deleteById(id);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * 批量删除代理
     *
     * @param ids 代理ID列表
     * @return Result对象，包含删除行数
     */
    @DeleteMapping("/delete-batches")
    public Result<Integer> deleteBatches(@RequestBody List<Long> ids) {
        int count = proxyService.deleteByIds(ids);
        return ResultUtil.getSuccessResult(count);
    }
}
