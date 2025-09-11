// 基础实体
interface BaseEntity {
    id: number;
    createdAt?: number;
    updatedAt?: number;
    deletedAt?: number;
    createdBy?: string;
    updatedBy?: string;
    deletedBy?: string;
}

export interface ApiResponse<T = any> {
    code: number;
    msg: string;
    data: T;
}

// 账号实体
export interface AccountEntity extends BaseEntity {
    name: string;
    username: string;
    password?: string; // 密码通常是可选的，尤其是在获取列表时
    email: string;
    phone: string;
    status: number; // 0-禁用, 1-启用
    role: string;
    remark: string;
    lastLoginAt?: number;
    lastLoginIp?: string;
}

// 号池实体
export interface PoolEntity extends BaseEntity {
    name: string;
    endpoint: string;
    username: string;
    password?: string;
    address: string;
    monitoringIntervalTime: number;
    minActiveChannels: number;
    maxMonitorRetries: number;
    latency?: number;
}

// 代理实体
export interface ProxyEntity extends BaseEntity {
    name: string;
    proxyUrl: string;
    source: string;
    address: string;
    status: number;
    bindCount?: number;

}

export interface Channel {
    id: number;
    type: number;
    key: string;
    status: number;
    name: string;
    baseUrl: string;
    modelMapping: string;
    autoBan: number;
    weight: number;
    created_time: number;
    test_time: number;
    response_time: number;
    balance: number;
    balance_updated_time: number;
    models: string;
    groups: string[];
    used_quota: number;
    priority: number;
    other: string;
    otherInfo: string;
    tag: string;
    setting: string;
    proxy: number;
    paramOverride: string;
    openaiOrganization: string;
    testModel: string;
    statusCodeMapping: string;
}

export interface PageResult<T> {
    items: T[];
    total: number;
    pageNum: number;
    pageSize: number;
}
