import { ref, onUnmounted } from 'vue'

export function useCountdown(initialTime = 60) {
  const countdown = ref(0)
  const isRunning = ref(false)
  let timer = null

  const start = (time = initialTime) => {
    if (isRunning.value) return
    
    countdown.value = time
    isRunning.value = true
    
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        stop()
      }
    }, 1000)
  }

  const stop = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    countdown.value = 0
    isRunning.value = false
  }

  const reset = () => {
    stop()
    countdown.value = 0
  }

  // 组件卸载时清理定时器
  onUnmounted(() => {
    stop()
  })

  return {
    countdown,
    isRunning,
    start,
    stop,
    reset
  }
}
