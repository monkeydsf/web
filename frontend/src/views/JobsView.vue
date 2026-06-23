<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, getSession } from '../api'
import { mockJobs } from '../data/mockJobs'

const router = useRouter()
const jobs = ref([])
const loading = ref(true)
const notice = ref('')
const keyword = ref('')
const city = ref('')
const jobType = ref('')
const filteredJobs = ref([])
const selectedJob = ref(null)
const coverLetter = ref('')
const submitting = ref(false)
const chattingJobId = ref(null)
const applyMessage = ref('')
const currentPage = ref(1)
const pageSize = 5
const favorites = ref([])
const historyJobs = ref([])
const cityOptions = ref([])
const jobTypeOptions = ref([])
const compareIds = ref([])
const notifications = ref([])

const compareJobs = computed(() => jobs.value.filter(job => compareIds.value.includes(job.id)))
const totalPages = computed(() => Math.max(1, Math.ceil(filteredJobs.value.length / pageSize)))
const pagedJobs = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredJobs.value.slice(start, start + pageSize)
})
const pageNumbers = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  for (let page = start; page <= end; page += 1) pages.push(page)
  return pages
})
const favoriteJobs = computed(() => favorites.value)
const favoriteJobIds = computed(() => new Set(favorites.value.map(item => item.job?.id)))

function loadLocalList(key) {
  try {
    return JSON.parse(localStorage.getItem(key) || '[]')
  } catch {
    return []
  }
}

function saveLocalList(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

function recordHistory(job) {
  const list = loadLocalList('campus-career-job-history').filter(item => item.id !== job.id)
  list.unshift({ id: job.id, title: job.title, company: job.company, city: job.city })
  historyJobs.value = list.slice(0, 6)
  saveLocalList('campus-career-job-history', historyJobs.value)
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '薪资面议'
  return `${job.salaryMin}-${job.salaryMax}${job.jobType === '实习' ? '/天' : '/月'}`
}

function filterJobs() {
  let result = [...jobs.value]
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(job => [job.title, job.company, job.description].some(item => item?.toLowerCase().includes(kw)))
  }
  if (city.value) {
    const c = city.value.toLowerCase()
    result = result.filter(job => job.city.toLowerCase().includes(c))
  }
  if (jobType.value && jobType.value !== '全部类型') {
    result = result.filter(job => job.jobType === jobType.value)
  }
  filteredJobs.value = result
  currentPage.value = 1
}

function changePage(page) {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  currentPage.value = page
  window.scrollTo({ top: 360, behavior: 'smooth' })
}

function openApply(job) {
  recordHistory(job)
  const session = getSession()
  const role = session?.user?.role
  if (!session || (role !== 'JOB_SEEKER' && role !== 'STUDENT')) {
    router.push({ path: '/login', query: { role: 'JOB_SEEKER', redirect: '/jobs' } })
    return
  }
  selectedJob.value = job
  coverLetter.value = `你好，我对 ${job.title} 岗位很感兴趣，希望有机会进一步沟通。`
  applyMessage.value = ''
}

function closeApply() {
  selectedJob.value = null
  coverLetter.value = ''
  applyMessage.value = ''
}

async function submitApplication() {
  if (!selectedJob.value) return
  submitting.value = true
  applyMessage.value = ''
  try {
    await api(`/applications/jobs/${selectedJob.value.id}`, {
      method: 'POST',
      body: JSON.stringify({ coverLetter: coverLetter.value }),
      timeout: 5000
    })
    closeApply()
    router.push('/applications')
  } catch (error) {
    applyMessage.value = error.message || '投递失败'
  } finally {
    submitting.value = false
  }
}

async function startCompanyChat(job) {
  recordHistory(job)
  const session = getSession()
  const role = session?.user?.role
  if (!session || (role !== 'JOB_SEEKER' && role !== 'STUDENT')) {
    router.push({ path: '/login', query: { role: 'JOB_SEEKER', redirect: '/jobs' } })
    return
  }
  chattingJobId.value = job.id
  try {
    const existing = await api('/applications/mine')
    const matched = existing.find(item => item.job?.id === job.id)
    if (matched) {
      router.push(`/messages/${matched.id}`)
      return
    }
    const created = await api(`/applications/jobs/${job.id}`, {
      method: 'POST',
      body: JSON.stringify({ coverLetter: `你好，我对 ${job.title} 岗位很感兴趣，希望进一步沟通。` }),
      timeout: 5000
    })
    router.push(`/messages/${created.id}`)
  } catch (error) {
    applyMessage.value = error.message || '创建对话失败'
  } finally {
    chattingJobId.value = null
  }
}

async function toggleFavorite(job) {
  const session = getSession()
  const role = session?.user?.role
  if (!session || (role !== 'JOB_SEEKER' && role !== 'STUDENT')) {
    router.push({ path: '/login', query: { role: 'JOB_SEEKER', redirect: '/jobs' } })
    return
  }
  try {
    await api(`/favorites/${job.id}`, { method: 'POST' })
    await loadFavorites()
  } catch (error) {
    notice.value = error.message || '收藏失败'
  }
}

function isFavorite(job) {
  return favoriteJobIds.value.has(job.id)
}

function toggleCompare(job) {
  if (compareIds.value.includes(job.id)) {
    compareIds.value = compareIds.value.filter(id => id !== job.id)
    return
  }
  compareIds.value = [...compareIds.value, job.id].slice(-3)
}

async function reportJob(job) {
  const session = getSession()
  if (!session) {
    router.push({ path: '/login', query: { redirect: '/jobs' } })
    return
  }
  const reason = window.prompt('请输入举报原因', '岗位信息不实')
  if (!reason) return
  try {
    await api(`/reports/jobs/${job.id}`, {
      method: 'POST',
      body: JSON.stringify({ reason, detail: `来源于职位页：${job.title}` })
    })
    notice.value = '举报已提交'
  } catch (error) {
    notice.value = error.message || '举报失败'
  }
}

async function loadFavorites() {
  const session = getSession()
  const role = session?.user?.role
  if (!session || (role !== 'JOB_SEEKER' && role !== 'STUDENT')) {
    favorites.value = []
    return
  }
  try {
    favorites.value = await api('/favorites')
  } catch {
    favorites.value = []
  }
}

async function loadNotifications() {
  const session = getSession()
  if (!session) {
    notifications.value = []
    return
  }
  try {
    notifications.value = (await api('/notifications')).slice(0, 3)
  } catch {
    notifications.value = []
  }
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
    filteredJobs.value = [...jobs.value]
    currentPage.value = 1
    cityOptions.value = [...new Set(jobs.value.map(job => job.city))].filter(Boolean)
    jobTypeOptions.value = [...new Set(jobs.value.map(job => job.jobType))].filter(Boolean)
    historyJobs.value = loadLocalList('campus-career-job-history')
    await loadFavorites()
    await loadNotifications()
  }
}

onMounted(loadJobs)
</script>

<template>
  <div class="jobs-page">
    <section class="jobs-hero">
      <div class="jobs-hero-inner">
        <span class="page-tag">Job listings</span>
        <h1>职位介绍</h1>
        <p>从城市、关键词和岗位类型筛选，快速看到适合大学生起步的机会。</p>
      </div>
    </section>

    <section class="jobs-body">
      <div class="jobs-toolbar">
        <form class="filter-bar" @submit.prevent="filterJobs">
          <label>
            <span>关键词</span>
            <input v-model="keyword" type="text" placeholder="职位 / 公司" />
          </label>
          <label>
            <span>城市</span>
            <select v-model="city">
              <option value="">全部城市</option>
              <option v-for="item in cityOptions" :key="item" :value="item">{{ item }}</option>
            </select>
          </label>
          <label>
            <span>类型</span>
            <select v-model="jobType">
              <option>全部类型</option>
              <option v-for="item in jobTypeOptions" :key="item" :value="item">{{ item }}</option>
            </select>
          </label>
          <button type="submit" class="filter-btn">筛选</button>
        </form>

        <aside class="jobs-side-panel">
          <div>
            <strong>收藏</strong>
            <span>{{ favorites.length }} 个岗位</span>
          </div>
          <div v-if="favoriteJobs.length" class="job-chip-list">
            <button v-for="item in favoriteJobs" :key="item.id" type="button" @click="router.push(`/jobs#job-${item.job.id}`)">
              {{ item.job.title }}
            </button>
          </div>
          <p v-else class="notice">收藏后会显示在这里。</p>

          <div>
            <strong>通知</strong>
          </div>
          <div v-if="notifications.length" class="job-chip-list">
            <button v-for="item in notifications" :key="item.id" type="button" @click="router.push(item.relatedApplicationId ? `/messages/${item.relatedApplicationId}` : '/workbench')">
              {{ item.title }}
            </button>
          </div>

          <div>
            <strong>最近浏览</strong>
          </div>
          <div v-if="historyJobs.length" class="job-chip-list">
            <button v-for="item in historyJobs" :key="item.id" type="button" @click="router.push(`/jobs#job-${item.id}`)">
              {{ item.title }}
            </button>
          </div>
        </aside>
      </div>

      <div v-if="loading" class="job-list">
        <article v-for="item in 4" :key="item" class="job-item loading-card">
          <div></div>
          <strong></strong>
          <p></p>
        </article>
      </div>

      <div v-else class="job-list">
        <article v-for="job in pagedJobs" :id="`job-${job.id}`" :key="job.id" class="job-item" @mouseenter="recordHistory(job)">
          <div class="job-main">
            <span class="job-type-tag">{{ job.jobType || '校招' }}</span>
            <h3>{{ job.title }}</h3>
            <p class="job-meta">{{ job.company }} · {{ job.city }}</p>
            <small class="job-desc">{{ job.description }}</small>
            <details class="job-detail">
              <summary>查看任职要求</summary>
              <p>{{ job.requirementText || '暂无详细要求' }}</p>
            </details>
          </div>
          <div class="job-side">
            <strong class="salary">{{ salaryText(job) }}</strong>
            <span class="edu">{{ job.educationRequirement }}</span>
            <button class="admin-ghost job-chat-btn" type="button" @click="toggleFavorite(job)">
              {{ isFavorite(job) ? '取消收藏' : '收藏岗位' }}
            </button>
            <button class="admin-ghost job-chat-btn" type="button" @click="toggleCompare(job)">
              {{ compareIds.includes(job.id) ? '移出对比' : '加入对比' }}
            </button>
            <button class="apply-btn" type="button" @click="openApply(job)">投递岗位</button>
            <button class="admin-ghost job-chat-btn" type="button" :disabled="chattingJobId === job.id" @click="startCompanyChat(job)">
              {{ chattingJobId === job.id ? '打开中...' : '和企业对话' }}
            </button>
            <button class="admin-ghost job-chat-btn" type="button" @click="reportJob(job)">举报岗位</button>
          </div>
        </article>

        <div v-if="filteredJobs.length === 0" class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <p>没有找到匹配的职位，试着调整筛选条件。</p>
        </div>

        <nav v-else class="pagination" aria-label="职位分页">
          <span>共 {{ filteredJobs.length }} 个岗位</span>
          <div>
            <button type="button" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">上一页</button>
            <button
              v-for="page in pageNumbers"
              :key="page"
              type="button"
              :class="{ active: page === currentPage }"
              @click="changePage(page)"
            >
              {{ page }}
            </button>
            <button type="button" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">下一页</button>
          </div>
        </nav>
      </div>

      <section v-if="compareJobs.length" class="compare-panel">
        <div class="admin-panel-head">
          <div>
            <h2>岗位对比</h2>
            <p>最多对比 3 个岗位，适合快速筛选。</p>
          </div>
        </div>
        <div class="compare-grid">
          <article v-for="job in compareJobs" :key="job.id" class="compare-card">
            <strong>{{ job.title }}</strong>
            <p>{{ job.company }} · {{ job.city }}</p>
            <span>{{ salaryText(job) }}</span>
            <small>{{ job.educationRequirement }}</small>
          </article>
        </div>
      </section>

      <p v-if="notice" class="notice">{{ notice }}</p>
    </section>

    <div v-if="selectedJob" class="modal-backdrop" @click.self="closeApply">
      <section class="apply-modal" role="dialog" aria-modal="true" aria-labelledby="apply-title">
        <div class="admin-panel-head">
          <div>
            <h2 id="apply-title">投递岗位</h2>
            <p>{{ selectedJob.company }} · {{ selectedJob.title }}</p>
          </div>
          <button class="admin-ghost" type="button" @click="closeApply">关闭</button>
        </div>
        <form class="apply-form" @submit.prevent="submitApplication">
          <label>
            <span>投递留言</span>
            <textarea v-model="coverLetter" maxlength="1000" required></textarea>
          </label>
          <button class="login-submit" :disabled="submitting">{{ submitting ? '提交中...' : '确认投递' }}</button>
        </form>
        <p v-if="applyMessage" class="notice">{{ applyMessage }}</p>
      </section>
    </div>
  </div>
</template>
