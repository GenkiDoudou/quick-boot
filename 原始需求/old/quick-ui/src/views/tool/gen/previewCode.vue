<!-- 预览代码-->


<template>
  <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick" type="card">
    <el-tab-pane
        v-for="(value, key) in dataList"
        :label="value.name"
        :name="value.name"
        :key="value.name"
    >

      <el-card>
        <el-link :underline="false" icon="DocumentCopy" v-copyText="value.content" v-copyText:callback="copyTextSuccess"
                 style="float:right">&nbsp;复制
        </el-link>
        <pre>{{ value.content }}</pre>
      </el-card>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>

import baseService from "@/service/baseService.js";
import {getCurrentInstance} from "vue";

const activeName = ref('');
const dataList = ref([]);

const {proxy} = getCurrentInstance();

const init = (tableId) => {
  dataList.value = [];
  baseService.get("/generator/gentable/preview/" + tableId).then(res => {
    dataList.value = res.data;
    activeName.value = dataList.value[0].name;

  })
}

const  copyTextSuccess = (va)=>{
  proxy.$modal.msgSuccess("复制成功");
}
defineExpose({
  init
})
</script>
