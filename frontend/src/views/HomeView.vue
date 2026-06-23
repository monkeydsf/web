<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { mockJobs } from '../data/mockJobs'

const router = useRouter()
const API_ORIGIN = 'http://localhost:8087'

const jobs = ref([])
const loading = ref(true)
const notice = ref('')
const activeCategory = ref('全部')
const homeBackground = ref(`${API_ORIGIN}/backgrounds/home.svg`)
const categories = ['全部', '实习', '校招', '前端', '后端', '产品', '运营', '设计']

const categoryBlocks = [
  ['互联网/AI', 'Java', 'C/C++', 'PHP'],
  ['电子/电气/通信', '电子工程师', '硬件工程师'],
  ['产品', '产品经理', '产品专员/助理', '产品总监'],
  ['客服/运营', '客服专员', '客服主管', '客服经理'],
  ['销售', '销售专员', '电话销售', '网络销售'],
  ['人力/行政/法务', '人力资源专员/助理'],
  ['财务/审计/税务', '会计', '总账会计', '成本会计']
]

const filteredJobs = computed(() => {
  if (activeCategory.value === '全部') return jobs.value
  return jobs.value.filter((job) => {
    const text = `${job.jobType || ''} ${job.title || ''} ${job.description || ''} ${job.company || ''}`.toLowerCase()
    return text.includes(activeCategory.value.toLowerCase())
  })
})

const topJobs = computed(() => filteredJobs.value.slice(0, 4))
const quickJobs = computed(() => jobs.value.slice(0, 8))

function withApiOrigin(url) {
  if (!url) return homeBackground.value
  return url.startsWith('http') ? url : `${API_ORIGIN}${url}`
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '薪资面议'
  const unit = job.jobType === '实习' ? '/天' : '/月'
  return `${job.salaryMin}-${job.salaryMax}${unit}`
}

function goJobs() {
  router.push({ name: 'jobs' })
}

function applyCategory(name) {
  activeCategory.value = name
}

async function loadJobs() {
  loading.value = true
  notice.value = ''
  try {
    const data = await api('/jobs?status=OPEN')
    jobs.value = Array.isArray(data) && data.length ? data : mockJobs
  } catch {
    jobs.value = mockJobs
    notice.value = '当前展示的是示例岗位，后端启动后会自动切换为真实数据。'
  } finally {
    loading.value = false
  }
}

async function loadHomeBackground() {
  try {
    const data = await api('/backgrounds', { timeout: 5000 })
    const home = data.find((item) => item.pageKey === 'home')
    if (home?.imageUrl) homeBackground.value = withApiOrigin(home.imageUrl)
  } catch {
    homeBackground.value = `${API_ORIGIN}/backgrounds/home.svg`
  }
}

onMounted(() => {
  loadHomeBackground()
  loadJobs()
})
</script>

<template>
  <div class="home-page">
    <section class="home-hero">
      <form class="home-search glass-card" @submit.prevent="goJobs">
        <label class="home-search-type">
          <span>职位类型</span>
          <select v-model="activeCategory">
            <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label class="home-search-keyword">
          <span>搜索职位、公司</span>
          <input type="text" placeholder="输入关键词" />
        </label>
        <label class="home-search-map">
          <span>地图</span>
          <button type="button" class="map-pill" @click="goJobs">查看职位地图</button>
        </label>
        <button type="submit" class="search-btn">搜索</button>
      </form>

      <div class="home-hot-list">
        <span class="home-hot-label">热门职位：</span>
        <button
          v-for="item in categories.slice(1, 8)"
          :key="item"
          type="button"
          class="hot-chip"
          @click="applyCategory(item)"
        >
          {{ item }}
        </button>
      </div>
    </section>

    <section class="home-stage">
      <aside class="home-category-panel glass-card">
        <div class="home-category-scroll">
          <article v-for="item in categoryBlocks" :key="item[0]" class="home-category-row">
            <strong>{{ item[0] }}</strong>
            <div>
              <span v-for="tag in item.slice(1)" :key="tag">{{ tag }}</span>
            </div>
            <svg width="10" height="10" viewBox="0 0 10 10" fill="none" aria-hidden="true">
              <path d="M3 1.5L6.5 5L3 8.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </article>
        </div>
        <div class="home-category-footer">
          <span>1 / 4</span>
          <div>
            <button type="button">‹</button>
            <button type="button">›</button>
          </div>
        </div>
      </aside>

      <section class="home-hero-board glass-card" :style="{ '--hero-art': `url(${homeBackground})` }">
        <div class="home-hero-grid">
          <article class="hero-feature hero-feature-lg">
            <span class="hero-badge">直聘简历</span>
            <h1>写好简历，找好工作</h1>
            <p>密集展示岗位、公司、薪资、城市和方向，先筛选再投递。</p>
          </article>
          <article class="hero-feature hero-feature-sm">
            <h2>前端岗位热</h2>
            <p>Web 前端工程师精选</p>
          </article>
          <article class="hero-feature hero-feature-wide">
            <h2>算法方向强需求</h2>
            <p>算法工程师岗位精选</p>
          </article>
          <article class="hero-feature hero-feature-sm hero-feature-bottom">
            <h2>高薪 Java 岗</h2>
            <p>Java 工程师岗位精选</p>
          </article>
        </div>
      </section>
    </section>

    <section class="home-jobs-section glass-card">
      <div class="section-heading compact">
        <span>Featured positions</span>
        <h2>热门职位</h2>
        <p>把岗位独立出来，首屏先看方向，再看具体机会，层次更清楚。</p>
      </div>

      <div v-if="loading" class="job-list home-job-list">
        <article v-for="item in 6" :key="item" class="job-item loading-card home-job-card">
          <div></div>
          <strong></strong>
          <p></p>
        </article>
      </div>

      <div v-else class="home-job-grid">
        <article v-for="job in topJobs" :key="job.id" class="home-job-card">
          <div class="job-main">
            <span class="job-type-tag">{{ job.jobType || '校招' }}</span>
            <h3>{{ job.title }}</h3>
            <p class="job-meta">{{ job.company }} · {{ job.city }}</p>
            <small class="job-desc">{{ job.description }}</small>
          </div>
          <div class="job-side">
            <strong class="salary">{{ salaryText(job) }}</strong>
            <span class="edu">{{ job.educationRequirement }}</span>
          </div>
        </article>
      </div>

      <p v-if="notice" class="notice">{{ notice }}</p>

      <div class="view-all">
        <button class="view-all-btn" type="button" @click="goJobs">查看全部职位</button>
      </div>
    </section>

    <section class="home-quick-grid">
      <article class="home-quick-panel glass-card">
        <div class="section-heading compact">
          <span>Quick picks</span>
          <h2>快速浏览</h2>
        </div>
        <div class="home-quick-list">
          <button v-for="job in quickJobs" :key="job.id" type="button" @click="goJobs">
            <strong>{{ job.title }}</strong>
            <span>{{ job.company }} · {{ job.city }}</span>
          </button>
        </div>
      </article>

      <article class="home-quick-panel glass-card">
        <div class="section-heading compact">
          <span>Platform service</span>
          <h2>平台功能</h2>
        </div>
        <div class="service-grid home-service-grid">
          <article>
            <strong>岗位发布</strong>
            <p>企业维护岗位、城市、薪资和任职要求。</p>
          </article>
          <article>
            <strong>学生投递</strong>
            <p>查看详情后一键投递，记录清晰可追踪。</p>
          </article>
          <article>
            <strong>状态审核</strong>
            <p>投递状态支持待审核、已查看、通过和拒绝。</p>
          </article>
        </div>
      </article>
    </section>
  </div>
</template>
