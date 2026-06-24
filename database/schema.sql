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

INSERT INTO users (username, password_hash, role, full_name, email, phone, major, graduation_year, created_at)
SELECT 'admin', '$2a$10$8B2f/pmmrCkHsC8tEbJMlemmd2Oq3bPIvvrJX2YzBheiSX1s0i8be', 'ADMIN', '就业中心管理员', 'career-office@school.edu', '13800000000', '就业服务中心', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password_hash, role, full_name, email, phone, major, graduation_year, created_at)
SELECT 'student', '$2a$10$l10hxQjvzISegauruUtbu.MG3XKw/bV/I9STdEcnKCLLo/uwGv5Y6', 'JOB_SEEKER', '李明', 'liming@example.com', '13800000001', '软件工程', 2026, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'student');

INSERT INTO users (username, password_hash, role, full_name, email, phone, major, graduation_year, created_at)
SELECT 'company', '$2a$10$IaQGfwUKN3wSOKbf5tAbOOjFm0o3IPktA.2lxxkSdNAjIr1utqxaa', 'COMPANY', '星途科技招聘专员', 'hr@xingtu.example.com', '13800000002', '星途科技', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'company');

INSERT INTO users (username, password_hash, role, full_name, email, phone, major, graduation_year, created_at)
SELECT 'student2', '$2a$10$l10hxQjvzISegauruUtbu.MG3XKw/bV/I9STdEcnKCLLo/uwGv5Y6', 'JOB_SEEKER', '王雨晴', 'wangyuqing@example.com', '13800000003', '数据科学与大数据技术', 2025, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'student2');

INSERT INTO users (username, password_hash, role, full_name, email, phone, major, graduation_year, created_at)
SELECT 'student3', '$2a$10$l10hxQjvzISegauruUtbu.MG3XKw/bV/I9STdEcnKCLLo/uwGv5Y6', 'JOB_SEEKER', '陈思远', 'chensiyuan@example.com', '13800000004', '市场营销', 2026, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'student3');

INSERT INTO users (username, password_hash, role, full_name, email, phone, major, graduation_year, created_at)
SELECT 'company2', '$2a$10$IaQGfwUKN3wSOKbf5tAbOOjFm0o3IPktA.2lxxkSdNAjIr1utqxaa', 'COMPANY', '蓝桥数字招聘专员', 'hr@lanqiao.example.com', '13800000005', '蓝桥数字', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'company2');

INSERT INTO company_profiles (company_name, website, industry, size, address, description, verified, updated_at)
SELECT '星途科技', 'https://xingtu.example.com', '互联网软件', '100-499人', '杭州市滨江区网商路88号', '专注企业协同和校园招聘数字化服务，提供后端、前端、测试等实习岗位。', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM company_profiles WHERE company_name = '星途科技');

INSERT INTO company_profiles (company_name, website, industry, size, address, description, verified, updated_at)
SELECT '蓝桥数字', 'https://lanqiao.example.com', '数字化服务', '50-99人', '上海市杨浦区国定路66号', '面向中小企业提供前端开发和数据产品解决方案。', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM company_profiles WHERE company_name = '蓝桥数字');

INSERT INTO company_profiles (company_name, website, industry, size, address, description, verified, updated_at)
SELECT '启航教育', 'https://qihang.example.com', '教育培训', '500人以上', '南京市鼓楼区中央路100号', '提供课程咨询、教务运营和数据运营岗位，适合教育行业方向学生。', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM company_profiles WHERE company_name = '启航教育');

INSERT INTO resumes (user_id, expected_city, expected_position, expected_salary, education, skills, project_experience, portfolio_url, file_name, file_url, parsed_text, updated_at)
SELECT u.id, '杭州 / 上海', 'Java 后端实习生', '180-260/天', '软件工程本科，主修 Java 程序设计、数据库系统、Web 开发技术。', 'Java、Spring Boot、MySQL、Vue3、RESTful API、Git', '校园求职系统：实现登录、岗位管理、投递、消息通知和后台统计模块。', 'https://github.com/demo/campus-career', 'liming-resume.pdf', '/uploads/resumes/liming-resume.pdf', '李明，软件工程，熟悉 Java Spring Boot MySQL Vue，参与校园求职系统项目。', NOW()
FROM users u
WHERE u.username = 'student'
  AND NOT EXISTS (SELECT 1 FROM resumes r WHERE r.user_id = u.id);

INSERT INTO resumes (user_id, expected_city, expected_position, expected_salary, education, skills, project_experience, portfolio_url, file_name, file_url, parsed_text, updated_at)
SELECT u.id, '上海 / 南京', '数据运营助理', '6000-8000/月', '数据科学与大数据技术本科，掌握统计分析和数据可视化。', 'Excel、SQL、Python、数据分析、报表制作', '校园活动数据分析：清洗报名数据并制作转化漏斗报表。', '', 'wangyuqing-resume.docx', '/uploads/resumes/wangyuqing-resume.docx', '王雨晴，数据科学，熟悉 SQL Python Excel 数据分析。', NOW()
FROM users u
WHERE u.username = 'student2'
  AND NOT EXISTS (SELECT 1 FROM resumes r WHERE r.user_id = u.id);

INSERT INTO resumes (user_id, expected_city, expected_position, expected_salary, education, skills, project_experience, portfolio_url, file_name, file_url, parsed_text, updated_at)
SELECT u.id, '杭州 / 广州', '新媒体运营实习生', '120-180/天', '市场营销本科，参与校园社团宣传和活动策划。', '文案写作、活动策划、公众号排版、用户调研', '校园招聘公众号运营：完成选题、推文排版和数据复盘。', '', NULL, NULL, '陈思远，市场营销，擅长文案、活动策划和新媒体运营。', NOW()
FROM users u
WHERE u.username = 'student3'
  AND NOT EXISTS (SELECT 1 FROM resumes r WHERE r.user_id = u.id);

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT 'Java 后端实习生', '星途科技', '杭州', '实习', 180, 260, '本科及以上', '参与 Spring Boot 接口开发、数据库表设计和接口联调。', '熟悉 Java 基础、MySQL，了解 RESTful API。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Java 后端实习生' AND company = '星途科技');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '前端开发实习生', '蓝桥数字', '上海', '实习', 160, 240, '本科及以上', '负责 Vue3 页面开发、组件封装和后台管理系统交互优化。', '掌握 HTML/CSS/JavaScript，了解 Vue 组件化开发。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '前端开发实习生' AND company = '蓝桥数字');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '软件测试工程师', '远景数科', '苏州', '校招', 6500, 8500, '本科及以上', '参与测试用例编写、接口测试和缺陷跟踪，保障系统上线质量。', '逻辑清晰，了解软件测试流程，会使用常见缺陷管理工具。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '软件测试工程师' AND company = '远景数科');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '数据运营助理', '启航教育', '南京', '校招', 5000, 7000, '本科及以上', '整理招聘渠道数据，制作周报，协助完成校园招聘活动复盘。', '细心，熟悉 Excel，具备基础数据分析意识。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '数据运营助理' AND company = '启航教育');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '新媒体运营实习生', '青橙传媒', '杭州', '实习', 120, 180, '本科及以上', '负责公众号、小红书内容选题、排版发布和数据复盘。', '文字表达好，关注热点，能独立完成基础内容编辑。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '新媒体运营实习生' AND company = '青橙传媒');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '市场活动执行', '云帆互联', '上海', '校招', 6000, 8000, '本科及以上', '协助线下活动策划、物料准备、现场执行和活动复盘。', '沟通能力强，执行细致，能接受短期出差或周末活动。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '市场活动执行' AND company = '云帆互联');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '人力资源助理', '禾木咨询', '南京', '实习', 120, 180, '本科及以上', '协助简历筛选、面试邀约、员工档案整理和招聘数据维护。', '人力资源、工商管理、心理学等相关专业优先。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '人力资源助理' AND company = '禾木咨询');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '财务助理', '华信制造', '苏州', '校招', 5500, 7500, '本科及以上', '协助凭证整理、费用报销审核、发票管理和月度数据核对。', '会计、财务管理相关专业，熟悉 Excel，做事严谨。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '财务助理' AND company = '华信制造');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '平面设计实习生', '像素工场', '广州', '实习', 130, 200, '本科及以上', '参与海报、活动视觉、社媒配图和品牌物料设计。', '熟悉 Photoshop、Illustrator，有作品集优先。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '平面设计实习生' AND company = '像素工场');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '课程顾问', '启航教育', '成都', '校招', 5000, 9000, '本科及以上', '负责学员咨询、课程介绍、学习规划沟通和后续跟进。', '表达清楚，有服务意识，教育学、市场营销专业优先。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '课程顾问' AND company = '启航教育');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '供应链管培生', '安捷物流', '武汉', '校招', 6500, 9000, '本科及以上', '轮岗学习仓储、运输、计划和供应商协同流程。', '物流管理、工业工程、工商管理相关专业优先。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '供应链管培生' AND company = '安捷物流');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '行政文秘', '城建发展集团', '西安', '校招', 5000, 6500, '本科及以上', '负责会议安排、材料整理、文书流转和日常行政支持。', '文字功底好，熟悉 Office，工作细致稳定。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '行政文秘' AND company = '城建发展集团');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '法务助理实习生', '明衡律所', '北京', '实习', 150, 220, '本科及以上', '协助合同初审、案例检索、材料归档和法律文书整理。', '法学相关专业，具备基础检索能力和严谨表达。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '法务助理实习生' AND company = '明衡律所');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '电商运营专员', '海蓝优选', '广州', '校招', 6000, 8500, '本科及以上', '负责商品上架、活动配置、店铺数据分析和用户反馈整理。', '电子商务、市场营销相关专业，熟悉电商平台规则优先。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '电商运营专员' AND company = '海蓝优选');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '后端开发工程师', '星途科技', '杭州', '校招', 9000, 13000, '本科及以上', '参与校园招聘 SaaS 平台核心业务开发，负责接口设计和性能优化。', '熟悉 Java、Spring Boot、Redis、MySQL，有课程项目或实习经历优先。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '后端开发工程师' AND company = '星途科技');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '产品助理实习生', '蓝桥数字', '上海', '实习', 150, 220, '本科及以上', '协助需求整理、竞品分析、原型验收和项目跟进。', '表达清楚，逻辑严谨，熟悉 Axure、墨刀或 Figma 优先。', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '产品助理实习生' AND company = '蓝桥数字');

INSERT INTO jobs (title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT '已关闭测试岗位', '星途科技', '杭州', '实习', 100, 120, '本科及以上', '用于演示岗位关闭状态。', '该岗位已停止招聘。', 'CLOSED', NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '已关闭测试岗位' AND company = '星途科技');

INSERT INTO job_applications (user_id, job_id, status, cover_letter, applied_at, student_read_at, company_read_at, interview_at, interview_location, interview_note)
SELECT u.id, j.id, 'PENDING', '您好，我熟悉 Java、Spring Boot 和 MySQL，希望参与贵公司的后端实习项目。', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL, NULL, NULL, NULL, NULL
FROM users u, jobs j
WHERE u.username = 'student'
  AND j.title = 'Java 后端实习生'
  AND j.company = '星途科技'
  AND NOT EXISTS (SELECT 1 FROM job_applications a WHERE a.user_id = u.id AND a.job_id = j.id);

INSERT INTO job_applications (user_id, job_id, status, cover_letter, applied_at, student_read_at, company_read_at, interview_at, interview_location, interview_note)
SELECT u.id, j.id, 'REVIEWED', '我做过 Vue3 课程项目，希望投递前端实习岗位。', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL, NULL
FROM users u, jobs j
WHERE u.username = 'student'
  AND j.title = '前端开发实习生'
  AND j.company = '蓝桥数字'
  AND NOT EXISTS (SELECT 1 FROM job_applications a WHERE a.user_id = u.id AND a.job_id = j.id);

INSERT INTO job_applications (user_id, job_id, status, cover_letter, applied_at, student_read_at, company_read_at, interview_at, interview_location, interview_note)
SELECT u.id, j.id, 'ACCEPTED', '我具备 SQL 和 Excel 数据分析能力，希望参与招聘数据运营工作。', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), '腾讯会议 665-778-990', '请提前准备一段数据分析项目介绍。'
FROM users u, jobs j
WHERE u.username = 'student2'
  AND j.title = '数据运营助理'
  AND j.company = '启航教育'
  AND NOT EXISTS (SELECT 1 FROM job_applications a WHERE a.user_id = u.id AND a.job_id = j.id);

INSERT INTO job_applications (user_id, job_id, status, cover_letter, applied_at, student_read_at, company_read_at, interview_at, interview_location, interview_note)
SELECT u.id, j.id, 'REJECTED', '我有校园公众号运营经历，想申请新媒体运营实习。', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), NULL, NULL, NULL
FROM users u, jobs j
WHERE u.username = 'student3'
  AND j.title = '新媒体运营实习生'
  AND j.company = '青橙传媒'
  AND NOT EXISTS (SELECT 1 FROM job_applications a WHERE a.user_id = u.id AND a.job_id = j.id);

INSERT INTO job_applications (user_id, job_id, status, cover_letter, applied_at, student_read_at, company_read_at, interview_at, interview_location, interview_note)
SELECT u.id, j.id, 'PENDING', '我希望投递星途科技校招后端岗位，具备完整 Web 项目经验。', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, NULL, NULL, NULL, NULL
FROM users u, jobs j
WHERE u.username = 'student2'
  AND j.title = '后端开发工程师'
  AND j.company = '星途科技'
  AND NOT EXISTS (SELECT 1 FROM job_applications a WHERE a.user_id = u.id AND a.job_id = j.id);

INSERT INTO application_messages (application_id, sender_id, content, created_at)
SELECT a.id, s.id, '您好，我已投递该岗位，想进一步了解实习周期和技术栈要求。', DATE_SUB(NOW(), INTERVAL 4 DAY)
FROM job_applications a
JOIN users s ON s.username = 'student'
JOIN jobs j ON j.id = a.job_id
WHERE a.user_id = s.id AND j.title = 'Java 后端实习生' AND j.company = '星途科技'
  AND NOT EXISTS (SELECT 1 FROM application_messages m WHERE m.application_id = a.id AND m.content = '您好，我已投递该岗位，想进一步了解实习周期和技术栈要求。');

INSERT INTO application_messages (application_id, sender_id, content, created_at)
SELECT a.id, c.id, '你好，实习周期建议三个月以上，技术栈主要是 Spring Boot、MySQL 和 Vue。', DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM job_applications a
JOIN users c ON c.username = 'company'
JOIN jobs j ON j.id = a.job_id
WHERE j.title = 'Java 后端实习生' AND j.company = '星途科技'
  AND NOT EXISTS (SELECT 1 FROM application_messages m WHERE m.application_id = a.id AND m.content = '你好，实习周期建议三个月以上，技术栈主要是 Spring Boot、MySQL 和 Vue。');

INSERT INTO interview_schedules (application_id, scheduled_at, location, note, status, created_at)
SELECT a.id, DATE_ADD(NOW(), INTERVAL 2 DAY), '腾讯会议 665-778-990', '请提前准备一段数据分析项目介绍。', 'CONFIRMED', NOW()
FROM job_applications a
JOIN jobs j ON j.id = a.job_id
JOIN users u ON u.id = a.user_id
WHERE u.username = 'student2' AND j.title = '数据运营助理' AND j.company = '启航教育'
  AND NOT EXISTS (SELECT 1 FROM interview_schedules i WHERE i.application_id = a.id);

INSERT INTO notifications (recipient_id, type, title, content, related_application_id, related_job_id, read_at, created_at)
SELECT u.id, 'SYSTEM', '欢迎使用职策', '完善简历后可以获得更准确的岗位匹配建议。', NULL, NULL, NULL, NOW()
FROM users u
WHERE u.username IN ('student', 'student2', 'student3')
  AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.recipient_id = u.id AND n.title = '欢迎使用职策');

INSERT INTO notifications (recipient_id, type, title, content, related_application_id, related_job_id, read_at, created_at)
SELECT c.id, 'APPLICATION', '收到新投递', '李明投递了 Java 后端实习生。', a.id, j.id, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM users c, users s, jobs j, job_applications a
WHERE c.username = 'company' AND s.username = 'student' AND j.title = 'Java 后端实习生' AND j.company = '星途科技'
  AND a.user_id = s.id AND a.job_id = j.id
  AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.recipient_id = c.id AND n.title = '收到新投递' AND n.related_application_id = a.id);

INSERT INTO notifications (recipient_id, type, title, content, related_application_id, related_job_id, read_at, created_at)
SELECT s.id, 'INTERVIEW', '面试安排', '你有一场 数据运营助理 的面试安排。', a.id, j.id, NULL, NOW()
FROM users s, jobs j, job_applications a
WHERE s.username = 'student2' AND j.title = '数据运营助理' AND j.company = '启航教育'
  AND a.user_id = s.id AND a.job_id = j.id
  AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.recipient_id = s.id AND n.title = '面试安排' AND n.related_application_id = a.id);

INSERT INTO job_favorites (user_id, job_id, created_at)
SELECT u.id, j.id, DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM users u, jobs j
WHERE u.username = 'student' AND j.title IN ('Java 后端实习生', '后端开发工程师')
  AND j.company = '星途科技'
  AND NOT EXISTS (SELECT 1 FROM job_favorites f WHERE f.user_id = u.id AND f.job_id = j.id);

INSERT INTO job_favorites (user_id, job_id, created_at)
SELECT u.id, j.id, NOW()
FROM users u, jobs j
WHERE u.username = 'student2' AND j.title IN ('数据运营助理', '产品助理实习生')
  AND NOT EXISTS (SELECT 1 FROM job_favorites f WHERE f.user_id = u.id AND f.job_id = j.id);

INSERT INTO job_reports (job_id, reporter_id, reason, detail, status, created_at)
SELECT j.id, u.id, '信息需要核实', '岗位描述中薪资范围较宽，建议管理员核实。', 'OPEN', DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM jobs j, users u
WHERE j.title = '市场活动执行' AND j.company = '云帆互联' AND u.username = 'student'
  AND NOT EXISTS (SELECT 1 FROM job_reports r WHERE r.job_id = j.id AND r.reporter_id = u.id);

INSERT INTO contact_messages (name, email, subject, message, created_at)
SELECT '李明', 'liming@example.com', '希望增加更多 Java 岗位', '建议平台增加更多杭州地区后端实习岗位。', NOW()
WHERE NOT EXISTS (SELECT 1 FROM contact_messages WHERE email = 'liming@example.com' AND subject = '希望增加更多 Java 岗位');

INSERT INTO site_backgrounds (page_key, page_name, image_url, updated_at)
SELECT 'home', '首页', '/backgrounds/home.svg', NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_backgrounds WHERE page_key = 'home');

INSERT INTO site_backgrounds (page_key, page_name, image_url, updated_at)
SELECT 'jobs', '职位介绍', '/backgrounds/jobs.svg', NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_backgrounds WHERE page_key = 'jobs');

INSERT INTO site_backgrounds (page_key, page_name, image_url, updated_at)
SELECT 'service', '服务内容', '/backgrounds/service.svg', NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_backgrounds WHERE page_key = 'service');

INSERT INTO site_backgrounds (page_key, page_name, image_url, updated_at)
SELECT 'contact', '联系页面', '/backgrounds/contact.svg', NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_backgrounds WHERE page_key = 'contact');

INSERT INTO site_backgrounds (page_key, page_name, image_url, updated_at)
SELECT 'login', '登录注册', '/backgrounds/login.svg', NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_backgrounds WHERE page_key = 'login');

INSERT INTO site_backgrounds (page_key, page_name, image_url, updated_at)
SELECT 'resume', '我的简历', '/backgrounds/resume.svg', NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_backgrounds WHERE page_key = 'resume');

INSERT INTO site_backgrounds (page_key, page_name, image_url, updated_at)
SELECT 'applications', '我的投递', '/backgrounds/applications.svg', NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_backgrounds WHERE page_key = 'applications');

INSERT INTO site_backgrounds (page_key, page_name, image_url, updated_at)
SELECT 'admin', '后台管理', '/backgrounds/admin.svg', NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_backgrounds WHERE page_key = 'admin');
