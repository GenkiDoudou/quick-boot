---
layout: home
hero:
  name: QuickBoot
  text: 企业级全栈开发框架
  tagline: 让开发变得简单而优雅，专注业务逻辑，告别重复劳动
  image:
    src: logo.svg
    alt: quickboot
  actions:
    - theme: brand
      text: 🚀 快速开始
      link: /docs/guide/quick-start
    - theme: alt
      text: 📚 阅读文档
      link: /docs/guide/introduction
    - theme: alt
      text: 💻 GitHub
      link: https://github.com/rainsoil/quick-boot

features:
  - icon: 
      src: /icons/rocket.svg
      width: 48
      height: 48
    title: 开箱即用
    details: 内置完整的前后端解决方案，集成主流技术栈，零配置快速启动项目，让你专注于业务开发
    link: /docs/guide/quick-start
    linkText: 立即体验
  
  - icon: 
      src: /icons/component.svg
      width: 48
      height: 48
    title: 管理端 quick-ui
    details: Vue 3 + Element Plus，动态路由与 C7 JSON 表格/表单，对接后端权限与 OAuth 客户端
    link: /docs/frontend/
    linkText: 管理端文档
  
  - icon: 
      src: /icons/security.svg
      width: 48
      height: 48
    title: 后端 quickboot
    details: Spring Boot 多模块、统一响应与防火墙、OAuth2 / 系统管理 / 监控等企业能力
    link: /docs/backend/
    linkText: 后端文档
  
  - icon: 
      src: /icons/performance.svg
      width: 48
      height: 48
    title: 移动端 quick-h5
    details: uni-app（H5 / 微信小程序）对接同一套后端登录与菜单能力
    link: /docs/h5/
    linkText: 移动端文档
  
  - icon: 
      src: /icons/code.svg
      width: 48
      height: 48
    title: 开发约定
    details: 前后端分层、命名与协作规范见仓库 code_formater.md / AGENTS.md
    link: /docs/backend/conventions
    linkText: 后端约定
  
  - icon: 
      src: /icons/tools.svg
      width: 48
      height: 48
    title: 环境与联调
    details: JDK、Node、pnpm 与本地三端启动步骤
    link: /docs/guide/installation
    linkText: 环境搭建
---

<style>
:root {
  --vp-home-hero-name-color: transparent;
  --vp-home-hero-name-background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  --vp-home-hero-image-background-image: linear-gradient(135deg, #667eea33 0%, #764ba255 50%, #f093fb33 100%);
  --vp-home-hero-image-filter: blur(68px);
}

@media (min-width: 640px) {
  :root {
    --vp-home-hero-image-filter: blur(88px);
  }
}

.VPFeature {
  transition: all 0.3s ease;
}

.VPFeature:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.VPFeature .icon {
  transition: transform 0.3s ease;
}

.VPFeature:hover .icon {
  transform: scale(1.1);
}

/* 自定义特性卡片样式 */
.VPFeatures {
  padding-top: 24px !important;
}

.VPFeature .title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.VPFeature .details {
  font-size: 14px;
  line-height: 1.6;
  color: var(--vp-c-text-2);
}

/* Hero 区域优化 */
.VPHero .name {
  font-size: 64px !important;
  font-weight: 800 !important;
  letter-spacing: -0.02em;
}

.VPHero .text {
  font-size: 32px !important;
  font-weight: 600 !important;
  background: linear-gradient(120deg, var(--vp-c-brand-1), var(--vp-c-brand-2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.VPHero .tagline {
  font-size: 18px !important;
  line-height: 1.6 !important;
  max-width: 600px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 按钮组优化 */
.VPHero .actions {
  gap: 16px;
  padding-top: 32px;
}

.VPButton {
  padding: 12px 32px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}

.VPButton.brand {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
}

.VPButton.brand:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.4) !important;
}

.VPButton.alt:hover {
  transform: translateY(-2px);
  border-color: var(--vp-c-brand-1) !important;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .VPHero .name {
    font-size: 48px !important;
  }
  
  .VPHero .text {
    font-size: 24px !important;
  }
  
  .VPHero .tagline {
    font-size: 16px !important;
  }
}
</style>

<!-- 添加统计信息区域 -->
<div class="stats-container">
  <div class="stat-item">
    <div class="stat-number">50+</div>
    <div class="stat-label">前端组件</div>
  </div>
  <div class="stat-item">
    <div class="stat-number">30+</div>
    <div class="stat-label">后端组件</div>
  </div>
  <div class="stat-item">
    <div class="stat-number">100%</div>
    <div class="stat-label">开源免费</div>
  </div>
  <div class="stat-item">
    <div class="stat-number">10x</div>
    <div class="stat-label">效率提升</div>
  </div>
</div>

<style>
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 32px;
  max-width: 1200px;
  margin: 64px auto;
  padding: 48px 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.stat-item {
  text-align: center;
  padding: 24px;
  transition: transform 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-4px);
}

.stat-number {
  font-size: 48px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 16px;
  color: var(--vp-c-text-2);
  font-weight: 500;
}

@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
    margin: 48px auto;
    padding: 32px 16px;
  }
  
  .stat-number {
    font-size: 36px;
  }
  
  .stat-label {
    font-size: 14px;
  }
}
</style>
