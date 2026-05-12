<template>
  <div class="layer-inside-wrap">
    <div class="layer-inside-head">
      <div class="title" @click="$emit('close')">
        <el-icon :size="22">
          <back />
        </el-icon>
        {{ title }}
      </div>
      <div class="close" @click="$emit('close')">
        <el-icon :size="22"><close /></el-icon>
      </div>
    </div>
    <div class="layer-inside-cont">
      <slot></slot>
    </div>
  </div>
</template>
<script>
import { defineComponent, ref } from 'vue'
export default defineComponent({
  props: {
    title:{
      type:String,
      default:''
    }
  },
  emits: ['close'],
  setup(props, ctx) {
  }
})
</script>

<style lang="scss" scoped>
.layer-inside-wrap{
  background: #fff;
  z-index: 99;
  .layer-inside-head{
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 25px;
    line-height: 55px;
    padding: 0 20px;
    box-sizing: border-box;
    border-bottom: 1px solid #ededed;
    cursor: pointer;
    i{
      vertical-align: -5px;
    }
    .title{
      font-weight: bold;
      color: var(--el-color-primary);
      font-size:18px;
      i{
        margin-right: 5px;
        cursor: pointer;
      }
    }
    .close{
      cursor: pointer;
      color: var(--el-color-primary);
    }
  }
  .layer-inside-cont{
    padding: 20px 20px;
    box-sizing: border-box;
    background-color: #fff;
  }
  .layer-inside-foot{
    padding: 20px 20px 30px;
    box-sizing: border-box;
    text-align: center;
    background-color: #fff;
  }
}
</style>
