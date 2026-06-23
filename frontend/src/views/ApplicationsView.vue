<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()
const applications = ref([])
const loading = ref(true)
const message = ref('')
const timelineOpen = ref(null)
const activeTab = ref('all')

const statusConfig = {
  PENDING: { text: '待审核', color: '#d97706', bg: '#fef3c7' },
  REVIEWED: { text: '已查看', color: '#2563eb', bg: '#dbeafe' },
  ACCEPTED: { text: '已通过', color: '#059669', bg: '#dcfce7' },
  REJECTED: { text: '已拒绝', color: '#dc2626', bg: '#fee2e2' }
}

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'PENDING', label: '待审核' },
  { key: 'REVIEWED', label: '已查看' },
  { key: 'ACCEPTED', label: '已通过' },
  { key: 'REJECTED', label: '已拒绝' }
]

const filteredApplications = ref([])

function applyFilter() {
  if (activeTab.value === 'all') {
    filteredApplications.value = applications.value
  } else {
    filteredApplications.value = applications.value.filter(a => a.status === activeTab.value)
  }
}

function switchTab(key) {
  activeTab.value = key
  applyFilter()
}

function countByStatus(status) {
  return applications.value.filter(a => a.status === status).length
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '面议'
  return `${job.salaryMin}-${job.salaryMax}${job.jobType === '实习' ? '/天' : '/月'}`
}

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  return Math.floor(diff / 86400) + '天前'
}

function companyInitial(name) {
  return name ? name.charAt(0) : '·'
}

const avatarColors = ['av-blue', 'av-green', 'av-purple', 'av-orange', 'av-teal']

function avatarColor(index) {
  return avatarColors[index % avatarColors.length]
}

function timeline(item) {
  return [
    { key: 'applied', label: '已投递', done: true, time: item.appliedAt },
    { key: 'reviewed', label: '已查看', done: !!item.studentReadAt || item.status !== 'PENDING', time: item.studentReadAt },
    { key: 'accepted', label: '通过', done: item.status === 'ACCEPTED', time: item.status === 'ACCEPTED' ? item.appliedAt : '' },
    { key: 'interview', label: '面试安排', done: !!item.interviewAt, time: item.interviewAt },
    { key: 'rejected', label: '拒绝', done: item.status === 'REJECTED', time: item.status === 'REJECTED' ? item.appliedAt : '' }
  ]
}

async function loadApplications() {
  loading.value = true
  message.value = ''
  try {
    applications.value = await api('/applications/mine')
    applyFilter()
  } catch (error) {
    message.value = error.message || '投递记录加载失败'
  } finally {
    loading.value = false
  }
}

async function cancelApplication(item) {
  if (!window.confirm(`确认撤回 ${item.job.title} 的投递吗？`)) return
  message.value = ''
  try {
    await api(`/applications/${item.id}/cancel`, { method: 'POST' })
    await loadApplications()
    message.value = '投递已撤回'
  } catch (error) {
    message.value = error.message || '撤回失败'
  }
}

onMounted(loadApplications)
</script>

<template>
  <div class="app-v2">
    <!-- Hero -->
    <section class="app-hero">
      <div class="app-hero-inner">
        <div class="app-hero-left">
          <h1>我的投递</h1>
          <p>查看投递状态，管理申请记录</p>
        </div>
        <div class="app-hero-stats">
          <div class="ahs-item">
            <strong>{{ applications.length }}</strong>
            <span>总投递</span>
          </div>
          <div class="ahs-item">
            <strong>{{ countByStatus('PENDING') }}</strong>
            <span>待审核</span>
          </div>
          <div class="ahs-item">
            <strong>{{ countByStatus('ACCEPTED') }}</strong>
            <span>已通过</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Content -->
    <section class="app-content">
      <div class="app-layout">
        <!-- Tabs -->
        <div class="app-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            class="app-tab"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
            <span v-if="tab.key !== 'all'" class="app-tab-count">{{ countByStatus(tab.key) }}</span>
          </button>
          <button type="button" class="app-refresh" @click="loadApplications">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
          </button>
        </div>

        <!-- List -->
        <div class="app-list">
          <div v-if="loading" class="app-loading">
            <div v-for="n in 3" :key="n" class="app-loading-card">
              <div class="alc-avatar"></div>
              <div class="alc-lines"><span></span><span style="width:60%"></span><span style="width:40%"></span></div>
            </div>
          </div>

          <template v-else>
            <div v-if="!filteredApplications.length" class="app-empty">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              <p>暂无投递记录</p>
              <span>去职位页选择合适岗位开始投递</span>
            </div>

            <div v-for="(item, idx) in filteredApplications" :key="item.id" class="app-card">
              <div class="ac-top">
                <div class="ac-left">
                  <div class="ac-avatar" :class="avatarColor(idx)">{{ companyInitial(item.job.company) }}</div>
                  <div class="ac-info">
                    <h3>{{ item.job.title }}</h3>
                    <div class="ac-meta">
                      <span class="ac-company">{{ item.job.company }}</span>
                      <span class="ac-dot">·</span>
                      <span>{{ item.job.city }}</span>
                      <span class="ac-dot">·</span>
                      <span>{{ salaryText(item.job) }}</span>
                    </div>
                  </div>
                </div>
                <span class="ac-status" :style="{ background: statusConfig[item.status]?.bg, color: statusConfig[item.status]?.color }">
                  {{ statusConfig[item.status]?.text || item.status }}
                </span>
              </div>

              <div v-if="item.coverLetter" class="ac-letter">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                <span>{{ item.coverLetter }}</span>
              </div>

              <div class="ac-bottom">
                <span class="ac-time">{{ timeAgo(item.appliedAt) }}</span>
                <div class="ac-actions">
                  <button type="button" class="ac-btn" @click="router.push(`/messages/${item.id}`)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                    联系企业
                  </button>
                  <button type="button" class="ac-btn" @click="timelineOpen = timelineOpen === item.id ? null : item.id">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    {{ timelineOpen === item.id ? '收起' : '时间线' }}
                  </button>
                  <button v-if="item.status === 'PENDING'" type="button" class="ac-btn ac-danger" @click="cancelApplication(item)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    撤回
                  </button>
                </div>
              </div>

              <!-- Timeline -->
              <div v-if="timelineOpen === item.id" class="ac-timeline">
                <div v-for="step in timeline(item)" :key="step.key" class="atl-step" :class="{ done: step.done }">
                  <div class="atl-dot"></div>
                  <div class="atl-info">
                    <strong>{{ step.label }}</strong>
                    <span>{{ step.time ? timeAgo(step.time) : '未到达' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>

        <p v-if="message" class="app-msg">{{ message }}</p>
      </div>
    </section>
  </div>
</template>
