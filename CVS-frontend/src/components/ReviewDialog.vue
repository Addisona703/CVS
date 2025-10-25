<template>
  <el-dialog
    :model-value="visible"
    width="520px"
    destroy-on-close
    class="review-dialog"
    @close="handleClose"
  >
    <template #header>
      <div class="dialog-title">
        <el-icon><EditPen /></el-icon>
        <span>调整评价</span>
      </div>
    </template>

    <div class="review-summary">
      <div class="student-meta">
        <strong>{{ record?.studentName || '未知学生' }}</strong>
        <small>{{ record?.studentNo }}</small>
      </div>
      <div class="record-meta">
        <span>签到：{{ formatDateTime(record?.checkInTime) }}</span>
        <span>签退：{{ formatDateTime(record?.checkOutTime) }}</span>
      </div>
      <div class="student-feedback" v-if="record?.studentEvaluation">
        <el-tag type="info" size="small">学生自评</el-tag>
        <span class="feedback">{{ record.studentEvaluation }}</span>
      </div>
    </div>

    <el-form :model="form" label-width="90px" label-position="left">
      <el-form-item label="教师评分">
        <el-rate
          v-model="form.teacherRating"
          :colors="['#99ccff', '#409EFF', '#1f6bff']"
          show-score
        />
      </el-form-item>
      <el-form-item label="教师评语">
        <el-input
          v-model="form.teacherEvaluation"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 6 }"
          maxlength="500"
          show-word-limit
          placeholder="填写教师对于学生服务表现的评价..."
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="footer-actions">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">
          保存调整
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { EditPen } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  record: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'submit'])

const form = reactive({
  teacherRating: 0,
  teacherEvaluation: ''
})

const syncFromRecord = () => {
  form.teacherRating = props.record?.teacherRating ?? props.record?.studentRating ?? 0
  form.teacherEvaluation = props.record?.teacherEvaluation ?? ''
}

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      syncFromRecord()
    }
  },
  { immediate: true }
)

watch(
  () => props.record,
  () => {
    if (props.visible) {
      syncFromRecord()
    }
  }
)

const handleClose = () => {
  emit('update:visible', false)
}

const handleConfirm = () => {
  emit('submit', {
    teacherRating: form.teacherRating,
    teacherEvaluation: form.teacherEvaluation
  })
}

const formatDateTime = (value) => {
  if (!value) return '—'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.review-dialog {
  .dialog-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 600;
    font-size: 18px;
    color: #1f2a44;
  }

  .review-summary {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 16px;
    padding: 12px 14px;
    background: rgba(31, 107, 255, 0.06);
    border-radius: 14px;

    .student-meta {
      display: flex;
      align-items: baseline;
      gap: 12px;

      strong {
        font-size: 16px;
        color: #1f2a44;
      }

      small {
        color: rgba(31, 42, 68, 0.6);
      }
    }

    .record-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      font-size: 13px;
      color: rgba(31, 42, 68, 0.65);
    }

    .student-feedback {
      display: flex;
      align-items: center;
      gap: 10px;
      color: rgba(31, 42, 68, 0.8);

      .feedback {
        line-height: 1.5;
      }
    }
  }

  .footer-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>

