import { createRouter, createWebHistory } from 'vue-router'
import UserLayout from '../views/user/Layout.vue'
//导入Layout.vue文件，并在配置路由的时候作为父容器，先加载
import AdminLayout from '../views/admin/AdminLayout.vue'
import { ElMessage } from 'element-plus'
import AdminLogin from '@/views/admin/Login.vue'
import AdminDashboard from '@/views/admin/Dashboard.vue'
import GateControl from '@/views/admin/GateControl.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/user/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/user/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: UserLayout,
    children: [
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('../views/user/Monitor.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'gates',
        name: 'Gates',
        component: () => import('../views/user/Gates.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'strategy',
        name: 'Strategy',
        component: () => import('../views/user/Strategy.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/profile/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile/avatar',
        name: 'Avatar',
        component: () => import('../views/user/profile/Avatar.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile/password',
        name: 'Password',
        component: () => import('../views/user/profile/Password.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '',//如果空路由，会跳转到/login，也就是刚点进去的时候第一个显示的就是login
        redirect: '/login'
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin,
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { requiresAdmin: true }
      },
      {
        path: 'gates',
        name: 'GateControl',
        component: GateControl,
        meta: { requiresAdmin: true, title: '水闸管理' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),//告诉vue要加载哪个组件
        meta: { requiresAdmin: true }
      },
      {
        path: 'logs',
        name: 'AccessLogs',
        component: () => import('@/views/admin/AccessLogs.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'password',
        name: 'AdminPassword',
        component: () => import('@/views/admin/Password.vue'),
        meta: { requiresAdmin: true }//requiresAdmin自定义名称，表示需要
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫（beforeEach是全局前置守卫，于在路由跳转前进行统一的权限控制和访问拦截）
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')
  const adminInfo = localStorage.getItem('adminInfo')
  
  // 先判断是否访问登录页面
  if (to.path === '/login') {
    if (isAuthenticated) {
      ElMessage.info('您已登录')
      next('/monitor')//调转到monitor路由
    } else {
      next()//访问当前路由
    }
  }
  // 再判断是否访问管理员登录页
  else if (to.path === '/admin/login') {
    if (adminInfo) {
      ElMessage.info('您已登录管理员账号')
      next('/admin/dashboard')
    } else {
      next()
    }
  }
  // 判断需要管理员权限的页面
  else if (to.meta.requiresAdmin) {//检查需要requiresAdmin标记的
    if (!adminInfo) {
      ElMessage.warning('请先登录管理员账号')
      next('/admin/login')
    } else {
      next()
    }
  }
  // 判断需要用户权限的页面
  else if (to.meta.requiresAuth) {
    if (!isAuthenticated) {
      ElMessage.warning('请先登录')
      next('/login')
    } else {
      next()
    }
  }
  // 其他情况
  else {
    next()
  }
})

export default router 