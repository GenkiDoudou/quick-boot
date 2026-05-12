<template>

  <el-form :model="form">

    <el-row>

      <el-col :span="12">
        <el-form-item label="选择字典值">


          <el-input v-model="form.userId2"
                    placeholder="请输入关键字搜索"
                    clearable
          >
          </el-input>
        </el-form-item>
        <el-form-item label="选择字典值">

          <c7Select
              v-model="form.userId"
              :fetch-data="() => getDicts(form.userId2)"
              :fetch-params="{ }"
              :data-formatter="dataFormatter"
              placeholder="请输入关键字搜索"
              clearable
          >

          </c7Select>
          选中的数据: {{ form.userId }}
        </el-form-item>

      </el-col>

    </el-row>

  </el-form>

</template>

<script setup>
import {ref} from 'vue'
// 使用全局注册的 C7Select 组件
// import c7Select from '../C7Select.vue'
import {getDicts} from '@/api/system/dict/data'

const form = ref({
  userId: '',
  userId2: 'sys_user_sex'
})
const dictData = ref([
  {
    label: '男',
    value: '1'
  },
  {
    label: '女',
    value: '2'
  }
])

const dataFormatter = (list) => {
  console.log(list);
  return list;
}

</script>
