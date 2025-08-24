<template>
  <div class="create-activity">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑活动' : '创建活动' }}</h1>
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    
    <el-card>
      <el-form
        ref="activityFormRef"
        :model="activityForm"
        :rules="activityRules"
        label-width="120px"
        size="large"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="activityForm.title" placeholder="请输入活动标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动类型" prop="type">
              <el-select v-model="activityForm.type" placeholder="请选择活动类型">
                <el-option label="环保公益" value="ENVIRONMENTAL" />
                <el-option label="社区服务" value="COMMUNITY" />
                <el-option label="教育支教" value="EDUCATION" />
                <el-option label="敬老助残" value="ELDERLY_CARE" />
                <el-option label="其他" value="OTHER" />
              </el-select>
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
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="activityForm.location" placeholder="请输入活动地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxParticipants">
              <el-input-number
                v-model="activityForm.maxParticipants"
                :min="1"
                :max="1000"
                placeholder="请输入最大参与人数"
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
                value-format="YYYY-MM-DD HH:mm:ss"
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
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报名截止时间" prop="registrationDeadline">
              <el-date-picker
                v-model="activityForm.registrationDeadline"
                type="datetime"
                placeholder="请选择报名截止时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="积分奖励" prop="points">
              <el-input-number
                v-model="activityForm.points"
                :min="0"
                :max="100"
                placeholder="请输入积分奖励"
                style="width: 100%"
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
        
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="activityForm.contactInfo" placeholder="请输入联系方式" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit('DRAFT')">
            保存草稿
          </el-button>
          <el-button type="success" :loading="submitting" @click="handleSubmit('PUBLISHED')">
            发布活动
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
  title: '',
  type: '',
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

const validateEndTime = (rule, value, callback) => {
  if (value && activityForm.startTime && new Date(value) <= new Date(activityForm.startTime)) {
    callback(new Error('结束时间必须晚于开始时间'))
  } else {
    callback()
  }
}

const validateRegistrationDeadline = (rule, value, callback) => {
  if (value && activityForm.startTime && new Date(value) >= new Date(activityForm.startTime)) {
    callback(new Error('报名截止时间必须早于活动开始时间'))
  } else {
    callback()
  }
}

const activityRules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择活动类型', trigger: 'change' }
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
    { required: true, message: '请选择开始时间', trigger: 'change' }
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
    submitting.value = true
    
    const submitData = {
      ...activityForm,
      status
    }
    
    if (isEdit.value) {
      await activityAPI.updateActivity(route.params.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await activityAPI.createActivity(submitData)
      ElMessage.success(status === 'DRAFT' ? '保存成功' : '发布成功')
    }
    
    router.push('/teacher/activities')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  activityFormRef.value.resetFields()
}

// 获取活动详情
const fetchActivityDetail = async (id) => {
  try {
    const response = await activityAPI.getActivityById(id)
    if (response.code === 200) {
      Object.assign(activityForm, response.data)
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
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
}
</style>
