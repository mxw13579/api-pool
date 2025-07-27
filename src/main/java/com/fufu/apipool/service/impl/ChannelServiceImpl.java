package com.fufu.apipool.service.impl;

import com.fufu.apipool.entity.ChannelEntity;
import com.fufu.apipool.mapper.ChannelMapper;
import com.fufu.apipool.service.ChannelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelMapper channelMapper;

    @Override
    public List<ChannelEntity> getChannelsByPoolId(Long poolId) {
        return channelMapper.selectByPoolId(poolId);
    }

    @Override
    public void addChannel(ChannelEntity channelEntity) {
        channelEntity.setCreatedAt(System.currentTimeMillis() / 1000);
        // 检查是否存在
        if (channelMapper.selectById(channelEntity.getPoolId(), channelEntity.getId()) != null) {
            channelMapper.update(channelEntity);
        } else {
            channelMapper.insert(channelEntity);
        }
    }

    @Override
    public void updateChannel(ChannelEntity channelEntity) {
        channelMapper.update(channelEntity);
    }

    @Override
    public void deleteChannel(Long poolId, Long channelId) {
        channelMapper.delete(poolId, channelId);
    }

    @Override
    public ChannelEntity getChannelById(Long poolId, Long channelId) {
        return channelMapper.selectById(poolId, channelId);
    }
}
