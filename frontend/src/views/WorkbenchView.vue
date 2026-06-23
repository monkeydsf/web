<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const resumeTools = [
  { title: '完善简历', desc: '补齐经历、技能和求职目标', path: '/resume', color: '#4a90e2' },
  { title: '基础信息', desc: '补城市、岗位和薪资期望', path: '/resume', color: '#5a9fd4' },
  { title: '项目经历', desc: '整理项目亮点和量化结果', path: '/resume', color: '#6b8cce' }
]

const aiTools = [
  { title: '岗位匹配', desc: '根据条件筛选适合投递的岗位方向', path: '/tools/job-match', color: '#4a90e2' },
  { title: '面试表达', desc: '练习自我介绍、项目表达和追问', path: '/tools/interview-practice', color: '#5a9fd4' },
  { title: '求职咨询', desc: '多轮回答求职过程中的具体问题', path: '/tools/consult', color: '#6b8cce' },
  { title: '谈 Offer', desc: '梳理沟通策略和可谈条件', path: '/tools/offer-negotiation', color: '#7ea8d9' }
]

const moreTools = [
  { title: '职业性格测试', desc: '看你更适合哪类岗位环境', path: '/tools/personality-test' },
  { title: '30 天求职规划', desc: '把目标拆成每天能做的动作', path: '/tools/30-day-plan' },
  { title: '投递话术生成', desc: '生成邮件、开场白和内推表达', path: '/tools/application-script' },
  { title: '公司避坑雷达', desc: '整理风险点和追问问题', path: '/tools/company-radar' }
]

function openTool(tool) {
  if (tool.path) router.push(tool.path)
}
</script>

<template>
  <div class="wb-v2">
    <section class="wb-hero">
      <div class="wb-hero-inner">
        <div class="wb-hero-left">
          <h1>工作台</h1>
          <p>简历、岗位、面试和投递，按求职流程逐步推进</p>
        </div>
        <button class="wb-hero-btn" type="button" @click="router.push('/resume')">我的简历</button>
      </div>
    </section>

    <section class="wb-section">
      <div class="wb-section-inner">
        <div class="wb-section-head">
          <div>
            <h2>简历</h2>
            <p>先把基础材料补齐</p>
          </div>
        </div>
        <div class="wb-tools-grid">
          <div
            v-for="tool in resumeTools"
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
            <span class="wbt-link">打开</span>
          </div>
        </div>
      </div>
    </section>

    <section class="wb-section">
      <div class="wb-section-inner">
        <div class="wb-section-head">
          <div>
            <h2>AI 工具</h2>
            <p>针对不同求职场景分别处理</p>
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
            <span class="wbt-link">使用工具</span>
          </div>
        </div>
      </div>
    </section>

    <section class="wb-section wb-section-last">
      <div class="wb-section-inner">
        <div class="wb-section-head">
          <div>
            <h2>更多工具</h2>
            <p>补充规划、文案和判断</p>
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
