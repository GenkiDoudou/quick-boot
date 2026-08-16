/**
 * 用户行为监控 API：会话列表与时间线轨迹。
 */
import request from '@/utils/request'

/** 分页查询用户行为会话列表 */
export function listUserBehaviorSessions(query) {
  return request({
    url: '/monitor/userBehavior/sessions',
    method: 'get',
    params: query
  })
}

/** 获取指定会话下的行为时间线（页面访问、操作、接口等事件） */
export function getUserBehaviorTimeline(query) {
  return request({
    url: '/monitor/userBehavior/timeline',
    method: 'get',
    params: query
  })
}
