package com.fufu.apipool.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.fufu.apipool.common.exception.AuthenticationException;
import com.fufu.apipool.entity.AccountEntity;
import com.fufu.apipool.mapper.AccountMapper;
import com.fufu.apipool.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账号服务实现类
 * 提供账号的增删改查操作
 * @author lizelin
 */
@Slf4j
@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;


    /**
     * 实现登录功能
     * @param username 用户名
     * @param password 密码
     * @return 登录成功后返回 Token
     */
    @Override
    public String login(String username, String password) {
        // 输入参数验证
        if (username == null || username.trim().isEmpty()) {
            throw AuthenticationException.invalidCredentials();
        }
        if (password == null || password.isEmpty()) {
            throw AuthenticationException.invalidCredentials();
        }
        
        // 1. 根据用户名查询用户
        AccountEntity account = accountMapper.selectByUsername(username);
        if (account == null) {
            throw AuthenticationException.invalidCredentials(); // 统一错误信息，不透露具体原因
        }

        // 2. 验证密码
        // 使用 passwordEncoder.matches() 方法进行比对
        // 第一个参数是明文密码，第二个参数是数据库中存储的加密密码
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw AuthenticationException.invalidCredentials(); // 统一错误信息
        }

        // 3. 检查账号状态
        if (account.getStatus() != 1) {
            throw AuthenticationException.accountDisabled();
        }

        // 4. 登录！
        // StpUtil.login() 会为这个用户生成 Token，并自动处理后续的认证
        StpUtil.login(account.getId());

        // 5. 返回 Token
        return StpUtil.getTokenValue();
    }

    /**
     * 根据ID查询账号
     * @param id 账号ID
     * @return AccountEntity 账号实体
     */
    @Override
    public AccountEntity selectById(Long id) {
        return accountMapper.selectById(id);
    }

    /**
     * 查询所有账号
     * @return List<AccountEntity> 账号列表
     */
    @Override
    public List<AccountEntity> selectAll() {
        return accountMapper.selectAll();
    }

    /**
     * 新增账号
     * @param accountEntity 账号实体
     * @return int 插入结果
     */
    @Override
    public int insert(AccountEntity accountEntity) {
        accountEntity.setPassword(passwordEncoder.encode(accountEntity.getPassword()));
        return accountMapper.insert(accountEntity);
    }

    /**
     * 更新账号
     * @param accountEntity 账号实体
     * @return int 更新结果
     */
    @Override
    public int update(AccountEntity accountEntity) {
        // 安全的密码更新逻辑
        if (accountEntity.getPassword() != null && !accountEntity.getPassword().trim().isEmpty()) {
            // 检查新密码是否与原密码相同（避免不必要的加密）
            AccountEntity existingAccount = accountMapper.selectById(accountEntity.getId());
            if (existingAccount != null && !passwordEncoder.matches(accountEntity.getPassword(), existingAccount.getPassword())) {
                accountEntity.setPassword(passwordEncoder.encode(accountEntity.getPassword()));
            } else if (existingAccount != null) {
                // 密码相同，保持不变
                accountEntity.setPassword(existingAccount.getPassword());
            }
        } else {
            // 密码为空或空字符串时，保持原密码不变
            AccountEntity existingAccount = accountMapper.selectById(accountEntity.getId());
            if (existingAccount != null) {
                accountEntity.setPassword(existingAccount.getPassword());
            }
        }
        return accountMapper.update(accountEntity);
    }

    /**
     * 根据ID删除账号
     * @param id 账号ID
     * @return int 删除结果
     */
    @Override
    public int deleteById(Long id) {
        return accountMapper.deleteById(id);
    }

    /**
     * 获取账号总数
     * @return long 账号总数
     */
    @Override
    public long count() {
        return accountMapper.count();
    }
}
