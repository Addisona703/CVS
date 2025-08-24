<template>
  <div class="system-status">
    <div class="page-header">
      <h1>系统状态</h1>
      <el-button @click="refreshAll" :loading="loading">刷新</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>系统信息</template>
          <el-descriptions :column="1" border v-if="info">
            <el-descriptions-item label="名称">{{ info.name }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ info.version }}</el-descriptions-item>
            <el-descriptions-item label="描述">{{ info.description }}</el-descriptions-item>
            <el-descriptions-item label="时间戳">{{ info.timestamp }}</el-descriptions-item>
            <el-descriptions-item label="API 文档">
              <a :href="info.apiDocs" target="_blank">{{ info.apiDocs }}</a>
            </el-descriptions-item>
            <el-descriptions-item label="API JSON">
              <a :href="info.apiJson" target="_blank">{{ info.apiJson }}</a>
            </el-descriptions-item>
          </el-descriptions>
          <div v-else class="empty">暂无数据</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>健康检查</template>
          <div class="health">
            <el-tag :type="healthOk ? 'success' : 'danger'">
              {{ healthOk ? '系统健康' : '异常' }}
            </el-tag>
            <div class="msg">{{ healthMsg }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { systemAPI } from '@/api'

const loading = ref(false)
const info = ref(null)
const healthOk = ref(false)
const healthMsg = ref('')

const refreshAll = async () => {
  loading.value = true
  try {
    const [infoResp, healthResp] = await Promise.all([
      systemAPI.getInfo(),
      systemAPI.health()
    ])
    info.value = infoResp.data || null
    // 健康检查接口返回可能是字符串或标准响应，这里兼容处理
    if (typeof healthResp.data === 'string') {
      healthOk.value = true
      healthMsg.value = healthResp.data
    } else {
      healthOk.value = healthResp.code === 200
      healthMsg.value = healthResp.message || '系统健康'
    }
  } catch (e) {
    healthOk.value = false
    healthMsg.value = '无法获取健康状态'
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => refreshAll())
</script>

<style scoped lang="scss">
.system-status {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
  .health { display: flex; align-items: center; gap: 12px; }
  .empty { color: #909399; }
}
</style>

