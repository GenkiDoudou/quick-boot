<template>

  <C7Dialog :visible="visibleRef" mode="dialog" title="查看" width="80%" :footer="false"
             @close="handleClose">


    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px" :disabled="true">


      <el-row>
        <el-col :span="12">
          <el-form-item label="方法名称:" prop="method">
            {{ dataForm.method }}

          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="操作人员:" prop="operName">
            {{ dataForm.operName }}
          </el-form-item>
        </el-col>
      </el-row>




      <el-row>
        <el-col :span="8">
          <el-form-item label="请求URL:" prop="operUrl">
            {{ dataForm.requestMethod }} {{ dataForm.operUrl }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="主机地址" prop="operIp">
            {{ dataForm.operIp }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="操作地点" prop="operLocation">
            {{ dataForm.operLocation }}
          </el-form-item>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="20">
          <el-form-item label="请求参数" prop="operParam">
            <div class="json-content">
              <pre v-if="isValidJson(dataForm.operParam)" v-html="highlightJson(dataForm.operParam)"></pre>
              <span v-else>{{ dataForm.operParam || '无' }}</span>
            </div>
          </el-form-item>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="20">
          <el-form-item label="返回内容" prop="jsonResult">
            <div class="json-content">
              <pre v-if="isValidJson(dataForm.jsonResult)" v-html="highlightJson(dataForm.jsonResult)"></pre>
              <span v-else>{{ dataForm.jsonResult || '无' }}</span>
            </div>
          </el-form-item>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="8">
          <el-form-item label="操作状态码" prop="status">
            <span :style="dataForm.status!=200  ? 'color: red':''">
              {{ dataForm.status }}
            </span>

          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="操作时间" prop="operTime">
            {{ dataForm.operTime }}

          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="消耗时间" prop="costTime">
            {{ dataForm.costTime }}

          </el-form-item>
        </el-col>
      </el-row>


    </el-form>
  </C7Dialog>
</template>

<script setup>
import {C7Dialog} from "@/components/c7"
import {reactive, ref, getCurrentInstance} from "vue";
import { getOperlog } from '@/api/system/operlog.js';

const {proxy} = getCurrentInstance();
const emit = defineEmits(["refreshDataList"]);
const visibleRef = ref(false);
const dataForm = ref({
  operId: "",
  title: "",
  businessType: "",
  method: "",
  requestMethod: "",
  operName: "",
  operUrl: "",
  operIp: "",
  operLocation: "",
  operParam: "",
  jsonResult: "",
  status: "",
  operTime: "",
  costTime: ""
})
const handleClose = () => {
  visibleRef.value = false;
};
//  校验
const rules = ref(
    {
      // xxx: [{required: true, message: '请输入xxx', trigger: 'blur'}]


    }
);

// 初始化方法
const init = (id) => {
  visibleRef.value = true;
  if (id) {
    dataForm.value.operId = id;
    getInfo(id);
  }
}
// 根据id查询详情
const getInfo = (id) => {
  console.log('获取操作日志详情，ID:', id);
  getOperlog(id).then(res => {
    console.log('操作日志详情响应:', res);
    dataForm.value = res.data;
  }).catch(error => {
    console.error('获取操作日志详情失败:', error);
    proxy.$message.error('获取详情失败');
  });
}

// 检查是否为有效的JSON字符串
const isValidJson = (str) => {
  if (!str || typeof str !== 'string') return false;
  try {
    JSON.parse(str);
    return true;
  } catch (e) {
    return false;
  }
}

// 格式化JSON字符串
const formatJson = (str) => {
  if (!str) return '';
  try {
    const obj = JSON.parse(str);
    return JSON.stringify(obj, null, 2);
  } catch (e) {
    return str;
  }
}

// 高亮JSON语法
const highlightJson = (jsonStr) => {
  if (!jsonStr) return '';
  try {
    const obj = JSON.parse(jsonStr);
    const formatted = JSON.stringify(obj, null, 2);
    return formatted
      .replace(/(".*?")\s*:/g, '<span class="json-key">$1</span>:')
      .replace(/:\s*(".*?")/g, ': <span class="json-string">$1</span>')
      .replace(/:\s*(\d+)/g, ': <span class="json-number">$1</span>')
      .replace(/:\s*(true|false)/g, ': <span class="json-boolean">$1</span>')
      .replace(/:\s*(null)/g, ': <span class="json-null">$1</span>');
  } catch (e) {
    return jsonStr;
  }
}
const dataFormRef = ref()


defineExpose({
  init
})
</script>

<style scoped>
.json-content {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  background-color: #f8f9fa;
}

.json-content pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.4;
  color: #2c3e50;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.json-content span {
  color: #606266;
  font-style: italic;
}

/* JSON语法高亮样式 */
:deep(.json-key) {
  color: #0969da;
  font-weight: 600;
}

:deep(.json-string) {
  color: #0a3069;
}

:deep(.json-number) {
  color: #cf222e;
  font-weight: 500;
}

:deep(.json-boolean) {
  color: #8250df;
  font-weight: 500;
}

:deep(.json-null) {
  color: #6e7781;
  font-style: italic;
}
</style>
