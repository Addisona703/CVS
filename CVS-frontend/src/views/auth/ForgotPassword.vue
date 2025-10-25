<template>
  <div class="forgot-password">
    <div class="form-header">
      <h2>重置密码</h2>
      <p>请输入您的邮箱，我们将发送重置链接。</p>
    </div>

    <t-form
      ref="formRef"
      :data="form"
      :rules="rules"
      :colon="false"
      :label-width="0"
      class="forgot-form"
      @submit="handleSendReset"
    >
      <t-form-item name="email">
        <t-input
          v-model="form.email"
          placeholder="请输入邮箱地址"
          clearable
          size="large"
        >
          <template #prefix-icon>
            <t-icon name="mail" />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item class="form-actions">
        <t-button
          theme="primary"
          type="submit"
          block
          size="large"
          class="submit-btn"
          :loading="loading"
        >
          发送重置链接
        </t-button>
      </t-form-item>

      <t-link theme="primary" hover="color" class="back-link" @click="router.push('/auth/login')">
        返回登录
      </t-link>
    </t-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { authAPI } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  email: ''
})

const emailValidator = (val) => {
  const normalized = val?.trim() || ''
  if (!normalized) {
    return { result: false, message: '请输入邮箱地址', type: 'error' }
  }
  const emailPattern = /^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/
  if (emailPattern.test(normalized)) {
    return { result: true }
  } else {
    return { result: false, message: '请输入正确的邮箱地址', type: 'error' }
  }
}

const rules = {
  email: [{ validator: emailValidator, trigger: 'blur' }]
}

const handleSendReset = async ({ validateResult, firstError }) => {
  if (validateResult !== true) {
    MessagePlugin.warning(firstError || '请检查表单输入')
    return
  }

  try {
    loading.value = true
    await authAPI.forgotPassword({ email: form.email.trim() })
    MessagePlugin.success('重置链接已发送，请查收邮箱')
  } catch (error) {
    console.error('发送重置链接失败:', error)
    MessagePlugin.error(error.response?.data?.message || '发送失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.forgot-password {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 400px;
  margin: 0 auto;
}

.form-header {
  display: flex;
  flex-direction: column;
  gap: 8px;

  h2 {
    margin: 0;
    font-size: 28px;
    font-weight: 600;
    color: #1f2937;
    letter-spacing: -0.01em;
  }

  p {
    margin: 0;
    font-size: 14px;
    color: #6b7280;
  }
}

.forgot-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-actions {
  margin-bottom: 0;
}

.submit-btn {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
}

.back-link {
  display: inline-flex;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
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
</style>
