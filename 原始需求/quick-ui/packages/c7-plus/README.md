# C7 Plus Component Library

基于 Vue 3 + TypeScript + Element Plus 的业务组件库。

## 安装

```bash
npm install @quick-ui/c7-plus
```

## 使用

### 完整引入

```typescript
import { createApp } from 'vue'
import C7Plus from '@quick-ui/c7-plus'
import App from './App.vue'

const app = createApp(App)
app.use(C7Plus)
app.mount('#app')
```

### 按需引入

```vue
<script setup lang="ts">
import { C7DatePicker, C7Radio, C7Checkbox } from '@quick-ui/c7-plus'
</script>

<template>
  <c7-date-picker v-model="date" />
  <c7-radio v-model="radio" :dataList="options" />
  <c7-checkbox v-model="checkbox" :dataList="options" />
</template>
```

## 组件列表

- C7Card - 卡片组件
- C7Cascader - 级联选择器
- C7Checkbox - 多选组件
- C7Crud - CRUD 组件
- C7DatePicker - 日期选择器
- C7Dialog - 对话框组件
- C7DictTag - 字典标签组件
- C7JsonTableColumn - 动态表格列组件
- C7Layer - 层级容器组件
- C7Preview - 文件预览组件
- C7Radio - 单选组件
- C7SwitchForm - 表单切换组件
- C7Title - 标题组件

## 开发

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建
npm run build
```

## 许可证

MIT

