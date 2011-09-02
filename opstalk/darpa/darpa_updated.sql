delimiter $$

CREATE DATABASE `darpadb` /*!40100 DEFAULT CHARACTER SET utf8 */$$

delimiter $$

CREATE TABLE `alert` (
  `alertID` int(11) DEFAULT NULL,
  `userID` int(11) DEFAULT NULL,
  `alert` varchar(500) DEFAULT NULL,
  `priority` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `location` (
  `userID` int(11) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `speed` double DEFAULT NULL,
  `direction` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `message` (
  `userID` int(11) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `message` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `people` (
  `userID` int(11) NOT NULL,
  `pname` varchar(80) DEFAULT NULL,
  `levelID` int(11) DEFAULT NULL,
  PRIMARY KEY (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `person_level` (
  `levelID` int(11) DEFAULT NULL,
  `levelName` varchar(40) DEFAULT NULL,
  `image` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `task` (
  `taskID` int(11) DEFAULT NULL,
  `task` varchar(500) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `priority` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$



