import { createWebHistory, createRouter } from 'vue-router'
import Layout from '@/layout/index.vue'

export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  // C7 组件演示页：已由 Flyway V9 写入 sys_menu（顶级「组件演示」/demo），登录后由 /getRouters 动态挂载，勿在此重复注册以免冲突。
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  // {
  //   path: '/user2',
  //   component: Layout,
  //   hidden: false,
  //   children: [
  //     {
  //       path: 'profile',
  //       component: () => import('@/views/system/user/index.vue'),
  //       name: 'userList',
  //       meta: { title: '用户管理', icon: 'user' }
  //     }
  //   ]
  //
  //
  // },
  // {
  //   path: '/system/dept',
  //   component: Layout,
  //   hidden: false,
  //   children: [
  //     {
  //       path: '',
  //       component: () => import('@/views/system/dept/index.vue'),
  //       name: 'Dept',
  //       meta: { title: '部门管理', icon: 'tree' }
  //     }
  //   ]
  // },
  // {
  //   path: '/system/dict/type',
  //   component: Layout,
  //   hidden: false,
  //   children: [
  //     {
  //       path: '',
  //       component: () => import('@/views/system/dict/type/index.vue'),
  //       name: 'DictType',
  //       meta: { title: '字典管理', icon: 'setting' }
  //     }
  //   ]
  // },
  {
    path: '/system/dict/data/:dictType',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '',
        component: () => import('@/views/system/dict/data/index.vue'),
        name: 'DictData',
        meta: { title: '字典项管理', icon: 'list' }
      }
    ]
  },
  // 参数设置：已由 Flyway V9 写入 sys_menu（系统管理下），登录后由 /getRouters 动态挂载。
  // {
  //   path: '/system/notice',
  //   component: Layout,
  //   hidden: false,
  //   children: [
  //     {
  //       path: '',
  //       component: () => import('@/views/system/notice/index.vue'),
  //       name: 'SysNotice',
  //       meta: { title: '通知公告', icon: 'message' }
  //     }
  //   ]
  // },
  // {
  //   path: '/system/role',
  //   component: Layout,
  //   hidden: false,
  //   children: [
  //     {
  //       path: '',
  //       component: () => import('@/views/system/role/index.vue'),
  //       name: 'SysRole',
  //       meta: { title: '角色管理', icon: 'peoples' }
  //     }
  //   ]
  // },

]

export const dynamicRoutes = []

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export default router

