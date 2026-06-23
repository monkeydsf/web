<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()
const applications = ref([])
const loading = ref(true)
const message = ref('')
const timelineOpen = ref(null)

const statusText = {
  PENDING: '待审核',
  REVIEWED: '已查看',
  ACCEPTED: '通过',
  REJECTED: '拒绝'
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '薪资面议'
  return `${job.salaryMin}-${job.salaryMax}${job.jobType === '实习' ? '/天' : '/月'}`
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
  <div class="student-page">
    <section class="student-hero">
      <div>
        <span class="page-tag">Applications</span>
        <h1>我的投递</h1>
        <p>查看已投岗位、投递留言和审核状态，未处理前可以撤回。</p>
      </div>
      <button class="admin-ghost" @click="loadApplications">刷新记录</button>
    </section>

    <section class="student-panel">
      <p v-if="loading" class="notice">加载中...</p>

      <div v-else-if="applications.length" class="application-cards">
        <article v-for="item in applications" :key="item.id" class="application-card">
          <div>
            <span :class="['status-pill', item.status.toLowerCase()]">{{ statusText[item.status] || item.status }}</span>
            <h2>{{ item.job.title }}</h2>
            <p>{{ item.job.company }} · {{ item.job.city }} · {{ salaryText(item.job) }}</p>
            <small>投递时间：{{ item.appliedAt?.slice(0, 16).replace('T', ' ') }}</small>
            <blockquote>{{ item.coverLetter || '暂无投递留言' }}</blockquote>
          </div>
          <div class="application-actions">
            <button class="admin-ghost" @click="router.push(`/messages/${item.id}`)">联系企业</button>
            <button class="admin-ghost" type="button" @click="timelineOpen = timelineOpen === item.id ? null : item.id">
              {{ timelineOpen === item.id ? '收起时间线' : '查看时间线' }}
            </button>
            <button class="admin-danger" @click="cancelApplication(item)">撤回投递</button>
          </div>
          <div v-if="timelineOpen === item.id" class="application-timeline">
            <div v-for="step in timeline(item)" :key="step.key" :class="['timeline-step', { done: step.done }]">
              <strong>{{ step.label }}</strong>
              <small>{{ step.time ? step.time.slice(0, 16).replace('T', ' ') : '未到达' }}</small>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <p>还没有投递记录，去职位页选择合适岗位开始投递。</p>
      </div>

      <p v-if="message" class="notice">{{ message }}</p>
    </section>
  </div>
</template>
