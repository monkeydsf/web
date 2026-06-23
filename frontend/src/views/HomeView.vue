<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { mockJobs } from '../data/mockJobs'

const router = useRouter()
const API_ORIGIN = 'http://localhost:8087'

const baseSlides = [
  {
    title: '校园招聘信息统一发布',
    desc: '为大学生提供实习、校招、就业指导一体化服务。',
    label: '就业服务',
    image: `${API_ORIGIN}/backgrounds/home.svg`
  },
  {
    title: '快速找到适合自己的岗位',
    desc: '按城市、方向、企业筛选机会，让求职更有目标。',
    label: '职位匹配',
    image: 'https://picsum.photos/seed/university-job-fair/1600/900'
  },
  {
    title: '投递进度清晰可见',
    desc: '学生投递、企业审核、状态反馈形成完整闭环。',
    label: '投递管理',
    image: 'https://picsum.photos/seed/student-interview-room/1600/900'
  }
]

const currentSlide = ref(0)
const slides = ref(baseSlides)
const jobs = ref([])
const loading = ref(true)
const notice = ref('')
let timer = null

const activeSlide = computed(() => slides.value[currentSlide.value] || slides.value[0])
const visibleJobs = computed(() => jobs.value.slice(0, 4))

function withApiOrigin(url) {
  if (!url) return ''
  return url.startsWith('http') ? url : `${API_ORIGIN}${url}`
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '薪资面议'
  return `${job.salaryMin}-${job.salaryMax}${job.jobType === '实习' ? '/天' : '/月'}`
}

function changeSlide(index) {
  currentSlide.value = index
}

function nextSlide() {
  currentSlide.value = (currentSlide.value + 1) % slides.value.length
}

function goJobs() {
  router.push({ name: 'jobs' })
}

function goService() {
  router.push({ name: 'workbench' })
}

async function loadJobs() {
  loading.value = true
  notice.value = ''
  try {
    const data = await api('/jobs?status=OPEN')
    jobs.value = Array.isArray(data) && data.length ? data : mockJobs
  } catch {
    jobs.value = mockJobs
    notice.value = '当前展示演示职位，后端启动后会自动读取真实数据。'
  } finally {
    loading.value = false
  }
}

async function loadHomeBackground() {
  try {
    const data = await api('/backgrounds', { timeout: 5000 })
    const home = data.find((item) => item.pageKey === 'home')
    if (home?.imageUrl) {
      slides.value = [
        {
          ...baseSlides[0],
          image: withApiOrigin(home.imageUrl)
        },
        ...baseSlides.slice(1)
      ]
    }
  } catch {
    slides.value = baseSlides
  }
}

onMounted(() => {
  loadHomeBackground()
  loadJobs()
  timer = window.setInterval(nextSlide, 5000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<template>
  <div>
    <section class="hero">
      <div class="hero-visual">
        <img :src="activeSlide.image" :alt="activeSlide.title" />
        <div class="hero-shade"></div>
        <div class="hero-content">
          <span class="slide-label">{{ activeSlide.label }}</span>
          <h1>{{ activeSlide.title }}</h1>
          <p>{{ activeSlide.desc }}</p>
          <div class="hero-actions">
            <button class="primary-link" @click="goJobs">浏览职位</button>
            <button class="plain-link" @click="goService">进入工作台</button>
          </div>
        </div>
        <div class="slide-tabs" aria-label="轮播切换">
          <button
            v-for="(slide, index) in slides"
            :key="slide.title"
            :class="{ active: currentSlide === index }"
            type="button"
            @click="changeSlide(index)"
          >
            {{ String(index + 1).padStart(2, '0') }}
          </button>
        </div>
      </div>

      <form class="search-panel" @submit.prevent="goJobs">
        <label>
          <span>职位关键词</span>
          <input type="text" placeholder="Java / 前端 / 运营" />
        </label>
        <label>
          <span>工作城市</span>
          <input type="text" placeholder="杭州 / 上海 / 南京" />
        </label>
        <label>
          <span>岗位类型</span>
          <select>
            <option>全部类型</option>
            <option>实习</option>
            <option>校招</option>
          </select>
        </label>
        <button type="submit" class="search-btn">搜索职位</button>
      </form>
    </section>

    <section class="home-jobs">
      <div class="section-heading">
        <span>Featured positions</span>
        <h2>热门职位</h2>
        <p>精选适合大学生起步的岗位，信息直观，便于快速判断方向和投递价值。</p>
      </div>

      <div v-if="loading" class="job-list">
        <article v-for="item in 4" :key="item" class="job-item loading-card">
          <div></div>
          <strong></strong>
          <p></p>
        </article>
      </div>

      <div v-else class="job-list">
        <article v-for="job in visibleJobs" :key="job.id" class="job-item">
          <div class="job-main">
            <span>{{ job.jobType || '校招' }}</span>
            <h3>{{ job.title }}</h3>
            <p>{{ job.company }} · {{ job.city }}</p>
            <small>{{ job.description }}</small>
          </div>
          <div class="job-side">
            <strong>{{ salaryText(job) }}</strong>
            <span>{{ job.educationRequirement }}</span>
          </div>
        </article>
      </div>

      <p v-if="notice" class="notice">{{ notice }}</p>

      <div class="view-all">
        <button class="view-all-btn" @click="goJobs">查看全部职位</button>
      </div>
    </section>

    <section class="home-service">
      <div class="section-heading">
        <span>Platform service</span>
        <h2>面向学生和就业管理的轻量平台</h2>
      </div>
      <div class="service-grid">
        <article>
          <div class="service-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
          </div>
          <strong>岗位发布</strong>
          <p>企业维护岗位、城市、薪资和任职要求。</p>
        </article>
        <article>
          <div class="service-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2L11 13"/><path d="M22 2L15 22L11 13L2 9L22 2Z"/></svg>
          </div>
          <strong>学生投递</strong>
          <p>查看详情后一键投递，记录清晰可追踪。</p>
        </article>
        <article>
          <div class="service-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          </div>
          <strong>状态审核</strong>
          <p>投递状态支持待审核、已查看、通过和拒绝。</p>
        </article>
      </div>
    </section>
  </div>
</template>
