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

function timeAgo(value) {
  if (!value) return ''
  const d = new Date(value)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  return Math.floor(diff / 86400) + '天前'
}

function contactName(item) {
  return isManager.value ? item.student?.fullName || item.student?.username || '学生' : item.job?.company || '企业'
}

function contactInitial(item) {
  const name = contactName(item)
  return name ? name.charAt(0) : '·'
}

function senderLabel(item) {
  if (item.sender?.id === currentUserId.value) return '我'
  if (item.sender?.role === 'COMPANY' || item.sender?.role === 'ADMIN') return '企业'
  return item.sender?.fullName || '学生'
}

function isMine(item) {
  return item.sender?.id === currentUserId.value
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}

async function loadMessages(id) {
  if (!id) { messages.value = []; return }
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
    if (!applications.value.length) { activeId.value = null; messages.value = []; return }
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
  <div class="chat-v2">
    <!-- Sidebar -->
    <aside class="chat-sidebar">
      <div class="cs-header">
        <h2>消息</h2>
        <button type="button" class="cs-refresh" @click="loadApplications" title="刷新">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
        </button>
      </div>

      <div v-if="loading" class="cs-loading">
        <div v-for="n in 4" :key="n" class="cs-loading-item">
          <div class="cs-loading-avatar"></div>
          <div class="cs-loading-lines"><span></span><span style="width:50%"></span></div>
        </div>
      </div>

      <div v-else-if="!conversations.length" class="cs-empty">
        <p>暂无对话</p>
        <span>到职位页投递后开始沟通</span>
      </div>

      <div v-else class="cs-list">
        <button
          v-for="item in conversations"
          :key="item.applicationId"
          type="button"
          class="cs-item"
          :class="{ active: item.applicationId === activeId }"
          @click="selectConversation({ id: item.applicationId })"
        >
          <div class="cs-avatar">{{ contactInitial({ student: item.student, job: item.job }) }}</div>
          <div class="cs-item-body">
            <div class="cs-item-top">
              <span class="cs-name">{{ item.student?.fullName || item.student?.username || item.job?.company || '会话' }}</span>
              <span class="cs-time">{{ timeAgo(item.latestMessageAt) }}</span>
            </div>
            <div class="cs-item-bottom">
              <span class="cs-job">{{ item.job?.title }}</span>
              <span v-if="item.unread" class="cs-badge"></span>
            </div>
          </div>
        </button>
      </div>
    </aside>

    <!-- Chat -->
    <main class="chat-main">
      <div v-if="!activeApplication" class="cm-empty">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        <p>选择左侧联系人开始沟通</p>
      </div>

      <template v-else>
        <!-- Chat header -->
        <div class="cm-header">
          <div class="cm-header-info">
            <div class="cm-avatar">{{ contactInitial(activeApplication) }}</div>
            <div>
              <h3>{{ contactName(activeApplication) }}</h3>
              <span>{{ activeApplication.job?.title }} · {{ activeApplication.job?.city }}</span>
            </div>
          </div>
          <button type="button" class="cm-refresh" @click="loadMessages(activeId)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
          </button>
        </div>

        <!-- Messages -->
        <div ref="chatBody" class="cm-body">
          <div v-if="loadingMessages" class="cm-loading">加载中...</div>
          <div v-else-if="!messages.length" class="cm-empty-msg">
            <p>还没有消息</p>
            <span>发送面试时间、补充材料或岗位问题</span>
          </div>
          <template v-else>
            <div v-for="(item, idx) in messages" :key="item.id" class="cm-msg" :class="{ mine: isMine(item) }">
              <div v-if="!isMine(item)" class="cm-msg-avatar">{{ senderLabel(item).charAt(0) }}</div>
              <div class="cm-msg-body">
                <span class="cm-msg-label">{{ senderLabel(item) }}</span>
                <div class="cm-msg-bubble">{{ item.content }}</div>
                <span class="cm-msg-time">{{ timeAgo(item.createdAt) }}</span>
              </div>
            </div>
          </template>
        </div>

        <!-- Input -->
        <div class="cm-input-area">
          <textarea
            v-model="input"
            maxlength="2000"
            :disabled="!activeApplication"
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <button class="cm-send" type="button" :disabled="!canSend" @click="sendMessage">
            {{ sending ? '...' : '发送' }}
          </button>
        </div>
      </template>

      <p v-if="message" class="cm-error">{{ message }}</p>
    </main>
  </div>
</template>
