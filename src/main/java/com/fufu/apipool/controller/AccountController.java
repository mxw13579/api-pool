package com.fufu.apipool.controller;

import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.entity.AccountEntity;
import com.fufu.apipool.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账号控制器
 * 提供账号相关的REST接口
 * @author lizelin
 */
@Slf4j
@RestController
@RequestMapping("/api/account")
@AllArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 查询所有账号
     *
     * @return Result对象，包含账号列表
     */
    @GetMapping("/list")
    public Result<List<AccountEntity>> list() {
        List<AccountEntity> list = accountService.selectAll();
        return ResultUtil.getSuccessResult(list);
    }

    /**
     * 根据ID查询账号
     *
     * @param id 账号ID
     * @return Result对象，包含账号信息
     */
    @GetMapping("/{id}")
    public Result<AccountEntity> getById(@PathVariable Long id) {
        AccountEntity entity = accountService.selectById(id);
        return ResultUtil.getSuccessResult(entity);
    }

    /**
     * 新增账号
     *
     * @param accountEntity 账号实体
     * @return Result对象，包含插入行数
     */
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody AccountEntity accountEntity) {
        int count = accountService.insert(accountEntity);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * 更新账号
     *
     * @param accountEntity 账号实体
     * @return Result对象，包含更新行数
     */
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody AccountEntity accountEntity) {
        int count = accountService.update(accountEntity);
        return ResultUtil.getSuccessResult(count);
    }

    /**
     * 删除账号
     *
     * @param id 账号ID
     * @return Result对象，包含删除行数
     */
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable Long id) {
        int count = accountService.deleteById(id);
        return ResultUtil.getSuccessResult(count);
    }
}
