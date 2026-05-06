import useSettingsStore from '@/store/modules/settings'

export function useDynamicTitle() {
  const settingsStore = useSettingsStore()
  const title = settingsStore.title
  document.title = title
}
