# 组件迁移指南

## 概述

本指南帮助你从旧的组件迁移到新的 TypeScript 版本。新组件完全兼容旧组件的 API，可以无缝替换。

## 迁移步骤

### 1. 更新导入路径

**旧版本**:
```typescript
import C7Card from '@/components/c7-card/src/index.vue'
```

**新版本**:
```typescript
import { C7Card } from '@quick-ui/c7-plus'
```

### 2. 组件使用方式不变

所有组件的使用方式保持不变，只需要更新导入路径即可。

## 组件对照表

| 旧组件路径 | 新组件导入 | 变化说明 |
|-----------|-----------|---------|
| `components/c7-card` | `C7Card` | 无变化 |
| `components/c7-cascader` | `C7Cascader` | 无变化 |
| `components/c7-checkbox` | `C7Checkbox` | 无变化 |
| `components/c7-crud` | `C7Crud` | 无变化 |
| `components/c7-date-picker` | `C7DatePicker` | 无变化 |
| `components/c7-dialog` | `C7Dialog` | 无变化 |
| `components/c7-dict-tag` | `C7DictTag` | 修复了值匹配 bug |
| `components/c7-json-table-column` | `C7JsonTableColumn` | 无变化 |
| `components/c7-layer` | `C7Layer` | 无变化 |
| `components/c7-preview` | `C7Preview` | 无变化 |
| `components/c7-radio` | `C7Radio` | 无变化 |
| `components/c7-switch-form` | `C7SwitchForm` | 无变化 |
| `components/c7-title` | `C7Title` | 无变化 |

## 迁移示例

### 示例 1: 单个组件迁移

**旧代码**:
```vue
<script setup>
import C7Card from '@/components/c7-card/src/index.vue'

const label = ref('卡片标题')
</script>

<template>
  <c7-card :label="label">
    <p>内容</p>
  </c7-card>
</template>
```

**新代码**:
```vue
<script setup lang="ts">
import { ref } from 'vue'
import { C7Card } from '@quick-ui/c7-plus'

const label = ref('卡片标题')
</script>

<template>
  <c7-card :label="label">
    <p>内容</p>
  </c7-card>
</template>
```

### 示例 2: 多个组件迁移

**旧代码**:
```vue
<script setup>
import C7Card from '@/components/c7-card/src/index.vue'
import C7Title from '@/components/c7-title/src/index.vue'
import C7Crud from '@/components/c7-crud/src/index.vue'
</script>
```

**新代码**:
```vue
<script setup lang="ts">
import { C7Card, C7Title, C7Crud } from '@quick-ui/c7-plus'
</script>
```

### 示例 3: CRUD 组件迁移

**旧代码**:
```vue
<script setup>
import C7Crud from '@/components/c7-crud/src/index.vue'
import C7JsonTableColumn from '@/components/c7-json-table-column/src/index.vue'

const total = ref(0)
const searchParams = ref({})

const getList = async (params) => {
  const res = await api.getUserList(params)
  total.value = res.total
  return res
}
</script>

<template>
  <c7-crud
    :listFunction="getList"
    :pageTotal="total"
    v-model:searchParam="searchParams"
  >
    <template #search>
      <el-form-item label="用户名">
        <el-input v-model="searchParams.username" />
      </el-form-item>
    </template>

    <c7-json-table-column :columns="columns" />
  </c7-crud>
</template>
```

**新代码**:
```vue
<script setup lang="ts">
import { ref } from 'vue'
import { C7Crud, C7JsonTableColumn } from '@quick-ui/c7-plus'
import type { TableColumnProps } from '@quick-ui/c7-plus'

const total = ref(0)
const searchParams = ref({})

const getList = async (params: any) => {
  const res = await api.getUserList(params)
  total.value = res.total
  return res
}

const columns: TableColumnProps[] = [
  // ... 列配置
]
</script>

<template>
  <c7-crud
    :listFunction="getList"
    :pageTotal="total"
    v-model:searchParam="searchParams"
  >
    <template #search>
      <el-form-item label="用户名">
        <el-input v-model="searchParams.username" />
      </el-form-item>
    </template>

    <c7-json-table-column :columns="columns" />
  </c7-crud>
</template>
```

## 类型支持

新版本提供完整的 TypeScript 类型支持：

```typescript
import type { 
  TableColumnProps,
  ColumnEnumType,
  FormColumn,
  IColumn
} from '@quick-ui/c7-plus'

// 使用类型定义
const columns: TableColumnProps[] = [
  {
    prop: 'name',
    label: '姓名',
    columnType: ColumnEnumType.TEXT
  }
]
```

## 注意事项

### 1. C7DictTag 值匹配改进

新版本修复了值匹配的 bug，现在会统一转换为字符串进行比较：

```typescript
// 旧版本可能匹配失败
<c7-dict-tag :modelValue="1" :options="options" />

// 新版本正确匹配
<c7-dict-tag :modelValue="1" :options="options" />
```

### 2. 错误处理改进

新版本添加了完善的错误处理，异步操作失败时会：
- 打印错误日志
- 重置数据状态
- 不会导致页面崩溃

### 3. TypeScript 支持

建议使用 TypeScript 以获得更好的类型提示和错误检查：

```vue
<script setup lang="ts">
// 启用 TypeScript
</script>
```

## 常见问题

### Q: 是否需要修改现有代码？
A: 只需要更新导入路径，其他代码无需修改。

### Q: 新版本是否向后兼容？
A: 是的，完全向后兼容，API 保持一致。

### Q: 如何处理类型错误？
A: 导入对应的类型定义：
```typescript
import type { TableColumnProps } from '@quick-ui/c7-plus'
```

### Q: 旧组件可以和新组件混用吗？
A: 可以，但建议统一迁移到新版本。

## 迁移检查清单

- [ ] 更新所有组件的导入路径
- [ ] 测试所有使用到的组件功能
- [ ] 检查类型错误（如果使用 TypeScript）
- [ ] 验证异步数据加载
- [ ] 测试表单提交和验证
- [ ] 检查事件处理
- [ ] 验证插槽功能

## 获取帮助

如果在迁移过程中遇到问题，请：

1. 查看组件文档：`docs/docs/c7/`
2. 查看实现文档：`IMPLEMENTATION.md`
3. 查看示例代码
4. 提交 Issue

## 总结

新版本组件提供了：
- ✅ 完整的 TypeScript 支持
- ✅ 更好的错误处理
- ✅ 详细的代码注释
- ✅ 修复的已知 bug
- ✅ 100% API 兼容

迁移过程简单快速，只需更新导入路径即可！

