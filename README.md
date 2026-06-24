# 职策

这是一个用于《Web开发技术》大作业的全栈项目，项目名称为“职策”。系统包含学生端、公司/管理员后台，覆盖注册登录、岗位查询、简历维护、岗位投递、岗位管理、投递审核、AI 求职工具展示和数据统计。

## 技术栈

- 后端：Spring Boot 3、Spring Web、Spring Data JPA、MySQL
- 前端：Vue 3、Vite、原生 CSS
- 数据库：MySQL，建库脚本见 `database/schema.sql`

## 目录结构

```text
backend/   Spring Boot 后端接口服务
frontend/  Vue 前端项目
database/  一体化建表和演示数据 SQL
docs/      大作业文档草稿
```

## 运行步骤

1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：`http://localhost:8087`

MySQL 配置：

- 地址：`localhost:3306`
- 数据库：`campus_career`
- 用户名：`root`
- 密码：`666666`

如需手动初始化数据库，可导入一体化脚本：

```bash
mysql -u root -p < database/schema.sql
```

百炼 AI 配置：

- 环境变量：`DASHSCOPE_API_KEY`
- 默认模型：`qwen3.6-flash`
- 也可在 `backend/src/main/resources/application.yml` 的 `app.bailian.api-key` 中填写密钥

2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 演示账号

- 学生：`student / student123`
- 公司人员：`company / company123`
- 管理员：`admin / admin123`

## 主要功能

- 用户注册、登录、退出
- 岗位列表、关键词搜索、城市筛选、岗位详情
- 学生简历维护
- 简历状态卡、AI 核心工具和更多求职辅助工具展示
- 改简历页面可上传 PDF、DOC、DOCX、TXT 简历文件，并通过后端调用阿里云百炼模型生成修改建议
- 学生岗位投递、查看投递记录、撤回投递
- 公司人员/管理员发布、编辑、删除岗位
- 公司人员查看本企业投递记录，管理员查看全部投递记录
- 公司人员/管理员查看学生数、开放岗位数、投递数、待审核数等统计数据，并通过图表展示状态分布和运营指标
- 管理员维护各页面背景图，默认图片由后端服务器提供，上传图片保存到 `backend/uploads/backgrounds`

## 备注

本项目使用 MySQL 数据库，`database/schema.sql` 已整合建表语句和演示数据。后端首次启动也会自动补充演示账号和示例岗位。若本地 MySQL 账号密码不同，请修改 `backend/src/main/resources/application.yml`。
