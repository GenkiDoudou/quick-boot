<!-- 表格编辑-->
<template>


  <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick" type="card">
    <el-tab-pane label="基础信息" name="BasicInfo"></el-tab-pane>
    <el-tab-pane label="字段信息" name="ColumnInfo"></el-tab-pane>
  </el-tabs>

  <el-card>
    <component :is="currentComponent" ref="currentComponentRef"/>
  </el-card>
</template>


<script setup>
import BasicInfo from "@/views/tool/gen/basicInfo.vue";
import ColumnInfo from "@/views/tool/gen/columnInfo.vue";


const tableIdRef = ref()

const activeName = ref("basicInfo");

const currentComponent = ref(BasicInfo);

const init = (id) => {

  tableIdRef.value = id;
  activeName.value = "BasicInfo"
  nextTick(() => {
    currentComponentRef.value.init(id);
  })
}
const currentComponentRef = ref();
const handleClick = (tab) => {
  console.log(tab)
  switch (tab.props.name) {
    case 'BasicInfo':
      currentComponent.value = BasicInfo;
      nextTick(() => {
        currentComponentRef.value.init(tableIdRef.value);
      })
      break;
    case 'ColumnInfo':
      currentComponent.value = ColumnInfo;
      nextTick(() => {
        currentComponentRef.value.init(tableIdRef.value);
      })
      break;

    default:
      break;
  }
}
defineExpose({
  init
})
</script>
