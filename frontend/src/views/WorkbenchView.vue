<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const resumeChecks = [
  { label: '基础信息', done: true },
  { label: '项目经历', done: true },
  { label: '岗位匹配', done: false },
  { label: '面试表达', done: false }
]

const aiTools = [
  { title: '对话咨询', desc: '多轮询问求职方向、投递节奏和面试准备。', path: '/tools/consult' },
  { title: '改简历', desc: '检查表达、经历亮点和岗位匹配度。', path: '/tools/resume-review' },
  { title: '找岗位', desc: '根据城市、技能和期望薪资筛出机会。', path: '/tools/job-match' },
  { title: '练面试', desc: '围绕岗位生成问题并模拟追问。', path: '/tools/interview-practice' },
  { title: '谈 Offer', desc: '整理薪资、福利和入职时间谈判思路。', path: '/tools/offer-negotiation' }
]

const moreTools = [
  { title: '职业性格测试', path: '/tools/personality-test' },
  { title: '30 天求职规划', path: '/tools/30-day-plan' },
  { title: '投递话术生成', path: '/tools/application-script' },
  { title: '公司避坑雷达', path: '/tools/company-radar' },
  { title: '通知中心', path: '/notifications' }
]

function openTool(tool) {
  if (tool.path) router.push(tool.path)
}
</script>

<template>
  <div class="workbench-page">
    <section class="workbench-hero">
      <div>
        <span class="page-tag">Workbench</span>
        <h1>职策工作台</h1>
        <p>把简历、岗位、面试、Offer 和学习规划放在一个任务面板里，按求职阶段逐项推进。</p>
      </div>
      <button class="admin-ghost" type="button" @click="router.push('/resume')">我的简历</button>
    </section>

    <section class="career-tools-grid">
      <article class="resume-status-card">
        <div class="resume-score">
          <span>简历状态卡</span>
          <strong>72</strong>
          <small>待优化</small>
        </div>
        <div class="resume-checks">
          <div v-for="item in resumeChecks" :key="item.label" :class="{ done: item.done }">
            <span>{{ item.done ? '✓' : '·' }}</span>
            <p>{{ item.label }}</p>
          </div>
        </div>
        <button class="view-all-btn" @click="router.push('/resume')">完善简历</button>
      </article>

      <div class="tool-panel">
        <div class="tool-panel-head">
          <h3>AI 核心工具</h3>
          <p>围绕求职主流程提供辅助。</p>
        </div>
        <div class="ai-tool-grid">
          <article
            v-for="tool in aiTools"
            :key="tool.title"
            :class="['ai-tool-card', { available: tool.path }]"
            role="button"
            tabindex="0"
            @click="openTool(tool)"
            @keydown.enter="openTool(tool)"
          >
            <strong>{{ tool.title }}</strong>
            <span>{{ tool.desc }}</span>
            <small v-if="tool.path">进入工具</small>
          </article>
        </div>
      </div>

      <div class="tool-panel more-tool-panel">
        <div class="tool-panel-head">
          <h3>更多工具</h3>
          <p>补齐规划、话术和企业判断。</p>
        </div>
        <div class="more-tool-list">
          <button v-for="tool in moreTools" :key="tool.title" type="button" @click="router.push(tool.path)">
            {{ tool.title }}
          </button>
        </div>
      </div>
    </section>
  </div>
</template>
