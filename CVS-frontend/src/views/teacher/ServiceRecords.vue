<template>
  <div class="service-records">
    <div class="page-header">
      <h1>服务记录</h1>
      <el-button v-if="isAdmin" type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加记录
      </el-button>
      <el-button type="primary" plain @click="openGenerateDialog">
        自动生成记录
      </el-button>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动">
          <el-select v-model="searchForm.activityId" placeholder="请选择活动" clearable style="min-width: 260px">
            <el-option
              v-for="activity in myActivities"
              :key="activity.id"
              :label="activity.title"
              :value="activity.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 记录列表 -->
    <el-card>
      <el-table :data="recordList" :loading="loading" stripe>
        <el-table-column prop="activityTitle" label="活动" min-width="200" />
        <el-table-column prop="userName" label="学生" width="140" />
        <el-table-column label="时长" width="120">
          <template #default="{ row }">{{ row.durationHours }} 小时</template>
        </el-table-column>
        <el-table-column prop="evaluation" label="评价" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
    
    <!-- 添加/编辑记录对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingRecord ? '编辑服务记录' : '添加服务记录'"
      width="600px"
    >
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-form-item label="活动" prop="activityId">
          <el-select v-model="recordForm.activityId" placeholder="请选择活动">
            <el-option
              v-for="activity in myActivities"
              :key="activity.id"
              :label="activity.title"
              :value="activity.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学生" prop="userId">
          <el-select v-model="recordForm.userId" placeholder="请选择学生" filterable>
            <el-option
              v-for="student in activityStudents"
              :key="student.userId || student.id"
              :label="student.userName || student.name"
              :value="student.userId || student.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务时长" prop="durationHours">
          <el-input-number
            v-model="durationHours"
            :min="0.5"
            :max="24"
            :step="0.5"
            placeholder="请输入服务时长"
            style="width: 100%"
          />
          <span style="margin-left: 8px; color: #909399;">小时</span>
        </el-form-item>
        <el-form-item label="评价" prop="evaluation">
          <el-input
            v-model="recordForm.evaluation"
            type="textarea"
            :rows="3"
            placeholder="可选"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { activityAPI, signupAPI, serviceRecordAPI } from '@/api'
import { formatDateTime } from '@/utils/format'
import { usePagination } from '@/composables/usePagination'
import { usePermission } from '@/composables/usePermission'

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const { hasRole } = usePermission()
const isAdmin = computed(() => hasRole('ADMIN'))

const recordList = ref([])
const myActivities = ref([])
const activityStudents = ref([])
const showAddDialog = ref(false)
const editingRecord = ref(null)
const submitting = ref(false)
const recordFormRef = ref()

const searchForm = reactive({
  activityId: '',
  studentName: '',
  dateRange: []
})

const recordForm = reactive({
  activityId: '',
  userId: '',
  evaluation: ''
})

const recordRules = {
  activityId: [
    { required: true, message: '请选择活动', trigger: 'change' }
  ],
  userId: [
    { required: true, message: '请选择学生', trigger: 'change' }
  ]
}

const fetchMyActivities = async () => {
  try {
    const response = await activityAPI.getMyActivities({ current: 1, size: 100 })
    if (response.code === 200) {
      myActivities.value = response.data.records || []
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

const fetchActivityStudents = async (activityId) => {
  if (!activityId) {
    activityStudents.value = []
    return
  }
  
  try {
    const response = await signupAPI.getMyActivitySignups(activityId, { status: 'APPROVED' })
    if (response.code === 200) {
      activityStudents.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取活动学生失败:', error)
    // 使用模拟数据
    activityStudents.value = [
      { id: 1, name: '张三', studentId: '2021001' },
      { id: 2, name: '李四', studentId: '2021002' }
    ]
  }
}

const fetchRecordList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }

    // 使用真实接口：按活动筛选
    if (searchForm.activityId) {
      const resp = await serviceRecordAPI.getByActivity(searchForm.activityId, params)
      recordList.value = resp.data?.records || []
      updatePagination(resp.data || { current: 1, size: params.size, total: 0, pages: 0 })
      return
    }

    // Fallback：未选择活动时清空
    recordList.value = []
    updatePagination({ current: pagination.current, size: pagination.size, total: 0, pages: 0 })
  } catch (error) {
    console.error('获取服务记录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchRecordList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    activityId: '',
    studentName: '',
    dateRange: []
  })
  handleSearch()
}

const handleEdit = (row) => {
  editingRecord.value = row
  Object.assign(recordForm, {
    activityId: row.activityId,
    userId: row.userId,
    evaluation: row.evaluation || ''
  })
  durationHours.value = row.durationHours || (row.durationMinutes ? row.durationMinutes / 60 : 1)
  showAddDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${row.studentName}" 的服务记录吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await serviceRecordAPI.remove(row.id)
    ElMessage.success('删除成功')
    fetchRecordList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    await recordFormRef.value.validate()
    submitting.value = true
    
    const payload = {
      activityId: recordForm.activityId,
      userId: recordForm.userId,
      durationMinutes: Math.round(durationHours.value * 60),
      evaluation: recordForm.evaluation || undefined
    }
    if (editingRecord.value) {
      await serviceRecordAPI.update(editingRecord.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      // 仅 ADMIN 允许手工创建
      if (!isAdmin.value) throw new Error('无权限创建')
      await serviceRecordAPI.create(payload)
      ElMessage.success('添加成功')
    }
    
    showAddDialog.value = false
    resetForm()
    fetchRecordList()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  editingRecord.value = null
  Object.assign(recordForm, {
    activityId: '',
    userId: '',
    evaluation: ''
  })
  recordFormRef.value?.resetFields()
}

// 监听活动选择变化
watch(() => recordForm.activityId, (newActivityId) => {
  recordForm.studentId = ''
  fetchActivityStudents(newActivityId)
})

onMounted(() => {
  fetchMyActivities()
  fetchRecordList()
})
</script>

<style lang="scss" scoped>
.service-records {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
