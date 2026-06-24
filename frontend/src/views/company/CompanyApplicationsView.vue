<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api'

const router = useRouter()
const applications = ref([])
const loading = ref(true)
const message = ref('')

const statusConfig = {
  PENDING: { text: '待审核', color: '#d97706', bg: '#fef3c7' },
  REVIEWED: { text: '已查看', color: '#2563eb', bg: '#dbeafe' },
  ACCEPTED: { text: '已通过', color: '#059669', bg: '#dcfce7' },
  REJECTED: { text: '已拒绝', color: '#dc2626', bg: '#fee2e2' }
}

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
  try { applications.value = await api('/applications/company') } catch (e) { message.value = e.message || '投递加载失败' } finally { loading.value = false }
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
        <h2>收到的投递</h2>
        <span>查看求职者投递并更新审核状态</span>
      </div>
      <button class="adm-btn-outline" @click="loadApplications">刷新</button>
    </div>

    <div v-if="loading" class="adm-loading">
      <div v-for="n in 3" :key="n" class="adm-loading-row"><div class="adm-loading-avatar"></div><div class="adm-loading-lines"><span></span><span style="width:50%"></span></div></div>
    </div>

    <div v-else-if="!applications.length" class="adm-empty-state">
      <p>暂无投递记录</p>
      <span>求职者投递后会显示在这里</span>
    </div>

    <div v-else class="adm-list">
      <div v-for="item in applications" :key="item.id" class="adm-list-item">
        <div class="ali-info">
          <strong>{{ item.student.fullName }} · {{ item.job.title }}</strong>
          <span>{{ item.student.major || '未填写' }} · {{ item.job.company }}</span>
          <p v-if="item.coverLetter" class="ali-letter">{{ item.coverLetter }}</p>
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
