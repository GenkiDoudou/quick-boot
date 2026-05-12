/**
 * 接口签名工具类（基于国密SM3算法）
 */
import {sm3} from 'sm-crypto'
import {signatureConfig, isUrlInWhitelist} from './signature-config'

/**
 * 生成SM3签名
 * @param {Object} params - 参数对象
 * @param {string} secretKey - 密钥
 * @returns {string} 签名字符串（小写十六进制）
 */
export function generateSignature(params, secretKey) {
    if (!params || typeof params !== 'object') {
        throw new Error('参数必须是对象')
    }
    if (!secretKey) {
        throw new Error('密钥不能为空')
    }

    // 1. 按key排序
    const sortedKeys = Object.keys(params).sort()

    // 2. 拼接参数：key1=value1&key2=value2
    const paramStr = sortedKeys
        .filter(key => {
            // 跳过签名字段本身
            if (key === 'sign' || key === 'signature') {
                return false
            }
            // 跳过空值
            const value = params[key]
            return value !== null && value !== undefined && value !== ''
        })
        .map(key => `${key}=${params[key]}`)
        .join('&')

    // 3. 拼接密钥：参数字符串&key=secretKey
    const signContent = `${paramStr}&key=${secretKey}`

    if (import.meta.env.DEV) {
        console.log('[SM3签名] 待签名字符串:', signContent)
    }

    // 4. 使用SM3算法生成签名（返回小写十六进制）
    const signature = sm3(signContent)

    if (import.meta.env.DEV) {
        console.log('[SM3签名] 生成签名:', signature)
    }

    return signature
}

/**
 * 生成随机字符串（Nonce）
 * @returns {string} 随机字符串
 */
export function generateNonce() {
    return Date.now().toString() + Math.random().toString(36).substring(2, 15)
}

/**
 * 获取当前时间戳（秒）
 * @returns {number} 时间戳
 */
export function getCurrentTimestamp() {
    return Math.floor(Date.now() / 1000)
}

/**
 * 为GET请求生成签名
 * @param {Object} params - 请求参数
 * @returns {Object} 签名信息对象（包含timestamp、nonce、sign）
 */
export function signGetRequest(params = {}) {
    const timestamp = getCurrentTimestamp().toString()
    const nonce = generateNonce()

    const signParams = {
        ...params,
        timestamp: timestamp,
        nonce: nonce
    }

    // 生成签名
    const signature = generateSignature(signParams, signatureConfig.secretKey)

    // 返回签名信息（不包含业务参数）
    return {
        timestamp: timestamp,
        nonce: nonce,
        sign: signature
    }
}

/**
 * 为POST表单请求生成签名
 * @param {Object} params - 表单参数
 * @returns {Object} 签名信息对象（包含timestamp、nonce、sign）
 */
export function signFormRequest(params = {}) {
    const timestamp = getCurrentTimestamp().toString()
    const nonce = generateNonce()

    const signParams = {
        ...params,
        timestamp: timestamp,
        nonce: nonce
    }

    // 生成签名
    const signature = generateSignature(signParams, signatureConfig.secretKey)

    // 返回签名信息（不包含业务参数）
    return {
        timestamp: timestamp,
        nonce: nonce,
        sign: signature
    }
}

/**
 * 为POST JSON请求生成签名
 * @param {string|Object} data - JSON数据（字符串或对象）
 * @param {Object} urlParams - URL查询参数（可选）
 * @returns {Object} 签名信息对象（包含timestamp、nonce、sign）
 */
export function signJsonRequest(data, urlParams = {}) {
    // 如果是对象，转换为JSON字符串
    const jsonBody = typeof data === 'string' ? data : JSON.stringify(data)

    const signParams = {
        ...urlParams,  // 包含URL查询参数

        timestamp: getCurrentTimestamp().toString(),
        nonce: generateNonce(),
        json: jsonBody  // 关键：将JSON字符串作为json参数
    }

    // 生成签名
    const signature = generateSignature(signParams, signatureConfig.secretKey)

    // 返回签名信息（不包含json字段）
    return {
        timestamp: signParams.timestamp,
        nonce: signParams.nonce,
        sign: signature
    }
}

/**
 * 解析URL中的查询参数
 * @param {string} url - URL字符串
 * @returns {Object} 查询参数对象
 */
function parseUrlParams(url) {
    if (!url || !url.includes('?')) {
        return {}
    }

    const queryString = url.split('?')[1]
    if (!queryString) {
        return {}
    }

    const params = {}
    queryString.split('&').forEach(param => {
        const [key, value] = param.split('=')
        if (key) {
            params[key] = decodeURIComponent(value || '')
        }
    })

    return params
}

/**
 * 为请求添加签名
 * @param {Object} config - axios请求配置对象
 * @returns {Object} 添加签名后的配置对象
 */
export function addSignature(config) {
    // 检查是否启用签名
    if (!signatureConfig.enabled) {
        if (import.meta.env.DEV) {
            console.log('[签名] 签名功能未启用')
        }
        return config
    }

    console.log(config)
    // 检查是否在白名单中
    const url = config.url || ''
    if (isUrlInWhitelist(url)) {
        if (import.meta.env.DEV) {
            console.log('[签名] URL在白名单中，跳过签名:', url)
        }
        return config
    }

    const method = (config.method || 'get').toLowerCase()
    const contentType = config.headers['Content-Type'] || config.headers['content-type'] || ''

    // 解析URL中的查询参数
    const urlParams = parseUrlParams(url)

    if (import.meta.env.DEV) {
        console.log('[签名] 开始生成签名:', {
            url,
            method,
            contentType,
            urlParams
        })
    }

    if (method === 'get') {
        // GET请求：签名所有参数（包括URL参数和params参数）
        const params = {...urlParams, ...(config.params || {})}
        const signInfo = signGetRequest(params)

        // 将签名信息放入Header（不修改原始参数）
        config.headers['timestamp'] = signInfo.timestamp
        config.headers['nonce'] = signInfo.nonce
        config.headers['sign'] = signInfo.sign

        if (import.meta.env.DEV) {
            console.log('[签名] GET请求签名完成:', signInfo)
            console.log('[签名] 请求参数:', params)
        }
    } else if (method === 'post' || method === 'put' || method === 'patch') {
        if (contentType.includes('application/json')) {
            // POST JSON请求：将JSON字符串作为json参数签名，同时包含URL参数
            const jsonData = config.data
            const formData = {...urlParams, ...(config.params || {})}
            const signInfo = signJsonRequest(jsonData, formData)

            // 将签名信息放入Header
            config.headers['timestamp'] = signInfo.timestamp
            config.headers['nonce'] = signInfo.nonce
            config.headers['sign'] = signInfo.sign

            if (import.meta.env.DEV) {
                console.log('[签名] POST JSON请求签名完成:', signInfo)
                console.log('[签名] URL参数:', urlParams)
            }
        } else {
            // POST 表单请求：签名所有参数（包括URL参数和表单参数）
            const formData = {...urlParams, ...(config.params || {})}
            const signInfo = signFormRequest(formData)

            // 将签名信息放入Header（不修改原始表单数据）
            config.headers['timestamp'] = signInfo.timestamp
            config.headers['nonce'] = signInfo.nonce
            config.headers['sign'] = signInfo.sign

            if (import.meta.env.DEV) {
                console.log('[签名] POST表单请求签名完成:', signInfo)
                console.log('[签名] 表单参数:', formData)
            }
        }
    }

    return config
}

/**
 * 打印签名信息（用于调试）
 * @param {Object} signInfo - 签名信息对象
 */
export function printSignInfo(signInfo) {
    console.log('========== 签名信息 ==========')
    console.log('timestamp:', signInfo.timestamp)
    console.log('nonce:', signInfo.nonce)
    console.log('sign:', signInfo.sign)
    console.log('==============================')
}

