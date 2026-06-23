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
    title: '', company: '', city: '', jobType: '实习',
    salaryMin: 0, salaryMax: 0, educationRequirement: '本科及以上',
    description: '', requirementText: '', status: 'OPEN'
  }
}

function resetForm() { editingId.value = null; Object.assign(form, emptyJob()) }

async function loadJobs() {
  loading.value = true; message.value = ''
  try { jobs.value = await api('/jobs') } catch (e) { message.value = e.message || '岗位加载失败' } finally { loading.value = false }
}

async function saveJob() {
  loading.value = true; message.value = ''
  try {
    const path = editingId.value ? `/jobs/${editingId.value}` : '/jobs'
    const method = editingId.value ? 'PUT' : 'POST'
    await api(path, { method, body: JSON.stringify({ ...form, salaryMin: Number(form.salaryMin), salaryMax: Number(form.salaryMax) }) })
    resetForm(); await loadJobs()
  } catch (e) { message.value = e.message || '保存失败' } finally { loading.value = false }
}

function editJob(job) {
  editingId.value = job.id
  Object.assign(form, {
    title: job.title, company: job.company, city: job.city,
    jobType: job.jobType || '实习', salaryMin: job.salaryMin || 0,
    salaryMax: job.salaryMax || 0, educationRequirement: job.educationRequirement || '本科及以上',
    description: job.description || '', requirementText: job.requirementText || '', status: job.status || 'OPEN'
  })
}

async function removeJob(job) {
  if (!window.confirm(`确认删除岗位：${job.title}？`)) return
  loading.value = true; message.value = ''
  try { await api(`/jobs/${job.id}`, { method: 'DELETE' }); await loadJobs() } catch (e) { message.value = e.message || '删除失败' } finally { loading.value = false }
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '面议'
  return `${job.salaryMin}-${job.salaryMax}`
}

onMounted(loadJobs)
</script>

<template>
  <div class="adm-page">
    <div class="adm-page-head">
      <div>
        <h2>岗位管理</h2>
        <span>新增、编辑和删除岗位</span>
      </div>
      <button class="adm-btn-outline" @click="resetForm">清空表单</button>
    </div>

    <div class="adm-grid-2">
      <!-- Form -->
      <form class="adm-form" @submit.prevent="saveJob">
        <div class="adm-form-row">
          <div class="adm-field"><label>岗位名称</label><input v-model="form.title" required /></div>
          <div class="adm-field"><label>企业名称</label><input v-model="form.company" required /></div>
        </div>
        <div class="adm-form-row">
          <div class="adm-field"><label>城市</label><input v-model="form.city" required /></div>
          <div class="adm-field">
            <label>类型</label>
            <select v-model="form.jobType"><option>实习</option><option>校招</option></select>
          </div>
        </div>
        <div class="adm-form-row">
          <div class="adm-field"><label>最低薪资</label><input v-model="form.salaryMin" type="number" /></div>
          <div class="adm-field"><label>最高薪资</label><input v-model="form.salaryMax" type="number" /></div>
        </div>
        <div class="adm-form-row">
          <div class="adm-field"><label>学历要求</label><input v-model="form.educationRequirement" /></div>
          <div class="adm-field">
            <label>状态</label>
            <select v-model="form.status"><option value="OPEN">开放</option><option value="CLOSED">关闭</option></select>
          </div>
        </div>
        <div class="adm-field"><label>岗位描述</label><textarea v-model="form.description" rows="3"></textarea></div>
        <div class="adm-field"><label>任职要求</label><textarea v-model="form.requirementText" rows="3"></textarea></div>
        <button class="adm-btn-primary" type="submit" :disabled="loading">{{ editingId ? '保存修改' : '发布岗位' }}</button>
      </form>

      <!-- List -->
      <div class="adm-list-panel">
        <div class="adm-list-head">
          <h3>岗位列表</h3>
          <span>{{ jobs.length }} 条</span>
        </div>
        <div class="adm-list">
          <div v-for="job in jobs" :key="job.id" class="adm-list-item">
            <div class="ali-info">
              <strong>{{ job.title }}</strong>
              <span>{{ job.company }} · {{ job.city }} · {{ job.jobType }}</span>
            </div>
            <div class="ali-right">
              <span class="ali-salary">{{ salaryText(job) }}</span>
              <span class="adm-badge" :class="job.status === 'OPEN' ? 'badge-green' : 'badge-red'">{{ job.status === 'OPEN' ? '开放' : '关闭' }}</span>
              <button class="adm-btn-sm" @click="editJob(job)">编辑</button>
              <button class="adm-btn-sm adm-danger" @click="removeJob(job)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <p v-if="message" class="adm-msg">{{ message }}</p>
  </div>
</template>
