<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="客户名称/编号/联系人/电话" clearable style="width:280px" @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="handleAdd" style="margin-left:auto;">新增客户</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="customerCode" label="客户编号" width="150" />
        <el-table-column prop="customerName" label="客户名称" min-width="160" />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
        :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        @change="fetchData" style="margin-top:16px;justify-content:flex-end;"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑客户' : '新增客户'" width="550px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="客户编号">
          <el-input :model-value="form.customerCode" disabled />
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
import { getCustomerList, createCustomer, updateCustomer, deleteCustomer } from '@/api/customer'

const route = useRoute()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({ page: 1, size: 10, keyword: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({ customerName: '', contactPerson: '', phone: '', email: '', address: '', remark: '', customerCode: '' })

const rules = {
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { customerName: '', contactPerson: '', phone: '', email: '', address: '', remark: '', customerCode: '' })
  editId.value = null
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCustomerList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  query.keyword = ''
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
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = { ...form }
    delete data.customerCode
    if (isEdit.value) {
      await updateCustomer(editId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createCustomer(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除客户 "${row.customerName}"？`, '提示', { type: 'warning' })
  await deleteCustomer(row.id)
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
