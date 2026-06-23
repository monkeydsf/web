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
  { role: 'assistant', content: '你好，我是职策对话咨询。你可以问简历、岗位选择、面试准备、投递节奏和 Offer 判断。' }
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
  nextTick(() => { if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight })
}

function buildPrompt(question) {
  const history = messages.value.slice(-8).map((m) => `${m.role === 'user' ? '学生' : '职策'}：${m.content}`).join('\n')
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
  input.value = ''; error.value = ''; loading.value = true; scrollToBottom()
  try {
    const data = await api('/ai/prompt-test', {
      method: 'POST',
      body: JSON.stringify({ prompt: buildPrompt(question) }),
      timeout: 70000
    })
    messages.value.push({ role: 'assistant', content: data.content || 'AI 没有返回有效内容，请换个问法再试。' })
  } catch (err) {
    error.value = err.message || '咨询失败，请检查后端和百炼 API Key。'
  } finally { loading.value = false; scrollToBottom() }
}

function useQuickPrompt(prompt) { input.value = prompt; sendMessage(prompt) }
</script>

<template>
  <div class="tool-v2">
    <section class="tool-hero">
      <div class="tool-hero-inner">
        <div class="tool-hero-left">
          <h1>求职咨询</h1>
          <p>多轮对话，解决求职路上的各种疑问</p>
        </div>
        <button type="button" class="tool-back" @click="router.push('/workbench')">返回工作台</button>
      </div>
    </section>

    <section class="tool-content">
      <div class="consult-v2">
        <!-- Quick prompts -->
        <aside class="cv2-side">
          <div class="cv2-side-header">
            <h3>快捷问题</h3>
          </div>
          <button
            v-for="prompt in quickPrompts"
            :key="prompt"
            class="cv2-quick"
            type="button"
            :disabled="loading"
            @click="useQuickPrompt(prompt)"
          >{{ prompt }}</button>
        </aside>

        <!-- Chat -->
        <main class="cv2-chat">
          <div ref="chatBody" class="cv2-messages">
            <div v-for="(message, index) in messages" :key="index" class="cv2-msg" :class="message.role">
              <div v-if="message.role === 'assistant'" class="cv2-msg-avatar">AI</div>
              <div class="cv2-msg-bubble markdown-result" v-html="renderMarkdown(message.content)"></div>
            </div>
            <div v-if="loading" class="cv2-msg assistant">
              <div class="cv2-msg-avatar">AI</div>
              <div class="cv2-msg-bubble cv2-typing">正在整理建议...</div>
            </div>
          </div>

          <p v-if="error" class="tool-error">{{ error }}</p>

          <form class="cv2-input" @submit.prevent="sendMessage()">
            <input
              v-model="input"
              type="text"
              placeholder="输入你的求职问题..."
              @keydown.enter.exact.prevent="sendMessage()"
            />
            <button type="submit" :disabled="!canSend">发送</button>
          </form>
        </main>
      </div>
    </section>
  </div>
</template>
