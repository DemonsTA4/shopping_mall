import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/styles/main.scss'
import i18n from './i18n'

import { formatUtcToLocal } from '@/utils/datetime';

// 导入mock服务（开发环境）
//import './mock'

const app = createApp(App)

// 注册所有ElementPlus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(router)
app.use(pinia)
app.use(createPinia())
app.use(ElementPlus)
app.use(i18n)

app.mount('#app')