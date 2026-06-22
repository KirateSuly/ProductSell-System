/**
 * Vue Router 路由配置
 *
 * 路由架构：
 * - /login         → 登录页（公开访问，仅管理员登录入口）
 * - /               → MainLayout 布局（需认证）
 *   - /dashboard    → 系统首页（仪表盘）
 *   - /users        → 用户管理（仅 ADMIN）
 *   - /products     → 商品管理
 *   - /customers    → 客户管理
 *   - /orders       → 订单管理
 *   - /statistics   → 销售统计
 *
 * 路由守卫逻辑：
 * 1. 目标页需要认证 + 无 Token → 跳转 /login
 * 2. 已登录访问 /login → 跳转 /dashboard
 * 3. 目标页有角色限制 + 角色不匹配 → 跳转 /dashboard
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '系统首页' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/user/UserManageView.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'products',
        name: 'ProductManage',
        component: () => import('@/views/product/ProductManageView.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'customers',
        name: 'CustomerManage',
        component: () => import('@/views/customer/CustomerManageView.vue'),
        meta: { title: '客户管理' }
      },
      {
        path: 'orders',
        name: 'OrderManage',
        component: () => import('@/views/order/OrderManageView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/StatisticsView.vue'),
        meta: { title: '销售统计' }
      }
    ]
  },
  // 未匹配路由重定向到首页
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：认证与权限校验
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 未登录访问需认证页面 → 跳转登录
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
  }
  // 已登录访问登录页 → 跳转首页
  else if (to.path === '/login' && userStore.token) {
    next('/dashboard')
  }
  // 角色权限校验
  else if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    next('/dashboard')
  }
  else {
    next()
  }
})

export default router
