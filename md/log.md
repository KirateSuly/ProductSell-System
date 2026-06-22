# 开发日志

## 前端统计图表优化 — 销售趋势拆分 & 热销商品调整

**日期**: 2026-06-10

**需求**:
1. 销售趋势模块中，订单数和销售额原先共用一个折线图，Y 轴刻度不匹配（订单数几十 vs 销售额数万）
2. 热销商品从 Top 10 改为 Top 5，并确保所有 5 个商品名称完整显示

**修改内容**:

1. **销售趋势拆分为两个独立折线图**：
   - 原 `salesTrendOption`（单个双线图）拆为 `orderCountTrendOption` + `salesAmountTrendOption`
   - 订单数折线图：Y 轴标签为"订单数"，蓝色 (`#5470c6`)
   - 销售额折线图：Y 轴标签为"销售额(元)"，绿色 (`#91cc75`)
   - 各自拥有独立 Y 轴刻度，趋势变化更清晰

2. **布局调整**：第一行从 2 列 (`span=12` × 2) 改为 3 列 (`span=8` × 3)，容纳两个趋势图 + 热销商品

3. **热销商品改为 Top 5**：
   - 模板标题从 `Top 10` 改为 `Top 5`
   - API 请求参数 `limit: 10` → `limit: 5`

4. **X 轴标签完整显示**：`topProductsOption` 的 xAxis 添加 `axisLabel: { interval: 0, rotate: 30 }`，强制显示全部 5 个商品名

**涉及文件**:
- [StatisticsView.vue](frontend/src/views/statistics/StatisticsView.vue) — 模板布局、图表配置、数据加载逻辑

## AI 助手 — apikey暴露问题

**日期**: 2026-06-12

**问题**: git上传到公共仓库，存在apikey明文出现问题，以及后续安全隐患，暴露apikey问题。

**原因**：apikey存在配置文件application.yml中，上传配置容易暴露。

**修复**：将Apikey移到本地.vscode/launch.json 的 env 中（启动项），而在application.yml中填入${AI_API_KEY}无效字段。

**涉及文件**：launch.json、application.yml

## AI 助手 — 侧边栏菜单跳转修复

**日期**: 2026-06-12

**问题**: 点击侧边栏"AI 助手"菜单项后无法正确打开 AI 聊天抽屉，而是跳转到首页或其他页面。

**原因**: `el-menu` 的 `router` 属性会将每个 `el-menu-item` 的 `index` 值当作 Vue Router 路径进行 `router.push(index)` 跳转。"AI 助手"菜单的 `index="ai"` 被解析为路由 `/ai`，该路由不存在，导致跳转到首页。`@click.prevent` 只能阻止 DOM 事件，无法拦截 Element Plus 内部的 router 行为。

**修复**:

1. **去掉 `el-menu` 的 `router` 属性**，改用 `@select` 事件手动控制导航
2. **新增 `handleMenuSelect(index)` 方法**：
   - `index === 'ai'` → 调用 `appStore.openAIChat()` 打开抽屉
   - 其他 `index` → 调用 `router.push(index)` 正常跳转
3. 引入 `useRouter`，删除不再需要的 `handleAIClick` 方法

**涉及文件**:

- [SidebarMenu.vue](frontend/src/components/layout/SidebarMenu.vue) — 修改 `el-menu` 属性和脚本逻辑
