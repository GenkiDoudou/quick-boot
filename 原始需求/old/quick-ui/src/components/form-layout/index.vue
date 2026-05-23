<template>
  <div class="form-layout-container">
    <div ref="formMainLeft">
      <slot name="form"></slot>
    </div>


    <el-row :gutter="10" class="mb8">
      <slot name="extra"></slot>
    </el-row>


  </div>

</template>
<script setup lang="ts">
</script>