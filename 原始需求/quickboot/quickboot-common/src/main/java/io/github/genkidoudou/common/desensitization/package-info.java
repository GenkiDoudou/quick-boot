/**
 * 字段脱敏模块
 * 
 * 提供基于注解的字段脱敏功能，在 JSON 序列化时自动对敏感字段进行脱敏处理
 * 
 * 主要功能：
 * 1. 支持多种常见脱敏类型：姓名、身份证号、手机号、银行卡号、邮箱、地址、密码
 * 2. 支持自定义脱敏策略
 * 3. 基于 Jackson 序列化器实现，无侵入性
 * 4. 使用简单，只需在字段上添加 @Sensitive 注解
 * 
 * 使用示例：
 * <pre>
 * public class UserDTO {
 *     &#64;Sensitive(type = SensitiveType.NAME)
 *     private String name;
 *     
 *     &#64;Sensitive(type = SensitiveType.MOBILE)
 *     private String phone;
 *     
 *     &#64;Sensitive(type = SensitiveType.ID_CARD)
 *     private String idCard;
 *     
 *     &#64;Sensitive(type = SensitiveType.BANK_CARD)
 *     private String bankCard;
 *     
 *     &#64;Sensitive(type = SensitiveType.EMAIL)
 *     private String email;
 *     
 *     &#64;Sensitive(type = SensitiveType.ADDRESS)
 *     private String address;
 *     
 *     &#64;Sensitive(type = SensitiveType.PASSWORD)
 *     private String password;
 *     
 *     // 自定义脱敏：保留前3位和后4位
 *     &#64;Sensitive(type = SensitiveType.CUSTOM, strategy = "3,4")
 *     private String customField;
 * }
 * </pre>
 * 
 * 脱敏规则：
 * <ul>
 *   <li>NAME（姓名）：保留姓氏，其他用*代替。例如：张三 -> 张*</li>
 *   <li>ID_CARD（身份证号）：保留前6位和后4位。例如：110101199001011234 -> 110101********1234</li>
 *   <li>MOBILE（手机号）：保留前3位和后4位。例如：13812345678 -> 138****5678</li>
 *   <li>BANK_CARD（银行卡号）：保留前4位和后4位。例如：6222021234567890123 -> 6222***********0123</li>
 *   <li>EMAIL（邮箱）：保留@前的前2位和@后的域名。例如：example@gmail.com -> ex****@gmail.com</li>
 *   <li>ADDRESS（地址）：保留前6个字符（通常是省市区）。例如：北京市朝阳区某某街道123号 -> 北京市朝阳区******</li>
 *   <li>PASSWORD（密码）：全部用*代替。例如：123456 -> ******</li>
 *   <li>CUSTOM（自定义）：根据 strategy 属性指定保留位数，格式为 "start,end"</li>
 * </ul>
 * 
 * 技术实现：
 * <ul>
 *   <li>使用 Jackson 的 @JsonSerialize 注解和自定义序列化器实现</li>
 *   <li>通过 @JacksonAnnotationsInside 元注解简化使用</li>
 *   <li>实现 ContextualSerializer 接口支持注解参数传递</li>
 *   <li>提供独立的工具类 DesensitizationUtil 可单独使用</li>
 * </ul>
 * 
 * 注意事项：
 * <ul>
 *   <li>脱敏仅在序列化时生效，不影响原始数据</li>
 *   <li>如果字段值为 null 或空字符串，不进行脱敏处理</li>
 *   <li>如果字段长度不足以进行脱敏，返回原始值</li>
 *   <li>自定义策略格式必须为 "start,end"，否则返回原始值</li>
 * </ul>
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
package io.github.genkidoudou.common.desensitization;
