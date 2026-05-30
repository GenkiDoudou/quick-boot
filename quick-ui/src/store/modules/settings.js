import defaultSettings from '@/settings'
import { useDynamicTitle } from '@/utils/dynamicTitle'
import { defineStore } from 'pinia'

const { sideTheme, showSettings, navType, topNav, tagsView, fixedHeader, sidebarLogo, dynamicTitle } = defaultSettings

const storageSetting = JSON.parse(localStorage.getItem('layout-setting')) || ''

function resolveNavType(storage) {
  if (storage?.navType !== undefined && storage.navType !== null && storage.navType !== '') {
    return Number(storage.navType)
  }
  if (storage?.topNav === true) {
    return 2
  }
  return navType
}

const useSettingsStore = defineStore(
  'settings',
  {
    state: () => ({
      title: '',
      theme: storageSetting.theme || '#409EFF',
      sideTheme: storageSetting.sideTheme || sideTheme,
      showSettings: showSettings,
      navType: resolveNavType(storageSetting),
      topNav: storageSetting.topNav === undefined ? topNav : storageSetting.topNav,
      tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
      fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
      sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
      dynamicTitle: storageSetting.dynamicTitle === undefined ? dynamicTitle : storageSetting.dynamicTitle
    }),
    actions: {
      changeSetting(data) {
        const { key, value } = data
        if (Object.prototype.hasOwnProperty.call(this, key)) {
          this[key] = value
        }
      },
      setTitle(title) {
        this.title = title
        useDynamicTitle()
      }
    }
  })

export default useSettingsStore
