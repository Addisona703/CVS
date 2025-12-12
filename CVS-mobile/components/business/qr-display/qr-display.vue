<template>
  <view class="qr-display">
    <canvas 
      canvas-id="qrcode" 
      :style="{ width: canvasSize + 'px', height: canvasSize + 'px' }"
      class="qr-canvas"
    />
  </view>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import uQRCode from 'uqrcodejs'

const props = defineProps({
  data: {
    type: String,
    required: true
  },
  size: {
    type: Number,
    default: 200
  }
})

// 将 rpx 转换为 px (uni-app 中 1rpx ≈ 0.5px on most devices)
const canvasSize = computed(() => props.size)

const generateQRCode = () => {
  if (!props.data) return
  
  setTimeout(() => {
    const qr = new uQRCode()
    qr.data = props.data
    qr.size = props.size
    qr.make()
    
    const canvasContext = uni.createCanvasContext('qrcode')
    qr.canvasContext = canvasContext
    qr.drawCanvas()
  }, 100)
}

watch(() => props.data, () => {
  generateQRCode()
})

onMounted(() => {
  generateQRCode()
})
</script>

<style scoped>
.qr-display {
  display: flex;
  justify-content: center;
  align-items: center;
}

.qr-canvas {
  display: block;
}
</style>
