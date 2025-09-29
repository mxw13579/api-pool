package com.fufu.apipool.controller;

import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.domain.dto.PageRequest;
import com.fufu.apipool.domain.dto.ProxyBatchesEntity;
import com.fufu.apipool.domain.vo.PageResult;
import com.fufu.apipool.domain.vo.ProxyVO;
import com.fufu.apipool.entity.ProxyEntity;
import com.fufu.apipool.service.ProxyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Proxy management REST endpoints.
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/proxy")
@AllArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;

    /**
     * Paginated proxy listing.
     */
    @GetMapping("/list")
    public Result<PageResult<ProxyVO>> list(@Valid PageRequest pageRequest) {
        PageResult<ProxyVO> pageResult = proxyService.selectPage(pageRequest);
        return ResultUtil.getSuccessResult(pageResult);
    }

    /**
     * Full proxy list (legacy compatibility).
     */
    @GetMapping("/list/all")
    public Result<List<ProxyVO>> listAll() {
        List<ProxyVO> list = proxyService.selectAll();
        return ResultUtil.getSuccessResult(list);
    }

    /**
     * Fetch a proxy by id.
     */
    @GetMapping("/{id}")
    public Result<ProxyVO> getById(@PathVariable Long id) {
        ProxyVO entity = proxyService.selectById(id);
        return ResultUtil.getSuccessResult(entity);
    }

    /**
     * Create a proxy.
     */
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody ProxyEntity proxyEntity) {
        int count = proxyService.insert(proxyEntity);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * Batch create proxies.
     */
    @PostMapping("/add-batches")
    public Result<Integer> addBatches(@RequestBody ProxyBatchesEntity proxyBatches) {
        int count = proxyService.addBatches(proxyBatches);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * Update a proxy.
     */
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody ProxyEntity proxyEntity) {
        int count = proxyService.update(proxyEntity);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * Delete a proxy by id.
     */
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable Long id) {
        int count = proxyService.deleteById(id);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * Batch delete proxies.
     */
    @DeleteMapping("/delete-batches")
    public Result<Integer> deleteBatches(@RequestBody List<Long> ids) {
        int count = proxyService.deleteByIds(ids);
        return ResultUtil.getSuccessResult(count);
    }
}
