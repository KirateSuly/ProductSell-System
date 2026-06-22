<template>
  <div class="sidebar-menu">
    <div class="logo">
      <span v-if="!appStore.sidebarCollapsed">商品销售管理</span>
      <span v-else>销售</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      :collapse="appStore.sidebarCollapsed"
      @select="handleMenuSelect"
    >
      <el-menu-item index="/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <span>系统首页</span>
      </el-menu-item>
      <el-menu-item v-if="userStore.role === 'ADMIN'" index="/users">
        <el-icon><UserFilled /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      <el-menu-item index="/products">
        <el-icon><GoodsFilled /></el-icon>
        <span>商品管理</span>
      </el-menu-item>
      <el-menu-item index="/customers">
        <el-icon><Avatar /></el-icon>
        <span>客户管理</span>
      </el-menu-item>
      <el-menu-item index="/orders">
        <el-icon><Document /></el-icon>
        <span>订单管理</span>
      </el-menu-item>
      <el-menu-item index="/statistics">
        <el-icon><DataAnalysis /></el-icon>
        <span>销售统计</span>
      </el-menu-item>
      <el-menu-item index="ai">
        <el-icon><ChatDotRound /></el-icon>
        <span>AI 助手</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

/** 菜单选择统一处理：路由菜单跳转，AI 助手打开抽屉 */
function handleMenuSelect(index) {
  if (index === 'ai') {
    appStore.openAIChat()
  } else {
    router.push(index)
  }
}
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.el-menu {
  border-right: none;
}
</style>
