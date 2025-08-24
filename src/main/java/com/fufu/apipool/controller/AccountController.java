package com.fufu.apipool.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.common.component.RateLimiter;
import com.fufu.apipool.common.util.IpUtils;
import com.fufu.apipool.domain.dto.LoginRequest;
import com.fufu.apipool.entity.AccountEntity;
import com.fufu.apipool.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private final RateLimiter rateLimiter;

    /**
     * 登录接口
     * @param loginRequest 包含用户名和密码
     * @param request HTTP请求对象，用于获取客户端IP
     * @return Result对象，包含JWT token
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest, 
                                            HttpServletRequest request) {
        try {
            // 获取客户端IP地址
            String clientIp = IpUtils.getClientIp(request);
            
            // 检查IP请求频率限制
            if (!rateLimiter.isLoginAllowed(clientIp)) {
                log.warn("登录请求频率限制: IP={}, username={}", clientIp, loginRequest.getUsername());
                return ResultUtil.getFailResult("", "RATE_LIMIT", "请求过于频繁，请稍后重试");
            }
            
            // 检查用户名请求频率限制 (防止针对特定用户的暴力破解)
            if (!rateLimiter.isLoginAllowed(loginRequest.getUsername())) {
                log.warn("登录请求频率限制: username={}, IP={}", loginRequest.getUsername(), clientIp);
                return ResultUtil.getFailResult("", "RATE_LIMIT", "该账户登录请求过于频繁，请稍后重试");
            }
            
            String token = accountService.login(loginRequest.getUsername(), loginRequest.getPassword());
            // Sa-Token 默认的 token 名称是 "satoken"，也可以自定义
            return ResultUtil.getSuccessResult(Collections.singletonMap("token", token));
        } catch (Exception e) {
            log.error("登陆失败",e);
            return ResultUtil.getFailResult("登陆失败","LoginError",e.getMessage());
        }
    }

    /**
     * 退出登录
     * @return Result对象，包含退出结果
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        StpUtil.logout();
        return ResultUtil.getSuccessResult("退出成功");
    }

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
    public Result<Integer> add(@Valid @RequestBody AccountEntity accountEntity) {
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
    public Result<Integer> update(@Valid @RequestBody AccountEntity accountEntity) {
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
