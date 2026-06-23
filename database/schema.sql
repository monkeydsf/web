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

CREATE TABLE IF NOT EXISTS company_profiles (
  company_name VARCHAR(120) PRIMARY KEY,
  website VARCHAR(255),
  industry VARCHAR(120),
  size VARCHAR(80),
  address VARCHAR(255),
  description VARCHAR(4000),
  verified TINYINT(1) NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL
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
  file_name VARCHAR(255),
  file_url VARCHAR(1000),
  parsed_text TEXT,
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
  student_read_at DATETIME NULL,
  company_read_at DATETIME NULL,
  interview_at DATETIME NULL,
  interview_location VARCHAR(255),
  interview_note VARCHAR(1000),
  CONSTRAINT fk_applications_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id),
  CONSTRAINT uk_user_job UNIQUE (user_id, job_id)
);

CREATE TABLE IF NOT EXISTS application_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  application_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  content VARCHAR(2000) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_application_messages_application FOREIGN KEY (application_id) REFERENCES job_applications(id),
  CONSTRAINT fk_application_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  recipient_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  title VARCHAR(120) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  related_application_id BIGINT NULL,
  related_job_id BIGINT NULL,
  read_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS job_favorites (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_job_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_job_favorites_job FOREIGN KEY (job_id) REFERENCES jobs(id),
  CONSTRAINT uk_job_favorite UNIQUE (user_id, job_id)
);

CREATE TABLE IF NOT EXISTS job_reports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  job_id BIGINT NOT NULL,
  reporter_id BIGINT NOT NULL,
  reason VARCHAR(120) NOT NULL,
  detail VARCHAR(2000),
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_job_reports_job FOREIGN KEY (job_id) REFERENCES jobs(id),
  CONSTRAINT fk_job_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS interview_schedules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  application_id BIGINT NOT NULL,
  scheduled_at DATETIME NOT NULL,
  location VARCHAR(255),
  note VARCHAR(1000),
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_interview_schedules_application FOREIGN KEY (application_id) REFERENCES job_applications(id)
);

CREATE TABLE IF NOT EXISTS site_backgrounds (
  page_key VARCHAR(40) PRIMARY KEY,
  page_name VARCHAR(120) NOT NULL,
  image_url VARCHAR(1000) NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS contact_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  email VARCHAR(120) NOT NULL,
  subject VARCHAR(120) NOT NULL,
  message VARCHAR(4000) NOT NULL,
  created_at DATETIME NOT NULL
);
