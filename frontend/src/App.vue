<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSession, logout, sessionState } from './api'
import LoginPanel from './components/LoginPanel.vue'

const route = useRoute()
const router = useRouter()
const showLogin = ref(false)
const loginRole = ref('')
const showDropdown = ref(false)

const publicNavItems = computed(() => {
  const items = [
    { name: 'home', label: '首页', path: '/' },
    { name: 'jobs', label: '职位', path: '/jobs' }
  ]
  const role = getSession()?.user?.role
  if (role === 'JOB_SEEKER' || role === 'STUDENT') {
    items.push({ name: 'applications', label: '我的投递', path: '/applications' })
    items.push({ name: 'messages', label: '消息', path: '/messages' })
    items.push({ name: 'workbench', label: '工作台', path: '/workbench' })
  }
  if (role === 'COMPANY') {
    items.push({ name: 'company-jobs', label: '我的岗位', path: '/company/jobs' })
    items.push({ name: 'company-apps', label: '收到的投递', path: '/company/applications' })
    items.push({ name: 'messages', label: '消息', path: '/messages' })
  }
  if (role === 'ADMIN') {
    items.push({ name: 'admin-dashboard', label: '后台管理', path: '/admin/dashboard' })
  }
  items.push({ name: 'learn', label: '学习', path: '/learn' })
  return items
})

const currentRoute = computed(() => {
  if (route.path.startsWith('/messages')) return 'messages'
  if (route.path.startsWith('/company')) return route.name
  if (route.path.startsWith('/admin')) return 'admin-dashboard'
  return route.name
})
const session = sessionState

const avatarText = computed(() => {
  const user = getSession()?.user
  const name = user?.fullName || user?.username
  return name ? name.slice(0, 1).toUpperCase() : 'U'
})

const roleText = computed(() => {
  const role = getSession()?.user?.role
  if (role === 'ADMIN') return '管理员'
  if (role === 'COMPANY') return '企业用户'
  if (role === 'JOB_SEEKER' || role === 'STUDENT') return '求职者'
  return ''
})

const dashboardPath = computed(() => {
  const role = getSession()?.user?.role
  if (role === 'COMPANY') return '/company/dashboard'
  if (role === 'ADMIN') return '/admin/dashboard'
  return '/resume'
})

async function handleLogout() {
  await logout(); showLogin.value = false; showDropdown.value = false; router.push('/')
}

function openLogin(role) {
  loginRole.value = role || ''
  showLogin.value = true; showDropdown.value = false
  // Force navigate to login page with role param
  if (role && route.path !== '/login') {
    router.push({ path: '/login', query: { role } })
  }
}

function closeLogin() { showLogin.value = false }

function toggleDropdown() { showDropdown.value = !showDropdown.value }

watch(session, () => { showLogin.value = false; showDropdown.value = false })
</script>

<template>
  <div class="page" @click="showDropdown = false">
    <header v-if="!route.path.startsWith('/admin') && !route.path.startsWith('/company')" class="top-nav">
      <router-link class="logo" to="/" aria-label="职策首页">
        <span>职策</span>
      </router-link>

      <nav class="nav-links" aria-label="主导航">
        <router-link v-for="item in publicNavItems" :key="item.name" :to="item.path" :class="{ active: currentRoute === item.name }">
          {{ item.label }}
        </router-link>
      </nav>

      <div class="avatar-entry" @click.stop>
        <template v-if="session">
          <div class="avatar-circle">{{ avatarText }}</div>
          <span class="avatar-meta">
            <strong>{{ getSession()?.user?.fullName || getSession()?.user?.username }}</strong>
            <small>{{ roleText }}</small>
          </span>
          <div class="avatar-actions">
            <button type="button" @click="router.push(dashboardPath)">我的</button>
            <button type="button" @click="handleLogout">退出</button>
          </div>
        </template>
        <template v-else>
          <div class="login-dropdown-wrap">
            <button class="avatar-login" type="button" @click.stop="toggleDropdown">登录</button>
            <div v-if="showDropdown" class="login-dropdown">
              <button type="button" @click="openLogin('JOB_SEEKER')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="7" r="4"/><path d="M5.5 21a7.5 7.5 0 0 1 13 0"/></svg>
                我要找工作
              </button>
              <button type="button" @click="openLogin('COMPANY')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                我要招聘
              </button>
            </div>
          </div>
        </template>
      </div>
    </header>

    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <div v-if="showLogin" class="login-modal-backdrop" @click.self="closeLogin">
      <section class="login-modal-card" role="dialog" aria-modal="true">
        <button class="login-close-btn" type="button" @click="closeLogin">&times;</button>
        <LoginPanel :role="loginRole" @success="closeLogin" />
      </section>
    </div>
  </div>
</template>
