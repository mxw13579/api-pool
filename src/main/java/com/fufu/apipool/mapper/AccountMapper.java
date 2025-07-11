package com.fufu.apipool.mapper;

import com.fufu.apipool.entity.AccountEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 账号Mapper接口
 * @author lizelin
 */
@Mapper
public interface AccountMapper {
    /**
     * 根据ID查询账号
     * @param id  id
     * @return 账号
     */
    AccountEntity selectById(Long id);
    /**
     * 查询所有账号
     * @return 账号列表
     */
    List<AccountEntity> selectAll();
    /**
     * 新增账号
     * @param accountEntity 账号
     * @return 影响行数
     */
    int insert(AccountEntity accountEntity);
    /**
     * 更新账号
     * @param accountEntity 账号
     * @return 影响行数
     */
    int update(AccountEntity accountEntity);
    /**
     * 根据ID删除账号
     * @param id id
     * @return 删除的行数
     */
    int deleteById(Long id);

    /**
     * 根据用户名查询账号
     * @param username 用户名
     * @return 账号
     */
    AccountEntity selectByUsername(String username);

    /**
     * 查询账号总数
     * @return 账号总数
     */
    long count();


}
