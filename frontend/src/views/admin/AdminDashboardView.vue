<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'

const stats = ref({
  studentCount: 0, openJobCount: 0, applicationCount: 0,
  pendingCount: 0, acceptedCount: 0, unreadNotificationCount: 0,
  interviewCount: 0, reportCount: 0, favoriteCount: 0
})
const loading = ref(true)

const statItems = [
  { key: 'studentCount', label: '求职者', icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2', color: '#4a90e2' },
  { key: 'openJobCount', label: '在招岗位', icon: 'M20 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z', color: '#059669' },
  { key: 'applicationCount', label: '投递总数', icon: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z', color: '#7c3aed' },
  { key: 'pendingCount', label: '待审核', icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z', color: '#d97706' },
  { key: 'acceptedCount', label: '已通过', icon: 'M22 11.08V12a10 10 0 1 1-5.93-9.14', color: '#059669' },
  { key: 'unreadNotificationCount', label: '未读通知', icon: 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9', color: '#dc2626' },
  { key: 'interviewCount', label: '面试安排', icon: 'M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z', color: '#0891b2' },
  { key: 'reportCount', label: '岗位举报', icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z', color: '#e11d48' },
  { key: 'favoriteCount', label: '收藏岗位', icon: 'M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z', color: '#4a90e2' }
]

async function loadStats() {
  loading.value = true
  try { stats.value = await api('/dashboard/stats') } catch {} finally { loading.value = false }
}

onMounted(loadStats)
</script>

<template>
  <div class="adm-page">
    <div class="adm-page-head">
      <div>
        <h2>概览</h2>
        <span>平台运营数据一览</span>
      </div>
    </div>
    <div class="adm-stats-grid">
      <div v-for="item in statItems" :key="item.key" class="adm-stat-card">
        <div class="asc-icon" :style="{ background: item.color + '14', color: item.color }">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path :d="item.icon"/></svg>
        </div>
        <div class="asc-info">
          <span>{{ item.label }}</span>
          <strong>{{ loading ? '...' : stats[item.key] }}</strong>
        </div>
      </div>
    </div>
  </div>
</template>
