-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: bil_mall
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `bil_mall`
--

/*!40000 DROP DATABASE IF EXISTS `bil_mall`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `bil_mall` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `bil_mall`;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名',
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin','123','管理员','13677889988','admin@example.com'),(3,'admin1','123','jerry','13800138000','admin@hachimi.com');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carousel`
--

DROP TABLE IF EXISTS `carousel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carousel` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '轮播图名称',
  `cover` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '轮播图地址',
  `goods_id` int DEFAULT NULL COMMENT '关联商品ID',
  PRIMARY KEY (`id`),
  KEY `idx_carousel_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carousel`
--

LOCK TABLES `carousel` WRITE;
/*!40000 ALTER TABLE `carousel` DISABLE KEYS */;
INSERT INTO `carousel` VALUES (1,'猫粮上新','/images/banner1.jpg',1),(2,'犬粮精选','/images/banner2.jpg',2),(3,'互动玩具','/images/banner3.jpg',5);
/*!40000 ALTER TABLE `carousel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `nums` int NOT NULL DEFAULT '1' COMMENT '数量',
  `time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_user_goods` (`user_id`,`goods_id`),
  KEY `idx_cart_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collect`
--

DROP TABLE IF EXISTS `collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collect` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collect_user_goods` (`user_id`,`goods_id`),
  KEY `idx_collect_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collect`
--

LOCK TABLES `collect` WRITE;
/*!40000 ALTER TABLE `collect` DISABLE KEYS */;
INSERT INTO `collect` VALUES (1,2,1,'2026-09-10 10:20:00'),(2,2,5,'2026-09-11 14:30:00'),(4,4,1,'2026-09-14 15:01:22'),(5,4,2,'2026-09-15 09:30:16');
/*!40000 ALTER TABLE `collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goods`
--

DROP TABLE IF EXISTS `goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `descr` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品简介',
  `content` longtext COLLATE utf8mb4_unicode_ci COMMENT '商品详情',
  `cover` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品封面',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格',
  `store` int NOT NULL DEFAULT '0' COMMENT '库存',
  `admin_id` int DEFAULT NULL COMMENT '管理员ID',
  `date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上架日期',
  `type_id` int DEFAULT NULL COMMENT '分类ID',
  `state` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品状态',
  `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
  `merchant_id` int DEFAULT NULL COMMENT '归属商户ID',
  PRIMARY KEY (`id`),
  KEY `idx_goods_type_id` (`type_id`),
  KEY `idx_goods_admin_id` (`admin_id`),
  KEY `idx_goods_merchant_id` (`merchant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods`
--

LOCK TABLES `goods` WRITE;
/*!40000 ALTER TABLE `goods` DISABLE KEYS */;
INSERT INTO `goods` VALUES (1,'全价冻干双拼猫粮','高蛋白配方，满足成猫日常营养需求','<p>精选鸡肉与鱼肉，添加冻干颗粒，适口性好。</p>','/images/goods/g1.jpg',129.00,95,1,'2026-09-01',1,'上架',91,1),(2,'营养均衡犬粮','适合中小型成年犬的日常主粮','<p>科学配比蛋白质、脂肪和膳食纤维，帮助维持健康体态。</p>','/images/goods/g2.jpg',159.00,78,1,'2026-09-02',1,'上架',66,1),(3,'鸡肉冻干零食','无谷低敏，训练奖励好帮手','<p>整块鸡胸肉低温冻干，保留营养与香味。</p>','/images/goods/g3.jpg',39.90,200,1,'2026-09-03',2,'上架',128,1),(4,'猫咪磨牙小饼干','酥脆口感，帮助清洁牙齿','<p>添加薄荷成分，适合日常奖励与磨牙。</p>','/images/goods/g4.jpg',29.90,148,1,'2026-09-04',2,'上架',97,1),(5,'耐咬磨牙绳结玩具','互动拉扯，消耗宠物精力','<p>棉绳结构耐咬易清洁，适合室内互动游戏。</p>','/images/goods/g5.jpg',19.90,118,1,'2026-09-05',3,'上架',74,1),(6,'智能逗猫棒','自动变速，陪伴猫咪玩耍','<p>多档速度与替换逗猫头，适合忙碌时给猫咪增加运动量。</p>','/images/goods/g6.jpg',89.00,60,1,'2026-09-06',3,'上架',43,1),(7,'宠物温和沐浴露','清洁除味，呵护敏感皮肤','<p>温和配方易冲洗，适合犬猫日常清洁。</p>','/images/goods/g7.jpg',49.00,90,1,'2026-09-07',4,'上架',57,1),(8,'宠物护理梳','轻松梳理浮毛，减少打结','<p>圆头针梳设计，梳理舒适且便于清理。</p>','/images/goods/g8.jpg',35.00,108,1,'2026-09-08',4,'上架',63,1),(10,'深海三文鱼成猫粮','进口三文鱼原料，富含Omega-3，美毛护肤','<p>精选深海三文鱼，富含Omega-3脂肪酸，美毛护肤。无谷配方，减轻肠胃负担，适合1岁以上成猫。</p><p>净含量：1.5kg</p>','/images/goods/g10.jpg',189.00,120,1,'2026-09-09',1,'上架',35,1),(11,'幼犬奶糕粮','高钙配方助力幼犬发育，易消化好吸收','<p>针对2-12月龄幼犬设计，高钙高蛋白，添加DHA助力大脑发育。颗粒小而易咀嚼，好消化易吸收。</p><p>净含量：1.5kg</p>','/images/goods/g11.jpg',139.00,100,1,'2026-09-10',1,'上架',42,1),(12,'老年犬低脂调理粮','低脂易消化，呵护老年犬肠胃与关节','<p>为7岁以上老年犬定制，低脂配方控制体重，添加葡萄糖胺呵护关节，维生素E延缓衰老。</p><p>净含量：1.5kg</p>','/images/goods/g12.jpg',169.00,60,1,'2026-09-11',1,'上架',18,1),(13,'无谷鲜肉全期猫粮','85%鲜肉含量，全猫龄通用','<p>85%动物原料，鲜鸡肉+鲜鸭肉配方，无谷物豆类。全猫龄通用，孕期哺乳期母猫同样适用。</p><p>净含量：2kg</p>','/images/goods/g13.jpg',219.00,80,1,'2026-09-12',1,'上架',27,1),(14,'冻干鹌鹑零食','-40°C冻干锁鲜，高蛋白低脂肪','<p>整只鹌鹑-40°C真空冻干，锁住营养与鲜味。高蛋白低脂肪，无添加，猫咪狗狗都爱吃。</p><p>规格：50g/袋</p>','/images/goods/g14.jpg',45.00,200,1,'2026-09-13',2,'上架',66,1),(15,'磨牙洁齿棒','耐磨耐咬，帮助清洁牙齿减少牙垢','<p>添加洁齿因子，咀嚼过程中帮助清洁牙齿、减少牙垢。耐磨耐咬，适合中小型犬。</p><p>规格：200g/袋</p>','/images/goods/g15.jpg',25.90,180,1,'2026-09-14',2,'上架',88,1),(16,'三文鱼寿司罐头','大块鱼肉可视，汤汁浓郁补水','<p>大块三文鱼清晰可见，汤汁浓郁。补水补蛋白，可直接喂食或拌粮。</p><p>规格：85g/罐</p>','/images/goods/g16.jpg',15.90,300,1,'2026-09-15',2,'上架',120,1),(17,'金枪鱼猫条','一撕即喂，人猫互动小零食','<p>金枪鱼+鸡肉双拼口味，细腻肉泥一撕即喂。喂食互动两相宜，独立小包装方便携带。</p><p>规格：15g×10支</p>','/images/goods/g17.jpg',32.90,250,1,'2026-09-16',2,'上架',95,1),(18,'电动逗猫机器人','智能避障，自动玩伴告别孤单','<p>智能感应避障，自动旋转羽毛吸引猫咪追逐。USB充电续航，上班族猫咪的自动玩伴。</p>','/images/goods/g18.jpg',129.00,50,1,'2026-09-17',3,'上架',22,1),(19,'激光笔逗猫器','多图案切换，远距离互动','<p>5合1图案切换，射程远亮度高。长按续航短按点射，与猫咪远距离互动神器。</p>','/images/goods/g19.jpg',39.90,90,1,'2026-09-18',3,'上架',51,1),(20,'宠物训练飞盘','软胶材质不伤牙，训练玩耍两相宜','<p>软橡胶材质，韧性好不伤牙齿。浮水设计可在水中玩耍，户外训练互动好帮手。</p>','/images/goods/g20.jpg',18.90,150,1,'2026-09-19',3,'上架',60,1),(21,'瓦楞纸猫抓板','高密度瓦楞纸，耐磨耐抓不掉屑','<p>高密度瓦楞纸压制，紧密不掉屑。沙发救星，满足猫咪磨爪天性。</p>','/images/goods/g21.jpg',29.90,130,1,'2026-09-20',3,'上架',73,1),(22,'宠物益生菌调理粉','呵护肠胃，改善软便拉稀','<p>多重活性益生菌+益生元，调理肠道菌群。换粮、应激、软便时的肠胃守护者。</p><p>规格：100g/盒</p>','/images/goods/g22.jpg',69.00,110,1,'2026-09-21',4,'上架',30,1),(23,'宠物指甲剪套装','安全刀口有定位孔，附锉刀','<p>不锈钢锋利刀口，带安全定位孔防止剪伤血线。附赠指甲锉，剪磨一步到位。</p>','/images/goods/g23.jpg',26.00,95,1,'2026-09-22',4,'上架',40,1),(24,'宠物除臭喷雾','植物配方分解异味，人宠安心','<p>植物提取配方，分解而非掩盖异味。可直接喷洒于笼舍、猫砂盆周围，人宠安心。</p>','/images/goods/g24.jpg',42.00,120,1,'2026-09-23',4,'上架',55,1),(25,'吸水速干宠物浴巾','超细纤维，吸水快不掉毛','<p>超细纤维材质，吸水量可达自重5倍。速干不掉毛，洗澡后快速擦干少感冒。</p>','/images/goods/g25.jpg',22.00,140,1,'2026-09-24',4,'上架',48,1),(26,'记忆棉猫窝 四季通用','深睡窝感，可拆洗保暖猫窝','<p>记忆棉底部减压舒适，外套可拆洗，冬季保暖夏季透气。</p>','/images/goods/g26.jpg',89.00,60,NULL,'2026-09-17',3,'上架',0,2),(27,'宠物自动循环饮水机','活水循环，鼓励多喝水防结石','<p>三重过滤活水循环，静音水泵，2L大容量。</p>','/images/goods/g27.jpg',79.00,45,NULL,'2026-09-17',4,'上架',0,2),(28,'宠物雨衣 中小型犬','反光条设计，雨天遛狗不湿身','<p>轻量防水面料，胸口反光条夜间更安全，腹部魔术贴易穿脱。</p>','/images/goods/g28.jpg',45.00,80,NULL,'2026-09-17',3,'上架',0,2),(29,'猫薄荷逗猫抱枕','填充有机猫薄荷，猫咪抱咬解压','<p>加厚短毛绒面料，内置有机猫薄荷，缓解猫咪焦虑。</p>','/images/goods/g29.jpg',19.90,150,NULL,'2026-09-17',3,'上架',0,2),(30,'冻干鸡肉粒训练零食','单一肉源，训练奖励无负担','<p>-40°C冻干工艺锁住鲜味，高蛋白低脂肪，掰成小粒训练更方便。</p>','/images/goods/g30.jpg',32.00,100,NULL,'2026-09-17',2,'上架',0,2),(31,'宠物电动剃毛器','低噪不卡毛，新手也能推光','<p>R型陶瓷刀头不伤肤，35dB低噪，充插两用。</p>','/images/goods/g31.jpg',69.00,55,NULL,'2026-09-17',4,'上架',0,2),(32,'123','123','<p>123</p>','http://localhost:9999/file/download/1789611887172_touxiang.jpg',123.00,1233,NULL,'2026-09-17',1,'上架',0,2),(33,'深海鱼油美毛猫粮','进口鱼油，亮毛护肤','<p>深海鱼油配比，Omega-3 美毛护肤。</p>','/images/goods/g32.jpg',128.00,70,NULL,'2026-09-17',1,'上架',12,3),(34,'肠胃养护成猫粮','低敏配方，呵护玻璃胃','<p>单一蛋白低敏配方，添加益生元养护肠胃。</p>','/images/goods/g33.jpg',105.00,65,NULL,'2026-09-17',1,'上架',8,3),(35,'冻干多春鱼零食','整条冻干，补钙美毛','<p>整条多春鱼-40°C冻干，钙质丰富。</p>','/images/goods/g34.jpg',25.90,130,NULL,'2026-09-17',2,'上架',21,3),(36,'羊奶布丁猫零食','补水解馋，两只装','<p>宠物羊奶发酵，补水又解馋。</p>','/images/goods/g35.jpg',15.90,200,NULL,'2026-09-17',2,'上架',33,3),(37,'小型犬专用幼犬粮','高钙助长，好消化','<p>颗粒小易咀嚼，高钙配方助力幼犬发育。</p>','/images/goods/g36.jpg',98.00,60,NULL,'2026-09-17',1,'上架',15,4),(38,'牛肉味训练零食','撕小块训练更方便','<p>真肉烘焙，牛肉风味，训练奖励首选。</p>','/images/goods/g37.jpg',28.00,120,NULL,'2026-09-17',2,'上架',26,4),(39,'反光夜间遛狗绳','3M反光，夜跑更安全','<p>3M反光织带，缓冲减震，夜遛必备。</p>','/images/goods/g38.jpg',39.00,90,NULL,'2026-09-17',3,'上架',18,4),(40,'狗狗保暖窝垫','可机洗，秋冬必备','<p>加厚珊瑚绒，防滑底，整体可机洗。</p>','/images/goods/g39.jpg',69.00,40,NULL,'2026-09-17',4,'上架',9,4),(41,'豆腐猫砂 除臭款','遇水速溶，可冲厕所','<p>豌豆豆腐砂，除臭结团，直接冲马桶。</p>','/images/goods/g40.jpg',29.90,180,NULL,'2026-09-17',4,'上架',44,5),(42,'宠物湿巾 80抽','免洗配方，出门必备','<p>无酒精无香精，擦眼泪擦爪子。</p>','/images/goods/g41.jpg',12.90,300,NULL,'2026-09-17',4,'上架',51,5),(43,'猫咪化毛膏','排出毛球，减少呕吐','<p>添加麦芽糖浆与纤维素，温和排毛球。</p>','/images/goods/g42.jpg',42.00,85,NULL,'2026-09-17',4,'上架',29,5),(44,'宠物吸水速干毛巾','超细纤维，掉毛少','<p>加厚超细纤维，吸水快、易拧干。</p>','/images/goods/g43.jpg',22.00,140,NULL,'2026-09-17',4,'上架',16,5),(45,'电动滚轮逗猫球','智能滚動，自己陪自己玩','<p>智能避障滚轮，USB充电，猫咪自嗨神器。</p>','/images/goods/g44.jpg',49.00,75,NULL,'2026-09-17',3,'上架',23,6),(46,'猫抓板沙发保护款','拯救你家的沙发','<p>高密度瓦楞纸，附赠猫薄荷。</p>','/images/goods/g45.jpg',26.00,160,NULL,'2026-09-17',3,'上架',37,6),(47,'宠物响纸玩具组合','咔嚓响纸，猫界解压','<p>6只装响纸玩具，激发捕猎天性。</p>','/images/goods/g46.jpg',16.80,210,NULL,'2026-09-17',3,'上架',42,6),(48,'乳胶发声叫叫鸡','经典解压，狗子最爱','<p>天然乳胶，发声解压，中大型犬适用。</p>','/images/goods/g47.jpg',18.00,130,NULL,'2026-09-17',3,'上架',31,6);
/*!40000 ALTER TABLE `goods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant`
--

DROP TABLE IF EXISTS `merchant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `shop_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺名称',
  `logo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺LOGO',
  `descr` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺简介',
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '经营地址',
  `license` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业执照图片',
  `state` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '待审核' COMMENT '入驻状态：待审核/已通过/已驳回/已停用',
  `reject_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '驳回原因',
  `account` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '可结算余额（台账，不接真实打款）',
  `create_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '入驻申请时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant`
--

LOCK TABLES `merchant` WRITE;
/*!40000 ALTER TABLE `merchant` DISABLE KEYS */;
INSERT INTO `merchant` VALUES (1,'shop_demo','$2a$10$4pNOq/PaQayJpubr9E3OBOwTiwLIiVbO9J6khBYEBC5LFLwogbzOS','捞宝自营旗舰店',NULL,'平台自营，正品保障','13600001111',NULL,NULL,'已通过',NULL,0.00,'2026-09-16 00:00:00'),(2,'1234','$2a$10$BF45PlrWJaMA6KVA5BM1F.vfTGFff8SYnzwS/ymt6skrpt3vdOVz2','米基哈','http://localhost:9999/file/download/OIP-C.webp',NULL,'123',NULL,NULL,'已通过',NULL,0.00,'2026-09-17 10:18:42'),(3,'maomijia','123456','喵米家宠物专营',NULL,'专注猫咪主粮与零食，厂家直供','13811112222',NULL,NULL,'已通过',NULL,0.00,'2026-09-17 10:20:00'),(4,'wangwang','123456','汪汪严选',NULL,'犬粮/狗零食/出门装备一站购齐','13833334444',NULL,NULL,'已通过',NULL,0.00,'2026-09-17 10:20:00'),(5,'chongai','123456','宠爱有家生活馆',NULL,'宠物日用品与清洁护理好物','13855556666',NULL,NULL,'已通过',NULL,0.00,'2026-09-17 10:20:00'),(6,'lingdang','123456','铃铛宠物玩具屋',NULL,'让毛孩子玩得开心','13877778888',NULL,NULL,'已通过',NULL,0.00,'2026-09-17 10:20:00');
/*!40000 ALTER TABLE `merchant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `goods_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品单价',
  `nums` int NOT NULL DEFAULT '1' COMMENT '商品数量',
  `merchant_id` int DEFAULT NULL COMMENT '归属商户ID',
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order_id` (`order_id`),
  KEY `idx_order_item_goods_id` (`goods_id`),
  KEY `idx_order_item_merchant_id` (`merchant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,1,'全价冻干双拼猫粮',129.00,2,1),(2,2,5,'耐咬磨牙绳结玩具',19.90,2,1),(7,13,1,'全价冻干双拼猫粮',129.00,4,1),(8,14,2,'营养均衡犬粮',159.00,1,1),(9,14,4,'猫咪磨牙小饼干',29.90,1,1);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单名称',
  `order_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `nums` int NOT NULL DEFAULT '1' COMMENT '商品数量',
  `user_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货电话',
  `user_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货地址',
  `time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下单时间',
  `state` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '待付款' COMMENT '订单状态',
  `user_id` int NOT NULL COMMENT '用户ID',
  `merchant_id` int DEFAULT NULL COMMENT '归属商户ID',
  `parent_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结算批次号（跨商户拆单时同批次共用）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_order_no` (`order_no`),
  KEY `idx_orders_goods_id` (`goods_id`),
  KEY `idx_orders_user_id` (`user_id`),
  KEY `idx_orders_merchant_id` (`merchant_id`),
  KEY `idx_orders_parent_no` (`parent_no`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'全价冻干双拼猫粮','202609101020001',1,258.00,2,'13988776699','北京市朝阳区示例路 1 号','2026-09-10 10:20:00','已支付',2,1,'202609101020001'),(2,'耐咬磨牙绳结玩具','202609111430001',5,39.80,2,'13988776699','北京市朝阳区示例路 1 号','2026-09-11 14:30:00','待付款',2,1,'202609111430001'),(4,'宠物护理梳','202609141501004',8,35.00,1,NULL,NULL,'2026-09-14 15:01:04','已取消',4,1,'202609141501004'),(5,'耐咬磨牙绳结玩具','202609141501017',5,39.80,2,NULL,NULL,'2026-09-14 15:01:17','已取消',4,1,'202609141501017'),(6,'全价冻干双拼猫粮','202609141501023',1,129.00,1,NULL,NULL,'2026-09-14 15:01:23','已取消',4,1,'202609141501023'),(7,'猫咪磨牙小饼干','202609141501030',4,29.90,1,NULL,NULL,'2026-09-14 15:01:30','已取消',4,1,'202609141501030'),(8,'宠物护理梳','202609141501055',8,35.00,1,NULL,NULL,'2026-09-14 15:01:55','已取消',4,1,'202609141501055'),(9,'营养均衡犬粮','202609141502029',2,159.00,1,NULL,NULL,'2026-09-14 15:02:29','已取消',4,1,'202609141502029'),(13,'全价冻干双拼猫粮','20260915092837422115',1,516.00,4,NULL,NULL,'2026-09-15 09:28:37','已取消',4,1,'20260915092837422115'),(14,'营养均衡犬粮 等2件商品','20260915092903388924',2,188.90,2,NULL,NULL,'2026-09-15 09:29:03','已取消',4,1,'20260915092903388924');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `type`
--

DROP TABLE IF EXISTS `type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `type`
--

LOCK TABLES `type` WRITE;
/*!40000 ALTER TABLE `type` DISABLE KEYS */;
INSERT INTO `type` VALUES (1,'宠物主粮'),(4,'宠物护理'),(3,'宠物玩具'),(2,'宠物零食');
/*!40000 ALTER TABLE `type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名',
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `sex` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `infos` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个人介绍',
  `role` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色',
  `account` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','123','管理员','13677889988','admin@example.com','江苏南京',NULL,'男',27,'我是管理员','ADMIN',10000.00),(2,'tom','123','汤姆','13988776699','jerry@example.com','北京',NULL,'男',24,'喜欢给宠物挑选好物','USER',5000.00),(3,'jerry','123','杰瑞','15098765321','tom@example.com','上海',NULL,'男',25,'铲屎官一枚','USER',5000.00),(4,'123','$2a$10$LRKedhjy2GrcRnOOJnvYqucIaGoJIHzZ7lBCGQ9Q8kwHD7Ze5vnpO','123',NULL,NULL,NULL,'http://localhost:9999/file/download/touxiang.jpg',NULL,NULL,NULL,'USER',222222.00);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-17 11:23:50
