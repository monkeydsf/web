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
const resumeFile = ref({
  fileName: '',
  fileUrl: ''
})

const form = reactive({
  expectedCity: '',
  expectedPosition: '',
  expectedSalary: '',
  education: '',
  skills: '',
  projectExperience: '',
  portfolioUrl: ''
})

function applyResume(data) {
  Object.assign(form, {
    expectedCity: data.expectedCity || '',
    expectedPosition: data.expectedPosition || '',
    expectedSalary: data.expectedSalary || '',
    education: data.education || '',
    skills: data.skills || '',
    projectExperience: data.projectExperience || '',
    portfolioUrl: data.portfolioUrl || ''
  })
  resumeFile.value = {
    fileName: data.fileName || '',
    fileUrl: data.fileUrl || ''
  }
  savedAt.value = data.updatedAt ? data.updatedAt.slice(0, 16).replace('T', ' ') : ''
}

const resumeFileUrl = computed(() => {
  if (!resumeFile.value.fileUrl) return ''
  if (/^https?:\/\//i.test(resumeFile.value.fileUrl)) return resumeFile.value.fileUrl
  return `http://localhost:8087${resumeFile.value.fileUrl}`
})

async function loadResume() {
  loading.value = true
  message.value = ''
  try {
    applyResume(await api('/resume'))
  } catch (error) {
    message.value = error.message || '简历加载失败'
  } finally {
    loading.value = false
  }
}

async function saveResume() {
  loading.value = true
  message.value = ''
  try {
    const data = await api('/resume', {
      method: 'PUT',
      body: JSON.stringify(form),
      timeout: 5000
    })
    applyResume(data)
    message.value = '简历已保存'
  } catch (error) {
    message.value = error.message || '保存失败'
  } finally {
    loading.value = false
  }
}

async function uploadResumeFile(event) {
  const selected = event.target.files?.[0]
  message.value = ''
  if (!selected) return

  const allowed = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  const allowedExtension = /\.(pdf|doc|docx)$/i.test(selected.name)
  if (!allowed.includes(selected.type) && !allowedExtension) {
    message.value = '请上传 PDF、DOC 或 DOCX 格式的简历。'
    event.target.value = ''
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selected)
    const data = await api('/resume/file', {
      method: 'POST',
      body: formData,
      timeout: 20000
    })
    applyResume(data)
    message.value = '简历附件已上传'
  } catch (error) {
    message.value = error.message || '简历附件上传失败'
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

async function analyzeResume() {
  const jobId = Number(window.prompt('输入岗位 ID 进行匹配分析，留空取消：'))
  if (!jobId) return
  analyzing.value = true
  message.value = ''
  try {
    const data = await api(`/ai/resume-match/${jobId}`, { method: 'POST', timeout: 7000 })
    matchScore.value = data.score
    matchSummary.value = data.summary
    matchStrengths.value = data.strengths || []
    matchGaps.value = data.gaps || []
    matchSuggestions.value = data.suggestions || []
  } catch (error) {
    message.value = error.message || '匹配分析失败'
  } finally {
    analyzing.value = false
  }
}

onMounted(loadResume)
</script>

<template>
  <div class="student-page">
    <section class="student-hero">
      <div>
        <span class="page-tag">Resume</span>
        <h1>我的简历</h1>
        <p>维护求职方向、教育经历、技能和项目经历，投递岗位前先让信息完整。</p>
      </div>
      <div class="student-hero-card">
        <span>最近更新</span>
        <strong>{{ savedAt || '暂无记录' }}</strong>
        <button class="admin-ghost" type="button" @click="router.push('/tools/resume-review')">AI 改简历</button>
      </div>
    </section>

    <section class="student-panel">
      <div class="resume-file-panel">
        <div>
          <h2>简历附件</h2>
          <p>上传正式版 PDF 或 Word 简历，用于投递前留档和后续 AI 修改。</p>
        </div>
        <label class="resume-file-upload">
          <input type="file" accept=".pdf,.doc,.docx" :disabled="uploading" @change="uploadResumeFile" />
          <span>{{ uploading ? '上传中...' : '上传 PDF / Word' }}</span>
        </label>
        <a v-if="resumeFile.fileUrl" class="resume-file-card" :href="resumeFileUrl" target="_blank" rel="noreferrer">
          <strong>{{ resumeFile.fileName }}</strong>
          <span>打开已上传文件</span>
        </a>
        <div v-else class="resume-file-empty">
          <strong>还没有上传附件</strong>
          <span>支持 .pdf、.doc、.docx，单个文件最大 10MB。</span>
        </div>
      </div>

      <form class="resume-form" @submit.prevent="saveResume">
        <label>
          <span>期望城市</span>
          <input v-model="form.expectedCity" placeholder="杭州 / 上海 / 深圳" />
        </label>
        <label>
          <span>期望岗位</span>
          <input v-model="form.expectedPosition" placeholder="Java 后端 / 前端开发" />
        </label>
        <label>
          <span>期望薪资</span>
          <input v-model="form.expectedSalary" placeholder="8k-12k / 180元每天" />
        </label>
        <label>
          <span>作品链接</span>
          <input v-model="form.portfolioUrl" placeholder="GitHub / 作品集 / 博客链接" />
        </label>
        <label class="span-full">
          <span>教育经历</span>
          <textarea v-model="form.education" placeholder="学校、专业、主修课程、获奖情况"></textarea>
        </label>
        <label class="span-full">
          <span>技能特长</span>
          <textarea v-model="form.skills" placeholder="编程语言、框架、数据库、工具、证书"></textarea>
        </label>
        <label class="span-full">
          <span>项目经历</span>
          <textarea v-model="form.projectExperience" placeholder="项目背景、负责内容、技术栈和结果"></textarea>
        </label>
        <button class="primary-link span-full" :disabled="loading">
          {{ loading ? '处理中...' : '保存简历' }}
        </button>
        <button class="admin-ghost span-full" type="button" :disabled="analyzing" @click="analyzeResume">
          {{ analyzing ? '分析中...' : '分析岗位匹配' }}
        </button>
      </form>

      <div v-if="matchSummary" class="ai-result-box">
        <strong>岗位匹配结果</strong>
        <div class="match-score-box">
          <span>匹配分</span>
          <strong>{{ matchScore }} 分</strong>
        </div>
        <p>{{ matchSummary }}</p>
        <div class="resume-match-columns">
          <div>
            <h3>优势</h3>
            <ul>
              <li v-for="item in matchStrengths" :key="item">{{ item }}</li>
            </ul>
          </div>
          <div>
            <h3>缺口</h3>
            <ul>
              <li v-for="item in matchGaps" :key="item">{{ item }}</li>
            </ul>
          </div>
          <div>
            <h3>建议</h3>
            <ul>
              <li v-for="item in matchSuggestions" :key="item">{{ item }}</li>
            </ul>
          </div>
        </div>
      </div>

      <p v-if="message" class="notice">{{ message }}</p>
    </section>
  </div>
</template>
