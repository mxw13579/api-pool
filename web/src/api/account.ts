import request from './index';
import type { AccountEntity } from '@/types';

export const getAccountList = () => {
    return request({
        url: '/account/list',
        method: 'get',
    }) as Promise<AccountEntity[]>;
};

export const addAccount = (data: Omit<AccountEntity, 'id'>) => {
    return request({
        url: '/account/add',
        method: 'post',
        data,
    });
};

export const updateAccount = (data: AccountEntity) => {
    return request({
        url: '/account/update',
        method: 'put',
        data,
    });
};

export const deleteAccount = (id: number) => {
    return request({
        url: `/account/delete/${id}`,
        method: 'delete',
    });
};
