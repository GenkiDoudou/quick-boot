import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

/**
 * Element Plus 按需自动导入（组件 + 样式）。
 */
export default function createComponents() {
  return Components({
    resolvers: [
      ElementPlusResolver({
        importStyle: 'css',
      }),
    ],
    dts: false,
  })
}
