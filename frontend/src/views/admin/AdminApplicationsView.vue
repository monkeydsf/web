<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api'

const router = useRouter()
const applications = ref([])
const loading = ref(true)
const message = ref('')

async function loadApplications() {
  loading.value = true
  message.value = ''
  try {
    applications.value = await api('/applications')
  } catch (error) {
    message.value = error.message || '投递加载失败'
  } finally {
    loading.value = false
  }
}

async function changeStatus(item, status) {
  message.value = ''
  try {
    await api(`/applications/${item.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status })
    })
    await loadApplications()
  } catch (error) {
    message.value = error.message || '更新失败'
  }
}

onMounted(loadApplications)
</script>

<template>
  <section class="admin-panel">
    <div class="admin-panel-head">
      <div>
        <h2>投递管理</h2>
        <p>查看投递并更新审核状态。</p>
      </div>
      <button class="admin-logout" @click="loadApplications">刷新</button>
    </div>

    <div class="admin-table-list application-list">
      <article v-for="item in applications" :key="item.id">
        <div>
          <strong>{{ item.student.fullName }} · {{ item.job.title }}</strong>
          <p>{{ item.student.major || '未填写专业' }} · {{ item.job.company }} · {{ item.appliedAt?.slice(0, 10) }}</p>
          <small>{{ item.coverLetter || '暂无投递留言' }}</small>
        </div>
        <div class="admin-application-actions">
          <select :value="item.status" @change="changeStatus(item, $event.target.value)">
            <option value="PENDING">待审核</option>
            <option value="REVIEWED">已查看</option>
            <option value="ACCEPTED">通过</option>
            <option value="REJECTED">拒绝</option>
          </select>
          <button class="admin-ghost" type="button" @click="router.push(`/messages/${item.id}`)">沟通</button>
        </div>
      </article>
    </div>

    <p v-if="loading" class="notice">加载中...</p>
    <p v-if="message" class="notice">{{ message }}</p>
  </section>
</template>
