package io.github.genkidoudou.common.firewall.sensitiveword;

import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词服务
 * 
 * 提供敏感词检测、替换等功能
 * 基于 sensitive-word 库实现
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Slf4j
public class SensitiveWordService {

    /**
     * 敏感词配置属性
     *
     * @since 2026/03/02
     */
    private final SensitiveWordProperties properties;

    /**
     * 敏感词引擎
     *
     * @since 2026/03/02
     */
    private final SensitiveWordBs sensitiveWordBs;

    /**
     * 资源加载器
     *
     * @since 2026/03/02
     */
    private final ResourceLoader resourceLoader;

    /**
     * 构造函数
     *
     * @param properties 敏感词配置属性
     * @param resourceLoader 资源加载器
     * @since 2026/03/02
     */
    public SensitiveWordService(SensitiveWordProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.sensitiveWordBs = initSensitiveWordBs();
    }

    /**
     * 初始化敏感词引擎
     *
     * @return 敏感词引擎
     * @since 2026/03/02
     */
    private SensitiveWordBs initSensitiveWordBs() {
        // 1. 加载自定义白名单
        List<String> customWhiteWords = loadWordsFromFiles(properties.getWhiteList());
        
        // 2. 加载自定义黑名单
        List<String> customBlackWords = loadWordsFromFiles(properties.getBlackList());
        
        log.info("准备初始化敏感词引擎，自定义白名单: {} 个，自定义黑名单: {} 个", 
                customWhiteWords.size(), customBlackWords.size());
        
        // 3. 构建敏感词引擎
        SensitiveWordBs bs = SensitiveWordBs.newInstance()
                .ignoreCase(true)  // 忽略大小写
                .ignoreWidth(true)  // 忽略半角全角
                .ignoreRepeat(false)  // 不忽略重复字符
                .enableWordCheck(true)  // 启用词库检测
                .enableNumCheck(false)  // 不启用数字检测
                .enableEmailCheck(false)  // 不启用邮箱检测
                .enableUrlCheck(false);  // 不启用URL检测
        
        // 4. 关键：使用 WordDenys.chains() 来组合内置词库和自定义词库
        // sensitive-word 库默认使用 WordDenys.defaults() 作为内置词库
        if (!customBlackWords.isEmpty()) {
            // 创建自定义词库
            IWordDeny customDeny = () -> customBlackWords;
            
            // 组合默认词库和自定义词库
            IWordDeny combinedDeny = WordDenys.chains(
                    WordDenys.defaults(),  // 默认内置词库
                    customDeny             // 自定义词库
            );
            
            bs.wordDeny(combinedDeny);
            log.info("敏感词黑名单加载完成：系统内置词库 + 自定义 {} 个词", customBlackWords.size());
        } else {
            // 如果没有自定义词库，只使用默认内置词库
            bs.wordDeny(WordDenys.defaults());
            log.info("敏感词黑名单加载完成：仅使用系统内置词库");
        }
        
        // 5. 添加自定义白名单
        if (!customWhiteWords.isEmpty()) {
            bs.wordAllow(() -> customWhiteWords);
            log.info("自定义敏感词白名单加载完成，共 {} 个词", customWhiteWords.size());
        }
        
        // 6. 初始化
        bs.init();
        
        log.info("敏感词引擎初始化完成（系统内置词库 + 自定义词库）");
        return bs;
    }

    /**
     * 从文件列表加载敏感词
     *
     * @param filePaths 文件路径列表
     * @return 敏感词列表
     * @since 2026/03/02
     */
    private List<String> loadWordsFromFiles(List<String> filePaths) {
        List<String> words = new ArrayList<>();
        
        for (String filePath : filePaths) {
            try {
                // 加载资源
                Resource resource = resourceLoader.getResource(filePath);
                if (!resource.exists()) {
                    log.warn("敏感词文件不存在: {}", filePath);
                    continue;
                }
                
                int fileWordCount = 0;
                // 读取文件内容
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        // 跳过空行和注释行
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            words.add(line);
                            fileWordCount++;
                            // 打印每个词的详细信息（包括字节）
                            byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
                            StringBuilder hexStr = new StringBuilder();
                            for (byte b : bytes) {
                                hexStr.append(String.format("%02X ", b));
                            }
                        }
                    }
                }
                
            } catch (IOException e) {
                log.error("加载敏感词文件失败: {}", filePath, e);
            }
        }
        
        return words;
    }

    /**
     * 检测文本是否包含敏感词
     *
     * @param text 待检测文本
     * @return 是否包含敏感词
     * @since 2026/03/02
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return sensitiveWordBs.contains(text);
    }

    /**
     * 查找文本中的所有敏感词
     *
     * @param text 待检测文本
     * @return 敏感词列表
     * @since 2026/03/02
     */
    public List<String> findAll(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return sensitiveWordBs.findAll(text);
    }

    /**
     * 查找文本中的第一个敏感词
     *
     * @param text 待检测文本
     * @return 第一个敏感词，如果没有则返回 null
     * @since 2026/03/02
     */
    public String findFirst(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return sensitiveWordBs.findFirst(text);
    }

    /**
     * 替换文本中的敏感词
     *
     * @param text 待处理文本
     * @return 替换后的文本
     * @since 2026/03/02
     */
    public String replace(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return sensitiveWordBs.replace(text);
    }

    /**
     * 处理文本（根据策略）
     * 
     * 如果策略为 REPLACE，则替换敏感词
     * 如果策略为 THROW，则抛出异常
     *
     * @param text 待处理文本
     * @return 处理后的文本
     * @throws SensitiveWordException 当策略为 THROW 且检测到敏感词时抛出
     * @since 2026/03/02
     */
    public String process(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 检测是否包含敏感词
        if (!contains(text)) {
            return text;
        }
        
        // 根据策略处理
        if (properties.getStrategy() == SensitiveWordProperties.FilterStrategy.THROW) {
            // 抛出异常
            String sensitiveWord = findFirst(text);
            throw new SensitiveWordException(sensitiveWord);
        } else {
            // 替换敏感词
            return replace(text);
        }
    }
}
