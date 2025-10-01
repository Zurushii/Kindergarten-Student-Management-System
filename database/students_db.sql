CREATE DATABASE IF NOT EXISTS `kindergarten`;
USE `kindergarten`;

CREATE TABLE `students` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `birthdate` date NOT NULL,
  `address` varchar(255) NOT NULL,
  `allergy` varchar(100) DEFAULT NULL,
  `gender` varchar(10),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `students` (`name`, `birthdate`, `address`, `allergy`, `gender`) VALUES
('Nur Zahra Binti Amin','2019-08-17','Shah Alam','Milk','Female'),
('Ahmad Zafran Bin Ali','2020-05-16','Kuala Lumpur','Peanut','Male'),
('Siti Aisyah Binti Zainal','2019-06-28','Johor Bahru','None','Female'),
('Adam Hakim Bin Ismail','2019-03-07','Penang','Egg','Male'),
('Nurul Izzah Binti Rahman','2020-04-21','Ipoh','None','Female'),
('Lim Wei Jun','2019-02-24','Klang','None','Male'),
('Tan Mei Lin','2021-05-09','Subang Jaya','Gluten','Female'),
('Wong Jia Hao','2020-07-23','Petaling Jaya','None','Male'),
('Lee Xin Yi','2019-01-14','Malacca','None','Female'),
('Chong Kai Wen','2019-11-23','Seremban','None','Male'),
('Arun Kumar a/l Ramasamy','2020-11-27','Kuantan','None','Male'),
('Priya a/p Suresh','2020-03-16','Alor Setar','None','Female'),
('Rajesh Kumar a/l Sivalingam','2020-11-20','Kota Bharu','Penicillin','Male'),
('Ashok a/l Narayanan','2019-07-18','Kota Kinabalu','None','Male'),
('Kumar a/l Muniandy','2020-12-05','Sandakan','None','Male'),
('Nur Hidayah Binti Omar','2019-03-30','Tawau','None','Female'),
('Farah Nabila Binti Salleh','2019-09-13','Kangar','None','Female'),
('Syafiq Bin Abdullah','2021-02-14','Bintulu','Milk','Male'),
('Puteri Nadia Binti Mohd','2021-01-10','Miri','None','Female'),
('Haziq Bin Razak','2019-05-23','Sibu','Egg','Male'),
('Nur Aina Binti Iskandar','2020-08-15','Putrajaya','None','Female'),
('Indira Devi a/p Muniandy','2020-04-21','Cyberjaya','None','Female'),
('Chen Li Ting','2021-02-17','Taiping','None','Female'),
('Hoong Jia Ming','2021-01-17','Teluk Intan','Dust','Male'),
('Lim Shu Xuan','2020-03-05','Batu Pahat','None','Female'),
('Balasubramaniam a/l Perumal','2019-05-27','Kuala Terengganu','None','Male'),
('Shanthi a/p Krishnan','2019-12-17','Sungai Petani','None','Female'),
('Fatin Amira Binti Hussein','2020-05-16','Pasir Gudang','Penicillin','Female'),
('Muhammad Danish Bin Saad','2020-07-06','Nilai','None','Male'),
('Ibrahim Bin Hassan','2020-03-25','Manjung','None','Male');