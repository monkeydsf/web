-- Supplemental SQL for campus_career.
-- Adds resume file fields, application chat messages, and richer multi-major demo jobs.

CREATE DATABASE IF NOT EXISTS campus_career DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_career;

SET @schema_name = DATABASE();

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE resumes ADD COLUMN file_name VARCHAR(255)',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'resumes'
    AND column_name = 'file_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE resumes ADD COLUMN file_url VARCHAR(1000)',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'resumes'
    AND column_name = 'file_url'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS application_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  application_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  content VARCHAR(2000) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_application_messages_application
    FOREIGN KEY (application_id) REFERENCES job_applications(id),
  CONSTRAINT fk_application_messages_sender
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT 'Java 后端实习生', '星途科技', '杭州', '实习', 180, 260, '本科及以上',
         '参与 Spring Boot 接口开发、数据库表设计和接口联调。',
         '熟悉 Java 基础、MySQL，了解 RESTful API。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Java 后端实习生' AND company = '星途科技');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '前端开发实习生', '蓝桥数字', '上海', '实习', 160, 240, '本科及以上',
         '负责 Vue3 页面开发、组件封装和后台管理系统交互优化。',
         '掌握 HTML/CSS/JavaScript，了解 Vue 组件化开发。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '前端开发实习生' AND company = '蓝桥数字');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '软件测试工程师', '远景数科', '苏州', '校招', 6500, 8500, '本科及以上',
         '参与测试用例编写、接口测试和缺陷跟踪，保障系统上线质量。',
         '逻辑清晰，了解软件测试流程，会使用常见缺陷管理工具。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '软件测试工程师' AND company = '远景数科');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '数据运营助理', '启航教育', '南京', '校招', 5000, 7000, '本科及以上',
         '整理招聘渠道数据，制作周报，协助完成校园招聘活动复盘。',
         '细心，熟悉 Excel，具备基础数据分析意识。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '数据运营助理' AND company = '启航教育');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '新媒体运营实习生', '青橙传媒', '杭州', '实习', 120, 180, '本科及以上',
         '负责公众号、小红书内容选题、排版发布和数据复盘。',
         '文字表达好，关注热点，能独立完成基础内容编辑。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '新媒体运营实习生' AND company = '青橙传媒');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '市场活动执行', '云帆互联', '上海', '校招', 6000, 8000, '本科及以上',
         '协助线下活动策划、物料准备、现场执行和活动复盘。',
         '沟通能力强，执行细致，能接受短期出差或周末活动。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '市场活动执行' AND company = '云帆互联');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '人力资源助理', '禾木咨询', '南京', '实习', 120, 180, '本科及以上',
         '协助简历筛选、面试邀约、员工档案整理和招聘数据维护。',
         '人力资源、工商管理、心理学等相关专业优先。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '人力资源助理' AND company = '禾木咨询');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '财务助理', '华信制造', '苏州', '校招', 5500, 7500, '本科及以上',
         '协助凭证整理、费用报销审核、发票管理和月度数据核对。',
         '会计、财务管理相关专业，熟悉 Excel，做事严谨。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '财务助理' AND company = '华信制造');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '平面设计实习生', '像素工场', '广州', '实习', 130, 200, '本科及以上',
         '参与海报、活动视觉、社媒配图和品牌物料设计。',
         '熟悉 Photoshop、Illustrator，有作品集优先。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '平面设计实习生' AND company = '像素工场');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '课程顾问', '启航教育', '成都', '校招', 5000, 9000, '本科及以上',
         '负责学员咨询、课程介绍、学习规划沟通和后续跟进。',
         '表达清楚，有服务意识，教育学、市场营销专业优先。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '课程顾问' AND company = '启航教育');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '供应链管培生', '安捷物流', '武汉', '校招', 6500, 9000, '本科及以上',
         '轮岗学习仓储、运输、计划和供应商协同流程。',
         '物流管理、工业工程、工商管理相关专业优先。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '供应链管培生' AND company = '安捷物流');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '行政文秘', '城建发展集团', '西安', '校招', 5000, 6500, '本科及以上',
         '负责会议安排、材料整理、文书流转和日常行政支持。',
         '文字功底好，熟悉 Office，工作细致稳定。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '行政文秘' AND company = '城建发展集团');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '法务助理实习生', '明衡律所', '北京', '实习', 150, 220, '本科及以上',
         '协助合同初审、案例检索、材料归档和法律文书整理。',
         '法学相关专业，具备基础检索能力和严谨表达。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '法务助理实习生' AND company = '明衡律所');

INSERT INTO jobs
(title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at)
SELECT * FROM (
  SELECT '电商运营专员', '海蓝优选', '广州', '校招', 6000, 8500, '本科及以上',
         '负责商品上架、活动配置、店铺数据分析和用户反馈整理。',
         '电子商务、市场营销相关专业，熟悉电商平台规则优先。', 'OPEN', NOW()
) AS row_data
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = '电商运营专员' AND company = '海蓝优选');
