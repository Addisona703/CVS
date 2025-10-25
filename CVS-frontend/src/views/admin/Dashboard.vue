<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>管理员仪表板</h1>
      <p>欢迎回来，{{ userName }}！</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon users">
              <el-icon size="32">
                <User />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon activities">
              <el-icon size="32">
                <Calendar />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalActivities || 0 }}</div>
              <div class="stat-label">总活动数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon signups">
              <el-icon size="32">
                <UserFilled />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalSignups || 0 }}</div>
              <div class="stat-label">总报名数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon hours">
              <el-icon size="32">
                <Clock />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalServiceRecords || 0 }}</div>
              <div class="stat-label">服务记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活动统计图表和积分排行榜 -->
    <el-row :gutter="20" class="content-section">
      <el-col :xs="24" :lg="12">
        <t-card title="活动统计趋势" header-bordered>
          <template #actions>
            <el-radio-group v-model="statisticsDays" size="small" @change="fetchActivityStatistics">
              <el-radio-button :label="7">7天</el-radio-button>
              <el-radio-button :label="30">30天</el-radio-button>
              <el-radio-button :label="90">90天</el-radio-button>
            </el-radio-group>
          </template>
          <div class="chart-container" v-loading="chartLoading">
            <div ref="chartRef" class="chart" style="width: 100%; height: 350px;"></div>
          </div>
        </t-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <t-card title="积分排行榜" header-bordered>
          <template #actions>
            <a href="javascript:void(0)" @click="$router.push('/admin/points')">更多</a>
          </template>
          <div class="ranking-list">
            <div class="ranking-item" v-for="(item, index) in pointsRanking.slice(0, 5)" :key="item.userId || index">
              <div class="rank-number" :class="{ 'top-three': index < 3 }">
                {{ index + 1 }}
              </div>
              <div class="user-info">
                <div class="user-name">{{ item.name || '未知用户' }}</div>
                <div class="user-role">{{ getRoleLabel(item.role) }}</div>
              </div>
              <div class="user-points">{{ item.totalPoints || 0 }}分</div>
              <div class="rank-badge" v-if="index < 3">
                <el-icon v-if="index === 0" color="#ffd700">
                  <Trophy />
                </el-icon>
                <el-icon v-else-if="index === 1" color="#c0c0c0">
                  <Trophy />
                </el-icon>
                <el-icon v-else color="#cd7f32">
                  <Trophy />
                </el-icon>
              </div>
            </div>
            <div v-if="pointsRanking.length === 0" class="empty-list">
              <el-icon size="48" color="#ddd">
                <Trophy />
              </el-icon>
              <p>暂无排行榜数据</p>
            </div>
          </div>
        </t-card>
      </el-col>
    </el-row>


  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { statisticsAPI, activityAPI, pointsAPI } from '@/api'
import { ROLE_LABELS } from '@/utils/constants'
import * as echarts from 'echarts'
import {
  User,
  Calendar,
  UserFilled,
  Clock,
  Trophy
} from '@element-plus/icons-vue'

const authStore = useAuthStore()

const stats = ref({
  totalUsers: 0,
  totalActivities: 0,
  totalSignups: 0,
  totalServiceRecords: 0,
  onlineUsers: 0
})

// 图表相关
const chartRef = ref(null)
const chartInstance = ref(null)
const statisticsDays = ref(7)
const chartLoading = ref(false)

// 积分排行榜数据
const pointsRanking = ref([])

const userName = computed(() => authStore.userName)

const getRoleLabel = (role) => {
  return ROLE_LABELS[role] || role || '未知角色'
}

const fetchStats = async () => {
  try {
    const response = await statisticsAPI.getAdminDashboardStats()
    if (response.code === 200) {
      Object.assign(stats.value, response.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取活动统计数据并绘制图表
const fetchActivityStatistics = async () => {
  chartLoading.value = true
  try {
    // 确保传递的是数字类型
    const days = Number(statisticsDays.value)
    // console.log('请求统计数据，天数:', days, '类型:', typeof days)
    const response = await activityAPI.getActivityStatistics(days)
    if (response.code === 200 && response.data) {
      await nextTick()
      initChart(response.data)
    }
  } catch (error) {
    console.error('获取活动统计失败:', error)
  } finally {
    chartLoading.value = false
  }
}

// 初始化图表
const initChart = (data) => {
  if (!chartRef.value) return

  // 如果图表实例已存在，先销毁
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  // 创建新的图表实例
  chartInstance.value = echarts.init(chartRef.value)

  const dates = data.dailyStatistics.map(item => item.date)
  const activityCounts = data.dailyStatistics.map(item => item.activityCount)
  const participantCounts = data.dailyStatistics.map(item => item.participantCount)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['活动数', '参与人数'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        formatter: (value) => {
          // 格式化日期显示
          const date = new Date(value)
          return `${date.getMonth() + 1}/${date.getDate()}`
        }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '活动数',
        position: 'left',
        axisLabel: {
          formatter: '{value}'
        }
      },
      {
        type: 'value',
        name: '参与人数',
        position: 'right',
        axisLabel: {
          formatter: '{value}'
        }
      }
    ],
    series: [
      {
        name: '活动数',
        type: 'line',
        smooth: true,
        data: activityCounts,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '参与人数',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: participantCounts,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      }
    ]
  }

  chartInstance.value.setOption(option)

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    chartInstance.value?.resize()
  })
}

// 获取积分排行榜数据
const fetchPointsRanking = async () => {
  try {
    const response = await pointsAPI.getPointsRankingPage({ page: 1, size: 7 })
    if (response.code === 200) {
      pointsRanking.value = (response.data.records || []).map(item => ({
        ...item,
        // 确保字段名一致性
        userName: item.userName || item.username,
        totalPoints: item.totalPoints || 0,
        role: item.role || 'STUDENT'
      }))
    }
  } catch (error) {
    console.error('获取积分排行榜失败:', error)
    // 使用模拟数据作为备用
    pointsRanking.value = [
      { userId: 1, userName: '张三', role: 'STUDENT', totalPoints: 280 },
      { userId: 2, userName: '李四', role: 'STUDENT', totalPoints: 260 },
      { userId: 3, userName: '王五', role: 'TEACHER', totalPoints: 240 },
      { userId: 4, userName: '赵六', role: 'STUDENT', totalPoints: 220 },
      { userId: 5, userName: '钱七', role: 'STUDENT', totalPoints: 200 },
      { userId: 6, userName: '孙八', role: 'TEACHER', totalPoints: 180 },
      { userId: 7, userName: '周九', role: 'STUDENT', totalPoints: 160 }
    ]
  }
}

onMounted(() => {
  fetchStats()
  fetchActivityStatistics()
  fetchPointsRanking()
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .dashboard-header {
    margin-bottom: var(--space-6, 24px);

    h1 {
      margin: 0 0 var(--space-2, 8px) 0;
      font-size: 28px;
      color: #303133;
    }

    p {
      margin: 0;
      color: #606266;
      font-size: 16px;
    }
  }

  .stats-cards {
    margin-bottom: var(--space-6, 24px);

    .stat-card {
      margin-bottom: var(--space-4, 16px);

      :deep(.el-card__body) {
        padding: var(--space-5, 20px);
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: var(--space-4, 16px);

        .stat-icon {
          width: var(--space-15, 60px);
          height: var(--space-15, 60px);
          border-radius: var(--space-3, 12px);
          display: flex;
          align-items: center;
          justify-content: center;

          &.users {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
          }

          &.activities {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
          }

          &.signups {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            color: white;
          }

          &.hours {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
            color: white;
          }
        }

        .stat-info {
          flex: 1;

          .stat-number {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
            margin-bottom: var(--space-1, 4px);
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }

  .content-section {
    margin-bottom: var(--space-6, 24px);

    :deep(.t-card) {
      .t-card__actions a {
        color: #409eff;
        text-decoration: none;
        font-size: 14px;

        &:hover {
          color: #66b1ff;
        }
      }
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .chart-container {
      padding: var(--space-4, 16px) 0;

      .chart {
        min-height: 350px;
      }
    }

    .activity-list {
      .activity-item {
        padding: var(--space-4, 16px) 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .activity-info {
          .activity-title {
            font-size: 16px;
            color: #303133;
            margin-bottom: var(--space-2, 8px);
            font-weight: 500;
          }

          .activity-meta {
            display: flex;
            align-items: center;
            gap: var(--space-3, 12px);
            font-size: 14px;
            color: #909399;
            margin-bottom: var(--space-2, 8px);
          }

          .activity-desc {
            font-size: 14px;
            color: #606266;
            line-height: 1.4;
          }
        }
      }

      .empty-list {
        text-align: center;
        padding: var(--space-10, 40px) 0;
        color: #909399;

        p {
          margin: var(--space-4, 16px) 0 0 0;
        }
      }
    }

    .ranking-list {
      .ranking-item {
        display: flex;
        align-items: center;
        padding: var(--space-3, 12px) 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .rank-number {
          width: var(--space-8, 32px);
          height: var(--space-8, 32px);
          border-radius: 50%;
          background: #f5f7fa;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 600;
          margin-right: var(--space-4, 16px);

          &.top-three {
            background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
            color: white;
          }
        }

        .user-info {
          flex: 1;

          .user-name {
            font-size: 16px;
            color: #303133;
            margin-bottom: var(--space-1, 4px);
          }

          .user-role {
            font-size: 12px;
            color: #909399;
          }
        }

        .user-points {
          font-size: 16px;
          font-weight: 600;
          color: #e6a23c;
          margin-right: var(--space-4, 16px);
        }

        .rank-badge {
          margin-left: var(--space-2, 8px);
        }
      }
    }
  }

  .charts-section {
    margin-bottom: var(--space-6, 24px);

    .chart-placeholder {
      height: 300px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;

      p {
        margin: var(--space-4, 16px) 0 0 0;
        font-size: 16px;
      }
    }
  }

  .quick-actions {
    .action-buttons {
      display: flex;
      gap: var(--space-3, 12px);
      flex-wrap: wrap;

      .el-button {
        flex: 1;
        min-width: 120px;
      }
    }

    .system-status {
      .status-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--space-3, 12px) 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .status-label {
          color: #606266;
          font-size: 14px;
        }

        .status-value {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    .action-buttons {
      .el-button {
        width: 100%;
        margin-bottom: var(--space-2, 8px);
      }
    }
  }
}
</style>
