package com.fufu.apipool.service.impl;

import cn.dev33.satoken.stp.StpUtil;
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
        // 1. 根据用户名查询用户
        AccountEntity account = accountMapper.selectByUsername(username);
        if (account == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 验证密码
        // 使用 passwordEncoder.matches() 方法进行比对
        // 第一个参数是明文密码，第二个参数是数据库中存储的加密密码
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 检查账号状态
        if (account.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
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
        if (accountEntity.getPassword() != null && !accountEntity.getPassword().isEmpty()) {
            accountEntity.setPassword(passwordEncoder.encode(accountEntity.getPassword()));
        } else {
            accountEntity.setPassword(null);
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
