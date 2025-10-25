<template>
  <div class="admin-create-activity">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑活动' : '创建活动' }}</h1>
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <el-card>
      <el-form ref="activityFormRef" :model="activityForm" :rules="activityRules" label-width="120px">
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
          <el-input 
            v-model="activityForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述活动内容、目标和要求" 
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxParticipants">
              <el-input-number 
                v-model="activityForm.maxParticipants" 
                :min="1" 
                :max="1000" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="积分奖励" prop="points">
              <el-input-number 
                v-model="activityForm.points" 
                :min="0" 
                :max="100" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker 
                v-model="activityForm.startTime" 
                type="datetime" 
                placeholder="请选择开始时间" 
                style="width: 100%"
                format="YYYY-MM-DD HH:mm" 
                value-format="YYYY-MM-DD HH:mm" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker 
                v-model="activityForm.endTime" 
                type="datetime" 
                placeholder="请选择结束时间" 
                style="width: 100%"
                format="YYYY-MM-DD HH:mm" 
                value-format="YYYY-MM-DD HH:mm" 
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动要求">
          <el-input 
            v-model="activityForm.requirements" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入参与活动的要求和注意事项（可选）" 
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系方式" prop="contactInfo">
              <el-input v-model="activityForm.contactInfo" placeholder="请输入联系方式" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名截止时间" prop="registrationDeadline">
              <el-date-picker 
                v-model="activityForm.registrationDeadline" 
                type="datetime" 
                placeholder="请选择报名截止时间"
                style="width: 100%" 
                format="YYYY-MM-DD HH:mm" 
                value-format="YYYY-MM-DD HH:mm" 
              />
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
          <el-button type="info" @click="fillTestData">
            填充测试数据
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { activityAPI } from '@/api'

const router = useRouter()
const route = useRoute()

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
  callback()
}

const validateRegistrationDeadline = (rule, value, callback) => {
  if (!value) return callback()
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
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入最大参与人数', trigger: 'blur' }],
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
  points: [{ required: true, message: '请输入积分奖励', trigger: 'blur' }],
  contactInfo: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const handleSubmit = async (status) => {
  try {
    await activityFormRef.value.validate()

    if (status === 'PUBLISHED') {
      const now = new Date()
      const startTime = new Date(activityForm.startTime)
      const endTime = new Date(activityForm.endTime)
      const registrationDeadline = activityForm.registrationDeadline ? new Date(activityForm.registrationDeadline) : null

      if (startTime.getTime() - now.getTime() < -60000) {
        ElMessage.error('发布失败：活动开始时间不能早于当前时间')
        return
      }

      if (endTime <= startTime) {
        ElMessage.error('发布失败：活动结束时间必须晚于开始时间')
        return
      }

      if (registrationDeadline && registrationDeadline >= startTime) {
        ElMessage.error('发布失败：报名截止时间必须早于活动开始时间')
        return
      }

      if (!activityForm.title.trim() || !activityForm.description.trim() || 
          !activityForm.location.trim() || !activityForm.contactInfo.trim()) {
        ElMessage.error('发布失败：请填写所有必填字段')
        return
      }
    }

    submitting.value = true

    const formatDateTime = (dateTimeStr) => {
      if (!dateTimeStr) return undefined
      if (dateTimeStr.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
        return dateTimeStr
      }
      let result = dateTimeStr
      if (result.includes('T')) {
        result = result.replace('T', ' ')
      }
      if (result.length > 16) {
        result = result.substring(0, 16)
      }
      if (!result.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
        return undefined
      }
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
      status: 'DRAFT'
    }

    if (isEdit.value) {
      const updateData = { ...baseData, id: activityForm.id }
      await activityAPI.updateActivity(updateData)
      
      if (status === 'PUBLISHED') {
        const publishResponse = await activityAPI.publishActivity(activityForm.id)
        ElMessage.success(publishResponse.message || '发布成功')
      } else {
        ElMessage.success('更新成功')
      }
    } else {
      const response = await activityAPI.createActivity(baseData)
      const activityId = response.data.id
      
      if (status === 'PUBLISHED') {
        const publishResponse = await activityAPI.publishActivity(activityId)
        ElMessage.success(publishResponse.message || '发布成功')
      } else {
        ElMessage.success('保存成功')
      }
    }

    router.push('/admin/activities')
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

const fetchActivityDetail = async (id) => {
  try {
    const response = await activityAPI.getActivityById(id)
    if (response.code === 200) {
      const activity = response.data
      const formatDateTimeForInput = (dateTimeStr) => {
        if (!dateTimeStr) return ''
        if (dateTimeStr.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
          return dateTimeStr
        }
        if (dateTimeStr.includes('T')) {
          return dateTimeStr.replace('T', ' ').substring(0, 16)
        }
        return dateTimeStr
      }

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
  if (route.params.id) {
    isEdit.value = true
    fetchActivityDetail(route.params.id)
  }
})
</script>

<style lang="scss" scoped>
.admin-create-activity {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }

  .el-card {
    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  :deep(.el-form) {
    .el-form-item__label {
      font-weight: 500;
      color: #606266;
    }

    .el-button {
      min-width: 100px;
    }
  }
}
</style>
