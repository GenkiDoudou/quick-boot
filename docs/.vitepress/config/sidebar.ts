export default {
    // 首页：左侧也显示文档入口（否则首页侧栏为空）
    "/": [
        {
            text: "📚 文档入口",
            collapsed: false,
            items: [
                {text: "💡 项目介绍", link: "/docs/guide/introduction"},
                {text: "📑 能力文档大纲", link: "/docs/guide/capabilities-outline"},
                {text: "⚡ 快速上手", link: "/docs/guide/quick-start"},
                {text: "⚙️ 后端概述", link: "/docs/backend/"},
                {text: "🎨 前端概述", link: "/docs/frontend/"},
                {text: "🔐 OAuth2 集成", link: "/docs/backend/modules/oauth2"},
                {text: "📤 导入导出中心", link: "/docs/backend/modules/import-export-center"},
                {text: "🔗 全链路监控", link: "/docs/backend/modules/trace-chain"},
            ]
        },
    ],

    // 指南
    "/docs/guide/": [
        {
            text: '🚀 新手入门',
            collapsed: false,
            items: [
                {text: '💡 项目介绍', link: '/docs/guide/introduction'},
                {text: '📑 能力文档大纲', link: '/docs/guide/capabilities-outline'},
                {text: '⚡ 快速上手', link: '/docs/guide/quick-start'},
                {text: '🔧 环境搭建', link: '/docs/guide/installation'},
            ]
        },
        {
            text: '📖 其他',
            collapsed: false,
            items: [
                {text: '🔍 常见问题', link: '/docs/guide/faq'},
                {text: '🤝 贡献指南', link: '/docs/guide/contributing'},
            ]
        },
    ],

    // AI 开发工作流
    "/docs/skill/": [
        {
            text: '📋 总览',
            collapsed: false,
            items: [
                {text: 'AI 开发工作流', link: '/docs/skill/'},
                {text: 'Quick-Boot 与 Cursor 工作流', link: '/docs/skill/quickboot-workflow'},
            ]
        },
        {
            text: '🧩 Cursor Skills / Commands',
            collapsed: false,
            items: [
                {text: '仓库 .cursor 文件全量对照', link: '/docs/skill/cursor-repo-files'},
                {text: 'Cursor Skills 详解', link: '/docs/skill/cursor-skills'},
                {text: 'Cursor Commands 详解', link: '/docs/skill/cursor-commands'},
            ]
        },
        {
            text: '📐 OpenSpec',
            collapsed: false,
            items: [
                {text: 'OpenSpec 详细指南（SDD）', link: '/docs/sdd/openspec'},
                {text: 'OpenSpec 操作指南（速查）', link: '/docs/skill/openspec-operation'},
            ]
        },
    ],

    // 规格驱动开发（SDD）
    '/docs/sdd/': [
        {
            text: '📐 SDD / Spec-Kit',
            collapsed: false,
            items: [
                {text: 'SDD 目录说明', link: '/docs/sdd/'},
                {text: 'Spec-Kit 安装与初始化', link: '/docs/sdd/spec-kit-install-init'},
                {text: 'Spec-Kit 新功能与需求变更', link: '/docs/sdd/spec-kit-change-workflow'},
                {text: 'Spec-Kit 命令速查', link: '/docs/sdd/spec-kit-command-cheatsheet'},
            ]
        },
    ],

    // 架构设计
    '/docs/design/': [
        {
            text: '🏗️ 系统设计',
            collapsed: false,
            items: [
                {text: '📋 设计概述', link: '/docs/design/index'},
                {text: '🏗️ 系统架构', link: '/docs/design/architecture'},
                {text: '💾 数据库设计', link: '/docs/design/database'},
            ]
        },
        {
            text: '🔐 安全设计',
            collapsed: false,
            items: [
                {text: '🔑 认证授权', link: '/docs/design/auth'},
                {text: '🛡️ 安全防护', link: '/docs/design/security'},
                {text: '👥 客户端管理', link: '/docs/design/client-management'},
            ]
        },
        {
            text: '📦 模块设计',
            collapsed: false,
            items: [
                {text: '📦 功能模块', link: '/docs/design/modules'},
                {text: '⚙️ 后端组件', link: '/docs/design/backend-components'},
                {text: '🎯 前端组件', link: '/docs/design/frontend-components'},
            ]
        },
        {
            text: '🔧 后端通用组件设计',
            collapsed: false,
            items: [
                {text: '🛡️ 安全防护模块综合设计', link: '/docs/design/后端通用组件设计/安全防护模块综合设计方案'},
                {text: '👥 客户端管理模块设计', link: '/docs/design/后端通用组件设计/客户端管理模块设计'},
                {text: '🚫 请求方式和域名拦截模块设计', link: '/docs/design/后端通用组件设计/请求方式和域名拦截模块设计'},
                {text: '🌐 请求来源拦截模块设计', link: '/docs/design/后端通用组件设计/请求来源拦截模块设计'},
                {text: '⚠️ 异常处理模块设计', link: '/docs/design/后端通用组件设计/异常处理模块设计'},
                {text: '🌍 国际化模块设计', link: '/docs/design/后端通用组件设计/国际化模块设计'},
                {text: '🎯 核心模块设计', link: '/docs/design/后端通用组件设计/核心模块设计'},
                {text: '🌍 跨域配置模块设计', link: '/docs/design/后端通用组件设计/跨域配置模块设计'},
                {text: '📝 日志记录模块设计', link: '/docs/design/后端通用组件设计/日志记录模块设计'},
                {text: '🛡️ SQL注入拦截模块设计', link: '/docs/design/后端通用组件设计/SQL注入拦截模块设计'},
                {text: '🔐 密码编码器模块设计', link: '/docs/design/后端通用组件设计/密码编码器模块设计'},
                {text: '🛡️ 安全头管理模块设计', link: '/docs/design/后端通用组件设计/安全头管理模块设计'},
                {text: '🛡️ XSS脚本注入拦截模块设计', link: '/docs/design/后端通用组件设计/XSS脚本注入拦截模块设计'},
                {text: '🔐 安全登录模块设计', link: '/docs/design/后端通用组件设计/安全登录模块设计'},
                {text: '🔑 登录模块设计', link: '/docs/design/后端通用组件设计/登录模块设计'},
                {text: '  └ P0 核心能力', link: '/docs/design/后端通用组件设计/登录模块设计-P0-核心能力'},
                {text: '  └ P1 会话控制', link: '/docs/design/后端通用组件设计/登录模块设计-P1-会话控制'},
                {text: '  └ P2 扩展能力', link: '/docs/design/后端通用组件设计/登录模块设计-P2-扩展能力'},
                {text: '  └ P3 可选扩展', link: '/docs/design/后端通用组件设计/登录模块设计-P3-可选扩展'},
                {text: '📁 文件上传模块设计', link: '/docs/design/后端通用组件设计/文件上传模块设计'},
            ]
        },
    ],

    // 后端手册
    '/docs/backend/': [
        {
            text: '📚 开发指南',
            collapsed: false,
            items: [
                {text: '📖 后端概述', link: '/docs/backend/index'},
                {text: '📝 开发规范', link: '/docs/backend/development-guide'},
                {text: '🎯 项目结构', link: '/docs/backend/structure'},
            ]
        },
        {
            text: '📦 功能模块',
            collapsed: false,
            items: [
                {text: '📦 模块总览', link: '/docs/backend/modules/index'},
                {text: '🔐 OAuth2 集成', link: '/docs/backend/modules/oauth2'},
                {text: '👤 用户管理', link: '/docs/backend/modules/user-management'},
                {text: '🔐 权限管理', link: '/docs/backend/modules/permission-management'},
                {text: '🏢 部门/字典/参数/公告', link: '/docs/backend/modules/system-management'},
                {text: '📊 监控审计', link: '/docs/backend/modules/monitor-audit'},
                {text: '🐢 慢 SQL 日志', link: '/docs/backend/modules/slow-sql'},
                {text: '🔗 全链路监控', link: '/docs/backend/modules/trace-chain'},
                {text: '📁 文件管理', link: '/docs/backend/modules/file-management'},
                {text: '📤 导入导出中心', link: '/docs/backend/modules/import-export-center'},
                {text: '⏰ 定时任务', link: '/docs/backend/modules/job-scheduler'},
                {text: '🛠️ 代码生成', link: '/docs/backend/modules/codegen'},
                {text: '🛡️ 安全防护', link: '/docs/backend/modules/security-module'},
                {text: '📱 客户端管理', link: '/docs/backend/modules/client-management'},
            ]
        },
        {
            text: '📡 公共接口',
            collapsed: false,
            items: [
                {text: '📋 接口规范', link: '/docs/backend/api/index'},
                {text: '👥 用户接口', link: '/docs/backend/api/user'},
                {text: '🔒 权限接口', link: '/docs/backend/api/permission'},
                {text: '⚙️ 系统接口', link: '/docs/backend/api/system'},
            ]
        },
        {
            text: '🔧 工具类库',
            collapsed: false,
            items: [
                {text: '🛠️ 工具类总览', link: '/docs/backend/utils/index'},
                {text: '💾 Redis 工具', link: '/docs/backend/utils/redis'},
                {text: '🔑 JWT 工具', link: '/docs/backend/utils/jwt'},
                {text: '🔐 加密工具', link: '/docs/backend/utils/crypto'},
                {text: '📅 日期工具', link: '/docs/backend/utils/date'},
                {text: '📝 字符串工具', link: '/docs/backend/utils/string'},
            ]
        },
        {
            text: '🧩 组件库',
            collapsed: false,
            items: [
                {text: '📚 组件总览', link: '/docs/backend/components/index'},
            ]
        },
        {
            text: '🔧 通用组件',
            collapsed: false,
            items: [
                {text: '🎯 核心模块', link: '/docs/backend/components/通用组件/核心模块使用文档'},
                {text: '🔐 字段脱敏模块', link: '/docs/backend/components/通用组件/字段脱敏模块使用文档'},
                {text: '👥 客户端管理模块', link: '/docs/backend/components/通用组件/客户端管理模块使用文档'},
                {text: '🚫 敏感词过滤模块', link: '/docs/backend/components/通用组件/敏感词过滤模块使用文档'},
                {text: '🌐 请求方式和域名拦截模块', link: '/docs/backend/components/通用组件/请求方式和域名拦截模块使用文档'},
                {text: '🔒 请求来源拦截模块', link: '/docs/backend/components/通用组件/请求来源拦截模块使用文档'},
                {text: '🛡️ 防幂等模块', link: '/docs/backend/components/通用组件/防幂等模块使用文档'},
                {text: '⚠️ 异常处理模块', link: '/docs/backend/components/通用组件/异常处理模块使用文档'},
                {text: '🌍 国际化模块', link: '/docs/backend/components/通用组件/国际化模块使用文档'},
                {text: '🌍 跨域配置模块', link: '/docs/backend/components/通用组件/跨域配置模块使用文档'},
                {text: '📝 日志记录模块', link: '/docs/backend/components/通用组件/日志记录模块使用文档'},
                {text: '🛡️ SQL注入拦截模块', link: '/docs/backend/components/通用组件/SQL注入拦截模块使用文档'},
                {text: '🔐 密码编码器模块', link: '/docs/backend/components/通用组件/密码编码器模块使用文档'},
                {text: '🛡️ 安全头管理模块', link: '/docs/backend/components/通用组件/安全头管理模块使用文档'},
                {text: '🛡️ XSS脚本注入拦截模块', link: '/docs/backend/components/通用组件/XSS脚本注入拦截模块使用文档'},
                {text: '🔐 安全登录模块', link: '/docs/backend/components/通用组件/安全登录模块使用文档'},
                {text: '📁 文件上传模块', link: '/docs/backend/components/通用组件/文件上传模块使用文档'},
            ]
        },
    ],

    // 前端手册
    '/docs/frontend/': [
        {
            text: '📚 开发指南',
            collapsed: false,
            items: [
                {text: '📖 前端概述', link: '/docs/frontend/index'},
                {text: '📝 开发规范', link: '/docs/frontend/development-guide'},
                {text: '🎯 项目结构', link: '/docs/frontend/structure'},
                {text: '📄 业务页面总览', link: '/docs/frontend/modules/index'},
                {text: '📋 列表页模板', link: '/docs/frontend/list-page-template'},
                {text: '📡 用户行为监控', link: '/docs/frontend/modules/user-behavior-monitor'},
            ]
        },
        {
            text: '⚙️ 核心功能',
            collapsed: false,
            items: [
                {text: '🚦 路由配置', link: '/docs/frontend/router'},
                {text: '💾 状态管理', link: '/docs/frontend/store'},
                {text: '🛠️ 工具函数', link: '/docs/frontend/utils'},
                {text: '🎨 样式管理', link: '/docs/frontend/styles'},
                {text: '🌐 国际化', link: '/docs/frontend/i18n'},
            ]
        },
        {
            text: '🧩 组件库',
            collapsed: false,
            items: [
                {text: '🔘 C7Button 按钮', link: '/docs/frontend/components/通用组件/c7-button'},
                {text: '🔘 C7ButtonGroup 按钮组', link: '/docs/frontend/components/通用组件/c7-button-group'},
                {text: '🃏 C7Card 卡片', link: '/docs/frontend/components/通用组件/c7-card'},
                {text: '🌲 C7Cascader 级联选择器', link: '/docs/frontend/components/通用组件/c7-cascader'},
                {text: '☑️ C7Checkbox 复选框', link: '/docs/frontend/components/通用组件/c7-checkbox'},
                {text: '📋 C7Copy 复制', link: '/docs/frontend/components/通用组件/c7-copy'},
                {text: '📅 C7DatePicker 日期选择器', link: '/docs/frontend/components/通用组件/c7-date-picker'},
                {text: '📋 C7Descriptions 描述列表', link: '/docs/frontend/components/通用组件/c7-descriptions'},
                {text: '🪟 C7Dialog 弹窗/抽屉', link: '/docs/frontend/components/通用组件/c7-dialog'},
                {text: '🏷️ C7DictTag 字典标签', link: '/docs/frontend/components/通用组件/c7-dict-tag'},
                {text: '💬 C7MessageBox 对话框', link: '/docs/frontend/components/通用组件/c7-message-box'},
                {text: '⬇️ C7ExcelDownload Excel下载', link: '/docs/frontend/components/通用组件/c7-excel-download'},
                {text: '⬆️ C7ExcelUpload Excel导入', link: '/docs/frontend/components/通用组件/c7-excel-upload'},
                {text: '📋 C7JsonForm JSON动态表单', link: '/docs/frontend/components/通用组件/c7-json-form'},
                {text: '📊 C7JsonTable JSON动态表格', link: '/docs/frontend/components/通用组件/c7-json-table'},
                {text: '📄 C7Pagination 分页', link: '/docs/frontend/components/通用组件/c7-pagination'},
                {text: '🔀 C7Switch 开关', link: '/docs/frontend/components/通用组件/c7-switch'},
                {text: '🕐 C7TimePicker 时间选择器', link: '/docs/frontend/components/通用组件/c7-time-picker'},
                {text: '📌 C7Title 标题', link: '/docs/frontend/components/通用组件/c7-title'},
                {text: '🌲 C7TreeSelect 树形选择器', link: '/docs/frontend/components/通用组件/c7-tree-select'},
                {text: '📤 C7Upload 文件上传', link: '/docs/frontend/components/通用组件/c7-upload'},
                {text: '💧 C7Watermark 水印', link: '/docs/frontend/components/通用组件/c7-watermark'},
                {text: '📊 C7JsonTableColumn 表格列渲染', link: '/docs/frontend/components/通用组件/c7-json-table-column'},
            ]
        },
    ],



    // 部署指南
    '/docs/deploy/': [
        {
            text: '📋 部署准备',
            collapsed: false,
            items: [
                {text: '📋 环境要求', link: '/docs/deploy/requirements'},
                {text: '🔧 配置说明', link: '/docs/deploy/configuration'},
            ]
        },
        {
            text: '💻 本地部署',
            collapsed: false,
            items: [
                {text: '⚙️ 后端部署', link: '/docs/deploy/local-backend'},
                {text: '🎯 前端部署', link: '/docs/deploy/local-frontend'},
                {text: '🔗 联调测试', link: '/docs/deploy/local-testing'},
            ]
        },
        {
            text: '🐳 Docker 部署',
            collapsed: false,
            items: [
                {text: '📦 镜像构建', link: '/docs/deploy/docker-build'},
                {text: '🚀 Docker Compose', link: '/docs/deploy/docker-compose'},
                {text: '🔧 容器管理', link: '/docs/deploy/docker-management'},
            ]
        },
        {
            text: '🌐 生产部署',
            collapsed: false,
            items: [
                {text: '🌐 Nginx 配置', link: '/docs/deploy/nginx'},
                {text: '🔒 SSL 证书', link: '/docs/deploy/ssl'},
                {text: '⚡ 性能优化', link: '/docs/deploy/optimization'},
                {text: '📊 监控告警', link: '/docs/deploy/monitoring'},
            ]
        },
    ],
};
