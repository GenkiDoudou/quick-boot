<template>
  <div class="app-container">

    <div v-for="(item, index) in showIndexs" :key="index">
      {{item}}
      <div v-if="modelValue == item.name">
        {{item}}
        <Layer :title="item.title" @close="close(item.closeIndex)" v-if="item.header">
          <slot :name="item.name" v-if="modelValue == item.name"></slot>
        </Layer>
        <slot :name="item.name" v-if="modelValue == item.name && !item.header"></slot>
      </div>
    </div>

  </div>
</template>
<script setup>

import Layer from "@/components/layer/index.vue";

const emit = defineEmits(['update:modelValue']);
const props = defineProps({
  showIndexs: {
    type: Array,
    default: [],
    required: true
  },
  modelValue: {
    type: [String],
    default: "",
  },
})

const close = (closeIndex) => {

  emit("update:modelValue", closeIndex);
}

</script>
