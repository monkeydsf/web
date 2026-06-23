<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const resumeChecks = [
  { label: '基础信息', done: true },
  { label: '项目经历', done: true },
  { label: '岗位匹配', done: false },
  { label: '面试表达', done: false }
]

const resumeScore = 72

const aiTools = [
  { title: '对话咨询', desc: '多轮询问求职方向、投递节奏和面试准备', path: '/tools/consult', color: '#4a90e2' },
  { title: '改简历', desc: '检查表达、经历亮点和岗位匹配度', path: '/tools/resume-review', color: '#5a9fd4' },
  { title: '找岗位', desc: '根据城市、技能和期望薪资筛出机会', path: '/tools/job-match', color: '#6b8cce' },
  { title: '练面试', desc: '围绕岗位生成问题并模拟追问', path: '/tools/interview-practice', color: '#7ea8d9' },
  { title: '谈 Offer', desc: '整理薪资、福利和入职时间谈判思路', path: '/tools/offer-negotiation', color: '#4a90e2' }
]

const moreTools = [
  { title: '职业性格测试', desc: '了解适合的岗位环境', path: '/tools/personality-test' },
  { title: '30 天求职规划', desc: '按天拆解求职目标', path: '/tools/30-day-plan' },
  { title: '投递话术生成', desc: '生成邮件和开场白', path: '/tools/application-script' },
  { title: '公司避坑雷达', desc: '评估风险和可谈条件', path: '/tools/company-radar' },
  { title: '通知中心', desc: '查看最新消息', path: '/notifications' }
]

function openTool(tool) {
  if (tool.path) router.push(tool.path)
}

function scoreColor(score) {
  if (score >= 80) return '#059669'
  if (score >= 60) return '#d97706'
  return '#dc2626'
}
</script>

<template>
  <div class="wb-v2">
    <!-- Hero -->
    <section class="wb-hero">
      <div class="wb-hero-inner">
        <div class="wb-hero-left">
          <h1>工作台</h1>
          <p>管理简历、岗位、面试，按求职阶段逐项推进</p>
        </div>
        <button class="wb-hero-btn" type="button" @click="router.push('/resume')">我的简历</button>
      </div>
    </section>

    <!-- Resume Status -->
    <section class="wb-section">
      <div class="wb-section-inner">
        <div class="wb-resume-grid">
          <!-- Score card -->
          <div class="wb-score-card">
            <div class="wsc-ring">
              <svg viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="42" fill="none" stroke="#e8eef6" stroke-width="8" />
                <circle cx="50" cy="50" r="42" fill="none" :stroke="scoreColor(resumeScore)" stroke-width="8" stroke-linecap="round" :stroke-dasharray="264" :stroke-dashoffset="264 - (264 * resumeScore / 100)" transform="rotate(-90 50 50)" style="transition: stroke-dashoffset 1s ease" />
              </svg>
              <div class="wsc-score">
                <strong>{{ resumeScore }}</strong>
                <span>分</span>
              </div>
            </div>
            <div class="wsc-info">
              <h3>简历评分</h3>
              <p>完善更多信息可以提高匹配度</p>
              <button type="button" @click="router.push('/resume')">去完善</button>
            </div>
          </div>

          <!-- Check list -->
          <div class="wb-check-card">
            <h3>完成进度</h3>
            <div class="wb-checks">
              <div v-for="item in resumeChecks" :key="item.label" class="wb-check-item" :class="{ done: item.done }">
                <div class="wci-dot">
                  <svg v-if="item.done" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                </div>
                <span>{{ item.label }}</span>
              </div>
            </div>
            <div class="wb-progress-bar">
              <div class="wpb-fill" :style="{ width: (resumeChecks.filter(c => c.done).length / resumeChecks.length * 100) + '%' }"></div>
            </div>
            <p class="wb-progress-text">{{ resumeChecks.filter(c => c.done).length }}/{{ resumeChecks.length }} 已完成</p>
          </div>
        </div>
      </div>
    </section>

    <!-- AI Tools -->
    <section class="wb-section">
      <div class="wb-section-inner">
        <div class="wb-section-head">
          <div>
            <h2>AI 求职工具</h2>
            <p>围绕求职主流程提供智能辅助</p>
          </div>
        </div>
        <div class="wb-tools-grid">
          <div
            v-for="tool in aiTools"
            :key="tool.title"
            class="wb-tool-card"
            role="button"
            tabindex="0"
            @click="openTool(tool)"
            @keydown.enter="openTool(tool)"
          >
            <div class="wbt-dot" :style="{ background: tool.color }"></div>
            <h3>{{ tool.title }}</h3>
            <p>{{ tool.desc }}</p>
            <span class="wbt-link">使用工具 →</span>
          </div>
        </div>
      </div>
    </section>

    <!-- More Tools -->
    <section class="wb-section wb-section-last">
      <div class="wb-section-inner">
        <div class="wb-section-head">
          <div>
            <h2>更多工具</h2>
            <p>规划、话术和企业评估</p>
          </div>
        </div>
        <div class="wb-more-grid">
          <button
            v-for="tool in moreTools"
            :key="tool.title"
            type="button"
            class="wb-more-card"
            @click="router.push(tool.path)"
          >
            <div class="wbm-left">
              <h3>{{ tool.title }}</h3>
              <p>{{ tool.desc }}</p>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>
