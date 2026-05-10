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
  {
    path: '/dev/c7-button-e2e',
    component: () => import('@/views/dev/C7ButtonE2E.vue'),
    hidden: false,
    meta: { title: 'C7Button E2E' }
  },
  {
    path: '/dev/c7-select-e2e',
    component: () => import('@/views/dev/C7SelectE2E.vue'),
    hidden: false,
    meta: { title: 'C7Select Dev' }
  },
  {
    path: '/dev/c7-cascader-e2e',
    component: () => import('@/views/dev/C7CascaderE2E.vue'),
    hidden: false,
    meta: { title: 'C7Cascader Dev' }
  },
  {
    path: '/dev/c7-pagination-e2e',
    component: () => import('@/views/dev/C7PaginationE2E.vue'),
    hidden: true,
    meta: { title: 'C7Pagination Dev' }
  },
  {
    path: '/dev/c7-copy-e2e',
    component: () => import('@/views/dev/C7CopyE2E.vue'),
    hidden: true,
    meta: { title: 'C7Copy Dev' }
  },
  {
    path: '/dev/c7-card-e2e',
    component: () => import('@/views/dev/C7CardE2E.vue'),
    hidden: true,
    meta: { title: 'C7Card Dev' }
  },
  {
    path: '/dev/c7-checkbox-e2e',
    component: () => import('@/views/dev/C7CheckboxE2E.vue'),
    hidden: true,
    meta: { title: 'C7Checkbox Dev' }
  },
  {
    path: '/dev/c7-radio-e2e',
    component: () => import('@/views/dev/C7RadioE2E.vue'),
    hidden: true,
    meta: { title: 'C7Radio Dev' }
  },
  {
    path: '/dev/c7-switch-e2e',
    component: () => import('@/views/dev/C7SwitchE2E.vue'),
    hidden: true,
    meta: { title: 'C7Switch Dev' }
  },
  {
    path: '/dev/c7-datepicker-e2e',
    component: () => import('@/views/dev/C7DatePickerE2E.vue'),
    hidden: true,
    meta: { title: 'C7DatePicker Dev' }
  },
  {
    path: '/dev/c7-timepicker-e2e',
    component: () => import('@/views/dev/C7TimePickerE2E.vue'),
    hidden: true,
    meta: { title: 'C7TimePicker Dev' }
  },
  {
    path: '/dev/c7-title-e2e',
    component: () => import('@/views/dev/C7TitleE2E.vue'),
    hidden: true,
    meta: { title: 'C7Title Dev' }
  },
  {
    path: '/dev/c7-dialog-e2e',
    component: () => import('@/views/dev/C7DialogE2E.vue'),
    hidden: true,
    meta: { title: 'C7Dialog Dev' }
  },
  {
    path: '/dev/c7-dict-tag-e2e',
    component: () => import('@/views/dev/C7DictTagE2E.vue'),
    hidden: true,
    meta: { title: 'C7DictTag Dev' }
  },
  {
    path: '/dev/c7-watermark-e2e',
    component: () => import('@/views/dev/C7WatermarkE2E.vue'),
    hidden: true,
    meta: { title: 'C7Watermark Dev' }
  },
  {
    path: '/dev/c7-preview-e2e',
    component: () => import('@/views/dev/C7PreviewE2E.vue'),
    hidden: true,
    meta: { title: 'C7Preview Dev' }
  },
  {
    path: '/dev/c7-json-table-column-e2e',
    component: () => import('@/views/dev/C7JsonTableColumnE2E.vue'),
    hidden: true,
    meta: { title: 'C7JsonTableColumn Dev' }
  },
  {
    path: '/dev/c7-json-table-e2e',
    component: () => import('@/views/dev/C7JsonTableE2E.vue'),
    hidden: true,
    meta: { title: 'C7JsonTable Dev' }
  },
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
  {
    path: '/user2',
    component: Layout,
    hidden: false,
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/index.vue'),
        name: 'userList',
        meta: { title: '用户管理', icon: 'user' }
      }
    ]


  },
  {
    path: '/system/dept',
    component: Layout,
    hidden: false,
    children: [
      {
        path: '',
        component: () => import('@/views/system/dept/index.vue'),
        name: 'Dept',
        meta: { title: '部门管理', icon: 'tree' }
      }
    ]
  },
  {
    path: '/system/dict/type',
    component: Layout,
    hidden: false,
    children: [
      {
        path: '',
        component: () => import('@/views/system/dict/type/index.vue'),
        name: 'DictType',
        meta: { title: '字典管理', icon: 'setting' }
      }
    ]
  },
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

