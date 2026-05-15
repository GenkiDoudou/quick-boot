/**
 * 日志记录模块
 * 提供接口请求日志拦截和记录功能
 * 
 * <p>功能特性：</p>
 * <ul>
 *     <li>自动拦截所有 Controller 方法</li>
 *     <li>记录请求参数、返回结果、耗时等信息</li>
 *     <li>支持通过注解忽略日志记录</li>
 *     <li>通过 OpenAPI 注解获取接口描述</li>
 *     <li>通过 Spring 事件发布日志</li>
 *     <li>默认实现控制台日志打印</li>
 * </ul>
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
package io.github.genkidoudou.common.logger;
