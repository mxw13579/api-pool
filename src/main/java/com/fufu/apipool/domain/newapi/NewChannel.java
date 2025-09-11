package com.fufu.apipool.domain.newapi;

import lombok.Data;

/**
 * new-api 渠道
 *
 * @author lizelin
 */
@Data
public class NewChannel {
    String mode = "single";
    Channel channel;
}
