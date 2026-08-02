import request from '@/utils/request'
import { getCaptchaConfig } from '@/api/captcha'

/**
 * @deprecated 请用 {@link getCaptchaConfig}；保留别名兼容登录页旧引用。
 */
export function getLoginCaptchaConfig() {
    return getCaptchaConfig().then((cfg) => ({
        code: 200,
        data: { captchaEnabled: cfg.captchaEnabled, type: cfg.type }
    }))
}

/** 社交登录入口列表（改用 sa-token 后再接 IdP；当前为空） */
export function listOauthProviders() {
    return Promise.resolve({
        code: 200,
        data: []
    })
}

// 账号密码登录 → POST /login（uuid 为天爱校验成功后的 id，可选）
export function login(username, password, uuid) {
    const data = { username, password }
    if (uuid) {
        data.uuid = uuid
    }
    return request({
        url: '/login',
        headers: {
            isToken: false,
            repeatSubmit: false
        },
        method: 'post',
        data
    })
}

export function getSocialPending(ticket) {
    return request({
        url: '/auth/social/pending',
        headers: { isToken: false },
        method: 'get',
        params: { ticket }
    })
}

export function socialAutoCreate(ticket) {
    return request({
        url: '/auth/social/auto-create',
        headers: { isToken: false },
        method: 'post',
        data: { ticket }
    })
}

export function socialBind(ticket, username, password) {
    return request({
        url: '/auth/social/bind',
        headers: { isToken: false },
        method: 'post',
        data: { ticket, username, password }
    })
}

export function socialComplete(ticket) {
    return request({
        url: '/auth/social/complete',
        headers: { isToken: false },
        method: 'get',
        params: { ticket }
    })
}

// 手机号登录
export function phoneLogin(phone, smsCode) {
    const data = {
        phone,
        smsCode
    }
    return request({
        url: '/phoneLogin',
        headers: {
            isToken: false,
            repeatSubmit: false
        },
        method: 'post',
        data: data
    })
}

// 发送短信验证码
export function sendSms(phone) {
    return request({
        url: '/sendSms',
        headers: {
            isToken: false
        },
        method: 'post',
        data: { phone }
    })
}

// 扫码登录
export function qrcodeLogin(qrcodeId) {
    return request({
        url: '/qrcodeLogin',
        headers: {
            isToken: false,
            repeatSubmit: false
        },
        method: 'post',
        data: { qrcodeId }
    })
}

// 获取用户详细信息（映射 /auth/me）
export function getInfo() {
    return request({
        url: '/auth/me',
        method: 'get'
    }).then((res) => {
        const me = res?.data || {}
        const roles = Array.isArray(me.roles) ? me.roles : []
        const permissions = Array.isArray(me.permissions) ? me.permissions : []
        return {
            ...res,
            data: {
                user: {
                    userId: me.userId,
                    userName: me.username,
                    nickName: me.nickName,
                    avatar: ''
                },
                roles,
                permissions
            }
        }
    })
}

// 退出方法（JWT 无服务端会话，本地清理即可）
export function logout() {
    return Promise.resolve({ code: 200 })
}

// 获取二维码
export function getQRCode() {
    return request({
        url: '/qrcodeImage',
        headers: {
            isToken: false
        },
        method: 'get',
        timeout: 20000
    })
}
