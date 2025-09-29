package com.fufu.apipool.common.util;

import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SQL安全工具类
 * 用于阻止SQL注入攻击
 */
@Slf4j
public class SqlSecurityUtil {

    // SQL关键词黑名单 (小写)
    private static final Set<String> SQL_KEYWORDS = new HashSet<>(Arrays.asList(
        "select", "insert", "update", "delete", "drop", "create", "alter", "exec", "execute",
        "union", "and", "or", "xor", "not", "like", "between", "in", "exists", "any", "all",
        "case", "when", "then", "else", "end", "if", "while", "declare", "set", "print",
        "sp_", "xp_", "fn_", "information_schema", "sys", "master", "msdb", "tempdb",
        "pg_", "mysql", "sqlite_master", "pragma", "attach", "detach", "vacuum"
    ));

    // 允许的排序字段名模式：只允许字母、数字、下划线
    private static final Pattern VALID_COLUMN_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    // 代理表允许的排序字段白名单
    public static final Set<String> PROXY_ALLOWED_COLUMNS = new HashSet<>(Arrays.asList(
        "id", "name", "proxy_url", "source", "address", "status",
        "created_at", "updated_at", "created_by", "updated_by"
    ));

    /**
     * 验证并清理orderBy参数 - 用于代理表
     * @param orderBy 排序字段
     * @return 安全的排序字段，如果不安全返回null
     */
    public static String sanitizeProxyOrderBy(String orderBy) {
        return sanitizeOrderBy(orderBy, PROXY_ALLOWED_COLUMNS, "proxy");
    }

    /**
     * 验证并清理orderBy参数
     * @param orderBy 排序字段
     * @param allowedColumns 允许字段白名单
     * @param tableName 表名（用于日志）
     * @return 安全字段，不安全返回null
     */
    public static String sanitizeOrderBy(String orderBy, Set<String> allowedColumns, String tableName) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return null;
        }

        String trimmed = orderBy.trim();
        String normalized = trimmed.toLowerCase();

        if (trimmed.length() > 50) {
            log.warn("OrderBy参数过长，表: {}, 参数: {}", tableName, trimmed.substring(0, 50) + "...");
            return null;
        }

        if (SQL_KEYWORDS.contains(normalized)) {
            log.warn("OrderBy参数命中SQL关键字'{}' 表: {}, 参数: {}", normalized, tableName, trimmed);
            return null;
        }

        if (!VALID_COLUMN_PATTERN.matcher(trimmed).matches()) {
            log.warn("OrderBy参数格式不合法，表: {}, 参数: {}", tableName, trimmed);
            return null;
        }

        if (!allowedColumns.contains(normalized)) {
            log.warn("OrderBy参数不在白名单中，表: {}, 参数: {}", tableName, trimmed);
            return null;
        }

        return normalized;
    }

    /**
     * 验证排序方向
     * @param direction 排序方向
     * @return 安全的排序方向，默认desc
     */
    public static String sanitizeOrderDirection(String direction) {
        if (direction == null || direction.trim().isEmpty()) {
            return "desc";
        }

        String cleaned = direction.trim().toLowerCase();
        if ("asc".equals(cleaned)) {
            return "asc";
        } else if ("desc".equals(cleaned)) {
            return "desc";
        } else {
            log.warn("非法的排序方向参数: {}", direction);
            return "desc";
        }
    }

    /**
     * 构建安全的排序子句
     * @param orderBy 排序字段
     * @param direction 排序方向
     * @param allowedColumns 允许字段白名单
     * @param tableName 表名
     * @return 安全的排序子句
     */
    public static String buildSafeOrderClause(String orderBy, String direction,
                                              Set<String> allowedColumns, String tableName) {
        String safeOrderBy = sanitizeOrderBy(orderBy, allowedColumns, tableName);
        String safeDirection = sanitizeOrderDirection(direction);

        if (safeOrderBy == null) {
            return "created_at desc";
        }

        return safeOrderBy + " " + safeDirection;
    }
}