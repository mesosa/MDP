-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Värd: 127.0.0.1
-- Tid vid skapande: 21 mars 2015 kl 22:31
-- Serverversion: 5.6.17
-- PHP-version: 5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Databas: `braincamera`
--

-- --------------------------------------------------------

--
-- Tabellstruktur `activity`
--

CREATE TABLE IF NOT EXISTS `activity` (
  `Id` varchar(40) DEFAULT NULL,
  `Datetime` datetime NOT NULL,
  `Value` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumpning av Data i tabell `activity`
--

INSERT INTO `activity` (`Id`, `Datetime`, `Value`) VALUES
('1', '2015-03-20 15:47:37', 0.25),
('1', '2015-03-20 15:47:39', 0.35),
('1', '2015-03-20 15:47:42', 0.35),
('1', '2015-03-20 15:47:43', 0.42),
('1', '2015-03-20 15:47:44', 0.54),
('1', '2015-03-20 15:47:45', 0.62),
('1', '2015-03-20 15:47:47', 0.83),
('1', '2015-03-20 15:47:46', 0.78),
('1', '2015-03-20 15:47:48', 0.85),
('1', '2015-03-20 15:47:49', 0.42),
('1', '2015-03-20 15:47:50', 0.31),
('1', '2015-03-20 15:47:51', 0.15),
('2', '2015-03-21 19:30:51', 0.8),
('2', '2015-03-21 19:31:11', 0.8),
('2', '2015-03-21 19:31:34', 0.8),
('1', '2015-03-20 15:47:52', 0.68);

-- --------------------------------------------------------

--
-- Tabellstruktur `images`
--

CREATE TABLE IF NOT EXISTS `images` (
  `Id` varchar(40) NOT NULL,
  `Url` varchar(160) NOT NULL,
  `Latitude` varchar(20) NOT NULL,
  `Longitude` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumpning av Data i tabell `images`
--

INSERT INTO `images` (`Id`, `Url`, `Latitude`, `Longitude`) VALUES
('1', 'images/20150320154747.jpg', '55.615372', '12.9853');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
