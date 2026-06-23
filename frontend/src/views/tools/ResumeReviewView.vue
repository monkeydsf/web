<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api'
import { renderMarkdown } from '../../utils/markdown'

const router = useRouter()
const file = ref(null)
const prompt = ref('请从岗位匹配度、项目经历表达、量化成果和排版结构四个方面帮我修改简历。')
const sending = ref(false)
const result = ref('')
const error = ref('')

const fileSize = computed(() => {
  if (!file.value) return ''
  const kb = file.value.size / 1024
  return kb > 1024 ? `${(kb / 1024).toFixed(1)} MB` : `${kb.toFixed(0)} KB`
})
const renderedResult = computed(() => renderMarkdown(result.value, '上传简历并发送后，这里会显示 AI 返回的修改建议。'))

function handleFileChange(event) {
  const selected = event.target.files?.[0]
  error.value = ''
  result.value = ''
  if (!selected) {
    file.value = null
    return
  }
  const allowed = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  const allowedExtension = /\.(pdf|doc|docx)$/i.test(selected.name)
  if (!allowed.includes(selected.type) && !allowedExtension) {
    error.value = '请上传 PDF、DOC 或 DOCX 格式的简历。'
    event.target.value = ''
    file.value = null
    return
  }
  file.value = selected
}

function clearFile() {
  file.value = null
  result.value = ''
  error.value = ''
}

async function sendToAi() {
  if (!file.value) {
    error.value = '请先上传简历文件。'
    return
  }
  sending.value = true
  error.value = ''
  result.value = ''
  const formData = new FormData()
  formData.append('file', file.value)
  formData.append('prompt', prompt.value)
  try {
    const data = await api('/ai/resume-review', {
      method: 'POST',
      body: formData,
      timeout: 70000
    })
    result.value = data.content || 'AI 没有返回有效内容，请稍后重试。'
  } catch (err) {
    error.value = err.message || 'AI 调用失败，请检查后端 API Key 配置。'
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <div class="tool-workspace">
    <section class="tool-workspace-head">
      <div>
        <span class="page-tag">AI resume review</span>
        <h1>改简历</h1>
        <p>上传简历文件，把文件和修改要求发送给 AI，获取结构、表达和岗位匹配建议。</p>
      </div>
      <button class="admin-ghost" type="button" @click="router.push('/workbench')">返回工作台</button>
    </section>

    <section class="resume-review-grid">
      <aside class="upload-panel">
        <div class="tool-panel-head">
          <h2>上传文件</h2>
          <p>支持 PDF、DOC、DOCX，建议上传最新版本。</p>
        </div>

        <label class="resume-dropzone">
          <input type="file" accept=".pdf,.doc,.docx" @change="handleFileChange" />
          <span>选择简历文件</span>
          <small>文件会发送到后端，再由后端调用百炼平台。</small>
        </label>

        <article v-if="file" class="uploaded-file-card">
          <div>
            <strong>{{ file.name }}</strong>
            <p>{{ fileSize }}</p>
          </div>
          <button class="admin-ghost" type="button" @click="clearFile">移除</button>
        </article>

        <p v-if="error" class="notice">{{ error }}</p>
      </aside>

      <section class="ai-send-panel">
        <div class="tool-panel-head">
          <h2>发送给 AI</h2>
          <p>填写你的修改目标，AI 会按目标给出简历优化建议。</p>
        </div>

        <label class="ai-prompt-box">
          <span>修改要求</span>
          <textarea v-model="prompt"></textarea>
        </label>

        <button class="login-submit" :disabled="sending" @click="sendToAi">
          {{ sending ? '发送中...' : '发送文件给 AI' }}
        </button>

        <div class="ai-result-box">
          <strong>AI 分析结果</strong>
          <div class="markdown-result" v-html="renderedResult"></div>
        </div>
      </section>
    </section>
  </div>
</template>
