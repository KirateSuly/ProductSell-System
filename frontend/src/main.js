/**
 * 应用入口文件
 *
 * 初始化流程：
 * 1. 创建 Vue 3 应用实例
 * 2. 注册 Element Plus（中文本地化）
 * 3. 注册 Vue Router（路由守卫在 router/index.js 中配置）
 * 4. 注册 Pinia（状态管理，支持 localStorage 持久化）
 * 5. 全局注册 Element Plus Icons
 * 6. 挂载到 #app
 */
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import './assets/styles/global.css'

const app = createApp(App)

// Element Plus UI 框架（中文）
app.use(ElementPlus, { locale: zhCn })
// 路由
app.use(router)
// 状态管理
app.use(pinia)

// 全局注册 Element Plus 图标组件（按名称使用，如 <el-icon><UserFilled /></el-icon>）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
