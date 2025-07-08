package com.fufu.apipool.controller;

import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.domain.newapi.Channel;
import com.fufu.apipool.entity.PoolEntity;
import com.fufu.apipool.service.PoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 号池控制器
 * 提供号池相关的REST接口
 * @author lizelin
 */
@Slf4j
@RestController
@RequestMapping("/api/pool")
@AllArgsConstructor
public class PoolController {

    private final PoolService poolService;


    @GetMapping("/getChannelsByPoolId")
    public Result<List<Channel>> getChannelsByPoolId(@RequestParam Long poolId) {
        return ResultUtil.getSuccessResult( poolService.getChannelsByPoolId(poolId));
    }
    /**
     * 查询所有号池
     *
     * @return Result对象，包含号池列表
     */
    @GetMapping("/list")
    public Result<List<PoolEntity>> list() {
        List<PoolEntity> list = poolService.selectAll();
        return ResultUtil.getSuccessResult(list);
    }

    /**
     * 根据ID查询号池
     *
     * @param id 号池ID
     * @return Result对象，包含号池信息
     */
    @GetMapping("/{id}")
    public Result<PoolEntity> getById(@PathVariable Long id) {
        PoolEntity entity = poolService.selectById(id);
        return ResultUtil.getSuccessResult(entity);
    }

    /**
     * 新增号池
     *
     * @param poolEntity 号池实体
     * @return Result对象，包含插入行数
     */
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody PoolEntity poolEntity) {
        int count = poolService.insert(poolEntity);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * 更新号池
     *
     * @param poolEntity 号池实体
     * @return Result对象，包含更新行数
     */
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody PoolEntity poolEntity) {
        int count = poolService.update(poolEntity);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * 删除号池
     *
     * @param id 号池ID
     * @return Result对象，包含删除行数
     */
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable Long id) {
        int count = poolService.deleteById(id);
        return ResultUtil.getSuccessResult(count);
    }
}
