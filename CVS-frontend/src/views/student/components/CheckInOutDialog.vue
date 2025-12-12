<template>
  <el-dialog
    v-model="dialogVisible"
    title="活动签到签退"
    width="540px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="check-dialog-content">
      <!-- 选项卡 -->
      <el-tabs v-model="activeTab" class="check-tabs">
        <el-tab-pane label="活动签到" name="checkin" />
        <el-tab-pane label="活动签退" name="checkout" />
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
              :autosize="{ minRows: 3, maxRows: 5 }"
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
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import { checkAPI } from '@/api/check'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = ref(false)
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

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  // 重置表单
  activeTab.value = 'checkin'
  token.value = ''
  checkinSuccess.value = false
  checkoutSuccess.value = false
  checkoutForm.studentRating = 0
  checkoutForm.studentEvaluation = ''
}

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
    emit('success', 'checkin')
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
    emit('success', 'checkout')
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
.check-dialog-content {
  padding: 0;

  .check-tabs {
    margin-bottom: 20px;

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
      padding: 10px 14px;
      font-size: 13px;
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
  }

  .form-section {
    animation: fadeIn 0.25s ease;

    :deep(.el-form-item) {
      margin-bottom: 16px;

      &:last-of-type {
        margin-bottom: 0;
      }
    }

    :deep(.el-form-item__label) {
      font-weight: 600;
      color: #303133;
      font-size: 13px;
      margin-bottom: 6px;
      padding-bottom: 0;
    }

    :deep(.el-input__wrapper) {
      padding: 10px 14px;
      border-radius: 8px;
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
      padding: 10px 14px;
      border-radius: 8px;
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
      height: 42px;
      font-size: 14px;
      font-weight: 600;
      border-radius: 8px;
      margin-top: 16px;
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
      margin-top: 12px;
      border-radius: 8px;
      border: none;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

      :deep(.el-alert__content) {
        line-height: 1.6;
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
</style>
