<script setup>
import { onMounted, ref } from 'vue'
import { api, markNotificationRead } from '../api'

const notifications = ref([])
const unreadCount = ref(0)
const loading = ref(true)
const message = ref('')
const activeTab = ref('all')

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unread', label: '未读' }
]

const filteredNotifications = ref([])

function applyFilter() {
  if (activeTab.value === 'unread') {
    filteredNotifications.value = notifications.value.filter(n => !n.readAt)
  } else {
    filteredNotifications.value = notifications.value
  }
}

function switchTab(key) {
  activeTab.value = key
  applyFilter()
}

function typeIcon(type) {
  if (type === 'APPLICATION_STATUS') return 'M22 11.08V12a10 10 0 1 1-5.93-9.14'
  if (type === 'MESSAGE') return 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z'
  if (type === 'INTERVIEW') return 'M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z'
  return 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0'
}

function typeColor(type) {
  if (type === 'APPLICATION_STATUS') return '#4a90e2'
  if (type === 'MESSAGE') return '#059669'
  if (type === 'INTERVIEW') return '#d97706'
  return '#7c3aed'
}

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  return Math.floor(diff / 86400) + ' 天前'
}

async function loadNotifications() {
  loading.value = true
  message.value = ''
  try {
    notifications.value = await api('/notifications')
    const summary = await api('/notifications/summary')
    unreadCount.value = summary.unreadCount || 0
    applyFilter()
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
  <div class="msg-v2">
    <!-- Header -->
    <section class="msg-hero">
      <div class="msg-hero-inner">
        <div class="msg-hero-left">
          <h1>通知</h1>
          <p v-if="unreadCount">{{ unreadCount }} 条未读通知</p>
          <p v-else>所有通知已读</p>
        </div>
      </div>
    </section>

    <!-- Content -->
    <section class="msg-content">
      <div class="msg-layout">
        <!-- Tabs -->
        <div class="msg-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            class="msg-tab"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
            <span v-if="tab.key === 'unread' && unreadCount" class="msg-tab-badge">{{ unreadCount }}</span>
          </button>
        </div>

        <!-- List -->
        <div class="msg-list">
          <div v-if="loading" class="msg-loading">
            <div v-for="n in 4" :key="n" class="msg-loading-item">
              <div class="msg-loading-icon"></div>
              <div class="msg-loading-lines"><span></span><span style="width:60%"></span></div>
            </div>
          </div>

          <template v-else>
            <div v-if="!filteredNotifications.length" class="msg-empty">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
              <p>暂无通知</p>
            </div>

            <button
              v-for="item in filteredNotifications"
              :key="item.id"
              type="button"
              class="msg-item"
              :class="{ unread: !item.readAt }"
              @click="markRead(item)"
            >
              <div class="msg-icon" :style="{ background: typeColor(item.type) + '18', color: typeColor(item.type) }">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path :d="typeIcon(item.type)"/></svg>
              </div>
              <div class="msg-item-body">
                <div class="msg-item-top">
                  <h3>{{ item.title }}</h3>
                  <span class="msg-item-time">{{ timeAgo(item.createdAt) }}</span>
                </div>
                <p>{{ item.content }}</p>
              </div>
              <div v-if="!item.readAt" class="msg-dot"></div>
            </button>
          </template>
        </div>

        <p v-if="message" class="msg-error">{{ message }}</p>
      </div>
    </section>
  </div>
</template>
