<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../../api'

const reports = ref([])
const loading = ref(true)
const message = ref('')
const activeTab = ref('all')

const statusConfig = {
  OPEN: { text: '待处理', color: '#d97706', bg: '#fef3c7' },
  REVIEWED: { text: '已查看', color: '#2563eb', bg: '#dbeafe' },
  RESOLVED: { text: '已处理', color: '#059669', bg: '#dcfce7' },
  REJECTED: { text: '驳回', color: '#dc2626', bg: '#fee2e2' }
}

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'OPEN', label: '待处理' },
  { key: 'REVIEWED', label: '已查看' },
  { key: 'RESOLVED', label: '已处理' },
  { key: 'REJECTED', label: '驳回' }
]

const filteredReports = computed(() => {
  if (activeTab.value === 'all') return reports.value
  return reports.value.filter(r => r.status === activeTab.value)
})

function countByStatus(status) {
  return reports.value.filter(r => r.status === status).length
}

function switchTab(key) { activeTab.value = key }

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr); const now = new Date(); const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  return Math.floor(diff / 86400) + '天前'
}

async function loadReports() {
  loading.value = true; message.value = ''
  try { reports.value = await api('/reports') } catch (e) { message.value = e.message || '举报加载失败' } finally { loading.value = false }
}

async function changeStatus(item, status) {
  message.value = ''
  try { await api(`/reports/${item.id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }); await loadReports() } catch (e) { message.value = e.message || '状态更新失败' }
}

onMounted(loadReports)
</script>

<template>
  <div class="adm-page">
    <div class="adm-page-head">
      <div>
        <h2>举报处理</h2>
        <span>查看岗位举报并更新处理状态</span>
      </div>
      <button class="adm-btn-outline" @click="loadReports">刷新</button>
    </div>

    <div class="adm-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="adm-tab"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
        <span v-if="tab.key !== 'all'" class="adm-tab-count">{{ countByStatus(tab.key) }}</span>
      </button>
    </div>

    <div v-if="loading" class="adm-loading">
      <div v-for="n in 3" :key="n" class="adm-loading-row"><div class="adm-loading-avatar"></div><div class="adm-loading-lines"><span></span><span style="width:50%"></span></div></div>
    </div>

    <div v-else-if="!filteredReports.length" class="adm-empty">
      <p>暂无该状态的举报记录</p>
    </div>

    <div v-else class="adm-list">
      <div v-for="item in filteredReports" :key="item.id" class="adm-list-item">
        <div class="ali-left">
          <div class="adm-app-avatar rp-red">{{ (item.reporter.fullName || 'U').charAt(0) }}</div>
          <div class="ali-info">
            <strong>{{ item.job.title }} · {{ item.reason }}</strong>
            <span>{{ item.job.company }} · {{ item.reporter.fullName }}</span>
            <p v-if="item.detail" class="ali-letter">{{ item.detail }}</p>
          </div>
        </div>
        <div class="ali-right">
          <span class="adm-badge" :style="{ background: statusConfig[item.status]?.bg, color: statusConfig[item.status]?.color }">{{ statusConfig[item.status]?.text }}</span>
          <span class="ali-time">{{ timeAgo(item.createdAt) }}</span>
          <select class="adm-select" :value="item.status" @change="changeStatus(item, $event.target.value)">
            <option value="OPEN">待处理</option>
            <option value="REVIEWED">已查看</option>
            <option value="RESOLVED">已处理</option>
            <option value="REJECTED">驳回</option>
          </select>
        </div>
      </div>
    </div>

    <p v-if="message" class="adm-msg">{{ message }}</p>
  </div>
</template>
