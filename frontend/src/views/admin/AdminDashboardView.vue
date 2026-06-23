<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'

const stats = ref({
  studentCount: 0,
  openJobCount: 0,
  applicationCount: 0,
  pendingCount: 0,
  acceptedCount: 0,
  unreadNotificationCount: 0,
  interviewCount: 0,
  reportCount: 0,
  favoriteCount: 0
})
const loading = ref(true)
const message = ref('')

async function loadStats() {
  loading.value = true
  message.value = ''
  try {
    stats.value = await api('/dashboard/stats')
  } catch (error) {
    message.value = error.message || '概览加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<template>
  <section class="admin-panel">
    <div class="admin-panel-head">
      <h2>概览</h2>
      <p>查看岗位、投递和待处理事项的汇总信息。</p>
    </div>
    <div class="admin-summary-grid">
      <article class="admin-summary-card">
        <span>求职者</span>
        <strong>{{ loading ? '...' : stats.studentCount }}</strong>
      </article>
      <article class="admin-summary-card">
        <span>在招岗位</span>
        <strong>{{ loading ? '...' : stats.openJobCount }}</strong>
      </article>
      <article class="admin-summary-card">
        <span>投递总数</span>
        <strong>{{ loading ? '...' : stats.applicationCount }}</strong>
      </article>
      <article class="admin-summary-card">
        <span>待审核</span>
        <strong>{{ loading ? '...' : stats.pendingCount }}</strong>
      </article>
      <article class="admin-summary-card">
        <span>已通过</span>
        <strong>{{ loading ? '...' : stats.acceptedCount }}</strong>
      </article>
      <article class="admin-summary-card">
        <span>未读通知</span>
        <strong>{{ loading ? '...' : stats.unreadNotificationCount }}</strong>
      </article>
      <article class="admin-summary-card">
        <span>面试安排</span>
        <strong>{{ loading ? '...' : stats.interviewCount }}</strong>
      </article>
      <article class="admin-summary-card">
        <span>岗位举报</span>
        <strong>{{ loading ? '...' : stats.reportCount }}</strong>
      </article>
      <article class="admin-summary-card">
        <span>收藏岗位</span>
        <strong>{{ loading ? '...' : stats.favoriteCount }}</strong>
      </article>
    </div>
    <p v-if="message" class="notice">{{ message }}</p>
  </section>
</template>
