-- phpMyAdmin SQL Dump
-- version 5.0.4
-- https://www.phpmyadmin.net/

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+01:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `djs_db`
--
CREATE DATABASE IF NOT EXISTS `djs_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `djs_db`;

-- --------------------------------------------------------

--
-- Table structure for table `account_demand`
--

CREATE TABLE `account_demand` (
  `demand_id` int(11) NOT NULL,
  `adresse` varchar(250) DEFAULT NULL,
  `agrement` varchar(250) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `description` varchar(250) DEFAULT NULL,
  `name` varchar(80) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `president` varchar(80) DEFAULT NULL,
  `version` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `activity`
--

CREATE TABLE `activity` (
  `activity_id` int(11) NOT NULL,
  `header` text,
  `label` text,
  `minVentilationCount` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `sectionPrototype` varchar(250) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `contract_template` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `principal_id` int(11) NOT NULL,
  `creationDate` varchar(10) NOT NULL,
  `lastLogin` bigint(20) DEFAULT NULL,
  `name` varchar(80) NOT NULL,
  `password` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `role` varchar(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `admin_message`
--

CREATE TABLE `admin_message` (
  `message_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `epoch` bigint(20) NOT NULL,
  `representation` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `adminName` varchar(80) NOT NULL,
  `receiverName` varchar(80) DEFAULT NULL,
  `adminAttachment` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `article`
--

CREATE TABLE `article` (
  `article_id` int(11) NOT NULL,
  `access` int(11) NOT NULL,
  `author` varchar(80) DEFAULT NULL,
  `content_en` text,
  `content_fr` text,
  `created` bigint(20) NOT NULL,
  `lastUpdate` bigint(20) NOT NULL,
  `summary_en` text,
  `summary_fr` text,
  `thumbnails` text,
  `title_en` varchar(250) DEFAULT NULL,
  `title_fr` varchar(250) DEFAULT NULL,
  `version` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `attachment_file`
--

CREATE TABLE `attachment_file` (
  `binary_file_id` int(11) NOT NULL,
  `contentType` varchar(80) NOT NULL,
  `name` varchar(80) NOT NULL,
  `size` int(11) NOT NULL,
  `uploadTime` bigint(20) NOT NULL,
  `uploader` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `contents` mediumblob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `avatar_image`
--

CREATE TABLE `avatar_image` (
  `binary_file_id` int(11) NOT NULL,
  `contentType` varchar(80) NOT NULL,
  `name` varchar(80) NOT NULL,
  `size` int(11) NOT NULL,
  `uploadTime` bigint(20) NOT NULL,
  `uploader` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `contents` mediumblob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `budget`
--

CREATE TABLE `budget` (
  `budget_id` int(11) NOT NULL,
  `budget` decimal(19,2) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `activity` int(11) NOT NULL,
  `contract_instance` int(11) NOT NULL,
  `section` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `comment`
--

CREATE TABLE `comment` (
  `comment_id` int(11) NOT NULL,
  `article_id` int(11) NOT NULL,
  `author` varchar(80) DEFAULT NULL,
  `content` text NOT NULL,
  `lastEdit` bigint(20) DEFAULT NULL,
  `likes` int(11) NOT NULL,
  `posted` bigint(20) NOT NULL,
  `version` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `contract_instance`
--

CREATE TABLE `contract_instance` (
  `contract_instance_id` int(11) NOT NULL,
  `achievement` varchar(250) DEFAULT NULL,
  `achievementLevel` int(11) DEFAULT NULL,
  `assignmentDate` bigint(20) DEFAULT NULL,
  `lastDownload` bigint(20) DEFAULT NULL,
  `lastUpdate` bigint(20) DEFAULT NULL,
  `retrait` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `association` int(11) NOT NULL,
  `contract_template` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `contract_template`
--

CREATE TABLE `contract_template` (
  `contract_template_id` int(11) NOT NULL,
  `creationDate` bigint(20) DEFAULT NULL,
  `downloadDate` bigint(20) DEFAULT NULL,
  `lastUpdate` bigint(20) DEFAULT NULL,
  `lastUpdater` varchar(255) DEFAULT NULL,
  `name` varchar(80) NOT NULL,
  `template` text,
  `version` int(11) DEFAULT NULL,
  `season` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `dossier_file`
--

CREATE TABLE `dossier_file` (
  `binary_file_id` int(11) NOT NULL,
  `contentType` varchar(80) NOT NULL,
  `name` varchar(80) NOT NULL,
  `size` int(11) NOT NULL,
  `uploadTime` bigint(20) NOT NULL,
  `uploader` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `contents` mediumblob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `global_budget`
--

CREATE TABLE `global_budget` (
  `global_budget_id` int(11) NOT NULL,
  `budget` decimal(19,2) NOT NULL,
  `header` text,
  `version` int(11) DEFAULT NULL,
  `activity` int(11) NOT NULL,
  `contract_instance` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `guest_message`
--

CREATE TABLE `guest_message` (
  `message_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `epoch` bigint(20) NOT NULL,
  `representation` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `destination` int(11) NOT NULL,
  `email` varchar(254) DEFAULT NULL,
  `guestName` varchar(80) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `title` varchar(250) DEFAULT NULL,
  `attachment` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `hibernate_sequences`
--

CREATE TABLE `hibernate_sequences` (
  `sequence_name` varchar(255) NOT NULL,
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `image_file`
--

CREATE TABLE `image_file` (
  `binary_file_id` int(11) NOT NULL,
  `contentType` varchar(80) NOT NULL,
  `name` varchar(80) NOT NULL,
  `size` int(11) NOT NULL,
  `uploadTime` bigint(20) NOT NULL,
  `uploader` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `contents` mediumblob NOT NULL,
  `type` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `message`
--

CREATE TABLE `message` (
  `message_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `epoch` bigint(20) NOT NULL,
  `representation` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `destination` int(11) NOT NULL,
  `senderName` varchar(80) NOT NULL,
  `title` varchar(250) DEFAULT NULL,
  `attachment` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `property`
--

CREATE TABLE `property` (
  `property_id` int(11) NOT NULL,
  `access` int(11) NOT NULL,
  `defaultHeader` text,
  `defaultValue` text,
  `index` int(11) NOT NULL,
  `label` varchar(250) DEFAULT NULL,
  `name` varchar(80) NOT NULL,
  `prototype` varchar(250) DEFAULT NULL,
  `required` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `contract_template` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `property_value`
--

CREATE TABLE `property_value` (
  `property_value_id` int(11) NOT NULL,
  `header` text,
  `value` text,
  `version` int(11) DEFAULT NULL,
  `contract_instance` int(11) NOT NULL,
  `property` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `season`
--

CREATE TABLE `season` (
  `season_id` int(11) NOT NULL,
  `index` int(11) NOT NULL,
  `name` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `section`
--

CREATE TABLE `section` (
  `section_id` int(11) NOT NULL,
  `index` int(11) NOT NULL,
  `name` varchar(80) NOT NULL,
  `processingState` int(11) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `sport_association` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `section_template`
--

CREATE TABLE `section_template` (
  `section_template_id` int(11) NOT NULL,
  `name` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `sport_association`
--

CREATE TABLE `sport_association` (
  `principal_id` int(11) NOT NULL,
  `creationDate` varchar(10) NOT NULL,
  `lastLogin` bigint(20) DEFAULT NULL,
  `name` varchar(80) NOT NULL,
  `password` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `adresse` varchar(250) DEFAULT NULL,
  `agence` varchar(250) DEFAULT NULL,
  `agrement` varchar(250) DEFAULT NULL,
  `banque` varchar(250) DEFAULT NULL,
  `compte` varchar(250) DEFAULT NULL,
  `contractsCount` int(11) DEFAULT NULL,
  `description` varchar(250) DEFAULT NULL,
  `downloadedContractsCount` int(11) DEFAULT NULL,
  `email` varchar(250) DEFAULT NULL,
  `lastUpdate` bigint(20) DEFAULT NULL,
  `lastUpdater` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `president` varchar(80) DEFAULT NULL,
  `unreadMessagesCount` int(11) DEFAULT NULL,
  `uploadedFilesCount` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `statistic`
--

CREATE TABLE `statistic` (
  `statistic_id` date NOT NULL,
  `visitorsCount` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `youth_association`
--

CREATE TABLE `youth_association` (
  `principal_id` int(11) NOT NULL,
  `creationDate` varchar(10) NOT NULL,
  `lastLogin` bigint(20) DEFAULT NULL,
  `name` varchar(80) NOT NULL,
  `password` varchar(80) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `adresse` varchar(250) DEFAULT NULL,
  `agence` varchar(250) DEFAULT NULL,
  `agrement` varchar(250) DEFAULT NULL,
  `banque` varchar(250) DEFAULT NULL,
  `compte` varchar(250) DEFAULT NULL,
  `contractsCount` int(11) DEFAULT NULL,
  `description` varchar(250) DEFAULT NULL,
  `downloadedContractsCount` int(11) DEFAULT NULL,
  `email` varchar(250) DEFAULT NULL,
  `lastUpdate` bigint(20) DEFAULT NULL,
  `lastUpdater` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `president` varchar(80) DEFAULT NULL,
  `unreadMessagesCount` int(11) DEFAULT NULL,
  `uploadedFilesCount` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;