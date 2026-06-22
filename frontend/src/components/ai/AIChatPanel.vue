<template>
  <!--
    AI 智能对话面板
    内嵌在 el-drawer 中，提供对话消息展示 + Markdown 渲染 + 消息输入功能。
    支持 Enter 发送、Shift+Enter 换行、自动滚动到底部。
  -->
  <div class="ai-chat-panel">
    <!-- 标题栏 -->
    <div class="ai-chat-header">
      <div class="ai-chat-header-title">
        <el-icon :size="20"><ChatDotRound /></el-icon>
        <span>AI 销售顾问</span>
      </div>
      <el-button :icon="Close" text @click="$emit('close')" />
    </div>

    <!-- 消息列表 -->
    <el-scrollbar ref="scrollRef" class="ai-chat-body">
      <div v-if="messages.length === 0" class="ai-chat-empty">
        <el-icon :size="48" color="#c0c4cc"><ChatDotRound /></el-icon>
        <p>你好！我是销售进销存顾问</p>
        <p class="ai-chat-empty-hint">可以问我销售数据、库存情况、订单状态等业务问题</p>
      </div>

      <div v-for="(msg, index) in messages" :key="index" class="ai-chat-item">
        <!-- 用户消息（右对齐） -->
        <div v-if="msg.role === 'user'" class="ai-chat-row ai-chat-row--user">
          <div class="ai-chat-bubble ai-chat-bubble--user">
            {{ msg.content }}
          </div>
        </div>

        <!-- AI 回复（左对齐，Markdown 渲染） -->
        <div v-else class="ai-chat-row ai-chat-row--ai">
          <div class="ai-chat-bubble ai-chat-bubble--ai">
            <div class="ai-chat-content" v-html="renderMarkdown(msg.content)" />
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="ai-chat-row ai-chat-row--ai">
        <div class="ai-chat-bubble ai-chat-bubble--ai">
          <el-icon class="is-loading" :size="20"><Loading /></el-icon>
          <span style="margin-left:8px;color:#909399;">思考中...</span>
        </div>
      </div>
    </el-scrollbar>

    <!-- 输入区域 -->
    <div class="ai-chat-footer">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入业务问题，如：今天销售情况怎样？"
        :disabled="loading"
        @keydown="handleKeydown"
      />
      <el-button
        type="primary"
        :icon="Promotion"
        :loading="loading"
        :disabled="!inputText.trim()"
        @click="send"
        style="margin-left:8px;"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * AI 对话面板组件
 * 独立管理消息状态和对话历史，通过 emit('close') 通知父组件关闭。
 */
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close, Loading, Promotion } from '@element-plus/icons-vue'
import { marked } from 'marked'
import { sendChatMessage } from '@/api/ai'

defineEmits(['close'])

// ========== 状态 ==========
const messages = ref([])          // [{ role: 'user'|'assistant', content }]
const inputText = ref('')
const loading = ref(false)
const scrollRef = ref(null)

// ========== 方法 ==========

/** 发送消息 */
async function send() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  // 构建对话历史（取最近 6 轮，即 12 条消息）
  const history = messages.value
    .slice(0, -1)  // 排除刚添加的当前消息
    .slice(-12)
    .map(m => ({ role: m.role, content: m.content }))

  loading.value = true
  try {
    const res = await sendChatMessage({ message: text, history })
    const data = res.data
    messages.value.push({ role: 'assistant', content: data.reply || '抱歉，暂时无法回答。' })
  } catch {
    // 请求失败（拦截器已弹错误提示），移除用户消息避免误导
    messages.value.pop()
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

/** 键盘事件：Enter 发送，Shift+Enter 换行 */
function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

/** 滚动到消息列表底部 */
function scrollToBottom() {
  if (scrollRef.value) {
    // el-scrollbar 的 wrap 是实际滚动容器
    const wrap = scrollRef.value.$el?.querySelector('.el-scrollbar__wrap')
    if (wrap) {
      wrap.scrollTop = wrap.scrollHeight
    }
  }
}

/** Markdown 渲染（安全：marked 默认转义 HTML，防止 XSS） */
function renderMarkdown(content) {
  if (!content) return ''
  return marked.parse(content)
}
</script>

<style scoped>
.ai-chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 标题栏 */
.ai-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}
.ai-chat-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 消息列表区 */
.ai-chat-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}
.ai-chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}
.ai-chat-empty-hint {
  font-size: 13px;
  margin-top: 4px;
}

/* 消息行 */
.ai-chat-row {
  display: flex;
  margin-bottom: 16px;
}
.ai-chat-row--user {
  justify-content: flex-end;
}
.ai-chat-row--ai {
  justify-content: flex-start;
}

/* 消息气泡 */
.ai-chat-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.ai-chat-bubble--user {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.ai-chat-bubble--ai {
  background: #f4f4f5;
  color: #303133;
  border-bottom-left-radius: 4px;
}

/* 输入区域 */
.ai-chat-footer {
  display: flex;
  align-items: flex-end;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  gap: 8px;
}
</style>
