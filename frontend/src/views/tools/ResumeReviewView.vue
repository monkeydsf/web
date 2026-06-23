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
  error.value = ''; result.value = ''
  if (!selected) { file.value = null; return }
  const allowed = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  const allowedExtension = /\.(pdf|doc|docx)$/i.test(selected.name)
  if (!allowed.includes(selected.type) && !allowedExtension) {
    error.value = '请上传 PDF、DOC 或 DOCX 格式的简历。'
    event.target.value = ''; file.value = null; return
  }
  file.value = selected
}

function clearFile() { file.value = null; result.value = ''; error.value = '' }

async function sendToAi() {
  if (!file.value) { error.value = '请先上传简历文件。'; return }
  sending.value = true; error.value = ''; result.value = ''
  const formData = new FormData()
  formData.append('file', file.value)
  formData.append('prompt', prompt.value)
  try {
    const data = await api('/ai/resume-review', {
      method: 'POST', body: formData, timeout: 70000
    })
    result.value = data.content || 'AI 没有返回有效内容，请稍后重试。'
  } catch (err) {
    error.value = err.message || 'AI 调用失败，请检查后端 API Key 配置。'
  } finally { sending.value = false }
}
</script>

<template>
  <div class="tool-v2">
    <section class="tool-hero">
      <div class="tool-hero-inner">
        <div class="tool-hero-left">
          <h1>AI 改简历</h1>
          <p>上传简历，获取结构、表达和岗位匹配建议</p>
        </div>
        <button type="button" class="tool-back" @click="router.push('/workbench')">返回工作台</button>
      </div>
    </section>

    <section class="tool-content">
      <div class="tool-grid">
        <aside class="tool-input">
          <div class="ti-header">
            <h3>上传文件</h3>
            <span>支持 PDF、DOC、DOCX</span>
          </div>

          <label class="ti-dropzone">
            <input type="file" accept=".pdf,.doc,.docx" @change="handleFileChange" />
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            <strong>选择简历文件</strong>
            <span>点击或拖拽上传</span>
          </label>

          <div v-if="file" class="ti-file">
            <div class="tif-info">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              <div>
                <strong>{{ file.name }}</strong>
                <span>{{ fileSize }}</span>
              </div>
            </div>
            <button type="button" class="ti-file-remove" @click="clearFile">&times;</button>
          </div>

          <div class="ti-field">
            <label>修改要求</label>
            <textarea v-model="prompt" rows="3"></textarea>
          </div>

          <button class="ti-btn" :disabled="sending" @click="sendToAi">
            {{ sending ? '发送中...' : '发送给 AI' }}
          </button>
          <p v-if="error" class="tool-error">{{ error }}</p>
        </aside>

        <main class="tool-result">
          <div class="tr-header">
            <h3>AI 分析结果</h3>
          </div>
          <div class="tr-body markdown-result" v-html="renderedResult"></div>
        </main>
      </div>
    </section>
  </div>
</template>
