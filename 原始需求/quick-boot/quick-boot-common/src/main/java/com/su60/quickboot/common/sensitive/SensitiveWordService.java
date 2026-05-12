package com.su60.quickboot.common.sensitive;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.result.WordResultHandlers;
import com.su60.quickboot.common.security.config.SecurityProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词服务，封装 houbb 工具初始化与检测。
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "security.sensitive-word", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SensitiveWordService {

	private final SecurityProperties securityProperties;

	private SensitiveWordBs sensitiveWordBs;
	private List<String> whiteList;
	private List<String> blackList;

	@PostConstruct
	public void init() {
		reload();
	}

	/**
	 * 重新加载敏感词词库。
	 */
	public synchronized void reload() {
		SecurityProperties.SensitiveWordProperties cfg = securityProperties.getSensitiveWord();

		this.whiteList = SensitiveWordFileLoader.load(cfg.getWhiteListPath());
		this.blackList = SensitiveWordFileLoader.load(cfg.getBlackListPath());

		SensitiveWordBs bs = SensitiveWordBs.newInstance()
				.ignoreCase(true)
				.ignoreWidth(true)
				.ignoreNumStyle(true)
				.ignoreChineseStyle(true)
				.ignoreEnglishStyle(true)
				.ignoreRepeat(true)
				.init();

		this.sensitiveWordBs = bs;
		log.info("敏感词库已加载，白名单:{}条，黑名单:{}条",
				CollUtil.size(whiteList), CollUtil.size(blackList));
	}

	/**
	 * 获取文本中的敏感词列表。
	 */
	public List<String> findWords(String text) {
		if (sensitiveWordBs == null || text == null || text.isEmpty()) {
			return List.of();
		}
		String handled = removeWhiteList(text);
		List<String> hits = sensitiveWordBs.findAll(handled, WordResultHandlers.word());
		if (CollUtil.isNotEmpty(blackList)) {
			blackList.forEach(word -> {
				if (handled.contains(word)) {
					hits.add(word);
				}
			});
		}
		return hits;
	}

	/**
	 * 是否包含敏感词。
	 */
	public boolean contains(String text) {
		return !findWords(text).isEmpty();
	}


	/**
	 * 是否包含敏感词。
	 */
	public String getText(String text) {
		return String.join(",", findWords(text));
	}

	/**
	 * 替换敏感词为 *
	 */
	public String replace(String text) {
		if (sensitiveWordBs == null || text == null || text.isEmpty()) {
			return text;
		}
		String result = sensitiveWordBs.replace(text);
		if (CollUtil.isNotEmpty(blackList)) {
			for (String word : blackList) {
				if (StrUtil.isBlank(word)) {
					continue;
				}
				result = result.replace(word, StrUtil.repeat('*', word.length()));
			}
		}
		return result;
	}

	private String removeWhiteList(String text) {
		if (CollUtil.isEmpty(whiteList)) {
			return text;
		}
		String cleaned = text;
		for (String w : whiteList) {
			if (StrUtil.isNotBlank(w) && cleaned.contains(w)) {
				cleaned = cleaned.replace(w, "");
			}
		}
		return cleaned;
	}
}
