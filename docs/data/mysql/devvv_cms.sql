-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: devvv_cms
-- ------------------------------------------------------
-- Server version	8.0.19

CREATE DATABASE `devvv_cms` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
use devvv_cms;

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
-- Table structure for table `cms_admin_user`
--

DROP TABLE IF EXISTS `cms_admin_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_admin_user` (
  `admin_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '管理员id',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(50) DEFAULT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `mobile` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `department_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `status` char(1) NOT NULL DEFAULT 'E' COMMENT '状态; E正常,D禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_count` bigint DEFAULT NULL COMMENT '登录次数',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_admin_user`
--

LOCK TABLES `cms_admin_user` WRITE;
/*!40000 ALTER TABLE `cms_admin_user` DISABLE KEYS */;
INSERT INTO `cms_admin_user` VALUES (1,'superadmin','e10adc3949ba59abbe56e057f20f883e','超级管理员','/cmsFile/2512/31/114239_742f096da61d4d0587497512971aa736.jpeg','15122222222',1,'E','2026-01-09 10:09:05','127.0.0.1',1,0,'2025-12-26 14:50:08',0,'2026-01-09 10:35:35');
/*!40000 ALTER TABLE `cms_admin_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_admin_user_role`
--

DROP TABLE IF EXISTS `cms_admin_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_admin_user_role` (
  `admin_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (`admin_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_admin_user_role`
--

LOCK TABLES `cms_admin_user_role` WRITE;
/*!40000 ALTER TABLE `cms_admin_user_role` DISABLE KEYS */;
INSERT INTO `cms_admin_user_role` VALUES (1,1,'2025-12-31 11:59:14');
/*!40000 ALTER TABLE `cms_admin_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_department`
--

DROP TABLE IF EXISTS `cms_department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '上级部门ID，0表示顶级部门',
  `id_path` varchar(500) NOT NULL COMMENT 'id路径',
  `dept_code` varchar(64) NOT NULL COMMENT '部门编码',
  `dept_name` varchar(64) NOT NULL COMMENT '部门名称',
  `sort` int DEFAULT '0' COMMENT '排序，越小越靠前',
  `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
  `mobile` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` char(1) DEFAULT 'E' COMMENT '状态：E启用，D停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_id_path` (`id_path`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_department`
--

LOCK TABLES `cms_department` WRITE;
/*!40000 ALTER TABLE `cms_department` DISABLE KEYS */;
INSERT INTO `cms_department` VALUES (1,0,'/1/','DEV','开发部',1,NULL,NULL,'E',NULL,0,'2025-12-31 09:40:17','2025-12-31 10:36:09'),(2,0,'/2/','PRODUCT','产品部',2,NULL,NULL,'E',NULL,0,'2025-12-31 09:41:44','2025-12-31 10:36:09'),(3,0,'/3/','TEST','测试部',3,NULL,NULL,'E',NULL,0,'2025-12-31 09:42:05','2025-12-31 10:36:09'),(4,0,'/4/','GUEST','游客',999,NULL,NULL,'E','新用户所在临时部门',0,'2025-12-31 09:48:35','2025-12-31 10:36:09');
/*!40000 ALTER TABLE `cms_department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_menu`
--

DROP TABLE IF EXISTS `cms_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `parent_id` bigint NOT NULL COMMENT '父级ID',
  `id_path` varchar(500) NOT NULL COMMENT 'ID路径',
  `menu_name` varchar(255) NOT NULL COMMENT '菜单名称',
  `menu_type` varchar(255) NOT NULL COMMENT '菜单类型',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT NULL COMMENT '排序',
  `route_path` varchar(255) DEFAULT NULL COMMENT '路由路径',
  `page_type` varchar(255) DEFAULT NULL COMMENT '页面类型',
  `api_url` varchar(255) DEFAULT NULL COMMENT '接口路径',
  `ext_api_url` varchar(2000) DEFAULT NULL COMMENT '扩展接口路径',
  `extra` varchar(5000) DEFAULT NULL COMMENT '扩展配置',
  `custom_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '自定义页面code',
  `button_position` varchar(255) DEFAULT NULL COMMENT '按钮所在位置',
  `button_action` varchar(255) DEFAULT NULL COMMENT '按钮行为',
  `before_show_script` varchar(2000) DEFAULT NULL COMMENT '行数据回显前脚本',
  `before_submit_script` varchar(2000) DEFAULT NULL COMMENT '表单提交前脚本',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_id` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_menu`
--

LOCK TABLES `cms_menu` WRITE;
/*!40000 ALTER TABLE `cms_menu` DISABLE KEYS */;
INSERT INTO `cms_menu` VALUES (1,0,'/1/','首页','MODULE','HomeFilled',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'2026-01-09 10:33:00',0,'2026-01-09 10:33:00'),(2,1,'/1/2/','工作台','PAGE','DataBoard',2,'/home','CUSTOM',NULL,NULL,NULL,'HomeDashboard','FOOTER','MODAL_FORM',NULL,NULL,0,'2026-01-09 10:33:00',0,'2026-01-09 10:33:00'),(3,1,'/1/3/','欢迎','PAGE','iconify:arcticons:hello-bank',3,'/welcome','CUSTOM',NULL,NULL,NULL,'GuestWelcome','FOOTER','MODAL_FORM',NULL,NULL,0,'2026-01-09 10:33:00',0,'2026-01-09 10:33:00'),(4,0,'/4/','系统管理','MODULE','Setting',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'2026-01-09 10:33:00',0,'2026-01-09 10:33:00'),(5,4,'/3/5/','菜单权限','DIRECTORY','Folder',1,NULL,NULL,NULL,NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,0,'2026-01-09 10:33:00',0,'2026-01-09 10:33:00'),(6,5,'/3/4/6/','菜单管理','PAGE','Menu',1,'/sys/menu','CUSTOM',NULL,NULL,NULL,'menu_manage',NULL,NULL,NULL,NULL,0,'2026-01-09 10:33:00',0,'2026-01-09 10:33:00'),(7,5,'/3/4/7/','用户列表','PAGE','UserFilled',9,'/sys/admin','LIST','/cmsApi/admin/pageListAdmin',NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(8,7,'/3/4/20/8/','添加用户','BUTTON','Plus',1,NULL,NULL,'/cmsApi/admin/createAdmin',NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(9,7,'/3/4/20/9/','修改用户','BUTTON','Edit',2,NULL,NULL,'/cmsApi/admin/updateAdmin',NULL,NULL,NULL,'ROW','MODAL_FORM','row.roleIdList = row.roleList?.map(role => role.id);\r\nreturn row;',NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(10,5,'/3/4/10/','角色列表','PAGE','User',10,'/sys/role','LIST','/cmsApi/role/pageListRole',NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(11,10,'/3/4/6/11/','添加角色','BUTTON','Plus',0,NULL,NULL,'/cmsApi/role/createRole',NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(12,10,'/3/4/6/12/','修改角色','BUTTON','Edit',11,NULL,NULL,'/cmsApi/role/updateRole',NULL,NULL,NULL,'ROW','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(13,10,'/3/4/6/13/','删除角色','BUTTON','Delete',12,NULL,NULL,'/cmsApi/role/deleteRole',NULL,NULL,NULL,'ROW','CONFIRM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(14,10,'/3/4/6/14/','分配菜单','BUTTON','iconify:grommet-icons:tree',13,NULL,NULL,'/cmsApi/role/assignRoleMenu','/cmsApi/role/roleMenus,/cmsApi/menu/allMenuTree','{\"buttonType\":\"primary\"}',NULL,'ROW','MODAL_FORM','const menuIds = await request.post(\'/cmsApi/role/roleMenus\', {roleId: row.id})\nreturn {roleId:row.id, roleName:row.roleName, menuIds};',NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(15,5,'/3/4/15/','部门列表','PAGE','iconify:mingcute:department-line',11,'/sys/dept','LIST','/cmsApi/dept/treeListDept',NULL,'{\"rowKey\":\"id\",\"defaultExpandAll\":true,\"pagination\":false}',NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(16,15,'/3/4/12/16/','添加部门','BUTTON','Plus',1,NULL,NULL,'/cmsApi/dept/createDept',NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(17,15,'/3/4/12/17/','添加下级部门','BUTTON','Plus',10,NULL,NULL,'/cmsApi/dept/createDept',NULL,NULL,NULL,'ROW','MODAL_FORM','return { parentId: row.id, parentName: row.deptName}',NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(18,15,'/3/4/12/18/','修改部门','BUTTON','Edit',11,NULL,NULL,'/cmsApi/dept/updateDept',NULL,NULL,NULL,'ROW','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(19,15,'/3/4/12/19/','删除部门','BUTTON','Delete',12,NULL,NULL,'/cmsApi/dept/deleteDept',NULL,NULL,NULL,'ROW','CONFIRM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(20,4,'/3/20/','系统设置','DIRECTORY','Setting',2,NULL,NULL,NULL,NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(21,20,'/3/39/21/','系统参数','PAGE','Setting',1,'/sys/setting','LIST','/cmsApi/sys/setting/listAll',NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(22,21,'/3/39/40/22/','添加配置','BUTTON','Plus',1,NULL,NULL,'/cmsApi/sys/setting/createSetting',NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(23,21,'/3/39/40/23/','同步更新','BUTTON','iconify:material-symbols-light:rule-settings',2,NULL,NULL,'/cmsApi/sys/setting/syncSetting',NULL,NULL,NULL,'FOOTER','CONFIRM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(24,21,'/3/39/40/24/','修改配置','BUTTON','Edit',3,NULL,NULL,'/cmsApi/sys/setting/updateSetting',NULL,NULL,NULL,'ROW','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(25,21,'/3/39/40/25/','历史记录','BUTTON','Coin',12,NULL,NULL,'/cmsApi/sys/setting/listHistory',NULL,'{ \"buttonType\":\"warning\"}',NULL,'ROW','MODAL_LIST','return  {key:row.key}',NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(26,25,'/3/39/40/44/26/','还原配置','BUTTON','RefreshLeft',1,NULL,NULL,'/cmsApi/sys/setting/updateSetting',NULL,'{ \"buttonType\":\"danger\"}',NULL,'ROW','CONFIRM',NULL,'return {\n    key: formData.key,\n    value: formData.value,\n    sort: formData.sort,\n    remark: formData.remark,\n    changeRemark: \'还原自版本:\'+formData.version\n}',1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(27,4,'/3/27/','文档','DIRECTORY','CopyDocument',100,NULL,NULL,NULL,NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(28,27,'/3/37/28/','Java接口文档','PAGE','Document',100,'http://127.0.0.1:8801/swagger-ui/index.html','OUTER_LINK',NULL,NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(29,27,'/3/37/29/','Nacos','PAGE','iconify:material-symbols:rule-settings',101,'http://127.0.0.1:8849/index.html','OUTER_LINK',NULL,NULL,NULL,NULL,'FOOTER','MODAL_FORM',NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01'),(30,27,'/3/37/30/','ElementUI组件库','PAGE','ElementPlus',102,'https://element-plus.org/zh-CN/component/overview','OUTER_LINK',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,'2026-01-09 10:33:01',1,'2026-01-09 10:33:01');
/*!40000 ALTER TABLE `cms_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_menu_field`
--

DROP TABLE IF EXISTS `cms_menu_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_menu_field` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `menu_id` bigint DEFAULT NULL COMMENT '所属菜单ID',
  `field_key` varchar(255) NOT NULL COMMENT '字段Code',
  `field_label` varchar(255) NOT NULL COMMENT '字段描述',
  `field_type` varchar(255) NOT NULL COMMENT '字段类型; SEARCH, COLUMN, FORM',
  `required` tinyint(1) DEFAULT NULL COMMENT '是否必须',
  `input_type` varchar(255) DEFAULT NULL COMMENT '输入框类型',
  `show_type` varchar(255) DEFAULT NULL COMMENT '展示类型',
  `width` varchar(255) DEFAULT NULL COMMENT '宽',
  `format_script` varchar(2000) DEFAULT NULL COMMENT '格式化脚本',
  `extra` varchar(5000) DEFAULT NULL COMMENT '扩展配置',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单页面字段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_menu_field`
--

LOCK TABLES `cms_menu_field` WRITE;
/*!40000 ALTER TABLE `cms_menu_field` DISABLE KEYS */;
INSERT INTO `cms_menu_field` VALUES (1,7,'adminId','用户ID','Search',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(2,7,'nickname','昵称','Search',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(3,7,'roleId','角色','Search',0,'select',NULL,NULL,NULL,'{\"queryUrl\":\"/cmsApi/role/listRole\",\"props\":{\"value\":\"id\",\"label\":\"roleName\"}}',1,'2026-01-09 10:33:01'),(4,7,'adminId','用户ID','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(5,7,'username','用户名','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(6,7,'nickname','昵称','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(7,7,'avatar','头像','Column',NULL,NULL,'IMAGE','','','',1,'2026-01-09 10:33:01'),(8,7,'mobile','手机号','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(9,7,'deptId','所属部门','Column',NULL,NULL,'TEXT','','return row.department?.deptName || row.deptId','',1,'2026-01-09 10:33:01'),(10,7,'roles','角色','Column',NULL,NULL,'TEXT','','return row.roleList?.map(item => item.roleName).join(\',\')','',1,'2026-01-09 10:33:01'),(11,7,'status','状态','Column',NULL,NULL,'TAG','','return value===\'Enable\'?\'正常\':value===\'Disable\'?\'禁用\':value','',1,'2026-01-09 10:33:01'),(12,7,'lastLoginTime','最后登录时间','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(13,8,'username','用户名','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(14,8,'password','密码','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(15,8,'nickname','昵称','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(16,8,'mobile','手机号','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(17,8,'status','状态','Form',1,'select',NULL,NULL,NULL,'{\"defaultValue\":\"Enable\",\"enumOptions\":\"com.devvv.commons.common.enums.status.EnableStatus\"}',1,'2026-01-09 10:33:01'),(18,8,'departmentId','所属部门','Form',1,'treeSelect',NULL,NULL,NULL,'{\"queryUrl\":\"/cmsApi/dept/treeListDept\",\"checkStrictly\":true,\"showCheckbox\":false,\"props\":{\"value\":\"id\",\"label\":\"deptName\",\"children\":\"children\"},\"defaultValue\":\"4\"}',1,'2026-01-09 10:33:01'),(19,8,'roleIdList','角色','Form',1,'select',NULL,NULL,NULL,'{\"queryUrl\":\"/cmsApi/role/listRole\",\"multiple\":true,\"props\":{\"value\":\"id\",\"label\":\"roleName\"},\"defaultValue\":[\"2\"]}',1,'2026-01-09 10:33:01'),(20,9,'adminId','用户ID','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(21,9,'nickname','昵称','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(22,9,'avatarFile','头像','Form',0,'upload',NULL,NULL,NULL,'{\"accept\":\"image/*\"}',1,'2026-01-09 10:33:01'),(23,9,'mobile','手机号','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(24,9,'status','状态','Form',1,'select',NULL,NULL,NULL,'{\"defaultValue\":\"Enable\",\"enumOptions\":\"com.devvv.commons.common.enums.status.EnableStatus\"}',1,'2026-01-09 10:33:01'),(25,9,'departmentId','所属部门','Form',1,'treeSelect',NULL,NULL,NULL,'{\"queryUrl\":\"/cmsApi/dept/treeListDept\",\"checkStrictly\":true,\"showCheckbox\":false,\"props\":{\"value\":\"id\",\"label\":\"deptName\",\"children\":\"children\"},\"defaultValue\":\"4\"}',1,'2026-01-09 10:33:01'),(26,9,'roleIdList','角色','Form',1,'select',NULL,NULL,NULL,'{\"queryUrl\":\"/cmsApi/role/listRole\",\"multiple\":true,\"props\":{\"value\":\"id\",\"label\":\"roleName\"},\"defaultValue\":[\"2\"]}',1,'2026-01-09 10:33:01'),(27,10,'roleCode','角色Code','Search',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(28,10,'roleName','角色名称','Search',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(29,10,'status','状态','Search',0,'select',NULL,NULL,NULL,'{\"enumOptions\":\"com.devvv.commons.common.enums.status.EnableStatus\"}',1,'2026-01-09 10:33:01'),(30,10,'id','角色ID','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(31,10,'roleCode','角色Code','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(32,10,'roleName','角色名称','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(33,10,'status','状态','Column',NULL,NULL,'TAG','','return value===\'Enable\'?\'正常\':value===\'Disable\'?\'禁用\':value','',1,'2026-01-09 10:33:01'),(34,10,'remark','备注','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(35,10,'createTime','创建时间','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(36,11,'roleCode','角色Code','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(37,11,'roleName','角色名称','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(38,11,'sort','序号','Form',1,'number',NULL,NULL,NULL,'{\"defaultValue\":10}',1,'2026-01-09 10:33:01'),(39,11,'status','状态','Form',1,'select',NULL,NULL,NULL,'{\"defaultValue\":\"Enable\",\"enumOptions\":\"com.devvv.commons.common.enums.status.EnableStatus\"}',1,'2026-01-09 10:33:01'),(40,11,'remark','备注','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(41,12,'id','角色ID','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(42,12,'roleCode','角色Code','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(43,12,'roleName','角色名称','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(44,12,'sort','排序','Form',1,'number',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(45,12,'status','状态','Form',1,'select',NULL,NULL,NULL,'{\"enumOptions\":\"com.devvv.commons.common.enums.status.EnableStatus\"}',1,'2026-01-09 10:33:01'),(46,12,'remark','备注','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(47,14,'roleId','角色ID','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(48,14,'roleName','角色名称','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(49,14,'menuIds','菜单','Form',0,'treeSelect',NULL,NULL,NULL,'{\"queryUrl\":\"/cmsApi/menu/allMenuTree\",\"multiple\":true,\"checkStrictly\":false,\"showCheckbox\":true,\"collapseTags\":true,\"props\":{\"value\":\"id\",\"label\":\"menuName\",\"children\":\"children\"}}',1,'2026-01-09 10:33:01'),(50,15,'id','ID','Column',NULL,NULL,'TEXT','10%','','',1,'2026-01-09 10:33:01'),(51,15,'idPath','路径','Column',NULL,NULL,'TEXT','20%','','',1,'2026-01-09 10:33:01'),(52,15,'deptCode','部门Code','Column',NULL,NULL,'TEXT','10%','','',1,'2026-01-09 10:33:01'),(53,15,'deptName','部门名称','Column',NULL,NULL,'TEXT','10%','','',1,'2026-01-09 10:33:01'),(54,15,'leader','负责人','Column',NULL,NULL,'TEXT','10%','','',1,'2026-01-09 10:33:01'),(55,15,'mobile','联系电话','Column',NULL,NULL,'TEXT','10%','','',1,'2026-01-09 10:33:01'),(56,15,'status','状态','Column',NULL,NULL,'TAG','10%','return value===\'Enable\'?\'正常\':value===\'Disable\'?\'禁用\':value','',1,'2026-01-09 10:33:01'),(57,15,'remark','备注','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(58,16,'parentId','上级部门ID','Form',1,'input',NULL,NULL,NULL,'{\"defaultValue\":0}',1,'2026-01-09 10:33:01'),(59,16,'deptCode','部门Code','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(60,16,'deptName','部门名称','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(61,16,'sort','排序','Form',1,'number',NULL,NULL,NULL,'{\"defaultValue\":10}',1,'2026-01-09 10:33:01'),(62,16,'leader','负责人','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(63,16,'mobile','联系电话','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(64,16,'status','状态','Form',1,'select',NULL,NULL,NULL,'{\"defaultValue\":\"Enable\",\"enumOptions\":\"com.devvv.commons.common.enums.status.EnableStatus\"}',1,'2026-01-09 10:33:01'),(65,16,'remark','备注','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(66,17,'parentId','父级ID','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(67,17,'parentName','父级部门','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(68,17,'deptCode','部门Code','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(69,17,'deptName','部门名称','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(70,17,'sort','排序','Form',1,'number',NULL,NULL,NULL,'{\"defaultValue\":10}',1,'2026-01-09 10:33:01'),(71,17,'leader','负责人','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(72,17,'mobile','联系电话','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(73,17,'status','状态','Form',1,'select',NULL,NULL,NULL,'{\"defaultValue\":\"Enable\",\"enumOptions\":\"com.devvv.commons.common.enums.status.EnableStatus\"}',1,'2026-01-09 10:33:01'),(74,17,'remark','备注','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(75,18,'id','ID','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(76,18,'parentId','上级部门ID','Form',1,'input',NULL,NULL,NULL,'{\"disabled\":true}',1,'2026-01-09 10:33:01'),(77,18,'deptCode','部门Code','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(78,18,'deptName','部门名称','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(79,18,'sort','排序','Form',1,'number',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(80,18,'leader','负责人','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(81,18,'mobile','联系电话','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(82,18,'status','状态','Form',1,'select',NULL,NULL,NULL,'{\"enumOptions\":\"com.devvv.commons.common.enums.status.EnableStatus\"}',1,'2026-01-09 10:33:01'),(83,18,'remark','备注','Form',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(84,21,'key','参数名','Search',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(85,21,'value','参数值','Search',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(86,21,'remark','备注','Search',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(87,21,'key','参数名','Column',NULL,NULL,'TEXT','200px','','',1,'2026-01-09 10:33:01'),(88,21,'value','参数值','Column',NULL,NULL,'TEXT','50%','','',1,'2026-01-09 10:33:01'),(89,21,'version','版本号','Column',NULL,NULL,'TEXT','100px','','',1,'2026-01-09 10:33:01'),(90,21,'sort','排序','Column',NULL,NULL,'TEXT','100px','','',1,'2026-01-09 10:33:01'),(91,21,'remark','备注','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(92,21,'updateTime','更新时间','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(93,22,'key','参数名','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(94,22,'value','参数值','Form',1,'textarea',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(95,22,'sort','排序','Form',0,'number',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(96,22,'remark','备注','Form',0,'textarea',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(97,24,'key','参数名','Form',1,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(98,24,'value','参数值','Form',1,'textarea',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(99,24,'sort','排序','Form',0,'number',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(100,24,'remark','备注','Form',0,'textarea',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(101,24,'changeRemark','变更备注','Form',0,'textarea',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(102,25,'value','参数值','Search',0,'input',NULL,NULL,NULL,'',1,'2026-01-09 10:33:01'),(103,25,'id','日志ID','Column',NULL,NULL,'TEXT','70px','','',1,'2026-01-09 10:33:01'),(104,25,'key','参数名','Column',NULL,NULL,'TEXT','200px','','',1,'2026-01-09 10:33:01'),(105,25,'value','参数值','Column',NULL,NULL,'TEXT','30%','','',1,'2026-01-09 10:33:01'),(106,25,'sort','序号','Column',NULL,NULL,'TEXT','100px','','',1,'2026-01-09 10:33:01'),(107,25,'remark','备注','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(108,25,'version','版本号','Column',NULL,NULL,'TEXT','70px','','',1,'2026-01-09 10:33:01'),(109,25,'changeRemark','变动说明','Column',NULL,NULL,'TEXT','','','',1,'2026-01-09 10:33:01'),(110,25,'changeTime','变动时间','Column',NULL,NULL,'TEXT','180px','','',1,'2026-01-09 10:33:01'),(111,25,'changeAdminName','操作人','Column',NULL,NULL,'TEXT','100px','','',1,'2026-01-09 10:33:01');
/*!40000 ALTER TABLE `cms_menu_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_role`
--

DROP TABLE IF EXISTS `cms_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码，唯一标识，如 ADMIN',
  `role_name` varchar(64) NOT NULL COMMENT '角色名称，如 管理员',
  `status` char(1) DEFAULT 'E' COMMENT '状态：E启用，D停用',
  `sort` int DEFAULT '0' COMMENT '排序值，越小越靠前',
  `data_scope` tinyint DEFAULT '0' COMMENT '数据权限范围：0全部数据 1本部门 2本部门及以下 3仅本人 4自定义',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_role`
--

LOCK TABLES `cms_role` WRITE;
/*!40000 ALTER TABLE `cms_role` DISABLE KEYS */;
INSERT INTO `cms_role` VALUES (1,'SUPER_ADMIN','超管','E',0,0,NULL,0,'2025-12-26 16:00:48','2025-12-26 16:01:06'),(2,'GUEST','游客','E',2,0,'新用户的临时角色',0,'2025-12-31 10:33:59','2025-12-31 10:34:24');
/*!40000 ALTER TABLE `cms_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_role_menu`
--

DROP TABLE IF EXISTS `cms_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (`role_id`,`menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_role_menu`
--

LOCK TABLES `cms_role_menu` WRITE;
/*!40000 ALTER TABLE `cms_role_menu` DISABLE KEYS */;
INSERT INTO `cms_role_menu` VALUES (2,3,'2025-12-31 10:34:24');
/*!40000 ALTER TABLE `cms_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_setting`
--

DROP TABLE IF EXISTS `sys_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_setting` (
  `key` varchar(50) NOT NULL COMMENT '参数名',
  `value` text NOT NULL COMMENT '参数值',
  `read_only` tinyint(1) DEFAULT '0' COMMENT '是否只读',
  `sort` int NOT NULL DEFAULT '999' COMMENT '序号',
  `remark` varchar(300) DEFAULT NULL COMMENT '备注信息',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`key`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_setting`
--

LOCK TABLES `sys_setting` WRITE;
/*!40000 ALTER TABLE `sys_setting` DISABLE KEYS */;
INSERT INTO `sys_setting` VALUES ('SysVersion','1.0.0',0,10,'系统版本',1,'2026-01-05 12:43:48','2026-01-05 12:43:48');
/*!40000 ALTER TABLE `sys_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_setting_history`
--

DROP TABLE IF EXISTS `sys_setting_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_setting_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `key` varchar(50) DEFAULT NULL COMMENT '参数名',
  `value` text COMMENT '参数值',
  `read_only` tinyint(1) DEFAULT '0' COMMENT '是否只读',
  `sort` int NOT NULL DEFAULT '999' COMMENT '序号',
  `remark` varchar(300) DEFAULT NULL COMMENT '备注信息',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `change_remark` varchar(500) DEFAULT NULL COMMENT '变动说明',
  `change_admin_id` bigint DEFAULT NULL COMMENT '修改人',
  `change_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_key` (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数配置-历史记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_setting_history`
--

LOCK TABLES `sys_setting_history` WRITE;
/*!40000 ALTER TABLE `sys_setting_history` DISABLE KEYS */;
INSERT INTO `sys_setting_history` VALUES (1,'SysVersion','1.0.0',0,10,'系统版本',1,'添加配置',1,'2026-01-05 12:43:48');
/*!40000 ALTER TABLE `sys_setting_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'devvv_cms'
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
