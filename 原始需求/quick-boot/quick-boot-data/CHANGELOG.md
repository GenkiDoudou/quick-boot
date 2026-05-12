# Changelog
Changelog of My Project.

## Unreleased
### No issue

**docs(components): 添加前端组件文档**

 * - 新增 C7Button 按钮组件文档，包含基本用法、预设类型、确认提示、表单验证等功能说明
 * - 新增 C7ButtonGroup 按钮组组件文档，介绍自动折叠、响应式布局、下拉菜单等功能
 * - 新增 C7Card 卡片组件文档，涵盖展开收起、色块装饰、标题尺寸等特性说明
 * - 新增 C7Cascader 级联选择器文档，包括懒加载、多选、返回结果类型等配置选项
 * - 新增 C7Checkbox 多选组件文档，支持全选、按钮样式、异步数据加载等功能
 * - 新增 C7Copy 复制组件文档，提供多种显示模式和自定义提示功能
 * - 新增 C7DatePicker 日期选择器文档，包含自动格式推断、范围值合并等功能
 * - 新增 C7Descriptions 描述列表组件文档，支持多列布局、字典转换、自定义格式化
 * - 新增 C7Dialog 对话框组件文档，统一 Dialog 和 Drawer 两种模式的使用方式
 * - 新增 C7DictTag 字典标签组件文档，实现值到标签的转换和多值处理
 * - 新增 C7JsonForm JSON 表单组件文档，支持动态生成表单和联动功能
 * - 新增 C7JsonTable 表格组件文档，集成搜索、分页、排序、筛选等完整功能

[96fffcf28cba45b](https://gitee.com/hexlo-dev/quickboot/commit/96fffcf28cba45b) luyanan *2026-02-14 11:54:10*

**refactor(api): 统一REST API端点并改进依赖注入**

 * - 将所有控制器中的PUT和DELETE请求改为POST请求，统一更新和删除操作的端点设计
 * - 移除Autowired注解，改用构造函数注入方式实现依赖注入
 * - 在登录控制器中移除try-catch块，统一使用全局异常处理器
 * - 更新全局异常处理器中的参数校验和错误处理逻辑
 * - 修改开发环境数据库配置信息
 * - 更新前端依赖包的哈希值和版本信息

[a2eec412805d911](https://gitee.com/hexlo-dev/quickboot/commit/a2eec412805d911) luyanan *2026-02-14 10:59:14*

**refactor(c7-plus): 重构组件库以提升代码质量与错误处理**

 * - 在 c7-button 组件中引入防抖和错误处理功能，优化点击逻辑
 * - 统一使用 logger 替代 console 输出，增强日志管理
 * - 实现标准化错误处理机制，替代原有的 console.error
 * - 修复 SysMenuServiceImpl 中断言条件的逻辑错误
 * - 禁用 flyway 数据库迁移功能，调整应用配置
 * - 优化 c7-json-table 组件卸载逻辑，防止内存泄漏
 * - 改进 c7-dialog 组件卸载处理，避免路由切换时白屏问题
 * - 优化 c7-json-form 组件双向绑定逻辑，防止循环更新
 * - 增强日期选择器占位符处理，支持范围类型占位符
 * - 移除生产环境中的调试日志输出
 * - 重构系统客户端视图，优化复制功能和错误处理
 * - 优化表格数据加载逻辑，提升性能和稳定性
 * - 增加组件卸载时的资源清理机制，防止状态泄漏

[30c65091a430afe](https://gitee.com/hexlo-dev/quickboot/commit/30c65091a430afe) luyanan *2026-02-14 08:30:12*

**refactor(system): 优化OAuth客户端管理功能**

 * - 移除控制器中的测试代码main方法
 * - 更新开发环境数据库配置信息
 * - 重构字典标签显示逻辑，支持逗号分隔的多值处理
 * - 移除调试用的console.log语句
 * - 优化OAuth客户端详情页面UI布局，使用卡片分组展示
 * - 调整表格列宽度和溢出提示配置
 * - 添加组件事件发射器定义
 * - 修改按钮组属性绑定方式

[fe0a6aadfb50bcd](https://gitee.com/hexlo-dev/quickboot/commit/fe0a6aadfb50bcd) luyanan *2026-02-14 06:00:40*

**feat(security): 添加响应加密控制参数**

 * - 在 SM2EncryptResponseWrapper 中新增 encryption 参数控制是否执行加密
 * - 修改 SecureEncryptionFilter 中 isEnableResponseEncrypt 方法增加 HttpServletResponse 参数
 * - 添加文件下载请求判断逻辑，避免对下载响应进行加密处理
 * - 调整前端请求拦截器中响应处理逻辑的位置
 * - 保持二进制数据响应的直接返回功能

[dd89af42856284c](https://gitee.com/hexlo-dev/quickboot/commit/dd89af42856284c) luyanan *2026-02-12 16:01:25*

**feat(security): 添加响应加密控制参数**

 * - 在 SM2EncryptResponseWrapper 中新增 encryption 参数控制是否执行加密
 * - 修改 SecureEncryptionFilter 中 isEnableResponseEncrypt 方法增加 HttpServletResponse 参数
 * - 添加文件下载请求判断逻辑，避免对下载响应进行加密处理
 * - 调整前端请求拦截器中响应处理逻辑的位置
 * - 保持二进制数据响应的直接返回功能

[4ea135e6b4b3699](https://gitee.com/hexlo-dev/quickboot/commit/4ea135e6b4b3699) luyanan *2026-02-12 15:53:00*

**feat(security): 实现SM3签名验签与Nonce时间戳验证功能**

 * - 新增SecurityNonceUtil工具类，提供Nonce生成与解析功能
 * - 在SecureEncryptionFilter中集成SM3签名验证逻辑
 * - 修改SM3SignatureUtils，优化签名生成和验证流程
 * - 添加请求参数收集功能，支持GET和POST JSON参数提取
 * - 增加时间戳过期检查，防止重放攻击
 * - 更新SysOauthClient相关实体类，添加状态和密钥字段
 * - 配置安全属性，支持签名功能开关控制
 * - 添加jsoup依赖用于安全防护增强

[af7892520eb7d4d](https://gitee.com/hexlo-dev/quickboot/commit/af7892520eb7d4d) luyanan *2026-02-12 09:43:27*

**增加文档**


[5236d729510eca6](https://gitee.com/hexlo-dev/quickboot/commit/5236d729510eca6) luyanan *2026-02-11 15:42:14*

**feat(system): 优化OAuth客户端服务实现并增强前端界面**

 * - 在SysOauthClientServiceImpl中添加缓存配置和清理注解
 * - 将私钥字段重命名为客户端密钥，更新相关业务逻辑
 * - 修改验证类型判断逻辑从equals改为contains方式
 * - 新增getEnableByClientId方法用于根据客户端ID查询启用状态
 * - 在SaTokenPermissionConfiguration中使用LoginUser缓存数据提升性能
 * - 移除不必要的数据权限规则引擎代码
 * - 重构前端OAuth客户端视图，添加复制功能和卡片式布局
 * - 优化客户端信息查看对话框的UI展示效果

[590c292373bbbd9](https://gitee.com/hexlo-dev/quickboot/commit/590c292373bbbd9) luyanan *2026-02-10 15:20:27*

**refactor(system): 优化OAuth客户端管理功能**

 * - 移除控制器中的测试代码main方法
 * - 更新开发环境数据库配置信息
 * - 重构字典标签显示逻辑，支持逗号分隔的多值处理
 * - 移除调试用的console.log语句
 * - 优化OAuth客户端详情页面UI布局，使用卡片分组展示
 * - 调整表格列宽度和溢出提示配置
 * - 添加组件事件发射器定义
 * - 修改按钮组属性绑定方式

[ec2918532e4eeb6](https://gitee.com/hexlo-dev/quickboot/commit/ec2918532e4eeb6) luyanan *2026-02-09 09:39:50*

**feat(security): 实现SM3签名验签与Nonce时间戳验证功能**

 * - 新增SecurityNonceUtil工具类，提供Nonce生成与解析功能
 * - 在SecureEncryptionFilter中集成SM3签名验证逻辑
 * - 修改SM3SignatureUtils，优化签名生成和验证流程
 * - 添加请求参数收集功能，支持GET和POST JSON参数提取
 * - 增加时间戳过期检查，防止重放攻击
 * - 更新SysOauthClient相关实体类，添加状态和密钥字段
 * - 配置安全属性，支持签名功能开关控制
 * - 添加jsoup依赖用于安全防护增强

[a6c1ef58f6214a1](https://gitee.com/hexlo-dev/quickboot/commit/a6c1ef58f6214a1) luyanan *2026-02-08 15:40:22*

**feat(security): 添加响应加密控制参数**

 * - 在 SM2EncryptResponseWrapper 中新增 encryption 参数控制是否执行加密
 * - 修改 SecureEncryptionFilter 中 isEnableResponseEncrypt 方法增加 HttpServletResponse 参数
 * - 添加文件下载请求判断逻辑，避免对下载响应进行加密处理
 * - 调整前端请求拦截器中响应处理逻辑的位置
 * - 保持二进制数据响应的直接返回功能

[f62293bb9ced154](https://gitee.com/hexlo-dev/quickboot/commit/f62293bb9ced154) luyanan *2026-02-05 15:23:08*

**debug(security): 移除调试代码和硬编码密钥**

 * - 删除了 SecureEncryptionFilter 中的调试打印语句
 * - 注释掉了 SmUtils 中动态生成密钥对的代码
 * - 使用硬编码的固定密钥对替换动态生成逻辑
 * - 移除了调试用的加密解密测试代码

[91223efdec1cde5](https://gitee.com/hexlo-dev/quickboot/commit/91223efdec1cde5) luyanan *2026-02-05 14:54:34*

**feat(request): 添加响应处理和加密配置优化**

 * - 从 secureEncryption.js 导入 responseHandler 函数
 * - 移除 request.js 中的加密测试代码和注释
 * - 在响应拦截器中使用 responseHandler 处理响应数据
 * - 删除不再使用的 KEY_CONFIG 配置对象
 * - 重构 secureEncryption.js 中的加密启用逻辑
 * - 添加响应解密功能和相关配置
 * - 分离请求加密和响应解密的启用配置
 * - 添加私钥配置验证和错误处理
 * - 更新 .env.development 环境变量配置
 * - 修复 settings.js 中的安全加密白名单配置语法错误

[c5f0a3288e95744](https://gitee.com/hexlo-dev/quickboot/commit/c5f0a3288e95744) luyanan *2026-02-05 14:52:03*

**refactor(security): 重构安全加密过滤器实现请求响应加解密功能**

 * - 移除 SM2EncryptResponseWrapper 构造函数中的 function 参数
 * - 重构 SecureEncryptionFilter 实现请求解密和响应加密的独立控制
 * - 添加 isEnableRequestDecrypt 和 isEnableResponseEncrypt 方法控制加解密开关
 * - 修复 SaTokenLoginService 中空指针异常问题
 * - 更新开发环境数据库配置信息
 * - 优化前端 secureEncryption.js 中的参数处理逻辑
 * - 添加解密URL白名单配置到 settings.js
 * - 更新前端开发环境加密配置参数

[c98bdfbe8142d9f](https://gitee.com/hexlo-dev/quickboot/commit/c98bdfbe8142d9f) luyanan *2026-02-05 09:36:16*

**feat(security): 更新SM2加密解密过滤器配置**

 * - 将SM2CryptoFilter、SM2DecryptRequestWrapper、SM2EncryptResponseWrapper标记为注释状态
 * - 修改SM2EncryptResponseWrapper构造函数参数和encryptAndWrite方法实现
 * - 添加AbstractOncePerRequestFilter抽象类提供基础过滤器功能
 * - 重构SecurityProperties中的Crypto配置类结构
 * - 调整RequestParamFilterFilter过滤器执行顺序
 * - 更新SignatureFilter签名验证过滤器代码格式

[c58b585eaff94b1](https://gitee.com/hexlo-dev/quickboot/commit/c58b585eaff94b1) luyanan *2026-02-04 16:12:16*

**feat(request): 添加请求参数加密功能**

 * - 引入 secureEncryption.js 工具文件实现参数加密逻辑
 * - 在 request.js 中集成 paramHandler 函数处理请求参数加密
 * - 添加 SM4 和 SM2 国密算法支持及密钥生成函数
 * - 配置开发环境变量启用加密功能和设置公钥
 * - 注释掉原有的 SM 测试代码并保留加密流程调用
 * - 实现请求参数自动加密传输的安全机制

[b1a9b0f2430d1fb](https://gitee.com/hexlo-dev/quickboot/commit/b1a9b0f2430d1fb) luyanan *2026-02-04 09:41:05*

**feat(utils): 添加请求白名单判断功能并优化代码格式**

 * - 添加 isWhiteRequest 函数用于判断请求是否命中白名单
 * - 添加 extractPath 函数提取 pathname（去掉域名、query、hash）
 * - 添加 matchWithWildcard 函数实现通配符匹配
 * - 从 settings 中引入 permissionWhiteList 替代硬编码白名单
 * - 统一代码缩进格式，将所有函数体调整为一致的缩进风格
 * - 优化 import 语句格式，统一空格和括号使用方式
 * - 在 settings.js 中新增 permissionWhiteList 配置项

[80d9419cd02651a](https://gitee.com/hexlo-dev/quickboot/commit/80d9419cd02651a) luyanan *2026-02-04 09:40:39*

**feat(request): 添加请求参数加密功能**

 * - 引入 secureEncryption.js 工具文件实现参数加密逻辑
 * - 在 request.js 中集成 paramHandler 函数处理请求参数加密
 * - 添加 SM4 和 SM2 国密算法支持及密钥生成函数
 * - 配置开发环境变量启用加密功能和设置公钥
 * - 注释掉原有的 SM 测试代码并保留加密流程调用
 * - 实现请求参数自动加密传输的安全机制

[5146b53a4530fa2](https://gitee.com/hexlo-dev/quickboot/commit/5146b53a4530fa2) luyanan *2026-02-04 09:39:59*

**feat(generator): 完善代码生成器功能并优化前端模板**

 * - 排除定时任务表和flyway表的生成
 * - 添加主键字段和类型的提取与处理
 * - 使用SysConfigUtils获取作者和包路径配置
 * - 自动处理主键、备注、创建更新字段的显示逻辑
 * - 生成API接口文件并支持搜索字段过滤
 * - 重构前端add-or-update组件使用新UI组件库
 * - 创建系统配置控制器和服务管理功能
 * - 优化菜单权限查询逻辑
 * - 添加国际化异常消息定义

[51558ec276d778f](https://gitee.com/hexlo-dev/quickboot/commit/51558ec276d778f) luyanan *2026-02-04 06:49:33*

**feat(security): 新增国密算法加密解密功能**

 * - 添加后端SM2和SM4加密解密工具类SmUtils
 * - 配置application-dev.yml中的加密相关配置项
 * - 在前端request.js中集成加密解密工具并进行测试
 * - 添加前端SmUtils.js加密解密工具类
 * - 配置前端开发环境中的加密开关
 * - 更新依赖包相关文件

[541e4e835ff2b8b](https://gitee.com/hexlo-dev/quickboot/commit/541e4e835ff2b8b) luyanan *2026-02-03 16:07:19*

**1. 增加前后端签名校验**


[57560a453d6e836](https://gitee.com/hexlo-dev/quickboot/commit/57560a453d6e836) luyanan *2026-01-31 16:07:38*

**feat(security): 增加敏感词过滤、防xss和SQL注入功能**

 * - 新增RequestParamFilterFilter实现请求参数安全过滤
 * - 集成SqlInjectUtils进行SQL注入检测和防护
 * - 集成HtmlUtil实现XSS攻击防护
 * - 集成SensitiveWordService提供敏感词过滤功能
 * - 添加JsonTraverseUtil支持JSON格式参数的安全处理
 * - 配置SecurityProperties用于安全管理配置
 * - 升级Spring Boot版本至3.2.5
 * - 集成Micrometer Tracing和OpenTelemetry链路追踪
 * - 添加Jasypt加密支持配置文件敏感信息保护
 * - 在R响应对象中增加traceId和timestamp字段
 * - 优化线程池配置以支持链路追踪上下文传递
 * - 添加SecuritySignRequestFilter实现请求签名验证
 * - 添加BodyReaderHttpServletRequestWrapper支持请求体重用读取
 * - 新增OAuth客户端管理模块提供客户端认证功能
 * - 修复SysMenuServiceImpl中菜单名称唯一性校验逻辑
 * - 修复文件上传大小计算单位错误问题
 * - 更新README文档完善待办事项列表

[c976de4305fdef6](https://gitee.com/hexlo-dev/quickboot/commit/c976de4305fdef6) luyanan *2026-01-23 15:43:58*

**feat(security): 增加敏感词过滤、防xss和SQL注入功能**

 * - 新增RequestParamFilterFilter实现请求参数安全过滤
 * - 集成SqlInjectUtils进行SQL注入检测和防护
 * - 集成HtmlUtil实现XSS攻击防护
 * - 集成SensitiveWordService提供敏感词过滤功能
 * - 添加JsonTraverseUtil支持JSON格式参数的安全处理
 * - 配置SecurityProperties用于安全管理配置
 * - 升级Spring Boot版本至3.2.5
 * - 集成Micrometer Tracing和OpenTelemetry链路追踪
 * - 添加Jasypt加密支持配置文件敏感信息保护
 * - 在R响应对象中增加traceId和timestamp字段
 * - 优化线程池配置以支持链路追踪上下文传递

[b5731d7869e6d8b](https://gitee.com/hexlo-dev/quickboot/commit/b5731d7869e6d8b) luyanan *2026-01-18 07:33:54*

**增加敏感词过滤、防xss 和SQL注入**


[67f949c44a1a71e](https://gitee.com/hexlo-dev/quickboot/commit/67f949c44a1a71e) luyanan *2026-01-15 15:26:30*

**chore(deps): 更新依赖项并添加Element Plus图标支持**

 * - 添加Element Plus图标模块及其相关依赖
 * - 集成Vue共享工具库以支持组件开发
 * - 生成新的依赖缓存文件以优化构建性能
 * - 添加完整的图标映射和元数据配置
 * - 配置开发环境支持Element Plus组件库
 * - 更新项目依赖管理以支持新功能模块

[5d418fbac4dc710](https://gitee.com/hexlo-dev/quickboot/commit/5d418fbac4dc710) luyanan *2026-01-14 14:31:34*

**!1 Update README.md**

 * Merge pull request !1 from gitee-agent/N/A

[1acdd832c992eee](https://gitee.com/hexlo-dev/quickboot/commit/1acdd832c992eee) luyanan *2026-01-12 13:05:47*

**Update README.md**


[b40ddd0d864bc73](https://gitee.com/hexlo-dev/quickboot/commit/b40ddd0d864bc73) gitee-bot *2026-01-12 13:04:31*

**docs: 删除项目优化分析和依赖优化文档**

 * - 移除了 MODULE_DEPENDENCY_OPTIMIZATION.md 文件
 * - 移除了 PROJECT_OPTIMIZATION_ANALYSIS.md 文件
 * - 清理了 .vscode/settings.json 中的 Java 编译配置

[aae5966834bf075](https://gitee.com/hexlo-dev/quickboot/commit/aae5966834bf075) luyanan *2026-01-12 13:03:05*

**feat(generator): 完善代码生成器功能并优化前端模板**

 * - 排除定时任务表和flyway表的生成
 * - 添加主键字段和类型的提取与处理
 * - 使用SysConfigUtils获取作者和包路径配置
 * - 自动处理主键、备注、创建更新字段的显示逻辑
 * - 生成API接口文件并支持搜索字段过滤
 * - 重构前端add-or-update组件使用新UI组件库
 * - 创建系统配置控制器和服务管理功能
 * - 优化菜单权限查询逻辑
 * - 添加国际化异常消息定义

[851387818c4b382](https://gitee.com/hexlo-dev/quickboot/commit/851387818c4b382) luyanan *2026-01-12 12:57:51*

**feat(data): 实现数据权限控制功能**

 * - 新增 DataPermission 注解用于标记需要数据权限控制的方法
 * - 实现 DataPermissionAspect 切面处理数据权限注解
 * - 创建 DataPermissionInterceptor 拦截器在 MyBatis 层面添加数据权限条件
 * - 添加 DataScopeType 枚举定义数据权限范围类型
 * - 扩展 LoginUser 类添加数据权限相关属性
 * - 实现 TableMatchUtil 工具类支持表名匹配功能
 * - 添加多种数据权限规则实现类如 AllRule、CustomRule 等
 * - 更新 BaseServiceImpl 支持分页查询的扩展功能
 * - 移除部门管理相关前端代码和 SQL 脚本

[b994332e59be5cf](https://gitee.com/hexlo-dev/quickboot/commit/b994332e59be5cf) luyanan *2026-01-07 14:18:45*

**feat(core): 引入IP工具类及Excel增强功能**

 * - 新增 IpUtils 工具类，支持 IPv4/IPv6 地址解析与内网判断
 * - 集成 ip2region 实现离线 IP 地址库查询
 * - 添加 Jackson 配置，Long 类型序列化为字符串防止精度丢失
 * - 优化 MyBatis 字段填充逻辑，通过 getUserId 替代 getUser 提升性能
 * - 调整 LoginUserUtils 用户信息获取方式，适配 JWT 存储结构
 * - 新增 CacheUtils 缓存操作工具类，封装 Spring Cache 常用方法
 * - 扩展 Excel 导出能力，支持 Cursor 流式导出与大数据分批处理
 * - 引入 ExcelDictConverter 字典转换器，支持注解驱动的字段映射
 * - 重构 ExcelUtils 工具类，增加模板导出与多 Sheet 支持
 * - 新增 ExcelUtils2 全新导出引擎，提供链式 API 和更灵活的数据源接入
 * - 增强 Excel 读取功能，内置校验机制并支持错误汇总报告
 * - 在 pom.xml 中引入 ip2region 依赖，移除注释掉的 sa-token-jwt 配置

[baf19d85fb4e626](https://gitee.com/hexlo-dev/quickboot/commit/baf19d85fb4e626) luyanan *2025-12-21 03:45:22*

**chore: 删除用户信息表相关代码**

 * - 删除了用户信息表的 Mapper、Service、Controller 及相关实体类和 DO 类
 * - 清除了用户信息表的 SQL 脚本
 * - 移除了前端用户信息表的列表和表单组件

[264ae900aa45460](https://gitee.com/hexlo-dev/quickboot/commit/264ae900aa45460) luyanan *2025-09-19 13:01:26*

**feat(file): 添加 MinIO 文件存储支持并优化文件上传功能**

 * - 新增 MinioFileTemplateStorage 类实现 MinIO 文件存储
 * -重构文件上传逻辑，添加文件校验功能
 * - 优化文件查看接口，增加路径安全检查
 * - 更新相关配置和依赖以支持 MinIO

[d8a185e94990f62](https://gitee.com/hexlo-dev/quickboot/commit/d8a185e94990f62) luyanan *2025-09-18 14:24:34*

**build(dependencies): 更新依赖版本并移除冗余代码**

 * - 更新 MyBatis-Plus、SA-Token、MySQL 等依赖版本- 移除各模块中的重复依赖声明
 * - 更新 validation-api为 jakarta.validation-api
 * -调整代码中的 javax 导包为 jakarta

[6c7fd7d82a2a863](https://gitee.com/hexlo-dev/quickboot/commit/6c7fd7d82a2a863) luyanan *2025-09-16 14:15:33*

**refactor(quick-boot-example): 删除缓存相关代码**

 * - 移除 CacheController、CacheService 类- 删除 QuickBootSecurityExampleApplication 中的缓存启用注解

[9578426da124681](https://gitee.com/hexlo-dev/quickboot/commit/9578426da124681) luyanan *2025-09-15 15:23:10*

**refactor(quick-boot-example): 删除示例模块代码**

 * - 移除了 LoginUser、TestController 和 UserDetailsServiceImpl 类
 * - 更新了 CHANGELOG.md 文件
 * - 注释掉了 pom.xml 中的代码格式和检查风格插件配置

[bf3b8f1c26f8deb](https://gitee.com/hexlo-dev/quickboot/commit/bf3b8f1c26f8deb) luyanan *2025-09-15 14:52:32*

**refactor(docs): 重构文档网站并整合 C7 组件**

 * - 导入 C7 组件库到 docs 项目
 * - 更新按钮、标题、卡片等组件的示例
 * - 移除重复的组件导入语句
 * - 优化部分组件的使用方式

[366a6a06637a35f](https://gitee.com/hexlo-dev/quickboot/commit/366a6a06637a35f) luyanan *2025-09-14 08:46:50*

**chore(docs): 清理 .vitepress/cache 目录- 删除了 @element-plus_icons-vue.js、@vue_shared.js、_metadata.json 等多个缓存文件**

 * - 移除了与项目无关的导入和导出语句
 * - 清理了无用的代码片段和映射文件

[6d8a1d15d132766](https://gitee.com/hexlo-dev/quickboot/commit/6d8a1d15d132766) luyanan *2025-09-13 09:29:22*

**chore(docs): 清理 .vitepress/cache 目录- 删除了 @element-plus_icons-vue.js、@vue_shared.js、_metadata.json 等多个缓存文件**

 * - 移除了与项目无关的导入和导出语句
 * - 清理了无用的代码片段和映射文件

[760bcc812406cac](https://gitee.com/hexlo-dev/quickboot/commit/760bcc812406cac) luyanan *2025-09-13 09:28:12*

**chore(docs): 清理 .vitepress/cache 目录- 删除了 @element-plus_icons-vue.js、@vue_shared.js、_metadata.json 等多个缓存文件**

 * - 移除了与项目无关的导入和导出语句
 * - 清理了无用的代码片段和映射文件

[24344613779122c](https://gitee.com/hexlo-dev/quickboot/commit/24344613779122c) luyanan *2025-08-08 14:54:14*

**docs(c7): 补充 json-form、json-table 组件文档- 完善 c7-json-form 组件的属性、插槽、事件说明**

 * - 添加 c7-json-form 组件的字段配置示例
 * - 完善 c7-json-table 组件的属性、插槽、事件说明
 * - 添加 c7-json-table 组件的列配置示例

[5151e0581886f9e](https://gitee.com/hexlo-dev/quickboot/commit/5151e0581886f9e) luyanan *2025-07-27 13:45:25*

**docs:优化文档配置和页面布局**

 * - 修复侧边栏中 c7-json-table 链接地址
 * - 移除部分赞助商信息
 * - 删除 Pinia 相关代码
 * -简化分页组件布局
 * - 更新文档推送脚本

[df0f89ed530440c](https://gitee.com/hexlo-dev/quickboot/commit/df0f89ed530440c) luyanan *2025-07-27 13:05:57*

**chore:**

 * - 移除所有组件和工具类文件
 * - 删除 package.json 和 tsconfig.json
 * -移除 vite 配置文件

[437ccc0e7d08a22](https://gitee.com/hexlo-dev/quickboot/commit/437ccc0e7d08a22) luyanan *2025-07-27 12:08:18*

**feat(bak): 新增 c7-json-table 组件并重构相关功能**

 * - 新增 c7-json-table 组件，用于展示 JSON 数据的表格
 * - 重构 c7-button 组件，增加更多属性和事件支持
 * - 新增 utils 目录，包含常用的工具函数
 * - 新增 hooks 目录，包含自定义 Hook 函数
 * - 更新 docs 目录，添加新的组件文档和示例
 * - 优化项目结构，统一命名规范

[9cd32a42d79e10a](https://gitee.com/hexlo-dev/quickboot/commit/9cd32a42d79e10a) luyanan *2025-07-26 14:29:43*

**feat(c7-plus): 新增 c7JsonTableColumn 组件**

 * - 添加 c7JsonTableColumn 组件，实现基于 JSON 配置的动态表格列
 * - 支持文本、标签、图片、插槽等多种列类型
 * - 集成 c7DictTag 和 c7Preview 组件
 * - 更新文档和示例

[27de82ad9a923d7](https://gitee.com/hexlo-dev/quickboot/commit/27de82ad9a923d7) luyanan *2025-07-24 14:21:04*

**feat(docs): 新增 JSON Form组件并优化 CRUD 示例**

 * - 新增 JSON Form 组件，支持动态表单生成和数据绑定
 * - 更新 CRUD 示例，使用 c7-dict-tag 组件替换原有的性别字段显示
 * - 在主题配置中添加 Element Plus 的中文语言包
 * - 调整表单验证规则和提示信息，提升用户体验

[3d925a44f77b14d](https://gitee.com/hexlo-dev/quickboot/commit/3d925a44f77b14d) luyanan *2025-07-23 15:09:36*

**feat(components): 新增 crud 和 dict-tag 组件**

 * - 在 c7-plus 组件库中添加了 c7-crud 和 c7-dict-tag 两个新组件
 * - 更新了文档侧边栏，增加了新组件的链接
 * - 创建了新组件的文档页面和示例演示
 * - 在项目中集成了新组件，并更新了相关类型定义

[4346ba184776935](https://gitee.com/hexlo-dev/quickboot/commit/4346ba184776935) luyanan *2025-07-20 14:10:39*

**feat(c7-cascader):重构级联组件并添加新功能**

 * - 重新设计了级联组件的属性和API，增加了更多自定义选项
 * - 添加了懒加载和非懒加载两种数据加载方式
 * - 实现了多种结果类型的返回值处理
 * - 优化了父级节点数据的获取逻辑
 * - 重构了组件内部的数据处理和绑定逻辑

[46ccecab16ee476](https://gitee.com/hexlo-dev/quickboot/commit/46ccecab16ee476) luyanan *2025-07-12 15:46:14*

**feat(c7Checkbox): 重构复选框组件并添加新功能- 重新设计了 c7Checkbox 组件的 API 和内部逻辑- 添加了多种复选框样式和使用场景的示例**

 * - 新增了全选/全不选、按钮样式、change 事件等功能
 * - 优化了数据加载和处理逻辑，支持异步数据获取
 * - 更新了文档，增加了更多示例和详细的属性说明

[d318d6c6b3c4ed4](https://gitee.com/hexlo-dev/quickboot/commit/d318d6c6b3c4ed4) luyanan *2025-07-02 14:33:14*

**docs(c7): 重构 DatePicker组件文档**

 * - 更新文档结构，增加基础用法、结果合并、默认值和数组结果等示例
 * - 添加组件 Attributes、Events 和 Slots 的详细说明
 * - 重新组织代码示例，使其更加清晰和模块化
 * - 优化组件实现，提高代码可读性和维护性

[78fc447f109a4af](https://gitee.com/hexlo-dev/quickboot/commit/78fc447f109a4af) luyanan *2025-07-01 15:51:01*

**feat(c7-button): 重构按钮组件并添加新功能**

 * -重构了 c7-button 组件的内部逻辑和结构
 * - 添加了多种按钮类型和样式
 * -增加了按钮点击事件处理和二次确认功能
 * -集成了表单验证功能
 * - 优化了文档和示例代码

[64b60ac7c2bf002](https://gitee.com/hexlo-dev/quickboot/commit/64b60ac7c2bf002) luyanan *2025-06-30 15:02:08*

**refactor(c7-select): 重构 c7Select 组件并优化数据获取方式**

 * - 新增 api.js 文件，统一处理数据请求
 * - 修改 demo2.vue，使用新的数据获取方法
 * - 重构 c7-select 组件，使用 utils 中的 jsonGet 函数
 * - 新增 utils/utils.js 文件，添加通用的 jsonGet 函数

[51f07205772d0a3](https://gitee.com/hexlo-dev/quickboot/commit/51f07205772d0a3) luyanan *2025-06-28 13:41:49*

**docs(c7-select): 更新文档并添加示例**

 * - 重构了 c7-select 组件的代码，优化了功能和性能
 * - 更新了文档，增加了更多示例和使用说明
 * - 添加了多个新的示例文件，展示了不同功能的使用方法
 * - 调整了组件属性和方法，提高了灵活性和可定制性

[0f69b610873b3cb](https://gitee.com/hexlo-dev/quickboot/commit/0f69b610873b3cb) luyanan *2025-06-28 09:21:18*

**feat(dict): 重构字典模块并添加新功能**

 * - 重新设计了字典存储和查询逻辑，提高了效率和灵活性
 * - 新增 C7Select 组件，用于异步加载字典数据
 * - 更新了相关文档和示例- 优化了代码结构，提高了可维护性

[7106cf20c57c367](https://gitee.com/hexlo-dev/quickboot/commit/7106cf20c57c367) luyanan *2025-06-25 14:31:56*

**style(system): 优化字典列表页面样式**

 * - 在字典列表页面，将字典类型链接样式改为蓝色可点击样式- 优化代码格式，调整导入语句和空格

[5f9e8cc45522590](https://gitee.com/hexlo-dev/quickboot/commit/5f9e8cc45522590) luyanan *2025-06-21 15:03:08*

**fix(c7-ui): 修改el-table表格重置的时候参数不重置的bug**

 * 修改el-table表格重置的时候参数不重置的bug

[00a362cb55266fc](https://gitee.com/hexlo-dev/quickboot/commit/00a362cb55266fc) luyanan *2025-06-21 14:49:49*

**fix(docs): 修改文档**


[27b4972691bdd6e](https://gitee.com/hexlo-dev/quickboot/commit/27b4972691bdd6e) luyanan *2025-06-02 12:57:31*

**fix(docs): 修改文档**


[7e2533168be8263](https://gitee.com/hexlo-dev/quickboot/commit/7e2533168be8263) luyanan *2025-05-29 14:32:32*

**fix(system): 表格分页**


[88d911f1d2389ef](https://gitee.com/hexlo-dev/quickboot/commit/88d911f1d2389ef) luyanan *2025-05-09 13:51:47*

**fix(manage): 修改管理系统的代码**


[51e1c0cdb30b3ae](https://gitee.com/hexlo-dev/quickboot/commit/51e1c0cdb30b3ae) luyanan *2025-05-08 15:08:08*

**fix(system): 定时任务模块修改**


[bceeb2af96df363](https://gitee.com/hexlo-dev/quickboot/commit/bceeb2af96df363) luyanan *2025-04-28 14:43:07*

**feat(system): 管理系统组件修改**


[f0ddf7d01a72600](https://gitee.com/hexlo-dev/quickboot/commit/f0ddf7d01a72600) luyanan *2025-04-27 14:42:55*

**fix(docs): 修改字典值**


[3c0e6677fd1f4c0](https://gitee.com/hexlo-dev/quickboot/commit/3c0e6677fd1f4c0) luyanan *2025-04-23 15:09:56*

**fix(docs): 修改字典值**


[ae13cca1e54643d](https://gitee.com/hexlo-dev/quickboot/commit/ae13cca1e54643d) luyanan *2025-04-20 14:47:08*

**fix(docs): table组件的demo**


[b01cac4a38f9d44](https://gitee.com/hexlo-dev/quickboot/commit/b01cac4a38f9d44) luyanan *2025-04-17 15:03:05*

**fix(table组件):**


[7a03f2585378c41](https://gitee.com/hexlo-dev/quickboot/commit/7a03f2585378c41) luyanan *2025-04-13 15:52:20*

**fix(docs): 表格组件**


[52ac3993142df82](https://gitee.com/hexlo-dev/quickboot/commit/52ac3993142df82) luyanan *2025-04-03 15:10:03*

**fix(docs): 修改**


[230a12b4b67aceb](https://gitee.com/hexlo-dev/quickboot/commit/230a12b4b67aceb) luyanan *2025-04-03 13:58:09*

**fix(docs): 组件迁移**


[ce705c1627261a2](https://gitee.com/hexlo-dev/quickboot/commit/ce705c1627261a2) luyanan *2025-03-27 15:34:19*

**fix(docs): 字典值封装**


[2699ff6b78e1dfd](https://gitee.com/hexlo-dev/quickboot/commit/2699ff6b78e1dfd) luyanan *2025-03-25 15:36:00*

**fix(docs): tableHook**


[6d75fb170f63a26](https://gitee.com/hexlo-dev/quickboot/commit/6d75fb170f63a26) luyanan *2025-03-19 15:03:33*

**feat(ui): 增加title组件**


[866c00967de9ae3](https://gitee.com/hexlo-dev/quickboot/commit/866c00967de9ae3) luyanan *2025-03-18 07:07:57*

**fix(docs): 增加搜索组件**


[9df8e67ba2a93ac](https://gitee.com/hexlo-dev/quickboot/commit/9df8e67ba2a93ac) luyanan *2025-03-15 14:16:13*

**fix(docs): 增加文件上传和文件预览组件**


[090496a34c8231a](https://gitee.com/hexlo-dev/quickboot/commit/090496a34c8231a) luyanan *2025-03-11 14:51:31*

**文档**


[ebeac02da584544](https://gitee.com/hexlo-dev/quickboot/commit/ebeac02da584544) luyanan *2025-03-09 16:07:00*

**fix(docs): 对级联的组件的封装**


[787aa22058d29a4](https://gitee.com/hexlo-dev/quickboot/commit/787aa22058d29a4) luyanan *2025-03-04 14:17:10*

**feat(docs): dictHook**

 * 1. 抽离字典Hook
 * 2. 修改select组件使用dictHook

[5a4c6cb26d1dce8](https://gitee.com/hexlo-dev/quickboot/commit/5a4c6cb26d1dce8) luyanan *2025-03-01 16:54:19*

**feat(ui): 增加title组件**


[140fba5aa55baf3](https://gitee.com/hexlo-dev/quickboot/commit/140fba5aa55baf3) luyanan *2025-02-28 10:26:06*

**feat(docs): 增加title和card组件的文档**


[18c851d206fa260](https://gitee.com/hexlo-dev/quickboot/commit/18c851d206fa260) luyanan *2025-02-25 15:41:02*

**fix(docs): 增加对ui组件封装的需求**


[d4f9c85ee1c393e](https://gitee.com/hexlo-dev/quickboot/commit/d4f9c85ee1c393e) luyanan *2025-02-23 12:52:52*

**feat(doc): 破解全局注入的迷咒**


[572f864546e408e](https://gitee.com/hexlo-dev/quickboot/commit/572f864546e408e) luyanan *2025-02-23 02:28:38*

**feat(doc): 破解全局注入的迷咒**


[b4064c978779f86](https://gitee.com/hexlo-dev/quickboot/commit/b4064c978779f86) luyanan *2025-02-22 03:19:24*

**组件移动到packages**


[ada1e045ab49b88](https://gitee.com/hexlo-dev/quickboot/commit/ada1e045ab49b88) luyanan *2025-02-21 07:59:22*

**解决文件路径注入的问题**


[56f54638e421b25](https://gitee.com/hexlo-dev/quickboot/commit/56f54638e421b25) luyanan *2025-02-21 06:26:02*

**文件注入**


[4596975a82ecf8b](https://gitee.com/hexlo-dev/quickboot/commit/4596975a82ecf8b) luyanan *2025-02-20 10:34:36*

**feat(doc): 文档处理**


[32a3407a8f02741](https://gitee.com/hexlo-dev/quickboot/commit/32a3407a8f02741) luyanan *2025-02-19 15:37:41*

**feat(字典值处理):**


[ac21b682c5c979c](https://gitee.com/hexlo-dev/quickboot/commit/ac21b682c5c979c) luyanan *2025-02-19 14:52:31*

**feat(vitepress):**


[af9f5c8aefefb9f](https://gitee.com/hexlo-dev/quickboot/commit/af9f5c8aefefb9f) luyanan *2025-02-17 15:45:27*

**feat(docs): 增加docs文档**

 * 增加docs文档

[567792a36f3202e](https://gitee.com/hexlo-dev/quickboot/commit/567792a36f3202e) luyanan *2025-02-13 15:35:29*

**feat(字典值处理):**


[0b2969fc49c75fa](https://gitee.com/hexlo-dev/quickboot/commit/0b2969fc49c75fa) luyanan *2025-02-12 14:22:55*

**fix(ui): 字典值加载的bug**


[8ecac1903739dad](https://gitee.com/hexlo-dev/quickboot/commit/8ecac1903739dad) luyanan *2025-02-11 15:43:44*

**fix(ui): 组件代码**

 * 修改字典值多次加载的问题

[2d9061f553bd072](https://gitee.com/hexlo-dev/quickboot/commit/2d9061f553bd072) luyanan *2025-02-10 15:31:27*

**组件移动到packages**


[911eadcb10f64ee](https://gitee.com/hexlo-dev/quickboot/commit/911eadcb10f64ee) luyanan *2025-02-08 10:08:27*

**fix(ui): 修改组件导入部分的代码**


[67c38f13948ce10](https://gitee.com/hexlo-dev/quickboot/commit/67c38f13948ce10) luyanan *2025-02-06 09:31:19*

**feat(ui): 增加组件**


[d55241207db03db](https://gitee.com/hexlo-dev/quickboot/commit/d55241207db03db) luyanan *2025-02-04 15:22:55*

**feat(ui): 增加组件**


[0045c575699e7e7](https://gitee.com/hexlo-dev/quickboot/commit/0045c575699e7e7) luyanan *2025-01-24 09:29:30*

**文档**


[6ab43e7086d827f](https://gitee.com/hexlo-dev/quickboot/commit/6ab43e7086d827f) luyanan *2025-01-23 14:21:21*

**feat(init): 基础功能开发**


[2ce901cec7ce711](https://gitee.com/hexlo-dev/quickboot/commit/2ce901cec7ce711) luyanan *2025-01-09 14:26:28*

**feat(init): 基础功能开发**


[435473e96153b07](https://gitee.com/hexlo-dev/quickboot/commit/435473e96153b07) luyanan *2025-01-09 13:42:37*


