<script setup>
import { computed, onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { mockJobs } from '../data/mockJobs'

const router = useRouter()
const overview = ref(null)
const loading = ref(true)
const keyword = ref('')

const categories = [
  {
    title: '技术开发', icon: 'code',
    subs: ['Java 开发', 'Python 开发', '前端开发', '后端开发', '算法工程师', '全栈开发', '移动开发', 'DevOps']
  },
  {
    title: '产品设计', icon: 'design',
    subs: ['产品经理', 'UI 设计师', '交互设计', '视觉设计', '用户研究', '数据分析']
  },
  {
    title: '运营市场', icon: 'market',
    subs: ['新媒体运营', '内容运营', '增长运营', '品牌策划', '市场推广', '社群运营']
  },
  {
    title: '职能支持', icon: 'office',
    subs: ['人力资源', '财务会计', '行政文秘', '法务合规', '采购管理']
  },
  {
    title: '教育培训', icon: 'edu',
    subs: ['讲师', '教研', '课程设计', '教育产品', '培训管理']
  },
  {
    title: '实习岗位', icon: 'intern',
    subs: ['暑期实习', '日常实习', '远程实习', '项目实习', '管培实习']
  }
]

const activeCat = ref(0)

const activeSubs = computed(() => categories[activeCat.value]?.subs || [])

const features = [
  { title: '海量岗位', desc: '覆盖全国多城市的实习与校招职位', icon: 'grid' },
  { title: 'AI 辅助', desc: '简历优化、面试模拟、岗位匹配一步到位', icon: 'spark' },
  { title: '精准匹配', desc: '按专业、技能和城市智能推荐岗位', icon: 'free' },
  { title: '实时更新', desc: '岗位信息每日更新，不错过任何机会', icon: 'refresh' }
]

const platformStats = reactive([
  { target: 2400, value: 0, suffix: '+', label: '已服务学生' },
  { target: 580, value: 0, suffix: '+', label: '在招岗位' },
  { target: 120, value: 0, suffix: '+', label: '合作企业' },
  { target: 36, value: 0, suffix: '', label: '覆盖城市' }
])

function animateCountUp() {
  const duration = 1200
  const startTime = performance.now()
  function step(now) {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const ease = 1 - Math.pow(1 - progress, 3)
    platformStats.forEach(s => {
      s.value = Math.round(s.target * ease)
    })
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

const jobs = computed(() => overview.value?.featuredJobs?.length ? overview.value.featuredJobs : mockJobs)
const hotJobs = computed(() => jobs.value.slice(0, 5))

const avatarColors = ['av-blue', 'av-green', 'av-purple', 'av-orange', 'av-teal']

function companyInitial(name) {
  return name ? name.charAt(0) : '·'
}

function avatarColor(index) {
  return avatarColors[index % avatarColors.length]
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '面议'
  const unit = job.jobType === '实习' ? '/天' : '/月'
  return `${job.salaryMin}-${job.salaryMax}${unit}`
}

function goJobs(query = {}) {
  router.push({ name: 'jobs', query })
}

async function loadHome() {
  loading.value = true
  try {
    overview.value = await api('/home', { timeout: 5000 })
  } catch {
    overview.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHome()
  setTimeout(animateCountUp, 400)
})
</script>

<template>
  <div class="home-v2">
    <!-- Hero -->
    <section class="home-hero">
      <div class="home-hero-inner">
        <div class="home-hero-left">
          <h1 class="home-hero-title">找到属于你的下一站</h1>
          <p class="home-hero-desc">覆盖实习与校招，AI 帮你更快拿到 Offer</p>
          <form class="home-search-form" @submit.prevent="goJobs({ keyword })">
            <div class="home-search-wrap">
              <svg class="home-search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              <input v-model="keyword" type="text" placeholder="搜索职位、公司、关键词" class="home-search-input" />
              <button type="submit" class="home-search-btn">搜索</button>
            </div>
          </form>

<div class="home-quick-tags">
            <span class="home-quick-label">热门搜索</span>
            <button v-for="tag in ['Java', '前端', 'Python', '产品', '杭州', '上海', '实习']" :key="tag" type="button" class="home-quick-tag" @click="goJobs({ keyword: tag })">{{ tag }}</button>
          </div>
        </div>

        <div class="home-hero-right">
          <div class="home-hero-visual">
            <div class="visual-card vc-1">
              <div class="vc-dot" style="background:#4a90e2"></div>
              <div class="vc-lines">
                <span></span><span></span><span style="width:60%"></span>
              </div>
            </div>
            <div class="visual-card vc-2">
              <div class="vc-dot" style="background:#6b8cce"></div>
              <div class="vc-lines">
                <span></span><span style="width:70%"></span><span></span>
              </div>
            </div>
            <div class="visual-card vc-3">
              <div class="vc-dot" style="background:#5a9fd4"></div>
              <div class="vc-lines">
                <span style="width:50%"></span><span></span><span style="width:80%"></span>
              </div>
            </div>
            <div class="visual-float visual-float-1">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            </div>
            <div class="visual-float visual-float-2">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            </div>
            <div class="visual-float visual-float-3">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="home-features">
      <div class="home-features-inner">
        <div v-for="f in features" :key="f.title" class="home-feature-item">
          <div class="home-feature-icon">
            <svg v-if="f.icon === 'grid'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
            <svg v-else-if="f.icon === 'spark'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
            <svg v-else-if="f.icon === 'free'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
          </div>
          <div>
            <strong>{{ f.title }}</strong>
            <span>{{ f.desc }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Platform Stats -->
    <section class="home-platform">
      <div class="home-platform-inner">
        <div v-for="s in platformStats" :key="s.label" class="home-platform-item">
          <strong>{{ s.value.toLocaleString() }}{{ s.suffix }}</strong>
          <span>{{ s.label }}</span>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="home-section">
      <div class="home-section-inner">
        <div class="home-section-header">
          <div>
            <h2>岗位分类</h2>
            <p>选择方向，探索更多岗位</p>
          </div>
          <button type="button" class="home-link-btn" @click="goJobs">查看全部职位</button>
        </div>

        <div class="home-cat-tabs">
          <button
            v-for="(cat, i) in categories"
            :key="cat.title"
            type="button"
            class="home-cat-tab"
            :class="{ active: activeCat === i }"
            @click="activeCat = i"
          >
            <svg v-if="cat.icon === 'code'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            <svg v-else-if="cat.icon === 'design'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
            <svg v-else-if="cat.icon === 'market'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12V7H5a2 2 0 0 1 0-4h14v4"/><path d="M3 5v14a2 2 0 0 0 2 2h16v-5"/></svg>
            <svg v-else-if="cat.icon === 'office'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
            <svg v-else-if="cat.icon === 'edu'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
            <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
            <span>{{ cat.title }}</span>
          </button>
        </div>

        <div class="home-cat-subs">
          <button
            v-for="(sub, i) in activeSubs"
            :key="sub"
            type="button"
            class="home-cat-sub"
            :style="{ animationDelay: (i * 0.04) + 's' }"
            @click="goJobs({ keyword: sub })"
          >
            {{ sub }}
          </button>
        </div>
      </div>
    </section>

    <!-- Hot Jobs -->
    <section class="home-section">
      <div class="home-section-inner">
        <div class="home-section-header">
          <div>
            <h2>热门岗位</h2>
            <p>近期受欢迎的实习与校招机会</p>
          </div>
          <button type="button" class="home-link-btn" @click="goJobs">查看全部</button>
        </div>
        <div class="hot-jobs-list">
          <div
            v-for="(job, idx) in hotJobs"
            :key="job.id"
            class="hot-job-row"
            @click="goJobs({ id: job.id })"
          >
            <div class="hjr-avatar" :class="avatarColor(idx)">{{ companyInitial(job.company) }}</div>
            <div class="hjr-left">
              <h3 class="hjr-title">{{ job.title }}</h3>
              <div class="hjr-info">
                <span class="hjr-company">{{ job.company }}</span>
                <span class="hjr-dot">·</span>
                <span>{{ job.city }}</span>
                <span class="hjr-dot">·</span>
                <span>{{ job.educationRequirement }}</span>
              </div>
            </div>
            <div class="hjr-right">
              <span class="hjr-type" :class="job.jobType === '实习' ? 'type-intern' : 'type-campus'">{{ job.jobType }}</span>
              <span class="hjr-salary">{{ salaryText(job) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <span class="footer-logo">职策</span>
          <span class="footer-desc">面向大学生的校园求职平台</span>
        </div>
        <div class="footer-links">
          <div class="footer-col">
            <strong>求职</strong>
            <a href="/jobs">浏览职位</a>
            <a href="/resume">我的简历</a>
            <a href="/applications">投递记录</a>
          </div>
          <div class="footer-col">
            <strong>AI 工具</strong>
            <a href="/tools/resume-review">AI 改简历</a>
            <a href="/tools/consult">求职咨询</a>
            <a href="/tools/job-match">岗位匹配</a>
            <a href="/tools/interview-practice">面试模拟</a>
          </div>
          <div class="footer-col">
            <strong>更多</strong>
            <a href="/workbench">工作台</a>
            <a href="/learn">学习资源</a>
            <a href="/notifications">通知中心</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <span>&copy; 2026 职策 Campus Career. All rights reserved.</span>
      </div>
    </footer>

</div>
</template>
