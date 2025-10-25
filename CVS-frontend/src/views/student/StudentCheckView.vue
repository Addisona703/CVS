<template>
  <div class="student-check-page">
    <el-card class="check-card">
      <header class="card-header">
        <div class="title">
          <el-icon><Aim /></el-icon>
          <div>
            <h2>志愿活动签到 / 签退</h2>
            <p>扫描二维码后粘贴口令完成签到签退流程</p>
          </div>
        </div>
        <el-tag v-if="checkoutSuccess" type="success" effect="dark">等待教师审核中</el-tag>
      </header>

      <section class="token-section">
        <el-form label-position="top" :model="{ token }">
          <el-form-item label="签到口令">
            <el-input
              v-model="token"
              placeholder="在此粘贴二维码口令"
              :disabled="checkinSuccess && checkoutSuccess"
              clearable
            >
              <template #append>
                <el-button text type="primary" @click="pasteFromClipboard">粘贴</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        <div class="action-group">
          <el-button
            type="primary"
            :loading="checkinLoading"
            :disabled="checkinSuccess || !token"
            @click="handleCheckin"
          >
            {{ checkinSuccess ? '已完成签到' : '立即签到' }}
          </el-button>
          <el-button
            type="success"
            :loading="checkoutLoading"
            :disabled="checkoutSuccess || !token"
            @click="openCheckout"
          >
            {{ checkoutSuccess ? '已完成签退' : '提交签退' }}
          </el-button>
        </div>
      </section>

      <el-collapse-transition>
        <section v-if="checkoutVisible" class="checkout-section">
          <h3>签退自评</h3>
          <el-form :model="checkoutForm" label-position="top">
            <el-form-item label="自我评分 (1-5分)">
              <el-rate
                v-model="checkoutForm.studentRating"
                :colors="['#99ccff', '#409EFF', '#1f6bff']"
                :allow-half="false"
              />
            </el-form-item>
            <el-form-item label="服务心得">
              <el-input
                v-model="checkoutForm.studentEvaluation"
                type="textarea"
                :autosize="{ minRows: 4, maxRows: 6 }"
                maxlength="400"
                show-word-limit
                placeholder="请描述本次服务的主要内容、收获或建议..."
              />
            </el-form-item>
            <div class="checkout-actions">
              <el-button @click="cancelCheckout" :disabled="checkoutLoading">取消</el-button>
              <el-button type="primary" :loading="checkoutLoading" @click="handleCheckout">
                提交签退
              </el-button>
            </div>
          </el-form>
        </section>
      </el-collapse-transition>

      <el-alert
        v-if="checkinSuccess && !checkoutSuccess"
        type="info"
        show-icon
        class="tip-alert"
        title="已完成签到，完成服务后请及时提交签退自评"
      />
      <el-alert
        v-if="checkoutSuccess"
        type="success"
        show-icon
        class="tip-alert"
        title="签退已提交，结果将在教师审核后同步至个人记录"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Aim } from '@element-plus/icons-vue'
import { checkAPI } from '@/api/check'

const route = useRoute()

const token = ref('')
const checkinSuccess = ref(false)
const checkoutSuccess = ref(false)
const checkoutVisible = ref(false)
const checkinLoading = ref(false)
const checkoutLoading = ref(false)

const checkoutForm = reactive({
  studentRating: 0,
  studentEvaluation: ''
})

const applyTokenFromRoute = () => {
  const incoming = route.query.token
  if (typeof incoming === 'string' && incoming.trim()) {
    token.value = incoming.trim()
  }
}

onMounted(() => {
  applyTokenFromRoute()
})

watch(
  () => route.query.token,
  () => applyTokenFromRoute()
)

const validateToken = () => {
  const value = token.value.trim()
  if (!value) {
    ElMessage.warning('请先粘贴二维码口令')
    return null
  }
  return value
}

const handleCheckin = async () => {
  const value = validateToken()
  if (!value) return
  checkinLoading.value = true
  try {
    await checkAPI.studentCheckin({ token: value })
    checkinSuccess.value = true
    ElMessage.success('签到成功，祝你志愿顺利！')
  } catch (error) {
    console.error('签到失败', error)
  } finally {
    checkinLoading.value = false
  }
}

const openCheckout = () => {
  // 直接打开签退自评表单，不检查签到状态
  // 后端会验证token是否有效
  if (!token.value.trim()) {
    ElMessage.warning('请先输入签退口令')
    return
  }
  checkoutVisible.value = true
}

const cancelCheckout = () => {
  if (checkoutLoading.value) return
  checkoutVisible.value = false
}

const handleCheckout = async () => {
  const value = validateToken()
  if (!value) return

  if (!checkoutForm.studentRating || checkoutForm.studentRating < 1) {
    ElMessage.warning('请为本次服务打一个分数')
    return
  }

  if (!checkoutForm.studentEvaluation.trim()) {
    ElMessage.warning('请填写服务心得或自评内容')
    return
  }

  checkoutLoading.value = true
  try {
    await checkAPI.studentCheckout({
      token: value,
      studentRating: checkoutForm.studentRating,
      studentEvaluation: checkoutForm.studentEvaluation.trim()
    })
    checkoutSuccess.value = true
    checkoutVisible.value = false
    ElMessage.success('签退信息已提交，请等待教师审核')
  } catch (error) {
    console.error('签退失败', error)
  } finally {
    checkoutLoading.value = false
  }
}

const pasteFromClipboard = async () => {
  try {
    const text = await navigator.clipboard.readText()
    if (text) {
      token.value = text.trim()
      ElMessage.success('已粘贴剪贴板内容')
    } else {
      ElMessage.info('剪贴板中没有内容')
    }
  } catch (error) {
    ElMessage.error('无法访问剪贴板，请手动粘贴')
  }
}
</script>

<style lang="scss" scoped>
.student-check-page {
  min-height: calc(100vh - 120px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: clamp(24px, 4vw, 48px) 16px;
  background: linear-gradient(180deg, rgba(233, 242, 255, 0.6), rgba(255, 255, 255, 0.9));

  .check-card {
    width: min(640px, 100%);
    border-radius: 26px;
    box-shadow: 0 24px 80px -30px rgba(31, 107, 255, 0.35);
    border: none;
    padding: 0 6px 24px;

    :deep(.el-card__body) {
      padding: clamp(22px, 4vw, 36px);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .title {
        display: flex;
        gap: 14px;
        align-items: flex-start;

        h2 {
          margin: 0;
          font-size: 24px;
          font-weight: 700;
          color: #1f2937;
        }

        p {
          margin: 6px 0 0;
          color: rgba(31, 41, 55, 0.6);
          font-size: 14px;
        }

        :deep(svg) {
          font-size: 34px;
          color: #1f6bff;
          padding: 12px;
          border-radius: 16px;
          background: linear-gradient(135deg, rgba(31, 107, 255, 0.15), rgba(31, 107, 255, 0.05));
          box-shadow: inset 0 0 0 1px rgba(31, 107, 255, 0.15);
        }
      }
    }

    .token-section {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .action-group {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
      }
    }

    .checkout-section {
      margin-top: 28px;
      padding: 20px 24px;
      background: rgba(31, 107, 255, 0.06);
      border-radius: 20px;

      h3 {
        margin: 0 0 18px;
        font-size: 18px;
        font-weight: 600;
        color: #1f2937;
      }

      .checkout-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        margin-top: 10px;
      }
    }

    .tip-alert {
      margin-top: 20px;
    }
  }
}

@media (max-width: 720px) {
  .student-check-page {
    padding: 18px 12px;
  }
}
</style>

