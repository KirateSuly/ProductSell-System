<template>
  <div ref="pupilRef" class="animated-pupil" :style="pupilStyle" />
</template>

<script setup>
/**
 * 动画瞳孔组件
 * 一个小黑点，跟随鼠标移动。橙、黄色角色直接用它（无白色眼底）。
 *
 * Props:
 * - forceLookX/Y: 强制瞳孔看向指定方向（如偷看密码、角色对视）
 *   不传则默认跟随鼠标
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  size:        { type: Number, default: 12 },
  maxDistance: { type: Number, default: 5 },
  pupilColor:  { type: String, default: 'black' },
  forceLookX:  { type: Number, default: undefined },
  forceLookY:  { type: Number, default: undefined }
})

const mouseX = ref(0)
const mouseY = ref(0)
const pupilRef = ref(null)

function handleMouseMove(e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

onMounted(() => window.addEventListener('mousemove', handleMouseMove))
onUnmounted(() => window.removeEventListener('mousemove', handleMouseMove))

const pupilStyle = computed(() => {
  let x = 0, y = 0

  if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
    x = props.forceLookX
    y = props.forceLookY
  } else if (pupilRef.value) {
    const rect = pupilRef.value.getBoundingClientRect()
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
    width: `${props.size}px`,
    height: `${props.size}px`,
    backgroundColor: props.pupilColor,
    borderRadius: '50%',
    transform: `translate(${x}px, ${y}px)`,
    transition: 'transform 0.1s ease-out'
  }
})
</script>
