<template>
  <div class="student-check-page">
    <el-card class="check-card">
      <header class="card-header">
        <h2>志愿活动签到签退</h2>
        <p>扫描二维码后粘贴口令完成签到或签退流程</p>
      </header>

      <!-- 选项卡 -->
      <el-tabs v-model="activeTab" class="check-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="活动签到" name="checkin">
          <div class="tab-icon">📝</div>
        </el-tab-pane>
        <el-tab-pane label="活动签退" name="checkout">
          <div class="tab-icon">✅</div>
        </el-tab-pane>
      </el-tabs>

      <!-- 签到表单 -->
      <section v-if="activeTab === 'checkin'" class="form-section">
        <el-form label-position="top">
          <el-form-item label="签到口令">
            <el-input
              v-model="token"
              placeholder="在此粘贴二维码口令"
              :disabled="checkinSuccess"
              clearable
              size="large"
            >
              <template #suffix>
                <el-button 
                  :icon="DocumentCopy" 
                  link 
                  @click="pasteFromClipboard"
                  title="从剪贴板粘贴"
                />
              </template>
            </el-input>
          </el-form-item>
        </el-form>

        <el-button
          type="primary"
          size="large"
          class="submit-button"
          :loading="checkinLoading"
          :disabled="checkinSuccess || !token"
          @click="handleCheckin"
        >
          <span v-if="checkinSuccess">✓ 已完成签到</span>
          <span v-else>确认签到</span>
        </el-button>

        <el-alert
          v-if="checkinSuccess"
          type="success"
          show-icon
          class="tip-alert"
          title="签到成功！完成服务后请切换到-活动签退-提交自评"
          :closable="false"
        />
      </section>

      <!-- 签退表单 -->
      <section v-if="activeTab === 'checkout'" class="form-section">
        <el-form label-position="top">
          <el-form-item label="签退口令">
            <el-input
              v-model="token"
              placeholder="在此粘贴二维码口令"
              :disabled="checkoutSuccess"
              clearable
              size="large"
            >
              <template #suffix>
                <el-button 
                  :icon="DocumentCopy" 
                  link 
                  @click="pasteFromClipboard"
                  title="从剪贴板粘贴"
                />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="自我评分 (1-5分)" class="rating-item">
            <el-rate
              v-model="checkoutForm.studentRating"
              :colors="['#99ccff', '#409EFF', '#1f6bff']"
              :allow-half="false"
              :disabled="checkoutSuccess"
              size="large"
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
              :disabled="checkoutSuccess"
            />
          </el-form-item>
        </el-form>

        <el-button
          type="primary"
          size="large"
          class="submit-button"
          :loading="checkoutLoading"
          :disabled="checkoutSuccess || !token"
          @click="handleCheckout"
        >
          <span v-if="checkoutSuccess">✓ 已完成签退</span>
          <span v-else>确认签退</span>
        </el-button>

        <el-alert
          v-if="checkoutSuccess"
          type="success"
          show-icon
          class="tip-alert"
          title="签退已提交，结果将在教师审核后同步至个人记录"
          :closable="false"
        />
      </section>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import { checkAPI } from '@/api/check'

const route = useRoute()

const activeTab = ref('checkin')
const token = ref('')
const checkinSuccess = ref(false)
const checkoutSuccess = ref(false)
const checkinLoading = ref(false)
const checkoutLoading = ref(false)

const checkoutForm = reactive({
  studentRating: 0,
  studentEvaluation: ''
})

const handleTabChange = (tabName) => {
  // 切换选项卡时可以做一些额外处理
  console.log('切换到:', tabName)
}

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
  min-height: 100vh;
  padding:20px 24px 80px;
  background: 
    radial-gradient(circle at 15% 20%, rgba(64, 158, 255, 0.06) 0%, transparent 40%),
    radial-gradient(circle at 85% 80%, rgba(103, 194, 58, 0.05) 0%, transparent 40%),
    linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  background-attachment: fixed;
  position: relative;

  // 添加微妙的背景图案
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      repeating-linear-gradient(90deg, transparent, transparent 40px, rgba(64, 158, 255, 0.015) 40px, rgba(64, 158, 255, 0.015) 80px),
      repeating-linear-gradient(0deg, transparent, transparent 40px, rgba(64, 158, 255, 0.015) 40px, rgba(64, 158, 255, 0.015) 80px);
    pointer-events: none;
    z-index: 0;
  }

  .check-card {
    max-width: 520px;
    margin: 0 auto;
    border-radius: 20px;
    box-shadow: 
      0 8px 32px -8px rgba(31, 107, 255, 0.15),
      0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(64, 158, 255, 0.08);
    background: #ffffff;
    position: relative;
    z-index: 1;

    :deep(.el-card__body) {
      padding: 36px 32px;
    }

    .card-header {
      text-align: center;
      margin-bottom: 28px;
      padding-bottom: 20px;
      border-bottom: 1px solid rgba(64, 158, 255, 0.08);

      h2 {
        margin: 0 0 6px 0;
        font-size: 24px;
        font-weight: 700;
        color: #1f2937;
        letter-spacing: -0.5px;
      }

      p {
        margin: 0;
        color: #606266;
        font-size: 13px;
        line-height: 1.5;
      }
    }

    .check-tabs {
      margin-bottom: 24px;

      :deep(.el-tabs__header) {
        margin-bottom: 0;
      }

      :deep(.el-tabs__nav-wrap) {
        &::after {
          display: none;
        }
      }

      :deep(.el-tabs__nav) {
        display: flex;
        width: 100%;
        border: 1.5px solid #e4e7ed;
        border-radius: 12px;
        padding: 3px;
        background: #f5f7fa;
      }

      :deep(.el-tabs__item) {
        flex: 1;
        text-align: center;
        padding: 12px 16px;
        font-size: 14px;
        font-weight: 600;
        color: #606266;
        border-radius: 9px;
        transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        border: none;
        height: auto;
        line-height: 1.4;

        &:hover {
          color: #409eff;
          background: rgba(64, 158, 255, 0.06);
        }

        &.is-active {
          color: #ffffff;
          background: linear-gradient(135deg, #409eff 0%, #1f6bff 100%);
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.25);
        }
      }

      :deep(.el-tabs__active-bar) {
        display: none;
      }

      .tab-icon {
        display: none;
      }
    }

    .form-section {
      animation: fadeIn 0.25s ease;

      :deep(.el-form-item) {
        margin-bottom: 20px;
      }

      :deep(.el-form-item__label) {
        font-weight: 600;
        color: #303133;
        font-size: 14px;
        margin-bottom: 8px;
        padding-bottom: 0;
      }

      :deep(.el-input__wrapper) {
        padding: 12px 15px;
        border-radius: 10px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
        transition: all 0.25s ease;
        border: 1px solid transparent;

        &:hover {
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.12);
          border-color: rgba(64, 158, 255, 0.2);
        }

        &.is-focus {
          box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
          border-color: #409eff;
        }
      }

      :deep(.el-textarea__inner) {
        padding: 12px 15px;
        border-radius: 10px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
        transition: all 0.25s ease;
        border: 1px solid #dcdfe6;

        &:hover {
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.12);
          border-color: rgba(64, 158, 255, 0.3);
        }

        &:focus {
          box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
          border-color: #409eff;
        }
      }

      .rating-item {
        :deep(.el-rate) {
          height: 32px;
          display: flex;
          align-items: center;
        }

        :deep(.el-rate__icon) {
          font-size: 26px;
          margin-right: 6px;
        }
      }

      .submit-button {
        width: 100%;
        height: 44px;
        font-size: 15px;
        font-weight: 600;
        border-radius: 10px;
        margin-top: 4px;
        transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover:not(:disabled) {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }

        &:active:not(:disabled) {
          transform: translateY(0);
        }
      }

      .tip-alert {
        margin-top: 16px;
        border-radius: 10px;
        border: none;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

        :deep(.el-alert__content) {
          line-height: 1.6;
        }
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .student-check-page {
    padding: 30px 16px 50px;

    .check-card {
      border-radius: 16px;

      :deep(.el-card__body) {
        padding: 28px 24px;
      }

      .card-header {
        margin-bottom: 24px;
        padding-bottom: 16px;

        h2 {
          font-size: 20px;
        }

        p {
          font-size: 12px;
        }
      }

      .check-tabs {
        margin-bottom: 20px;

        :deep(.el-tabs__item) {
          padding: 10px 12px;
          font-size: 13px;
        }
      }

      .form-section {
        .submit-button {
          height: 42px;
          font-size: 14px;
        }
      }
    }
  }
}
</style>
