<template>
  <div class="category-management">
    <div class="page-header">
      <h1>分类管理</h1>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加分类
      </el-button>
    </div>
    
    <!-- 分类列表 -->
    <el-card>
      <el-table :data="categoryList" :loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="分类描述" min-width="200" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="productCount" label="商品数量" width="100">
          <template #default="{ row }">
            <el-tag :type="row.productCount > 0 ? 'success' : 'info'">
              {{ row.productCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              :disabled="row.productCount > 0"
              @click="deleteCategory(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
      @close="resetForm"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入分类描述" 
          />
        </el-form-item>
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number 
            v-model="form.sortOrder" 
            :min="0" 
            placeholder="请输入排序顺序" 
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { mallAPI } from '@/api'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const categoryList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  description: '',
  sortOrder: 0
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 100, message: '分类名称不能超过100字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '分类描述不能超过500字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序顺序', trigger: 'blur' },
    { type: 'number', min: 0, message: '排序顺序不能为负数', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const response = await mallAPI.getCategories()
    if (response.code === 200) {
      categoryList.value = response.data
      // 获取每个分类的商品数量
      await fetchProductCounts()
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 获取每个分类的商品数量
const fetchProductCounts = async () => {
  try {
    for (const category of categoryList.value) {
      const response = await mallAPI.getProductList({ 
        categoryId: category.id,
        pageSize: 1 
      })
      if (response.code === 200) {
        category.productCount = response.data.total
      }
    }
  } catch (error) {
    console.error('获取商品数量失败:', error)
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    description: row.description || '',
    sortOrder: row.sortOrder || 0
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    name: '',
    description: '',
    sortOrder: 0
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await mallAPI.updateCategory(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await mallAPI.createCategory(form)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchCategoryList()
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      console.error('提交失败:', error)
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  } finally {
    submitting.value = false
  }
}

// 删除分类
const deleteCategory = async (row) => {
  // 检查是否有商品
  if (row.productCount > 0) {
    ElMessage.warning('该分类下存在商品，无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类 "${row.name}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await mallAPI.deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchCategoryList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      if (error.response?.data?.message?.includes('商品')) {
        ElMessage.error('该分类下存在商品，无法删除')
      } else {
        ElMessage.error('删除失败')
      }
    }
  }
}

onMounted(() => {
  fetchCategoryList()
})
</script>

<style lang="scss" scoped>
.category-management {
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
}
</style>