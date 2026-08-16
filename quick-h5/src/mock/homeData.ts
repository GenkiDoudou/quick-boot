/** 首页壳层：消息/待办仍为 mock；快捷入口已接真 API，勿再作快捷回退数据 */

export interface ShortcutItem {
  id: string
  label: string
  short: string
  tone?: 'green' | 'blue' | 'amber' | 'rose'
  badge?: number
}

export interface MessageItem {
  id: string
  tag: string
  tagType: 'flow' | 'sys' | 'warn'
  title: string
  time: string
  unread: boolean
}

export interface TodoItem {
  id: string
  title: string
  priority: '高' | '中' | '低'
  done: boolean
}

export const homeShortcuts: ShortcutItem[] = [
  { id: 'approve', label: '待审批', short: '审', tone: 'green' },
  { id: 'online', label: '在线用户', short: '在', tone: 'blue' },
  { id: 'login-log', label: '登录日志', short: '登', tone: 'amber', badge: 2 },
  { id: 'report', label: '报表', short: '报', tone: 'rose' },
]

export const homeMessages: MessageItem[] = [
  { id: '1', tag: '告警', tagType: 'warn', title: '慢 SQL：users 查询耗时 320ms', time: '10:12', unread: true },
  { id: '2', tag: '流程', tagType: 'flow', title: '你有 1 条待审批', time: '09:40', unread: true },
  { id: '3', tag: '系统', tagType: 'sys', title: '本周六 02:00 系统维护 30 分钟', time: '昨天', unread: false },
]

export const homeTodos: TodoItem[] = [
  { id: 't1', title: '处理合同审批', priority: '高', done: false },
  { id: 't2', title: '查看登录失败记录', priority: '中', done: false },
  { id: 't3', title: '阅读系统公告', priority: '低', done: true },
]
