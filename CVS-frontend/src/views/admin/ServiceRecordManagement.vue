<template>
  <div class="admin-service-records">
    <div class="page-header">
      <h1>服务记录管理</h1>
      <div class="actions">
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><plus /></el-icon>
          新建记录
        </el-button>
        <el-button @click="openGenerateDialog">
          <el-icon><MagicStick /></el-icon>
          自动生成记录
        </el-button>
      </div>
    </div>

    <!-- 查询筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="输入用户ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="活动ID">
          <el-input v-model="searchForm.activityId" placeholder="输入活动ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card>
      <el-table :data="recordList" :loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="用户" width="140" />
        <el-table-column prop="activityTitle" label="活动" min-width="180" />
        <el-table-column prop="durationHours" label="时长(小时)" width="120" />
        <el-table-column prop="evaluation" label="评价" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
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

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="showEditDialog" :title="editing ? '编辑记录' : '新建记录'" width="600px">
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model.number="recordForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="活动ID" prop="activityId">
          <el-input v-model.number="recordForm.activityId" placeholder="请输入活动ID" />
        </el-form-item>
        <el-form-item label="服务时长" prop="durationMinutes">
          <el-input-number v-model.number="durationHours" :min="0.5" :step="0.5" :max="24" style="width: 100%" />
          <span style="margin-left: 8px; color: #909399;">小时</span>
        </el-form-item>
        <el-form-item label="评价" prop="evaluation">
          <el-input v-model="recordForm.evaluation" type="textarea" :rows="3" placeholder="请输入评价" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRecord">确定</el-button>
      </template>
    </el-dialog>

    <!-- 自动生成对话框 -->
    <el-dialog v-model="showGenerateDialog" title="自动生成服务记录" width="520px">
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="用户ID">
          <el-input v-model.number="generateForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="活动ID">
          <el-input v-model.number="generateForm.activityId" placeholder="请输入活动ID" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="generateForm.evaluation" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGenerateDialog = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="submitGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import { serviceRecordAPI } from '@/api'
import { usePagination } from '@/composables/usePagination'
import { formatDateTime } from '@/utils/format'
import { usePermission } from '@/composables/usePermission'

const { hasRole } = usePermission()
const isAdmin = computed(() => hasRole('ADMIN'))

const { loading, pagination, handleSizeChange, handleCurrentChange, updatePagination } = usePagination()

const recordList = ref([])

const searchForm = reactive({
  userId: '',
  activityId: ''
})

const showEditDialog = ref(false)
const editing = ref(false)
const submitting = ref(false)
const recordFormRef = ref()

const recordForm = reactive({
  userId: '',
  activityId: '',
  durationMinutes: 60,
  evaluation: ''
})
const durationHours = ref(1)

const recordRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  activityId: [{ required: true, message: '请输入活动ID', trigger: 'blur' }],
  durationMinutes: [{ required: true, message: '请输入服务时长', trigger: 'change' }]
}

const showGenerateDialog = ref(false)
const generating = ref(false)
const generateForm = reactive({ userId: '', activityId: '', evaluation: '' })

const fetchList = async () => {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size }
    let resp
    if (searchForm.userId) {
      resp = await serviceRecordAPI.getByUser(searchForm.userId, params)
    } else if (searchForm.activityId) {
      resp = await serviceRecordAPI.getByActivity(searchForm.activityId, params)
    } else {
      resp = await serviceRecordAPI.getAll(params)
    }
    recordList.value = resp.data?.records || []
    updatePagination(resp.data || { current: 1, size: params.size, total: 0, pages: 0 })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchList()
}
const handleReset = () => {
  searchForm.userId = ''
  searchForm.activityId = ''
  handleSearch()
}

const openCreateDialog = () => {
  editing.value = false
  Object.assign(recordForm, { userId: '', activityId: '', durationMinutes: 60, evaluation: '' })
  durationHours.value = 1
  showEditDialog.value = true
}
const openEditDialog = (row) => {
  editing.value = true
  Object.assign(recordForm, {
    userId: row.userId,
    activityId: row.activityId,
    durationMinutes: row.durationMinutes ?? Math.round((row.durationHours || 1) * 60),
    evaluation: row.evaluation || ''
  })
  durationHours.value = (row.durationHours || (row.durationMinutes / 60) || 1)
  showEditDialog.value = true
}

const submitRecord = async () => {
  try {
    await recordFormRef.value.validate()
    submitting.value = true
    const payload = { ...recordForm, durationMinutes: Math.round(durationHours.value * 60) }
    if (editing.value) {
      const id = recordList.value.find(r => r.userId === recordForm.userId && r.activityId === recordForm.activityId)?.id
      await serviceRecordAPI.update(id, payload)
      ElMessage.success('更新成功')
    } else {
      await serviceRecordAPI.create(payload)
      ElMessage.success('创建成功')
    }
    showEditDialog.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除ID为 ${row.id} 的服务记录吗？`, '提示', { type: 'warning' })
    await serviceRecordAPI.remove(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    // cancelled or error
  }
}

const openGenerateDialog = () => {
  Object.assign(generateForm, { userId: '', activityId: '', evaluation: '' })
  showGenerateDialog.value = true
}
const submitGenerate = async () => {
  try {
    generating.value = true
    await serviceRecordAPI.generate({ userId: generateForm.userId, activityId: generateForm.activityId, evaluation: generateForm.evaluation })
    ElMessage.success('生成成功')
    showGenerateDialog.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    generating.value = false
  }
}

onMounted(() => {
  // 仅 ADMIN 可见此页面，路由已限制
  fetchList()
})
</script>

<style scoped lang="scss">
.admin-service-records {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
  .search-card { margin-bottom: 16px; }
  .pagination { margin-top: 16px; text-align: right; }
}
</style>

