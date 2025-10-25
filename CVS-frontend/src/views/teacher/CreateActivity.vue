<template>
  <div class="create-activity">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑活动' : '创建活动' }}</h1>
      <el-button @click="$router.back()">
        <el-icon>
          <ArrowLeft />
        </el-icon>
        返回
      </el-button>
    </div>

    <el-card>
      <el-form ref="activityFormRef" :model="activityForm" :rules="activityRules" label-width="120px" size="large">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="activityForm.title" placeholder="请输入活动标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="activityForm.location" placeholder="请输入活动地点" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动描述" prop="description">
          <el-input v-model="activityForm.description" type="textarea" :rows="3" placeholder="请详细描述活动内容、目标和要求" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxParticipants">
              <el-input-number v-model="activityForm.maxParticipants" :min="1" :max="1000" placeholder="请输入最大参与人数"
                style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="积分奖励" prop="points">
              <el-input-number v-model="activityForm.points" :min="0" :max="100" placeholder="请输入积分奖励"
                style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="activityForm.startTime" type="datetime" placeholder="请选择开始时间" style="width: 100%"
                format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="activityForm.endTime" type="datetime" placeholder="请选择结束时间" style="width: 100%"
                format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动要求">
          <el-input v-model="activityForm.requirements" type="textarea" :rows="3" placeholder="请输入参与活动的要求和注意事项（可选）" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系方式" prop="contactInfo">
              <el-input v-model="activityForm.contactInfo" placeholder="请输入联系方式" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名截止时间" prop="registrationDeadline">
              <el-date-picker v-model="activityForm.registrationDeadline" type="datetime" placeholder="请选择报名截止时间"
                style="width: 100%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit('DRAFT')">
            保存草稿
          </el-button>
          <el-button type="success" :loading="submitting" @click="handleSubmit('PUBLISHED')">
            发布活动
          </el-button>
          <el-button type="info" plain @click="fillTestData">
            填充测试数据
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { activityAPI } from '@/api'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 判断是否为管理员
const isAdmin = computed(() => authStore.user?.role === 'ADMIN')

const activityFormRef = ref()
const submitting = ref(false)
const isEdit = ref(false)

const activityForm = reactive({
  id: '',
  title: '',
  description: '',
  location: '',
  maxParticipants: 30,
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  points: 10,
  requirements: '',
  contactInfo: ''
})

const fillTestData = () => {
  const now = new Date()
  const pad = (num) => String(num).padStart(2, '0')
  const formatPickerValue = (date) => {
    const d = new Date(date)
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  const addMinutes = (minutes) => formatPickerValue(new Date(now.getTime() + minutes * 60000))

  activityForm.title = `测试活动 ${now.getHours()}${pad(now.getMinutes())}`
  activityForm.description = '这是自动填充的测试活动描述，用于快速验证创建流程。'
  activityForm.location = '教学楼A座101室'
  activityForm.maxParticipants = 20
  activityForm.points = 5
  activityForm.requirements = '测试要求：准时到场，保持课堂纪律。'
  activityForm.contactInfo = '张老师 13800000000'
  activityForm.registrationDeadline = addMinutes(1)
  activityForm.startTime = addMinutes(2)
  activityForm.endTime = addMinutes(4)

  activityFormRef.value?.clearValidate?.()
  ElMessage.success('已填充测试数据')
}

const validateEndTime = (rule, value, callback) => {
  if (!value || !activityForm.startTime) return callback()
  const valid = new Date(value).getTime() > new Date(activityForm.startTime).getTime()
  callback(valid ? undefined : new Error('结束时间必须晚于开始时间'))
}

const validateStartTime = (rule, value, callback) => {
  if (!value) return callback()

  const startTime = new Date(value)
  if (Number.isNaN(startTime.getTime())) {
    return callback(new Error('请选择有效的开始时间'))
  }

  const now = Date.now()
  if (startTime.getTime() - now < -60000) {
    return callback(new Error('开始时间必须晚于当前时间'))
  }

  callback()
}

const validateRegistrationDeadline = (rule, value, callback) => {
  if (!value) return callback()

  // 只验证报名截止时间必须早于活动开始时间
  if (activityForm.startTime) {
    const deadline = new Date(value).getTime()
    const startTime = new Date(activityForm.startTime).getTime()

    if (deadline >= startTime) {
      return callback(new Error('报名截止时间必须早于活动开始时间'))
    }
  }

  callback()
}

const activityRules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入活动描述', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
    { validator: validateStartTime, trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: validateEndTime, trigger: 'change' }
  ],
  registrationDeadline: [
    { required: true, message: '请选择报名截止时间', trigger: 'change' },
    { validator: validateRegistrationDeadline, trigger: 'change' }
  ],
  points: [
    { required: true, message: '请输入积分奖励', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

// 创建活动
const handleSubmit = async (status) => {
  try {
    await activityFormRef.value.validate()

    // 如果是发布活动，需要额外验证时间
    if (status === 'PUBLISHED') {
      const now = new Date()
      const startTime = new Date(activityForm.startTime)
      const endTime = new Date(activityForm.endTime)
      const registrationDeadline = activityForm.registrationDeadline ? new Date(activityForm.registrationDeadline) : null

      // 验证活动开始时间
      if (startTime.getTime() - now.getTime() < -60000) {
        ElMessage.error('发布失败：活动开始时间不能早于当前时间')
        return
      }

      // 验证活动结束时间
      if (endTime <= startTime) {
        ElMessage.error('发布失败：活动结束时间必须晚于开始时间')
        return
      }

      // 验证报名截止时间
      if (registrationDeadline && registrationDeadline >= startTime) {
        ElMessage.error('发布失败：报名截止时间必须早于活动开始时间')
        return
      }

      // 验证必填字段
      if (!activityForm.title.trim()) {
        ElMessage.error('发布失败：活动标题不能为空')
        return
      }
      if (!activityForm.description.trim()) {
        ElMessage.error('发布失败：活动描述不能为空')
        return
      }
      if (!activityForm.location.trim()) {
        ElMessage.error('发布失败：活动地点不能为空')
        return
      }
      if (!activityForm.contactInfo.trim()) {
        ElMessage.error('发布失败：联系方式不能为空')
        return
      }
    }

    submitting.value = true

    // 准备提交数据，确保时间格式严格符合后端Jackson配置：yyyy-MM-dd HH:mm
    const formatDateTime = (dateTimeStr) => {
      if (!dateTimeStr) return undefined

      console.log('原始时间值:', dateTimeStr)

      // 如果已经是正确的格式 "YYYY-MM-DD HH:mm"，直接返回
      if (dateTimeStr.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
        console.log('已经是正确格式:', dateTimeStr)
        return dateTimeStr
      }

      // 处理各种可能的输入格式
      let result = dateTimeStr

      // 移除ISO格式中的T分隔符
      if (result.includes('T')) {
        result = result.replace('T', ' ')
      }

      // 确保只保留到分钟，移除秒和毫秒
      if (result.length > 16) {
        result = result.substring(0, 16)
      }

      // 验证最终格式
      if (!result.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
        console.error('无法转换的时间格式:', dateTimeStr)
        return undefined
      }

      console.log('转换后的时间:', result)
      return result
    }

    const baseData = {
      title: activityForm.title,
      description: activityForm.description,
      location: activityForm.location,
      maxParticipants: activityForm.maxParticipants,
      startTime: formatDateTime(activityForm.startTime),
      endTime: formatDateTime(activityForm.endTime),
      registrationDeadline: formatDateTime(activityForm.registrationDeadline),
      points: activityForm.points,
      requirements: activityForm.requirements,
      contactInfo: activityForm.contactInfo,
      status: 'DRAFT' // 创建时总是草稿状态
    }

    // console.log('提交数据:', baseData)

    if (isEdit.value) {
      // 更新活动时需要包含id
      const updateData = {
        ...baseData,
        id: activityForm.id
      }
      await activityAPI.updateActivity(updateData)

      // 如果是发布操作，调用发布接口
      if (status === 'PUBLISHED') {
        const publishResponse = await activityAPI.publishActivity(activityForm.id)
        ElMessage.success(publishResponse.message || '发布成功')
      } else {
        ElMessage.success('更新成功')
      }
    } else {
      // 创建活动时不包含id
      const response = await activityAPI.createActivity(baseData)
      const activityId = response.data.id

      // 如果是发布操作，调用发布接口
      if (status === 'PUBLISHED') {
        const publishResponse = await activityAPI.publishActivity(activityId)
        ElMessage.success(publishResponse.message || '发布成功')
      } else {
        ElMessage.success('保存成功')
      }
    }

    // 根据用户角色跳转到不同页面
    if (isAdmin.value) {
      router.push('/admin/activities')
    } else {
      router.push('/teacher/activities')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  activityFormRef.value.resetFields()
}

// 获取活动草稿详情
const fetchActivityDetail = async (id) => {
  try {
    const response = await activityAPI.getActivityById(id)
    if (response.code === 200) {
      const activity = response.data

      // 根据Jackson配置，后端返回的时间格式应该已经是"yyyy-MM-dd HH:mm"格式
      // 如果不是，需要转换
      const formatDateTimeForInput = (dateTimeStr) => {
        if (!dateTimeStr) return ''
        // 如果已经是"YYYY-MM-DD HH:mm"格式，直接返回
        if (dateTimeStr.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
          return dateTimeStr
        }
        // 如果是ISO格式，转换为日期选择器格式
        if (dateTimeStr.includes('T')) {
          return dateTimeStr.replace('T', ' ').substring(0, 16)
        }
        return dateTimeStr
      }

      // 填充表单数据
      Object.assign(activityForm, {
        id: activity.id,
        title: activity.title,
        description: activity.description,
        location: activity.location,
        maxParticipants: activity.maxParticipants,
        startTime: formatDateTimeForInput(activity.startTime),
        endTime: formatDateTimeForInput(activity.endTime),
        registrationDeadline: formatDateTimeForInput(activity.registrationDeadline),
        points: activity.points,
        requirements: activity.requirements,
        contactInfo: activity.contactInfo
      })
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
    router.back()
  }
}

onMounted(() => {
  // 如果路径附带了id则代表是编辑活动，没有则是创建
  if (route.params.id) {
    isEdit.value = true
    fetchActivityDetail(route.params.id)
  }
})
</script>

<style lang="scss" scoped>
.create-activity {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--space-6, 24px);

    h1 {
      margin: 0;
      font-size: 28px;
      color: #303133;
      font-weight: 600;
    }

    .el-button {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }

  .el-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
    }

    :deep(.el-card__body) {
      padding: var(--space-6, 24px);
    }
  }

  :deep(.el-form) {
    .el-form-item__label {
      font-weight: 500;
      color: #606266;
    }

    .el-input,
    .el-textarea,
    .el-input-number,
    .el-date-picker {
      transition: all 0.3s ease;

      &:hover {
        border-color: #67c23a;
      }
    }

    .el-button {
      min-width: 100px;
      font-weight: 500;
      transition: all 0.3s ease;

      &.el-button--primary {
        background-color: #67c23a;
        border-color: #67c23a;

        &:hover {
          background-color: #85ce61;
          border-color: #85ce61;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
        }

        &:active {
          background-color: #5daf34;
          border-color: #5daf34;
          transform: translateY(0);
        }
      }

      &.el-button--success {
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
        }

        &:active {
          transform: translateY(0);
        }
      }

      &.el-button--default {
        &:hover {
          color: #67c23a;
          border-color: #67c23a;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .create-activity {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--space-4, 16px);

      .el-button {
        width: 100%;
        justify-content: center;
      }
    }

    :deep(.el-form) {
      .el-form-item {
        margin-bottom: var(--space-5, 20px);
      }

      .el-button {
        width: 100%;
        margin-bottom: var(--space-2, 8px);
      }
    }
  }
}
</style>
