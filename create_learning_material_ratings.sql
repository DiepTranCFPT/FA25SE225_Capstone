-- Create learning_material_ratings table with user_id instead of student_id

CREATE TABLE `learning_material_ratings` (
  `id` varchar(255) NOT NULL,
  `comment` text DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `rating` int(11) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `learning_material_id` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_learning_material_user` (`learning_material_id`, `user_id`),
  KEY `FK_learning_material` (`learning_material_id`),
  KEY `FK_user` (`user_id`),
  CONSTRAINT `FK_learning_material_ratings_material` FOREIGN KEY (`learning_material_id`) REFERENCES `learning_materials` (`id`),
  CONSTRAINT `FK_learning_material_ratings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
