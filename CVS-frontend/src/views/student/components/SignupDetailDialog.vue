<template>
  <el-dialog
    :model-value="visible"
    title="报名详情"
    width="520px"
    @update:modelValue="handleVisibleChange"
  >
    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <div v-if="mergedDetail" class="detail-body">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="活动名称">
            {{ mergedDetail.activityTitle || '未提供' }}
          </el-descriptions-item>
          <el-descriptions-item label="报名时间">
            {{ formatDisplayTime(mergedDetail.createdAt || mergedDetail.signupTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="报名状态">
            <el-tag :type="statusTagType(mergedDetail.status)">
              {{ getStatusLabel(mergedDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="mergedDetail.reason" label="报名原因">
            {{ mergedDetail.reason }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="mergedDetail.status === 'REJECTED' && mergedDetail.rejectReason"
            label="拒绝原因"
          >
            <span class="reject-text">{{ mergedDetail.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="mergedDetail.signedIn" label="签到时间">
            {{ formatDisplayTime(mergedDetail.signInTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="mergedDetail.signedOut" label="签退时间">
            {{ formatDisplayTime(mergedDetail.signOutTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="暂无报名详情" />
    </template>
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { signupAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  signup: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(props.modelValue)
const loading = ref(false)
const detail = ref(null)
let lastFetchedId = null

const handleVisibleChange = (val) => {
  visible.value = val
}

const handleClose = () => {
  visible.value = false
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      fetchSignupDetail()
    } else {
      detail.value = null
      lastFetchedId = null
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

watch(
  () => props.signup?.id,
  () => {
    if (visible.value) {
      fetchSignupDetail()
    } else {
      detail.value = null
      lastFetchedId = null
    }
  }
)

const mergedDetail = computed(() => {
  if (!props.signup && !detail.value) {
    return null
  }
  return {
    ...props.signup,
    ...detail.value
  }
})

const getStatusLabel = (status) => {
  return STATUS_LABELS[status] || status || '未知状态'
}

const statusTagType = (status) => {
  const colorKey = STATUS_COLORS[status]
  const tagMap = {
    success: 'success',
    warning: 'warning',
    danger: 'danger',
    info: 'info'
  }
  return tagMap[colorKey] || 'info'
}

const formatDisplayTime = (value) => {
  if (!value) {
    return '—'
  }
  return formatDateTime(value)
}

const fetchSignupDetail = async () => {
  const signupId = props.signup?.id
  if (!signupId || signupId === lastFetchedId) {
    return
  }
  loading.value = true
  try {
    const response = await signupAPI.getSignupById(signupId)
    if (response.code === 200) {
      detail.value = response.data
      lastFetchedId = signupId
    } else {
      ElMessage.error(response.message || '获取报名详情失败')
      detail.value = null
    }
  } catch (error) {
    console.error('获取报名详情失败:', error)
    ElMessage.error('获取报名详情失败，请稍后重试')
    detail.value = null
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.detail-body {
  padding: 8px 0;
}

.reject-text {
  color: #f56c6c;
}
</style>
