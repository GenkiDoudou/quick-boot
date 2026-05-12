package com.su60.quickboot.common.desensitize;

import cn.hutool.core.util.StrUtil;

/**
 * 脱敏工具。
 */
public class DesensitizeUtils {

    public static String mobile(String mobile) {
        if (StrUtil.isBlank(mobile) || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    public static String idCard(String idCard) {
        if (StrUtil.isBlank(idCard) || idCard.length() < 8) {
            return idCard;
        }
        int len = idCard.length();
        return idCard.substring(0, 6) + "********" + idCard.substring(len - 4);
    }

    public static String bankCard(String bankCard) {
        if (StrUtil.isBlank(bankCard) || bankCard.length() < 8) {
            return bankCard;
        }
        String cleaned = bankCard.replaceAll("\\s", "");
        int len = cleaned.length();
        if (len <= 8) {
            return "****" + cleaned.substring(len - 4);
        }
        return cleaned.substring(0, 4) + " **** **** " + cleaned.substring(len - 4);
    }

    public static String realName(String name) {
        if (StrUtil.isBlank(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "**";
    }

    public static String email(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 3) {
            return username.charAt(0) + "***" + domain;
        }
        return username.substring(0, 3) + "***" + domain;
    }

    public static String address(String address) {
        if (StrUtil.isBlank(address) || address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "***";
    }

    public static String custom(String value, int prefixKeep, int suffixKeep, char maskChar) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        int len = value.length();
        if (len <= prefixKeep + suffixKeep) {
            return value;
        }
        String prefix = value.substring(0, prefixKeep);
        String suffix = value.substring(len - suffixKeep);
        int maskLen = len - prefixKeep - suffixKeep;
        return prefix + String.valueOf(maskChar).repeat(maskLen) + suffix;
    }
}
