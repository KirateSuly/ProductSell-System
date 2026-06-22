<template>
  <div class="login-page">
    <!-- ==================== 左侧：动画角色区域 ==================== -->
    <div class="characters-panel">
      <!-- Logo / 品牌 -->
      <div class="characters-panel__brand">
        <div class="brand-icon">
          <el-icon :size="16"><StarFilled /></el-icon>
        </div>
        <span>商品销售管理系统</span>
      </div>

      <!-- 角色舞台 -->
      <div class="characters-stage">
        <div ref="stageRef" class="stage-inner" :style="{ width: '550px', height: '400px' }">
          <!-- 紫色角色（后排左） -->
          <div
            ref="purpleRef"
            class="character character--purple"
            :style="purpleStyle"
          >
            <div class="character__eyes" :style="purpleEyesStyle">
              <AnimatedEye
                :size="18"
                :pupil-size="7"
                :max-distance="5"
                eye-color="white"
                pupil-color="#2D2D2D"
                :is-blinking="isPurpleBlinking"
                :force-look-x="purpleEyeLookX"
                :force-look-y="purpleEyeLookY"
              />
              <AnimatedEye
                :size="18"
                :pupil-size="7"
                :max-distance="5"
                eye-color="white"
                pupil-color="#2D2D2D"
                :is-blinking="isPurpleBlinking"
                :force-look-x="purpleEyeLookX"
                :force-look-y="purpleEyeLookY"
              />
            </div>
          </div>

          <!-- 黑色角色（后排右） -->
          <div
            ref="blackRef"
            class="character character--black"
            :style="blackStyle"
          >
            <div class="character__eyes" :style="blackEyesStyle">
              <AnimatedEye
                :size="16"
                :pupil-size="6"
                :max-distance="4"
                eye-color="white"
                pupil-color="#2D2D2D"
                :is-blinking="isBlackBlinking"
                :force-look-x="blackEyeLookX"
                :force-look-y="blackEyeLookY"
              />
              <AnimatedEye
                :size="16"
                :pupil-size="6"
                :max-distance="4"
                eye-color="white"
                pupil-color="#2D2D2D"
                :is-blinking="isBlackBlinking"
                :force-look-x="blackEyeLookX"
                :force-look-y="blackEyeLookY"
              />
            </div>
          </div>

          <!-- 橙色角色（前排左，半圆形） -->
          <div
            ref="orangeRef"
            class="character character--orange"
            :style="orangeStyle"
          >
            <div class="character__eyes" :style="orangeEyesStyle">
              <AnimatedPupil
                :size="12"
                :max-distance="5"
                pupil-color="#2D2D2D"
                :force-look-x="orangeEyeLookX"
                :force-look-y="orangeEyeLookY"
              />
              <AnimatedPupil
                :size="12"
                :max-distance="5"
                pupil-color="#2D2D2D"
                :force-look-x="orangeEyeLookX"
                :force-look-y="orangeEyeLookY"
              />
            </div>
          </div>

          <!-- 黄色角色（前排右，圆角矩形） -->
          <div
            ref="yellowRef"
            class="character character--yellow"
            :style="yellowStyle"
          >
            <div class="character__eyes" :style="yellowEyesStyle">
              <AnimatedPupil
                :size="12"
                :max-distance="5"
                pupil-color="#2D2D2D"
                :force-look-x="yellowEyeLookX"
                :force-look-y="yellowEyeLookY"
              />
              <AnimatedPupil
                :size="12"
                :max-distance="5"
                pupil-color="#2D2D2D"
                :force-look-x="yellowEyeLookX"
                :force-look-y="yellowEyeLookY"
              />
            </div>
            <!-- 黄色角色的嘴巴（一条横线） -->
            <div class="character__mouth" :style="yellowMouthStyle" />
          </div>
        </div>
      </div>

      <!-- 底部链接 -->
      <div class="characters-panel__footer">
        <a href="#" @click.prevent>隐私政策</a>
        <a href="#" @click.prevent>服务条款</a>
        <a href="#" @click.prevent>联系我们</a>
      </div>

      <!-- 装饰元素 -->
      <div class="characters-panel__grid" />
      <div class="characters-panel__blob characters-panel__blob--1" />
      <div class="characters-panel__blob characters-panel__blob--2" />
    </div>

    <!-- ==================== 右侧：登录表单 ==================== -->
    <div class="login-panel">
      <div class="login-panel__inner">
        <!-- 移动端 Logo（大屏隐藏） -->
        <div class="login-panel__mobile-brand">
          <div class="brand-icon brand-icon--mobile">
            <el-icon :size="16"><StarFilled /></el-icon>
          </div>
          <span>商品销售管理系统</span>
        </div>

        <!-- 标题 -->
        <div class="login-panel__header">
          <h1>欢迎回来！</h1>
          <p>请输入您的账号信息</p>
        </div>

        <!-- 登录表单（保留原有认证流程） -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          size="large"
          @submit.prevent="handleLogin"
        >
          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              @focus="isTyping = true"
              @blur="isTyping = false"
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="••••••••"
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            >
              <template #suffix>
                <el-icon
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                >
                  <View v-if="!showPassword" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 记住我 + 忘记密码 -->
          <div class="login-form__options">
            <el-checkbox v-model="form.rememberMe">
              记住我（7天内免登录）
            </el-checkbox>
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="login-form__error">
            {{ error }}
          </div>

          <!-- 登录按钮 -->
          <el-button
            type="primary"
            :loading="loading"
            class="login-form__submit"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>

      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 登录页面（动画角色版）
 *
 * 在保留原有 Element Plus 表单认证流程的基础上，
 * 新增了四个会与用户互动的卡通角色：
 * - 紫色/黑色角色：眼睛带白色眼底，会眨眼
 * - 橙色/黄色角色：纯黑瞳孔，跟随鼠标
 * - 所有角色眼睛都会跟随鼠标移动
 * - 输入时角色会对视、偷看等
 *
 * 安全认证流程（未变）：
 * 1. 表单校验 → 2. SHA-256 脱敏 → 3. API 登录 → 4. 存储 Token → 5. 跳转 /dashboard
 */
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock, View, Hide, StarFilled } from '@element-plus/icons-vue'
import { sha256 } from '@/utils/crypto'
import AnimatedPupil from '@/components/login/AnimatedPupil.vue'
import AnimatedEye from '@/components/login/AnimatedEye.vue'

// ==================== 基础状态 ====================
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123',
  rememberMe: false
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// ==================== 动画状态 ====================
const mouseX = ref(0)
const mouseY = ref(0)
const isTyping = ref(false)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

// 角色 DOM 引用
const stageRef = ref(null)
const purpleRef = ref(null)
const blackRef = ref(null)
const yellowRef = ref(null)
const orangeRef = ref(null)

// 眨眼和偷看定时器
let purpleBlinkTimeout = null
let purpleBlinkEndTimeout = null
let blackBlinkTimeout = null
let blackBlinkEndTimeout = null
let peekTimeout = null
let peekEndTimeout = null
let lookTimeout = null

// ==================== 鼠标追踪 ====================
function handleMouseMove(e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

onMounted(() => window.addEventListener('mousemove', handleMouseMove))
onUnmounted(() => window.removeEventListener('mousemove', handleMouseMove))

// ==================== 角色位置计算 ====================
/*
 * 注意：不能直接用角色自身 rect 计算鼠标偏移，因为角色会 skewX 倾斜。
 * 倾斜后 rect 会跟着偏，形成"反馈循环"→角色向右歪了就回不来。
 *
 * 解决方法：以外层舞台 stageRef 的左上角为基准 + 角色静态位置，
 * 算出角色中心后再算鼠标偏移。这样就完全不受角色自身变换影响。
 */

// 每个角色在舞台内的静态位置（CSS 中写死的）
const CHAR_STATIC = {
  purple: { left: 70,  width: 180 }, // 中心 X = 70 + 90 = 160
  black:  { left: 240, width: 120 }, // 中心 X = 240 + 60 = 300
  orange: { left: 0,   width: 240 }, // 中心 X = 0 + 120 = 120
  yellow: { left: 310, width: 140 }  // 中心 X = 310 + 70 = 380
}

function calcStaticCenter(stageRect, key, heightRatio) {
  if (!stageRect) return { cx: 0, cy: 0 }
  const s = CHAR_STATIC[key]
  return {
    cx: stageRect.left + s.left + s.width / 2,
    cy: stageRect.top  + stageRect.height * heightRatio // 头部 ≈ 高度 1/3
  }
}

function calcPosition(key, heightRatio) {
  const stage = stageRef.value
  if (!stage) return { faceX: 0, faceY: 0, bodySkew: 0 }

  const stageRect = stage.getBoundingClientRect()
  const { cx, cy } = calcStaticCenter(stageRect, key, heightRatio)
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy

  // 倾斜用舞台相对位置（而非角色相对位置），避免角色靠左时只有鼠标到最左边才左倾的问题
  const stageRelX = (mouseX.value - stageRect.left) / stageRect.width   // 0=舞台左边缘, 1=舞台右边缘
  const normalizedSkew = (0.5 - stageRelX) * 2  // 左半=+1(左倾), 右半=-1(右倾)

  return {
    faceX: Math.max(-15, Math.min(15, dx / 20)),
    faceY: Math.max(-10, Math.min(10, dy / 30)),
    bodySkew: Math.max(-10, Math.min(12, normalizedSkew * 12))
  }
}

const purplePos = computed(() => calcPosition('purple', 1/3))
const blackPos  = computed(() => calcPosition('black',  1/3))
const orangePos = computed(() => calcPosition('orange', 1/3))
const yellowPos = computed(() => calcPosition('yellow', 1/3))

// 密码是否隐藏但有内容（触发偷看状态）
const isPasswordHidden = computed(() => form.password.length > 0 && !showPassword.value)

// ==================== 紫色角色样式 ====================
const purpleStyle = computed(() => {
  const skew = purplePos.value.bodySkew || 0
  let transform
  if (showPassword.value) {
    transform = 'skewX(0deg)'
  } else if (isTyping.value || isPasswordHidden.value) {
    transform = `skewX(${skew - 12}deg) translateX(40px)`
  } else {
    transform = `skewX(${skew}deg)`
  }
  return {
    left: '70px',
    width: '180px',
    height: (isTyping.value || isPasswordHidden.value) ? '440px' : '400px',
    backgroundColor: '#6C3FF5',
    borderRadius: '10px 10px 0 0',
    zIndex: 1,
    transform,
    transformOrigin: 'bottom center'
  }
})

const purpleEyeLookX = computed(() => {
  if (showPassword.value) return isPurplePeeking.value ? 4 : -4
  if (isLookingAtEachOther.value) return 3
  return undefined
})
const purpleEyeLookY = computed(() => {
  if (showPassword.value) return isPurplePeeking.value ? 5 : -4
  if (isLookingAtEachOther.value) return 4
  return undefined
})

const purpleEyesStyle = computed(() => {
  const fx = purplePos.value.faceX || 0
  const fy = purplePos.value.faceY || 0
  return {
    left: showPassword.value ? '20px' : isLookingAtEachOther.value ? '55px' : `${45 + fx}px`,
    top: showPassword.value ? '35px' : isLookingAtEachOther.value ? '65px' : `${40 + fy}px`,
    gap: '32px'
  }
})

// ==================== 黑色角色样式 ====================
const blackStyle = computed(() => {
  const skew = blackPos.value.bodySkew || 0
  let transform
  if (showPassword.value) {
    transform = 'skewX(0deg)'
  } else if (isLookingAtEachOther.value) {
    transform = `skewX(${skew * 1.5 + 10}deg) translateX(20px)`
  } else if (isTyping.value || isPasswordHidden.value) {
    transform = `skewX(${skew * 1.5}deg)`
  } else {
    transform = `skewX(${skew}deg)`
  }
  return {
    left: '240px',
    width: '120px',
    height: '310px',
    backgroundColor: '#2D2D2D',
    borderRadius: '8px 8px 0 0',
    zIndex: 2,
    transform,
    transformOrigin: 'bottom center'
  }
})

const blackEyeLookX = computed(() => {
  if (showPassword.value) return -4
  if (isLookingAtEachOther.value) return 0
  return undefined
})
const blackEyeLookY = computed(() => {
  if (showPassword.value) return -4
  if (isLookingAtEachOther.value) return -4
  return undefined
})

const blackEyesStyle = computed(() => {
  const fx = blackPos.value.faceX || 0
  const fy = blackPos.value.faceY || 0
  return {
    left: showPassword.value ? '10px' : isLookingAtEachOther.value ? '32px' : `${26 + fx}px`,
    top: showPassword.value ? '28px' : isLookingAtEachOther.value ? '12px' : `${32 + fy}px`,
    gap: '24px'
  }
})

// ==================== 橙色角色样式 ====================
const orangeStyle = computed(() => ({
  left: '0px',
  width: '240px',
  height: '200px',
  zIndex: 3,
  backgroundColor: '#FF9B6B',
  borderRadius: '120px 120px 0 0',
  transform: showPassword.value ? 'skewX(0deg)' : `skewX(${orangePos.value.bodySkew || 0}deg)`,
  transformOrigin: 'bottom center'
}))

const orangeEyeLookX = computed(() => showPassword.value ? -5 : undefined)
const orangeEyeLookY = computed(() => showPassword.value ? -4 : undefined)

const orangeEyesStyle = computed(() => {
  const fx = orangePos.value.faceX || 0
  const fy = orangePos.value.faceY || 0
  return {
    left: showPassword.value ? '50px' : `${82 + fx}px`,
    top: showPassword.value ? '85px' : `${90 + fy}px`,
    gap: '32px'
  }
})

// ==================== 黄色角色样式 ====================
const yellowStyle = computed(() => ({
  left: '310px',
  width: '140px',
  height: '230px',
  backgroundColor: '#E8D754',
  borderRadius: '70px 70px 0 0',
  zIndex: 4,
  transform: showPassword.value ? 'skewX(0deg)' : `skewX(${yellowPos.value.bodySkew || 0}deg)`,
  transformOrigin: 'bottom center'
}))

const yellowEyeLookX = computed(() => showPassword.value ? -5 : undefined)
const yellowEyeLookY = computed(() => showPassword.value ? -4 : undefined)

const yellowEyesStyle = computed(() => {
  const fx = yellowPos.value.faceX || 0
  const fy = yellowPos.value.faceY || 0
  return {
    left: showPassword.value ? '20px' : `${52 + fx}px`,
    top: showPassword.value ? '35px' : `${40 + fy}px`,
    gap: '24px'
  }
})

const yellowMouthStyle = computed(() => {
  const fx = yellowPos.value.faceX || 0
  const fy = yellowPos.value.faceY || 0
  return {
    left: showPassword.value ? '10px' : `${40 + fx}px`,
    top: showPassword.value ? '88px' : `${88 + fy}px`
  }
})

// ==================== 眨眼动画（紫色角色） ====================
function schedulePurpleBlink() {
  purpleBlinkTimeout = setTimeout(() => {
    isPurpleBlinking.value = true
    purpleBlinkEndTimeout = setTimeout(() => {
      isPurpleBlinking.value = false
      schedulePurpleBlink()
    }, 150)
  }, Math.random() * 4000 + 3000) // 3-7 秒随机间隔
}

// 眨眼动画（黑色角色）
function scheduleBlackBlink() {
  blackBlinkTimeout = setTimeout(() => {
    isBlackBlinking.value = true
    blackBlinkEndTimeout = setTimeout(() => {
      isBlackBlinking.value = false
      scheduleBlackBlink()
    }, 150)
  }, Math.random() * 4000 + 3000)
}

onMounted(() => {
  schedulePurpleBlink()
  scheduleBlackBlink()
})

// ==================== 输入时角色对视 ====================
watch(isTyping, (val) => {
  clearTimeout(lookTimeout)
  if (val) {
    isLookingAtEachOther.value = true
    lookTimeout = setTimeout(() => {
      isLookingAtEachOther.value = false
    }, 800)
  } else {
    isLookingAtEachOther.value = false
  }
})

// ==================== 密码可见时偷看 ====================
function schedulePeek() {
  clearTimeout(peekTimeout)
  clearTimeout(peekEndTimeout)
  if (form.password.length > 0 && showPassword.value) {
    peekTimeout = setTimeout(() => {
      isPurplePeeking.value = true
      peekEndTimeout = setTimeout(() => {
        isPurplePeeking.value = false
        schedulePeek()
      }, 800)
    }, Math.random() * 3000 + 2000) // 2-5 秒随机偷看
  }
}

watch([() => form.password, showPassword], ([pwd, show]) => {
  clearTimeout(peekTimeout)
  clearTimeout(peekEndTimeout)
  if (pwd.length > 0 && show) {
    schedulePeek()
  } else {
    isPurplePeeking.value = false
  }
})

// ==================== 清理定时器 ====================
onUnmounted(() => {
  clearTimeout(purpleBlinkTimeout)
  clearTimeout(purpleBlinkEndTimeout)
  clearTimeout(blackBlinkTimeout)
  clearTimeout(blackBlinkEndTimeout)
  clearTimeout(peekTimeout)
  clearTimeout(peekEndTimeout)
  clearTimeout(lookTimeout)
})

// ==================== 登录逻辑（保留原有安全流程） ====================
async function handleLogin() {
  // Element Plus 表单验证
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  error.value = ''
  try {
    // SHA-256 脱敏后再传输，防止密码明文泄露
    const hashedPassword = await sha256(form.password)
    await userStore.login({
      username: form.username,
      password: hashedPassword,
      rememberMe: form.rememberMe
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    // API 错误已在 Axios 响应拦截器中以 ElMessage 提示
    // 此处额外设置本地错误信息
    if (err?.response?.data?.message) {
      error.value = err.response.data.message
    } else {
      error.value = '登录失败，请检查用户名和密码'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ============================== 整体布局 ============================== */
.login-page {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
}

/* ============================== 左侧：角色面板 ============================== */
.characters-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  overflow: hidden;
}

.characters-panel__brand {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.brand-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}

/* 角色舞台 */
.characters-stage {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  flex: 1;
  min-height: 420px;
  margin: 24px 0;
}

.stage-inner {
  position: relative;
}

/* 底部链接 */
.characters-panel__footer {
  position: relative;
  z-index: 20;
  display: flex;
  gap: 32px;
  font-size: 14px;
  opacity: 0.6;
}

.characters-panel__footer a {
  color: inherit;
  text-decoration: none;
  transition: opacity 0.2s;
}

.characters-panel__footer a:hover {
  opacity: 1;
}

/* 装饰：网格 */
.characters-panel__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 0;
}

/* 装饰：模糊光斑 */
.characters-panel__blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(96px);
  z-index: 0;
  background: rgba(255, 255, 255, 0.1);
}

.characters-panel__blob--1 {
  top: 25%;
  right: 25%;
  width: 256px;
  height: 256px;
}

.characters-panel__blob--2 {
  bottom: 25%;
  left: 25%;
  width: 384px;
  height: 384px;
  background: rgba(255, 255, 255, 0.05);
}

/* ============================== 角色通用 ============================== */
.character {
  position: absolute;
  bottom: 0;
  transition: all 0.7s ease-in-out;
}

.character__eyes {
  position: absolute;
  display: flex;
  transition: all 0.7s ease-in-out;
}

/* 橙色和黄色角色眼睛过渡更快 */
.character--orange .character__eyes,
.character--yellow .character__eyes {
  transition: all 0.2s ease-out;
}

/* 嘴巴 */
.character__mouth {
  position: absolute;
  width: 80px;
  height: 4px;
  background-color: #2D2D2D;
  border-radius: 9999px;
  transition: all 0.2s ease-out;
}

/* ============================== 右侧：登录面板 ============================== */
.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #fff;
}

.login-panel__inner {
  width: 100%;
  max-width: 420px;
}

/* 移动端品牌标识（默认隐藏） */
.login-panel__mobile-brand {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 48px;
  color: #303133;
}

.brand-icon--mobile {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

/* 标题 */
.login-panel__header {
  text-align: center;
  margin-bottom: 40px;
}

.login-panel__header h1 {
  font-size: 30px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.login-panel__header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 表单 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.login-form__options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}


/* 错误提示 */
.login-form__error {
  padding: 12px;
  font-size: 14px;
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.2);
  border-radius: 8px;
  margin-bottom: 8px;
}

/* 登录按钮 */
.login-form__submit {
  width: 100%;
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 500 !important;
  margin-top: 4px;
}

/* 密码切换图标 */
.password-toggle {
  cursor: pointer;
  color: #909399;
  transition: color 0.2s;
}

.password-toggle:hover {
  color: #303133;
}

/* ============================== 响应式：移动端隐藏角色 ============================== */
@media (max-width: 1023px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .characters-panel {
    display: none;
  }

  .login-panel__mobile-brand {
    display: flex;
  }
}
</style>
