<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="商品名称/编号/品牌" clearable style="width:220px" @keyup.enter="fetchData" />
        <el-select v-model="query.category" placeholder="分类" clearable style="width:140px">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width:100px">
          <el-option label="在售" :value="1" />
          <el-option label="停售" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="handleAdd" style="margin-left:auto;">新增商品</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.imageUrl" :src="row.imageUrl" style="width:50px;height:50px" fit="cover" />
            <span v-else style="color:#ccc">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="productCode" label="商品编号" width="130" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="price" label="售价" width="90" />
        <el-table-column prop="stockQuantity" label="库存" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在售' : '停售' }}</el-tag>
          </template>
        </el-table-column>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="650px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" filterable allow-create placeholder="请选择或输入分类" style="width:100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="可空"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号">
              <el-input v-model="form.specification" placeholder="可空"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :precision="2" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="成本价">
              <el-input-number v-model="form.costPrice" :precision="2" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位">
              <el-input v-model="form.unit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="库存数量">
              <el-input-number v-model="form.stockQuantity" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="在售" inactive-text="停售" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="商品图片">
          <ImageUpload v-model="form.imageUrl" />
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
import { getProductList, createProduct, updateProduct, deleteProduct } from '@/api/product'
import ImageUpload from '@/components/common/ImageUpload.vue'

const route = useRoute()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const query = reactive({ page: 1, size: 10, keyword: '', category: '', status: null })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({
  productName: '', category: '', brand: '', specification: '', unit: '个',
  price: 0, costPrice: 0, stockQuantity: 0, description: '', imageUrl: '', status: 1
})

const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { productName: '', category: '', brand: '', specification: '', unit: '个', price: 0, costPrice: 0, stockQuantity: 0, description: '', imageUrl: '', status: 1 })
  editId.value = null
}

async function fetchData() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.status && params.status !== 0) delete params.status
    const res = await getProductList(params)
    tableData.value = res.data.records
    total.value = res.data.total
    // 提取现有分类
    const cats = new Set(categories.value)
    tableData.value.forEach(p => { if (p.category) cats.add(p.category) })
    categories.value = [...cats]
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  query.keyword = ''
  query.category = ''
  query.status = null
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
    if (isEdit.value) {
      await updateProduct(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createProduct({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除商品 "${row.productName}"？`, '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => {
  fetchData()
  // 从快捷操作跳转过来时自动打开新增对话框
  if (route.query.action === 'add') {
    handleAdd()
  }
})
</script>
