import request from './index';
import type { PoolEntity, Channel } from '@/types';

// 获取号池列表
export const getPoolList = (): Promise<PoolEntity[]> => {
    return request({
        url: '/pool/list',
        method: 'get',
    });
};

// 新增号池
export const addPool = (data: Omit<PoolEntity, 'id'>): Promise<any> => {
    return request({
        url: '/pool/add',
        method: 'post',
        data,
    });
};

// 更新号池
export const updatePool = (data: PoolEntity): Promise<any> => {
    return request({
        url: '/pool/update',
        method: 'put',
        data,
    });
};

// 删除号池
export const deletePool = (id: number): Promise<any> => {
    return request({
        url: `/pool/delete/${id}`,
        method: 'delete',
    });
};

// 获取渠道列表
export const getChannelsByPoolId = (poolId: number): Promise<Channel[]> => {
    return request({
        url: '/pool/getChannelsByPoolId',
        method: 'get',
        params: { poolId },
    });
};

// 测试渠道
export const testChannelByPoolId = (poolId: number, channelId: number): Promise<any> => {
    return request.put(`/pool/testChannelByPoolId/${poolId}`, null, {
        params: { channelId }
    });
};

// 更新渠道（用于启用/禁用/编辑）
export const updateChannelByPoolId = (poolId: number, channel: Channel): Promise<any> => {
    return request.put(`/pool/updateChannelByPoolId/${poolId}`, channel);
};

// 删除渠道
export const deleteChannelByPoolId = (poolId: number, channelId: number): Promise<any> => {
    return request.delete(`/pool/deleteChannelByPoolId/${poolId}`, {
        params: { channelId }
    });
};

/**
 * 根据号池ID和渠道ID查询渠道详细信息
 * @param poolId 号池ID
 * @param channelId 渠道ID
 * @returns 渠道详情
 */
export function getChannelDetail(poolId: number, channelId: number): Promise<Channel> {
    return request({
        url: `/pool/getChannelByPoolId/${poolId}`,
        method: 'get',
        params: { channelId }
    });
}

/**
 * 根据号池ID添加新渠道
 * @param poolId 号池ID
 * @param channel 渠道数据
 * @returns Promise
 */
export function addChannelByPoolId(poolId: number, channel: Channel): Promise<any> {
    return request({
        url: `/pool/addChannelsByPoolId/${poolId}`,
        method: 'post',
        data: channel
    });
}
