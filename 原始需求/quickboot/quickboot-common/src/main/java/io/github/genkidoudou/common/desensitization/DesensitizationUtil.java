package io.github.genkidoudou.common.desensitization;

/**
 * 脱敏工具类
 * 提供各种类型的数据脱敏方法
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public class DesensitizationUtil {

    private static final String MASK_CHAR = "*";

    /**
     * 根据类型和策略进行脱敏
     *
     * @param value    原始值
     * @param type     脱敏类型
     * @param strategy 自定义策略
     * @return 脱敏后的值
     */
    public static String desensitize(String value, SensitiveType type, String strategy) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        switch (type) {
            case NAME:
                return desensitizeName(value);
            case ID_CARD:
                return desensitizeIdCard(value);
            case MOBILE:
                return desensitizeMobile(value);
            case BANK_CARD:
                return desensitizeBankCard(value);
            case EMAIL:
                return desensitizeEmail(value);
            case ADDRESS:
                return desensitizeAddress(value);
            case PASSWORD:
                return desensitizePassword(value);
            case CUSTOM:
                return desensitizeCustom(value, strategy);
            default:
                return value;
        }
    }

    /**
     * 姓名脱敏：保留姓氏，其他用*代替
     * 例如：张三 -> 张*，欧阳娜娜 -> 欧阳**
     */
    public static String desensitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        int length = name.length();
        if (length == 1) {
            return name;
        }
        // 保留第一个字符（姓氏）
        return name.charAt(0) + MASK_CHAR.repeat(length - 1);
    }

    /**
     * 身份证号脱敏：保留前6位和后4位
     * 例如：110101199001011234 -> 110101********1234
     */
    public static String desensitizeIdCard(String idCard) {
        if (idCard == null || idCard.length() < 11) {
            return idCard;
        }
        return keepStartEnd(idCard, 6, 4);
    }

    /**
     * 手机号脱敏：保留前3位和后4位
     * 例如：13812345678 -> 138****5678
     */
    public static String desensitizeMobile(String mobile) {
        if (mobile == null || mobile.length() < 8) {
            return mobile;
        }
        return keepStartEnd(mobile, 3, 4);
    }

    /**
     * 银行卡号脱敏：保留前4位和后4位
     * 例如：6222021234567890123 -> 6222***********0123
     */
    public static String desensitizeBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 9) {
            return bankCard;
        }
        return keepStartEnd(bankCard, 4, 4);
    }

    /**
     * 邮箱脱敏：保留@前的前2位和@后的域名
     * 例如：example@gmail.com -> ex****@gmail.com
     */
    public static String desensitizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 2) {
            return username + domain;
        }
        return username.substring(0, 2) + MASK_CHAR.repeat(username.length() - 2) + domain;
    }

    /**
     * 地址脱敏：保留前6个字符（通常是省市区）
     * 例如：北京市朝阳区某某街道123号 -> 北京市朝阳区******
     */
    public static String desensitizeAddress(String address) {
        if (address == null || address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + MASK_CHAR.repeat(6);
    }

    /**
     * 密码脱敏：全部用*代替
     * 例如：123456 -> ******
     */
    public static String desensitizePassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return MASK_CHAR.repeat(6);
    }

    /**
     * 自定义脱敏：根据策略保留指定位数
     * 策略格式：start,end
     * 例如：3,4 表示保留前3位和后4位
     */
    public static String desensitizeCustom(String value, String strategy) {
        if (value == null || value.isEmpty() || strategy == null || strategy.isEmpty()) {
            return value;
        }

        try {
            String[] parts = strategy.split(",");
            if (parts.length != 2) {
                return value;
            }
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            return keepStartEnd(value, start, end);
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 保留开始和结束指定位数，中间用*代替
     *
     * @param value 原始值
     * @param start 保留开始位数
     * @param end   保留结束位数
     * @return 脱敏后的值
     */
    private static String keepStartEnd(String value, int start, int end) {
        if (value == null || value.length() <= start + end) {
            return value;
        }
        int maskLength = value.length() - start - end;
        return value.substring(0, start) + MASK_CHAR.repeat(maskLength) + value.substring(value.length() - end);
    }
}
