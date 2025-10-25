<template>
  <div class="activity-qrcode-page">
    <div class="page-header">
      <el-button class="back-btn" text @click="$router.back()">
        <el-icon>
          <ArrowLeft />
        </el-icon>
        返回上一页
      </el-button>
      <div class="header-text">
        <h1>{{ activity?.title || '活动二维码' }}</h1>
      </div>
      <el-tag :type="isCheckout ? 'danger' : 'success'" effect="light">
        {{ isCheckout ? '签退模式' : '签到模式' }}
      </el-tag>
    </div>

    <div class="content-grid">
      <el-card class="section-card qr-card" shadow="hover">
        <div class="qr-section">
          <div class="qr-heading">
            <h3>{{ isCheckout ? '签退二维码' : '签到二维码' }}</h3>
            <el-tag size="small" effect="dark">
              有效期 {{ EXPIRY_MINUTES }} 分钟
            </el-tag>
          </div>
          <p class="qr-tip">
            {{ isCheckout ? '学生扫码完成签退并填写自评，二维码将在过期前自动刷新。' : '学生扫码或输入口令即可完成签到，二维码将在过期前自动刷新。' }}
          </p>

          <div class="qr-wrapper">
            <qr-display :value="token" :expires-at="expiresAt" :size="280" />
          </div>

          <div v-if="token" class="qr-actions">
            <el-button type="primary" @click="copyToken" :icon="DocumentCopy">
              复制口令
            </el-button>
            <el-button type="info" @click="refreshToken" :loading="loading" :icon="Refresh">
              手动刷新
            </el-button>
          </div>

          <el-alert v-if="!token && !loading" title="二维码生成失败" type="error" :closable="false" show-icon
            class="error-alert">
            <template #default>
              <p>无法生成二维码，请检查活动状态或稍后重试。</p>
              <el-button type="primary" size="small" @click="generateToken">
                重新生成
              </el-button>
            </template>
          </el-alert>
        </div>
      </el-card>

      <el-card class="section-card signed-card" shadow="hover">
        <template #header>
          <div class="signed-header">
            <div>
              <span class="title">{{ isCheckout ? '未签退学生' : '未签到学生' }}</span>
              <span class="subtitle">{{ isCheckout ? '实时掌握已签到但尚未签退的学生' : '实时掌握尚未扫码签到的报名学生' }}</span>
            </div>
            <div class="header-actions">
              <el-tag :type="isCheckout ? 'info' : 'warning'" effect="light">
                {{ isCheckout ? '未签退' : '未签到' }} {{ pendingStudents.length }} 人
              </el-tag>
              <el-button type="primary" link :icon="Refresh" @click="fetchPendingStudents" :loading="pendingLoading">
                {{ isCheckout ? '刷新未签退' : '刷新未签到' }}
              </el-button>
            </div>
          </div>
        </template>

        <el-skeleton v-if="pendingLoading" animated :rows="4" />
        <el-empty v-else-if="!pendingStudents.length" :description="isCheckout ? '所有学生均已签退' : '所有报名学生均已签到'" />
        <el-table v-else :data="pendingStudents" stripe size="small" class="signed-table">
          <el-table-column label="学生" min-width="180">
            <template #default="{ row }">
              <div class="table-user">
                <el-avatar :size="32">
                  {{ (row.name || '--').slice(0, 1) }}
                </el-avatar>
                <div class="user-text">
                  <span class="name">{{ row.name || '未知姓名' }}</span>
                  <span class="meta">{{ row.username || '—' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="160">
            <template #default>
              {{ isCheckout ? '尚未签退' : '尚未签到' }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, DocumentCopy, Refresh } from '@element-plus/icons-vue'
import { activityAPI } from '@/api'
import { checkAPI } from '@/api/check'
import QrDisplay from '@/components/QrDisplay.vue'

const route = useRoute()

const activityId = computed(() => route.params.id)
const isCheckout = computed(() => route.query.type === 'checkout')

const activity = ref(null)
const token = ref('')
const expiresAt = ref(0)
const loading = ref(false)
const pendingStudents = ref([])
const pendingLoading = ref(false)

let refreshTimer = null
let refreshInterval = null
const EXPIRY_MINUTES = 3

// 获取活动详情
const fetchActivity = async () => {
  if (!activityId.value) return
  try {
    const response = await activityAPI.getActivityById(activityId.value)
    if (response.code === 200) {
      activity.value = response.data
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动信息失败')
  }
}

const fetchPendingStudents = async () => {
  if (!activityId.value) {
    pendingStudents.value = []
    return
  }
  pendingLoading.value = true
  try {
    const response = isCheckout.value
      ? await checkAPI.getPendingCheckoutStudents(activityId.value)
      : await checkAPI.getPendingCheckinStudents(activityId.value)
      console.log(response)
    if (response.code === 200) {
      pendingStudents.value = Array.isArray(response.data) ? response.data : []
    }
  } catch (error) {
    console.error(`获取未${isCheckout.value ? '签退' : '签到'}学生失败:`, error)
  } finally {
    pendingLoading.value = false
  }
}


// 生成二维码
const generateToken = async () => {
  if (!activityId.value) return
  loading.value = true
  try {
    const response = isCheckout.value
      ? await checkAPI.generateCheckoutToken(activityId.value)
      : await checkAPI.generateCheckinToken(activityId.value)

    if (response.data?.token) {
      token.value = response.data.token
      expiresAt.value = Date.now() + EXPIRY_MINUTES * 60 * 1000
      ElMessage.success(`${isCheckout.value ? '签退' : '签到'}二维码生成成功`)

      // 设置自动刷新
      setupAutoRefresh()
    } else {
      throw new Error('未获取到口令')
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error('生成二维码失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 刷新二维码
const refreshToken = () => {
  generateToken()
}

// 复制口令
const copyToken = async () => {
  try {
    await navigator.clipboard.writeText(token.value)
    ElMessage.success('已复制口令到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}



// 设置自动刷新
const setupAutoRefresh = () => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }

  // 在二维码过期前30秒自动刷新
  const refreshTime = (EXPIRY_MINUTES * 60 - 30) * 1000
  refreshTimer = setTimeout(() => {
    generateToken()
  }, refreshTime)
}

watch(
  () => isCheckout.value,
  () => {
    fetchPendingStudents()
    generateToken()
  }
)

watch(
  () => activityId.value,
  () => {
    fetchActivity()
    fetchPendingStudents()
  }
)

onMounted(() => {
  fetchActivity()
  generateToken()
  fetchPendingStudents()

  // 定期同步未签到/未签退名单
  refreshInterval = setInterval(() => {
    fetchActivity()
    fetchPendingStudents()
  }, 10000) // 每10秒更新一次
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style lang="scss" scoped>
.activity-qrcode-page {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: var(--space-6, 24px);

    .back-btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      font-weight: 500;
      color: #67c23a;
      transition: all 0.2s ease;

      :deep(.el-icon) {
        font-size: 16px;
      }

      &:hover {
        background: rgba(103, 194, 58, 0.1);
        color: #5daf34;
      }
    }

    .header-text {
      flex: 1;
      text-align: center;

      h1 {
        margin: 0 0 4px 0;
        font-size: 28px;
        font-weight: 600;
        color: #303133;
      }
    }

    :deep(.el-tag) {
      padding: 4px 16px;
      font-weight: 500;
    }
  }

  .content-grid {
    display: grid;
    grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
    gap: var(--space-5, 20px);
  }

  .section-card {
    :deep(.el-card__body) {
      padding: var(--space-6, 24px);
    }
  }

  .qr-card {
    display: flex;
    justify-content: center;

    .qr-section {
      width: 100%;
      max-width: 420px;
      margin: 0 auto;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-5, 20px);
      text-align: center;

      .qr-heading {
        display: flex;
        align-items: center;
        gap: var(--space-3, 12px);

        h3 {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
          color: #303133;
        }

        :deep(.el-tag) {
          padding: 2px 12px;
        }
      }

      .qr-tip {
        margin: 0;
        color: #606266;
        font-size: 14px;
        line-height: 1.5;
      }

      .qr-wrapper {
        display: flex;
        justify-content: center;
      }

      .qr-actions {
        display: flex;
        justify-content: center;
        gap: var(--space-3, 12px);

        .el-button {
          padding: 12px 24px;
          font-weight: 500;
        }
      }

      .error-alert {
        width: 100%;

        p {
          margin-bottom: var(--space-3, 12px);
        }
      }
    }
  }

  .signed-card {
    :deep(.el-card__header) {
      padding: var(--space-5, 20px) var(--space-6, 24px);
      background: #fafafa;
    }

    .signed-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--space-4, 16px);

      .title {
        display: block;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }

      .subtitle {
        display: block;
        font-size: 13px;
        color: #909399;
        margin-top: 2px;
      }

      .header-actions {
        display: flex;
        align-items: center;
        gap: var(--space-3, 12px);
      }
    }

    .signed-table {
      margin-top: var(--space-2, 8px);

      :deep(.el-table__header th) {
        background: #f5f7fa;
        color: #606266;
        font-weight: 600;
      }

      :deep(.el-table__body tr:hover > td) {
        background-color: rgba(103, 194, 58, 0.08);
      }
    }

    .table-user {
      display: flex;
      align-items: center;
      gap: var(--space-3, 12px);

      .user-text {
        display: flex;
        flex-direction: column;
        gap: 2px;

        .name {
          font-weight: 600;
          color: #303133;
        }

        .meta {
          font-size: 12px;
          color: #909399;
        }
      }
    }


  }
}

// 确保按钮颜色与系统一致
:deep(.el-button) {
  &.el-button--primary {
    background-color: #67c23a;
    border-color: #67c23a;

    &:hover,
    &:focus {
      background-color: #85ce61;
      border-color: #85ce61;
    }

    &:active {
      background-color: #5daf34;
      border-color: #5daf34;
    }
  }

  &.el-button--info {
    background-color: #909399;
    border-color: #909399;

    &:hover,
    &:focus {
      background-color: #a6a9ad;
      border-color: #a6a9ad;
    }
  }
}

@media (max-width: 1024px) {
  .activity-qrcode-page {
    .content-grid {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .activity-qrcode-page {
    .page-header {
      flex-direction: column;
      align-items: flex-start;

      .header-text {
        text-align: left;
      }
    }

    .section-card :deep(.el-card__body) {
      padding: var(--space-5, 20px);
    }

    .qr-card .qr-section .qr-actions {
      flex-direction: column;
      width: 100%;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
