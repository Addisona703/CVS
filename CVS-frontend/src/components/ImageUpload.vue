<template>
  <div class="image-upload">
    <el-upload
      ref="uploadRef"
      :action="uploadAction"
      :headers="uploadHeaders"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :disabled="uploading"
      accept="image/*"
      drag
    >
      <div class="upload-content">
        <div v-if="!imageUrl && !uploading" class="upload-placeholder">
          <el-icon class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">
            <p>点击或拖拽图片到此处上传</p>
            <p class="upload-tip">支持 JPG、PNG、GIF 格式，文件大小不超过 5MB</p>
          </div>
        </div>
        
        <div v-else-if="uploading" class="upload-loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <p>上传中...</p>
        </div>
        
        <div v-else class="image-preview">
          <img 
            :src="imageUrl" 
            :alt="fileName" 
            class="preview-image" 
            @error="handleImageError"
            @load="handleImageLoad" />
          <div class="image-overlay">
            <div class="overlay-actions">
              <el-button 
                type="primary" 
                size="small" 
                circle
                @click.stop="previewImage"
              >
                <el-icon><ZoomIn /></el-icon>
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                circle
                @click.stop="removeImage"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-upload>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="60%" center>
      <div class="preview-dialog">
        <img :src="imageUrl" :alt="fileName" class="preview-dialog-image" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Loading, ZoomIn, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const uploadRef = ref()
const uploading = ref(false)
const previewVisible = ref(false)
const fileName = ref('')

const imageUrl = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
    emit('change', value)
  }
})

const uploadAction = computed(() => {
  return import.meta.env.VITE_API_BASE_URL + '/files/upload/image'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 上传前验证
const beforeUpload = (file) => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }

  // 检查文件大小
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }

  uploading.value = true
  fileName.value = file.name
  return true
}

// 上传成功
const handleSuccess = (response) => {
  uploading.value = false
  console.log('上传响应:', response)
  if (response.code === 200) {
    imageUrl.value = response.data.fileUrl
    fileName.value = response.data.originalFileName
    console.log('图片URL:', response.data.fileUrl)
    ElMessage.success('上传成功')
  } else {
    console.error('上传失败:', response)
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleError = (error) => {
  uploading.value = false
  console.error('上传失败:', error)
  ElMessage.error('上传失败，请重试')
}

// 预览图片
const previewImage = () => {
  previewVisible.value = true
}

// 删除图片
const removeImage = () => {
  imageUrl.value = ''
  fileName.value = ''
}

// 图片加载成功
const handleImageLoad = () => {
  console.log('图片加载成功:', imageUrl.value)
}

// 图片加载失败
const handleImageError = (event) => {
  console.error('图片加载失败:', imageUrl.value)
  ElMessage.error('图片加载失败，请检查图片路径')
}

// 手动触发上传
const triggerUpload = () => {
  uploadRef.value?.$el.querySelector('input').click()
}

defineExpose({
  triggerUpload
})
</script>

<style lang="scss" scoped>
.image-upload {
  :deep(.el-upload) {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #409eff;
    }
    
    &.is-dragover {
      border-color: #409eff;
      background-color: rgba(64, 158, 255, 0.06);
    }
  }
  
  :deep(.el-upload-dragger) {
    background-color: transparent;
    border: none;
    width: 100%;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .upload-content {
    width: 100%;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  
  .upload-placeholder {
    text-align: center;
    color: #8c939d;
    
    .upload-icon {
      font-size: 48px;
      color: #c0c4cc;
      margin-bottom: 16px;
    }
    
    .upload-text {
      p {
        margin: 0;
        line-height: 1.5;
        
        &.upload-tip {
          font-size: 12px;
          color: #a8abb2;
          margin-top: 4px;
        }
      }
    }
  }
  
  .upload-loading {
    text-align: center;
    color: #409eff;
    
    .loading-icon {
      font-size: 32px;
      animation: rotate 2s linear infinite;
      margin-bottom: 8px;
    }
    
    p {
      margin: 0;
      font-size: 14px;
    }
  }
  
  .image-preview {
    width: 100%;
    height: 100%;
    position: relative;
    
    .preview-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 6px;
    }
    
    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;
      border-radius: 6px;
      
      &:hover {
        opacity: 1;
      }
      
      .overlay-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .preview-dialog {
    text-align: center;
    
    .preview-dialog-image {
      max-width: 100%;
      max-height: 60vh;
      object-fit: contain;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>