<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api'
import { renderMarkdown } from '../../utils/markdown'

const route = useRoute()
const router = useRouter()

const tool = computed(() => route.meta.tool || {
  title: '求职工具',
  tag: 'Career tool',
  intro: '输入你的情况，生成可执行建议。',
  inputLabel: '输入内容',
  placeholder: '请描述你的情况。',
  button: '生成建议',
  template: '请根据以下内容给出建议：'
})

const input = ref('')
const result = ref('')
const loading = ref(false)
const error = ref('')
const renderedResult = computed(() => renderMarkdown(result.value, '填写左侧内容后，点击生成按钮，这里会显示 AI 输出。'))

watch(
  () => route.fullPath,
  () => { input.value = ''; result.value = ''; error.value = '' }
)

async function generate() {
  if (!input.value.trim()) { error.value = '请先填写内容。'; return }
  loading.value = true; error.value = ''; result.value = ''
  try {
    const data = await api('/ai/prompt-test', {
      method: 'POST',
      body: JSON.stringify({ prompt: `${tool.value.template}\n\n${input.value.trim()}` }),
      timeout: 70000
    })
    result.value = data.content || 'AI 没有返回有效内容，请稍后重试。'
  } catch (err) {
    error.value = err.message || '生成失败，请检查后端和百炼 API Key。'
  } finally { loading.value = false }
}
</script>

<template>
  <div class="tool-v2">
    <section class="tool-hero">
      <div class="tool-hero-inner">
        <div class="tool-hero-left">
          <h1>{{ tool.title }}</h1>
          <p>{{ tool.intro }}</p>
        </div>
        <button type="button" class="tool-back" @click="router.push('/workbench')">返回工作台</button>
      </div>
    </section>

    <section class="tool-content">
      <div class="tool-grid">
        <aside class="tool-input">
          <div class="ti-header">
            <h3>{{ tool.inputLabel }}</h3>
            <span>内容越具体，生成越准确</span>
          </div>
          <textarea v-model="input" :placeholder="tool.placeholder"></textarea>
          <button class="ti-btn" :disabled="loading" @click="generate">
            {{ loading ? '生成中...' : tool.button }}
          </button>
          <p v-if="error" class="tool-error">{{ error }}</p>
        </aside>

        <main class="tool-result">
          <div class="tr-header">
            <h3>生成结果</h3>
            <span>可复制到简历或求职计划</span>
          </div>
          <div class="tr-body markdown-result" v-html="renderedResult"></div>
        </main>
      </div>
    </section>
  </div>
</template>
