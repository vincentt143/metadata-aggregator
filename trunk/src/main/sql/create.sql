-- MySQL dump 10.13  Distrib 5.5.19, for osx10.6 (i386)
--
-- Host: 192.168.56.3    Database: sydma
-- ------------------------------------------------------
-- Server version	5.5.24-0ubuntu0.12.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity_log`
--

DROP TABLE IF EXISTS `activity_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity` varchar(255) DEFAULT NULL,
  `changes` varchar(20000) NOT NULL,
  `date` datetime NOT NULL,
  `version` int(11) DEFAULT NULL,
  `principal` bigint(20) NOT NULL,
  `research_group` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK611AA614CAA83BB1` (`principal`),
  KEY `FK611AA6145F7D97C7` (`research_group`),
  CONSTRAINT `FK611AA6145F7D97C7` FOREIGN KEY (`research_group`) REFERENCES `research_group` (`id`),
  CONSTRAINT `FK611AA614CAA83BB1` FOREIGN KEY (`principal`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `building`
--

DROP TABLE IF EXISTS `building`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `building` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address_line1` varchar(255) DEFAULT NULL,
  `address_line2` varchar(255) DEFAULT NULL,
  `building_code` varchar(255) NOT NULL,
  `building_name` varchar(255) NOT NULL,
  `campus` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `post_code` varchar(255) DEFAULT NULL,
  `state_name` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=741 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dbbackup`
--

DROP TABLE IF EXISTS `dbbackup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dbbackup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` varchar(100) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `file` varchar(1000) NOT NULL,
  `user` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `research_dataset` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK80D8ABC0FF878579` (`research_dataset`),
  CONSTRAINT `FK80D8ABC0FF878579` FOREIGN KEY (`research_dataset`) REFERENCES `research_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dbschema`
--

DROP TABLE IF EXISTS `dbschema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dbschema` (
  `name` varchar(100) NOT NULL,
  `filename` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dbuser`
--

DROP TABLE IF EXISTS `dbuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dbuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `access_level` varchar(255) NOT NULL,
  `db_password` varchar(255) NOT NULL,
  `db_username` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `database_instance` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `db_username` (`db_username`),
  KEY `FKB04089492CBF15BC` (`database_instance`),
  CONSTRAINT `FKB04089492CBF15BC` FOREIGN KEY (`database_instance`) REFERENCES `research_datasetdb` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dms_user`
--

DROP TABLE IF EXISTS `dms_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dms_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin` bit(1) NOT NULL,
  `username` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `file_annotation`
--

DROP TABLE IF EXISTS `file_annotation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_annotation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `annotation` varchar(1000) NOT NULL,
  `out_of_date` bit(1) NOT NULL,
  `path` varchar(20000) NOT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `average_speed` double DEFAULT NULL,
  `copy_started_time_stamp` bigint(20) DEFAULT NULL,
  `created_time_stamp` bigint(20) NOT NULL,
  `current_bytes` bigint(20) NOT NULL,
  `current_number_of_directories` int(11) NOT NULL,
  `current_number_of_files` int(11) NOT NULL,
  `destination` varchar(8192) DEFAULT NULL,
  `destination_dir` varchar(8192) DEFAULT NULL,
  `finished_time_stamp` bigint(20) DEFAULT NULL,
  `project_code` bigint(20) DEFAULT NULL,
  `source` varchar(8192) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_bytes` bigint(20) NOT NULL,
  `total_number_of_directories` int(11) NOT NULL,
  `total_number_of_files` int(11) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `update_time_stamp` bigint(20) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `worker_id` int(11) NOT NULL,
  `dms_user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK19BBDDB34D3BB` (`dms_user`),
  CONSTRAINT `FK19BBDDB34D3BB` FOREIGN KEY (`dms_user`) REFERENCES `dms_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_detail_metadata`
--

DROP TABLE IF EXISTS `job_detail_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job_detail_metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `metadata` longtext,
  `metadata_schema` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `job` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK393636DBADCEE860` (`job`),
  CONSTRAINT `FK393636DBADCEE860` FOREIGN KEY (`job`) REFERENCES `job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_from`
--

DROP TABLE IF EXISTS `job_from`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job_from` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_dir` varchar(8192) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `job` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9FB602ECADCEE860` (`job`),
  CONSTRAINT `FK9FB602ECADCEE860` FOREIGN KEY (`job`) REFERENCES `job` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `master_vocabulary`
--

DROP TABLE IF EXISTS `master_vocabulary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `master_vocabulary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `master_vocabulary_term`
--

DROP TABLE IF EXISTS `master_vocabulary_term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `master_vocabulary_term` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `master_vocabulary` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKEC29C848FB06DB83` (`master_vocabulary`),
  CONSTRAINT `FKEC29C848FB06DB83` FOREIGN KEY (`master_vocabulary`) REFERENCES `master_vocabulary` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permission_entry`
--

DROP TABLE IF EXISTS `permission_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `permission_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `access_level` varchar(255) DEFAULT NULL,
  `path` varchar(20000) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4B075A0236E0B74E` (`user`),
  CONSTRAINT `FK4B075A0236E0B74E` FOREIGN KEY (`user`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `public_access_right`
--

DROP TABLE IF EXISTS `public_access_right`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `public_access_right` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `short_name` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `publication`
--

DROP TABLE IF EXISTS `publication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `publication` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `citations` varchar(1000) NOT NULL,
  `url` varchar(400) NOT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rds_request`
--

DROP TABLE IF EXISTS `rds_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rds_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount_of_storage` int(11) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `name` varchar(100) NOT NULL,
  `request_status` varchar(255) DEFAULT NULL,
  `requester` varchar(255) DEFAULT NULL,
  `time_and_date_of_request` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `data_management_contact` bigint(20) DEFAULT NULL,
  `principal_investigator` bigint(20) NOT NULL,
  `research_group` bigint(20) DEFAULT NULL,
  `subject_code` varchar(255) NOT NULL,
  `subject_code2` varchar(255) DEFAULT NULL,
  `subject_code3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK232440D11C7F6E5C` (`data_management_contact`),
  KEY `FK232440D14696BB9D` (`principal_investigator`),
  KEY `FK232440D149E08F06` (`subject_code`),
  KEY `FK232440D15F7D97C7` (`research_group`),
  KEY `FK232440D1BE0B58F8` (`subject_code2`),
  KEY `FK232440D1BE0B58F9` (`subject_code3`),
  CONSTRAINT `FK232440D11C7F6E5C` FOREIGN KEY (`data_management_contact`) REFERENCES `users` (`id`),
  CONSTRAINT `FK232440D14696BB9D` FOREIGN KEY (`principal_investigator`) REFERENCES `users` (`id`),
  CONSTRAINT `FK232440D149E08F06` FOREIGN KEY (`subject_code`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FK232440D15F7D97C7` FOREIGN KEY (`research_group`) REFERENCES `research_group` (`id`),
  CONSTRAINT `FK232440D1BE0B58F8` FOREIGN KEY (`subject_code2`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FK232440D1BE0B58F9` FOREIGN KEY (`subject_code3`) REFERENCES `research_subject_code` (`subject_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_database_query`
--

DROP TABLE IF EXISTS `research_database_query`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_database_query` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) NOT NULL,
  `name` varchar(100) NOT NULL,
  `query` varchar(1000) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `research_datasetdb` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK315028283BBC2035` (`research_datasetdb`),
  CONSTRAINT `FK315028283BBC2035` FOREIGN KEY (`research_datasetdb`) REFERENCES `research_datasetdb` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_dataset`
--

DROP TABLE IF EXISTS `research_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_dataset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `additional_location_information` varchar(255) DEFAULT NULL,
  `date_from` datetime DEFAULT NULL,
  `date_to` datetime DEFAULT NULL,
  `description` varchar(1000) NOT NULL,
  `is_physical` bit(1) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `publicise_status` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `physical_location` bigint(20) DEFAULT NULL,
  `public_access_right` bigint(20) DEFAULT NULL,
  `research_project` bigint(20) NOT NULL,
  `subject_code` varchar(255) NOT NULL,
  `subject_code2` varchar(255) DEFAULT NULL,
  `subject_code3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`research_project`),
  KEY `FK45C951D443794889` (`physical_location`),
  KEY `FK45C951D42E9CB2BB` (`research_project`),
  KEY `FK45C951D449E08F06` (`subject_code`),
  KEY `FK45C951D4FF72B9CE` (`public_access_right`),
  KEY `FK45C951D4BE0B58F8` (`subject_code2`),
  KEY `FK45C951D4BE0B58F9` (`subject_code3`),
  CONSTRAINT `FK45C951D42E9CB2BB` FOREIGN KEY (`research_project`) REFERENCES `research_project` (`id`),
  CONSTRAINT `FK45C951D443794889` FOREIGN KEY (`physical_location`) REFERENCES `building` (`id`),
  CONSTRAINT `FK45C951D449E08F06` FOREIGN KEY (`subject_code`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FK45C951D4BE0B58F8` FOREIGN KEY (`subject_code2`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FK45C951D4BE0B58F9` FOREIGN KEY (`subject_code3`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FK45C951D4FF72B9CE` FOREIGN KEY (`public_access_right`) REFERENCES `public_access_right` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_dataset_keywords`
--

DROP TABLE IF EXISTS `research_dataset_keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_dataset_keywords` (
  `research_dataset` bigint(20) NOT NULL,
  `keywords` bigint(20) NOT NULL,
  KEY `FKDA874DB593EE2648` (`keywords`),
  KEY `FKDA874DB5FF878579` (`research_dataset`),
  CONSTRAINT `FKDA874DB593EE2648` FOREIGN KEY (`keywords`) REFERENCES `vocabulary` (`id`),
  CONSTRAINT `FKDA874DB5FF878579` FOREIGN KEY (`research_dataset`) REFERENCES `research_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_dataset_publications`
--

DROP TABLE IF EXISTS `research_dataset_publications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_dataset_publications` (
  `research_dataset` bigint(20) NOT NULL,
  `publications` bigint(20) NOT NULL,
  UNIQUE KEY `publications` (`publications`),
  KEY `FK1BA55452CCB0C73B` (`publications`),
  KEY `FK1BA55452FF878579` (`research_dataset`),
  CONSTRAINT `FK1BA55452CCB0C73B` FOREIGN KEY (`publications`) REFERENCES `publication` (`id`),
  CONSTRAINT `FK1BA55452FF878579` FOREIGN KEY (`research_dataset`) REFERENCES `research_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_datasetdb`
--

DROP TABLE IF EXISTS `research_datasetdb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_datasetdb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_of_restoration` varchar(100) DEFAULT NULL,
  `db_hostname` varchar(255) NOT NULL,
  `db_name` varchar(255) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `state` varchar(1000) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `dbbackup_used` bigint(20) DEFAULT NULL,
  `db_schema` varchar(100) NOT NULL,
  `last_restored_by` bigint(20) DEFAULT NULL,
  `research_dataset` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `db_name` (`db_name`),
  KEY `FKF8BC3952E4F54CFA` (`last_restored_by`),
  KEY `FKF8BC3952E2E2EFD9` (`db_schema`),
  KEY `FKF8BC3952FF878579` (`research_dataset`),
  KEY `FKF8BC3952B70C454` (`dbbackup_used`),
  CONSTRAINT `FKF8BC3952B70C454` FOREIGN KEY (`dbbackup_used`) REFERENCES `dbbackup` (`id`),
  CONSTRAINT `FKF8BC3952E2E2EFD9` FOREIGN KEY (`db_schema`) REFERENCES `dbschema` (`name`),
  CONSTRAINT `FKF8BC3952E4F54CFA` FOREIGN KEY (`last_restored_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKF8BC3952FF878579` FOREIGN KEY (`research_dataset`) REFERENCES `research_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_group`
--

DROP TABLE IF EXISTS `research_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) NOT NULL,
  `directory_path` varchar(255) DEFAULT NULL,
  `is_physical` bit(1) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `url` varchar(400) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `data_management_contact` bigint(20) DEFAULT NULL,
  `principal_investigator` bigint(20) NOT NULL,
  `subject_code` varchar(255) NOT NULL,
  `subject_code2` varchar(255) DEFAULT NULL,
  `subject_code3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `FKC9643ADB1C7F6E5C` (`data_management_contact`),
  KEY `FKC9643ADB4696BB9D` (`principal_investigator`),
  KEY `FKC9643ADB49E08F06` (`subject_code`),
  KEY `FKC9643ADBBE0B58F8` (`subject_code2`),
  KEY `FKC9643ADBBE0B58F9` (`subject_code3`),
  CONSTRAINT `FKC9643ADB1C7F6E5C` FOREIGN KEY (`data_management_contact`) REFERENCES `users` (`id`),
  CONSTRAINT `FKC9643ADB4696BB9D` FOREIGN KEY (`principal_investigator`) REFERENCES `users` (`id`),
  CONSTRAINT `FKC9643ADB49E08F06` FOREIGN KEY (`subject_code`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FKC9643ADBBE0B58F8` FOREIGN KEY (`subject_code2`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FKC9643ADBBE0B58F9` FOREIGN KEY (`subject_code3`) REFERENCES `research_subject_code` (`subject_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_group_keywords`
--

DROP TABLE IF EXISTS `research_group_keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_group_keywords` (
  `research_group` bigint(20) NOT NULL,
  `keywords` bigint(20) NOT NULL,
  KEY `FK4A249C8E93EE2648` (`keywords`),
  KEY `FK4A249C8E5F7D97C7` (`research_group`),
  CONSTRAINT `FK4A249C8E5F7D97C7` FOREIGN KEY (`research_group`) REFERENCES `research_group` (`id`),
  CONSTRAINT `FK4A249C8E93EE2648` FOREIGN KEY (`keywords`) REFERENCES `vocabulary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_project`
--

DROP TABLE IF EXISTS `research_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) NOT NULL,
  `name` varchar(100) NOT NULL,
  `url` varchar(400) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `research_group` bigint(20) NOT NULL,
  `subject_code` varchar(255) NOT NULL,
  `subject_code2` varchar(255) DEFAULT NULL,
  `subject_code3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`research_group`),
  KEY `FKDD53E87549E08F06` (`subject_code`),
  KEY `FKDD53E8755F7D97C7` (`research_group`),
  KEY `FKDD53E875BE0B58F8` (`subject_code2`),
  KEY `FKDD53E875BE0B58F9` (`subject_code3`),
  CONSTRAINT `FKDD53E87549E08F06` FOREIGN KEY (`subject_code`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FKDD53E8755F7D97C7` FOREIGN KEY (`research_group`) REFERENCES `research_group` (`id`),
  CONSTRAINT `FKDD53E875BE0B58F8` FOREIGN KEY (`subject_code2`) REFERENCES `research_subject_code` (`subject_code`),
  CONSTRAINT `FKDD53E875BE0B58F9` FOREIGN KEY (`subject_code3`) REFERENCES `research_subject_code` (`subject_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_project_keywords`
--

DROP TABLE IF EXISTS `research_project_keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_project_keywords` (
  `research_project` bigint(20) NOT NULL,
  `keywords` bigint(20) NOT NULL,
  KEY `FKC8B59C3493EE2648` (`keywords`),
  KEY `FKC8B59C342E9CB2BB` (`research_project`),
  CONSTRAINT `FKC8B59C342E9CB2BB` FOREIGN KEY (`research_project`) REFERENCES `research_project` (`id`),
  CONSTRAINT `FKC8B59C3493EE2648` FOREIGN KEY (`keywords`) REFERENCES `vocabulary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_project_publications`
--

DROP TABLE IF EXISTS `research_project_publications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_project_publications` (
  `research_project` bigint(20) NOT NULL,
  `publications` bigint(20) NOT NULL,
  UNIQUE KEY `publications` (`publications`),
  KEY `FK94FA4B51CCB0C73B` (`publications`),
  KEY `FK94FA4B512E9CB2BB` (`research_project`),
  CONSTRAINT `FK94FA4B512E9CB2BB` FOREIGN KEY (`research_project`) REFERENCES `research_project` (`id`),
  CONSTRAINT `FK94FA4B51CCB0C73B` FOREIGN KEY (`publications`) REFERENCES `publication` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `research_subject_code`
--

DROP TABLE IF EXISTS `research_subject_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research_subject_code` (
  `subject_code` varchar(255) NOT NULL,
  `subject_name` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`subject_code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `display_name` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(150) DEFAULT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `givenname` varchar(100) DEFAULT NULL,
  `has_rstudio_account` bit(1) DEFAULT NULL,
  `institution` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `surname` varchar(100) DEFAULT NULL,
  `user_type` varchar(255) DEFAULT NULL,
  `username` varchar(100) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users_roles`
--

DROP TABLE IF EXISTS `users_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users_roles` (
  `users` bigint(20) NOT NULL,
  `roles` bigint(20) NOT NULL,
  PRIMARY KEY (`users`,`roles`),
  KEY `FKF6CCD9C63D50598B` (`users`),
  KEY `FKF6CCD9C63D22EEEB` (`roles`),
  CONSTRAINT `FKF6CCD9C63D22EEEB` FOREIGN KEY (`roles`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKF6CCD9C63D50598B` FOREIGN KEY (`users`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vocabulary`
--

DROP TABLE IF EXISTS `vocabulary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vocabulary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `research_group` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKC8B550E65F7D97C7` (`research_group`),
  CONSTRAINT `FKC8B550E65F7D97C7` FOREIGN KEY (`research_group`) REFERENCES `research_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-09-14 16:51:02
