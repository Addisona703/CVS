<template>
  <el-card class="participants-card section-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>参与者列表</span>
        <el-tag type="info" v-if="participants.length">共 {{ participants.length }} 人</el-tag>
      </div>
    </template>
    <el-table :data="participants" stripe v-loading="loading">
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="username" label="学号" />
      <el-table-column prop="createdAt" label="报名时间">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && participants.length === 0" description="暂无参与者" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { signupAPI } from '@/api'

const props = defineProps({
  activityId: {
    type: [String, Number],
    required: true
  }
})

const participants = ref([])
const loading = ref(false)

const getStatusLabel = (status) => {
  return STATUS_LABELS[status] || status
}

const getStatusType = (status) => {
  const colorMap = {
    success: 'success',
    danger: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return colorMap[STATUS_COLORS[status]] || 'info'
}

const fetchParticipants = async () => {
  loading.value = true
  try {
    const response = await signupAPI.getActivitySignups(props.activityId, {
      pageNum: 1,
      pageSize: 100,
      params: {
        status: 'APPROVED'
      }
    })
    if (response.code === 200) {
      participants.value = response.data.records || []
    }
  } catch (err) {
    console.error('获取参与者列表失败:', err)
    participants.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchParticipants()
})
</script>

<style lang="scss" scoped>
.participants-card {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 600;
    color: #1f2532;

    :deep(.el-tag) {
      border-radius: 999px;
    }
  }

  :deep(.el-card__header) {
    padding: 20px 24px;
  }

  :deep(.el-tag) {
    border-radius: 999px;
  }

  :deep(.el-table) {
    border-radius: 12px;
    overflow: hidden;
  }

  :deep(.el-table__header th) {
    background: #f5f7fa;
    color: #4e5969;
    font-weight: 600;
  }
}

.section-card {
  border-radius: 14px;
  border: 1px solid #e5e6eb;
  background: #fff;
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    box-shadow: 0 14px 32px rgba(15, 31, 64, 0.08);
    transform: translateY(-2px);
  }

  :deep(.el-card__header) {
    padding: 18px 24px;
    border-bottom: 1px solid #eef0f6;
    background: linear-gradient(180deg, #fafbff 0%, #ffffff 100%);
  }

  :deep(.el-card__body) {
    padding: 24px;
  }
}
</style>
