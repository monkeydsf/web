<script setup>
import { computed, onMounted, ref } from 'vue'
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

const applicationChart = computed(() => {
  const pending = stats.value.pendingCount || 0
  const accepted = stats.value.acceptedCount || 0
  const viewed = stats.value.viewedCount || 0
  const other = Math.max((stats.value.applicationCount || 0) - pending - accepted - viewed, 0)
  return [
    { label: '待审核', value: pending, color: '#d97706' },
    { label: '已查看', value: viewed, color: '#0891b2' },
    { label: '已通过', value: accepted, color: '#059669' },
    { label: '其他', value: other, color: '#64748b' }
  ]
})

const funnelChart = computed(() => [
  { label: '发布岗位', value: stats.value.jobCount || 0, color: '#4a90e2' },
  { label: '收到投递', value: stats.value.applicationCount || 0, color: '#7c3aed' },
  { label: '进入审核', value: (stats.value.pendingCount || 0) + (stats.value.viewedCount || 0), color: '#d97706' },
  { label: '通过', value: stats.value.acceptedCount || 0, color: '#059669' }
])

const maxFunnelValue = computed(() => Math.max(...funnelChart.value.map(item => item.value), 1))

function chartPercent(value, total) {
  if (!total) return 0
  return Math.round((value / total) * 100)
}

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

    <div class="dashboard-chart-grid">
      <section class="dashboard-chart-card">
        <div class="dashboard-chart-head">
          <h3>投递状态分布</h3>
          <span>{{ loading ? '加载中' : `${stats.applicationCount} 份投递` }}</span>
        </div>
        <div class="status-bars">
          <div v-for="item in applicationChart" :key="item.label" class="status-bar-row">
            <div class="status-bar-meta">
              <span>{{ item.label }}</span>
              <strong>{{ loading ? '...' : item.value }}</strong>
            </div>
            <div class="status-bar-track">
              <div class="status-bar-fill" :style="{ width: chartPercent(item.value, stats.applicationCount) + '%', background: item.color }"></div>
            </div>
          </div>
        </div>
      </section>

      <section class="dashboard-chart-card">
        <div class="dashboard-chart-head">
          <h3>招聘转化概览</h3>
          <span>从岗位发布到通过</span>
        </div>
        <div class="metric-bar-chart">
          <div v-for="item in funnelChart" :key="item.label" class="metric-bar-item">
            <div class="metric-bar-wrap">
              <div class="metric-bar" :style="{ height: (loading ? 12 : Math.max((item.value / maxFunnelValue) * 120, item.value ? 16 : 4)) + 'px', background: item.color }"></div>
            </div>
            <strong>{{ loading ? '...' : item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
