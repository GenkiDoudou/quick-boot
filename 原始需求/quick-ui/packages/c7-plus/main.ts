import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import C7Plus from './src/index'

const app = createApp(App)

app.use(ElementPlus)
app.use(C7Plus)

app.mount('#app')

