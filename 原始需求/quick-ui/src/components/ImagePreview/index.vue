<template>
  <div class="file-preview">

<!--    <Fragment v-if="showButton">-->
<!--    </Fragment>-->
    <Fragment v-for="(file, index) in files" :key="index" class="file-item" v-if="!showButton">
      <el-image v-if="isImage(file.src)"

                :key="index"
                :src="file.src"
                :preview-src-list="showPreview ? imageSources : []"
                :hide-on-click-modal="!showPreview"
                :initial-index="currentIndex"
                style="width: 100px; height: 100px; margin: 5px;"
                @click="handleClick(index)"
      ></el-image>


      <a v-else :href="file.src" target="_blank" style="margin: 5px;">
        {{ file.name }}
      </a>
    </Fragment>


    <el-button v-if="showButton" @click="openDialog">{{ placeholder }}</el-button>
    <el-dialog v-model="dialogVisible" title="文件预览" :fullscreen="false" :append-to-body="true">
      <span v-for="(file, index) in files" :key="index" >
        <el-image v-if="isImage(file.src)"

                  :key="index"
                  :src="file.src"
                  :preview-src-list="showPreview ? imageSources : []"
                  :hide-on-click-modal="!showPreview"
                  :initial-index="currentIndex"
                  style="width: 100px; height: 100px; margin: 5px;"
                  @click="handleClick(index)"
        ></el-image>
        <a v-else :href="file.src" target="_blank" style="margin: 5px;">
          {{ file.name }}
        </a>
      </span>
      <div>
<!--      <el-image-->
        <!--                v-for="(src, index) in files"-->
        <!--                :key="index"-->
        <!--                :src="src.src"-->
        <!--                :preview-src-list="showPreview ? imageSources : []"-->
        <!--                :hide-on-click-modal="!showPreview"-->
        <!--                :initial-index="currentIndex"-->
        <!--                style="width: 100px; height: 100px; margin: 5px;"-->
        <!--                @click="handleClick(index)"-->
        <!--      ></el-image>-->
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, watch, toRefs, computed} from 'vue';

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  showPreview: {
    type: Boolean,
    default: true
  },
  showButton: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '查看图片'
  }
});

const {src, showPreview, showButton} = toRefs(props);

const imageSources = computed(() => src.value.split(',').map(s => s.trim()));

const currentIndex = ref(0);
const dialogVisible = ref(false);
const currentImage = ref('');

const handleClick = (index) => {
  if (!showPreview.value) {
    currentIndex.value = index;
    currentImage.value = imageSources.value[index];
    dialogVisible.value = true;
  }
};
const files = computed(() => {
  return imageSources.value.map(src => ({
    src,
    name: src.split('/').pop()
  }));
});
const isImage = (src) => {
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];
  const extension = src.split('.').pop().toLowerCase();
  return imageExtensions.includes(extension);
};
const openDialog = () => {
  currentIndex.value = 0;
  currentImage.value = imageSources.value[0];
  dialogVisible.value = true;
};
</script>


<style scoped>
 .file-preview {
   display: flex;
   flex-wrap: wrap;
   align-items: center;
 }

.file-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>
