<template>
  <!--
    AI 浮动触发按钮
    支持鼠标/手指拖动到屏幕任意位置，短距离移动（< 5px）视为点击。
    Props: visible — 控制显示/隐藏（抽屉打开时隐藏）
    Emits: click — 点击按钮时触发（非拖拽）
  -->
  <Transition name="ai-btn-fade">
    <div
      v-show="visible"
      ref="btnRef"
      class="ai-floating-btn"
      :style="positionStyle"
      @pointerdown="onDragStart"
      @pointermove="onDragMove"
      @pointerup="onDragEnd"
      @pointercancel="onDragEnd"
    >
      <el-tooltip content="AI 销售顾问" placement="left">
        <el-button type="primary" circle size="large" class="ai-floating-btn-core">
          <el-icon :size="24"><ChatDotRound /></el-icon>
        </el-button>
      </el-tooltip>
    </div>
  </Transition>
</template>

<script setup>
/**
 * AI 浮动触发按钮 —— 支持任意拖动
 *
 * 拖拽逻辑：
 * - pointerdown 记录起始位置，开始拖拽
 * - pointermove 移动时更新元素位置
 * - pointerup 时移动距离 < 5px 视为点击，触发 emit('click')
 */
import { ref, computed } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'

defineProps({
  visible: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click'])

// ========== 位置状态 ==========
const btnRef = ref(null)
const posX = ref(window.innerWidth - 64)   // 默认右下角
const posY = ref(window.innerHeight - 270)

// ========== 拖拽状态 ==========
const dragging = ref(false)
const startX = ref(0)
const startY = ref(0)
const startPosX = ref(0)
const startPosY = ref(0)
const moved = ref(false)

/** 计算样式 */
const positionStyle = computed(() => ({
  left: posX.value + 'px',
  top: posY.value + 'px'
}))

// ========== 拖拽方法 ==========

function onDragStart(e) {
  dragging.value = true
  moved.value = false
  startX.value = e.clientX
  startY.value = e.clientY
  startPosX.value = posX.value
  startPosY.value = posY.value
  // 捕获指针事件，防止拖动时失去焦点
  btnRef.value?.setPointerCapture(e.pointerId)
}

function onDragMove(e) {
  if (!dragging.value) return
  const dx = e.clientX - startX.value
  const dy = e.clientY - startY.value
  if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
    moved.value = true
  }
  let newX = startPosX.value + dx
  let newY = startPosY.value + dy
  // 边界限制（不能拖到屏幕外）
  newX = Math.max(0, Math.min(newX, window.innerWidth - 52))
  newY = Math.max(0, Math.min(newY, window.innerHeight - 52))
  posX.value = newX
  posY.value = newY
}

function onDragEnd() {
  if (!dragging.value) return
  dragging.value = false
  // 移动距离 < 5px 视为点击
  if (!moved.value) {
    emit('click')
  }
}
</script>

<style scoped>
.ai-floating-btn {
  position: fixed;
  z-index: 1000;
  cursor: grab;
  user-select: none;
  touch-action: none;  /* 阻止浏览器默认滚动，保证拖拽流畅 */
}
.ai-floating-btn:active {
  cursor: grabbing;
}
.ai-floating-btn-core {
  pointer-events: auto;
}

.ai-btn-fade-enter-active,
.ai-btn-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.ai-btn-fade-enter-from,
.ai-btn-fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
</style>
