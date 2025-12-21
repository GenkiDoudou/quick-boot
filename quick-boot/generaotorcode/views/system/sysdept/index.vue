<template>
    <div class="app-container">
        <qtable-search :columns="searchColumns" :modelValue="state.dataForm" @handle-reset="state.handleReset"
                       @handle-search="state.getDataList"
                       @add-btn-handle="addOrUpdateHandle()"
                       @deleteHandle="state.deleteHandle"
                                    add-btn-perms="system:sysdept:add"
                    del-btn-perms="system:sysdept:remove"

        ></qtable-search>
        <qtable v-loading="state.dataListLoading" :tableData="state.dataList" :columns="jsonColumns"
                :page="state.page" :limit="state.limit" :total="state.total"
                @pageSizeChangeHandle="state.pageSizeChangeHandle"
                @pageCurrentChangeHandle="state.pageCurrentChangeHandle"
                @selection-change="state.dataListSelectionChangeHandle" :table-props={selection:true}>
            <!-- 自定义列, 可以通过 order 配置列的顺序 -->
            <el-table-column label="操作" order="99" width="150px">
                <template #default="scope">
                    <el-tooltip content="修改" placement="top">
                        <el-button link type="primary" icon="Edit" @click="addOrUpdateHandle(scope.row.id)"
                                   v-hasPermi="['system:sysdept:edit']"
                        >编辑
                        </el-button>
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top">
                        <el-button link type="primary" icon="Delete" @click="state.deleteHandle(scope.row.id)"
                                   v-hasPermi="['system:sysdept:remove']"
                        >删除
                        </el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
        </qtable>

        <!-- 弹窗, 新增 / 修改 -->
        <add-or-update :key="addKey" ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>


    </div>
</template>


<script setup name="sysdept">
    import tableView from "@/hooks/tableView";
    import {reactive, ref, toRefs} from "vue";

    import AddOrUpdate from "./add-or-update.vue";

    const view = reactive({
        getDataListURL: "/system/sysdept/list",
        getDataListIsPage: true,
        deleteURL: "/system/sysdept",
        deleteIsBatch: true,
        exportURL: "/system/sysdept/export",
        dataForm: {}
    });
    const {proxy} = getCurrentInstance();

    const state = reactive({...tableView(view), ...toRefs(view)});


    // 列表字段配置
    const jsonColumns = ref([

        {
            label: "父部门id",
            prop: "parentId",

        },


        {
            label: "部门名称",
            prop: "deptName",

        },


        {
            label: "显示顺序",
            prop: "orderNum",

        },


        {
            label: "负责人",
            prop: "leader",

        },


        {
            label: "联系电话",
            prop: "phone",

        },


        {
            label: "部门状态（0正常 1停用）",
            prop: "status",
            dictType: "COMMON_STATUS",

        },


    ]);

    // 搜索字段配置
    const searchColumns = ref([

        {
            label: "父部门id",
            prop: "parentId",
            type: "input",
            placeholder: "请输入父部门id"
        },


        {
            label: "部门名称",
            prop: "deptName",
            type: "input",
            placeholder: "请输入部门名称"
        },



    ]);

    const addKey = ref(0);
    const addOrUpdateRef = ref();
    const addOrUpdateHandle = (id) => {

        addKey.value++;
        nextTick(() => {
            addOrUpdateRef.value.init(id);
        });
    };
</script>