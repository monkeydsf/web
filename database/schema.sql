-- MySQL schema for campus_career
CREATE DATABASE IF NOT EXISTS campus_career DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_career;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(40) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  full_name VARCHAR(40) NOT NULL,
  email VARCHAR(80),
  phone VARCHAR(30),
  major VARCHAR(80),
  graduation_year INT,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS resumes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  expected_city VARCHAR(120),
  expected_position VARCHAR(80),
  expected_salary VARCHAR(40),
  education VARCHAR(2000),
  skills VARCHAR(2000),
  project_experience VARCHAR(3000),
  portfolio_url VARCHAR(1000),
  updated_at DATETIME NOT NULL,
  CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS jobs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(120) NOT NULL,
  company VARCHAR(120) NOT NULL,
  city VARCHAR(80) NOT NULL,
  job_type VARCHAR(40),
  salary_min INT,
  salary_max INT,
  education_requirement VARCHAR(80),
  description VARCHAR(3000),
  requirement_text VARCHAR(3000),
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS job_applications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  cover_letter VARCHAR(1000),
  applied_at DATETIME NOT NULL,
  CONSTRAINT fk_applications_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id),
  CONSTRAINT uk_user_job UNIQUE (user_id, job_id)
);

CREATE TABLE IF NOT EXISTS site_backgrounds (
  page_key VARCHAR(40) PRIMARY KEY,
  page_name VARCHAR(120) NOT NULL,
  image_url VARCHAR(1000) NOT NULL,
  updated_at DATETIME NOT NULL
);
