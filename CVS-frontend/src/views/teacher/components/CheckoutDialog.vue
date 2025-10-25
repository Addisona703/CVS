<template>
  <el-dialog
    :model-value="visible"
    title="学生签退"
    width="520px"
    :close-on-click-modal="false"
    @update:modelValue="updateVisible"
    @close="handleClose"
  >
    <el-steps :active="activeStep" finish-status="success" align-center>
      <el-step title="服务评价" />
      <el-step title="服务内容" />
      <el-step title="服务评分" />
    </el-steps>

    <div class="step-content">
      <template v-if="activeStep === 0">
        <el-form :model="form" label-position="top">
          <el-form-item label="请对该学生的服务进行评价（可选）">
            <el-input
              v-model="form.evaluation"
              type="textarea"
              :rows="4"
              maxlength="300"
              show-word-limit
              placeholder="请输入对学生服务表现的评价..."
            />
          </el-form-item>
        </el-form>
      </template>

      <template v-else-if="activeStep === 1">
        <el-form :model="form" label-position="top">
          <el-form-item label="请描述本次志愿服务的具体内容" :error="errors.description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请详细描述本次志愿服务的工作内容和成果..."
            />
          </el-form-item>
        </el-form>
      </template>

      <template v-else>
        <el-form :model="form" label-position="top">
          <el-form-item label="请为该学生的服务表现评分" :error="errors.rating">
            <div class="rating-wrapper">
              <el-rate v-model="ratingValue" :max="5" />
              <span class="rating-tip">{{ ratingValue || '-' }} / 5</span>
            </div>
          </el-form-item>
        </el-form>
      </template>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" :disabled="loading">取消</el-button>
        <el-button
          type="primary"
          @click="handleNext"
          :loading="loading"
        >
          {{ activeStep === 2 ? '完成签退' : '下一步' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { signupAPI } from '@/api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  signupId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
const activeStep = ref(0)
const loading = ref(false)

const form = reactive({
  evaluation: '',
  description: '',
  rating: null
})

const errors = reactive({
  description: '',
  rating: ''
})

const ratingValue = computed({
  get: () => form.rating,
  set: (val) => {
    form.rating = val
  }
})

const resetState = () => {
  activeStep.value = 0
  form.evaluation = ''
  form.description = ''
  form.rating = null
  errors.description = ''
  errors.rating = ''
  loading.value = false
}

const updateVisible = (val) => {
  visible.value = val
}

const handleClose = () => {
  emit('update:modelValue', false)
  resetState()
}

const handleCancel = () => {
  handleClose()
}

const validateStep = () => {
  errors.description = ''
  errors.rating = ''

  if (activeStep.value === 1) {
    if (!form.description || !form.description.trim()) {
      errors.description = '请填写服务内容描述'
      return false
    }
  }

  if (activeStep.value === 2) {
    const score = Number(form.rating)
    if (!score || score < 1 || score > 5) {
      errors.rating = '请给出 1-5 的评分'
      return false
    }
  }

  return true
}

const submitCheckout = async () => {
  if (!props.signupId) {
    ElMessage.error('未找到报名记录，无法签退')
    return
  }

  loading.value = true
  try {
    await signupAPI.checkOut(props.signupId, {
      rating: Number(form.rating),
      evaluation: form.evaluation || '',
      description: form.description || ''
    })
    ElMessage.success('签退成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('签退失败:', error)
    ElMessage.error(error.response?.data?.message || '签退失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleNext = async () => {
  if (!validateStep()) {
    return
  }

  if (activeStep.value === 2) {
    await submitCheckout()
    return
  }

  activeStep.value += 1
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (!val) {
      resetState()
    }
  }
)

watch(
  () => visible.value,
  (val) => {
    emit('update:modelValue', val)
  }
)
</script>

<style scoped>
.step-content {
  margin-top: 24px;
  min-height: 150px;
}

.rating-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-tip {
  font-size: 14px;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
