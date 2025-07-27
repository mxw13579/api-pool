package com.fufu.apipool.mapper;

import com.fufu.apipool.entity.ProxyEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 代理Mapper接口
 * @author lizelin
 */
@Mapper
public interface ProxyMapper {
    ProxyEntity selectById(Long id);

    List<ProxyEntity> selectAll();

    int insert(ProxyEntity proxyEntity);

    long getLastInsertId();

    int update(ProxyEntity proxyEntity);

    int deleteById(Long id);

    int deleteByIds(List<Long> ids);
}
