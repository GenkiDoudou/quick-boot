// 加密工具类
import {sm2, sm3, sm4} from 'sm-crypto'

/**
 * sm4 加密工具类
 * @type {{}}
 */

export const sm4Utils = {

    /**
     * 前端加密：自动拼接IV+密文 → Base64
     * @param {string} plaintext 明文
     * @returns {string} Base64密文（含IV）
     */
    encryptSM4(plaintext, sm4Key, ivHex) {
        return sm4.encrypt(plaintext, sm4Key, {
            mode: 'cbc',
            cipherType: 'hex', // 先输出十六进制密文
            iv: ivHex           // 显式传入 IV
        });
    },

    /**
     * 前端解密：自动分离IV → 解密
     * @param {string} base64Cipher 后端返回的Base64密文（含IV）
     * @returns {string} 明文
     */
    decryptSM4(plaintext, sm4Key, ivHex) {
        return sm4.decrypt(plaintext, sm4Key, {
            mode: 'cbc',
            cipherType: 'hex', // 先输出十六进制密文
            iv: ivHex           // 显式传入 IV
        });
    },

// 生成 16 字节 hex
    genHex16() {
        return [...crypto.getRandomValues(new Uint8Array(16))]
            .map(b => b.toString(16).padStart(2, '0'))
            .join('')
    }

}


/**************************sm2***********************/

const CONFIG = {
    userId: '1234567812345678', // Hutool 默认 UserID
    cipherMode: 1,              // 1 = C1C3C2 格式
    publicKeyType: 'hex',       // 使用十六进制格式
    privateKeyType: 'hex'       // 避免 Base64 转换
}
export const sm2Utils = {
    // 配置常量（与后端 Hutool 严格对齐）
// 配置（与 Hutool 严格对齐）


    /**
     * 用公钥加密
     * @param plaintext 明文字符串
     * @param publicKeyHex 公钥（十六进制字符串，130字符）
     * @return 密文（Base64 字符串）
     */
    encryptSM2(plaintext, publicKeyHex) {
        return sm2.doEncrypt(
            plaintext,
            publicKeyHex,
            {
                cipherMode: CONFIG.cipherMode,
                publicKeyType: CONFIG.publicKeyType,
                userId: CONFIG.userId
            }
        );
    },

    /**
     * 用私钥解密
     * @param cipherBase64 密文（Base64 字符串）
     * @param privateKeyHex 私钥（十六进制字符串，64字符）
     * @return 明文字符串
     */
    decryptSM2(cipherBase64, privateKeyHex) {
        return sm2.doDecrypt(
            cipherBase64,
            privateKeyHex,
            {
                cipherMode: CONFIG.cipherMode,
                privateKeyType: CONFIG.privateKeyType,
                userId: CONFIG.userId
            }
        );
    },

    /**
     * 生成 SM2 密钥对（十六进制格式）
     * @return 对象 { publicKey: "130字符十六进制", privateKey: "64字符十六进制" }
     */
    generateKeyPair() {
        // sm-crypto 原生返回十六进制，无需任何转换
        return sm2.generateKeyPairHex();
    }

}

/**
 * 前端 Nonce 工具类
 */
/**
 * 前端 Nonce 工具类 (纯 JS 版)
 * 逻辑：Base64( 4位随机前缀 + XOR混淆后的Hex + 4位随机后缀 )
 */
export const securityNonceUtils = {
    /**
     * 生成 Nonce
     * @param {string} salt 约定的盐值
     */
    generate(salt) {
        const timestamp = Date.now().toString(); // 13位字符串
        let obscuredHex = "";

        for (let i = 0; i < timestamp.length; i++) {
            // 循环异或混淆字符编码
            const mixed = timestamp.charCodeAt(i) ^ salt.charCodeAt(i % salt.length);
            // 转为16进制并补齐两位
            obscuredHex += mixed.toString(16).padStart(2, '0');
        }

        // 生成随机4位字符串作为前后缀（干扰项）
        const prefix = Math.random().toString(36).substring(2, 6);
        const suffix = Math.random().toString(36).substring(2, 6);

        // 使用浏览器原生的 btoa 进行 Base64 编码
        return btoa(prefix + obscuredHex + suffix);
    },

    /**
     * 从 Nonce 解析 Timestamp
     * @param {string} nonce
     * @param {string} salt
     */
    parse(nonce, salt) {
        try {
            const decoded = atob(nonce);
            // 截取中间的 Hex 串 (去掉前4位和后4位)
            const hex = decoded.substring(4, decoded.length - 4);
            let timestampStr = "";

            for (let i = 0; i < hex.length / 2; i++) {
                const hexPair = hex.substring(i * 2, i * 2 + 2);
                const mixed = parseInt(hexPair, 16);
                // 再次异或还原字符
                timestampStr += String.fromCharCode(mixed ^ salt.charCodeAt(i % salt.length));
            }
            return parseInt(timestampStr);
        } catch (e) {
            console.error("Nonce 解析失败:", e);
            return -1;
        }
    }
};

// sm3算法
export const sm3Utils = {
    /**
     * sm3 加密
     * @param {string} plaintext 明文
     * @returns {string} Base64密文
     */
    digest(plaintext) {
        return sm3(plaintext);
    }
}