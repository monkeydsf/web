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
  () => {
    input.value = ''
    result.value = ''
    error.value = ''
  }
)

async function generate() {
  if (!input.value.trim()) {
    error.value = '请先填写内容。'
    return
  }
  loading.value = true
  error.value = ''
  result.value = ''
  try {
    const data = await api('/ai/prompt-test', {
      method: 'POST',
      body: JSON.stringify({
        prompt: `${tool.value.template}\n\n${input.value.trim()}`
      }),
      timeout: 70000
    })
    result.value = data.content || 'AI 没有返回有效内容，请稍后重试。'
  } catch (err) {
    error.value = err.message || '生成失败，请检查后端和百炼 API Key。'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="tool-workspace">
    <section class="tool-workspace-head">
      <div>
        <span class="page-tag">{{ tool.tag }}</span>
        <h1>{{ tool.title }}</h1>
        <p>{{ tool.intro }}</p>
      </div>
      <button class="admin-ghost" type="button" @click="router.push('/workbench')">返回工作台</button>
    </section>

    <section class="prompt-tool-grid">
      <aside class="prompt-input-panel">
        <div class="tool-panel-head">
          <h2>{{ tool.inputLabel }}</h2>
          <p>内容越具体，生成结果越贴近你的真实求职场景。</p>
        </div>
        <textarea v-model="input" :placeholder="tool.placeholder"></textarea>
        <button class="login-submit" :disabled="loading" @click="generate">
          {{ loading ? '生成中...' : tool.button }}
        </button>
        <p v-if="error" class="notice">{{ error }}</p>
      </aside>

      <section class="prompt-result-panel">
        <div class="tool-panel-head">
          <h2>生成结果</h2>
          <p>可直接复制到简历、投递消息或求职计划里继续修改。</p>
        </div>
        <div class="markdown-result" v-html="renderedResult"></div>
      </section>
    </section>
  </div>
</template>
