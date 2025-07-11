import { createRouter, createWebHistory } from 'vue-router';
import Layout from '@/layout/Index.vue';

const routes = [
    {
        path: '/login',
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


router.beforeEach((to, from, next): void => {
    const token = localStorage.getItem('authToken');
    if (to.meta.isPublic || token) {
        next();
    } else {
        next({ name: 'Login' });
    }
});


export default router;
