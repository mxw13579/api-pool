package com.fufu.apipool.domain.newapi;

import lombok.Data;

/**
 * Google云服务账号实体类，对应服务账号JSON密钥文件结构。
 * 用于存储和传递Google Service Account相关信息。
 * @author lizelin
 */
@Data
public class GoogleServiceAccount {

    /**
     * 账号类型，通常为"service_account"。
     */
    private String type;

    /**
     * 项目ID。
     */
    private String project_id;

    /**
     * 私钥ID。
     */
    private String private_key_id;

    /**
     * 私钥内容。
     */
    private String private_key;

    /**
     * 客户端邮箱。
     */
    private String client_email;

    /**
     * 客户端ID。
     */
    private String client_id;

    /**
     * 授权URI。
     */
    private String auth_uri;

    /**
     * Token获取URI。
     */
    private String token_uri;

    /**
     * 授权提供方的X509证书URL。
     */
    private String auth_provider_x509_cert_url;

    /**
     * 客户端X509证书URL。
     */
    private String client_x509_cert_url;

    /**
     * Google API Universe域名。
     */
    private String universe_domain;

}
