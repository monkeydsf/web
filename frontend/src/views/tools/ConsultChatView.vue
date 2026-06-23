<script setup>
import { computed, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api'
import { renderMarkdown } from '../../utils/markdown'

const router = useRouter()
const input = ref('')
const loading = ref(false)
const error = ref('')
const messages = ref([
  {
    role: 'assistant',
    content: '你好，我是职策对话咨询。你可以问简历、岗位选择、面试准备、投递节奏和 Offer 判断。'
  }
])
const chatBody = ref(null)

const quickPrompts = [
  '我不知道自己适合什么岗位，帮我分析一下。',
  '我准备投 Java 后端实习，接下来一周怎么安排？',
  '帮我判断这个岗位描述是否适合应届生。',
  '面试前 30 分钟我应该重点复习什么？'
]

const canSend = computed(() => input.value.trim() && !loading.value)

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}

function buildPrompt(question) {
  const history = messages.value
    .slice(-8)
    .map((message) => `${message.role === 'user' ? '学生' : '职策'}：${message.content}`)
    .join('\n')

  return `你是职策平台的求职咨询助手，请用中文回答，语气直接、具体、可执行。
回答要求：
1. 优先给行动建议，不要空泛鼓励
2. 如果信息不足，先说明需要补充哪些信息
3. 可以用 Markdown 分点输出

历史对话：
${history}

学生最新问题：
${question}`
}

async function sendMessage(text = input.value) {
  const question = text.trim()
  if (!question || loading.value) return

  messages.value.push({ role: 'user', content: question })
  input.value = ''
  error.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const data = await api('/ai/prompt-test', {
      method: 'POST',
      body: JSON.stringify({ prompt: buildPrompt(question) }),
      timeout: 70000
    })
    messages.value.push({
      role: 'assistant',
      content: data.content || 'AI 没有返回有效内容，请换个问法再试。'
    })
  } catch (err) {
    error.value = err.message || '咨询失败，请检查后端和百炼 API Key。'
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function useQuickPrompt(prompt) {
  input.value = prompt
  sendMessage(prompt)
}
</script>

<template>
  <div class="tool-workspace">
    <section class="tool-workspace-head">
      <div>
        <span class="page-tag">Career consult</span>
        <h1>对话咨询</h1>
        <p>围绕求职方向、简历、投递、面试和 Offer 做多轮提问，得到更贴近当前情况的建议。</p>
      </div>
      <button class="admin-ghost" type="button" @click="router.push('/workbench')">返回工作台</button>
    </section>

    <section class="consult-grid">
      <aside class="consult-side-panel">
        <div class="tool-panel-head">
          <h2>快捷咨询</h2>
          <p>选择一个问题开始，也可以直接输入自己的情况。</p>
        </div>
        <button
          v-for="prompt in quickPrompts"
          :key="prompt"
          class="consult-prompt-btn"
          type="button"
          :disabled="loading"
          @click="useQuickPrompt(prompt)"
        >
          {{ prompt }}
        </button>
      </aside>

      <section class="consult-chat-panel">
        <div ref="chatBody" class="consult-chat-body">
          <article
            v-for="(message, index) in messages"
            :key="index"
            :class="['consult-message', message.role]"
          >
            <span>{{ message.role === 'user' ? '我' : '职策' }}</span>
            <div class="markdown-result" v-html="renderMarkdown(message.content)"></div>
          </article>
          <article v-if="loading" class="consult-message assistant">
            <span>职策</span>
            <p class="consult-typing">正在整理建议...</p>
          </article>
        </div>

        <p v-if="error" class="notice">{{ error }}</p>

        <form class="consult-input-bar" @submit.prevent="sendMessage()">
          <textarea
            v-model="input"
            placeholder="输入你的求职问题，例如：我简历项目少，怎么投后端实习？"
            @keydown.enter.exact.prevent="sendMessage()"
          ></textarea>
          <button class="login-submit" type="submit" :disabled="!canSend">
            {{ loading ? '发送中...' : '发送' }}
          </button>
        </form>
      </section>
    </section>
  </div>
</template>
