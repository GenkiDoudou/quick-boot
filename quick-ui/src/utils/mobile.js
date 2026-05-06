/**
 * 移动端工具函数
 */

/**
 * 检测是否为移动设备
 */
export function isMobileDevice() {
  const userAgent = navigator.userAgent || navigator.vendor || window.opera
  return /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(userAgent.toLowerCase())
}

/**
 * 检测是否为 iOS 设备
 */
export function isIOS() {
  return /iphone|ipad|ipod/i.test(navigator.userAgent.toLowerCase())
}

/**
 * 检测是否为 Android 设备
 */
export function isAndroid() {
  return /android/i.test(navigator.userAgent.toLowerCase())
}

/**
 * 获取设备类型
 */
export function getDeviceType() {
  const width = window.innerWidth
  if (width < 480) {
    return 'xs' // 超小屏
  } else if (width < 768) {
    return 'sm' // 小屏手机
  } else if (width < 1024) {
    return 'md' // 平板
  } else {
    return 'lg' // 桌面
  }
}

/**
 * 防止 iOS 输入框自动放大
 */
export function preventIOSZoom() {
  if (isIOS()) {
    const inputs = document.querySelectorAll('input, textarea, select')
    inputs.forEach(input => {
      input.addEventListener('focus', () => {
        document.body.style.zoom = 1
      })
      input.addEventListener('blur', () => {
        document.body.style.zoom = 1
      })
    })
  }
}

/**
 * 处理安全区域（刘海屏、底部导航栏等）
 */
export function handleSafeArea() {
  const root = document.documentElement
  const env = CSS.supports('padding: max(0px)') ? 'max' : 'env'
  
  root.style.setProperty('--safe-area-inset-top', `${env}(0px, env(safe-area-inset-top))`)
  root.style.setProperty('--safe-area-inset-right', `${env}(0px, env(safe-area-inset-right))`)
  root.style.setProperty('--safe-area-inset-bottom', `${env}(0px, env(safe-area-inset-bottom))`)
  root.style.setProperty('--safe-area-inset-left', `${env}(0px, env(safe-area-inset-left))`)
}

/**
 * 禁用双击缩放
 */
export function disableDoubleTapZoom() {
  let lastTouchEnd = 0
  document.addEventListener('touchend', (event) => {
    const now = Date.now()
    if (now - lastTouchEnd <= 300) {
      event.preventDefault()
    }
    lastTouchEnd = now
  }, false)
}

/**
 * 获取视口高度（考虑虚拟键盘）
 */
export function getViewportHeight() {
  return Math.max(
    document.documentElement.clientHeight,
    window.innerHeight || 0
  )
}

/**
 * 监听设备方向变化
 */
export function onOrientationChange(callback) {
  window.addEventListener('orientationchange', callback)
  window.addEventListener('resize', callback)
  
  return () => {
    window.removeEventListener('orientationchange', callback)
    window.removeEventListener('resize', callback)
  }
}

/**
 * 获取设备像素比
 */
export function getDevicePixelRatio() {
  return window.devicePixelRatio || 1
}

/**
 * 检测是否支持触摸
 */
export function isTouchSupported() {
  return (
    ('ontouchstart' in window) ||
    (navigator.maxTouchPoints > 0) ||
    (navigator.msMaxTouchPoints > 0)
  )
}

/**
 * 初始化移动端环境
 */
export function initMobileEnvironment() {
  if (isMobileDevice()) {
    preventIOSZoom()
    handleSafeArea()
    disableDoubleTapZoom()
  }
}
