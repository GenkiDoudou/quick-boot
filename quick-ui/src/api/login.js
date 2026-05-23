import request from '@/utils/request'

/**
 * 是否启用登录行为验证码（与后端 qc.login.captcha-enabled 一致）。
 * @returns {Promise<{ code: number, msg?: string, data: { captchaEnabled: boolean } }>}
 */
export function getLoginCaptchaConfig() {
    return request({
        url: '/login/captcha-config',
        headers: { isToken: false },
        method: 'get'
    })
}

// 账号密码登录（天爱二次校验：validate 成功后回传 captchaId）
export function login(username, password, captchaId) {
    return request({
        url: '/login',
        headers: {
            isToken: false,
            repeatSubmit: false
        },
        method: 'post',
        params: { username, password, captchaId }
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

// 获取用户详细信息
export function getInfo() {
    return request({
        url: '/getInfo',
        method: 'get'
    })
}

// 退出方法
export function logout() {
    return request({
        url: '/logout',
        method: 'post'
    })
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
