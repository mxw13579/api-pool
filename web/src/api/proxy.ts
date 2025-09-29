import request from './index';
import type { ProxyEntity } from '@/types';

/** 分页查询参数 */
export interface PageRequest {
  pageNum?: number;
  pageSize?: number;
  orderBy?: string;
  orderDirection?: 'asc' | 'desc';
}

/** 分页结果 */
export interface PageResult<T> {
  items: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  totalPages: number;
}

/** 分页获取代理列表 */
export const getProxyList = (params?: PageRequest): Promise<PageResult<ProxyEntity>> => {
    return request.get('/proxy/list', { params }).then(res => res.data);
};

/** 获取所有代理列表（不分页） */
export const getAllProxyList = (): Promise<ProxyEntity[]> => {
    return request.get('/proxy/list/all').then(res => res.data);
};

/** 新增代理 */
export const addProxy = (data: Omit<ProxyEntity, 'id'>): Promise<any> => {
    return request.post('/proxy/add', data);
};

/** 更新代理 */
export const updateProxy = (data: Partial<ProxyEntity>): Promise<any> => {
    return request.put('/proxy/update', data);
};

/** 批量新增代理 */
export const addProxyBatches = (data: any): Promise<number> => {
    return request.post('/proxy/add-batches', data).then(res => res.data);
};

/** 删除代理 */
export const deleteProxy = (id: number): Promise<any> => {
    return request.delete(`/proxy/delete/${id}`);
};

/** 批量删除代理 */
export const deleteProxyBatches = (ids: number[]): Promise<any> => {
    return request.delete('/proxy/delete-batches', { data: ids });
};
