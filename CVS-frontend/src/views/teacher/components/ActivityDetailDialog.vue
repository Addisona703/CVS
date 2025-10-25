<template>
  <el-dialog
    :model-value="visible"
    title="活动详情"
    width="600px"
    @update:modelValue="handleVisibleChange"
  >
    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <div v-if="activityDetail" class="activity-detail">
        <header class="detail-header">
          <div class="title-wrapper">
            <h3 class="detail-title">{{ activityDetail.title || '未命名活动' }}</h3>
            <el-tag :type="statusTagType(activityDetail.status)" size="small">
              {{ getStatusLabel(activityDetail.status) }}
            </el-tag>
          </div>
        </header>

        <section class="detail-section">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="活动描述">
              {{ activityDetail.description || '暂无描述' }}
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">
              {{ formatDisplayDate(activityDetail.startTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="结束时间">
              {{ formatDisplayDate(activityDetail.endTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="活动地点">
              {{ activityDetail.location || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="报名人数">
              {{ (activityDetail.currentParticipants ?? 0) + '/' + (activityDetail.maxParticipants ?? 0) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDisplayDate(activityDetail.createdAt) }}
            </el-descriptions-item>
          </el-descriptions>
        </section>
      </div>
      <el-empty v-else description="暂无活动详情" />
    </template>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="showEditButton" type="primary" @click="handleEdit">
          编辑活动
        </el-button>
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { activityAPI } from '@/api'
import { formatDate } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  activityId: {
    type: [Number, String],
    default: null
  },
  activity: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const visible = ref(props.modelValue)
const loading = ref(false)
const detail = ref(null)

const currentId = computed(() => {
  if (props.activityId != null) {
    return Number(props.activityId)
  }
  return props.activity?.id ?? null
})

const mergedActivity = computed(() => {
  if (!props.activity && !detail.value) {
    return detail.value
  }
  return {
    ...props.activity,
    ...detail.value
  }
})

const activityDetail = computed(() => mergedActivity.value)

const showEditButton = computed(() => activityDetail.value?.status === 'DRAFT')

const getStatusLabel = (status) => {
  return STATUS_LABELS[status] || status || '未知状态'
}

const statusTagType = (status) => {
  const map = {
    success: 'success',
    warning: 'warning',
    danger: 'danger',
    info: 'info'
  }
  return map[STATUS_COLORS[status]] || 'info'
}

const formatDisplayDate = (value) => {
  if (!value) {
    return '—'
  }
  return formatDate(value)
}

const handleVisibleChange = (val) => {
  visible.value = val
}

const handleClose = () => {
  visible.value = false
}

const handleEdit = () => {
  if (!activityDetail.value?.id) {
    return
  }
  visible.value = false
  router.push(`/teacher/activities/create/${activityDetail.value.id}`)
}

const resetDetail = () => {
  detail.value = null
}

const fetchActivityDetail = async () => {
  const id = currentId.value
  if (!id) {
    resetDetail()
    return
  }
  if (detail.value?.id === id) {
    return
  }
  loading.value = true
  try {
    const response = await activityAPI.getActivityById(id)
    if (response.code === 200) {
      detail.value = response.data
    } else {
      ElMessage.error(response.message || '获取活动详情失败')
      resetDetail()
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败，请稍后重试')
    resetDetail()
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      fetchActivityDetail()
    } else {
      resetDetail()
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

watch(
  () => currentId.value,
  () => {
    if (visible.value) {
      fetchActivityDetail()
    } else {
      resetDetail()
    }
  }
)
</script>

<style scoped>
.activity-detail {
  padding: 4px 0;
}

.detail-header {
  margin-bottom: 16px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.detail-section {
  margin-top: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
