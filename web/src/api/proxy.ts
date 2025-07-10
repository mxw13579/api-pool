import request from './index';
import type { ProxyEntity } from '@/types';

export const getProxyList = () => {
    return request({
        url: '/proxy/list',
        method: 'get',
    }) as Promise<ProxyEntity[]>;
};

export const addProxy = (data: Omit<ProxyEntity, 'id'>) => {
    return request({
        url: '/proxy/add',
        method: 'post',
        data,
    });
};

export const updateProxy = (data: ProxyEntity) => {
    return request({
        url: '/proxy/update',
        method: 'put',
        data,
    });
};

export const deleteProxy = (id: number) => {
    return request({
        url: `/proxy/delete/${id}`,
        method: 'delete',
    });
};
