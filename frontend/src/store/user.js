/**
 * 用户认证状态管理（Pinia Store）
 *
 * 管理 JWT Token 和用户信息，支持 localStorage 持久化。
 * 页面刷新后 Token 和用户信息自动从 localStorage 恢复。
 *
 * 核心动作：
 * - login(credentials) → 调用登录 API，存储 token + userInfo
 * - logout()            → 清空 token + userInfo，移除 localStorage
 * - fetchUserInfo()    → 从服务端刷新当前用户信息
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // ========== 状态 ==========
  // 从 localStorage 恢复，支持页面刷新后保持登录状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // ========== 计算属性 ==========
  const username = computed(() => userInfo.value?.username || '')
  const realName = computed(() => userInfo.value?.realName || '')
  const role = computed(() => userInfo.value?.role || '')
  const isLoggedIn = computed(() => !!token.value)

  // ========== 动作 ==========

  /**
   * 登录
   * 成功后 token 和 userInfo 同步写入 localStorage
   */
  async function login(credentials) {
    const res = await loginApi(credentials)
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
  }

  /**
   * 退出登录
   * 清空内存和 localStorage 中的认证信息
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  /**
   * 从服务端刷新用户信息
   * 适用于页面刷新后验证 Token 有效性并更新用户信息
   */
  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
  }

  return { token, userInfo, username, realName, role, isLoggedIn, login, logout, fetchUserInfo }
})
