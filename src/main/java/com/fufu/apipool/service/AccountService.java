package com.fufu.apipool.service;

import com.fufu.apipool.entity.AccountEntity;

import java.util.List;

/**
 * 账号服务接口
 * @author lizelin
 */
public interface AccountService {
    /**
     * 实现登录功能
     * @param username 用户名
     * @param password 密码
     * @return 登录成功后返回 Token
     */
    String login(String username, String password);

    /**
     * 根据ID查询账号
     * @param id 账号ID
     * @return 账号实体
     */
    AccountEntity selectById(Long id);

    /**
     * 查询所有账号
     * @return 账号列表
     */
    List<AccountEntity> selectAll();

    /**
     * 新增账号
     * @param accountEntity 账号实体
     * @return 插入行数
     */
    int insert(AccountEntity accountEntity);

    /**
     * 更新账号
     * @param accountEntity 账号实体
     * @return 更新行数
     */
    int update(AccountEntity accountEntity);

    /**
     * 根据ID删除账号
     * @param id 账号ID
     * @return 删除行数
     */
    int deleteById(Long id);

    /**
     * 查询账号总数
     * @return 账号总数
     */
    long count();
}
