<template>
  <div class="role-management">
    <div class="page-header">
      <h1>角色管理</h1>
    </div>

    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="输入用户ID" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="10">
        <el-card>
          <template #header>系统角色</template>
          <el-table :data="roleList" :loading="loadingRoles" height="360" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="角色标识" width="160" />
            <el-table-column prop="description" label="描述" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="14">
        <el-card>
          <template #header>用户角色</template>
          <div v-if="currentUserRole" class="user-role-display">
            <el-tag :type="getRoleTagType(currentUserRole.code)" size="large">
              {{ currentUserRole.description }}
            </el-tag>
            <p class="role-code">角色代码: {{ currentUserRole.code }}</p>
          </div>
          <div v-else class="no-role">
            <el-empty description="未查询到用户角色" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="assign-card">
      <template #header>分配角色</template>
      <el-form :model="assignForm" inline>
        <el-form-item label="选择角色">
          <el-select v-model="assignForm.role" placeholder="请选择角色" style="width: 240px">
            <el-option v-for="r in roleList" :key="r.code" :label="r.description" :value="r.code" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!assignForm.role || !currentUserId" @click="assignRole">分配</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleAPI } from '@/api'

const searchForm = reactive({ userId: '' })
const roleList = ref([])
const currentUserRole = ref(null)
const currentUserId = ref('')

const loadingRoles = ref(false)
const loadingUserRoles = ref(false)

const fetchRoles = async () => {
  loadingRoles.value = true
  try {
    const resp = await roleAPI.getAll()
    roleList.value = resp.data || []
  } catch (e) { console.error(e) } finally { loadingRoles.value = false }
}

const handleSearch = async () => {
  if (!searchForm.userId) return
  currentUserId.value = searchForm.userId
  loadingUserRoles.value = true
  try {
    const resp = await roleAPI.getUserRole(currentUserId.value)
    currentUserRole.value = resp.data || null
  } catch (e) {
    console.error(e)
    currentUserRole.value = null
  } finally {
    loadingUserRoles.value = false
  }
}

const assignForm = reactive({ role: '' })
const assignRole = async () => {
  try {
    await roleAPI.assignToUser(currentUserId.value, { role: assignForm.role })
    ElMessage.success('分配成功')
    assignForm.role = ''
    handleSearch()
  } catch (e) {
    console.error(e)
  }
}

const getRoleTagType = (roleCode) => {
  switch (roleCode) {
    case 'ADMIN': return 'danger'
    case 'TEACHER': return 'warning'
    case 'STUDENT': return 'success'
    default: return 'info'
  }
}

onMounted(() => {
  fetchRoles()
})
</script>

<style scoped lang="scss">
.role-management {
  .page-header { margin-bottom: 16px; }
  .search-card { margin-bottom: 16px; }
  .assign-card { margin-top: 16px; }

  .user-role-display {
    text-align: center;
    padding: 40px 20px;

    .role-code {
      margin-top: 12px;
      color: #666;
      font-size: 14px;
    }
  }

  .no-role {
    padding: 40px 20px;
  }
}
</style>

