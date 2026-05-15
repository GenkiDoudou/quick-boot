/**
 * 防幂等模块
 * 
 * 提供接口幂等性保证，防止重复提交和重复执行
 * 
 * 主要功能：
 * 1. 支持注解方式标记需要幂等处理的接口
 * 2. 支持自动拦截 POST、PUT、DELETE 请求
 * 3. 支持多种键生成策略（默认、URL、URL+用户、Token）
 * 4. 支持自定义键生成器
 * 5. 支持配置全局和注解级别的过期时间
 * 6. 基于 Redis 或 Caffeine 缓存实现
 * 
 * 技术实现：
 * <ul>
 *   <li>基于 Spring AOP 实现</li>
 *   <li>使用 Spring Cache 抽象层，支持 Redis 和 Caffeine</li>
 *   <li>支持自定义存储实现</li>
 *   <li>支持 Ant 风格路径匹配排除 URL</li>
 * </ul>
 * 
 * 配置示例：
 * <pre>
 * qc:
 *   idempotent:
 *     enabled: true
 *     # 是否自动拦截 POST、PUT、DELETE 请求
 *     auto-intercept: false
 *     # 排除的 URL 列表
 *     exclude-urls:
 *       - /login
 *       - /api/public/**
 *     # 全局过期时间（秒）
 *     expire-time: 5
 *     # 幂等键前缀
 *     key-prefix: idempotent
 *     # 默认提示信息
 *     default-message: 请勿重复提交
 *     # Token 请求头名称
 *     token-header: X-Idempotent-Token
 *     # 缓存类型（redis 或 caffeine）
 *     cache-type: redis
 * </pre>
 * 
 * 使用示例：
 * <pre>
 * // 1. 基本使用 - 使用默认策略
 * &#64;PostMapping("/order")
 * &#64;Idempotent
 * public R&lt;Order&gt; createOrder(&#64;RequestBody OrderDTO orderDTO) {
 *     return R.ok(orderService.createOrder(orderDTO));
 * }
 * 
 * // 2. 指定过期时间
 * &#64;PostMapping("/payment")
 * &#64;Idempotent(expireTime = 10, timeUnit = TimeUnit.SECONDS)
 * public R&lt;Payment&gt; processPayment(&#64;RequestBody PaymentDTO paymentDTO) {
 *     return R.ok(paymentService.processPayment(paymentDTO));
 * }
 * 
 * // 3. 使用 URL 策略
 * &#64;PostMapping("/comment")
 * &#64;Idempotent(strategy = KeyGenerateStrategy.URL)
 * public R&lt;Comment&gt; addComment(&#64;RequestBody CommentDTO commentDTO) {
 *     return R.ok(commentService.addComment(commentDTO));
 * }
 * 
 * // 4. 使用 URL + 用户策略
 * &#64;PostMapping("/like")
 * &#64;Idempotent(strategy = KeyGenerateStrategy.URL_USER, expireTime = 60)
 * public R&lt;Void&gt; like(&#64;RequestParam Long articleId) {
 *     likeService.like(articleId);
 *     return R.ok();
 * }
 * 
 * // 5. 使用 Token 策略
 * &#64;PostMapping("/submit")
 * &#64;Idempotent(strategy = KeyGenerateStrategy.TOKEN, message = "表单已提交，请勿重复提交")
 * public R&lt;Void&gt; submitForm(&#64;RequestBody FormDTO formDTO) {
 *     formService.submit(formDTO);
 *     return R.ok();
 * }
 * 
 * // 6. 执行完成后删除键
 * &#64;PostMapping("/vote")
 * &#64;Idempotent(deleteAfterExecution = true)
 * public R&lt;Void&gt; vote(&#64;RequestParam Long optionId) {
 *     voteService.vote(optionId);
 *     return R.ok();
 * }
 * 
 * // 7. 自定义键生成器
 * &#64;PostMapping("/custom")
 * &#64;Idempotent(strategy = KeyGenerateStrategy.CUSTOM, keyGenerator = "myKeyGenerator")
 * public R&lt;Void&gt; customMethod(&#64;RequestBody CustomDTO dto) {
 *     customService.process(dto);
 *     return R.ok();
 * }
 * </pre>
 * 
 * 键生成策略说明：
 * <ul>
 *   <li><b>DEFAULT</b>: 方法签名 + 所有参数的 hashCode</li>
 *   <li><b>URL</b>: 请求路径 + 所有参数的 hashCode</li>
 *   <li><b>URL_USER</b>: 请求路径 + 用户标识 + 所有参数的 hashCode</li>
 *   <li><b>TOKEN</b>: 从请求头中获取幂等 Token（需要前端生成并传递）</li>
 *   <li><b>CUSTOM</b>: 使用自定义的键生成器</li>
 * </ul>
 * 
 * 自定义键生成器示例：
 * <pre>
 * &#64;Component("myKeyGenerator")
 * public class MyKeyGenerator implements IdempotentKeyGenerator {
 *     
 *     &#64;Override
 *     public String generateKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
 *         // 自定义键生成逻辑
 *         Object[] args = joinPoint.getArgs();
 *         // 例如：使用第一个参数的某个字段作为键
 *         if (args.length > 0 && args[0] instanceof OrderDTO) {
 *             OrderDTO order = (OrderDTO) args[0];
 *             return "order:" + order.getOrderNo();
 *         }
 *         return "default:key";
 *     }
 * }
 * </pre>
 * 
 * 自定义存储实现示例：
 * <pre>
 * &#64;Component
 * public class CustomIdempotentStorage implements IdempotentStorage {
 *     
 *     &#64;Override
 *     public boolean setIfAbsent(String key, String value, long expireTime, TimeUnit timeUnit) {
 *         // 自定义存储逻辑
 *         return true;
 *     }
 *     
 *     &#64;Override
 *     public void delete(String key) {
 *         // 自定义删除逻辑
 *     }
 *     
 *     &#64;Override
 *     public boolean exists(String key) {
 *         // 自定义检查逻辑
 *         return false;
 *     }
 * }
 * </pre>
 * 
 * 自动拦截配置：
 * <pre>
 * qc:
 *   idempotent:
 *     # 启用自动拦截
 *     auto-intercept: true
 *     # 排除不需要幂等检查的 URL
 *     exclude-urls:
 *       - /login
 *       - /register
 *       - /api/public/**
 *       - /health
 * </pre>
 * 
 * Token 策略使用示例：
 * <pre>
 * // 前端代码
 * // 1. 获取幂等 Token（可以使用 UUID）
 * const token = generateUUID();
 * 
 * // 2. 在请求头中添加 Token
 * axios.post('/api/submit', data, {
 *     headers: {
 *         'X-Idempotent-Token': token
 *     }
 * });
 * 
 * // 后端代码
 * &#64;PostMapping("/submit")
 * &#64;Idempotent(strategy = KeyGenerateStrategy.TOKEN)
 * public R&lt;Void&gt; submit(&#64;RequestBody FormDTO formDTO) {
 *     formService.submit(formDTO);
 *     return R.ok();
 * }
 * </pre>
 * 
 * 异常处理：
 * <pre>
 * &#64;RestControllerAdvice
 * public class GlobalExceptionHandler {
 *     
 *     &#64;ExceptionHandler(IdempotentException.class)
 *     public R&lt;Void&gt; handleIdempotentException(IdempotentException e) {
 *         log.warn("幂等检查失败: {}", e.getMessage());
 *         return R.fail(e.getMessage());
 *     }
 * }
 * </pre>
 * 
 * 注意事项：
 * <ul>
 *   <li>幂等键会在过期时间后自动删除</li>
 *   <li>如果方法执行失败（抛出异常），幂等键会被删除，允许重试</li>
 *   <li>使用 TOKEN 策略时，前端需要生成并传递唯一的 Token</li>
 *   <li>自动拦截功能会拦截所有 POST、PUT、DELETE 请求，建议配置排除列表</li>
 *   <li>Redis 和 Caffeine 都支持过期时间，推荐使用 Redis（分布式环境）</li>
 *   <li>如果使用 Caffeine，需要确保缓存配置支持过期时间</li>
 * </ul>
 * 
 * 性能建议：
 * <ul>
 *   <li>合理设置过期时间，避免过长或过短</li>
 *   <li>使用 Redis 时建议配置连接池</li>
 *   <li>对于高并发场景，建议使用 Redis</li>
 *   <li>对于单机应用，可以使用 Caffeine</li>
 * </ul>
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
package io.github.genkidoudou.common.firewall.idempotent;
