import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import JobsView from '../views/JobsView.vue'
import LearnView from '../views/LearnView.vue'
import WorkbenchView from '../views/WorkbenchView.vue'
import LoginView from '../views/LoginView.vue'
import ResumeView from '../views/ResumeView.vue'
import ApplicationsView from '../views/ApplicationsView.vue'
import ApplicationChatView from '../views/ApplicationChatView.vue'
import ResumeReviewView from '../views/tools/ResumeReviewView.vue'
import ToolPromptView from '../views/tools/ToolPromptView.vue'
import ConsultChatView from '../views/tools/ConsultChatView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminJobsView from '../views/admin/AdminJobsView.vue'
import AdminApplicationsView from '../views/admin/AdminApplicationsView.vue'
import AdminBackgroundsView from '../views/admin/AdminBackgroundsView.vue'
import AdminReportsView from '../views/admin/AdminReportsView.vue'
import { getSession } from '../api'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/jobs', name: 'jobs', component: JobsView },
  { path: '/learn', name: 'learn', component: LearnView },
  { path: '/workbench', name: 'workbench', component: WorkbenchView },
  { path: '/messages/:id?', name: 'messages', component: ApplicationChatView, meta: { requiresAuth: true } },
  { path: '/notifications', name: 'notifications', component: NotificationsView, meta: { requiresAuth: true } },
  { path: '/resume', name: 'resume', component: ResumeView, meta: { requiresAuth: true, requiresStudent: true } },
  { path: '/applications', name: 'applications', component: ApplicationsView, meta: { requiresAuth: true, requiresStudent: true } },
  { path: '/applications/:id/chat', redirect: to => `/messages/${to.params.id}` },
  { path: '/tools/resume-review', name: 'resume-review', component: ResumeReviewView },
  { path: '/tools/consult', name: 'consult-chat', component: ConsultChatView },
  {
    path: '/tools/job-match',
    name: 'job-match',
    component: ToolPromptView,
    meta: {
      tool: {
        title: '找岗位',
        tag: 'Job match',
        intro: '输入专业、技能、城市和薪资期望，生成适合投递的岗位方向。',
        inputLabel: '求职条件',
        placeholder: '例如：软件工程，本科，熟悉 Java、Spring Boot、MySQL，希望在杭州找后端实习，薪资 180-250/天。',
        button: '生成岗位建议',
        template: '请根据以下求职条件，输出适合投递的岗位方向、关键词、城市建议、筛选规则和投递优先级：'
      }
    }
  },
  {
    path: '/tools/interview-practice',
    name: 'interview-practice',
    component: ToolPromptView,
    meta: {
      tool: {
        title: '练面试',
        tag: 'Interview practice',
        intro: '输入目标岗位和技术栈，生成面试问题、参考回答和追问方向。',
        inputLabel: '面试目标',
        placeholder: '例如：Java 后端实习生，项目用过 Spring Boot、Vue、MySQL，想练项目介绍和八股问题。',
        button: '生成面试练习',
        template: '请扮演面试官，基于以下目标生成面试练习，包括自我介绍修改、10 个问题、参考回答要点和追问：'
      }
    }
  },
  {
    path: '/tools/offer-negotiation',
    name: 'offer-negotiation',
    component: ToolPromptView,
    meta: {
      tool: {
        title: '谈 Offer',
        tag: 'Offer negotiation',
        intro: '整理薪资、福利、实习周期和入职条件，生成沟通策略和话术。',
        inputLabel: 'Offer 情况',
        placeholder: '例如：杭州后端实习，220/天，每周 5 天，要求尽快入职；我还有另一个 200/天 的 offer。',
        button: '生成谈判方案',
        template: '请根据以下 Offer 情况，输出风险判断、可谈条件、沟通顺序和可直接发送的话术：'
      }
    }
  },
  {
    path: '/tools/personality-test',
    name: 'personality-test',
    component: ToolPromptView,
    meta: {
      tool: {
        title: '职业性格测试',
        tag: 'Career personality',
        intro: '描述你的偏好、压力来源和合作方式，生成适合的岗位环境与避雷提醒。',
        inputLabel: '个人偏好',
        placeholder: '例如：我喜欢独立完成任务，能接受代码细节，不太喜欢频繁汇报，遇到压力会先查资料再求助。',
        button: '生成职业画像',
        template: '请根据以下个人偏好，输出职业性格画像、适合岗位、团队环境建议和需要避开的工作类型：'
      }
    }
  },
  {
    path: '/tools/30-day-plan',
    name: 'thirty-day-plan',
    component: ToolPromptView,
    meta: {
      tool: {
        title: '30 天求职规划',
        tag: '30 day plan',
        intro: '输入目标岗位和当前基础，生成每天可执行的求职推进计划。',
        inputLabel: '目标与基础',
        placeholder: '例如：30 天内找到 Java 实习，已学 Java 基础、Spring Boot、MySQL，有一个课程项目，简历还没完善。',
        button: '生成 30 天计划',
        template: '请根据以下目标与基础，生成 30 天求职规划，按周拆解目标，并给出每天任务：'
      }
    }
  },
  {
    path: '/tools/application-script',
    name: 'application-script',
    component: ToolPromptView,
    meta: {
      tool: {
        title: '投递话术生成',
        tag: 'Application script',
        intro: '输入岗位 JD 和你的亮点，生成邮件标题、正文、开场白和内推话术。',
        inputLabel: '岗位与个人亮点',
        placeholder: '例如：投递 Java 后端实习，JD 要求 Spring Boot 和 MySQL。我做过校园求职系统，有登录、岗位、投递模块。',
        button: '生成投递话术',
        template: '请根据以下岗位与个人亮点，生成邮件标题、邮件正文、Boss 开场白和内推话术：'
      }
    }
  },
  {
    path: '/tools/company-radar',
    name: 'company-radar',
    component: ToolPromptView,
    meta: {
      tool: {
        title: '公司避坑雷达',
        tag: 'Company radar',
        intro: '输入公司信息、岗位描述和面试感受，整理需要确认的问题和潜在风险。',
        inputLabel: '公司与岗位信息',
        placeholder: '例如：小型外包公司，岗位写全栈开发实习，面试说要能加班，薪资面议，试用期较长。',
        button: '生成风险雷达',
        template: '请根据以下公司与岗位信息，输出风险等级、需要追问的问题、可接受条件和避坑建议：'
      }
    }
  },
  { path: '/login', name: 'login', component: LoginView },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresManager: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'admin-dashboard', component: AdminDashboardView },
      { path: 'jobs', name: 'admin-jobs', component: AdminJobsView },
      { path: 'applications', name: 'admin-applications', component: AdminApplicationsView },
      { path: 'backgrounds', name: 'admin-backgrounds', component: AdminBackgroundsView },
      { path: 'reports', name: 'admin-reports', component: AdminReportsView }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const session = getSession()
  const role = session?.user?.role
  const isManager = role === 'COMPANY' || role === 'ADMIN'

  if (to.meta.requiresAuth && !session) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresManager && !isManager) {
    return { path: '/login', query: { role: 'COMPANY', redirect: '/admin/dashboard' } }
  }

  if (to.meta.requiresStudent && role !== 'JOB_SEEKER' && role !== 'STUDENT') {
    return { path: '/login', query: { role: 'JOB_SEEKER', redirect: to.fullPath } }
  }

  if (to.path === '/login' && session) {
    return role === 'JOB_SEEKER' || role === 'STUDENT' ? { path: '/jobs' } : { path: '/admin/dashboard' }
  }

  return true
})

export default router
