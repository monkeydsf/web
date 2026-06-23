package com.career.service;

import com.career.model.Job;
import com.career.model.JobStatus;
import com.career.model.Role;
import com.career.model.User;
import com.career.repository.JobRepository;
import com.career.repository.SiteBackgroundRepository;
import com.career.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final SiteBackgroundRepository backgroundRepository;
    private final AuthService authService;

    public DataInitializer(UserRepository userRepository,
                           JobRepository jobRepository,
                           SiteBackgroundRepository backgroundRepository,
                           AuthService authService) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.backgroundRepository = backgroundRepository;
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        createUserIfMissing("admin", "admin123", "就业中心管理员", Role.ADMIN, "career-office@school.edu");
        createUserIfMissing("student", "student123", "李明", Role.JOB_SEEKER, "liming@example.com");
        createUserIfMissing("company", "company123", "星途科技招聘专员", Role.COMPANY, "hr@xingtu.example.com");

        initJobs();
        initBackgrounds();
    }

    private void initJobs() {
        List<Job> samples = List.of(
                sampleJob("Java 后端实习生", "星途科技", "杭州", "实习", 180, 260,
                        "参与 Spring Boot 接口开发、数据库表设计和接口联调。",
                        "熟悉 Java 基础、MySQL，了解 RESTful API。"),
                sampleJob("前端开发实习生", "蓝桥数字", "上海", "实习", 160, 240,
                        "负责 Vue3 页面开发、组件封装和后台管理系统交互优化。",
                        "掌握 HTML/CSS/JavaScript，了解 Vue 组件化开发。"),
                sampleJob("软件测试工程师", "远景数科", "苏州", "校招", 6500, 8500,
                        "参与测试用例编写、接口测试和缺陷跟踪，保障系统上线质量。",
                        "逻辑清晰，了解软件测试流程，会使用常见缺陷管理工具。"),
                sampleJob("数据运营助理", "启航教育", "南京", "校招", 5000, 7000,
                        "整理招聘渠道数据，制作周报，协助完成校园招聘活动复盘。",
                        "细心，熟悉 Excel，具备基础数据分析意识。"),
                sampleJob("新媒体运营实习生", "青橙传媒", "杭州", "实习", 120, 180,
                        "负责公众号、小红书内容选题、排版发布和数据复盘。",
                        "文字表达好，关注热点，能独立完成基础内容编辑。"),
                sampleJob("市场活动执行", "云帆互联", "上海", "校招", 6000, 8000,
                        "协助线下活动策划、物料准备、现场执行和活动复盘。",
                        "沟通能力强，执行细致，能接受短期出差或周末活动。"),
                sampleJob("人力资源助理", "禾木咨询", "南京", "实习", 120, 180,
                        "协助简历筛选、面试邀约、员工档案整理和招聘数据维护。",
                        "人力资源、工商管理、心理学等相关专业优先。"),
                sampleJob("财务助理", "华信制造", "苏州", "校招", 5500, 7500,
                        "协助凭证整理、费用报销审核、发票管理和月度数据核对。",
                        "会计、财务管理相关专业，熟悉 Excel，做事严谨。"),
                sampleJob("平面设计实习生", "像素工场", "广州", "实习", 130, 200,
                        "参与海报、活动视觉、社媒配图和品牌物料设计。",
                        "熟悉 Photoshop、Illustrator，有作品集优先。"),
                sampleJob("课程顾问", "启航教育", "成都", "校招", 5000, 9000,
                        "负责学员咨询、课程介绍、学习规划沟通和后续跟进。",
                        "表达清楚，有服务意识，教育学、市场营销专业优先。"),
                sampleJob("供应链管培生", "安捷物流", "武汉", "校招", 6500, 9000,
                        "轮岗学习仓储、运输、计划和供应商协同流程。",
                        "物流管理、工业工程、工商管理相关专业优先。"),
                sampleJob("行政文秘", "城建发展集团", "西安", "校招", 5000, 6500,
                        "负责会议安排、材料整理、文书流转和日常行政支持。",
                        "文字功底好，熟悉 Office，工作细致稳定。"),
                sampleJob("法务助理实习生", "明衡律所", "北京", "实习", 150, 220,
                        "协助合同初审、案例检索、材料归档和法律文书整理。",
                        "法学相关专业，具备基础检索能力和严谨表达。"),
                sampleJob("电商运营专员", "海蓝优选", "广州", "校招", 6000, 8500,
                        "负责商品上架、活动配置、店铺数据分析和用户反馈整理。",
                        "电子商务、市场营销相关专业，熟悉电商平台规则优先。")
        );
        samples.forEach(job -> {
            if (!jobRepository.existsByTitleAndCompany(job.getTitle(), job.getCompany())) {
                jobRepository.save(job);
            }
        });
    }

    private void createUserIfMissing(String username, String password, String fullName, Role role, String email) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(authService.passwordEncoder().encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setEmail(email);
        user.setPhone("13800000000");
        user.setMajor(role == Role.JOB_SEEKER ? "软件工程" : "招聘管理");
        user.setGraduationYear(role == Role.JOB_SEEKER ? 2026 : null);
        userRepository.save(user);
    }

    private Job sampleJob(String title, String company, String city, String type, Integer min, Integer max,
                          String description, String requirement) {
        Job job = new Job();
        job.setTitle(title);
        job.setCompany(company);
        job.setCity(city);
        job.setJobType(type);
        job.setSalaryMin(min);
        job.setSalaryMax(max);
        job.setEducationRequirement("本科及以上");
        job.setDescription(description);
        job.setRequirementText(requirement);
        job.setStatus(JobStatus.OPEN);
        return job;
    }

    private void initBackgrounds() {
        createBackgroundIfMissing("home", "首页", "/backgrounds/home.svg");
        createBackgroundIfMissing("jobs", "职位介绍", "/backgrounds/jobs.svg");
        createBackgroundIfMissing("service", "服务内容", "/backgrounds/service.svg");
        createBackgroundIfMissing("contact", "联系页面", "/backgrounds/contact.svg");
        createBackgroundIfMissing("login", "登录注册", "/backgrounds/login.svg");
        createBackgroundIfMissing("resume", "我的简历", "/backgrounds/resume.svg");
        createBackgroundIfMissing("applications", "我的投递", "/backgrounds/applications.svg");
        createBackgroundIfMissing("admin", "后台管理", "/backgrounds/admin.svg");
    }

    private void createBackgroundIfMissing(String pageKey, String pageName, String imageUrl) {
        if (backgroundRepository.existsById(pageKey)) {
            return;
        }
        com.career.model.SiteBackground background = new com.career.model.SiteBackground();
        background.setPageKey(pageKey);
        background.setPageName(pageName);
        background.setImageUrl(imageUrl);
        backgroundRepository.save(background);
    }
}
