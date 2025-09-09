import request from './index';
import type { AccountEntity } from '@/types';

export const getAccountList = (): Promise<AccountEntity[]> => {
    return request.get('/account/list').then(res => res.data);
};

export const addAccount = (data: Omit<AccountEntity, 'id'>): Promise<any> => {
    return request.post('/account/add', data);
};

export const updateAccount = (data: AccountEntity): Promise<any> => {
    return request.put('/account/update', data);
};

export const deleteAccount = (id: number): Promise<any> => {
    return request.delete(`/account/delete/${id}`);
};
