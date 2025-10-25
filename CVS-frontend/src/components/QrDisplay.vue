<template>
  <div class="qr-display">
    <div v-if="value" class="qr-wrapper">
      <canvas ref="canvasRef" :width="size" :height="size" />
      <div v-if="caption" class="qr-caption">{{ caption }}</div>
      <div v-if="remainingLabel" class="qr-countdown">
        <el-icon><Timer /></el-icon>
        <span>{{ remainingLabel }}</span>
      </div>
    </div>
    <el-empty v-else description="暂无二维码" :image-size="size * 0.6" />
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Timer } from '@element-plus/icons-vue'
import QRCode from 'qrcode'

const props = defineProps({
  value: {
    type: String,
    default: ''
  },
  size: {
    type: Number,
    default: 220
  },
  caption: {
    type: String,
    default: ''
  },
  expiresAt: {
    type: Number,
    default: 0
  }
})

const canvasRef = ref(null)
const remainingSeconds = ref(0)
let timer = null

const renderQr = async () => {
  if (!canvasRef.value || !props.value) return
  try {
    await QRCode.toCanvas(canvasRef.value, props.value, {
      margin: 1,
      width: props.size,
      errorCorrectionLevel: 'M'
    })
  } catch (error) {
    console.error('二维码渲染失败:', error)
    ElMessage.error('二维码生成失败，请稍后重试')
  }
}

const updateRemaining = () => {
  if (!props.expiresAt) {
    remainingSeconds.value = 0
    return
  }
  const diff = Math.max(0, Math.round((props.expiresAt - Date.now()) / 1000))
  remainingSeconds.value = diff
  if (diff === 0 && timer) {
    clearInterval(timer)
    timer = null
  }
}

const startTimer = () => {
  if (timer) clearInterval(timer)
  updateRemaining()
  if (props.expiresAt > Date.now()) {
    timer = setInterval(updateRemaining, 1000)
  }
}

watch(
  () => props.value,
  async (newValue) => {
    if (newValue) {
      await nextTick()
      renderQr()
    }
  },
  { immediate: true }
)

watch(
  () => props.expiresAt,
  () => {
    startTimer()
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})

const remainingLabel = computed(() => {
  if (!remainingSeconds.value) return ''
  const minutes = String(Math.floor(remainingSeconds.value / 60)).padStart(2, '0')
  const seconds = String(remainingSeconds.value % 60).padStart(2, '0')
  return `剩余时间 ${minutes}:${seconds}`
})
</script>

<style lang="scss" scoped>
.qr-display {
  width: 100%;
  display: flex;
  justify-content: center;

  .qr-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--space-3, 12px);
    padding: var(--space-3, 12px);
    background: var(--bg-color-overlay, rgba(255, 255, 255, 0.9));
    border-radius: var(--border-radius-large, 20px);
    box-shadow: var(--qr-shadow, 0 8px 24px rgba(0, 0, 0, 0.12));
    transition: all var(--transition-duration, 0.3s) var(--transition-timing, ease);

    canvas {
      border-radius: var(--border-radius-medium, 16px);
      background: var(--bg-color-base, #ffffff);
    }

    .qr-caption {
      font-weight: 600;
      color: var(--text-color-primary, #303133);
      letter-spacing: 0.02em;
    }

    .qr-countdown {
      display: inline-flex;
      align-items: center;
      gap: var(--space-1-5, 6px);
      font-size: var(--font-size-base, 14px);
      color: var(--text-color-regular, #606266);

      :deep(svg) {
        font-size: var(--icon-size-small, 16px);
      }
    }
  }
}
</style>

