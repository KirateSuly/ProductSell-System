<template>
  <!--
    销售统计页面
    使用 ECharts (vue-echarts) 渲染图表：
    - 概览卡片：4 个关键指标
    - 销售趋势：两个独立折线图（订单数 / 销售额，各自 Y 轴）
    - 热销商品：柱状图 Top 10
    - 分类占比：环形饼图
    - 状态分布：饼图

    所有图表支持按日期范围筛选，数据从 /api/statistics/* 端点获取。
  -->
  <div class="page-container">
    <h3 style="margin-bottom: 16px;">销售统计</h3>

    <!-- 日期范围筛选 -->
    <div style="margin-bottom: 16px;">
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
        start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
        @change="loadAll" style="margin-right: 12px;" />
      <el-button @click="dateRange = null; loadAll()">清除筛选</el-button>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="item in overviewCards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行1：订单数趋势 + 销售额趋势 + 热销商品 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card>
          <template #header>订单数趋势（按月）</template>
          <v-chart :option="orderCountTrendOption" style="height: 350px;" autoresize />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>销售额趋势（按月）</template>
          <v-chart :option="salesAmountTrendOption" style="height: 350px;" autoresize />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>热销商品 Top 5</template>
          <v-chart :option="topProductsOption" style="height: 350px;" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行2：分类销售 + 订单状态 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header>各类销售占比</template>
          <v-chart :option="categorySalesOption" style="height: 350px;" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>订单状态分布</template>
          <v-chart :option="orderStatusOption" style="height: 350px;" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 统计图表 —— ECharts 集成
 *
 * 使用 vue-echarts 的 <v-chart> 组件，通过 reactive option 对象驱动图表更新。
 * 选择按需导入 ECharts 模块以减小打包体积。
 */
import { ref, reactive, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { getOverview, getSalesTrend, getTopProducts, getCategorySales, getOrderStatus } from '@/api/statistics'

// 按需注册 ECharts 组件和图表类型
use([CanvasRenderer, LineChart, BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

// ========== 状态 ==========
const dateRange = ref(null)

// 概览卡片数据
const overviewCards = ref([
  { label: '在售商品数', value: 0 },
  { label: '客户总数', value: 0 },
  { label: '订单总数', value: 0 },
  { label: '总销售额(元)', value: 0 }
])

// ========== 图表配置（reactive 驱动 ECharts 响应式更新）==========

/** 订单数趋势 —— 折线图（独立 Y 轴） */
const orderCountTrendOption = reactive({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value', name: '订单数' },
  series: [
    { name: '订单数', type: 'line', data: [], smooth: true, itemStyle: { color: '#5470c6' } }
  ]
})

/** 销售额趋势 —— 折线图（独立 Y 轴，单位：元） */
const salesAmountTrendOption = reactive({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value', name: '销售额(元)' },
  series: [
    { name: '销售额', type: 'line', data: [], smooth: true, itemStyle: { color: '#91cc75' } }
  ]
})

/** 热销商品 —— 横向柱状图 */
const topProductsOption = reactive({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  xAxis: { type: 'category', data: [] ,},
  yAxis: { type: 'value' },
  series: [{ name: '销量', type: 'bar', data: [], itemStyle: { color: '#409eff' } }]
})

/** 分类销售 —— 环形饼图 */
const categorySalesOption = reactive({
  tooltip: { trigger: 'item' },
  series: [{
    name: '分类销售', type: 'pie', radius: ['40%', '70%'], data: [],
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
  }]
})

/** 订单状态分布 —— 饼图 */
const orderStatusOption = reactive({
  tooltip: { trigger: 'item' },
  series: [{
    name: '订单状态', type: 'pie', radius: '60%', data: [],
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
  }]
})

// ========== 方法 ==========

/** 构建查询参数 */
function getParams() {
  const params = {}
  if (dateRange.value) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  return params
}

/**
 * 加载所有统计图表数据
 * 页面初始化时和日期范围变更时触发
 */
async function loadAll() {
  const params = getParams()
  try {
    // 1. 概览数据
    const overview = await getOverview(params)
    const d = overview.data
    overviewCards.value[0].value = d.totalProducts || 0
    overviewCards.value[1].value = d.totalCustomers || 0
    overviewCards.value[2].value = d.totalOrders || 0
    overviewCards.value[3].value = (d.totalRevenue || 0).toLocaleString()

    // 2. 销售趋势 —— 分别填充两个独立图表
    const trend = await getSalesTrend(params)
    const trendData = trend.data || []
    orderCountTrendOption.xAxis.data = trendData.map(t => t.month)
    orderCountTrendOption.series[0].data = trendData.map(t => t.orderCount)
    salesAmountTrendOption.xAxis.data = trendData.map(t => t.month)
    salesAmountTrendOption.series[0].data = trendData.map(t => t.totalAmount)

    // 3. 热销商品 Top 5
    const top = await getTopProducts({ ...params, limit: 5})
    const topData = top.data || []
    topProductsOption.xAxis.data = topData.map(t => t.productName)
    topProductsOption.series[0].data = topData.map(t => t.totalQuantity)

    // 4. 分类销售占比
    const category = await getCategorySales(params)
    categorySalesOption.series[0].data = (category.data || []).map(c => ({
      name: c.category, value: c.totalAmount
    }))

    // 5. 订单状态分布
    const status = await getOrderStatus(params)
    const statusMap = { PENDING: '待处理', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
    orderStatusOption.series[0].data = (status.data || []).map(s => ({
      name: statusMap[s.status] || s.status, value: s.count
    }))
  } catch {
    // 图表数据加载失败静默处理，不阻塞页面
  }
}

onMounted(loadAll)
</script>
