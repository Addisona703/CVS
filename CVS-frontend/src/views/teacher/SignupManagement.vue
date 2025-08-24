<template>
  <div class="signup-management">
    <div class="page-header">
      <h1>报名管理</h1>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动名称">
          <el-select v-model="searchForm.activityId" placeholder="请选择活动" clearable>
            <el-option
              v-for="activity in myActivities"
              :key="activity.id"
              :label="activity.title"
              :value="activity.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 报名列表 -->
    <el-card>
      <div class="table-header">
        <div class="batch-actions">
          <el-button 
            type="success" 
            :disabled="selectedSignups.length === 0"
            @click="batchApprove"
          >
            批量通过
          </el-button>
          <el-button 
            type="danger" 
            :disabled="selectedSignups.length === 0"
            @click="batchReject"
          >
            批量拒绝
          </el-button>
        </div>
      </div>
      
      <el-table 
        :data="signupList" 
        :loading="loading" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="activityTitle" label="活动名称" min-width="200" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signupTime" label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.signupTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="success" 
              size="small" 
              @click="approveSignup(row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="danger" 
              size="small" 
              @click="rejectSignup(row)"
            >
              拒绝
            </el-button>
            <el-button 
              v-if="row.status === 'APPROVED'" 
              type="primary" 
              size="small" 
              @click="checkIn(row)"
            >
              签到
            </el-button>
            <el-button 
              v-if="row.checkedIn && !row.checkedOut" 
              type="warning" 
              size="small" 
              @click="checkOut(row)"
            >
              签退
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { signupAPI, activityAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { STATUS_LABELS, STATUS_COLORS } from '@/utils/constants'
import { usePagination } from '@/composables/usePagination'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const signupList = ref([])
const myActivities = ref([])
const selectedSignups = ref([])

const searchForm = reactive({
  activityId: '',
  studentName: '',
  status: ''
})

const getStatusLabel = (status) => {
  return STATUS_LABELS[status] || status
}

const getStatusType = (status) => {
  const colorMap = {
    success: 'success',
    danger: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return colorMap[STATUS_COLORS[status]] || 'info'
}

const fetchMyActivities = async () => {
  try {
    const response = await activityAPI.getActivityList({ createdByMe: true })
    if (response.code === 200) {
      myActivities.value = response.data.records || []

      if (myActivities.value.length > 0) {
        searchForm.activityId = myActivities.value[0].id
        await fetchSignupList()
      }
    }
  } catch (error) {
    console.error('获取我的活动失败:', error)
    // 使用模拟数据
    myActivities.value = [
      { id: 1, title: '社区环保志愿活动' },
      { id: 2, title: '敬老院志愿服务' }
    ]
  }
}

const fetchSignupList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const response = await signupAPI.getMyActivitySignups(searchForm.activityId, params)
    if (response.code === 200) {
      signupList.value = response.data.records
      updatePagination(response.data)
    }
  } catch (error) {
    console.error('获取报名列表失败:', error)
    // 使用模拟数据
    signupList.value = [
      {
        id: 1,
        activityTitle: '社区环保志愿活动',
        studentName: '张三',
        studentId: '2021001',
        phone: '13800138001',
        email: 'zhangsan@example.com',
        status: 'PENDING',
        signupTime: '2024-01-15T10:30:00',
        checkedIn: false,
        checkedOut: false
      },
      {
        id: 2,
        activityTitle: '社区环保志愿活动',
        studentName: '李四',
        studentId: '2021002',
        phone: '13800138002',
        email: 'lisi@example.com',
        status: 'APPROVED',
        signupTime: '2024-01-15T11:00:00',
        checkedIn: true,
        checkedOut: false
      }
    ]
    updatePagination({
      current: 1,
      size: 10,
      total: 2,
      pages: 1
    })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchSignupList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    activityId: '',
    studentName: '',
    status: ''
  })
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedSignups.value = selection
}

const approveSignup = async (row) => {
  try {
    await signupAPI.approveSignup(row.id, true)
    ElMessage.success('审核通过')
    fetchSignupList()
  } catch (error) {
    console.error('审核失败:', error)
  }
}

const rejectSignup = async (row) => {
  try {
    await ElMessageBox.prompt(
      '请输入拒绝原因',
      '拒绝报名',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入拒绝原因'
      }
    )
    
    await signupAPI.approveSignup(row.id, false)
    ElMessage.success('已拒绝')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
    }
  }
}

const batchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量通过选中的 ${selectedSignups.value.length} 个报名吗？`,
      '批量审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    for (const signup of selectedSignups.value) {
      if (signup.status === 'PENDING') {
        await signupAPI.approveSignup(signup.id, true)
      }
    }
    
    ElMessage.success('批量审核完成')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
    }
  }
}

const batchReject = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量拒绝选中的 ${selectedSignups.value.length} 个报名吗？`,
      '批量拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    for (const signup of selectedSignups.value) {
      if (signup.status === 'PENDING') {
        await signupAPI.approveSignup(signup.id, false)
      }
    }
    
    ElMessage.success('批量拒绝完成')
    fetchSignupList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
    }
  }
}

const checkIn = async (row) => {
  try {
    await signupAPI.checkIn(row.id)
    ElMessage.success('签到成功')
    fetchSignupList()
  } catch (error) {
    console.error('签到失败:', error)
  }
}

const checkOut = async (row) => {
  try {
    await signupAPI.checkOut(row.id)
    ElMessage.success('签退成功')
    fetchSignupList()
  } catch (error) {
    console.error('签退失败:', error)
  }
}

onMounted(() => {
  fetchMyActivities()
})
</script>

<style lang="scss" scoped>
.signup-management {
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      color: #303133;
    }
  }
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-header {
    margin-bottom: 16px;
    
    .batch-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
