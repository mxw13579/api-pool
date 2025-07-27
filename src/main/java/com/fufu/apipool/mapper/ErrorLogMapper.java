package com.fufu.apipool.mapper;

import com.fufu.apipool.entity.ErrorLogEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErrorLogMapper {
    void insert(ErrorLogEntity log);
    List<ErrorLogEntity> findByPoolId(Long poolId);
}
