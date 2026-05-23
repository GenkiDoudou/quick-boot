export default [
    {
        text: "📚 指南",
        link: "/docs/guide/introduction",
        activeMatch: "/docs/guide/",
    },
    {
        text: "⚙️ 后端手册",
        link: "/docs/backend/",
        activeMatch: "/docs/backend/",
    },
    {
        text: "🎨 前端手册",
        link: "/docs/frontend/",
        activeMatch: "/docs/frontend/",
    },
    {
        text: "🚀 部署",
        link: "/docs/deploy/requirements",
        activeMatch: "/docs/deploy/",
    },
    {
        text: "🏗️ 架构设计",
        link: "/docs/design/",
        activeMatch: "/docs/design/",
    },
    {
        text: "更多",
        items: [
            {
                text: "指南 · 入门",
                items: [
                    {text: "💡 项目介绍", link: "/docs/guide/introduction"},
                    {text: "📑 能力文档大纲", link: "/docs/guide/capabilities-outline"},
                    {text: "⚡ 快速上手", link: "/docs/guide/quick-start"},
                    {text: "🔧 环境搭建", link: "/docs/guide/installation"},
                ]
            },
            {
                text: "指南 · 其他",
                items: [
                    {text: "🔍 常见问题", link: "/docs/guide/faq"},
                    {text: "🤝 贡献指南", link: "/docs/guide/contributing"},
                ]
            },
            {
                text: "AI / SDD",
                items: [
                    {text: "AI 开发工作流", link: "/docs/skill/"},
                    {text: "SDD 目录说明", link: "/docs/sdd/"},
                    {text: "OpenSpec 指南", link: "/docs/sdd/openspec"},
                ]
            },
        ]
    },
    {
        text: "📝 更新日志", 
        link: "/updatelog/index"
    },
    {
        text: "🔗 生态", 
        items: [
            {
                text: "资源链接",
                items: [
                    {text: '💻 GitHub', link: 'https://github.com/rainsoil/quick-boot'},
                    {text: '🦊 Gitee', link: 'https://gitee.com/rainsoil/quickboot'},
                ]
            },
            {
                text: "相关技术",
                items: [
                    {text: '🎨 Element Plus', link: "https://element-plus.org/zh-CN/"},
                    {text: '⚡ Vite', link: "https://cn.vitejs.dev/"},
                    {text: '🍃 Spring Boot', link: "https://spring.io/projects/spring-boot"},
                ]
            },
        ]
    }
];
