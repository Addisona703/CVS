<template>
  <view class="image-upload">
    <view class="upload-list">
      <view 
        v-for="(image, index) in imageList" 
        :key="index" 
        class="upload-item"
      >
        <image :src="image.url" mode="aspectFill" class="upload-image" />
        <view class="upload-mask">
          <view class="mask-actions">
            <text class="action-icon" @click="handlePreview(index)">👁️</text>
            <text class="action-icon" @click="handleDelete(index)">🗑️</text>
          </view>
        </view>
        <view v-if="image.uploading" class="upload-progress">
          <view class="progress-bar" :style="{ width: image.progress + '%' }"></view>
        </view>
      </view>
      
      <view 
        v-if="imageList.length < maxCount" 
        class="upload-item upload-btn"
        @click="handleChoose"
      >
        <text class="upload-icon">📷</text>
        <text class="upload-text">上传图片</text>
      </view>
    </view>
    
    <view v-if="tip" class="upload-tip">{{ tip }}</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  maxCount: {
    type: Number,
    default: 9
  },
  maxSize: {
    type: Number,
    default: 5 * 1024 * 1024 // 5MB
  },
  compress: {
    type: Boolean,
    default: true
  },
  quality: {
    type: Number,
    default: 80
  },
  tip: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const imageList = ref(props.modelValue.map(url => ({ url, uploading: false, progress: 0 })))

const handleChoose = async () => {
  try {
    const res = await uni.chooseImage({
      count: props.maxCount - imageList.value.length,
      sizeType: props.compress ? ['compressed'] : ['original'],
      sourceType: ['album', 'camera']
    })
    
    for (const file of res.tempFilePaths) {
      const fileInfo = await uni.getFileInfo({ filePath: file })
      
      if (fileInfo.size > props.maxSize) {
        uni.showToast({
          title: '图片大小超过限制',
          icon: 'none'
        })
        continue
      }
      
      const image = {
        url: file,
        uploading: true,
        progress: 0
      }
      
      imageList.value.push(image)
      
      // 模拟上传进度
      const progressInterval = setInterval(() => {
        if (image.progress < 90) {
          image.progress += 10
        }
      }, 100)
      
      // 这里应该调用实际的上传API
      // const uploadRes = await uploadImage(file)
      
      setTimeout(() => {
        clearInterval(progressInterval)
        image.uploading = false
        image.progress = 100
        updateValue()
      }, 1000)
    }
  } catch (error) {
    console.error('选择图片失败:', error)
  }
}

const handlePreview = (index) => {
  const urls = imageList.value.map(item => item.url)
  uni.previewImage({
    urls,
    current: index
  })
}

const handleDelete = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这张图片吗？',
    success: (res) => {
      if (res.confirm) {
        imageList.value.splice(index, 1)
        updateValue()
      }
    }
  })
}

const updateValue = () => {
  const urls = imageList.value.filter(item => !item.uploading).map(item => item.url)
  emit('update:modelValue', urls)
  emit('change', urls)
}
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.upload-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 8rpx;
  overflow: hidden;
}

.upload-image {
  width: 100%;
  height: 100%;
}

.upload-mask {
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
  transition: opacity 0.3s;
}

.upload-item:active .upload-mask {
  opacity: 1;
}

.mask-actions {
  display: flex;
  gap: 32rpx;
}

.action-icon {
  font-size: 48rpx;
}

.upload-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 8rpx;
  background: rgba(0, 0, 0, 0.3);
}

.progress-bar {
  height: 100%;
  background: #0052d9;
  transition: width 0.3s;
}

.upload-btn {
  border: 2rpx dashed #d9d9d9;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 64rpx;
  margin-bottom: 8rpx;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
}

.upload-tip {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #999;
  line-height: 1.6;
}
</style>
