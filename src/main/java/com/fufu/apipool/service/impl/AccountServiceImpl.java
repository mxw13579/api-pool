package com.fufu.apipool.service.impl;

import com.fufu.apipool.entity.AccountEntity;
import com.fufu.apipool.mapper.AccountMapper;
import com.fufu.apipool.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return accountMapper.insert(accountEntity);
    }

    /**
     * 更新账号
     * @param accountEntity 账号实体
     * @return int 更新结果
     */
    @Override
    public int update(AccountEntity accountEntity) {
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
}
