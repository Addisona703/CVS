<template>
  <div class="teacher-check-page">
    <el-card class="tool-card">
      <header class="card-header">
        <div class="title">
          <el-icon><Postcard /></el-icon>
          <div>
            <h2>活动签到 / 签退二维码</h2>
            <p>生成临时二维码口令，供学生扫码完成签到签退</p>
          </div>
        </div>
        <el-tag type="warning" effect="plain">二维码默认有效期 {{ expiryMinutes }} 分钟</el-tag>
      </header>

      <el-form :model="form" label-width="100px" class="form-area">
        <el-form-item label="活动编号">
          <el-input
            v-model="form.activityId"
            placeholder="请输入活动编号"
            clearable
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="操作">
          <el-space wrap>
            <el-button
              type="primary"
              :loading="checkinLoading"
              :disabled="!form.activityId"
              @click="generateToken('checkin')"
            >
              生成签到二维码
            </el-button>
            <el-button
              type="success"
              :loading="checkoutLoading"
              :disabled="!form.activityId"
              @click="generateToken('checkout')"
            >
              生成签退二维码
            </el-button>
          </el-space>
        </el-form-item>
      </el-form>

      <el-row :gutter="24" class="qr-row">
        <el-col :xs="24" :md="12">
          <section class="qr-panel">
            <h3>签到二维码</h3>
            <qr-display
              :value="checkinToken"
              :expires-at="checkinExpiresAt"
              caption="学生扫码或输入口令完成签到"
            />
            <el-button
              v-if="checkinToken"
              link
              type="primary"
              size="small"
              class="copy-btn"
              @click="copyToken(checkinToken)"
            >
              复制口令
            </el-button>
          </section>
        </el-col>
        <el-col :xs="24" :md="12">
          <section class="qr-panel">
            <h3>签退二维码</h3>
            <qr-display
              :value="checkoutToken"
              :expires-at="checkoutExpiresAt"
              caption="学生签退后填写自评"
            />
            <el-button
              v-if="checkoutToken"
              link
              type="primary"
              size="small"
              class="copy-btn"
              @click="copyToken(checkoutToken)"
            >
              复制口令
            </el-button>
          </section>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Postcard } from '@element-plus/icons-vue'
import { checkAPI } from '@/api/check'
import QrDisplay from '@/components/QrDisplay.vue'

const EXPIRY_MINUTES_DEFAULT = 3

const form = reactive({
  activityId: ''
})
const expiryMinutes = ref(EXPIRY_MINUTES_DEFAULT)

const checkinToken = ref('')
const checkoutToken = ref('')
const checkinExpiresAt = ref(0)
const checkoutExpiresAt = ref(0)
const checkinLoading = ref(false)
const checkoutLoading = ref(false)

const generateToken = async (type) => {
  if (!form.activityId) {
    ElMessage.warning('请先输入活动编号')
    return
  }

  const activityId = form.activityId.trim()
  if (!activityId) {
    ElMessage.warning('活动编号不能为空')
    return
  }

  const isCheckin = type === 'checkin'
  const loadingRef = isCheckin ? checkinLoading : checkoutLoading
  loadingRef.value = true
  try {
    const response = isCheckin
      ? await checkAPI.generateCheckinToken(activityId)
      : await checkAPI.generateCheckoutToken(activityId)
    const token = response.data?.token
    if (!token) {
      throw new Error('未获取到口令')
    }
    const expiresAt = Date.now() + expiryMinutes.value * 60 * 1000

    if (isCheckin) {
      checkinToken.value = token
      checkinExpiresAt.value = expiresAt
    } else {
      checkoutToken.value = token
      checkoutExpiresAt.value = expiresAt
    }

    ElMessage.success(`${isCheckin ? '签到' : '签退'}二维码生成成功`)
  } catch (error) {
    console.error('生成二维码失败', error)
  } finally {
    loadingRef.value = false
  }
}

const copyToken = async (token) => {
  try {
    await navigator.clipboard.writeText(token)
    ElMessage.success('已复制口令到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style lang="scss" scoped>
.teacher-check-page {
  min-height: calc(100vh - 120px);
  display: flex;
  justify-content: center;
  padding: clamp(24px, 4vw, 48px) 16px;
  background: linear-gradient(160deg, rgba(233, 244, 255, 0.45), rgba(255, 255, 255, 0.85));

  .tool-card {
    width: min(960px, 100%);
    border-radius: 28px;
    border: none;
    box-shadow: 0 40px 120px -60px rgba(31, 107, 255, 0.35);

    :deep(.el-card__body) {
      padding: clamp(24px, 4vw, 38px);
    }

    .card-header {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 26px;

      .title {
        display: flex;
        gap: 14px;
        align-items: flex-start;

        h2 {
          margin: 0;
          font-size: 24px;
          font-weight: 700;
          color: #1f2a44;
        }

        p {
          margin: 6px 0 0;
          color: rgba(31, 42, 68, 0.6);
          font-size: 14px;
        }

        :deep(svg) {
          font-size: 34px;
          color: #1f6bff;
          padding: 12px;
          border-radius: 16px;
          background: linear-gradient(135deg, rgba(31, 107, 255, 0.18), rgba(31, 107, 255, 0.06));
          box-shadow: inset 0 0 0 1px rgba(31, 107, 255, 0.12);
        }
      }
    }

    .form-area {
      background: rgba(31, 107, 255, 0.06);
      border-radius: 18px;
      padding: 18px clamp(18px, 3vw, 28px);
      margin-bottom: 24px;
    }

    .qr-row {
      margin-top: 12px;

      .qr-panel {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        padding: 20px;
        background: rgba(255, 255, 255, 0.92);
        border-radius: 22px;
        box-shadow: inset 0 0 0 1px rgba(31, 107, 255, 0.06);

        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #1f2a44;
        }

        .copy-btn {
          margin-top: -4px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .teacher-check-page {
    padding: 18px 12px;
  }
}
</style>
