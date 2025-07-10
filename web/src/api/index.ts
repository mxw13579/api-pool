import axios from 'axios';
import { ElMessage } from 'element-plus';

const service = axios.create({
    baseURL: '/api', // Vite 代理会处理这个
    timeout: 10000,
});

service.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code !== '200') {
            ElMessage({
                message: res.msg || 'Error',
                type: 'error',
                duration: 5 * 1000,
            });
            return Promise.reject(new Error(res.msg || 'Error'));
        } else {
            return res.data;
        }
    },
    error => {
        ElMessage({
            message: error.message,
            type: 'error',
            duration: 5 * 1000,
        });
        return Promise.reject(error);
    }
);

export default service;
