package io.github.genkidoudou.common.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I18nUtil 国际化工具测试类
 *
 * @author genkidoudou
 */
class I18nUtilTest {

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void testGetCurrentLocale() {
        Locale locale = I18nUtil.getCurrentLocale();
        assertNotNull(locale);
    }

    @Test
    void testSetCurrentLocale() {
        Locale zhCN = Locale.SIMPLIFIED_CHINESE;
        I18nUtil.setCurrentLocale(zhCN);
        
        assertEquals(zhCN, I18nUtil.getCurrentLocale());
    }

    @Test
    void testSetCurrentLocaleToEnglish() {
        Locale enUS = Locale.US;
        I18nUtil.setCurrentLocale(enUS);
        
        assertEquals(enUS, I18nUtil.getCurrentLocale());
    }

    @Test
    void testGetMessageWithCode() {
        String message = I18nUtil.getMessage("test.code");
        assertNotNull(message);
        // 如果没有配置MessageSource，应该返回code本身
        assertEquals("test.code", message);
    }

    @Test
    void testGetMessageWithCodeAndArgs() {
        String message = I18nUtil.getMessage("test.code", new Object[]{"arg1", "arg2"});
        assertNotNull(message);
    }

    @Test
    void testGetMessageWithCodeArgsAndDefault() {
        String defaultMessage = "默认消息";
        String message = I18nUtil.getMessage("test.code", null, defaultMessage);
        assertEquals(defaultMessage, message);
    }

    @Test
    void testGetMessageWithAllParams() {
        String defaultMessage = "Default Message";
        String message = I18nUtil.getMessage("test.code", null, defaultMessage, Locale.US);
        assertEquals(defaultMessage, message);
    }

    @Test
    void testGetMessageWithNullArgs() {
        String message = I18nUtil.getMessage("test.code", null);
        assertNotNull(message);
    }

    @Test
    void testGetMessageWithEmptyArgs() {
        String message = I18nUtil.getMessage("test.code", new Object[]{});
        assertNotNull(message);
    }

    @Test
    void testLocaleContextReset() {
        I18nUtil.setCurrentLocale(Locale.JAPAN);
        assertEquals(Locale.JAPAN, I18nUtil.getCurrentLocale());
        
        LocaleContextHolder.resetLocaleContext();
        
        // 重置后应该恢复到默认locale
        assertNotNull(I18nUtil.getCurrentLocale());
    }
}
