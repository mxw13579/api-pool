import { createRouter, createWebHistory } from 'vue-router';
import Layout from '@/layout/Index.vue';

// 导入token管理函数
function getToken(): string | null {
    try {
        const token = sessionStorage.getItem('auth_token');
        const expiry = sessionStorage.getItem('auth_token_expiry');
        
        if (!token || !expiry) {
            return null;
        }
        
        // 检查token是否过期
        if (Date.now() > parseInt(expiry)) {
            sessionStorage.removeItem('auth_token');
            sessionStorage.removeItem('auth_token_expiry');
            return null;
        }
        
        return token;
    } catch (error) {
        console.error('Error getting token:', error);
        return null;
    }
}

const routes = [
    {
        path: '/index.html',
        name: 'Login',
        component: () => import('@/views/login/Index.vue'),
        meta: { title: '登录', isPublic: true }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/pool',
        children: [
            {
                path: 'pool',
                name: 'Pool',
                component: () => import('@/views/pool/Index.vue'),
                meta: { title: '号池管理' }
            },
            {
                path: 'proxy',
                name: 'Proxy',
                component: () => import('@/views/proxy/Index.vue'),
                meta: { title: '代理管理' }
            },
            {
                path: 'account',
                name: 'Account',
                component: () => import('@/views/account/Index.vue'),
                meta: { title: '账户管理' }
            }
        ]
    },
    // 可以添加一个404页面
    {
        path: '/:pathMatch(.*)*',
        redirect: '/',
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});


router.beforeEach((to, _from, next): void => {
    const token = getToken(); // 使用安全的token获取函数
    if (to.meta.isPublic || token) {
        next();
    } else {
        next({ name: 'Login' });
    }
});


export default router;
