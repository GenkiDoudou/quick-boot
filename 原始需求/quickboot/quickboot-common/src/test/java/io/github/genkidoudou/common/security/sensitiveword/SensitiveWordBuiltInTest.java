package io.github.genkidoudou.common.security.sensitiveword;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 sensitive-word 内置词库
 *
 * @author luyanan
 * @since 2026/03/07
 */
class SensitiveWordBuiltInTest {

    /**
     * 测试默认配置是否包含内置词库
     */
    @Test
    void testDefaultBuiltInWords() {
        // 使用默认配置创建敏感词引擎
        SensitiveWordBs bs = SensitiveWordBs.newInstance().init();
        
        // 测试一些常见的敏感词（这些词应该在内置词库中）
        String[] testWords = {
            "法轮功",
            "六四",
            "台独",
            "藏独",
            "色情",
            "赌博",
            "毒品"
        };
        
        System.out.println("=== 测试内置词库 ===");
        for (String word : testWords) {
            boolean contains = bs.contains(word);
            System.out.println("测试词: " + word + " -> " + (contains ? "检测到" : "未检测到"));
        }
        
        // 至少应该检测到一些常见敏感词
        boolean hasAny = false;
        for (String word : testWords) {
            if (bs.contains(word)) {
                hasAny = true;
                break;
            }
        }
        
        System.out.println("内置词库是否生效: " + hasAny);
    }
    
    /**
     * 测试自定义词库是否会覆盖内置词库
     */
    @Test
    void testCustomWordsWithBuiltIn() {
        // 创建带自定义词库的敏感词引擎
        SensitiveWordBs bs = SensitiveWordBs.newInstance()
                .wordDeny(() -> List.of("自定义敏感词"))
                .init();
        
        System.out.println("=== 测试自定义词库 + 内置词库 ===");
        
        // 测试自定义词
        boolean customWord = bs.contains("自定义敏感词");
        System.out.println("自定义词检测: " + customWord);
        assertTrue(customWord, "应该检测到自定义敏感词");
        
        // 测试内置词是否还存在
        String[] builtInWords = {"法轮功", "六四", "色情"};
        for (String word : builtInWords) {
            boolean contains = bs.contains(word);
            System.out.println("内置词 '" + word + "' 检测: " + contains);
        }
    }
    
    /**
     * 测试只使用自定义词库（不使用内置词库）
     */
    @Test
    void testOnlyCustomWords() {
        // 如果要禁用内置词库，需要明确配置
        SensitiveWordBs bs = SensitiveWordBs.newInstance()
                .wordDeny(() -> List.of("自定义敏感词"))
                // 注意：这里没有找到禁用内置词库的方法
                .init();
        
        System.out.println("=== 测试是否可以只使用自定义词库 ===");
        
        // 测试自定义词
        boolean customWord = bs.contains("自定义敏感词");
        System.out.println("自定义词检测: " + customWord);
        
        // 测试内置词
        boolean builtInWord = bs.contains("法轮功");
        System.out.println("内置词检测: " + builtInWord);
    }
}
