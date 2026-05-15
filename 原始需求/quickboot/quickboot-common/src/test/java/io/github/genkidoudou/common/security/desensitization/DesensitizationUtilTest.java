package io.github.genkidoudou.common.security.desensitization;

import io.github.genkidoudou.common.desensitization.DesensitizationUtil;
import io.github.genkidoudou.common.desensitization.SensitiveType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DesensitizationUtil 脱敏工具测试类
 *
 * @author genkidoudou
 */
class DesensitizationUtilTest {

    @Test
    void testDesensitizeName() {
        assertEquals("张*", DesensitizationUtil.desensitizeName("张三"));
        assertEquals("欧阳**", DesensitizationUtil.desensitizeName("欧阳娜娜"));
        assertEquals("李", DesensitizationUtil.desensitizeName("李"));
        assertNull(DesensitizationUtil.desensitizeName(null));
        assertEquals("", DesensitizationUtil.desensitizeName(""));
    }

    @Test
    void testDesensitizeIdCard() {
        assertEquals("110101********1234", DesensitizationUtil.desensitizeIdCard("110101199001011234"));
        assertEquals("123456", DesensitizationUtil.desensitizeIdCard("123456")); // 长度不足
        assertNull(DesensitizationUtil.desensitizeIdCard(null));
    }

    @Test
    void testDesensitizeMobile() {
        assertEquals("138****5678", DesensitizationUtil.desensitizeMobile("13812345678"));
        assertEquals("1234567", DesensitizationUtil.desensitizeMobile("1234567")); // 长度不足
        assertNull(DesensitizationUtil.desensitizeMobile(null));
    }

    @Test
    void testDesensitizeBankCard() {
        assertEquals("6222***********0123", DesensitizationUtil.desensitizeBankCard("6222021234567890123"));
        assertEquals("12345678", DesensitizationUtil.desensitizeBankCard("12345678")); // 长度不足
        assertNull(DesensitizationUtil.desensitizeBankCard(null));
    }

    @Test
    void testDesensitizeEmail() {
        assertEquals("ex*****@gmail.com", DesensitizationUtil.desensitizeEmail("example@gmail.com"));
        assertEquals("ab@test.com", DesensitizationUtil.desensitizeEmail("ab@test.com"));
        assertEquals("a@test.com", DesensitizationUtil.desensitizeEmail("a@test.com"));
        assertEquals("invalid", DesensitizationUtil.desensitizeEmail("invalid")); // 无@符号
        assertNull(DesensitizationUtil.desensitizeEmail(null));
    }

    @Test
    void testDesensitizeAddress() {
        assertEquals("北京市朝阳区******", DesensitizationUtil.desensitizeAddress("北京市朝阳区某某街道123号"));
        assertEquals("北京市", DesensitizationUtil.desensitizeAddress("北京市")); // 长度不足
        assertNull(DesensitizationUtil.desensitizeAddress(null));
    }

    @Test
    void testDesensitizePassword() {
        assertEquals("******", DesensitizationUtil.desensitizePassword("123456"));
        assertEquals("******", DesensitizationUtil.desensitizePassword("verylongpassword"));
        assertEquals("", DesensitizationUtil.desensitizePassword(""));
        assertNull(DesensitizationUtil.desensitizePassword(null));
    }

    @Test
    void testDesensitizeCustom() {
        assertEquals("138****5678", DesensitizationUtil.desensitizeCustom("13812345678", "3,4"));
        assertEquals("110***34", DesensitizationUtil.desensitizeCustom("11012234", "3,2"));
        assertEquals("test", DesensitizationUtil.desensitizeCustom("test", "invalid")); // 无效策略
        assertEquals("test", DesensitizationUtil.desensitizeCustom("test", null));
        assertNull(DesensitizationUtil.desensitizeCustom(null, "3,4"));
    }

    @Test
    void testDesensitizeWithType() {
        assertEquals("张*", DesensitizationUtil.desensitize("张三", SensitiveType.NAME, null));
        assertEquals("110101********1234", DesensitizationUtil.desensitize("110101199001011234", SensitiveType.ID_CARD, null));
        assertEquals("138****5678", DesensitizationUtil.desensitize("13812345678", SensitiveType.MOBILE, null));
        assertEquals("6222***********0123", DesensitizationUtil.desensitize("6222021234567890123", SensitiveType.BANK_CARD, null));
        assertEquals("ex*****@gmail.com", DesensitizationUtil.desensitize("example@gmail.com", SensitiveType.EMAIL, null));
        assertEquals("北京市朝阳区******", DesensitizationUtil.desensitize("北京市朝阳区某某街道123号", SensitiveType.ADDRESS, null));
        assertEquals("******", DesensitizationUtil.desensitize("123456", SensitiveType.PASSWORD, null));
    }

    @Test
    void testDesensitizeWithCustomType() {
        assertEquals("138****5678", DesensitizationUtil.desensitize("13812345678", SensitiveType.CUSTOM, "3,4"));
    }

    @Test
    void testDesensitizeWithNullValue() {
        assertNull(DesensitizationUtil.desensitize(null, SensitiveType.NAME, null));
    }

    @Test
    void testDesensitizeWithEmptyValue() {
        assertEquals("", DesensitizationUtil.desensitize("", SensitiveType.NAME, null));
    }

    @Test
    void testDesensitizeCustomWithInvalidStrategy() {
        assertEquals("test", DesensitizationUtil.desensitizeCustom("test", "abc"));
        assertEquals("test", DesensitizationUtil.desensitizeCustom("test", "1"));
        assertEquals("test", DesensitizationUtil.desensitizeCustom("test", ""));
    }

    @Test
    void testDesensitizeCustomWithValueTooShort() {
        assertEquals("test", DesensitizationUtil.desensitizeCustom("test", "3,3"));
        assertEquals("test", DesensitizationUtil.desensitizeCustom("test", "5,5"));
    }
}
