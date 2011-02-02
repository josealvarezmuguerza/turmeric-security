-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.75-0ubuntu10.5


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema turmericdb
--

CREATE DATABASE IF NOT EXISTS turmericdb;
USE turmericdb;

DROP TABLE IF EXISTS `turmericdb`.`ConditionTbl`;
CREATE TABLE  `turmericdb`.`ConditionTbl` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `expression_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKC21C54C3643700EB` (`expression_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`ConditionTbl` (`id`,`createdBy`,`createdOn`,`updatedBy`,`updatedOn`,`expression_id`) VALUES 
 (1,NULL,NULL,NULL,NULL,1),
 (2,NULL,NULL,NULL,NULL,2);

DROP TABLE IF EXISTS `turmericdb`.`Expression`;
CREATE TABLE  `turmericdb`.`Expression` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `comment` varchar(255) default NULL,
  `name` varchar(255) default NULL,
  `primitiveValue_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKBCD6EB8FD60970B` (`primitiveValue_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Expression` (`id`,`createdBy`,`createdOn`,`updatedBy`,`updatedOn`,`comment`,`name`,`primitiveValue_id`) VALUES 
 (1,NULL,NULL,NULL,NULL,'Hits count','Hits',1),
 (2,NULL,NULL,NULL,NULL,'service count','serverCount',2);


DROP TABLE IF EXISTS `turmericdb`.`Operation`;
CREATE TABLE  `turmericdb`.`Operation` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `description` varchar(255) default NULL,
  `operationName` varchar(255) default NULL,
  `resource_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKDA8CF547555CDD6B` (`resource_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Operation` (`id`,`createdBy`,`createdOn`,`updatedBy`,`updatedOn`,`description`,`operationName`,`resource_id`) VALUES 
 (1,NULL,'2010-12-14 10:49:55',NULL,NULL,NULL,'checkout',1),
(2,NULL,'2010-12-14 10:49:55',NULL,NULL,NULL,'commit',2);

DROP TABLE IF EXISTS `turmericdb`.`Policy`;
CREATE TABLE  `turmericdb`.`Policy` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `active` bit(1) NOT NULL,
  `description` varchar(255) default NULL,
  `policyName` varchar(255) default NULL,
  `policyType` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Policy` (`id`,`createdBy`,`createdOn`,`updatedBy`,`updatedOn`,`active`,`description`,`policyName`,`policyType`) VALUES 
 (1,NULL,'2010-12-14 10:12:58',NULL,NULL,0x01,NULL,'FooBar9','BLACKLIST'),
 (2,NULL,'2010-12-14 10:29:01',NULL,NULL,0x01,NULL,'BL','BLACKLIST'),
 (3,NULL,'2010-12-14 16:39:04',NULL,NULL,0x01,NULL,'BL1','BLACKLIST'),
 (4,NULL,'2010-12-14 16:39:04',NULL,NULL,0x01,NULL,'RL1','RL'),
 (5,NULL,'2010-12-14 16:39:04',NULL,NULL,0x01,NULL,'RL2','RL'),
 (6,NULL,'2010-12-14 16:39:04',NULL,NULL,0x01,NULL,'whitelist1','WHITELIST');

DROP TABLE IF EXISTS `turmericdb`.`Policy_Operation`;
CREATE TABLE  `turmericdb`.`Policy_Operation` (
  `Policy_id` bigint(20) NOT NULL,
  `operations_id` bigint(20) NOT NULL,
  KEY `FK6CA0829A4BFA9764` (`operations_id`),
  KEY `FK6CA0829AD5D8832B` (`Policy_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Policy_Operation` (`Policy_id`,`operations_id`) VALUES 
 (1,1),
 (5,2);

DROP TABLE IF EXISTS `turmericdb`.`Policy_Resource`;
CREATE TABLE  `turmericdb`.`Policy_Resource` (
  `Policy_id` bigint(20) NOT NULL,
  `resources_id` bigint(20) NOT NULL,
  KEY `FKCAF2247BF4EC17F4` (`resources_id`),
  KEY `FKCAF2247BD5D8832B` (`Policy_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Policy_Resource` (`Policy_id`,`resources_id`) VALUES 
 (1,1),
 (5,2);
DROP TABLE IF EXISTS `turmericdb`.`Policy_Rule`;
CREATE TABLE  `turmericdb`.`Policy_Rule` (
  `Policy_id` bigint(20) NOT NULL,
  `rules_id` bigint(20) NOT NULL,
  KEY `FKE46F35E941837750` (`rules_id`),
  KEY `FKE46F35E9D5D8832B` (`Policy_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Policy_Rule` (`Policy_id`,`rules_id`) VALUES 
 (1,1),
 (4,1),
 (5,1),
(5,2),
 (6,1);

DROP TABLE IF EXISTS `turmericdb`.`Policy_Subject`;
CREATE TABLE  `turmericdb`.`Policy_Subject` (
  `Policy_id` bigint(20) NOT NULL,
  `subjects_id` bigint(20) NOT NULL,
  KEY `FK5E0FB31FAB44C2E` (`subjects_id`),
  KEY `FK5E0FB31FD5D8832B` (`Policy_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Policy_Subject` (`Policy_id`,`subjects_id`) VALUES 
 (1,1),
 (6,2),
(4,3),
(5,5),
(6,5),
(6,4);


DROP TABLE IF EXISTS `turmericdb`.`Policy_SubjectGroup`;
CREATE TABLE  `turmericdb`.`Policy_SubjectGroup` (
  `Policy_id` bigint(20) NOT NULL,
  `subjectGroups_id` bigint(20) NOT NULL,
  KEY `FKBFCC6EA0AC010E5E` (`subjectGroups_id`),
  KEY `FKBFCC6EA0D5D8832B` (`Policy_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `turmericdb`.`PrimitiveValue`;
CREATE TABLE  `turmericdb`.`PrimitiveValue` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `type` int(11) default NULL,
  `value` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`PrimitiveValue` (`id`,`createdBy`,`createdOn`,`updatedBy`,`updatedOn`,`type`,`value`) VALUES 
 (1,NULL,NULL,NULL,NULL,0,'HITS>5'),
 (2,NULL,NULL,NULL,NULL,0,'PaymentService:commit.count>3');
DROP TABLE IF EXISTS `turmericdb`.`Resource`;
CREATE TABLE  `turmericdb`.`Resource` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `description` varchar(255) default NULL,
  `resourceName` varchar(255) default NULL,
  `resourceType` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Resource` (`id`,`createdBy`,`createdOn`,`updatedBy`,`updatedOn`,`description`,`resourceName`,`resourceType`) VALUES 
 (1,NULL,'2010-12-14 10:49:55',NULL,NULL,'test','testService','SERVICE'),
 (2,NULL,'2010-12-14 11:08:27',NULL,NULL,'desc Payment','PaymentService','SERVICE');

DROP TABLE IF EXISTS `turmericdb`.`Rule`;
CREATE TABLE  `turmericdb`.`Rule` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `description` varchar(255) default NULL,
  `effect` int(11) default NULL,
  `effectDuration` bigint(20) default NULL,
  `priority` int(11) default NULL,
  `rolloverPeriod` bigint(20) default NULL,
  `ruleName` varchar(255) default NULL,
  `condition_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK270B1C6850D789` (`condition_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Rule` (`id`,`createdBy`,`createdOn`,`updatedBy`,`updatedOn`,`description`,`effect`,`effectDuration`,`priority`,`rolloverPeriod`,`ruleName`,`condition_id`) VALUES 
 (1,NULL,NULL,NULL,NULL,'HITS TEST',1,10000,NULL,30000,'rul1',1),
 (2,NULL,NULL,NULL,NULL,'ServiceXXX.OperationXX.count test',3,10000,NULL,30000,'server',2);

DROP TABLE IF EXISTS `turmericdb`.`Subject`;
CREATE TABLE  `turmericdb`.`Subject` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `description` varchar(255) default NULL,
  `emailContact` varchar(255) default NULL,
  `externalSubjectId` bigint(20) NOT NULL,
  `ipMask` varchar(255) default NULL,
  `subjectName` varchar(255) default NULL,
  `subjectType` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
INSERT INTO `turmericdb`.`Subject` (`id`,`createdBy`,`createdOn`,`updatedBy`,`updatedOn`,`description`,`emailContact`,`externalSubjectId`,`ipMask`,`subjectName`,`subjectType`) VALUES 
 (1,NULL,'2010-12-14 10:37:42',NULL,NULL,'IPDESK',NULL,0,NULL,'1.102.96.102','IP'),
 (2,NULL,'2010-12-14 10:37:42',NULL,NULL,'IPDESK',NULL,0,NULL,'1.102.96.103','IP'),
 (3,NULL,'2010-12-14 10:37:42',NULL,NULL,'IPDESK',NULL,0,NULL,'1.102.96.103','IP'),
 (4,NULL,'2010-12-14 10:37:42',NULL,NULL,'IPDESK',NULL,0,NULL,'1.102.96.104','IP'),
 (5,NULL,'2010-12-14 10:37:42',NULL,NULL,'IPDESK',NULL,0,NULL,'1.102.96.105','IP'),
 (6,NULL,'2010-12-14 10:37:42',NULL,NULL,'IPDESK',NULL,0,NULL,'1.102.96.106','IP');
DROP TABLE IF EXISTS `turmericdb`.`SubjectGroup`;
CREATE TABLE  `turmericdb`.`SubjectGroup` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `applyToAll` bit(1) NOT NULL,
  `applyToEach` bit(1) NOT NULL,
  `description` varchar(255) default NULL,
  `subjectGroupCalculator` varchar(255) default NULL,
  `subjectGroupName` varchar(255) default NULL,
  `subjectType` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `turmericdb`.`SubjectGroup_Subject`;
CREATE TABLE  `turmericdb`.`SubjectGroup_Subject` (
  `SubjectGroup_id` bigint(20) NOT NULL,
  `subjects_id` bigint(20) NOT NULL,
  KEY `FKFBFB71A0AB44C2E` (`subjects_id`),
  KEY `FKFBFB71A0B009DAAB` (`SubjectGroup_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `turmericdb`.`SubjectType`;
CREATE TABLE  `turmericdb`.`SubjectType` (
  `id` bigint(20) NOT NULL auto_increment,
  `createdBy` varchar(255) default NULL,
  `createdOn` datetime default NULL,
  `updatedBy` varchar(255) default NULL,
  `updatedOn` datetime default NULL,
  `description` varchar(255) default NULL,
  `external` bit(1) NOT NULL,
  `name` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
