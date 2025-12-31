<template>
  <view class="create-activity">
    <scroll-view class="form-scroll" scroll-y :show-scrollbar="false" enhanced>
      <view class="form-container">
      <!-- 活动标题 -->
      <view class="form-item">
        <view class="form-label required">活动标题</view>
        <input
          v-model="form.title"
          class="form-input"
          cursor-spacing="120"
          placeholder="请输入活动标题"
          maxlength="50"
        />
      </view>

      <!-- 活动描述 -->
      <view class="form-item">
        <view class="form-label required">活动描述</view>
        <textarea
          v-model="form.description"
          class="form-textarea"
          cursor-spacing="120"
          placeholder="请输入活动描述"
          maxlength="500"
        />
      </view>

      <!-- 活动地点 -->
      <view class="form-item">
        <view class="form-label required">活动地点</view>
        <input
          v-model="form.location"
          class="form-input"
          cursor-spacing="120"
          placeholder="请输入活动地点"
          maxlength="100"
        />
      </view>

      <!-- 开始时间 -->
      <view class="form-item">
        <view class="form-label required">开始时间</view>
        <picker
          mode="date"
          :value="form.startDate"
          @change="onStartDateChange"
        >
          <view class="picker-input">
            {{ form.startDate || '请选择日期' }}
          </view>
        </picker>
        <picker
          mode="time"
          :value="form.startTime"
          @change="onStartTimeChange"
        >
          <view class="picker-input">
            {{ form.startTime || '请选择时间' }}
          </view>
        </picker>
      </view>

      <!-- 结束时间 -->
      <view class="form-item">
        <view class="form-label required">结束时间</view>
        <picker
          mode="date"
          :value="form.endDate"
          @change="onEndDateChange"
        >
          <view class="picker-input">
            {{ form.endDate || '请选择日期' }}
          </view>
        </picker>
        <picker
          mode="time"
          :value="form.endTime"
          @change="onEndTimeChange"
        >
          <view class="picker-input">
            {{ form.endTime || '请选择时间' }}
          </view>
        </picker>
      </view>

      <!-- 报名截止时间 -->
      <view class="form-item">
        <view class="form-label required">报名截止时间</view>
        <picker
          mode="date"
          :value="form.registrationDeadlineDate"
          @change="onRegistrationDeadlineDateChange"
        >
          <view class="picker-input">
            {{ form.registrationDeadlineDate || '请选择日期' }}
          </view>
        </picker>
        <picker
          mode="time"
          :value="form.registrationDeadlineTime"
          @change="onRegistrationDeadlineTimeChange"
        >
          <view class="picker-input">
            {{ form.registrationDeadlineTime || '请选择时间' }}
          </view>
        </picker>
      </view>

      <!-- 最大人数 -->
      <view class="form-item">
        <view class="form-label required">最大人数</view>
        <input
          v-model.number="form.maxParticipants"
          class="form-input"
          type="number"
          cursor-spacing="120"
          placeholder="请输入最大人数"
        />
      </view>

      <!-- 积分奖励 -->
      <view class="form-item">
        <view class="form-label required">积分奖励</view>
        <input
          v-model.number="form.points"
          class="form-input"
          type="number"
          cursor-spacing="120"
          placeholder="请输入积分奖励"
        />
      </view>

      <!-- 活动要求 -->
      <view class="form-item">
      <view class="form-label">活动要求</view>
      <textarea
        v-model="form.requirements"
        class="form-textarea"
        cursor-spacing="120"
        placeholder="请输入活动要求（选填）"
        maxlength="500"
      />
    </view>
    </view>
    </scroll-view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="btn btn-secondary" @click="fillTestData" :disabled="submitting">
        填充测试数据
      </button>
      <button class="btn btn-secondary" @click="saveDraft" :disabled="submitting">
        保存草稿
      </button>
      <button class="btn btn-primary" @click="publish" :disabled="submitting">
        {{ isEdit ? '更新并发布' : '发布活动' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { createActivity, updateActivity, getActivityById, publishActivity } from '@/api/activity'
import { useForm } from '@/composables/useForm'

const isEdit = ref(false)
const activityId = ref(null)
const submitting = ref(false)

const form = ref({
  title: '',
  description: '',
  location: '',
  startDate: '',
  startTime: '',
  endDate: '',
  endTime: '',
  registrationDeadlineDate: '',
  registrationDeadlineTime: '',
  maxParticipants: null,
  points: null,
  requirements: ''
})

// 后端期望格式：yyyy-MM-dd HH:mm（与前台保持一致），不带秒
const joinDateTime = (date, time) => `${date} ${time}`
const parseDateTime = (date, time) => new Date(`${date}T${time}:00`)

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  
  if (options.id) {
    isEdit.value = true
    activityId.value = options.id
    loadActivity()
  }
})

const loadActivity = async () => {
  try {
    const activity = await getActivityById(activityId.value)
    
    // 解析日期时间
    const startDateTime = new Date(activity.startTime)
    const endDateTime = new Date(activity.endTime)
    const registrationDeadlineDateTime = new Date(activity.registrationDeadline)
    
    form.value = {
      title: activity.title,
      description: activity.description,
      location: activity.location,
      startDate: formatDate(startDateTime),
      startTime: formatTime(startDateTime),
      endDate: formatDate(endDateTime),
      endTime: formatTime(endDateTime),
      registrationDeadlineDate: formatDate(registrationDeadlineDateTime),
      registrationDeadlineTime: formatTime(registrationDeadlineDateTime),
      maxParticipants: activity.maxParticipants,
      points: activity.points,
      requirements: activity.requirements || ''
    }
  } catch (error) {
    console.error('加载活动失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatTime = (date) => {
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const onStartDateChange = (e) => {
  form.value.startDate = e.detail.value
}

const onStartTimeChange = (e) => {
  form.value.startTime = e.detail.value
}

const onEndDateChange = (e) => {
  form.value.endDate = e.detail.value
}

const onEndTimeChange = (e) => {
  form.value.endTime = e.detail.value
}

const onRegistrationDeadlineDateChange = (e) => {
  form.value.registrationDeadlineDate = e.detail.value
}

const onRegistrationDeadlineTimeChange = (e) => {
  form.value.registrationDeadlineTime = e.detail.value
}

const validateForm = () => {
  if (!form.value.title?.trim()) {
    uni.showToast({ title: '请输入活动标题', icon: 'none' })
    return false
  }
  if (!form.value.description?.trim()) {
    uni.showToast({ title: '请输入活动描述', icon: 'none' })
    return false
  }
  if (!form.value.location?.trim()) {
    uni.showToast({ title: '请输入活动地点', icon: 'none' })
    return false
  }
  if (!form.value.startDate || !form.value.startTime) {
    uni.showToast({ title: '请选择开始时间', icon: 'none' })
    return false
  }
  if (!form.value.endDate || !form.value.endTime) {
    uni.showToast({ title: '请选择结束时间', icon: 'none' })
    return false
  }
  if (!form.value.registrationDeadlineDate || !form.value.registrationDeadlineTime) {
    uni.showToast({ title: '请选择报名截止时间', icon: 'none' })
    return false
  }
  if (!form.value.maxParticipants || form.value.maxParticipants <= 0) {
    uni.showToast({ title: '请输入有效的最大人数', icon: 'none' })
    return false
  }
  if (!form.value.points || form.value.points < 0) {
    uni.showToast({ title: '请输入有效的积分奖励', icon: 'none' })
    return false
  }

  // 验证时间逻辑
  const startTime = parseDateTime(form.value.startDate, form.value.startTime)
  const endTime = parseDateTime(form.value.endDate, form.value.endTime)
  const registrationDeadline = parseDateTime(form.value.registrationDeadlineDate, form.value.registrationDeadlineTime)

  if (endTime <= startTime) {
    uni.showToast({ title: '结束时间必须晚于开始时间', icon: 'none' })
    return false
  }
  if (registrationDeadline >= startTime) {
    uni.showToast({ title: '报名截止时间必须早于开始时间', icon: 'none' })
    return false
  }

  return true
}

const fillTestData = () => {
  const now = new Date()
  const pad = (num) => String(num).padStart(2, '0')
  const formatPickerValue = (date) => {
    const d = new Date(date)
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  const addMinutes = (minutes) => formatPickerValue(new Date(now.getTime() + minutes * 60000))

  const titleSuffix = `${pad(now.getHours())}${pad(now.getMinutes())}`
  form.value.title = `测试活动 ${titleSuffix}`
  form.value.description = '这是自动填充的测试活动描述，用于快速验证创建流程。'
  form.value.location = '教学楼A座101室'
  form.value.maxParticipants = 20
  form.value.points = 5
  form.value.requirements = '测试要求：准时到场，保持课堂纪律。'
  form.value.contactInfo = '张老师 13800000000'

  const deadline = addMinutes(2)
  const start = addMinutes(3)
  const end = addMinutes(5)

  form.value.registrationDeadlineDate = deadline.split(' ')[0]
  form.value.registrationDeadlineTime = deadline.split(' ')[1]
  form.value.startDate = start.split(' ')[0]
  form.value.startTime = start.split(' ')[1]
  form.value.endDate = end.split(' ')[0]
  form.value.endTime = end.split(' ')[1]

  uni.showToast({ title: '已填充测试数据', icon: 'success' })
}

const buildActivityData = () => {
  return {
    title: form.value.title.trim(),
    description: form.value.description.trim(),
    location: form.value.location.trim(),
    startTime: joinDateTime(form.value.startDate, form.value.startTime),
    endTime: joinDateTime(form.value.endDate, form.value.endTime),
    registrationDeadline: joinDateTime(form.value.registrationDeadlineDate, form.value.registrationDeadlineTime),
    maxParticipants: form.value.maxParticipants,
    points: form.value.points,
    requirements: form.value.requirements?.trim() || null,
    contactInfo: form.value.contactInfo?.trim() || ' '
  }
}

const saveDraft = async () => {
  if (!validateForm()) return

  submitting.value = true
  try {
    const data = {
      ...buildActivityData(),
      status: 'DRAFT'
    }

    if (isEdit.value) {
      await updateActivity({ id: activityId.value, ...data })
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await createActivity(data)
      uni.showToast({ title: '保存成功', icon: 'success' })
    }

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('保存失败:', error)
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const publish = async () => {
  if (!validateForm()) return

  submitting.value = true
  try {
    const data = buildActivityData()

    if (isEdit.value) {
      await updateActivity({ id: activityId.value, ...data })
      await publishActivity(activityId.value)
      uni.showToast({ title: '更新并发布成功', icon: 'success' })
    } else {
      const result = await createActivity(data)
      await publishActivity(result.id)
      uni.showToast({ title: '发布成功', icon: 'success' })
    }

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('发布失败:', error)
    uni.showToast({ title: '发布失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.create-activity {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 220rpx; /* 留足空间避免被底部按钮遮挡 */
}

.form-scroll {
  max-height: calc(100vh - 160rpx);
}

.form-container {
  background: white;
  padding: 32rpx;
  box-sizing: border-box;
  padding-bottom: 40rpx; /* 额外留白，避免遮挡尾部字段 */
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;

  &.required::before {
    content: '*';
    color: #ff4d4f;
    margin-right: 4rpx;
  }
}

.form-input,
.form-textarea,
.picker-input {
  width: 100%;
  height: 40px;
  padding: 0rpx 24rpx; /* 增加垂直内边距，点击更容易 */
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
  background: white;
}

.form-textarea {
  min-height: 200rpx;
}

.picker-input {
  margin-bottom: 16rpx;
  color: #666;

  &:last-child {
    margin-bottom: 0;
  }
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: white;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.btn {
  flex: 1;
  height: 88rpx;
  border-radius: 8rpx;
  font-size: 32rpx;
  border: none;

  &.btn-secondary {
    background: #f5f5f5;
    color: #666;
  }

  &.btn-primary {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    color: white;
  }

  &:disabled {
    opacity: 0.6;
  }
}
</style>
