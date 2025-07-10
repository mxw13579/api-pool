import { createRouter, createWebHistory } from 'vue-router';
import Layout from '@/layout/Index.vue';

const routes = [
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
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
