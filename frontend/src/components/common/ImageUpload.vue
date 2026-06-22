<template>
  <div class="image-upload">
    <el-upload
      class="upload-component"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="customUpload"
      accept="image/jpeg,image/png,image/gif,image/webp"
      drag
    >
      <img v-if="modelValue" :src="imageUrl" class="upload-preview" />
      <div v-else class="upload-placeholder">
        <el-icon :size="40"><Plus /></el-icon>
        <div>点击或拖拽上传图片</div>
        <div class="upload-tip">支持 JPG/PNG/GIF/WebP，不超过 5MB</div>
      </div>
    </el-upload>
    <el-button v-if="modelValue" type="danger" size="small" @click="handleRemove" class="remove-btn">
      移除图片
    </el-button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/product'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const imageUrl = computed(() => props.modelValue || '')

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('仅支持 JPG/PNG/GIF/WebP 格式')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function customUpload(options) {
  try {
    const res = await uploadImage(options.file)
    if (res.code === 200) {
      emit('update:modelValue', res.data)
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch {
    ElMessage.error('上传失败，请重试')
  }
}

function handleRemove() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}
.upload-preview {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8c939d;
}
.upload-tip {
  font-size: 12px;
  margin-top: 4px;
}
.remove-btn {
  margin-top: 4px;
}
</style>
