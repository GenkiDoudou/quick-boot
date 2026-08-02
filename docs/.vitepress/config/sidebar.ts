export default {
    "/": [
        {
            text: "📚 文档入口",
            collapsed: false,
            items: [
                {text: "💡 项目介绍", link: "/docs/guide/introduction"},
                {text: "⚡ 快速上手", link: "/docs/guide/quick-start"},
                {text: "🔧 环境搭建", link: "/docs/guide/installation"},
            ]
        },
    ],

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
}
