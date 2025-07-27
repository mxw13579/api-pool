package com.fufu.apipool.monitor;

import lombok.Data;

/**
 * @author lizelin
 * @date 2025-07-13 18:54
 */
@Data
public class UserRecord {
    private String collectionId;
    private String collectionName;
    private String id;
    private String email;
    private boolean emailVisibility;
    private boolean verified;
    private String username;
    private String role;
    private String created;
    private String updated;
    private String avatar;
    private String name;
}
