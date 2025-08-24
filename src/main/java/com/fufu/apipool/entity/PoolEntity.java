package com.fufu.apipool.entity;

import com.fufu.apipool.common.BaseEntity;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 号池 实体
 * @author lizelin
 */
@Data
public class PoolEntity extends BaseEntity {
    /**
     * 号池名称
     */
    @NotBlank(message = "号池名称不能为空")
    @Size(max = 100, message = "号池名称长度不能超过100个字符")
    private String name;
    
    /**
     * api endpoint
     */
    @NotBlank(message = "API端点不能为空")
    @Size(max = 255, message = "API端点长度不能超过255个字符")
    @Pattern(regexp = "^https?://.*", message = "API端点必须以http://或https://开头")
    private String endpoint;
    
    /**
     * api 账号
     */
    @NotBlank(message = "API账号不能为空")
    @Size(max = 50, message = "API账号长度不能超过50个字符")
    private String username;
    
    /**
     * api 密码
     */
    @NotBlank(message = "API密码不能为空")
    @Size(max = 100, message = "API密码长度不能超过100个字符")
    private String password;
    
    /**
     * 号池所在地址
     */
    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;
    
    /**
     * 监控间隔时间/分钟级别
     */
    @Min(value = 1, message = "监控间隔时间必须大于0")
    @Max(value = 1440, message = "监控间隔时间不能超过1440分钟")
    private Integer monitoringIntervalTime;
    
    /**
     * 最小激活渠道数
     */
    @Min(value = 0, message = "最小激活渠道数不能小于0")
    @Max(value = 1000, message = "最小激活渠道数不能超过1000")
    private Integer minActiveChannels;

    /**
     * 最大监控重试次数
     */
    @Min(value = 1, message = "最大监控重试次数必须大于0")
    @Max(value = 10, message = "最大监控重试次数不能超过10")
    private Integer maxMonitorRetries;

    /**
     * 延迟
     */
    @Min(value = 0, message = "延迟值不能小于0")
    private Long latency;
}
