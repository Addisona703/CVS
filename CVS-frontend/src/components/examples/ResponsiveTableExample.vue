<template>
  <div class="responsive-table-example">
    <h3>响应式表格示例</h3>
    <p>此表格在不同屏幕尺寸下有不同的表现：</p>
    <ul>
      <li><strong>桌面端 (≥1024px):</strong> 完整表格显示</li>
      <li><strong>平板端 (768px-1023px):</strong> 横向滚动 + 阴影提示</li>
      <li><strong>移动端 (&lt;768px):</strong> 卡片布局</li>
    </ul>
    
    <ResponsiveTable>
      <template #table>
        <el-table :data="tableData" style="width: 100%; min-width: 800px;">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="活动名称" width="200" />
          <el-table-column prop="organizer" label="组织者" width="120" />
          <el-table-column prop="location" label="地点" width="150" />
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="endTime" label="结束时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary">查看</el-button>
              <el-button size="small">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
      
      <template #mobile-cards>
        <div 
          v-for="item in tableData" 
          :key="item.id"
          class="mobile-card"
        >
          <div class="card-header">{{ item.name }}</div>
          <div class="card-field">
            <span class="field-label">组织者:</span>
            <span class="field-value">{{ item.organizer }}</span>
          </div>
          <div class="card-field">
            <span class="field-label">地点:</span>
            <span class="field-value">{{ item.location }}</span>
          </div>
          <div class="card-field">
            <span class="field-label">时间:</span>
            <span class="field-value">{{ item.startTime }}</span>
          </div>
          <div class="card-field">
            <span class="field-label">状态:</span>
            <span class="field-value">
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ item.status }}
              </el-tag>
            </span>
          </div>
          <div class="card-actions">
            <el-button size="small" type="primary">查看</el-button>
            <el-button size="small">编辑</el-button>
          </div>
        </div>
      </template>
    </ResponsiveTable>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ResponsiveTable from '@/components/common/ResponsiveTable.vue'

const tableData = ref([
  {
    id: 1,
    name: '社区环保志愿活动',
    organizer: '张老师',
    location: '中央公园',
    startTime: '2024-01-15 09:00',
    endTime: '2024-01-15 17:00',
    status: '进行中'
  },
  {
    id: 2,
    name: '敬老院慰问活动',
    organizer: '李老师',
    location: '阳光敬老院',
    startTime: '2024-01-20 14:00',
    endTime: '2024-01-20 16:00',
    status: '已结束'
  },
  {
    id: 3,
    name: '图书馆整理志愿服务',
    organizer: '王老师',
    location: '市图书馆',
    startTime: '2024-01-25 10:00',
    endTime: '2024-01-25 12:00',
    status: '未开始'
  },
  {
    id: 4,
    name: '交通安全宣传活动',
    organizer: '赵老师',
    location: '学校门口',
    startTime: '2024-02-01 08:00',
    endTime: '2024-02-01 10:00',
    status: '未开始'
  }
])

const getStatusType = (status) => {
  switch (status) {
    case '进行中':
      return 'success'
    case '已结束':
      return 'info'
    case '未开始':
      return 'warning'
    default:
      return ''
  }
}
</script>

<style lang="scss" scoped>
.responsive-table-example {
  padding: var(--space-6, 24px);
  
  h3 {
    color: var(--text-color-primary, #303133);
    margin-bottom: var(--space-4, 16px);
  }
  
  p, ul {
    color: var(--text-color-secondary, #909399);
    margin-bottom: var(--space-4, 16px);
  }
  
  ul {
    padding-left: var(--space-5, 20px);
    
    li {
      margin-bottom: var(--space-2, 8px);
    }
  }
}

.mobile-card {
  background: var(--bg-color-base, #ffffff);
  border-radius: var(--border-radius-card, 8px);
  padding: var(--space-4, 16px);
  box-shadow: var(--card-shadow, 0 2px 8px rgba(0, 0, 0, 0.06));
  transition: all var(--transition-duration, 0.3s) var(--transition-timing, ease);
  
  &:hover {
    box-shadow: var(--card-shadow-hover, 0 4px 12px rgba(0, 0, 0, 0.1));
    transform: translateY(-2px);
  }
  
  .card-header {
    font-weight: 600;
    font-size: var(--font-size-large, 16px);
    color: var(--text-color-primary, #303133);
    margin-bottom: var(--space-3, 12px);
    padding-bottom: var(--space-2, 8px);
    border-bottom: 1px solid var(--border-color-lighter, #ebeef5);
  }
  
  .card-field {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--space-2, 8px) 0;
    
    .field-label {
      font-size: var(--font-size-small, 13px);
      color: var(--text-color-secondary, #909399);
      min-width: 80px;
    }
    
    .field-value {
      font-size: var(--font-size-base, 14px);
      color: var(--text-color-primary, #303133);
      text-align: right;
    }
  }
  
  .card-actions {
    margin-top: var(--space-3, 12px);
    padding-top: var(--space-2, 8px);
    border-top: 1px solid var(--border-color-lighter, #ebeef5);
    display: flex;
    gap: var(--space-2, 8px);
    justify-content: flex-end;
  }
}
</style>