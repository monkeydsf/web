<script setup>
import { computed, ref } from 'vue'
import { renderMarkdown } from '../utils/markdown'

const docs = [
  {
    id: 'java-roadmap',
    category: '后端开发',
    level: '入门到项目',
    title: 'Java 后端学习路线',
    desc: '适合准备 Java 后端实习和校招的学习顺序。',
    minutes: 12,
    content: `# Java 后端学习路线

## 1. Java 基础

- 熟悉基本语法、集合、异常、IO、泛型
- 重点理解面向对象、接口、继承、多态
- 会使用 Stream、Lambda 和常见工具类

## 2. 数据库与 SQL

- 掌握 MySQL 表设计、索引、事务
- 能写增删改查、分页、条件查询、聚合统计
- 理解慢查询、联合索引、唯一约束

## 3. Spring Boot

- 会写 Controller、Service、Repository 分层
- 掌握参数校验、异常处理、配置文件
- 会接 MySQL，完成登录、列表、详情、状态更新等业务

## 4. 项目训练

建议做一个完整项目，例如校园招聘、图书管理、在线学习平台。

项目至少包含：

- 登录注册
- 权限角色
- 数据增删改查
- 文件上传
- 状态流转
- 后台管理

## 5. 面试准备

重点准备 Java 集合、JVM 基础、MySQL 索引、Spring IOC/AOP、事务、项目亮点。`
  },
  {
    id: 'vue-basic',
    category: '前端开发',
    level: '基础',
    title: 'Vue 3 入门笔记',
    desc: '从组件、路由、接口请求到页面状态管理。',
    minutes: 10,
    content: `# Vue 3 入门笔记

## 组件结构

Vue 单文件组件通常由三部分组成：

- \`<script setup>\`：写数据、方法、接口调用
- \`<template>\`：写页面结构
- \`<style>\`：写组件样式

## 常用 API

- \`ref()\`：保存简单响应式数据
- \`reactive()\`：保存对象表单
- \`computed()\`：根据状态计算新值
- \`onMounted()\`：页面加载后请求数据

## 路由

用 \`vue-router\` 管理页面跳转：

- 首页：\`/\`
- 职位：\`/jobs\`
- 简历：\`/resume\`
- 学习中心：\`/learn\`

## 接口请求

把请求统一封装到 \`api.js\`，好处是：

- 统一加 Token
- 统一处理超时
- 统一处理 401 退出登录

## 练习建议

先写列表页，再写详情页，最后写表单提交和错误提示。`
  },
  {
    id: 'mysql-index',
    category: '数据库',
    level: '核心基础',
    title: 'MySQL 索引和事务',
    desc: '后端面试里最常见的数据库基础。',
    minutes: 8,
    content: `# MySQL 索引和事务

## 索引是什么

索引用来提高查询速度，可以理解为给数据建立目录。

常见索引：

- 主键索引
- 唯一索引
- 普通索引
- 联合索引

## 联合索引原则

联合索引遵循最左前缀原则。

例如索引是 \`(city, title, status)\`：

- 可以命中：\`city = ?\`
- 可以命中：\`city = ? and title = ?\`
- 不一定命中：\`title = ?\`

## 事务 ACID

- 原子性：要么都成功，要么都失败
- 一致性：数据从一个正确状态到另一个正确状态
- 隔离性：多个事务互不干扰
- 持久性：提交后数据不会丢失

## 项目里怎么用

涉及投递、支付、库存、状态更新时，通常需要事务保证数据一致。`
  },
  {
    id: 'springboot-project',
    category: '后端开发',
    level: '项目实战',
    title: 'Spring Boot 项目分层',
    desc: '理解 Controller、Service、Repository 怎么配合。',
    minutes: 9,
    content: `# Spring Boot 项目分层

## Controller

负责接收前端请求，处理参数，返回结果。

常见写法：

\`\`\`java
@GetMapping("/jobs")
public List<JobDto> list() {
    return jobService.list();
}
\`\`\`

## Service

负责业务逻辑，例如：

- 判断是否登录
- 判断是否重复投递
- 修改投递状态
- 调用第三方 AI 接口

## Repository

负责数据库访问，通常继承 \`JpaRepository\`。

## DTO

DTO 是给前端返回的数据结构，避免直接暴露数据库实体。

## 推荐分层

Controller 只做入口，Service 写业务，Repository 查数据库，DTO 负责输出。`
  },
  {
    id: 'project-resume',
    category: '求职能力',
    level: '简历表达',
    title: '项目经历怎么写进简历',
    desc: '把课程项目写成更像真实业务项目的表达。',
    minutes: 7,
    content: `# 项目经历怎么写进简历

## 推荐结构

一个项目经历可以按这个顺序写：

1. 项目背景
2. 技术栈
3. 负责模块
4. 关键难点
5. 结果或收获

## 不推荐写法

- 参与项目开发
- 完成前后端页面
- 实现基本功能

这些表达太空，面试官看不出你的具体贡献。

## 推荐写法

- 负责岗位投递模块，完成投递去重、状态流转和撤回逻辑
- 封装统一请求方法，处理 Token、超时和 401 自动退出
- 接入文件上传，支持 PDF/Word 简历保存和静态资源访问

## 核心原则

写清楚你做了什么、怎么做、解决了什么问题。`
  },
  {
    id: 'interview-basic',
    category: '求职能力',
    level: '面试准备',
    title: '技术面试准备清单',
    desc: '面试前按清单复习，避免只背零散八股。',
    minutes: 6,
    content: `# 技术面试准备清单

## 自我介绍

控制在 1 分钟左右，包含：

- 学校和专业
- 技术方向
- 一个最熟悉的项目
- 想投递的岗位

## 项目介绍

准备 3 个问题：

- 你负责了哪些模块？
- 项目里最难的点是什么？
- 如果继续优化，你会改哪里？

## 基础知识

后端重点：

- Java 集合
- MySQL 索引
- Spring Boot
- HTTP 和接口设计

前端重点：

- Vue 响应式
- 组件通信
- 路由
- 接口请求和状态处理

## 反问问题

可以问：

- 实习生主要负责哪些模块？
- 团队技术栈是什么？
- 是否有导师或代码评审？`
  }
]

const activeId = ref(docs[0].id)
const activeCategory = ref('全部')
const keyword = ref('')

const categories = computed(() => ['全部', ...new Set(docs.map((doc) => doc.category))])
const filteredDocs = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return docs.filter((doc) => {
    const matchesCategory = activeCategory.value === '全部' || doc.category === activeCategory.value
    const matchesKeyword = !text || `${doc.title}${doc.desc}${doc.category}`.toLowerCase().includes(text)
    return matchesCategory && matchesKeyword
  })
})
const activeDoc = computed(() => docs.find((doc) => doc.id === activeId.value) || filteredDocs.value[0] || docs[0])

function selectCategory(category) {
  activeCategory.value = category
  const first = filteredDocs.value[0]
  if (first) activeId.value = first.id
}

const categoryIcons = {
  '后端开发': 'M16 18l6-6-6-6',
  '前端开发': 'M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5',
  '数据库': 'M12 2C6.48 2 2 4.02 2 6.5S6.48 11 12 11s10-2.02 10-4.5S17.52 2 12 2zM2 12.5c0 2.48 4.48 4.5 10 4.5s10-2.02 10-4.5',
  '求职能力': 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z'
}
</script>

<template>
  <div class="learn-v2">
    <!-- Hero -->
    <section class="learn-hero">
      <div class="learn-hero-inner">
        <div class="learn-hero-left">
          <h1>学习中心</h1>
          <p>后端、前端、数据库和求职能力，边学边完善简历</p>
        </div>
        <div class="learn-hero-stat">
          <strong>{{ docs.length }}</strong>
          <span>篇文档</span>
        </div>
      </div>
    </section>

    <!-- Content -->
    <section class="learn-content">
      <div class="learn-shell">
        <!-- Sidebar -->
        <aside class="learn-sidebar">
          <div class="ls-search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input v-model="keyword" placeholder="搜索文档" />
          </div>

          <div class="ls-tabs">
            <button
              v-for="cat in categories"
              :key="cat"
              type="button"
              class="ls-tab"
              :class="{ active: activeCategory === cat }"
              @click="selectCategory(cat)"
            >
              {{ cat }}
            </button>
          </div>

          <div class="ls-docs">
            <button
              v-for="doc in filteredDocs"
              :key="doc.id"
              type="button"
              class="ls-doc"
              :class="{ active: activeDoc.id === doc.id }"
              @click="activeId = doc.id"
            >
              <div class="lsd-top">
                <span class="lsd-cat">{{ doc.category }}</span>
                <span class="lsd-time">{{ doc.minutes }} min</span>
              </div>
              <h3>{{ doc.title }}</h3>
              <p>{{ doc.desc }}</p>
            </button>
          </div>
        </aside>

        <!-- Reader -->
        <main class="learn-reader">
          <div class="lr-head">
            <div class="lr-meta">
              <span class="lr-cat">{{ activeDoc.category }}</span>
              <span class="lr-level">{{ activeDoc.level }}</span>
            </div>
            <h2>{{ activeDoc.title }}</h2>
            <p>{{ activeDoc.desc }}</p>
            <div class="lr-info">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              <span>约 {{ activeDoc.minutes }} 分钟阅读</span>
            </div>
          </div>
          <div class="lr-body markdown-result" v-html="renderMarkdown(activeDoc.content)"></div>
        </main>
      </div>
    </section>
  </div>
</template>
