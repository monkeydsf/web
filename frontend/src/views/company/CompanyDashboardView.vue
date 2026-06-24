<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'

const stats = ref({ jobCount: 0, applicationCount: 0, pendingCount: 0, acceptedCount: 0, viewedCount: 0 })
const loading = ref(true)

const statItems = [
  { key: 'jobCount', label: '我的岗位', color: '#4a90e2' },
  { key: 'applicationCount', label: '收到投递', color: '#7c3aed' },
  { key: 'pendingCount', label: '待审核', color: '#d97706' },
  { key: 'acceptedCount', label: '已通过', color: '#059669' },
  { key: 'viewedCount', label: '已查看', color: '#0891b2' }
]

async function loadStats() {
  loading.value = true
  try {
    const jobs = await api('/jobs/mine')
    const apps = await api('/applications/company')
    stats.value = {
      jobCount: Array.isArray(jobs) ? jobs.length : 0,
      applicationCount: Array.isArray(apps) ? apps.length : 0,
      pendingCount: Array.isArray(apps) ? apps.filter(a => a.status === 'PENDING').length : 0,
      acceptedCount: Array.isArray(apps) ? apps.filter(a => a.status === 'ACCEPTED').length : 0,
      viewedCount: Array.isArray(apps) ? apps.filter(a => a.status === 'REVIEWED').length : 0
    }
  } catch {} finally { loading.value = false }
}

onMounted(loadStats)
</script>

<template>
  <div class="adm-page">
    <div class="adm-page-head">
      <h2>企业概览</h2>
      <span>查看岗位和投递数据</span>
    </div>
    <div class="adm-stats-grid">
      <div v-for="item in statItems" :key="item.key" class="adm-stat-card">
        <div class="asc-dot" :style="{ background: item.color }"></div>
        <span>{{ item.label }}</span>
        <strong>{{ loading ? '...' : stats[item.key] }}</strong>
      </div>
    </div>
  </div>
</template>
