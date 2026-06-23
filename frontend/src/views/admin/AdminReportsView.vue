<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'

const reports = ref([])
const loading = ref(true)
const message = ref('')

async function loadReports() {
  loading.value = true
  message.value = ''
  try {
    reports.value = await api('/reports')
  } catch (error) {
    message.value = error.message || '举报加载失败'
  } finally {
    loading.value = false
  }
}

async function changeStatus(item, status) {
  try {
    await api(`/reports/${item.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status })
    })
    await loadReports()
  } catch (error) {
    message.value = error.message || '状态更新失败'
  }
}

onMounted(loadReports)
</script>

<template>
  <section class="admin-panel">
    <div class="admin-panel-head">
      <div>
        <h2>举报处理</h2>
        <p>查看岗位举报并更新处理状态。</p>
      </div>
      <button class="admin-logout" @click="loadReports">刷新</button>
    </div>

    <div class="admin-table-list">
      <article v-for="item in reports" :key="item.id">
        <div>
          <strong>{{ item.job.title }} · {{ item.reason }}</strong>
          <p>{{ item.job.company }} · {{ item.reporter.fullName }} · {{ item.createdAt?.slice(0, 10) }}</p>
          <small>{{ item.detail || '暂无补充信息' }}</small>
        </div>
        <select :value="item.status" @change="changeStatus(item, $event.target.value)">
          <option value="OPEN">待处理</option>
          <option value="REVIEWED">已查看</option>
          <option value="RESOLVED">已处理</option>
          <option value="REJECTED">驳回</option>
        </select>
      </article>
    </div>

    <p v-if="loading" class="notice">加载中...</p>
    <p v-if="message" class="notice">{{ message }}</p>
  </section>
</template>
