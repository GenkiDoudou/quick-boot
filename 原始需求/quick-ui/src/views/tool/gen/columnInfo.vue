<template>
  <div class="app-container">

    <el-table ref="dragTable" :data="dataList" row-key="id">
      <el-table-column
          label="字段列名"
          prop="columnName"
          min-width="15%"
          :show-overflow-tooltip="true"
      />
      <el-table-column label="字段描述" min-width="20%">
        <template #default="scope">
          <el-input v-model="scope.row.columnComment"></el-input>
        </template>
      </el-table-column>

      <el-table-column label="sql类型" min-width="13%" prop="columnType">
        <!--        <template #default="scope">-->
        <!--          <el-input v-model="scope.row.columnType"></el-input>-->
        <!--        </template>-->
      </el-table-column>

      <el-table-column label="Java类型" min-width="13%">
        <template #default="scope">
          <el-input v-model="scope.row.javaType"></el-input>
        </template>
      </el-table-column>

      <el-table-column label="java字段名" min-width="20%">
        <template #default="scope">
          <el-input v-model="scope.row.javaField"></el-input>
        </template>
      </el-table-column>

      <el-table-column label="必填" min-width="8%">
        <template #default="scope">
          <el-checkbox true-label="1" false-label="0" v-model="scope.row.isRequired"></el-checkbox>
        </template>
      </el-table-column>
      <!--      <el-table-column label="插入" min-width="8%">-->
      <!--        <template #default="scope">-->

      <!--          <el-checkbox true-label="1" false-label="0" v-model="scope.row.isInsert"></el-checkbox>-->
      <!--          &lt;!&ndash;          <el-input v-model="scope.row.columnComment"></el-input>&ndash;&gt;-->
      <!--        </template>-->
      <!--      </el-table-column>-->


      <el-table-column label="编辑" min-width="8%">
        <template #default="scope">
          <el-checkbox true-label="1" false-label="0" v-model="scope.row.isEdit"></el-checkbox>
        </template>
      </el-table-column>

      <el-table-column label="列表" min-width="8%">
        <template #default="scope">
          <el-checkbox true-label="1" false-label="0" v-model="scope.row.isList"></el-checkbox>

        </template>
      </el-table-column>

      <el-table-column label="查询" min-width="8%">
        <template #default="scope">
          <el-checkbox true-label="1" false-label="0" v-model="scope.row.isQuery"></el-checkbox>

        </template>
      </el-table-column>

      <el-table-column label="查询方式" min-width="13%">
        <template #default="scope">
        <c7-select :data-list="queryTypeDictData" v-model="scope.row.queryTyp"></c7-select>
        </template>
      </el-table-column>


      <el-table-column label="显示类型" min-width="13%">
        <template #default="scope">
          <c7-select :data-list="GEN_HTML_TYPE" v-model="scope.row.htmlType"></c7-select>
          <!--          <el-input v-model="scope.row.htmlType"></el-input>-->
        </template>
      </el-table-column>


      <el-table-column label="字典类型" min-width="13%">
        <template #default="scope">
          <c7-select :data-list="dictTypes" v-model="scope.row.dictType"></c7-select>
        </template>
      </el-table-column>
    </el-table>
    <el-form label-width="100px">
      <div style="text-align: center;margin-left:-100px;margin-top:10px;">
        <el-button type="primary" @click="submitForm()">提交</el-button>

      </div>
    </el-form>
  </div>
</template>


<script setup name="gentablecolumn">
import {reactive, ref, toRefs, getCurrentInstance} from "vue";
import {C7Select} from "@c7-plus"
import baseService from "@/service/baseService.js";

const {proxy} = getCurrentInstance();
const dataList = ref();
const {GEN_HTML_TYPE} = proxy.useDict("GEN_HTML_TYPE");

const queryTypeDictData = ref([
  {
    label: "等于",
    value: "EQ"
  },
  {
    label: "大于",
    value: "GT"
  },
  {
    label: "大于等于",
    value: "GTE"
  },
  {
    label: "小于",
    value: "LT"
  },
  {
    label: "小于等于",
    value: "LTE"
  },
  {
    label: "LIKE ",
    value: "LIKE"
  },
  {
    label: "BETWEEN ",
    value: "BETWEEN"
  }

])

const dictTypes = ref([]);
// 初始化
const init = (tableId) => {

  baseService.get("/generator/gentablecolumn/list?page=1&limit=1000&tableId=" + tableId).then(res => {
    dataList.value = res.data.records;
  });

  baseService.get("/system/dict/types").then(res => {
    dictTypes.value = res.data;
    dictTypes.value.forEach(a => {
      a.label = a.label + "(" + a.value + ")";
    })
  })
}


defineExpose({
  init
})
const submitForm = () => {
  console.log(dataList)
  baseService.post("/generator/gentablecolumn/updateBatch", dataList.value).then(res => {
    proxy.$modal.msgSuccess("操作成功");
  });
}
</script>
