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
const pageSize = 8
const favorites = ref([])
const historyJobs = ref([])
const cityOptions = ref([])
const jobTypeOptions = ref([])

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
const favoriteJobIds = computed(() => new Set(favorites.value.map((item) => item.job?.id)))

function loadLocalList(key) {
  try { return JSON.parse(localStorage.getItem(key) || '[]') } catch { return [] }
}

function saveLocalList(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

function recordHistory(job) {
  const list = loadLocalList('campus-career-job-history').filter((item) => item.id !== job.id)
  list.unshift({ id: job.id, title: job.title, company: job.company, city: job.city })
  historyJobs.value = list.slice(0, 5)
  saveLocalList('campus-career-job-history', historyJobs.value)
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '面议'
  return `${job.salaryMin}-${job.salaryMax}${job.jobType === '实习' ? '/天' : '/月'}`
}

function filterJobs() {
  let result = [...jobs.value]
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter((job) => [job.title, job.company, job.description].some((item) => item?.toLowerCase().includes(kw)))
  }
  if (city.value) {
    const c = city.value.toLowerCase()
    result = result.filter((job) => job.city.toLowerCase().includes(c))
  }
  if (jobType.value) {
    result = result.filter((job) => job.jobType === jobType.value)
  }
  filteredJobs.value = result
  currentPage.value = 1
}

function changePage(page) {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
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
    const matched = existing.find((item) => item.job?.id === job.id)
    if (matched) { router.push(`/messages/${matched.id}`); return }
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

function isFavorite(job) { return favoriteJobIds.value.has(job.id) }

async function loadFavorites() {
  const session = getSession()
  const role = session?.user?.role
  if (!session || (role !== 'JOB_SEEKER' && role !== 'STUDENT')) { favorites.value = []; return }
  try { favorites.value = await api('/favorites') } catch { favorites.value = [] }
}

async function loadJobs() {
  loading.value = true
  notice.value = ''
  try {
    const params = new URLSearchParams()
    params.set('status', 'OPEN')
    if (keyword.value) params.set('keyword', keyword.value)
    if (city.value) params.set('city', city.value)
    if (jobType.value) params.set('jobType', jobType.value)
    const data = await api(`/jobs?${params.toString()}`)
    jobs.value = Array.isArray(data) && data.length ? data : mockJobs
  } catch {
    jobs.value = mockJobs
  } finally {
    loading.value = false
    filteredJobs.value = [...jobs.value]
    currentPage.value = 1
    cityOptions.value = [...new Set(jobs.value.map((job) => job.city))].filter(Boolean)
    jobTypeOptions.value = [...new Set(jobs.value.map((job) => job.jobType))].filter(Boolean)
    historyJobs.value = loadLocalList('campus-career-job-history')
    await loadFavorites()
  }
}

onMounted(loadJobs)

const avatarColors = ['av-blue', 'av-green', 'av-purple', 'av-orange', 'av-teal']
function companyInitial(name) { return name ? name.charAt(0) : '·' }
function avatarColor(index) { return avatarColors[index % avatarColors.length] }
</script>

<template>
  <div class="jobs-v2">
    <!-- Hero -->
    <section class="jobs-hero-v2">
      <div class="jobs-hero-inner">
        <div class="jobs-hero-left">
          <h1>职位列表</h1>
          <p>浏览 {{ filteredJobs.length }} 个岗位，找到适合你的机会</p>
        </div>
        <div class="jobs-hero-stats">
          <div class="jhs-item">
            <strong>{{ favorites.length }}</strong>
            <span>已收藏</span>
          </div>
          <div class="jhs-item">
            <strong>{{ historyJobs.length }}</strong>
            <span>已浏览</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Filters -->
    <section class="jobs-filter-section">
      <form class="jobs-filter-bar" @submit.prevent="filterJobs">
        <div class="jfb-field">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input v-model="keyword" type="text" placeholder="搜索职位、公司、关键词" />
        </div>
        <div class="jfb-field">
          <select v-model="city">
            <option value="">全部城市</option>
            <option v-for="item in cityOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </div>
        <div class="jfb-field">
          <select v-model="jobType">
            <option value="">全部类型</option>
            <option v-for="item in jobTypeOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </div>
        <button class="jfb-search-btn" type="submit">筛选</button>
      </form>
    </section>

    <!-- Content -->
    <section class="jobs-content">
      <div class="jobs-layout-v2">
        <!-- Sidebar -->
        <aside class="jobs-sidebar">
          <div class="js-block">
            <strong>收藏岗位</strong>
            <div v-if="favorites.length" class="js-list">
              <button v-for="item in favorites" :key="item.id" type="button" class="js-item" @click="router.push(`/jobs#job-${item.job.id}`)">
                {{ item.job.title }}
              </button>
            </div>
            <p v-else class="js-empty">暂无收藏</p>
          </div>
          <div class="js-block">
            <strong>最近浏览</strong>
            <div v-if="historyJobs.length" class="js-list">
              <button v-for="item in historyJobs" :key="item.id" type="button" class="js-item" @click="router.push(`/jobs#job-${item.id}`)">
                {{ item.title }}
              </button>
            </div>
            <p v-else class="js-empty">暂无浏览记录</p>
          </div>
        </aside>

        <!-- Main -->
        <main class="jobs-main-v2">
          <div v-if="loading" class="jobs-loading">
            <div v-for="n in 4" :key="n" class="loading-row">
              <div class="loading-avatar"></div>
              <div class="loading-lines"><span></span><span style="width:60%"></span></div>
            </div>
          </div>

          <template v-else>
            <div class="jobs-list-v2">
              <div
                v-for="(job, idx) in pagedJobs"
                :key="job.id"
                class="job-row-v2"
                @mouseenter="recordHistory(job)"
              >
                <div class="jrv-avatar" :class="avatarColor(idx)">{{ companyInitial(job.company) }}</div>
                <div class="jrv-left">
                  <div class="jrv-head">
                    <h3>{{ job.title }}</h3>
                    <span class="jrv-type" :class="job.jobType === '实习' ? 'type-intern' : 'type-campus'">{{ job.jobType || '校招' }}</span>
                  </div>
                  <div class="jrv-info">
                    <span class="jrv-company">{{ job.company }}</span>
                    <span class="jrv-dot">·</span>
                    <span>{{ job.city }}</span>
                    <span class="jrv-dot">·</span>
                    <span>{{ job.educationRequirement }}</span>
                  </div>
                  <p class="jrv-desc">{{ job.description }}</p>
                </div>
                <div class="jrv-right">
                  <span class="jrv-salary">{{ salaryText(job) }}</span>
                  <div class="jrv-actions">
                    <button class="jrv-btn" type="button" @click.stop="toggleFavorite(job)" :title="isFavorite(job) ? '取消收藏' : '收藏'">
                      <svg width="15" height="15" viewBox="0 0 24 24" :fill="isFavorite(job) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                    </button>
                    <button class="jrv-apply-btn" type="button" @click.stop="openApply(job)">投递</button>
                    <button class="jrv-btn" type="button" :disabled="chattingJobId === job.id" @click.stop="startCompanyChat(job)" title="沟通">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="filteredJobs.length === 0" class="jobs-empty">
              <p>没有找到匹配的职位，换个关键词试试</p>
            </div>

            <nav v-if="totalPages > 1" class="jobs-pagination">
              <button type="button" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">上一页</button>
              <button v-for="page in pageNumbers" :key="page" type="button" :class="{ active: page === currentPage }" @click="changePage(page)">{{ page }}</button>
              <button type="button" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">下一页</button>
            </nav>
          </template>

          <p v-if="notice" class="jobs-notice">{{ notice }}</p>
        </main>
      </div>
    </section>

    <!-- Apply Modal -->
    <div v-if="selectedJob" class="modal-backdrop" @click.self="closeApply">
      <section class="apply-modal-v2" role="dialog" aria-modal="true">
        <div class="amv-head">
          <div>
            <h2>投递岗位</h2>
            <p>{{ selectedJob.company }} · {{ selectedJob.title }}</p>
          </div>
          <button class="amv-close" type="button" @click="closeApply">&times;</button>
        </div>
        <form class="amv-form" @submit.prevent="submitApplication">
          <label>
            <span>投递留言</span>
            <textarea v-model="coverLetter" maxlength="1000" required></textarea>
          </label>
          <button class="amv-submit" type="submit" :disabled="submitting">{{ submitting ? '提交中...' : '确认投递' }}</button>
        </form>
        <p v-if="applyMessage" class="amv-msg">{{ applyMessage }}</p>
      </section>
    </div>
  </div>
</template>
