import {appConfig} from '@/config/env'

export default {
    /**
     * 网页标题
     */
    title: appConfig.title,
    /**
     * 侧边栏主题 深色主题theme-dark，浅色主题theme-light
     */
    sideTheme: 'theme-dark',
    /**
     * 是否系统布局配置
     */
    showSettings: true,

    /**
     * 导航模式：1 左侧菜单 | 2 混合菜单（顶栏一级 + 侧栏子菜单）| 3 顶部菜单
     */
    navType: 1,

    /**
     * @deprecated 请使用 navType；保留兼容旧配置
     */
    topNav: false,

    /**
     * 是否显示 tagsView
     */
    tagsView: true,

    /**
     * 是否固定头部
     */
    fixedHeader: false,

    /**
     * 是否显示logo
     */
    sidebarLogo: true,

    /**
     * 是否显示动态标题
     */
    dynamicTitle: false,

    /**
     * @type {string | array} 'production' | ['production', 'development']
     * @description Need show err logs component.
     * The default is only used in the production env
     * If you want to also use it in dev, you can pass ['production', 'development']
     */
    errorLog: 'production',


    /**
     * 路由白名单
     **/
    permissionWhiteList: ['/login', '/register'],

    /**
     * 加密的url 白名单
     */
    secureEncryptionWhiteList: {
        request: ['/api/system/user/login'],
        response: ['/api/system/user/info']
    },

    /**
     * 解密的url白名单
     */
    decryptWhiteList: ['/api/system/user/info'],

    /**
     * 签名的url白名单
     */
    signatureWhiteList: ['/api/system/user/info'],
}
