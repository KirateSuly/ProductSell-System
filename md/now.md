# ProductSell 商品销售管理系统 — 答辩讲解文档

## 一、项目概述

**项目定位：** 面向中小型商贸企业的全栈商品销售管理系统，涵盖用户管理、商品管理、客户管理、订单管理、销售统计分析和 AI 智能助手七大功能模块。

**技术栈：**
| 层级 | 技术选型 |
|------|----------|
| 前端 | Vue 3.4 + Vite 5 + Element Plus 2.7 + ECharts 5 + Pinia 2 |
| 后端 | Spring Boot 3.2.5 + Java 17 + MyBatis-Plus 3.5.6 |
| 安全 | Spring Security + JWT (jjwt 0.12.5) + BCrypt |
| 数据库 | MySQL 8.0 |
| AI | Spring AI 1.0.0-M6 + DeepSeek API |

**数据库设计（5 张表）：** sys_user（用户表）、product（商品表）、customer（客户表）、order（订单表）、order_item（订单明细表）

---

## 二、系统架构

```
┌─────────────────────────────────────────────────────┐
│                    前端 (Vue 3 + Vite)               │
│  Login → Dashboard → 用户/商品/客户/订单/统计/AI     │
│  Axios ←→ JWT Token ←→ Pinia Store ←→ LocalStorage  │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP REST API (/api/*)
┌──────────────────────┴──────────────────────────────┐
│              后端 (Spring Boot 3.2)                   │
│  Controller 层 → Service 层 → Mapper 层 → MySQL      │
│  Security Filter → JWT 验证 → 角色鉴权               │
└─────────────────────────────────────────────────────┘
```

**项目结构：**
- `backend/` — Spring Boot 后端，采用经典三层架构（Controller → Service → Mapper）
- `frontend/` — Vue 3 前端，采用组件化开发 + Pinia 状态管理 + Vue Router 路由
- `sql/init.sql` — 数据库建表脚本

---

## 三、各功能模块实现详解

### 3.1 用户认证与权限管理

#### 3.1.1 双层密码哈希方案

```
用户输入密码 → 前端 SHA-256 哈希 → 网络传输 → 后端 BCrypt 加密 → 存入数据库
```

- **前端：** [crypto.js](frontend/src/utils/crypto.js) 使用浏览器 `SubtleCrypto` API 实现 SHA-256，确保密码明文不出浏览器
- **后端：** [PasswordHashUtil.java](backend/src/main/java/com/productsell/utils/PasswordHashUtil.java) 提供相同的 SHA-256 实现；[SecurityConfig.java](backend/src/main/java/com/productsell/config/SecurityConfig.java) 注入 `BCryptPasswordEncoder`，数据库存储的是 `BCrypt(SHA-256(plaintext))`

#### 3.1.2 JWT 无状态认证

**核心文件：** [JwtTokenProvider.java](backend/src/main/java/com/productsell/security/JwtTokenProvider.java)

- 使用 HMAC-SHA 算法签发 JWT，Payload 中包含 `sub`（用户名）和 `role`（角色）
- 默认有效期 24 小时，勾选"记住我"延长至 7 天
- [JwtAuthenticationFilter.java](backend/src/main/java/com/productsell/security/JwtAuthenticationFilter.java) 继承 `OncePerRequestFilter`，从请求头 `Authorization: Bearer <token>` 中提取、验证、解析 Token，加载用户信息并设置 SecurityContext

#### 3.1.3 暴力破解防护

**核心文件：** [LoginAttemptService.java](backend/src/main/java/com/productsell/security/LoginAttemptService.java)

- 基于 IP 地址的登录尝试计数，使用 `ConcurrentHashMap` 存储
- 同一 IP 在 15 分钟内超过 5 次失败尝试后拒绝登录
- 成功登录后自动清除计数

#### 3.1.4 前后端路由守卫

- **后端：** `SecurityConfig` 配置允许 `/api/auth/login`、Swagger 文档、`/uploads/**` 公开访问，其余接口需要认证。`UserController` 上使用 `@PreAuthorize("hasRole('ADMIN')")` 限制仅管理员可访问
- **前端：** [router/index.js](frontend/src/router/index.js) 的 `beforeEach` 守卫实现三重逻辑：未登录跳转登录页、已登录访问登录页重定向首页、角色不匹配重定向首页

---

### 3.2 用户管理模块（仅管理员）

**路由：** `/users`  
**权限：** 仅 ADMIN 角色  
**核心文件：** [UserController.java](backend/src/main/java/com/productsell/controller/UserController.java) + [UserManageView.vue](frontend/src/views/user/UserManageView.vue)

**实现要点：**
- 完整的 CRUD 操作，支持分页查询和关键字搜索
- 管理员账号保护机制：[DataInitializer.java](backend/src/main/java/com/productsell/config/DataInitializer.java) 每次启动确保 admin 账户存在；控制器层面阻止禁用或删除 admin 用户（返回 `ResultCode.ADMIN_CANNOT_DISABLE` / `ADMIN_CANNOT_DELETE`）
- 新建/编辑用户时，密码同样走 SHA-256 → BCrypt 双层哈希

---

### 3.3 商品管理模块

**路由：** `/products`  
**核心文件：** [ProductController.java](backend/src/main/java/com/productsell/controller/ProductController.java) + [ProductManageView.vue](frontend/src/views/product/ProductManageView.vue)

**实现要点：**
1. **商品 CRUD：** 支持商品名称、分类、品牌、售价、成本价、库存数量、商品描述等字段
2. **商品编码自动生成：** 格式为 `CP + yyyyMMdd + 3位流水号`（如 CP20250625001），使用 [CodeGenerator.java](backend/src/main/java/com/productsell/utils/CodeGenerator.java) 线程安全的 `synchronized` 方法每日自动递增
3. **图片上传：** [FileUploadUtils.java](backend/src/main/java/com/productsell/utils/FileUploadUtils.java) 实现 MIME 类型校验（JPG/PNG/GIF/WebP）、文件大小限制（5MB）、UUID 重命名、存储到 `uploads/products/` 目录，通过 [WebMvcConfig.java](backend/src/main/java/com/productsell/config/WebMvcConfig.java) 映射为 HTTP 静态资源
4. **前端组件：** [ImageUpload.vue](frontend/src/components/common/ImageUpload.vue) 基于 Element Plus 的 `el-upload` 实现拖拽上传+预览+删除
5. **级联删除：** 删除商品时自动清理对应的图片文件

---

### 3.4 客户管理模块

**路由：** `/customers`  
**核心文件：** [CustomerController.java](backend/src/main/java/com/productsell/controller/CustomerController.java) + [CustomerManageView.vue](frontend/src/views/customer/CustomerManageView.vue)

**实现要点：**
- 客户编码自动生成：格式为 `KH + yyyyMMdd + 3位流水号`
- 支持关键字搜索（按姓名、编码、联系人、电话联合模糊匹配）
- 提供 `/api/customers/all` 接口返回所有客户的下拉列表数据，供订单创建时选择

---

### 3.5 订单管理模块（核心模块）

**路由：** `/orders`  
**核心文件：** [OrderController.java](backend/src/main/java/com/productsell/controller/OrderController.java) + [OrderServiceImpl.java](backend/src/main/java/com/productsell/service/impl/OrderServiceImpl.java) + [OrderManageView.vue](frontend/src/views/order/OrderManageView.vue)

#### 3.5.1 订单创建 — 最关键的业务逻辑

`OrderServiceImpl.createOrder()` 方法（`@Transactional` 事务保护）：

```
1. 验证客户是否存在
2. 遍历订单明细，对每行：
   a. 验证商品是否存在、状态是否启用
   b. 检查库存是否充足 → 不足则抛出 STOCK_INSUFFICIENT 异常
   c. 快照商品名称和价格到 order_item（保证历史数据完整性）
   d. 计算小计 subtotal = price × quantity
3. 汇总订单总金额 totalAmount
4. 扣减每个商品的库存 quantity = quantity - orderQuantity
5. 保存订单和订单明细
6. 为每条明细记录回填 order_no（冗余字段，方便查询）
```

#### 3.5.2 订单修改

- 仅 PENDING 状态可编辑
- 先恢复旧库存 → 删除旧明细 → 重新处理（遍历新明细、校验库存、快照、扣减）→ 更新订单
- 全程 `@Transactional`，任何一步失败自动回滚

#### 3.5.3 订单删除

- 先恢复所有商品库存 → 删除订单明细 → 删除订单
- 全程 `@Transactional`

#### 3.5.4 订单状态流转

```
PENDING（待发货）→ SHIPPED（已发货）→ COMPLETED（已完成）
                  → CANCELLED（已取消）
```

状态变更通过 `/api/orders/{id}/status` 接口（PUT 请求传递新状态）。

#### 3.5.5 前端订单页面设计

- **多条件筛选：** 订单号、客户下拉选择、订单状态、日期范围
- **动态订单表单：** 客户选择 → 日期选择 → 商品明细表格（动态增删行，选择商品自动带出价格，实时计算小计和总金额）
- **详情弹窗：** 只读展示订单头信息 + 明细表格

---

### 3.6 销售统计分析模块

**路由：** `/statistics`  
**核心文件：** [StatisticsController.java](backend/src/main/java/com/productsell/controller/StatisticsController.java) + [StatisticsMapper.xml](backend/src/main/resources/mapper/StatisticsMapper.xml) + [StatisticsView.vue](frontend/src/views/statistics/StatisticsView.vue)

**实现要点：**
- 所有统计接口均支持可选的时间范围参数（startDate/endDate），不传则统计全部数据
- 9 条手写 SQL 查询（位于 StatisticsMapper.xml），分 5 个 API 接口：

| 接口 | SQL 查询 | 前端图表 |
|------|----------|----------|
| `/api/statistics/overview` | 总商品数、总客户数、总订单数、总营收 | 4 张统计卡片 |
| `/api/statistics/sales-trend` | 按月分组统计订单数和销售额 | 2 条折线图（订单量 + 销售额） |
| `/api/statistics/top-products` | 按销量排行取前 N 名 | 横向柱状图 |
| `/api/statistics/category-sales` | 按商品分类汇总销售额 | 环形饼图 |
| `/api/statistics/order-status` | 按订单状态统计数量 | 玫瑰饼图 |

- 前端使用 **vue-echarts 6 + ECharts 5** 实现数据可视化，所有图表随日期筛选框联动刷新
- SQL 中使用 `COALESCE(SUM(...), 0)` 处理空结果，使用 `DATE_FORMAT` 格式化时间

---

### 3.7 AI 智能助手模块

**路由：** 全局浮动按钮触发  
**核心文件：** [AIController.java](backend/src/main/java/com/productsell/controller/AIController.java) + [AIServiceImpl.java](backend/src/main/java/com/productsell/service/impl/AIServiceImpl.java) + [AIChatPanel.vue](frontend/src/components/ai/AIChatPanel.vue) + [AIFloatingButton.vue](frontend/src/components/ai/AIFloatingButton.vue)

#### 3.7.1 后端 AI 实现

- 使用 **Spring AI 1.0.0-M6** 的 OpenAI 兼容模式，对接 **DeepSeek Chat API**
- `AIServiceImpl.chat()` 构建了精心的 System Prompt：
  - **角色限定：** "你是 ProductSell 系统的智能助手"
  - **业务边界：** 只回答库存查询、销售分析、订单管理、客户管理相关问题
  - **实时数据注入：** 每次对话前，从数据库查询并注入到 System Prompt 中：
    - 系统概览数据（商品/客户/订单/营收总数）
    - 低库存预警（库存 < 10 的商品列表）
    - 最近 5 笔订单
    - 热销 Top 5 商品
    - 商品状态统计（上架/下架数量）
    - 最近添加的客户
  - **安全约束：** 拒绝回答超出商城业务范围的问题，不提供代码/技术建议
- 参数控制：`temperature=0.3`（降低幻觉），`max-tokens=1500`（控制回复长度）
- 对话历史管理：前后端各保留最近 6 轮（12 条消息）上下文

#### 3.7.2 前端 AI 实现

- **触发方式：** 可拖拽的浮动按钮（[AIFloatingButton.vue](frontend/src/components/ai/AIFloatingButton.vue)），短拖=点击打开，长拖=移动位置，任何页面均可调起
- **聊天面板：** 借助 Element Plus 的 `el-drawer` 从右侧滑出，不遮挡主界面
- **消息渲染：** AI 回复使用 `marked` 库渲染 Markdown（支持表格、代码块、标题）
- **输入优化：** Enter 发送，Shift+Enter 换行

---

### 3.8 登录页面动画设计

**核心文件：** [LoginView.vue](frontend/src/views/LoginView.vue) + [AnimatedEye.vue](frontend/src/components/login/AnimatedEye.vue) + [AnimatedPupil.vue](frontend/src/components/login/AnimatedPupil.vue)

- 4 个卡通角色（各具特色：紫色 Q 版角色、黑色神秘角色、橙色热情角色、黄色明亮角色）
- **眼球追踪：** 角色眼球跟随鼠标位置移动（CSS transform + JavaScript mousemove 事件计算相对角度）
- **眨眼动画：** CSS `@keyframes` 实现周期性眨眼
- **密码偷看动画：** 点击密码可见按钮时，角色做出"偷看"动作（眼球移动到特定方向）
- **角色倾斜：** 根据角色相对于舞台中心的位置，动态计算倾斜角度，制造立体空间感
- **响应式：** 屏幕宽度 < 992px 时隐藏角色，仅保留表单

---

## 四、技术亮点总结

| 亮点 | 说明 |
|------|------|
| **双层密码哈希** | 前端 SHA-256 + 后端 BCrypt，密码明文从不出浏览器 |
| **JWT 无状态认证** | 支持"记住我"延长有效期，前后端双重路由守卫 |
| **订单快照机制** | 商品名称和价格在创建订单时写入 order_item，确保历史数据完整性 |
| **事务性库存管理** | 下单扣库存、改单恢复+重算、删单恢复，全程 @Transactional |
| **线程安全编码生成** | synchronized 方法 + 数据库查询最大编码 + 日期前缀，每日自动递增 |
| **IP 防暴力破解** | ConcurrentHashMap 记录失败次数，15 分钟窗口 |
| **AI 实时数据注入** | 每次对话前从数据库查询最新业务数据注入 System Prompt |
| **数据可视化** | ECharts 5 种图表类型，支持时间范围联动筛选 |
| **手写 SQL 统计** | 9 条手写 SQL 满足复杂统计需求，COALESCE 处理空值 |
| **可拖拽浮动按钮** | 全局 AI 入口，短拖点击、长拖移动，不干扰主流程 |

---

## 五、安全措施

1. **认证安全：** JWT + BCrypt + 双层哈希，无明文密码
2. **授权安全：** 后端 `@PreAuthorize` 方法级鉴权 + 前端路由 `meta.roles` 控制
3. **防暴力破解：** IP 级登录失败计数 + 15 分钟锁定
4. **输入校验：** DTO 层使用 `@NotBlank`、`@Min`、`@Valid` 等 Jakarta Validation 注解
5. **文件上传安全：** MIME 类型白名单（仅图片）、文件大小限制（5MB）、UUID 重命名
6. **全局异常处理：** `@RestControllerAdvice` 统一捕获并返回结构化错误信息
7. **CORS 配置：** 明确配置允许的来源和方法，不随意开启
8. **admin 账户保护：** 禁止禁用/删除 admin 用户，每次启动重置其密码
