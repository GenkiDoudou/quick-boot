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
  // 部门管理：当前 getRouters 为空，使用 constantRoutes 静态挂载（与 permission 动态路由并存时可改为后端菜单驱动）
  {
    path: '/system',
    component: Layout,
    meta: { title: '系统管理', icon: 'system' },
    children: [
      {
        path: 'dept',
        component: () => import('@/views/system/dept/index.vue'),
        name: 'SystemDept',
        meta: { title: '部门管理', icon: 'tree' }
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
    path: '/demo',
    component: Layout,
    meta: { title: '组件演示', icon: 'tools' },
    children: [
      {
        path: 'c7-button',
        component: () => import('@/views/demo/c7-button/index'),
        name: 'DemoC7Button',
        meta: { title: 'C7Button 演示', icon: 'promotion' }
      },
      {
        path: 'c7-button-group',
        component: () => import('@/views/demo/c7-button-group/index'),
        name: 'DemoC7ButtonGroup',
        meta: { title: 'C7ButtonGroup 演示', icon: 'promotion' }
      },
      {
        path: 'c7-card',
        component: () => import('@/views/demo/c7-card/index'),
        name: 'DemoC7Card',
        meta: { title: 'C7Card 演示', icon: 'promotion' }
      },
      {
        path: 'c7-cascader',
        component: () => import('@/views/demo/c7-cascader/index'),
        name: 'DemoC7Cascader',
        meta: { title: 'C7Cascader 演示', icon: 'promotion' }
      },
      {
        path: 'c7-checkbox',
        component: () => import('@/views/demo/c7-checkbox/index'),
        name: 'DemoC7Checkbox',
        meta: { title: 'C7Checkbox 演示', icon: 'promotion' }
      },
      {
        path: 'c7-copy',
        component: () => import('@/views/demo/c7-copy/index'),
        name: 'DemoC7Copy',
        meta: { title: 'C7Copy 演示', icon: 'promotion' }
      },
      {
        path: 'c7-date-picker',
        component: () => import('@/views/demo/c7-date-picker/index'),
        name: 'DemoC7DatePicker',
        meta: { title: 'C7DatePicker 演示', icon: 'promotion' }
      },
      {
        path: 'c7-descriptions',
        component: () => import('@/views/demo/c7-descriptions/index'),
        name: 'DemoC7Descriptions',
        meta: { title: 'C7Descriptions 演示', icon: 'promotion' }
      },
      {
        path: 'c7-dialog',
        component: () => import('@/views/demo/c7-dialog/index'),
        name: 'DemoC7Dialog',
        meta: { title: 'C7Dialog 演示', icon: 'promotion' }
      },
      {
        path: 'c7-dict-tag',
        component: () => import('@/views/demo/c7-dict-tag/index'),
        name: 'DemoC7DictTag',
        meta: { title: 'C7DictTag 演示', icon: 'promotion' }
      },
      {
        path: 'c7-message-box',
        component: () => import('@/views/demo/c7-message-box/index'),
        name: 'DemoC7MessageBox',
        meta: { title: 'C7MessageBox 演示', icon: 'promotion' }
      },
      {
        path: 'c7-excel-download',
        component: () => import('@/views/demo/c7-excel-download/index'),
        name: 'DemoC7ExcelDownload',
        meta: { title: 'C7ExcelDownload 演示', icon: 'promotion' }
      },
      {
        path: 'c7-excel-upload',
        component: () => import('@/views/demo/c7-excel-upload/index'),
        name: 'DemoC7ExcelUpload',
        meta: { title: 'C7ExcelUpload 演示', icon: 'promotion' }
      },
      {
        path: 'c7-json-form',
        component: () => import('@/views/demo/c7-json-form/index'),
        name: 'DemoC7JsonForm',
        meta: { title: 'C7JsonForm 演示', icon: 'promotion' }
      },
      {
        path: 'c7-json-table',
        component: () => import('@/views/demo/c7-json-table/index'),
        name: 'DemoC7JsonTable',
        meta: { title: 'C7JsonTable 演示', icon: 'promotion' }
      },
      {
        path: 'c7-pagination',
        component: () => import('@/views/demo/c7-pagination/index'),
        name: 'DemoC7Pagination',
        meta: { title: 'C7Pagination 演示', icon: 'promotion' }
      },
      {
        path: 'c7-time-picker',
        component: () => import('@/views/demo/c7-time-picker/index'),
        name: 'DemoC7TimePicker',
        meta: { title: 'C7TimePicker 演示', icon: 'promotion' }
      },
      {
        path: 'c7-title',
        component: () => import('@/views/demo/c7-title/index'),
        name: 'DemoC7Title',
        meta: { title: 'C7Title 演示', icon: 'promotion' }
      },
      {
        path: 'c7-tree-select',
        component: () => import('@/views/demo/c7-tree-select/index'),
        name: 'DemoC7TreeSelect',
        meta: { title: 'C7TreeSelect 演示', icon: 'promotion' }
      },
      {
        path: 'c7-upload',
        component: () => import('@/views/demo/c7-upload/index'),
        name: 'DemoC7Upload',
        meta: { title: 'C7Upload 演示', icon: 'promotion' }
      },
      {
        path: 'c7-watermark',
        component: () => import('@/views/demo/c7-watermark/index'),
        name: 'DemoC7Watermark',
        meta: { title: 'C7Watermark 演示', icon: 'promotion' }
      },
      {
        path: 'c7-json-table-column',
        component: () => import('@/views/demo/c7-json-table-column/index'),
        name: 'DemoC7JsonTableColumn',
        meta: { title: 'C7JsonTableColumn 演示', icon: 'promotion' }
      }
    ]
  }
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
