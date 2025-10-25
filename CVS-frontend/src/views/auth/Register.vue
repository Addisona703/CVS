<template>
  <div class="register-stepper">
    <t-steps :current="currentStep - 1" status="process" class="stepper-track">
      <t-step-item title="账户信息" />
      <t-step-item title="个人信息" />
      <t-step-item title="验证完成" />
    </t-steps>

    <div class="stepper-panel">
      <header class="stepper-header">
        <p class="stepper-subtitle">{{ currentStepDescription }}</p>
      </header>

      <t-form ref="registerFormRef" :data="form" :rules="formRules" :colon="false" :label-width="0" class="stepper-form">
        <template v-if="currentStep === 1">
          <t-form-item name="username">
            <t-input v-model="form.username" placeholder="用户名/学号/工号" clearable size="large">
              <template #prefix-icon>
                <t-icon name="user" />
              </template>
            </t-input>
          </t-form-item>

          <t-form-item name="password">
            <t-input v-model="form.password" type="password" placeholder="设置密码" clearable size="large">
              <template #prefix-icon>
                <t-icon name="lock-on" />
              </template>
            </t-input>
          </t-form-item>

          <t-form-item name="confirmPassword">
            <t-input v-model="form.confirmPassword" type="password" placeholder="确认密码" clearable size="large">
              <template #prefix-icon>
                <t-icon name="lock-on" />
              </template>
            </t-input>
          </t-form-item>
        </template>

        <template v-else-if="currentStep === 2">
          <t-form-item name="name">
            <t-input v-model="form.name" placeholder="真实姓名" clearable size="large">
              <template #prefix-icon>
                <t-icon name="user-avatar" />
              </template>
            </t-input>
          </t-form-item>

          <t-form-item name="role">
            <t-select v-model="form.role" placeholder="选择角色" clearable size="large">
              <t-option label="学生" value="STUDENT">
                <div class="option-content">
                  <t-icon name="user" />
                  <span>学生</span>
                </div>
              </t-option>
              <t-option label="教师" value="TEACHER">
                <div class="option-content">
                  <t-icon name="user-setting" />
                  <span>教师</span>
                </div>
              </t-option>
            </t-select>
          </t-form-item>

          <t-form-item name="phone">
            <t-input v-model="form.phone" placeholder="手机号码" clearable size="large">
              <template #prefix-icon>
                <t-icon name="mobile" />
              </template>
            </t-input>
          </t-form-item>
        </template>

        <template v-else>
          <t-form-item name="email">
            <t-input v-model="form.email" placeholder="邮箱地址" clearable size="large">
              <template #prefix-icon>
                <t-icon name="mail" />
              </template>
            </t-input>
          </t-form-item>

          <t-form-item name="verificationCode">
            <t-input v-model="form.verificationCode" placeholder="邮箱验证码" maxlength="6" clearable size="large">
              <template #prefix-icon>
                <t-icon name="secured" />
              </template>
              <template #suffix>
                <t-button
                  theme="primary"
                  variant="text"
                  :loading="sendCodeLoading"
                  :disabled="countdown > 0 || !emailValid"
                  @click="handleSendCode"
                  class="code-btn"
                >
                  <span v-if="countdown > 0">{{ countdown }}s</span>
                  <span v-else>获取验证码</span>
                </t-button>
              </template>
            </t-input>
          </t-form-item>

          <t-form-item name="agreement" class="agreement-item">
            <t-checkbox v-model="form.agreement">
              我已阅读并同意
              <t-link theme="primary" hover="color" class="agreement-link">用户协议</t-link>
              和
              <t-link theme="primary" hover="color" class="agreement-link">隐私政策</t-link>
            </t-checkbox>
          </t-form-item>
        </template>

        <div class="form-actions">
          <t-button variant="outline" size="large" class="ghost-btn" @click="onGhostClick">
            {{ ghostLabel }}
          </t-button>
          <t-button theme="primary" size="large" class="primary-btn" :loading="loading" @click="onPrimaryClick">
            {{ primaryLabel }}
          </t-button>
        </div>
      </t-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { authAPI } from '@/api'

const router = useRouter()
const registerFormRef = ref()
const currentStep = ref(1)
const loading = ref(false)
const sendCodeLoading = ref(false)
const countdown = ref(0)
let countdownTimer = null

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  role: '',
  phone: '',
  email: '',
  verificationCode: '',
  agreement: false
})

const stepMeta = [
  { description: '设置账户凭证以开始使用平台。' },
  { description: '完善个人身份信息以便后续认证。' },
  { description: '完成邮箱验证并确认服务协议。' }
]

const currentStepDescription = computed(() => stepMeta[currentStep.value - 1].description)
const ghostLabel = computed(() => (currentStep.value === 1 ? '返回登录' : '上一步'))
const primaryLabel = computed(() => (currentStep.value === 3 ? '完成注册' : '下一步'))

const emailValid = computed(() => {
  if (!form.email) return false
  return /^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/.test(form.email)
})

const validateConfirmPassword = (val) => {
  if (val !== form.password) {
    return { result: false, message: '两次输入的密码不一致', type: 'error' }
  }
  return { result: true }
}

const formRules = {
  username: [
    { required: true, message: '请输入用户名/学号/工号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度需在3到20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  agreement: [
    { validator: (val) => val === true ? { result: true } : { result: false, message: '请阅读并同意用户协议', type: 'error' }, trigger: 'change' }
  ]
}

const resetCountdown = () => {
  countdown.value = 0
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const handleSendCode = async () => {
  if (countdown.value > 0 || sendCodeLoading.value) return
  if (!emailValid.value) {
    MessagePlugin.warning('请先输入正确的邮箱地址')
    return
  }

  try {
    sendCodeLoading.value = true
    await authAPI.sendCode({
      email: form.email.trim(),
      type: 'register'
    })
    MessagePlugin.success('验证码已发送，请查收邮箱')
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        resetCountdown()
      }
    }, 1000)
  } catch (error) {
    console.error('验证码发送失败:', error)
    MessagePlugin.error(error.response?.data?.message || '发送失败，请稍后重试')
  } finally {
    sendCodeLoading.value = false
  }
}

const stepFields = {
  1: ['username', 'password', 'confirmPassword'],
  2: ['name', 'role', 'phone'],
  3: ['email', 'verificationCode', 'agreement']
}

const validateCurrentStep = async () => {
  if (!registerFormRef.value) return false
  const fields = stepFields[currentStep.value]
  const result = await registerFormRef.value.validate({ fields })
  return result === true
}

const onGhostClick = () => {
  if (currentStep.value === 1) {
    router.push('/auth/login')
  } else {
    currentStep.value -= 1
  }
}

const onPrimaryClick = async () => {
  const valid = await validateCurrentStep()
  if (!valid) return

  if (currentStep.value < 3) {
    currentStep.value += 1
  } else {
    await handleSubmit()
  }
}

const handleSubmit = async () => {
  try {
    loading.value = true
    const payload = {
      username: form.username.trim(),
      password: form.password,
      name: form.name.trim(),
      role: form.role,
      phone: form.phone.trim(),
      email: form.email.trim(),
      verificationCode: form.verificationCode.trim()
    }

    const response = await authAPI.register(payload)
    if (response.code === 200) {
      MessagePlugin.success('注册成功，请登录')
      resetCountdown()
      router.push('/auth/login')
    } else {
      throw new Error(response.message || '注册失败，请稍后重试')
    }
  } catch (error) {
    console.error('注册提交失败:', error)
    MessagePlugin.error(error.response?.data?.message || error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  resetCountdown()
})
</script>

<style scoped lang="scss">
.register-stepper {
  display: flex;
  flex-direction: column;
  gap: 32px;
  max-width: 500px;
  margin: 0 auto;
}

.stepper-track {
  padding: 0 12px;
}

.stepper-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stepper-header {
  .stepper-subtitle {
    margin: 0;
    font-size: 14px;
    color: #6b7280;
    text-align: center;
  }
}

.stepper-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-btn {
  font-size: 13px;
  padding: 0 12px;
  white-space: nowrap;
}

.agreement-item {
  :deep(.t-checkbox__label) {
    font-size: 14px;
    color: #4b5563;
  }
}

.agreement-link {
  font-weight: 600;
  margin: 0 2px;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-top: 12px;
}

.ghost-btn {
  min-width: 120px;
  height: 48px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
}

.primary-btn {
  flex: 1;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
}

:deep(.t-form__item) {
  margin-bottom: 0;
}

:deep(.t-input) {
  border-radius: 8px;
  
  .t-input__inner {
    height: 48px;
    font-size: 15px;
  }
}

:deep(.t-select) {
  .t-input {
    border-radius: 8px;
  }
  
  .t-input__inner {
    height: 48px;
    font-size: 15px;
  }
}
</style>
