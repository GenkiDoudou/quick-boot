<template>
    <div class="app-container">
        <c7-json-table
                ref="tableRef"
                :listFunction="list${className}"
                :tableColumns="tableColumns"
                :delete-function="del${className}"
                :searchColumns="searchColumns"
                :tableProps="tableProps"
                rowsKey="data.records"
                totalKey="data.total"
        >
            <!-- 操作列 -->
            <template #table-operate="{ row }">
                <C7ButtonGroup>
                    <C7Button
                            type="primary"
                            link
                            icon="Edit"
                            @click="handleEdit(row)"
                            <#if tableEntity.verifyPermission == 'Y'>
                                v-hasPermi="['${moduleName!}:${className?lower_case}:edit']"
                            </#if>
                    >
                        编辑
                    </C7Button>

                    <C7Button
                            type="danger"
                            link
                            icon="Delete"
                            @click="tableRef.handleDelete(row.${keyField})"
                            <#if tableEntity.verifyPermission == 'Y'>
                                v-hasPermi="['${moduleName!}:${className?lower_case}:remove']"
                            </#if>
                    >
                        删除
                    </C7Button>
                </C7ButtonGroup>
            </template>
        </c7-json-table>

        <!-- 新增 / 修改 -->
        <add-or-update
                :key="addKey"
                ref="addOrUpdateRef"
                @refreshDataList="tableRef.refreshData()"
        />
    </div>
</template>

<script setup name="${className?lower_case}">
    import { ref, getCurrentInstance, nextTick } from 'vue'
    import { ElMessage, ElMessageBox } from 'element-plus'

    import AddOrUpdate from './add-or-update.vue'
    import {
        list${className},
        del${className},

    } from '@/api/${moduleName!}/${className?lower_case}'

    import { C7JsonTable, C7Button, C7ButtonGroup } from '@/components/c7'

    const { proxy } = getCurrentInstance()

    /* =====================================================
     *  dictType 自动收集（listFields + searchFields）
     * ===================================================== */
    <#assign dictTypes = []>
    <#list listFields as field>
    <#if field.dictType?? && field.dictType != ''>
    <#if !(dictTypes?seq_contains(field.dictType))>
    <#assign dictTypes = dictTypes + [field.dictType]>
    </#if>
    </#if>
    </#list>
    <#list searchFields as field>
    <#if field.dictType?? && field.dictType != ''>
    <#if !(dictTypes?seq_contains(field.dictType))>
    <#assign dictTypes = dictTypes + [field.dictType]>
    </#if>
    </#if>
    </#list>

    <#-- useDict 自动生成 -->
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

    /* =====================================================
     * refs
     * ===================================================== */
    const tableRef = ref()
    const addOrUpdateRef = ref()
    const addKey = ref(0)

    /* =====================================================
     * searchColumns
     * ===================================================== */
    const searchColumns = ref([
        <#list searchFields as field>
        {
            prop: "${field.javaField}",
            label: "${field.columnComment}",
            <#if field.dictType?? && field.dictType != ''>
            type: "select",
            dataList: ${field.dictType}
            <#else>
            type: "input"
            </#if>
        }<#if field_has_next>,</#if>
        </#list>
    ])

    /* =====================================================
     * tableColumns
     * ===================================================== */
    const tableColumns = ref([
        <#list listFields as field>
        {
            label: "${field.columnComment}",
            prop: "${field.javaField}",
            showOverflowTooltip: true
            <#if field.dictType?? && field.dictType != ''>,
            columnType: 'tag',
            dictList: ${field.dictType}
            </#if>
        },
        </#list>
        {
            label: "操作",
            prop: "table-operate",
            width: 160,
            fixed: "right"
        }
    ])

    /* =====================================================
     * tableProps
     * ===================================================== */
    const tableProps = ref({
        selection: true,
        border: true,
        stripe: true,
        height: 'auto',
        showRefresh: true,
        <#if tableEntity.verifyPermission == 'Y'>
        showAdd: proxy.checkPermission('${moduleName!}:${className?lower_case}:add'),
        showEdit: proxy.checkPermission('${moduleName!}:${className?lower_case}:edit'),
        showDelete: proxy.checkPermission('${moduleName!}:${className?lower_case}:remove'),
        showExport: proxy.checkPermission('${moduleName!}:${className?lower_case}:export')
        </#if>
    })

    /* =====================================================
     * handlers
     * ===================================================== */
    const handleAdd = () => {
        addKey.value++
        nextTick(() => {
            addOrUpdateRef.value.init()
        })
    }

    const handleEdit = (row) => {
        addKey.value++
        nextTick(() => {
            addOrUpdateRef.value.init(row.${keyField})
        })
    }

</script>
