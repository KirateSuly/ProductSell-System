<template>
  <!--
    系统首页 / 仪表盘
    展示四个概览统计卡片和快捷操作入口。
    数据来自 /api/statistics/overview 接口。
  -->
  <div class="page-container">
    <h3 style="margin-bottom: 16px;">系统首页</h3>

    <!-- 概览统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 24px;">
      <el-col :span="6" v-for="item in overviewCards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作入口 -->
    <el-card>
      <template #header>快捷操作</template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="link in quickLinks" :key="link.path">
          <el-button :icon="link.icon" @click="$router.push(link.path)" style="width:100%;height:80px;">
            {{ link.label }}
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 仪表盘页面
 * 页面挂载时加载概览统计数据，展示关键业务指标。
 */
import { ref, onMounted } from 'vue'
import { getOverview } from '@/api/statistics'
import { Plus, User, Document } from '@element-plus/icons-vue'

// 概览数据
const overviewCards = ref([
  { label: '在售商品数', value: 0 },
  { label: '客户总数', value: 0 },
  { label: '订单总数', value: 0 },
  { label: '总销售额(元)', value: 0 }
])

// 快捷操作链接
const quickLinks = [
  { label: '添加商品', path: '/products?action=add', icon: Plus },
  { label: '添加用户', path: '/users?action=add', icon: User },
  { label: '添加客户', path: '/customers?action=add', icon: User },
  { label: '添加订单', path: '/orders?action=add', icon: Document }
]

/** 加载概览数据 */
onMounted(async () => {
  try {
    const res = await getOverview()
    const data = res.data
    overviewCards.value = [
      { label: '在售商品数', value: data.totalProducts || 0 },
      { label: '客户总数', value: data.totalCustomers || 0 },
      { label: '订单总数', value: data.totalOrders || 0 },
      { label: '总销售额(元)', value: (data.totalRevenue || 0).toLocaleString() }
    ]
  } catch {
    // 加载失败显示默认值 0
  }
})
</script>
