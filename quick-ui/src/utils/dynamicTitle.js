/**
 * 根据 settingsStore.title 同步更新 document.title（动态标题开关开启时由 layout 调用）。
 */
import useSettingsStore from '@/store/modules/settings'

export function useDynamicTitle() {
  const settingsStore = useSettingsStore()
  const title = settingsStore.title
  document.title = title
}
