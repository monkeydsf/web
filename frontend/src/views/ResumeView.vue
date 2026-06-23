<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()
const loading = ref(false)
const uploading = ref(false)
const analyzing = ref(false)
const message = ref('')
const savedAt = ref('')
const matchScore = ref(null)
const matchSummary = ref('')
const matchStrengths = ref([])
const matchGaps = ref([])
const matchSuggestions = ref([])
const resumeFile = ref({ fileName: '', fileUrl: '' })

const form = reactive({
  expectedCity: '', expectedPosition: '', expectedSalary: '',
  education: '', skills: '', projectExperience: '', portfolioUrl: ''
})

function applyResume(data) {
  Object.assign(form, {
    expectedCity: data.expectedCity || '', expectedPosition: data.expectedPosition || '',
    expectedSalary: data.expectedSalary || '', education: data.education || '',
    skills: data.skills || '', projectExperience: data.projectExperience || '',
    portfolioUrl: data.portfolioUrl || ''
  })
  resumeFile.value = { fileName: data.fileName || '', fileUrl: data.fileUrl || '' }
  savedAt.value = data.updatedAt ? data.updatedAt.slice(0, 16).replace('T', ' ') : ''
}

const resumeFileUrl = computed(() => {
  if (!resumeFile.value.fileUrl) return ''
  if (/^https?:\/\//i.test(resumeFile.value.fileUrl)) return resumeFile.value.fileUrl
  return `http://localhost:8087${resumeFile.value.fileUrl}`
})

async function loadResume() {
  loading.value = true; message.value = ''
  try { applyResume(await api('/resume')) } catch (e) { message.value = e.message || '简历加载失败' } finally { loading.value = false }
}

async function saveResume() {
  loading.value = true; message.value = ''
  try {
    const data = await api('/resume', { method: 'PUT', body: JSON.stringify(form), timeout: 5000 })
    applyResume(data); message.value = '简历已保存'
  } catch (e) { message.value = e.message || '保存失败' } finally { loading.value = false }
}

async function uploadResumeFile(event) {
  const selected = event.target.files?.[0]; message.value = ''
  if (!selected) return
  const allowed = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  if (!allowed.includes(selected.type) && !/\.(pdf|doc|docx)$/i.test(selected.name)) {
    message.value = '请上传 PDF、DOC 或 DOCX 格式的简历。'; event.target.value = ''; return
  }
  uploading.value = true
  try {
    const formData = new FormData(); formData.append('file', selected)
    const data = await api('/resume/file', { method: 'POST', body: formData, timeout: 20000 })
    applyResume(data); message.value = '简历附件已上传'
  } catch (e) { message.value = e.message || '简历附件上传失败' } finally { uploading.value = false; event.target.value = '' }
}

async function analyzeResume() {
  const jobId = Number(window.prompt('输入岗位 ID 进行匹配分析，留空取消：'))
  if (!jobId) return
  analyzing.value = true; message.value = ''
  try {
    const data = await api(`/ai/resume-match/${jobId}`, { method: 'POST', timeout: 7000 })
    matchScore.value = data.score; matchSummary.value = data.summary
    matchStrengths.value = data.strengths || []; matchGaps.value = data.gaps || []; matchSuggestions.value = data.suggestions || []
  } catch (e) { message.value = e.message || '匹配分析失败' } finally { analyzing.value = false }
}

onMounted(loadResume)
</script>

<template>
  <div class="resume-v2">
    <section class="rv2-hero">
      <div class="rv2-hero-inner">
        <div class="rv2-hero-left">
          <h1>我的简历</h1>
          <p>维护求职信息，投递前让内容完整</p>
        </div>
        <div class="rv2-hero-right">
          <span v-if="savedAt" class="rv2-saved">最近更新 {{ savedAt }}</span>
          <button type="button" class="rv2-ai-btn" @click="router.push('/tools/resume-review')">AI 改简历</button>
        </div>
      </div>
    </section>

    <section class="rv2-content">
      <div class="rv2-layout">
        <!-- File upload -->
        <div class="rv2-file">
          <div class="rv2-file-header">
            <h3>简历附件</h3>
            <label class="rv2-upload-btn">
              <input type="file" accept=".pdf,.doc,.docx" :disabled="uploading" @change="uploadResumeFile" />
              {{ uploading ? '上传中...' : '上传文件' }}
            </label>
          </div>
          <div v-if="resumeFile.fileUrl" class="rv2-file-card">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            <a :href="resumeFileUrl" target="_blank">{{ resumeFile.fileName }}</a>
          </div>
          <div v-else class="rv2-file-empty">
            <span>支持 PDF / DOC / DOCX，最大 10MB</span>
          </div>
        </div>

        <!-- Form -->
        <form class="rv2-form" @submit.prevent="saveResume">
          <div class="rv2-row">
            <div class="rv2-field">
              <label>期望城市</label>
              <input v-model="form.expectedCity" placeholder="杭州 / 上海" />
            </div>
            <div class="rv2-field">
              <label>期望岗位</label>
              <input v-model="form.expectedPosition" placeholder="Java 后端 / 前端" />
            </div>
            <div class="rv2-field">
              <label>期望薪资</label>
              <input v-model="form.expectedSalary" placeholder="8k-12k / 180元/天" />
            </div>
          </div>

          <div class="rv2-field">
            <label>作品链接</label>
            <input v-model="form.portfolioUrl" placeholder="GitHub / 作品集" />
          </div>

          <div class="rv2-field">
            <label>教育经历</label>
            <textarea v-model="form.education" rows="2" placeholder="学校、专业、主修课程"></textarea>
          </div>

          <div class="rv2-field">
            <label>技能特长</label>
            <textarea v-model="form.skills" rows="2" placeholder="编程语言、框架、数据库"></textarea>
          </div>

          <div class="rv2-field">
            <label>项目经历</label>
            <textarea v-model="form.projectExperience" rows="3" placeholder="项目背景、负责内容、技术栈和结果"></textarea>
          </div>

          <div class="rv2-form-actions">
            <button class="rv2-save-btn" type="submit" :disabled="loading">{{ loading ? '保存中...' : '保存简历' }}</button>
            <button class="rv2-analyze-btn" type="button" :disabled="analyzing" @click="analyzeResume">{{ analyzing ? '分析中...' : '岗位匹配分析' }}</button>
          </div>
        </form>

        <!-- Match result -->
        <div v-if="matchSummary" class="rv2-match">
          <div class="rv2-match-header">
            <h3>岗位匹配结果</h3>
            <div class="rv2-match-score">
              <strong>{{ matchScore }}</strong>
              <span>分</span>
            </div>
          </div>
          <p>{{ matchSummary }}</p>
          <div class="rv2-match-grid">
            <div class="rv2-match-col">
              <h4>优势</h4>
              <ul><li v-for="item in matchStrengths" :key="item">{{ item }}</li></ul>
            </div>
            <div class="rv2-match-col">
              <h4>缺口</h4>
              <ul><li v-for="item in matchGaps" :key="item">{{ item }}</li></ul>
            </div>
            <div class="rv2-match-col">
              <h4>建议</h4>
              <ul><li v-for="item in matchSuggestions" :key="item">{{ item }}</li></ul>
            </div>
          </div>
        </div>

        <p v-if="message" class="tool-error">{{ message }}</p>
      </div>
    </section>
  </div>
</template>
