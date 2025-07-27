package com.fufu.apipool.service.impl;

import com.fufu.apipool.entity.ErrorLogEntity;
import com.fufu.apipool.mapper.ErrorLogMapper;
import com.fufu.apipool.service.ErrorLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ErrorLogServiceImpl implements ErrorLogService {

    private final ErrorLogMapper errorLogMapper;

    @Override
    public void logError(ErrorLogEntity log) {
        log.setCreatedAt(System.currentTimeMillis() / 1000);
        errorLogMapper.insert(log);
    }

    @Override
    public List<ErrorLogEntity> getErrorsByPoolId(Long poolId) {
        List<ErrorLogEntity> byPoolId = errorLogMapper.findByPoolId(poolId);
        log.info("s:{}",byPoolId);
        return byPoolId;
    }
}
