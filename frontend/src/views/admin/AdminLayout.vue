<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { getSession, logout as logoutSession } from '../../api'

const router = useRouter()
const session = computed(() => getSession())
const roleName = computed(() => session.value?.user?.role === 'ADMIN' ? '管理员' : '企业用户')

const menu = [
  { label: '概览', path: '/admin/dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-4 0h4' },
  { label: '岗位管理', path: '/admin/jobs', icon: 'M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z' },
  { label: '投递管理', path: '/admin/applications', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2' },
  { label: '背景管理', path: '/admin/backgrounds', icon: 'M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z' },
  { label: '举报处理', path: '/admin/reports', icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z' }
]

async function logout() {
  await logoutSession()
  router.push('/login')
}
</script>

<template>
  <div class="admin-v2">
    <aside class="adm-sidebar">
      <div class="adm-brand">
        <span class="adm-logo">职策</span>
        <span class="adm-role">{{ roleName }}</span>
      </div>

      <nav class="adm-nav">
        <RouterLink
          v-for="item in menu"
          :key="item.path"
          :to="item.path"
          class="adm-nav-item"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path :d="item.icon"/></svg>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="adm-footer">
        <div class="adm-user">
          <div class="adm-user-avatar">{{ (session?.user?.fullName || session?.user?.username || 'U').charAt(0) }}</div>
          <div class="adm-user-info">
            <strong>{{ session?.user?.fullName || session?.user?.username }}</strong>
            <span>{{ roleName }}</span>
          </div>
        </div>
        <button class="adm-logout" @click="logout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </aside>

    <main class="adm-main">
      <RouterView />
    </main>
  </div>
</template>
