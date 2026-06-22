<template>
  <!--
    订单管理页面 —— 最复杂的业务页面
    功能：订单列表搜索、创建/编辑订单（含动态明细表）、查看详情、状态管理、删除

    状态流转：PENDING（待处理）→ SHIPPED（已发货）→ COMPLETED（已完成）/ CANCELLED（已取消）
    约束：只有"待处理"状态的订单可以编辑
  -->
  <div class="page-container">
    <el-card>
      <!-- ========== 搜索区域 ========== -->
      <div class="search-bar">
        <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width:160px" @keyup.enter="fetchData" />
        <el-select v-model="query.customerId" placeholder="客户" clearable filterable style="width:200px">
          <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
        </el-select>
        <el-select v-model="query.orderStatus" placeholder="状态" clearable style="width:120px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="已发货" value="SHIPPED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:260px" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="handleAdd" style="margin-left:auto;">新增订单</el-button>
      </div>

      <!-- ========== 数据表格 ========== -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="customerName" label="客户" min-width="140" />
        <el-table-column prop="totalAmount" label="总金额" width="110" />
        <el-table-column prop="orderDate" label="下单日期" width="110">
          <template #default="{ row }">{{ formatDate(row.orderDate, 'YYYY-MM-DD') }}</template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="orderStatusMap[row.orderStatus]?.type || 'info'">
              {{ orderStatusMap[row.orderStatus]?.label || row.orderStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 操作列：详情、编辑（仅待处理可编辑）、状态变更下拉、删除 -->
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">详情</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)" :disabled="row.orderStatus !== 'PENDING'">编辑</el-button>
            <el-dropdown @command="(status) => handleStatusChange(row, status)" style="margin-left:4px">
              <el-button size="small" type="warning">状态<el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="PENDING" :disabled="row.orderStatus === 'PENDING'">待处理</el-dropdown-item>
                  <el-dropdown-item command="SHIPPED" :disabled="row.orderStatus === 'SHIPPED'">已发货</el-dropdown-item>
                  <el-dropdown-item command="COMPLETED">已完成</el-dropdown-item>
                  <el-dropdown-item command="CANCELLED">已取消</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- ========== 新增/编辑订单对话框 ========== -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑订单' : '新增订单'" width="800px" @close="resetForm">
      <!-- 订单主体信息 -->
      <el-form ref="formRef" :model="orderForm" :rules="orderRules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="orderForm.customerId" placeholder="请选择客户" filterable style="width:100%">
                <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下单日期" prop="orderDate">
              <el-date-picker v-model="orderForm.orderDate" type="datetime" placeholder="选择日期" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="收货地址">
          <el-input v-model="orderForm.deliveryAddress" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <!-- 订单明细：动态表格 -->
      <div style="margin: 16px 0;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
          <strong>订单明细</strong>
          <el-button type="primary" size="small" @click="addItem">添加商品</el-button>
        </div>
        <el-table :data="orderForm.items" border stripe size="small">
          <el-table-column label="商品" min-width="180">
            <template #default="{ row, $index }">
              <!-- 选择商品后自动填充单价 -->
              <el-select v-model="row.productId" placeholder="选择商品" filterable style="width:100%" @change="(val) => onProductChange($index, val)">
                <el-option v-for="p in productList" :key="p.id" :label="`${p.productName} (￥${p.price})`" :value="p.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="90">
            <template #default="{ row }">￥{{ row.productPrice || 0 }}</template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :max="99" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }">￥{{ (row.productPrice * row.quantity).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button size="small" type="danger" text @click="removeItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 动态计算总金额 -->
        <div style="text-align:right;margin-top:12px;font-size:16px;font-weight:bold;">
          总金额：<span style="color:#f56c6c;">￥{{ totalAmount.toFixed(2) }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- ========== 订单详情对话框（只读） ========== -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detail.order?.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ detail.order?.customerName }}</el-descriptions-item>
          <el-descriptions-item label="总金额">￥{{ detail.order?.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="orderStatusMap[detail.order?.orderStatus]?.type">
              {{ orderStatusMap[detail.order?.orderStatus]?.label }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单日期">{{ formatDate(detail.order?.orderDate) }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ detail.order?.deliveryAddress }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.items" border stripe size="small" style="margin-top:16px;">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="productPrice" label="单价" width="80" />
          <el-table-column prop="quantity" label="数量" width="60" />
          <el-table-column prop="subtotal" label="小计" width="100" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 订单管理 —— 组件逻辑
 *
 * 核心数据流：
 * - customerList: 所有客户（供下拉选择）
 * - productList:  所有商品（供明细选择）
 * - orderForm.items: 动态数组，支持添加/删除行
 * - totalAmount: 计算属性，自动汇总所有明细
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, createOrder, updateOrder, deleteOrder, updateOrderStatus } from '@/api/order'
import { getAllCustomers } from '@/api/customer'
import { getProductList } from '@/api/product'
import { formatDate, orderStatusMap } from '@/utils'

const route = useRoute()

// ========== 表格状态 ==========
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const customerList = ref([])       // 客户下拉选项
const productList = ref([])        // 商品下拉选项
const dateRange = ref(null)        // 日期范围筛选

// 查询参数
const query = reactive({
  page: 1, size: 10, orderNo: '', customerId: null, orderStatus: '', startDate: '', endDate: ''
})

// ========== 新增/编辑对话框 ==========
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

// 订单表单数据
const orderForm = reactive({
  customerId: null, orderDate: '', deliveryAddress: '', remark: '',
  items: [] // [{ productId, productPrice, quantity }]
})

const orderRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  orderDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

/**
 * 总金额计算属性
 * 自动对所有明细的 (单价 × 数量) 求和
 */
const totalAmount = computed(() => {
  return orderForm.items.reduce((sum, item) => sum + (item.productPrice || 0) * (item.quantity || 0), 0)
})

// ========== 详情对话框 ==========
const detailVisible = ref(false)
const detail = ref(null)

// ========== 方法 ==========

/** 重置表单 */
function resetForm() {
  formRef.value?.resetFields()
  orderForm.customerId = null
  orderForm.orderDate = ''
  orderForm.deliveryAddress = ''
  orderForm.remark = ''
  orderForm.items = []
  editId.value = null
}

/** 获取订单列表 */
async function fetchData() {
  loading.value = true
  try {
    // 日期范围参数转换
    if (dateRange.value) {
      query.startDate = dateRange.value[0]
      query.endDate = dateRange.value[1]
    } else {
      query.startDate = ''
      query.endDate = ''
    }
    const res = await getOrderList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

/** 重置搜索条件 */
function resetSearch() {
  query.orderNo = ''
  query.customerId = null
  query.orderStatus = ''
  dateRange.value = null
  fetchData()
}

/** 加载客户和商品下拉数据 */
async function loadSelectData() {
  const [custRes, prodRes] = await Promise.all([
    getAllCustomers(),
    getProductList({ page: 1, size: 1000 }) // 获取全量商品供选择
  ])
  customerList.value = custRes.data
  productList.value = prodRes.data.records || []
}

/** 新增订单 */
function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

/** 编辑订单：先加载订单详情再展示对话框 */
async function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  const res = await getOrderDetail(row.id)
  const data = res.data
  orderForm.customerId = data.order.customerId
  orderForm.orderDate = data.order.orderDate
  orderForm.deliveryAddress = data.order.deliveryAddress
  orderForm.remark = data.order.remark
  // 将已有明细映射到表单格式
  orderForm.items = (data.items || []).map(item => ({
    productId: item.productId,
    productPrice: item.productPrice,
    quantity: item.quantity
  }))
  dialogVisible.value = true
}

/** 提交订单（新增/编辑） */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (orderForm.items.length === 0) {
    ElMessage.warning('请至少添加一个商品')
    return
  }
  submitting.value = true
  try {
    // 构建请求载荷（只发送必要的字段）
    const payload = {
      customerId: orderForm.customerId,
      orderDate: orderForm.orderDate,
      deliveryAddress: orderForm.deliveryAddress,
      remark: orderForm.remark,
      items: orderForm.items.map(i => ({ productId: i.productId, quantity: i.quantity }))
    }
    if (isEdit.value) {
      await updateOrder(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createOrder(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData() // 刷新列表
  } finally {
    submitting.value = false
  }
}

/** 删除订单（确认后） */
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除订单 "${row.orderNo}"？`, '提示', { type: 'warning' })
  await deleteOrder(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

/** 修改订单状态 */
async function handleStatusChange(row, status) {
  await updateOrderStatus(row.id, status)
  ElMessage.success('状态更新成功')
  fetchData()
}

/** 查看订单详情 */
async function handleDetail(row) {
  const res = await getOrderDetail(row.id)
  detail.value = res.data
  detailVisible.value = true
}

/** 添加一行商品明细 */
function addItem() {
  orderForm.items.push({ productId: null, productPrice: 0, quantity: 1 })
}

/** 删除一行商品明细 */
function removeItem(index) {
  orderForm.items.splice(index, 1)
}

/** 选择商品后自动填充单价 */
function onProductChange(index, productId) {
  const product = productList.value.find(p => p.id === productId)
  if (product) {
    orderForm.items[index].productPrice = product.price
  }
}

// 页面初始化：加载订单列表和下拉数据
onMounted(() => {
  fetchData()
  loadSelectData()
  if (route.query.action === 'add') {
    handleAdd()
  }
})
</script>
