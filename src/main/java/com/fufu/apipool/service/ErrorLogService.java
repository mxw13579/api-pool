package com.fufu.apipool.service;

import com.fufu.apipool.entity.ErrorLogEntity;

import java.util.List;

public interface ErrorLogService {
    void logError(ErrorLogEntity log);
    List<ErrorLogEntity> getErrorsByPoolId(Long poolId);
}
