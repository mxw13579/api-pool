import axios, { AxiosResponse } from 'axios';

import { ElMessage } from 'element-plus';


console.log('✅ [request.ts] 模块被加载了！自定义的 Axios 实例即将被创建。');


const service = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
});

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 在发送请求之前做些什么
        const token = localStorage.getItem('authToken');
        if (token) {
            // 让每个请求携带自定义token
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        // 对请求错误做些什么
        console.log(error); // for debug
        return Promise.reject(error);
    }
);

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        // 后端返回的响应体
        const res = response.data;

        console.log('响应数据:', res);
        if (res.code == 200) {
            return res;
        }

        // 如果 code 是 401 (未登录或 token 失效)
        if (res.code == 401) {
            console.warn('业务码401：' + res.msg);
            localStorage.removeItem('authToken');
            // 跳转到登录页
            window.location.href = '/login';
            // 阻止继续执行，并返回一个被拒绝的 Promise
            return Promise.reject(new Error(res.msg || 'Error: Not Logged In'));
        }

        // 处理其他所有业务错误
        // 在这里可以弹出一个全局的错误提示
        // 比如使用 Element Plus 的 ElMessage
        ElMessage({
            message: res.msg || 'Error',
            type: 'error',
            duration: 5 * 1000
        });

        // 将错误继续抛出，以便在具体的 API 调用处可以捕获（如果需要）
        return Promise.reject(new Error(res.msg || 'Error'));
    },
    error => {
        // --- 处理 HTTP 层面错误 (非 2xx 状态码) ---
        console.error('HTTP Error:', error); // for debug

        // 这里可以处理网络断开、服务器崩溃(500)、请求超时等HTTP级别的错误
        let message = '网络请求失败，请稍后重试';
        if (error.response) {
            switch(error.response.status) {
                case 404:
                    message = '请求的资源未找到 (404)';
                    break;
                case 500:
                    message = '服务器内部错误 (500)';
                    break;
                // 其他 HTTP 错误码...
            }
        } else if (error.message.includes('timeout')) {
            message = '请求超时，请检查您的网络连接';
        }

        ElMessage({
            message: message,
            type: 'error',
            duration: 5 * 1000
        });

        return Promise.reject(error);
    }
);


export default service;
