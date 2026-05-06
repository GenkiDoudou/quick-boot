/**
 * 移动端配置文件
 */

export const mobileConfig = {
  // 响应式断点（像素）
  breakpoints: {
    xs: 320,    // 超小屏手机
    sm: 480,    // 小屏手机
    md: 768,    // 平板
    lg: 1024,   // 桌面
    xl: 1280,   // 大屏桌面
    xxl: 1536   // 超大屏
  },

  // 触摸目标最小尺寸（推荐 44px）
  touchTargetSize: 44,

  // 输入框最小字体大小（防止 iOS 自动放大）
  inputMinFontSize: 16,

  // 安全区域配置
  safeArea: {
    enabled: true,
    top: 0,
    right: 0,
    bottom: 0,
    left: 0
  },

  // 移动端特定功能
  features: {
    // 防止 iOS 输入框自动放大
    preventIOSZoom: true,

    // 禁用双击缩放
    disableDoubleTapZoom: true,

    // 处理安全区域
    handleSafeArea: true,

    // 禁用长按菜单
    disableCallout: true,

    // 禁用高亮效果
    disableTapHighlight: true,

    // 平滑滚动
    smoothScroll: true
  },

  // 响应式字体大小
  fontSize: {
    xs: 12,
    sm: 14,
    md: 16,
    lg: 18,
    xl: 20
  },

  // 响应式间距
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32,
    xxl: 48
  },

  // 设备检测
  detection: {
    // 检测移动设备
    isMobile: () => {
      const userAgent = navigator.userAgent || navigator.vendor || window.opera
      return /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(
        userAgent.toLowerCase()
      )
    },

    // 检测 iOS
    isIOS: () => {
      return /iphone|ipad|ipod/i.test(navigator.userAgent.toLowerCase())
    },

    // 检测 Android
    isAndroid: () => {
      return /android/i.test(navigator.userAgent.toLowerCase())
    },

    // 检测触摸支持
    isTouchSupported: () => {
      return (
        ('ontouchstart' in window) ||
        (navigator.maxTouchPoints > 0) ||
        (navigator.msMaxTouchPoints > 0)
      )
    }
  },

  // 视口配置
  viewport: {
    width: 'device-width',
    initialScale: 1.0,
    maximumScale: 1.0,
    userScalable: 'no',
    viewportFit: 'cover'
  },

  // 性能优化
  performance: {
    // 启用虚拟滚动
    enableVirtualScroll: true,

    // 启用图片懒加载
    enableLazyLoad: true,

    // 启用代码分割
    enableCodeSplit: true,

    // 启用 gzip 压缩
    enableGzip: true
  },

  // 调试模式
  debug: false
}

export default mobileConfig
