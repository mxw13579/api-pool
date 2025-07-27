package com.fufu.apipool.monitor;

import lombok.Data;

/**
 * @author lizelin
 * @date 2025-07-13 18:54
 */
@Data
public class AuthResponse {
    private String token;
    private UserRecord record;
}
