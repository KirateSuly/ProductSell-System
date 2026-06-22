<template>
  <el-container class="main-layout">
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="main-aside">
      <SidebarMenu />
    </el-aside>
    <el-container>
      <el-header class="main-header">
        <HeaderBar />
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <!-- AI 聊天抽屉 -->
  <el-drawer
    v-model="aiChatVisible"
    direction="rtl"
    :size="420"
    :with-header="false"
    :close-on-click-modal="false"
  >
    <AIChatPanel @close="appStore.closeAIChat()" />
  </el-drawer>

  <!-- AI 浮动按钮 -->
  <AIFloatingButton
    :visible="!appStore.aiChatVisible"
    @click="appStore.openAIChat()"
  />
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/store/app'
import SidebarMenu from './SidebarMenu.vue'
import HeaderBar from './HeaderBar.vue'
import AIChatPanel from '@/components/ai/AIChatPanel.vue'
import AIFloatingButton from '@/components/ai/AIFloatingButton.vue'

const appStore = useAppStore()
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)

// AI 聊天面板显隐（双向绑定 computed）
const aiChatVisible = computed({
  get: () => appStore.aiChatVisible,
  set: (val) => val ? appStore.openAIChat() : appStore.closeAIChat()
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}
.main-aside {
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
}
.main-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 16px;
  height: 56px;
}
.main-content {
  background: #f0f2f5;
  min-height: calc(100vh - 56px);
}
</style>
