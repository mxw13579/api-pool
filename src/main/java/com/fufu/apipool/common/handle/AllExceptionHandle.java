package com.fufu.apipool.common.handle;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.common.constant.CommonRespCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author lizelin
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class AllExceptionHandle {
    /**
     * 登录权限校验
     */
    @ExceptionHandler({NotLoginException.class, NotRoleException.class})
    public Result unauthorized(Exception e) {
        log.error("登录权限校验失败", e);
        return ResultUtil.getFailResult("", CommonRespCode.UNAUTHORIZED);
    }


    /**
     * 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result exception(Exception e) {
        log.error("全局异常处理", e);
        return ResultUtil.getFailResult("", CommonRespCode.SYSTEM_ERROR);
    }
}
