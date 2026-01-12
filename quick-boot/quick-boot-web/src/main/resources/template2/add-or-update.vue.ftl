<template>
    <c7-dialog
            :visible="visibleRef"
            mode="dialog"
            :title="(!dataForm.${keyField}) ? '新增' : '修改'"
            @submit="submit"
            @close="handleClose"
    >
        <el-form
                :model="dataForm"
                :rules="rules"
                ref="dataFormRef"
                label-width="100px"
        >
            <el-row :gutter="20">

                <#list addOrUpdateFields as field>
                    <el-col :span="12">
                        <el-form-item
                                label="${field.columnComment}"
                                prop="${field.javaField}"
                        >

                            <#-- ===== 字典字段 ===== -->
                            <#if field.dictType?? && field.dictType != ''>
                                <#if field.htmlType == 'radio'>
                                    <c7-radio
                                            v-model="dataForm.${field.javaField}"
                                            :data-list="${field.dictType}"
                                    />
                                <#else>
                                    <c7-select
                                            v-model="dataForm.${field.javaField}"
                                            :data-list="${field.dictType}"
                                    />
                                </#if>

                            <#-- ===== 日期 ===== -->
                            <#elseif field.htmlType == 'datetime'>
                                <el-date-picker
                                        v-model="dataForm.${field.javaField}"
                                        type="date"
                                        placeholder="请选择${field.columnComment}"
                                />

                            <#-- ===== 文本域 ===== -->
                            <#elseif field.htmlType == 'textarea'>
                                <el-input
                                        v-model="dataForm.${field.javaField}"
                                        type="textarea"
                                        placeholder="请输入${field.columnComment}"
                                />

                            <#-- ===== 默认输入框 ===== -->
                            <#else>
                                <el-input
                                        v-model="dataForm.${field.javaField}"
                                        placeholder="请输入${field.columnComment}"
                                />
                            </#if>

                        </el-form-item>
                    </el-col>
                </#list>

            </el-row>
        </el-form>
    </c7-dialog>
</template>
<script setup>
    import { ref, getCurrentInstance } from "vue"
    import { C7Dialog, C7Select, C7Radio } from "@/components/c7"

    /**
     * ================= API 引入（关键点）
     * 与你给的 dict 示例完全一致
     */
    import {
        get${className},
        add${className},
        update${className}
    } from "@/api/${moduleName!}/${className?lower_case}.js"

    const { proxy } = getCurrentInstance()
    const emit = defineEmits(["refreshDataList"])

    <#-- ================= 字典自动收集 ================= -->

    <#assign dictTypes = []>
    <#list addOrUpdateFields as field>
    <#if field.dictType?? && field.dictType != ''>
    <#if !(dictTypes?seq_contains(field.dictType))>
    <#assign dictTypes = dictTypes + [field.dictType]>
    </#if>
    </#if>
    </#list>

    <#if dictTypes?size gt 0>
    const {
        <#list dictTypes as dict>
        ${dict}<#if dict_has_next>,</#if>
        </#list>
    } = proxy.useDict(
        <#list dictTypes as dict>
        "${dict}"<#if dict_has_next>,</#if>
        </#list>
    )
    </#if>

    <#-- ================= 弹窗 & 表单 ================= -->

    const visibleRef = ref(false)
    const dataFormRef = ref()

    const dataForm = ref({
        ${keyField}: "",
        <#list addOrUpdateFields as field>
        ${field.javaField}: "",
        </#list>
    })

    <#-- ================= 校验规则 ================= -->

    const rules = ref({
        <#list addOrUpdateFields as field>
        <#if field.isRequired == '1'>
        ${field.javaField}: [
            { required: true, message: '请输入${field.columnComment}', trigger: 'blur' }
        ],
        </#if>
        </#list>
    })

    <#-- ================= 弹窗控制 ================= -->

    const handleClose = () => {
        visibleRef.value = false
    }

    const init = (${keyField}) => {
        // 防止新增 / 编辑串数据
        if (dataFormRef.value) {
            dataFormRef.value.resetFields()
        }

        visibleRef.value = true
        dataForm.value.${keyField} = ${keyField} || ""

        if (${keyField}) {
            getInfo(${keyField})
        }
    }

    <#-- ================= 查询详情 ================= -->

    const getInfo = (${keyField}) => {
        get${className}(${keyField}).then(res => {
            dataForm.value = res.data
        })
    }

    <#-- ================= 提交 ================= -->

    const submit = () => {
        dataFormRef.value.validate(valid => {
            if (!valid) return

            if (dataForm.value.${keyField}) {
                // 修改
                update${className}(dataForm.value).then(() => {
                    proxy.$modal.msgSuccess("修改成功")
                    visibleRef.value = false
                    emit("refreshDataList")
                })
            } else {
                // 新增
                add${className}(dataForm.value).then(() => {
                    proxy.$modal.msgSuccess("新增成功")
                    visibleRef.value = false
                    emit("refreshDataList")
                })
            }
        })
    }

    defineExpose({ init })
</script>
