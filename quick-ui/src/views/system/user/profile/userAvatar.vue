<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="options.img" title="点击上传头像" class="img-circle img-lg" />
    <el-dialog title="修改头像" v-model="open" width="800px" append-to-body @close="closeDialog">
      <el-row>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <p style="padding: 20px; color: #999;">头像上传功能（需安装 vue-cropper）</p>
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img :src="options.img" style="width:200px;height:200px;border-radius:50%;" />
          </div>
        </el-col>
      </el-row>
      <el-row style="margin-top: 20px;">
        <el-col :span="24" style="text-align: center;">
          <el-upload
            action="#"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <el-button>选择图片</el-button>
          </el-upload>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { uploadAvatar } from '@/api/system/user'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const open = ref(false)

const options = reactive({
  img: userStore.avatar,
  filename: 'avatar',
  previews: {}
})

function editCropper() {
  open.value = true
}

function beforeUpload(file) {
  if (file.type.indexOf('image/') === -1) {
    ElMessage.error('文件格式错误，请上传图片类型。')
    return false
  }
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    options.img = reader.result
    options.filename = file.name
  }
  return false
}

function closeDialog() {
  options.img = userStore.avatar
}
</script>

<style lang='scss' scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
  cursor: pointer;
}
.img-circle {
  border-radius: 50%;
}
.img-lg {
  width: 120px;
  height: 120px;
}
</style>
