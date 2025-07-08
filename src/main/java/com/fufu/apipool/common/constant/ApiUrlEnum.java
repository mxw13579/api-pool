package com.fufu.apipool.common.constant;

import lombok.Getter;

/**
 * 渠道接口枚举
 *
 * @author lizelin
 */
@Getter
public enum ApiUrlEnum {

    LIST("list", "GET", "/api/channel/", "查询渠道列表"),
    DETAIL("detail", "GET", "/api/channel/{id}", "查询渠道详情"),
    EDIT("edit", "PUT", "/api/channel/", "编辑渠道"),
    DELETE("delete", "DELETE", "/api/channel/{id}", "删除渠道"),
    ADD("add", "POST", "/api/channel/", "新增渠道");

    private final String code;
    private final String method;
    private final String url;
    private final String remark;

    ApiUrlEnum(String code, String method, String url, String remark) {
        this.code = code;
        this.method = method;
        this.url = url;
        this.remark = remark;
    }
}
