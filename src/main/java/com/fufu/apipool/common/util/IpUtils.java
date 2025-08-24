package com.fufu.apipool.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * IP地址工具类
 * @author lizelin
 */
public class IpUtils {
    
    private static final String[] IP_HEADERS = {
        "X-Forwarded-For",
        "X-Real-IP", 
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    };
    
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    
    /**
     * 获取客户端真实IP地址
     * @param request HttpServletRequest对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        // 依次检查各种可能包含真实IP的请求头
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // X-Forwarded-For可能包含多个IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        // 最后使用request.getRemoteAddr()
        String ip = request.getRemoteAddr();
        return StringUtils.hasText(ip) ? ip : UNKNOWN;
    }
    
    /**
     * 检查IP是否有效
     * @param ip IP地址
     * @return true表示有效，false表示无效
     */
    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && 
               !UNKNOWN.equalsIgnoreCase(ip) &&
               !isLocalhost(ip);
    }
    
    /**
     * 检查是否为本地地址
     * @param ip IP地址
     * @return true表示是本地地址
     */
    private static boolean isLocalhost(String ip) {
        return LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip);
    }
    
    /**
     * 检查IP地址格式是否有效
     * @param ip IP地址
     * @return true表示格式有效
     */
    public static boolean isValidIpFormat(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        
        // 简单的IPv4格式检查
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}