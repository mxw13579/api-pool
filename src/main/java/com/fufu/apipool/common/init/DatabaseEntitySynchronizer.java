package com.fufu.apipool.common.init;

import com.fufu.apipool.common.BaseEntity;
import com.fufu.apipool.entity.AccountEntity;
import com.fufu.apipool.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author lizelin
 */
@Slf4j
@Component
public class DatabaseEntitySynchronizer implements ApplicationRunner {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DataSource dataSource;
    @Value("${spring.datasource.url:}")
    private String dataSourceUrl;
    @Autowired
    private AccountService accountService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始执行数据库实体同步...");
        try {
            // 1. 在获取连接前，确保 SQLite 数据库文件目录存在
            if (dataSourceUrl != null && dataSourceUrl.startsWith("jdbc:sqlite:")) {
                String dbPath = dataSourceUrl.substring("jdbc:sqlite:".length());
                java.io.File dbFile = new java.io.File(dbPath);
                java.io.File parentDir = dbFile.getAbsoluteFile().getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (!created) {
                        log.warn("Failed to create database directory: {}", parentDir.getAbsolutePath());
                    }
                }
            }
            // 获取所有BaseEntity子类
            List<Class<?>> entityClasses = new ArrayList<>();
            String basePackage = "com.fufu.apipool.entity";
            for (Class<?> clazz : getClasses(basePackage)) {
                if (BaseEntity.class.isAssignableFrom(clazz) && !clazz.equals(BaseEntity.class)) {
                    entityClasses.add(clazz);
                }
            }
            try (Connection conn = dataSource.getConnection()) {
                for (Class<?> entityClass : entityClasses) {
                    syncTable(conn, entityClass);
                }
            }
            initDefaultAdminAccount();
        } catch (Exception e) {
            log.error("数据库实体同步失败", e);
        }
    }


    /**
     * 初始化默认管理员账号
     */
    private void initDefaultAdminAccount() {
        try {
            // 1. 检查数据库中是否已有用户
            if (accountService.count() == 0) {
                log.info("数据库中无用户，开始初始化默认管理员...");

                // 2. 创建一个默认的管理员实体
                AccountEntity admin = new AccountEntity();
                admin.setName("Administrator");
                admin.setUsername("admin");
                admin.setPassword("admin");
                admin.setRole("admin");
                admin.setStatus(1);
                admin.setEmail("admin@example.com");

                // 3. 调用 service 层的方法插入用户
                accountService.insert(admin);
                log.info("默认管理员(admin/admin)创建成功！");
            } else {
                log.info("数据库中已存在用户，跳过初始化。");
            }
        } catch (Exception e) {
            log.error("初始化默认管理员账号失败！", e);
        }
    }

    /**
     * 同步数据库表结构
     * @param conn 数据库连接
     * @param entityClass 实体类
     * @throws SQLException SQL异常
     */
    private void syncTable(Connection conn, Class<?> entityClass) throws SQLException {
        String tableName = camelToUnderscore(entityClass.getSimpleName().replace("Entity", ""));
        Map<String, String> fields = new LinkedHashMap<>();
        for (Field field : entityClass.getDeclaredFields()) {
            fields.put(camelToUnderscore(field.getName()), getSqlType(field.getType()));
        }
        for (Field field : BaseEntity.class.getDeclaredFields()) {
            fields.put(camelToUnderscore(field.getName()), getSqlType(field.getType()));
        }
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(null, null, tableName, null);
        if (!tables.next()) {
            // 表不存在，创建表
            StringBuilder sb = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
            sb.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
            fields.remove("id");
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                sb.append(entry.getKey()).append(" ").append(entry.getValue()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1).append(");");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sb.toString());
            }
        } else {
            // 表存在，检查字段
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            Set<String> existingColumns = new HashSet<>();
            while (columns.next()) {
                existingColumns.add(columns.getString("COLUMN_NAME"));
            }
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                if (!existingColumns.contains(entry.getKey())) {
                    String alter = "ALTER TABLE " + tableName + " ADD COLUMN " + entry.getKey() + " " + entry.getValue();
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(alter);
                    }
                }
            }
        }
    }
    private String getSqlType(Class<?> type) {
        if (type == Long.class || type == long.class) {
            return "INTEGER";
        }
        if (type == Integer.class || type == int.class) {
            return "INTEGER";
        }
        if (type == String.class) {
            return "TEXT";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "BOOLEAN";
        }
        if (type == Double.class || type == double.class) {
            return "REAL";
        }
        // 可扩展更多类型
        return "TEXT";
    }
    private String camelToUnderscore(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    private List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        // 只扫描类，不考虑接口和抽象类
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(BaseEntity.class));
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        scanner.setResourceLoader(resourceLoader);
        Set<org.springframework.beans.factory.config.BeanDefinition> candidateComponents =
                scanner.findCandidateComponents(packageName);
        for (org.springframework.beans.factory.config.BeanDefinition bd : candidateComponents) {
            Class<?> clazz = ClassUtils.forName(Objects.requireNonNull(bd.getBeanClassName()), this.getClass().getClassLoader());
            if (!clazz.equals(BaseEntity.class)) {
                classes.add(clazz);
            }
        }
        return classes;
    }
}
