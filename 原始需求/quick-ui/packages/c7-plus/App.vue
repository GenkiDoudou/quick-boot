<template>
  <div class="demo-container">
    <h1>C7 Plus 组件库示例</h1>
    
    <!-- 标题组件 -->
    <c7-title label="标题组件示例" labelColor="#409eff">
      <el-button type="primary">操作按钮</el-button>
    </c7-title>

    <!-- 卡片组件 -->
    <c7-card 
      label="卡片组件示例" 
      :isShowColorBlock="true"
      colorBlockColor="#67C23A"
    >
      <p>这是卡片内容区域</p>
    </c7-card>

    <!-- 表单组件 -->
    <c7-card label="表单组件示例">
      <el-form :model="form" label-width="100px">
        <el-form-item label="日期选择">
          <c7-date-picker v-model="form.date" type="date" />
        </el-form-item>

        <el-form-item label="单选">
          <c7-radio v-model="form.radio" :dataList="radioOptions" />
        </el-form-item>

        <el-form-item label="多选">
          <c7-checkbox v-model="form.checkbox" :dataList="checkboxOptions" />
        </el-form-item>

        <el-form-item label="级联选择">
          <c7-cascader v-model="form.cascader" :dataList="cascaderOptions" />
        </el-form-item>
      </el-form>
    </c7-card>

    <!-- 字典标签 -->
    <c7-card label="字典标签示例">
      <div style="margin-bottom: 10px;">
        <span>状态：</span>
        <c7-dict-tag :options="statusOptions" :modelValue="'1'" />
      </div>
      <div>
        <span>多个标签：</span>
        <c7-dict-tag :options="statusOptions" :modelValue="['1', '2']" />
      </div>
    </c7-card>

    <!-- 文件预览 -->
    <c7-card label="文件预览示例">
      <c7-preview 
        urls="https://via.placeholder.com/300,https://via.placeholder.com/400"
        displayType="image"
        coverType="None"
        width="150px"
        height="150px"
      />
    </c7-card>

    <!-- 对话框 -->
    <c7-card label="对话框示例">
      <el-button @click="dialogVisible = true">打开对话框</el-button>
      <c7-dialog 
        v-model:visible="dialogVisible"
        :modalProps="{ title: '对话框标题', width: '500px' }"
        @submit="handleSubmit"
      >
        <p>这是对话框内容</p>
      </c7-dialog>
    </c7-card>

    <!-- 表格 -->
    <c7-card label="CRUD 组件示例">
      <c7-crud
        :listFunction="getList"
        :pageTotal="total"
        v-model:searchParam="searchParams"
      >
        <template #search>
          <el-form-item label="关键词">
            <el-input v-model="searchParams.keyword" placeholder="请输入关键词" />
          </el-form-item>
        </template>

        <template #operate>
          <el-button type="primary">新增</el-button>
        </template>

        <c7-json-table-column :columns="columns" />
      </c7-crud>
    </c7-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { TableColumnProps } from './src/types/table'
import { ColumnEnumType } from './src/types/table'

// 表单数据
const form = reactive({
  date: '',
  radio: '',
  checkbox: [],
  cascader: []
})

// 单选选项
const radioOptions = [
  { label: '选项1', value: '1' },
  { label: '选项2', value: '2' },
  { label: '选项3', value: '3' }
]

// 多选选项
const checkboxOptions = [
  { label: '选项A', value: 'a' },
  { label: '选项B', value: 'b' },
  { label: '选项C', value: 'c' }
]

// 级联选项
const cascaderOptions = [
  {
    label: '浙江',
    value: 1,
    children: [
      { label: '杭州', value: 11 },
      { label: '宁波', value: 12 }
    ]
  },
  {
    label: '江苏',
    value: 2,
    children: [
      { label: '南京', value: 21 },
      { label: '苏州', value: 22 }
    ]
  }
]

// 状态选项
const statusOptions = [
  { label: '启用', value: '1', elTagType: 'success' },
  { label: '禁用', value: '2', elTagType: 'danger' },
  { label: '待审核', value: '3', elTagType: 'warning' }
]

// 对话框
const dialogVisible = ref(false)

const handleSubmit = () => {
  console.log('提交')
  dialogVisible.value = false
}

// CRUD
const total = ref(0)
const searchParams = ref({
  keyword: ''
})

const getList = async (params: any) => {
  console.log('查询参数:', params)
  // 模拟数据
  return {
    rows: [
      { id: 1, name: '张三', status: '1', age: 25 },
      { id: 2, name: '李四', status: '2', age: 30 },
      { id: 3, name: '王五', status: '3', age: 28 }
    ],
    total: 3
  }
}

const columns: TableColumnProps[] = [
  { 
    prop: 'name', 
    label: '姓名', 
    columnType: ColumnEnumType.TEXT 
  },
  { 
    prop: 'age', 
    label: '年龄', 
    columnType: ColumnEnumType.TEXT 
  },
  { 
    prop: 'status', 
    label: '状态', 
    columnType: ColumnEnumType.TAG,
    dictList: statusOptions
  }
]
</script>

<style scoped>
.demo-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.c7-card {
  margin-bottom: 20px;
}
</style>

