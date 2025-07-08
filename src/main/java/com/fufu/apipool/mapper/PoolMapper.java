package com.fufu.apipool.mapper;

import com.fufu.apipool.entity.PoolEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 号池Mapper接口
 * @author lizelin
 */
@Mapper
public interface PoolMapper {
    PoolEntity selectById(Long id);

    List<PoolEntity> selectAll();

    int insert(PoolEntity poolEntity);

    int update(PoolEntity poolEntity);

    int deleteById(Long id);
}
