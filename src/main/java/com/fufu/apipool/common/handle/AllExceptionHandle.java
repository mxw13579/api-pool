package com.fufu.apipool.common.handle;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.fufu.apipool.common.Result;
import com.fufu.apipool.common.ResultUtil;
import com.fufu.apipool.common.constant.CommonRespCode;
import com.fufu.apipool.common.exception.BusinessException;
import com.fufu.apipool.common.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
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
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result businessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResultUtil.getFailResult("", e.getCode(), e.getMessage());
    }
    
    /**
     * 认证异常处理
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result authenticationException(AuthenticationException e) {
        log.warn("认证异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResultUtil.getFailResult("", e.getCode(), e.getMessage());
    }
    
    /**
     * 数据验证异常处理 - @RequestBody
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result validationException(MethodArgumentNotValidException e) {
        log.warn("数据验证失败", e);
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResultUtil.getFailResult("", "VALIDATION_ERROR", errorMessage);
    }
    
    /**
     * 数据验证异常处理 - @ModelAttribute
     */
    @ExceptionHandler(BindException.class)
    public Result bindException(BindException e) {
        log.warn("数据绑定验证失败", e);
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResultUtil.getFailResult("", "VALIDATION_ERROR", errorMessage);
    }
    
    /**
     * 约束验证异常处理 - @Validated
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result constraintViolationException(ConstraintViolationException e) {
        log.warn("约束验证失败", e);
        String errorMessage = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return ResultUtil.getFailResult("", "VALIDATION_ERROR", errorMessage);
    }

    /**
     * 运行时异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public Result runtimeException(RuntimeException e) {
        log.error("运行时异常处理", e);
        // 生产环境不返回详细异常信息，只记录到日志
        String message = isProductionEnvironment() ? "系统繁忙，请稍后重试" : e.getMessage();
        return ResultUtil.getFailResult("", "500", message);
    }

    /**
     * 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result exception(Exception e) {
        log.error("全局异常处理", e);
        // 生产环境不返回详细异常信息，只记录到日志
        String message = isProductionEnvironment() ? "系统内部错误" : e.getMessage();
        return ResultUtil.getFailResult("", CommonRespCode.SYSTEM_ERROR, message);
    }
    
    /**
     * 判断是否为生产环境
     * 可以通过环境变量或配置文件来判断
     */
    private boolean isProductionEnvironment() {
        String profile = System.getProperty("spring.profiles.active");
        return "prod".equals(profile) || "production".equals(profile);
    }
}
