// 参数解密,响应解密的 方法
import {sm4Utils, sm2Utils, securityNonceUtils, sm3Utils} from '@/utils/SmUtils'
import defaultSetting from '@/settings.js'
import {isWhiteRequest} from '@/utils/index.js'
import {ElMessage} from "element-plus";
import {getToken} from "@/utils/auth.js";


export function paramHandler(config) {

    let enable = isEnableRequestEncrypt(config.url);
    if (!enable) {
        return;
    }
    let clientId = import.meta.env.VITE_CLIENT_ID;
    if (!clientId) {
        throw new Error('前端客户端id未配置！');
    }
    config.headers['client_id'] = clientId

    // 判断
    // 加密公钥
    let publicKey = import.meta.env.VITE_CRYPTO_PUBLIC_KEY;
    if (!publicKey) {
        throw new Error('前端公钥未配置！');
    }
    // 对参数进行加密
    let sm4Key = sm4Utils.genHex16();
    let ivHex = sm4Utils.genHex16();
    // 根据SM4 对请求参数进行加密
    if (config.params) {
        config.params = {
            _enc: sm4Utils.encryptSM4(JSON.stringify(config.params), sm4Key, ivHex)
        }
    }
    if (config.data) {
        config.data = {
            _enc: sm4Utils.encryptSM4(JSON.stringify(config.data), sm4Key, ivHex)
        }
    }
    let key = sm2Utils.encryptSM2(sm4Key + '&' + ivHex, publicKey);
    // 放参数里面
    config.params = {
        ...config.params,
        key: key
    }
}

// 参数是否加密
function isEnableRequestEncrypt(url) {
    return import.meta.env.VITE_CRYPTO_REQUEST_ENABLED && !isWhiteRequest(url, defaultSetting.secureEncryptionWhiteList.request);
}

// 响应是否解密
function isEnableResponseDecrypt(url) {
    return import.meta.env.VITE_CRYPTO_RESPONSE_ENABLED && !isWhiteRequest(url, defaultSetting.secureEncryptionWhiteList.response);
}

export function responseHandler(response) {
    if (!isEnableResponseDecrypt(response.config.url)) {
        return response;
    }
    let privateKey = import.meta.env.VITE_CRYPTO_PRIVATE_KEY;
    if (!privateKey) {
        throw new Error('前端私钥未配置！');
    }
    let data = response.data;
    let _key = data._key;

    let _enc = data._enc;
    // 如果key 或者enc为空, 则提示异常
    if (!_key || !_enc) {
        ElMessage({message: '返回内容错误,请检查', type: 'error', duration: 5 * 1000})
        throw new Error('响应解密失败：缺少必要的密钥或加密数据')
    }

    if (_key.startsWith('04')) {
        // 把04 截取了
        _key = _key.substring(2);
    }
    let sm4KeyAndIv = sm2Utils.decryptSM2(_key, privateKey);
    // sm4KeyAndIv 为空 或者不包含&的时候报错
    if (!sm4KeyAndIv || !sm4KeyAndIv.includes('&')) {
        ElMessage({message: '返回内容错误,请检查', type: 'error', duration: 5 * 1000})
        throw new Error('响应解密失败：密钥格式错误')
    }
    // 根据& 截取 或者sm4Key和IV
    let [sm4Key, ivHex] = sm4KeyAndIv.split('&');
    let res = sm4Utils.decryptSM4(_enc, sm4Key, ivHex);
    // json格式化一下
    response.data = JSON.parse(res);
    return response;
}

export function generateSignature(config) {
    // 是否开启签名验证
    if (!isEnableSignature(config.url)) {
        return;
    }
    let clientSecretKey = import.meta.env.VITE_CLIENT_SECRET;
    if (!clientSecretKey) {
        throw new Error('前端私钥未配置！');
    }
    // 生成nonce
    let nonce = securityNonceUtils.generate(clientSecretKey);
    // 根据参数生成签名

    let paramStr = '';
    if (config.params) {
        let params = config.params;
        // 1. 按key排序
        const sortedKeys = Object.keys(params).sort()
        // 2. 拼接参数：key1=value1&key2=value2
        paramStr = sortedKeys
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
    }
    if (config.data) {
        const data = config.data;
        const jsonBody = typeof data === 'string' ? data : JSON.stringify(data)
        paramStr = "json=" + jsonBody;
    }

    let signContent = "";
    if (paramStr && paramStr.length > 0) {
        signContent = paramStr + "&";
    }
    signContent = `${signContent}nonce=${nonce}`

    let  sign = sm3Utils.digest(signContent);
    config.headers['sign'] = sign
    config.headers['nonce'] = nonce
}

function isEnableSignature(url) {
    return import.meta.env.VITE_CRYPTO_SIGNATURE_ENABLED && !isWhiteRequest(url, defaultSetting.signatureWhiteList);
}



