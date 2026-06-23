<script setup>
const features = [
  {
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>',
    title: '岗位发布',
    desc: '企业维护职位信息，保证岗位、城市、薪资和要求都清楚。',
    details: ['企业信息维护', '岗位结构化管理', '薪资范围展示', '多城市同步发布']
  },
  {
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2L11 13"/><path d="M22 2L15 22L11 13L2 9L22 2Z"/></svg>',
    title: '学生投递',
    desc: '学生查看详情后一键投递，记录清晰可追踪。',
    details: ['一键快速投递', '投递记录追踪', '个人简历管理', '状态变化通知']
  },
  {
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>',
    title: '状态审核',
    desc: '投递状态支持待审核、已查看、通过和拒绝，全流程透明。',
    details: ['多状态流转管理', '审核意见反馈', '批量处理能力', '状态变化通知']
  },
  {
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20V10"/><path d="M18 20V4"/><path d="M6 20v-4"/></svg>',
    title: '数据统计',
    desc: '可视化展示招聘数据，帮助管理者看清趋势。',
    details: ['投递趋势分析', '岗位热度排行', '城市分布统计', '就业率追踪']
  }
]

const steps = [
  { num: '01', title: '注册登录', desc: '学生注册后进入前台，企业和管理员进入独立后台。' },
  { num: '02', title: '浏览职位', desc: '按城市、方向、企业筛选，快速定位目标岗位。' },
  { num: '03', title: '在线投递', desc: '一键提交简历，系统自动记录投递时间和状态。' },
  { num: '04', title: '跟踪反馈', desc: '实时查看审核进度，接收状态变化通知。' }
]
</script>

<template>
  <div class="service-page">
    <section class="service-hero">
      <div class="service-hero-inner">
        <span class="page-tag">Platform service</span>
        <h1>面向学生和就业管理的轻量平台</h1>
        <p>从岗位发布到投递审核，全流程数字化管理，让校园招聘更高效。</p>
      </div>
    </section>

    <section class="features-section">
      <div class="section-heading">
        <span>Core features</span>
        <h2>核心功能</h2>
      </div>
      <div class="features-grid">
        <article v-for="f in features" :key="f.title" class="feature-card">
          <div class="feature-icon" v-html="f.icon"></div>
          <h3>{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
          <ul class="feature-details">
            <li v-for="d in f.details" :key="d">{{ d }}</li>
          </ul>
        </article>
      </div>
    </section>

    <section class="steps-section">
      <div class="section-heading">
        <span>How it works</span>
        <h2>使用流程</h2>
      </div>
      <div class="steps-grid">
        <article v-for="s in steps" :key="s.num" class="step-card">
          <span class="step-num">{{ s.num }}</span>
          <h3>{{ s.title }}</h3>
          <p>{{ s.desc }}</p>
        </article>
      </div>
    </section>
  </div>
</template>
