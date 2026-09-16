-- 捞宝购物商城初始化脚本
-- 与 springboot/src/main/resources/application.yml 中的数据库名保持一致。

CREATE DATABASE IF NOT EXISTS `bil_mall`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `bil_mall`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `phone` varchar(255) DEFAULT NULL COMMENT '电话',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `phone` varchar(255) DEFAULT NULL COMMENT '电话',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `sex` varchar(255) DEFAULT NULL COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `infos` varchar(255) DEFAULT NULL COMMENT '个人介绍',
  `role` varchar(255) NOT NULL DEFAULT 'USER' COMMENT '角色',
  `account` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '账户余额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '分类名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS `goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '商品名称',
  `descr` varchar(500) DEFAULT NULL COMMENT '商品简介',
  `content` longtext COMMENT '商品详情',
  `cover` varchar(500) DEFAULT NULL COMMENT '商品封面',
  `price` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '价格',
  `store` int NOT NULL DEFAULT 0 COMMENT '库存',
  `admin_id` int DEFAULT NULL COMMENT '管理员ID',
  `date` varchar(50) DEFAULT NULL COMMENT '上架日期',
  `type_id` int DEFAULT NULL COMMENT '分类ID',
  `state` varchar(50) DEFAULT NULL COMMENT '商品状态',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  PRIMARY KEY (`id`),
  KEY `idx_goods_type_id` (`type_id`),
  KEY `idx_goods_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `carousel` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) DEFAULT NULL COMMENT '轮播图名称',
  `cover` varchar(500) DEFAULT NULL COMMENT '轮播图地址',
  `goods_id` int DEFAULT NULL COMMENT '关联商品ID',
  PRIMARY KEY (`id`),
  KEY `idx_carousel_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

CREATE TABLE IF NOT EXISTS `collect` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `time` varchar(50) DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collect_user_goods` (`user_id`, `goods_id`),
  KEY `idx_collect_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

CREATE TABLE IF NOT EXISTS `orders` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) DEFAULT NULL COMMENT '订单名称',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `price` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '订单金额',
  `nums` int NOT NULL DEFAULT 1 COMMENT '商品数量',
  `user_phone` varchar(255) DEFAULT NULL COMMENT '收货电话',
  `user_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `time` varchar(50) DEFAULT NULL COMMENT '下单时间',
  `state` varchar(50) NOT NULL DEFAULT '待付款' COMMENT '订单状态',
  `user_id` int NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_order_no` (`order_no`),
  KEY `idx_orders_goods_id` (`goods_id`),
  KEY `idx_orders_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `order_item` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `price` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '商品单价',
  `nums` int NOT NULL DEFAULT 1 COMMENT '商品数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order_id` (`order_id`),
  KEY `idx_order_item_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 购物车数据保存在前端本地（localStorage），不再建 cart 表

INSERT IGNORE INTO `admin` (`id`, `username`, `password`, `name`, `phone`, `email`) VALUES
  (1, 'admin', '123', '管理员', '13677889988', 'admin@example.com');

-- 管理员账号存 admin 表（前台选"管理员"角色登录走 /admin/login）；
-- user 表只存普通用户，前台"用户"登录只匹配 role='USER'，不放 ADMIN 角色的冗余账号。
INSERT IGNORE INTO `user`
  (`id`, `username`, `password`, `name`, `phone`, `email`, `address`, `avatar`, `sex`, `age`, `infos`, `role`, `account`)
VALUES
  (2, 'tom', '123', '汤姆', '13988776699', 'jerry@example.com', '北京', NULL, '男', 24, '爱逛捞宝，天天有新品', 'USER', 5000.00),
  (3, 'jerry', '123', '杰瑞', '15098765321', 'tom@example.com', '上海', NULL, '男', 25, '网购达人一枚', 'USER', 5000.00);

INSERT IGNORE INTO `type` (`id`, `name`) VALUES
  (1, '数码家电'),
  (2, '服饰鞋包'),
  (3, '美妆个护'),
  (4, '食品生鲜');

INSERT IGNORE INTO `goods`
  (`id`, `name`, `descr`, `content`, `cover`, `price`, `store`, `admin_id`, `date`, `type_id`, `state`, `sales`)
VALUES
  (1, '无线蓝牙耳机', '主动降噪，长时间佩戴舒适', '<p>蓝牙5.3稳定连接，支持主动降噪，续航可达30小时。</p>', '/images/bg2.jpg', 129.00, 100, 1, '2026-09-01', 1, '上架', 86),
  (2, '智能运动手表', '心率监测，多种运动模式', '<p>全天候心率与睡眠监测，50米防水，支持多种运动模式。</p>', '/images/bg1.jpeg', 159.00, 80, 1, '2026-09-02', 1, '上架', 64),
  (3, '男士休闲牛仔裤', '弹力面料，修身百搭', '<p>高弹力面料透气舒适，经典水洗工艺，日常通勤易搭配。</p>', '/images/bg2.jpg', 39.90, 200, 1, '2026-09-03', 2, '上架', 128),
  (4, '女士针织毛衣', '柔软亲肤，保暖不臃肿', '<p>细针织法柔软亲肤，版型宽松显瘦，适合秋冬叠穿。</p>', '/images/bg2.jpg', 29.90, 150, 1, '2026-09-04', 2, '上架', 95),
  (5, '保湿补水面膜', '深层补水，舒缓干燥', '<p>蕴含多重保湿成分，敷后水润不紧绷，适合各种肤质。</p>', '/images/bg1.jpeg', 19.90, 120, 1, '2026-09-05', 3, '上架', 72),
  (6, '氨基酸洁面乳', '温和清洁，不紧绷', '<p>氨基酸配方温和低刺激，清洁同时维持水油平衡。</p>', '/images/bg2.jpg', 89.00, 60, 1, '2026-09-06', 3, '上架', 43),
  (7, '精选阿拉比卡咖啡豆', '中度烘焙，香气浓郁', '<p>单一产地阿拉比卡豆，中度烘焙，酸甜平衡回味悠长。</p>', '/images/bg1.jpeg', 49.00, 90, 1, '2026-09-07', 4, '上架', 57),
  (8, '每日坚果礼盒', '科学配比，新鲜锁存', '<p>多种坚果与果干科学配比，独立小包装，新鲜便携。</p>', '/images/bg1.jpeg', 35.00, 110, 1, '2026-09-08', 4, '上架', 61);

INSERT IGNORE INTO `carousel` (`id`, `name`, `cover`, `goods_id`) VALUES
  (1, '数码上新', '/images/bg2.jpg', 1),
  (2, '服饰精选', '/images/bg1.jpeg', 2),
  (3, '美妆好物', '/images/bg1.jpeg', 5);

INSERT IGNORE INTO `collect` (`id`, `user_id`, `goods_id`, `time`) VALUES
  (1, 2, 1, '2026-09-10 10:20:00'),
  (2, 2, 5, '2026-09-11 14:30:00');

INSERT IGNORE INTO `orders`
  (`id`, `name`, `order_no`, `goods_id`, `price`, `nums`, `user_phone`, `user_address`, `time`, `state`, `user_id`)
VALUES
  (1, '无线蓝牙耳机', '202609101020001', 1, 258.00, 2, '13988776699', '北京市朝阳区示例路 1 号', '2026-09-10 10:20:00', '已支付', 2),
  (2, '保湿补水面膜', '202609111430001', 5, 39.80, 2, '13988776699', '北京市朝阳区示例路 1 号', '2026-09-11 14:30:00', '待付款', 2);

INSERT IGNORE INTO `order_item`
  (`id`, `order_id`, `goods_id`, `goods_name`, `price`, `nums`)
VALUES
  (1, 1, 1, '无线蓝牙耳机', 129.00, 2),
  (2, 2, 5, '保湿补水面膜', 19.90, 2);

SET FOREIGN_KEY_CHECKS = 1;
