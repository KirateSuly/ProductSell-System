-- 商品销售管理系统 - 数据库初始化脚本
-- MySQL 8.0

CREATE DATABASE IF NOT EXISTS product_sell
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE product_sell;

-- ============================================================
-- 系统用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录用户名',
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    real_name   VARCHAR(50)  COMMENT '真实姓名',
    phone       VARCHAR(20)  COMMENT '手机号',
    email       VARCHAR(100) COMMENT '邮箱',
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 默认管理员账号 (admin / admin123) 由应用的 DataInitializer 在首次启动时自动创建

-- ============================================================
-- 商品表
-- ============================================================
CREATE TABLE IF NOT EXISTS product (
    id             BIGINT         PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    product_code   VARCHAR(50)    NOT NULL UNIQUE COMMENT '商品编号(CP+日期+序号)',
    product_name   VARCHAR(200)   NOT NULL COMMENT '商品名称',
    category       VARCHAR(50)    COMMENT '分类',
    brand          VARCHAR(100)   COMMENT '品牌',
    specification  VARCHAR(200)   COMMENT '规格型号',
    unit           VARCHAR(20)    NOT NULL DEFAULT '个' COMMENT '单位',
    price          DECIMAL(10,2)  NOT NULL COMMENT '售价',
    cost_price     DECIMAL(10,2)  COMMENT '成本价',
    stock_quantity INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
    description    TEXT           COMMENT '商品描述',
    image_url      VARCHAR(500)   COMMENT '商品图片路径',
    status         TINYINT        NOT NULL DEFAULT 1 COMMENT '状态: 1-在售 0-停售',
    create_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_product_name (product_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================================
-- 客户表
-- ============================================================
CREATE TABLE IF NOT EXISTS customer (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_code   VARCHAR(50)  NOT NULL UNIQUE COMMENT '客户编号(KH+日期+序号)',
    customer_name   VARCHAR(200) NOT NULL COMMENT '客户名称',
    contact_person  VARCHAR(50)  COMMENT '联系人',
    phone           VARCHAR(20)  COMMENT '联系电话',
    email           VARCHAR(100) COMMENT '邮箱',
    address         VARCHAR(500) COMMENT '地址',
    remark          TEXT         COMMENT '备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_name (customer_name),
    INDEX idx_customer_code (customer_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ============================================================
-- 订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS `order` (
    id               BIGINT         PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_no         VARCHAR(50)    NOT NULL UNIQUE COMMENT '订单号(DD+日期+序号)',
    customer_id      BIGINT         NOT NULL COMMENT '客户ID',
    customer_name    VARCHAR(200)   NOT NULL COMMENT '客户名称(下单时快照)',
    total_amount     DECIMAL(12,2)  NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    order_status     VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/SHIPPED/COMPLETED/CANCELLED',
    order_date       DATETIME       NOT NULL COMMENT '下单日期',
    delivery_address VARCHAR(500)   COMMENT '收货地址',
    remark           TEXT           COMMENT '备注',
    create_time      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (order_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_status (order_status),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 订单明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS order_item (
    id            BIGINT         PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_id      BIGINT         NOT NULL COMMENT '订单ID',
    order_no      VARCHAR(50)    NOT NULL COMMENT '订单号(冗余)',
    product_id    BIGINT         NOT NULL COMMENT '商品ID',
    product_name  VARCHAR(200)   NOT NULL COMMENT '商品名称(下单时快照)',
    product_price DECIMAL(10,2)  NOT NULL COMMENT '商品单价(下单时快照)',
    quantity      INT            NOT NULL COMMENT '购买数量',
    subtotal      DECIMAL(12,2)  NOT NULL COMMENT '小计',
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
