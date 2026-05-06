package io.github.genkidoudou.common.security.firewall.sensitiveword;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * 对 {@link SensitiveWordBs} 的只读封装：{@link SensitiveWordBs#init()} 完成后实例仅用于并发读，
 * 本包装类可多线程调用 {@link #replace(String)}、{@link #assertNotContains(String)}、{@link #contains(String)}。
 */
public final class SensitiveWordEngine {

    private final SensitiveWordBs delegate;

    private SensitiveWordEngine(SensitiveWordBs delegate) {
        this.delegate = delegate;
    }

    /**
     * 按配置从资源加载黑白名单并链接内置默认库后 {@link SensitiveWordBs#init()}。
     *
     * @param properties    配置
     * @param resourceLoader Spring 资源加载器
     * @return 已初始化引擎
     */
    public static SensitiveWordEngine create(SensitiveWordFirewallProperties properties,
                                            ResourceLoader resourceLoader) {
        try {
            List<String> white = SensitiveWordListLoader.loadAll(resourceLoader, properties.getWhiteList());
            List<String> black = SensitiveWordListLoader.loadAll(resourceLoader, properties.getBlackList());
            List<String> blackCopy = black;
            List<String> whiteCopy = white;
            IWordDeny deny = blackCopy.isEmpty()
                    ? WordDenys.defaults()
                    : WordDenys.chains(WordDenys.defaults(), () -> blackCopy);
            IWordAllow allow = whiteCopy.isEmpty()
                    ? WordAllows.defaults()
                    : WordAllows.chains(WordAllows.defaults(), () -> whiteCopy);
            SensitiveWordBs bs = SensitiveWordBs.newInstance()
                    .wordDeny(deny)
                    .wordAllow(allow)
                    .init();
            return new SensitiveWordEngine(bs);
        } catch (IOException e) {
            throw new UncheckedIOException("加载敏感词资源失败", e);
        }
    }

    public String replace(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return delegate.replace(text);
    }

    /**
     * THROW 策略：若包含敏感词则抛出 {@link SensitiveWordException}（首词）。
     */
    public void assertNotContains(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (!delegate.contains(text)) {
            return;
        }
        List<String> all = delegate.findAll(text);
        String first = all.isEmpty() ? "" : all.get(0);
        throw new SensitiveWordException(first);
    }

    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return delegate.contains(text);
    }

    /**
     * 单测专用：直接使用已 {@link SensitiveWordBs#init()} 的实例。
     */
    static SensitiveWordEngine wrapForTests(SensitiveWordBs delegate) {
        return new SensitiveWordEngine(delegate);
    }
}
