package com.fufu.apipool.domain.newapi;

import lombok.Data;

/**
 * @author lizelin
 * @date 2025-07-10 23:04
 */
@Data
public class ChannelDTO extends Channel{
    /**
     * 0: "随机"
     * 1: "轮询"
     */
    private Integer proxy;

}
