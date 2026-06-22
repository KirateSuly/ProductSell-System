<template>
  <div class="page-container">
    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="用户名/姓名/手机号" clearable style="width: 240px" @clear="fetchData" @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="handleAdd" style="margin-left:auto;">新增用户</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :disabled="row.username === 'admin'" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
        style="margin-top:16px;justify-content:flex-end;"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%" :disabled="form.username === 'admin'">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" :disabled="form.username === 'admin'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { formatDate } from '@/utils'
import { sha256 } from '@/utils/crypto'

const route = useRoute()

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({
  username: '', password: '', realName: '', phone: '', email: '', role: 'USER', status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { username: '', password: '', realName: '', phone: '', email: '', role: 'USER', status: 1 })
  editId.value = null
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList({ page: page.value, size: size.value, keyword: keyword.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = { ...form }
    // SHA-256 脱敏后再发送，与服务端 BCrypt(SHA-256(明文)) 存储格式匹配
    if (form.password) {
      payload.password = await sha256(form.password)
    }
    if (isEdit.value) {
      await updateUser(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createUser(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户 "${row.username}"？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => {
  fetchData()
  if (route.query.action === 'add') {
    handleAdd()
  }
})
</script>
