package com.fufu.apipool.mapper;

import com.fufu.apipool.entity.ChannelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChannelMapper {
    List<ChannelEntity> selectByPoolId(Long poolId);

    void insert(ChannelEntity channelEntity);

    void update(ChannelEntity channelEntity);

    void delete(@Param("poolId") Long poolId,@Param("channelId") Long channelId);

    ChannelEntity selectById(@Param("poolId") Long poolId,@Param("channelId") Long channelId);
}
