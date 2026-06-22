<template>
  <div
    ref="eyeRef"
    class="animated-eye"
    :style="eyeStyle"
  >
    <!-- 眨眼时隐藏瞳孔 -->
    <div
      v-if="!isBlinking"
      class="animated-eye__pupil"
      :style="pupilStyle"
    />
  </div>
</template>

<script setup>
/**
 * 动画眼球组件（白色眼底 + 黑色瞳孔）
 * 紫、黑色角色使用。支持眨眼动画和强制注视方向。
 *
 * Props:
 * - forceLookX/Y: 强制注视方向（偷看密码、角色对视等场景）
 * - isBlinking:   眨眼时眼睛高度缩为 2px，同时隐藏瞳孔
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  size:        { type: Number, default: 48 },
  pupilSize:   { type: Number, default: 16 },
  maxDistance: { type: Number, default: 10 },
  eyeColor:    { type: String, default: 'white' },
  pupilColor:  { type: String, default: 'black' },
  isBlinking:  { type: Boolean, default: false },
  forceLookX:  { type: Number, default: undefined },
  forceLookY:  { type: Number, default: undefined }
})

const mouseX = ref(0)
const mouseY = ref(0)
const eyeRef = ref(null)

function handleMouseMove(e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

onMounted(() => window.addEventListener('mousemove', handleMouseMove))
onUnmounted(() => window.removeEventListener('mousemove', handleMouseMove))

// 眼球外层样式：控制大小、颜色、眨眼高度
const eyeStyle = computed(() => ({
  width: `${props.size}px`,
  height: props.isBlinking ? '2px' : `${props.size}px`,
  backgroundColor: props.eyeColor,
  borderRadius: '50%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  overflow: 'hidden',
  transition: 'all 0.15s'
}))

// 瞳孔偏移量：优先使用强制方向，否则跟随鼠标
const pupilStyle = computed(() => {
  let x = 0, y = 0

  if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
    x = props.forceLookX
    y = props.forceLookY
  } else if (eyeRef.value) {
    const rect = eyeRef.value.getBoundingClientRect()
    const cx = rect.left + rect.width / 2
    const cy = rect.top + rect.height / 2
    const dx = mouseX.value - cx
    const dy = mouseY.value - cy
    const dist = Math.min(Math.sqrt(dx * dx + dy * dy), props.maxDistance)
    const angle = Math.atan2(dy, dx)
    x = Math.cos(angle) * dist
    y = Math.sin(angle) * dist
  }

  return {
    width: `${props.pupilSize}px`,
    height: `${props.pupilSize}px`,
    backgroundColor: props.pupilColor,
    borderRadius: '50%',
    transform: `translate(${x}px, ${y}px)`,
    transition: 'transform 0.1s ease-out'
  }
})
</script>
