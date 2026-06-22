import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const aiChatVisible = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function openAIChat() {
    aiChatVisible.value = true
  }

  function closeAIChat() {
    aiChatVisible.value = false
  }

  return { sidebarCollapsed, toggleSidebar, aiChatVisible, openAIChat, closeAIChat }
})
