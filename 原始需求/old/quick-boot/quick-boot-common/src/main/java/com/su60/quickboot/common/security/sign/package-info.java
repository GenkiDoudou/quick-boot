/**
 * 接口签名验证模块
 * 
 * <p>本模块实现了基于国密SM3算法的接口签名验证功能，用于保证接口调用的安全性和完整性。</p>
 * 
 * <h2>核心功能</h2>
 * <ul>
 *   <li>IP白名单支持：白名单内的IP不参与验签</li>
 *   <li>灵活的参数传递：支持从Header或参数中接收签名信息</li>
 *   <li>多种请求类型支持：GET、POST表单、POST JSON</li>
 *   <li>国密SM3算法：使用Hutool的SmUtil.sm3()生成签名</li>
 *   <li>时间戳验证：防止重放攻击</li>
 *   <li>随机字符串：增强安全性</li>
 * </ul>
 * 
 * <h2>核心类说明</h2>
 * <ul>
 *   <li>{@link SignatureFilter} - 签名验证过滤器，自动拦截请求进行验签</li>
 *   <li>{@link SM3SignatureUtils} - SM3签名工具类，提供签名生成和验证方法</li>
 *   <li>{@link SignatureHelper} - 签名辅助类，方便客户端生成签名</li>
 *   <li>{@link CachedBodyHttpServletRequest} - 可重复读取Body的Request包装类</li>
 *   <li>{@link SignatureException} - 签名验证异常</li>
 * </ul>
 * 
 * <h2>配置说明</h2>
 * <pre>
 * security:
 *   sign:
 *     enabled: true                    # 是否启用签名验证
 *     algorithm: SM3                   # 签名算法
 *     secret-key: your-secret-key      # 签名密钥
 *     expire-time: 300                 # 有效期（秒）
 *     ignore-urls:                     # 忽略验签的URL
 *       - /actuator/**
 *       - /public/**
 *     ip-whitelist:                    # IP白名单
 *       - 127.0.0.1
 *       - 192.168.1.100
 * </pre>
 * 
 * <h2>签名算法</h2>
 * <ol>
 *   <li>收集参数：
 *     <ul>
 *       <li>GET请求：所有URL参数</li>
 *       <li>POST表单：所有表单参数</li>
 *       <li>POST JSON：将JSON字符串作为json参数（json=JSON字符串）</li>
 *     </ul>
 *   </li>
 *   <li>添加签名字段：timestamp（时间戳）、nonce（随机字符串）</li>
 *   <li>参数排序：按参数名（key）的字典序升序排列</li>
 *   <li>拼接字符串：key1=value1&amp;key2=value2&amp;key3=value3</li>
 *   <li>拼接密钥：参数字符串&amp;key=secretKey</li>
 *   <li>生成签名：使用SM3算法对拼接后的字符串进行哈希</li>
 * </ol>
 * 
 * <h2>使用示例</h2>
 * 
 * <h3>服务端（自动验签）</h3>
 * <p>只需配置即可，Filter会自动拦截请求进行验签。</p>
 * 
 * <h3>客户端（生成签名）</h3>
 * <pre>
 * // GET请求
 * Map&lt;String, String&gt; params = new HashMap&lt;&gt;();
 * params.put("username", "admin");
 * Map&lt;String, String&gt; signedParams = SignatureHelper.signGetRequest(params, SECRET_KEY);
 * 
 * // POST JSON请求
 * String jsonBody = "{\"username\":\"admin\"}";
 * Map&lt;String, String&gt; signHeaders = SignatureHelper.signJsonRequest(jsonBody, SECRET_KEY);
 * // 将signHeaders中的timestamp、nonce、sign放入Header或参数中
 * </pre>
 * 
 * <h2>安全建议</h2>
 * <ul>
 *   <li>密钥管理：使用环境变量或配置中心管理密钥，定期更换</li>
 *   <li>HTTPS：生产环境必须使用HTTPS，防止中间人攻击</li>
 *   <li>时间同步：确保服务器时间同步（使用NTP）</li>
 *   <li>日志审计：记录签名验证失败的请求，监控异常</li>
 *   <li>限流防护：对签名验证失败的IP进行限流，防止暴力破解</li>
 * </ul>
 * 
 * <h2>常见问题</h2>
 * <ul>
 *   <li>签名验证失败：检查密钥是否一致、时间戳是否有效、参数拼接是否正确</li>
 *   <li>时间戳过期：确保客户端和服务端时间同步，时间差不超过expire-time</li>
 *   <li>JSON请求验签失败：确保将JSON字符串作为json参数参与签名</li>
 * </ul>
 * 
 * <h2>调试</h2>
 * <p>开启DEBUG日志查看详细的签名生成和验证过程：</p>
 * <pre>
 * logging:
 *   level:
 *     com.su60.quickboot.common.security.sign: DEBUG
 * </pre>
 * 
 * @author luyanan
 * @since 2026/01/31
 * @see SignatureFilter
 * @see SM3SignatureUtils
 * @see SignatureHelper
 */
package com.su60.quickboot.common.security.sign;
