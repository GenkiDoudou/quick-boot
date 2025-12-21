<template>

    <q-modal :visible="visibleRef" mode="dialog" :title="(!dataForm.id)?'新增':'修改'"
             @close="handleClose" @submit="submit">


        <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">

            
                <el-row>
                    <el-col :span="20">
                        <el-form-item label="父部门id" prop="parentId">
                                <el-input v-model="dataForm.parentId"
                                          placeholder="请输入父部门id"/>

                        </el-form-item>
                    </el-col>
                </el-row>


                <el-row>
                    <el-col :span="20">
                        <el-form-item label="部门名称" prop="deptName">
                                <el-input v-model="dataForm.deptName"
                                          placeholder="请输入部门名称"/>

                        </el-form-item>
                    </el-col>
                </el-row>


                <el-row>
                    <el-col :span="20">
                        <el-form-item label="显示顺序" prop="orderNum">
                                <el-input v-model="dataForm.orderNum"
                                          placeholder="请输入显示顺序"/>

                        </el-form-item>
                    </el-col>
                </el-row>


                <el-row>
                    <el-col :span="20">
                        <el-form-item label="负责人" prop="leader">
                                <el-input v-model="dataForm.leader"
                                          placeholder="请输入负责人"/>

                        </el-form-item>
                    </el-col>
                </el-row>


                <el-row>
                    <el-col :span="20">
                        <el-form-item label="联系电话" prop="phone">
                                <el-input v-model="dataForm.phone"
                                          placeholder="请输入联系电话"/>

                        </el-form-item>
                    </el-col>
                </el-row>


                <el-row>
                    <el-col :span="20">
                        <el-form-item label="部门状态（0正常 1停用）" prop="status">
                                <el-input v-model="dataForm.status"
                                          placeholder="请输入部门状态（0正常 1停用）"/>

                        </el-form-item>
                    </el-col>
                </el-row>

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
        id: "",
        parentId: "",
        deptName: "",
        orderNum: "",
        leader: "",
        phone: "",
        status: "",


    })
    const handleClose = () => {
        visibleRef.value = false;
    };
    //  校验
    const rules = ref(
        {
            // xxx: [{required: true, message: '请输入xxx', trigger: 'blur'}]
            parentId: [{required: true, message: '请输入父部门id', trigger: 'blur'}],
            deptName: [{required: true, message: '请输入部门名称', trigger: 'blur'}],
            orderNum: [{required: true, message: '请输入显示顺序', trigger: 'blur'}],
            status: [{required: true, message: '请输入部门状态（0正常 1停用）', trigger: 'blur'}],


        }
    );

    // 初始化方法
    const init = ( id) => {
        visibleRef.value = true;
        if (id) {
            dataForm.value.id = id;
            getInfo(id);
        }

    }
    // 根据id查询详情
    const getInfo = (id) => {
        baseService.get("/system/sysdept/" + id).then(res => {
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
                    baseService.put("/system/sysdept", dataForm.value).then(res => {
                        proxy.$modal.msgSuccess("修改成功");
                        visibleRef.value = false;
                        emit("refreshDataList");
                    })
                } else {
                    //保存
                    baseService.post("/system/sysdept", dataForm.value).then(res => {
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