import request from './index';
import type {PoolEntity, Channel} from '@/types';

/** 批量为所有号池新增一个渠道 */
export const batchAddChannelToAll = (data: Partial<Channel>): Promise<string[]> => {
    return request.post('/pool/batchAddChannelToAll', data).then(res => res.data);
};

/** 获取号池列表 */
export const getPoolList = (): Promise<PoolEntity[]> => {
    return request.get('/pool/list').then(res => res.data);
};

/** 新增号池 */
export const addPool = (data: Omit<PoolEntity, 'id'>): Promise<any> => {
    return request.post('/pool/add', data);
};

/** 更新号池 */
export const updatePool = (data: Partial<PoolEntity>): Promise<any> => {
    return request.put('/pool/update', data);
};

/** 删除号池 */
export const deletePool = (id: number): Promise<any> => {
    return request.delete(`/pool/delete/${id}`);
};

/** 根据号池ID获取渠道列表 */
export const getChannelsByPoolId = (poolId: number): Promise<Channel[]> => {
    return request.get('/pool/getChannelsByPoolId', {params: {poolId}}).then(res => res.data);
};

/** 测试渠道 */
export const testChannelByPoolId = (poolId: number, channelId: number): Promise<any> => {
    return request.put(`/pool/testChannelByPoolId/${poolId}`, null, {params: {channelId}});
};

/** 更新渠道 */
export const updateChannelByPoolId = (poolId: number, channel: Partial<Channel>): Promise<any> => {
    return request.put(`/pool/updateChannelByPoolId/${poolId}`, channel);
};

/** 删除渠道 */
export const deleteChannelByPoolId = (poolId: number, channelId: number): Promise<any> => {
    return request.delete(`/pool/deleteChannelByPoolId/${poolId}`, {params: {channelId}});
};

/** 根据ID查询渠道详情 */
export const getChannelDetail = (poolId: number, channelId: number): Promise<Channel> => {
    return request.get(`/pool/getChannelByPoolId/${poolId}`, {params: {channelId}}).then(res => res.data);
};

/** 根据号池ID添加新渠道 */
export const addChannelByPoolId = (poolId: number, channel: Omit<Channel, 'id'>): Promise<any> => {
    return request.post(`/pool/addChannelsByPoolId/${poolId}`, channel);
};

/** 测试延迟 */
export const testPoolLatency = (poolId: number): Promise<number> => {
    return request.get(`/pool/test-latency/${poolId}`).then(res => res.data);
};

/** 获取统计信息 */
export const getPoolStatistics = (poolId: number): Promise<any> => {
    return request.get(`/pool/statistics/${poolId}`).then(res => res.data);
};

/** 获取错误信息 */
export const getErrorLogs = (poolId: number): Promise<any> => {
    return request.get(`/pool/error-logs/${poolId}`).then(res => res.data);
};

