<template>
  <div class="header-bar">
    <div class="header-left">
      <el-icon class="toggle-btn" @click="appStore.toggleSidebar" :size="20">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <el-dropdown trigger="click">
        <span class="user-info">
          <el-icon><UserFilled /></el-icon>
          {{ userStore.realName || userStore.username }}
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>
              {{ userStore.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const currentTitle = computed(() => route.meta?.title || '')

function handleLogout() {
  ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.toggle-btn {
  cursor: pointer;
}
.toggle-btn:hover {
  color: #409eff;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #333;
}
</style>
