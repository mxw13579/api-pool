package com.fufu.apipool.service;

import com.fufu.apipool.entity.ChannelEntity;

import java.util.List;

public interface ChannelService {
    List<ChannelEntity> getChannelsByPoolId(Long poolId);

    void addChannel(ChannelEntity channelEntity);

    void updateChannel(ChannelEntity channelEntity);

    void deleteChannel(Long poolId, Long channelId);

    ChannelEntity getChannelById(Long poolId, Long channelId);
}