/**
 * H5 表单轻量校验：失败时 toast，返回 false；成功返回 true。
 * 不引入 schema 库；页面在 submit 前组合调用。
 */
import { toastInfo } from './toast'

/** 必填：trim 后非空 */
export function required(label: string, value: unknown): boolean {
  const ok = value != null && String(value).trim() !== ''
  if (!ok) toastInfo(`请填写${label}`)
  return ok
}

/**
 * 手机号：可空跳过；非空须 11 位国内号段（1 开头）。
 * @returns 通过为 true
 */
export function mobile(value: unknown): boolean {
  const s = String(value ?? '').trim()
  if (!s) return true
  const ok = /^1\d{10}$/.test(s)
  if (!ok) toastInfo('手机号格式不正确')
  return ok
}

/**
 * 邮箱：可空跳过；非空做简单格式校验。
 * @returns 通过为 true
 */
export function email(value: unknown): boolean {
  const s = String(value ?? '').trim()
  if (!s) return true
  const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(s)
  if (!ok) toastInfo('邮箱格式不正确')
  return ok
}

/**
 * 依次执行校验函数；任一失败即中断（该函数内已 toast）。
 * @param checks 返回 boolean 的校验闭包列表
 */
export function assert(checks: Array<() => boolean>): boolean {
  for (const check of checks) {
    if (!check()) return false
  }
  return true
}
