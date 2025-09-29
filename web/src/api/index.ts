import axios, { type AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

const service = axios.create({
    baseURL: '/api', // Vite 代理会处理这个
    timeout: 10000,
    withCredentials: true, // 允许跨域请求携带 cookie
});

// 添加请求拦截器，自动添加token
service.interceptors.request.use(
    config => {
        const token = getToken();
        if (token) {
            config.headers['satoken'] = token; // sa-token默认的token头名称
        }
        return config;
    },
    error => {
        console.error('Request Error:', error);
        return Promise.reject(error);
    }
);

// Token管理工具函数
const TOKEN_KEY = 'auth_token';
const TOKEN_EXPIRY_KEY = 'auth_token_expiry';

// 获取Token
function getToken(): string | null {
    try {
        const token = sessionStorage.getItem(TOKEN_KEY);
        const expiry = sessionStorage.getItem(TOKEN_EXPIRY_KEY);
        
        if (!token || !expiry) {
            return null;
        }
        
        // 检查token是否过期
        if (Date.now() > parseInt(expiry)) {
            removeToken();
            return null;
        }
        
        return token;
    } catch (error) {
        console.error('Error getting token:', error);
        return null;
    }
}

// 设置Token (默认24小时过期)
export function setToken(token: string, expiryHours: number = 24): void {
    try {
        const expiry = Date.now() + (expiryHours * 60 * 60 * 1000);
        sessionStorage.setItem(TOKEN_KEY, token);
        sessionStorage.setItem(TOKEN_EXPIRY_KEY, expiry.toString());
    } catch (error) {
        console.error('Error setting token:', error);
    }
}

// 移除Token
export function removeToken(): void {
    try {
        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(TOKEN_EXPIRY_KEY);
    } catch (error) {
        console.error('Error removing token:', error);
    }
}

service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data;

        // 业务状态码不为 "200" (使用弱等于 `==` 更稳妥)
        if (res.code != 200) {
            // 如果是 401 (Token 失效或未登录)
            if (res.code == 401) {
                ElMessage.warning(res.msg || '登录已过期，请重新登录');
                removeToken(); // 使用安全的token移除函数
                // 延迟跳转，让用户能看到提示信息
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
            }
            // API认证异常处理
            else if (res.code === 'API_AUTH_EXPIRED') {
                ElMessage.warning('号池认证已失效，系统将自动重试');
                console.warn('API认证失效:', res.msg);
            }
            else if (res.code === 'API_AUTH_ERROR') {
                ElMessage.error('号池认证失败: ' + (res.msg || '请检查号池配置'));
                console.error('API认证错误:', res.msg);
            }
            else {
                // 其他所有业务错误，直接弹出错误信息
                ElMessage.error(res.msg || '操作失败');
            }
            // 中断 Promise 链，将错误抛出
            return Promise.reject(new Error(res.msg || 'Error'));
        }

        return res;
    },
    error => {
        console.error('HTTP Error:', error);
        let message = '网络请求异常，请稍后重试';
        if (error.response) {
            // 根据 HTTP 状态码提供更具体的错误信息
            switch (error.response.status) {
                case 404:
                    message = '请求的资源不存在 (404)';
                    break;
                case 500:
                case 502:
                case 503:
                case 504:
                    message = '服务器开小差了，请稍后再试 (5xx)';
                    break;
                default:
                    message = `请求错误 (${error.response.status})`;
            }
        } else if (error.message.includes('timeout')) {
            message = '请求超时，请检查网络连接';
        }
        ElMessage.error(message);
        return Promise.reject(error);
    }
);

export default service;
