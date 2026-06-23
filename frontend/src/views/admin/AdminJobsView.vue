<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../../api'

const jobs = ref([])
const loading = ref(true)
const message = ref('')
const editingId = ref(null)
const form = reactive(emptyJob())

function emptyJob() {
  return {
    title: '',
    company: '',
    city: '',
    jobType: '实习',
    salaryMin: 0,
    salaryMax: 0,
    educationRequirement: '本科及以上',
    description: '',
    requirementText: '',
    status: 'OPEN'
  }
}

function resetForm() {
  editingId.value = null
  Object.assign(form, emptyJob())
}

async function loadJobs() {
  loading.value = true
  message.value = ''
  try {
    jobs.value = await api('/jobs')
  } catch (error) {
    message.value = error.message || '岗位加载失败'
  } finally {
    loading.value = false
  }
}

async function saveJob() {
  loading.value = true
  message.value = ''
  try {
    const path = editingId.value ? `/jobs/${editingId.value}` : '/jobs'
    const method = editingId.value ? 'PUT' : 'POST'
    await api(path, {
      method,
      body: JSON.stringify({
        ...form,
        salaryMin: Number(form.salaryMin),
        salaryMax: Number(form.salaryMax)
      })
    })
    resetForm()
    await loadJobs()
  } catch (error) {
    message.value = error.message || '保存失败'
  } finally {
    loading.value = false
  }
}

function editJob(job) {
  editingId.value = job.id
  Object.assign(form, {
    title: job.title,
    company: job.company,
    city: job.city,
    jobType: job.jobType || '实习',
    salaryMin: job.salaryMin || 0,
    salaryMax: job.salaryMax || 0,
    educationRequirement: job.educationRequirement || '本科及以上',
    description: job.description || '',
    requirementText: job.requirementText || '',
    status: job.status || 'OPEN'
  })
}

async function removeJob(job) {
  if (!window.confirm(`确认删除岗位：${job.title}？`)) return
  loading.value = true
  message.value = ''
  try {
    await api(`/jobs/${job.id}`, { method: 'DELETE' })
    await loadJobs()
  } catch (error) {
    message.value = error.message || '删除失败'
  } finally {
    loading.value = false
  }
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '薪资面议'
  return `${job.salaryMin}-${job.salaryMax}`
}

onMounted(loadJobs)
</script>

<template>
  <section class="admin-panel">
    <div class="admin-panel-head">
      <div>
        <h2>岗位管理</h2>
        <p>新增、编辑和删除岗位信息。</p>
      </div>
      <button class="admin-logout" @click="resetForm">清空表单</button>
    </div>

    <div class="admin-grid">
      <form class="admin-form" @submit.prevent="saveJob">
        <label><span>岗位名称</span><input v-model="form.title" required /></label>
        <label><span>企业名称</span><input v-model="form.company" required /></label>
        <label><span>城市</span><input v-model="form.city" required /></label>
        <label>
          <span>类型</span>
          <select v-model="form.jobType">
            <option>实习</option>
            <option>校招</option>
          </select>
        </label>
        <label><span>最低薪资</span><input v-model="form.salaryMin" type="number" /></label>
        <label><span>最高薪资</span><input v-model="form.salaryMax" type="number" /></label>
        <label><span>学历要求</span><input v-model="form.educationRequirement" /></label>
        <label>
          <span>状态</span>
          <select v-model="form.status">
            <option value="OPEN">开放</option>
            <option value="CLOSED">关闭</option>
          </select>
        </label>
        <label class="admin-span"><span>岗位描述</span><textarea v-model="form.description"></textarea></label>
        <label class="admin-span"><span>任职要求</span><textarea v-model="form.requirementText"></textarea></label>
        <button class="admin-logout" :disabled="loading">{{ editingId ? '保存修改' : '发布岗位' }}</button>
      </form>

      <div class="admin-panel">
        <div class="admin-panel-head">
          <div>
            <h2>岗位列表</h2>
            <p>{{ jobs.length }} 条记录</p>
          </div>
        </div>
        <div class="admin-table-list">
          <article v-for="job in jobs" :key="job.id">
            <div>
              <strong>{{ job.title }}</strong>
              <p>{{ job.company }} · {{ job.city }} · {{ job.jobType }} · {{ salaryText(job) }}</p>
            </div>
            <span :class="['admin-badge', job.status === 'OPEN' ? 'open' : 'closed']">
              {{ job.status === 'OPEN' ? '开放' : '关闭' }}
            </span>
            <button class="admin-ghost" @click="editJob(job)">编辑</button>
            <button class="admin-danger" @click="removeJob(job)">删除</button>
          </article>
        </div>
      </div>
    </div>

    <p v-if="message" class="notice">{{ message }}</p>
  </section>
</template>
