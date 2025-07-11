import axios, { type AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

const service = axios.create({
    baseURL: '/api', // Vite 代理会处理这个
    timeout: 10000,
});
//请求拦截器
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        console.error('Request Error:', error);
        return Promise.reject(error);
    }
);

service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data;

        // 业务状态码不为 "200" (使用弱等于 `==` 更稳妥)
        if (res.code != 200) {
            // 如果是 401 (Token 失效或未登录)
            if (res.code == 401) {
                ElMessage.warning(res.msg || '登录已过期，请重新登录');
                localStorage.removeItem('authToken');
                // 延迟跳转，让用户能看到提示信息
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
            } else {
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
