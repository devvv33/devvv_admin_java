-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: devvv_user
-- ------------------------------------------------------
-- Server version	8.0.19

CREATE DATABASE `devvv_user` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
use devvv_user;

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
-- Table structure for table `t_ban`
--

DROP TABLE IF EXISTS `t_ban`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ban` (
  `target_type` char(1) NOT NULL COMMENT '目标类型',
  `target_value` varchar(50) NOT NULL COMMENT '目标值',
  `ban_type` char(50) NOT NULL COMMENT '封禁类型',
  `start_time` datetime DEFAULT NULL COMMENT '封禁生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '封禁结束时间',
  `reason` varchar(255) DEFAULT NULL COMMENT '封禁原因',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL COMMENT '封禁时间',
  PRIMARY KEY (`target_type`,`target_value`,`ban_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='封禁记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_ban`
--

LOCK TABLES `t_ban` WRITE;
/*!40000 ALTER TABLE `t_ban` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_ban` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_ban_log`
--

DROP TABLE IF EXISTS `t_ban_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ban_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '日志id',
  `target_type` char(1) DEFAULT NULL COMMENT '目标类型',
  `target_value` varchar(50) DEFAULT NULL COMMENT '目标值',
  `ban_type` char(50) DEFAULT NULL COMMENT '封禁类型',
  `start_time` datetime DEFAULT NULL COMMENT '封禁生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '封禁结束时间',
  `reason` varchar(255) DEFAULT NULL COMMENT '封禁原因',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL COMMENT '封禁时间',
  PRIMARY KEY (`id`),
  KEY `idx_ttype_tvalue` (`target_type`,`target_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='封禁日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_ban_log`
--

LOCK TABLES `t_ban_log` WRITE;
/*!40000 ALTER TABLE `t_ban_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_ban_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `u_user_basic`
--

DROP TABLE IF EXISTS `u_user_basic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `u_user_basic` (
  `user_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_type` char(1) NOT NULL DEFAULT 'C' COMMENT '用户类型;C普通用户 T测试用户',
  `nickname` varchar(100) NOT NULL COMMENT '用户昵称',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户头像',
  `gender` char(1) DEFAULT NULL COMMENT '性别：男(M)、女(F)',
  `user_status` char(1) NOT NULL COMMENT '状态：正常(E)、封禁(D)',
  `vip_type` varchar(1) DEFAULT NULL COMMENT 'VIP类型， N、普通VIP Y、年VIP',
  `vip_expire_time` datetime DEFAULT NULL COMMENT 'VIP到期时间',
  `package_type` char(2) NOT NULL COMMENT '包体',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `register_time` datetime DEFAULT NULL COMMENT '注册时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户基本信息'
/*!50100 PARTITION BY HASH (`user_id`)
PARTITIONS 10 */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `u_user_basic`
--

LOCK TABLES `u_user_basic` WRITE;
/*!40000 ALTER TABLE `u_user_basic` DISABLE KEYS */;
/*!40000 ALTER TABLE `u_user_basic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `u_user_key_mark`
--

DROP TABLE IF EXISTS `u_user_key_mark`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `u_user_key_mark` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户id',
  `key` varchar(50) NOT NULL COMMENT 'key',
  `value` varchar(100) DEFAULT NULL COMMENT '值',
  `ext` varchar(255) DEFAULT NULL COMMENT '扩展数据',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`user_id`,`key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户标记'
/*!50100 PARTITION BY HASH (`user_id`)
PARTITIONS 10 */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `u_user_key_mark`
--

LOCK TABLES `u_user_key_mark` WRITE;
/*!40000 ALTER TABLE `u_user_key_mark` DISABLE KEYS */;
/*!40000 ALTER TABLE `u_user_key_mark` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `u_user_mobile`
--

DROP TABLE IF EXISTS `u_user_mobile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `u_user_mobile` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `mobile` varchar(20) NOT NULL COMMENT '手机号',
  `unsubscribe` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已取消订阅',
  `unsubscribe_time` datetime DEFAULT NULL COMMENT '取消订阅时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uni_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户手机号';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `u_user_mobile`
--

LOCK TABLES `u_user_mobile` WRITE;
/*!40000 ALTER TABLE `u_user_mobile` DISABLE KEYS */;
/*!40000 ALTER TABLE `u_user_mobile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'devvv_user'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-09 10:45:06
