-- 商户端迁移脚本（存量库执行）
-- 适用：已按 code_scaffold.sql 初始化过的 bil_mall 库
-- 执行时间：2026-09-16
-- 内容：merchant 表 + goods/orders/order_item 加商户维度 + 存量数据回填 + 订单状态扩展

USE `bil_mall`;
SET NAMES utf8mb4;

-- ============ 1. 商户表 ============
CREATE TABLE IF NOT EXISTS `merchant` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL COMMENT '登录名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `shop_name` varchar(255) NOT NULL COMMENT '店铺名称',
  `logo` varchar(500) DEFAULT NULL COMMENT '店铺LOGO',
  `descr` varchar(500) DEFAULT NULL COMMENT '店铺简介',
  `phone` varchar(255) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '经营地址',
  `license` varchar(500) DEFAULT NULL COMMENT '营业执照图片',
  `state` varchar(50) NOT NULL DEFAULT '待审核' COMMENT '入驻状态：待审核/已通过/已驳回/已停用',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `account` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '可结算余额（台账，不接真实打款）',
  `create_time` varchar(50) DEFAULT NULL COMMENT '入驻申请时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户表';

-- ============ 2. 商品加归属商户 ============
-- admin_id 保留为「录入人/代运营管理员」，merchant_id 才是归属商户
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'bil_mall' AND TABLE_NAME = 'goods' AND COLUMN_NAME = 'merchant_id');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE `goods` ADD COLUMN `merchant_id` int DEFAULT NULL COMMENT ''归属商户ID''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = 'bil_mall' AND TABLE_NAME = 'goods' AND INDEX_NAME = 'idx_goods_merchant_id');
SET @sql := IF(@has_idx = 0,
  'ALTER TABLE `goods` ADD KEY `idx_goods_merchant_id` (`merchant_id`)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============ 3. 订单加商户 + 结算批次号 ============
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'bil_mall' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'merchant_id');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE `orders` ADD COLUMN `merchant_id` int DEFAULT NULL COMMENT ''归属商户ID''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'bil_mall' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'parent_no');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE `orders` ADD COLUMN `parent_no` varchar(50) DEFAULT NULL COMMENT ''结算批次号（跨商户拆单时同批次共用）''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = 'bil_mall' AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_merchant_id');
SET @sql := IF(@has_idx = 0,
  'ALTER TABLE `orders` ADD KEY `idx_orders_merchant_id` (`merchant_id`)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = 'bil_mall' AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_parent_no');
SET @sql := IF(@has_idx = 0,
  'ALTER TABLE `orders` ADD KEY `idx_orders_parent_no` (`parent_no`)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============ 4. 订单明细加商户（冗余，商户侧直查明细不用 join） ============
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'bil_mall' AND TABLE_NAME = 'order_item' AND COLUMN_NAME = 'merchant_id');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE `order_item` ADD COLUMN `merchant_id` int DEFAULT NULL COMMENT ''归属商户ID''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = 'bil_mall' AND TABLE_NAME = 'order_item' AND INDEX_NAME = 'idx_order_item_merchant_id');
SET @sql := IF(@has_idx = 0,
  'ALTER TABLE `order_item` ADD KEY `idx_order_item_merchant_id` (`merchant_id`)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============ 5. 存量数据回填 ============
-- 5.1 种子商户：承接原本归属 admin_id=1 的商品（原 admin 实际扮演「唯一大商户」）
INSERT IGNORE INTO `merchant` (`id`, `username`, `password`, `shop_name`, `descr`, `phone`, `state`, `create_time`)
VALUES (1, 'shop_demo', '123', '捞宝自营旗舰店', '平台自营，正品保障', '13600001111', '已通过', '2026-09-16 00:00:00');

-- 5.2 商品回填：已有归属管理员的商品划给自营商户
UPDATE `goods` SET `merchant_id` = 1 WHERE `merchant_id` IS NULL;

-- 5.3 订单/明细回填：按订单主商品（goods_id）的归属商户补
UPDATE `orders` o JOIN `goods` g ON g.id = o.goods_id
  SET o.merchant_id = g.merchant_id WHERE o.merchant_id IS NULL;
UPDATE `order_item` oi JOIN `goods` g ON g.id = oi.goods_id
  SET oi.merchant_id = g.merchant_id WHERE oi.merchant_id IS NULL;

-- 5.4 历史订单补批次号（无批次的老单用自身订单号充当，前端聚合逻辑兼容）
UPDATE `orders` SET `parent_no` = `order_no` WHERE `parent_no` IS NULL;

-- ============ 6. 种子商户账号 ============
-- 登录页选「商户」：shop_demo / 123
-- 新入驻走「注册 → 商户入驻」，state=待审核，管理员在后台审核
