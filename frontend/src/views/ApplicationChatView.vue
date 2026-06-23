<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, getSession } from '../api'

const route = useRoute()
const router = useRouter()
const applications = ref([])
const conversations = ref([])
const activeId = ref(route.params.id ? Number(route.params.id) : null)
const messages = ref([])
const input = ref('')
const loading = ref(true)
const loadingMessages = ref(false)
const sending = ref(false)
const message = ref('')
const chatBody = ref(null)

const currentUserId = computed(() => getSession()?.user?.id)
const isManager = computed(() => {
  const role = getSession()?.user?.role
  return role === 'COMPANY' || role === 'ADMIN'
})
const activeApplication = computed(() => applications.value.find(item => item.id === activeId.value) || null)
const canSend = computed(() => activeApplication.value && input.value.trim() && !sending.value)

function timeText(value) {
  return value ? value.slice(0, 16).replace('T', ' ') : ''
}

function contactName(item) {
  return isManager.value ? item.student?.fullName || item.student?.username || '学生' : item.job?.company || '企业'
}

function senderLabel(item) {
  if (item.sender?.id === currentUserId.value) return '我'
  if (item.sender?.role === 'COMPANY' || item.sender?.role === 'ADMIN') return '企业'
  return item.sender?.fullName || '学生'
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}

async function loadMessages(id) {
  if (!id) {
    messages.value = []
    return
  }
  loadingMessages.value = true
  message.value = ''
  try {
    messages.value = await api(`/applications/${id}/messages`)
    scrollToBottom()
  } catch (error) {
    message.value = error.message || '沟通记录加载失败'
  } finally {
    loadingMessages.value = false
  }
}

async function loadApplications() {
  loading.value = true
  message.value = ''
  try {
    applications.value = await api(isManager.value ? '/applications' : '/applications/mine')
    conversations.value = await api('/conversations')
    if (!applications.value.length) {
      activeId.value = null
      messages.value = []
      return
    }
    const routeId = route.params.id ? Number(route.params.id) : null
    const target = applications.value.find(item => item.id === routeId) || applications.value[0]
    activeId.value = target.id
    await loadMessages(target.id)
  } catch (error) {
    message.value = error.message || '对话列表加载失败'
  } finally {
    loading.value = false
  }
}

function selectConversation(item) {
  if (item.id === activeId.value) return
  activeId.value = item.id
  router.replace(`/messages/${item.id}`)
  loadMessages(item.id)
}

async function sendMessage() {
  const content = input.value.trim()
  if (!content || sending.value || !activeId.value) return

  sending.value = true
  message.value = ''
  try {
    const data = await api(`/applications/${activeId.value}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content }),
      timeout: 8000
    })
    messages.value.push(data)
    input.value = ''
    scrollToBottom()
  } catch (error) {
    message.value = error.message || '发送失败'
  } finally {
    sending.value = false
  }
}

watch(() => route.params.id, (value) => {
  const id = value ? Number(value) : null
  if (id && id !== activeId.value && applications.value.some(item => item.id === id)) {
    activeId.value = id
    loadMessages(id)
  }
})

onMounted(loadApplications)
</script>

<template>
  <div class="student-page messages-page">
    <section class="student-hero">
      <div>
        <span class="page-tag">Messages</span>
        <h1>企业对话</h1>
        <p>左侧选择联系人，右侧围绕岗位投递、面试安排和补充材料继续沟通。</p>
      </div>
      <button class="admin-ghost" type="button" @click="loadApplications">刷新</button>
    </section>

    <section class="messages-shell">
      <aside class="message-contact-list">
        <div class="message-contact-head">
          <strong>联系人</strong>
          <span>{{ conversations.length }} 个会话</span>
        </div>

        <p v-if="loading" class="notice">加载中...</p>
        <button
          v-for="item in conversations"
          v-else
          :key="item.applicationId"
          type="button"
          :class="['message-contact-item', { active: item.applicationId === activeId }]"
          @click="selectConversation({ id: item.applicationId })"
        >
          <strong>{{ item.student?.fullName || item.student?.username || item.job?.company || '会话' }}</strong>
          <span>{{ item.job?.title }} · {{ item.job?.city }}</span>
          <small>
            {{ timeText(item.latestMessageAt) }}
            <span v-if="item.unread" class="status-pill pending">未读</span>
          </small>
        </button>

        <div v-if="!loading && !applications.length" class="message-empty-mini">
          暂无对话。可以先到职位介绍页选择岗位并点击“和企业对话”。
        </div>
      </aside>

      <section class="company-chat-panel message-main-panel">
        <div v-if="activeApplication" class="message-chat-head">
          <div>
            <strong>{{ contactName(activeApplication) }}</strong>
            <span>{{ activeApplication.job?.title }} · {{ activeApplication.job?.company }}</span>
          </div>
          <small>投递时间：{{ timeText(activeApplication.appliedAt) }}</small>
        </div>

        <p v-if="message" class="notice">{{ message }}</p>

        <div ref="chatBody" class="company-chat-body">
          <div v-if="loadingMessages" class="company-chat-empty">正在加载消息...</div>
          <div v-else-if="!activeApplication" class="company-chat-empty">请选择左侧联系人开始沟通。</div>
          <div v-else-if="!messages.length" class="company-chat-empty">
            还没有消息，可以先发送面试时间、补充材料或岗位问题。
          </div>
          <article
            v-for="item in messages"
            :key="item.id"
            :class="['company-message', { mine: item.sender?.id === currentUserId }]"
          >
            <span>{{ senderLabel(item) }} · {{ timeText(item.createdAt) }}</span>
            <p>{{ item.content }}</p>
          </article>
        </div>

        <form class="company-chat-input" @submit.prevent="sendMessage">
          <textarea
            v-model="input"
            maxlength="2000"
            :disabled="!activeApplication"
            placeholder="输入消息，例如：您好，我想确认面试时间和需要准备的材料。"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <button class="login-submit" type="submit" :disabled="!canSend">
            {{ sending ? '发送中...' : '发送' }}
          </button>
        </form>
      </section>
    </section>
  </div>
</template>
