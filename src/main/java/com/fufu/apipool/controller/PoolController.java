package com.fufu.apipool.controller;

import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.domain.newapi.Channel;
import com.fufu.apipool.domain.newapi.ChannelDTO;
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

    /**
     * 根据号池ID查询渠道列表
     * @param poolId 号池ID
     * @return Result对象，包含渠道列表
     */
    @GetMapping("/getChannelsByPoolId")
    public Result<List<Channel>> getChannelsByPoolId(@RequestParam Long poolId) {
        return ResultUtil.getSuccessResult( poolService.getChannelsByPoolId(poolId));
    }


    /**
     * 批量为所有号池新增渠道
     *
     * @param dto 渠道信息
     * @return Result对象，包含操作结果
     */
    @PostMapping("/batchAddChannelToAll")
    public Result<Boolean> batchAddChannelToAll(@RequestBody ChannelDTO dto) {
        return ResultUtil.getSuccessResult(poolService.batchAddChannelToAll(dto));
    }

    /**
     * 根据号池ID和渠道ID查询渠道信息
     * @param poolId 号池ID
     * @param channelId 渠道ID
     * @return Result对象，包含渠道信息
     */
    @GetMapping("/getChannelByPoolId/{poolId}")
    public Result<Channel> getChannelByPoolId(@PathVariable("poolId") Long poolId,@RequestParam Long channelId) {
        return ResultUtil.getSuccessResult( poolService.getChannelByPoolId(poolId,channelId));
    }

    /**
     * 根据号池ID添加渠道
     * @param poolId 号池ID
     * @param dto 渠道信息
     * @return Result对象，包含添加结果
     */
    @PostMapping("/addChannelsByPoolId/{poolId}")
    public Result<Boolean> addChannelByPoolId(@PathVariable("poolId") Long poolId,@RequestBody ChannelDTO dto) {
        return ResultUtil.getSuccessResult(poolService.addChannelByPoolId(poolId,dto));
    }

    /**
     * 根据号池ID和渠道ID更新渠道信息
     * @param poolId 号池ID
     * @param channel 渠道信息
     * @return Result对象，包含更新结果
     */
    @PutMapping("/updateChannelByPoolId/{poolId}")
    public Result<Boolean> updateChannelByPoolId(@PathVariable("poolId") Long poolId,@RequestBody Channel channel) {
        return ResultUtil.getSuccessResult(poolService.updateChannelByPoolId(poolId,channel));
    }

    /**
     * 根据号池ID和渠道ID删除渠道信息
     * @param poolId 号池ID
     * @param channelId 渠道信息
     * @return Result对象，包含更新结果
     */
    @DeleteMapping("/deleteChannelByPoolId/{poolId}")
    public Result<Boolean> deleteChannelByPoolId(@PathVariable("poolId") Long poolId,@RequestParam Long channelId) {
        return ResultUtil.getSuccessResult(poolService.deleteChannelByPoolId(poolId,channelId));
    }

    /**
     * 测试渠道
     * @param poolId 号池ID
     * @param channelId 渠道ID
     * @return Result对象，包含测试结果
     */
    @PutMapping("/testChannelByPoolId/{poolId}")
    public Result<Long> updateChannelByPoolId(@PathVariable("poolId") Long poolId,@RequestParam Long channelId) {
        return ResultUtil.getSuccessResult(poolService.testChannelByPoolId(poolId,channelId));
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
