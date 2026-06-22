/**
 * Axios 实例封装 —— 统一请求/响应拦截
 *
 * 请求拦截器：
 * - 自动从 Pinia store 获取 JWT Token 附加到 Authorization 请求头
 *
 * 响应拦截器：
 * - 成功响应：检查业务状态码 code，非 200 时弹出错误提示
 * - 401 响应：清空 Token 并跳转登录页
 * - 403 响应：提示权限不足
 * - 网络异常：提示网络错误
 *
 * 所有 API 模块通过此实例发请求，获取到的 data 已经是解包后的 Result.data。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',  // Vite proxy 转发到 http://localhost:8081
  timeout: 15000    // 15 秒超时
})

// ========== 请求拦截器 ==========
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    // 添加 Bearer Token 认证头
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ========== 响应拦截器 ==========
request.interceptors.response.use(
  // 业务成功（HTTP 2xx）
  (response) => {
    const data = response.data
    // 业务状态码非 200 时提示错误
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data // 返回完整的 Result 对象，调用方通过 .data 取数据
  },
  // HTTP 错误（4xx/5xx/网络错误）
  (error) => {
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401: // Token 过期或无效
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          ElMessage.error('登录已过期，请重新登录')
          break
        case 403: // 权限不足
          ElMessage.error('没有操作权限')
          break
        default: // 其他服务端错误
          ElMessage.error(error.response.data?.message || '服务器错误')
      }
    } else {
      // 网络异常（无响应）
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
