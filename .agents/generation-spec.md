# generation-spec.md — 代码生成骨架

> 脚手架生成功能时按已探测技术栈选用下列骨架，并按真实目录调整路径。

## Vue SFC
```vue
<script setup lang="ts">
// defineProps / defineEmits / composables
</script>
<template>
  <!-- 模板 -->
</template>
```

## Java Controller / Service / Mapper
- Controller：REST + 校验 + OpenAPI 注解
- Service：事务与业务规则
- Mapper/Repository：仅持久化
- Entity/DTO/VO：映射清晰；是否语义字段避免 boolean