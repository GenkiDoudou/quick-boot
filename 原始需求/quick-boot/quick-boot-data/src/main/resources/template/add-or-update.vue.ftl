<template>

    <q-modal :visible="visibleRef" mode="dialog" :title="(!dataForm.id)?'新增':'修改'"
             @close="handleClose" @submit="submit">


        <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">

            <#list tableFields as field>
                <#if field.isPk != '0' && !(field.attrName == 'createBy'  || field.attrName == 'delFlag'  ||field.attrName == 'updateTime' || field.attrName == 'updateBy')>
                    <el-row>
                        <el-col :span="20">
                            <el-form-item label="${field.columnComment}" prop="${field.attrName}">
                                <el-input v-model="dataForm.${field.attrName}"
                                          placeholder="请输入${field.columnComment}"/>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </#if>
            </#list>
        </el-form>
    </q-modal>
</template>

<script setup>
    import qModal from '@/components/qModal/index.vue'
    import {reactive, ref} from "vue";
    import baseService from "@/service/baseService.ts";


    const {proxy} = getCurrentInstance();
    const emit = defineEmits(["refreshDataList"]);
    const visibleRef = ref(false);
    const dataForm = ref({
        <#list tableFields as field>
        <#if field.isPk != '0' && !(field.attrName == 'createBy'  || field.attrName == 'delFlag'  ||field.attrName == 'updateTime' || field.attrName == 'updateBy')>
        ${field.attrName}: "",
        </#if>
        </#list>


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
    const init = (dictType, id) => {
        visibleRef.value = true;
        if (id) {
            dataForm.value.id = id;
            getInfo(id);
        }

    }
    // 根据id查询详情
    const getInfo = (id) => {
        baseService.get("/${moduleName}/${className ?lower_case}/" + id).then(res => {
            dataForm.value = res.data;
        })
    }
    const dataFormRef = ref()
    // 提交
    const submit = () => {
        dataFormRef.value.validate(valid => {
            if (valid) {
                console.log(dataForm.value)
                if (dataForm.value.id != undefined) {
                    // 修改
                    baseService.put("/${moduleName}/${className ?lower_case}", dataForm.value).then(res => {
                        proxy.$modal.msgSuccess("修改成功");
                        visibleRef.value = false;
                        emit("refreshDataList");
                    })
                } else {
                    //保存
                    baseService.post("/${moduleName}/${className ?lower_case}", dataForm.value).then(res => {
                        proxy.$modal.msgSuccess("新增成功");
                        visibleRef.value = false;
                        emit("refreshDataList");
                    })
                }
            }
        });
    }

    defineExpose({
        init
    })
</script>