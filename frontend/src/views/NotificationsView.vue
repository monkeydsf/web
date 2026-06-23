<script setup>
import { onMounted, ref } from 'vue'
import { api, markNotificationRead } from '../api'

const notifications = ref([])
const unreadCount = ref(0)
const loading = ref(true)
const message = ref('')

async function loadNotifications() {
  loading.value = true
  message.value = ''
  try {
    notifications.value = await api('/notifications')
    const summary = await api('/notifications/summary')
    unreadCount.value = summary.unreadCount || 0
  } catch (error) {
    message.value = error.message || '通知加载失败'
  } finally {
    loading.value = false
  }
}

async function markRead(item) {
  if (item.readAt) return
  try {
    await markNotificationRead(item.id)
    await loadNotifications()
  } catch (error) {
    message.value = error.message || '标记失败'
  }
}

onMounted(loadNotifications)
</script>

<template>
  <div class="learning-page">
    <section class="learning-hero">
      <div>
        <span class="page-tag">Notifications</span>
        <h1>通知中心</h1>
        <p>集中查看投递状态、消息、面试和系统通知。</p>
      </div>
      <div class="learning-stats">
        <strong>{{ unreadCount }}</strong>
        <span>未读通知</span>
      </div>
    </section>

    <section class="learning-reader">
      <p v-if="loading" class="notice">加载中...</p>
      <div v-else class="notification-list">
        <article v-for="item in notifications" :key="item.id" class="notification-item" :class="{ unread: !item.readAt }" @click="markRead(item)">
          <strong>{{ item.title }}</strong>
          <p>{{ item.content }}</p>
          <small>{{ item.createdAt?.slice(0, 16).replace('T', ' ') }}</small>
        </article>
      </div>
      <p v-if="message" class="notice">{{ message }}</p>
    </section>
  </div>
</template>
