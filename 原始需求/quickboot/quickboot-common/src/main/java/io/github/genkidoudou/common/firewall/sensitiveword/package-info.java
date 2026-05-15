/**
 * 敏感词过滤模块
 * 
 * 提供敏感词检测、过滤、替换等功能
 * 支持对 HTTP 请求参数和请求体进行敏感词过滤
 * 
 * 主要功能：
 * 1. 支持表单请求和 JSON 请求的敏感词过滤
 * 2. 支持配置敏感词白名单和黑名单
 * 3. 支持配置忽略 URL 列表
 * 4. 支持两种过滤策略：替换为 * 或抛出异常
 * 5. 基于 sensitive-word 库实现，支持多种检测模式
 * 
 * 配置示例：
 * <pre>
 * qc:
 *   security:
 *     firewall:
 *       sensitive-word:
 *         enable: true
 *         whiteList:
 *           - classpath:sensitive-word-white.txt
 *         blackList:
 *           - classpath:sensitive-word-black.txt
 *         ignoreUrls:
 *           - /login
 *           - /api/public/**
 *         strategy: REPLACE
 * </pre>
 * 
 * 使用方式：
 * 1. 添加依赖：
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.github.houbb&lt;/groupId&gt;
 *     &lt;artifactId&gt;sensitive-word&lt;/artifactId&gt;
 *     &lt;version&gt;0.29.4&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 * 
 * 2. 配置敏感词文件（每行一个词）：
 * <pre>
 * # sensitive-word-black.txt
 * 敏感词1
 * 敏感词2
 * </pre>
 * 
 * 3. 启用敏感词过滤：
 * <pre>
 * qc.security.firewall.sensitive-word.enable=true
 * </pre>
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
package io.github.genkidoudou.common.firewall.sensitiveword;
