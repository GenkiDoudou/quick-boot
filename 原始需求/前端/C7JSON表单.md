# C7JSON表单（C7JsonForm）原始需求

## 背景
- 多页面存在大量结构相似的表单（输入/选择/日期/上传/树选择等），希望通过 JSON 配置快速生成，并支持联动显示、动态选项、字段禁用与自定义 slot。

## 目标
- 基于 `ElForm` 提供“配置驱动”的表单渲染器：
  - 支持常见字段类型
  - 支持字段排序、栅格布局、校验 rules
  - 支持 visible/disabled 动态函数
  - 支持字段联动（linkage）与动态选项（optionsWhen）

## 功能需求
### 1. 数据模型
- 外部 `v-model:modelValue` 绑定表单对象。
- 内部维护 `formData`（reactive），双向同步且防止循环更新。
- 初始化时对 columns 中所有 `prop` 赋默认值：`modelValue[prop] ?? defaultValue ?? null`。

### 2. 字段类型（type）
- `input`（默认）/`input-number`
- `select`（C7Select）
- `date*`（C7DatePicker，支持 date/daterange/datetime 等）
- `time`（C7TimePicker）
- `checkbox`（C7Checkbox）
- `radio`（C7Radio）
- `upload`（C7Upload）
- `tree-select`（C7TreeSelect）
- `cascader`（C7Cascader）
- `slot`（完全自定义渲染）

### 3. 联动与动态能力
- `visibleWhen(formData)`：控制字段显隐。
- `disabledWhen(formData)`：控制字段禁用。
- `optionsWhen(formData)`：动态返回 options 覆盖 dataList/options。
- `linkage(value, formData)`：字段变化时触发联动逻辑。
- 字段变化统一触发 `field-change(prop,value,formDataSnapshot)`。

### 4. 排序与布局
- `order` 升序排序，未设置排最后。
- 支持 `span` 栅格占比（默认 24）。
- `gutter` 控制列间距。

### 5. label 增强
- 支持 tooltip（问号提示）。
- 支持自定义 label slot：`#label-[prop]`。

## 插槽
- 字段 slot：`col.type='slot'` 时使用 `#<prop>`
- `actions`：表单底部操作区

## 事件
- `update:modelValue`
- `validate(isValid, {prop,message})`
- `field-change(prop,value,formData)`

## 对外能力（Expose）
- `validate/resetFields/clearValidate/scrollToField`
- `formData`

## 验收标准
- 通过 columns 配置即可生成可用表单；联动显隐与动态选项生效。
- 外部修改 `modelValue` 能同步到内部表单；内部修改也能 emit 回外部。

