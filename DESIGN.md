---
name: vue.ruoyi.vip Design System
version: 1.0.0
last_updated: 2026-05-10
author: https://getwebdesign.top
---

# Design Thinking
Before coding, understand the context and define a bold aesthetic direction:
- **Purpose**: What problem does this interface solve? Who is using it?
- **Style**: Choose an extreme style: minimalism, maximalism, retro-futurism, organic/natural, luxury/artisanal, playful/toy-like, magazine/editorial, raw/rough, art deco/geometric, soft pastel, industrial/practical, etc. There are many styles to choose from. You can draw inspiration from them, but ensure your design truly aligns with the selected aesthetic direction.
- **Constraints**: Technical requirements (frameworks, performance, accessibility).
- **Differentiation**: What makes it memorable? What one thing will people remember?
**Key**: Choose a clear conceptual direction and execute it precisely. Both bold maximalism and refined minimalism are viable — the key is intention, not intensity.
## Aesthetic Default Settings
- Start with composition, not components.
- Prioritize full-bleed hero images or full-canvas visual anchors.
- Make the brand or product name the most prominent text.
- Keep copy concise for quick scanning.
- Use white space, alignment, scaling, cropping, and contrast before adding borders.
- Default constraint system: no more than two fonts and one accent color.

# 整体氛围

## Design System (设计规范)

### 1. 核心风格
现代简约，以功能性和清晰度为核心，采用扁平化设计语言，强调信息层级和可读性。

### 2. 配色方案
主色调为深蓝色（`#0a2463`）和浅灰色（`#f5f7fa`），辅以蓝色高亮（`#409eff`）和绿色状态标识（`#67c23a`），整体配色冷静、专业。

### 3. 字体栈
主要使用无衬线字体 `PingFang SC` 或 `Helvetica Neue`，字号基于 14px 基础单位，确保易读性和一致性。

### 4. 质感
界面干净，无多余装饰，按钮和卡片带有轻微阴影，突出交互元素，整体质感轻盈且专业。

### 5. 动态交互 (Motion Design)
微动效用于按钮悬停和点击反馈，表格行有淡入效果，整体交互流畅自然。

## 组件规范
### Button / Card / Navigation / Input / Typography
- **Button**: 圆角设计，主按钮为蓝色背景，辅助按钮为浅灰色背景带蓝色文字。
- **Card**: 浅灰色背景，内边距适中，无明显边框。
- **Navigation**: 左侧固定导航栏，支持折叠，当前选中项有蓝色高亮。
- **Input**: 输入框带有细边框，聚焦时边框颜色变为蓝色。
- **Typography**: 标题使用粗体，正文使用常规体，字重和字号层级分明。

## 特殊元素 Few-shot 复刻样例

### 1. 元素名称
**部门管理表格**

**用途：**
展示部门列表信息，支持排序、筛选和操作。

**识别依据：**
页面核心内容区域，包含部门名称、排序、状态、创建时间及操作列。

**视觉规则：**
- 布局：两栏布局，左侧为导航栏，右侧为主内容区。
- 色彩：表头背景为浅灰色 (`#f5f7fa`)，表格行背景交替为白色和浅灰色，状态列为蓝色背景的圆角标签。
- 字体：标题和表头使用 14px 粗体，表格内容使用 14px 常规体。
- 间距：行高 40px，列间距 20px。
- 圆角/边框/阴影：表格单元格有细边框，状态标签为圆角矩形。
- 动效：鼠标悬停时行背景变为浅蓝色 (`#ecf5ff`)，点击操作按钮时有轻微缩放反馈。

**复刻提示词：**
> 创建一个响应式表格组件，包含部门名称、排序、状态（蓝色圆角标签）、创建时间和操作列。表头背景为浅灰色，表格行背景交替为白色和浅灰色，状态列为蓝色背景的圆角标签。支持鼠标悬停高亮和操作按钮点击反馈。

**结构草图：**
```html
<section class="department-table">
  <div class="table-header">
    <input placeholder="部门名称" />
    <select>
      <option>状态</option>
      <option>正常</option>
      <option>停用</option>
    </select>
    <button>搜索</button>
    <button>重置</button>
  </div>
  <table>
    <thead>
      <tr>
        <th>部门名称</th>
        <th>排序</th>
        <th>状态</th>
        <th>创建时间</th>
        <th>操作</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>若依科技</td>
        <td>0</td>
        <td><span class="status-tag">正常</span></td>
        <td>2026-01-18 10:58:14</td>
        <td>
          <button>修改</button>
          <button>新增</button>
          <button>删除</button>
        </td>
      </tr>
      <!-- 更多行 -->
    </tbody>
  </table>
</section>
```

---

### 2. 元素名称
**左侧导航栏**

**用途：**
提供系统功能入口，支持展开/折叠和当前选中项高亮。

**识别依据：**
页面左侧固定区域，包含多个功能模块，当前选中项有蓝色背景高亮。

**视觉规则：**
- 布局：垂直排列，支持折叠，当前选中项宽度略宽。
- 色彩：背景为深蓝色 (`#0a2463`)，文字为白色，选中项背景为浅蓝色 (`#1e5fff`)。
- 字体：文字大小为 14px，加粗显示当前选中项。
- 间距：每个菜单项上下间距 10px，图标与文字间距 10px。
- 圆角/边框/阴影：无明显边框，选中项有轻微阴影。

**复刻提示词：**
> 创建一个左侧导航栏组件，包含多个功能模块，支持展开/折叠。当前选中项背景为浅蓝色，文字为白色，图标与文字间距 10px，菜单项上下间距 10px。

**结构草图：**
```html
<aside class="sidebar">
  <div class="logo">若依管理系统</div>
  <nav>
    <ul>
      <li class="active">
        <i class="icon"></i>
        <span>部门管理</span>
      </li>
      <li>
        <i class="icon"></i>
        <span>用户管理</span>
      </li>
      <!-- 更多菜单项 -->
    </ul>
  </nav>
</aside>
```

---

### 3. 元素名称
**状态标签**

**用途：**
标识数据的状态，如“正常”、“停用”等。

**识别依据：**
表格中的状态列，使用蓝色背景的圆角标签显示状态。

**视觉规则：**
- 布局：独立的圆角矩形，内部居中显示状态文字。
- 色彩：背景为蓝色 (`#409eff`)，文字为白色。
- 字体：12px 粗体。
- 间距：内边距 4px 10px，圆角半径 4px。
- 动效：无明显动效，但作为状态标识需清晰易辨。

**复刻提示词：**
> 创建一个状态标签组件，背景为蓝色 (`#409eff`)，文字为白色，字体大小 12px，加粗显示。标签为圆角矩形，内边距 4px 10px，圆角半径 4px。

**结构草图：**
```html
<span class="status-tag">正常</span>
```

--- 

以上分析基于提供的 DOM 和截图，确保所有特殊元素均有真实证据支持，并提供了详细的复刻规范。

## 工程 CSS 证据

由实时 DOM computed styles 压缩生成。这里只保留高频 token 与设计意图，不输出原始 CSS 清单。

### 压缩设计 Token

**Mode:** mixed

#### 色彩角色
- **color.text.primary:** #606266 (88)
- **color.text.secondary:** #606266 (21)
- **color.surface.base:** #f5f7fa (20)
- **color.accent:** #606266 (41)
- **color.border.default:** #606266 (71)
- **color.focus.ring:** #606266 (88)

#### 字体角色
- **font.family.primary:** Helvetica Neue (122)
- **font.family.secondary:** Arial (48)
- **font.size.display:** 14px (1)
- **font.size.body:** 12px (20)
- **font.size.label:** 12px (66)
- **ratio:** display is 1.17x body

#### 间距节奏
- **base unit:** 5px
- **space.1:** 10px (212)
- **space.2:** 7px (112)
- **space.3:** 15px (39)
- **space.4:** 50px (20)
- **space.5:** 40px (16)

#### 圆角角色
- **radius.sharp:** 3px (34)

#### 阴影意图
- **level:** none
- **usage:** 0
- **note:** flat surfaces dominate

#### 动效意图
- **level:** moderate
- **range:** 100-300ms
- **common durations:** 250ms (50), 100ms (36), 300ms (15)
- **easing style:** cubic-bezier(0.645, 0.045, 0.355, 1) (13), ease, ease, ease (11), ease, ease (4)

### 差异化实现信号

- Flat surfaces are preferred over decorative depth.
- Motion is moderate, centered around 100-300ms.
- Type hierarchy is ratio-driven: display is 1.17x body.

### 采集诊断

- 从 1147 个 DOM 元素中采样了 171 个可见元素。
- 置信度: high.
- No extraction warnings.

## Core Principles
1. Firmly Refuse (Negative Constraints):
- Prohibit using purple/blue gradients on white backgrounds
- Prohibit using common fonts (Inter, Roboto, Arial, system-ui)
- Prohibit using predictable hero-CTA-feature-review templates
- Prohibit using common geometric shapes or abstract spots
- Prohibit using visual effects that look like stock images or clichés
## Performance Optimization
- Ensure pages load quickly, avoid unnecessary large resources
- Use modern image formats (WebP) and appropriate compression
- Implement lazy loading techniques for long page content
**Important Note**: The implementation complexity should match the aesthetic vision. Maximalist designs require complex code as well as a lot of animations and effects. Minimalist or refined designs require restraint, precision, and attention to spacing, typography, and subtle details. Elegance comes from a perfect interpretation of the vision.