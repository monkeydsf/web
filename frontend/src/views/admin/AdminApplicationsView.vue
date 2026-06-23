<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api'

const router = useRouter()
const applications = ref([])
const loading = ref(true)
const message = ref('')
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

const filteredApplications = computed(() => {
  if (activeTab.value === 'all') return applications.value
  return applications.value.filter(a => a.status === activeTab.value)
})

function countByStatus(status) {
  return applications.value.filter(a => a.status === status).length
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

async function loadApplications() {
  loading.value = true; message.value = ''
  try { applications.value = await api('/applications') } catch (e) { message.value = e.message || '投递加载失败' } finally { loading.value = false }
}

async function changeStatus(item, status) {
  message.value = ''
  try { await api(`/applications/${item.id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }); await loadApplications() } catch (e) { message.value = e.message || '更新失败' }
}

onMounted(loadApplications)
</script>

<template>
  <div class="adm-page">
    <div class="adm-page-head">
      <div>
        <h2>投递管理</h2>
        <span>审核投递并更新状态</span>
      </div>
      <button class="adm-btn-outline" @click="loadApplications">刷新</button>
    </div>

    <!-- Tabs -->
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

    <div v-else-if="!filteredApplications.length" class="adm-empty">
      <p>暂无该状态的投递记录</p>
    </div>

    <div v-else class="adm-list">
      <div v-for="item in filteredApplications" :key="item.id" class="adm-list-item adm-app-item">
        <div class="ali-left">
          <div class="adm-app-avatar">{{ (item.student.fullName || 'S').charAt(0) }}</div>
          <div class="ali-info">
            <strong>{{ item.student.fullName }} · {{ item.job.title }}</strong>
            <span>{{ item.student.major || '未填写' }} · {{ item.job.company }}</span>
            <p v-if="item.coverLetter" class="ali-letter">{{ item.coverLetter }}</p>
          </div>
        </div>
        <div class="ali-right">
          <span class="adm-badge" :style="{ background: statusConfig[item.status]?.bg, color: statusConfig[item.status]?.color }">{{ statusConfig[item.status]?.text }}</span>
          <span class="ali-time">{{ timeAgo(item.appliedAt) }}</span>
          <select class="adm-select" :value="item.status" @change="changeStatus(item, $event.target.value)">
            <option value="PENDING">待审核</option>
            <option value="REVIEWED">已查看</option>
            <option value="ACCEPTED">通过</option>
            <option value="REJECTED">拒绝</option>
          </select>
          <button class="adm-btn-sm" @click="router.push(`/messages/${item.id}`)">沟通</button>
        </div>
      </div>
    </div>

    <p v-if="message" class="adm-msg">{{ message }}</p>
  </div>
</template>
