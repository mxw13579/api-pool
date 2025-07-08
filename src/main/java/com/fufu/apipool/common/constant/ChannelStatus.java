package com.fufu.apipool.common.constant;

import lombok.Getter;

/**
 * 渠道状态枚举
 *
 * @author lizelin
 */
@Getter
public enum ChannelStatus {
    /**
     * 未知状态（默认值，不建议使用）
     */
    UNKNOWN(0, "unknown", "未知状态（默认值，不建议使用）"),
    /**
     * 启用
     */
    ENABLED(1, "enabled", "启用"),
    /**
     * 手动禁用
     */
    MANUALLY_DISABLED(2, "manually_disabled", "手动禁用"),
    /**
     * 自动禁用
     */
    AUTO_DISABLED(3, "auto_disabled", "自动禁用");
    /**
     * 状态编码
     */
    private final int code;
    /**
     * 英文标识
     */
    private final String identifier;
    /**
     * 中文说明
     */
    private final String description;

    ChannelStatus(int code, String identifier, String description) {
        this.code = code;
        this.identifier = identifier;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static ChannelStatus fromCode(int code) {
        for (ChannelStatus status : ChannelStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
