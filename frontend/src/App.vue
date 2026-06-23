<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSession, logout, sessionState } from './api'
import LoginPanel from './components/LoginPanel.vue'

const route = useRoute()
const router = useRouter()
const showLogin = ref(false)

const publicNavItems = computed(() => {
  const items = [
    { name: 'home', label: '首页', path: '/' },
    { name: 'jobs', label: '职位', path: '/jobs' },
    { name: 'contact', label: '联系', path: '/contact' },
    { name: 'notifications', label: '通知', path: '/notifications' },
    { name: 'messages', label: '对话', path: '/messages' },
    { name: 'workbench', label: '工作台', path: '/workbench' },
    { name: 'learn', label: '学习', path: '/learn' }
  ]
  const role = getSession()?.user?.role
  if (role === 'JOB_SEEKER' || role === 'STUDENT') {
    items.splice(3, 0, { name: 'applications', label: '我的投递', path: '/applications' })
  }
  return items
})

const currentRoute = computed(() => (route.path.startsWith('/messages') ? 'messages' : route.name))
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
  return '未登录'
})

async function handleLogout() {
  await logout()
  showLogin.value = false
  router.push('/')
}

function openLogin() { showLogin.value = true }
function closeLogin() { showLogin.value = false }

watch(session, (value) => {
  if (value) showLogin.value = false
})
</script>

<template>
  <div class="page">
    <header v-if="!route.path.startsWith('/admin')" class="top-nav">
      <router-link class="logo" to="/" aria-label="首页">
        <span>职策</span>
      </router-link>

      <nav class="nav-links" aria-label="主导航">
        <router-link
          v-for="item in publicNavItems"
          :key="item.name"
          :to="item.path"
          :class="{ active: currentRoute === item.name }"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <div class="avatar-entry" :title="session ? roleText : '登录'" aria-label="用户入口">
        <span class="avatar-circle">{{ session ? avatarText : '' }}</span>
        <span v-if="session" class="avatar-meta">
          <strong>{{ getSession()?.user?.fullName || getSession()?.user?.username }}</strong>
          <small>{{ roleText }}</small>
        </span>
        <span v-if="session" class="avatar-actions">
          <button
            type="button"
            @click="router.push(getSession()?.user?.role === 'COMPANY' || getSession()?.user?.role === 'ADMIN' ? '/admin/dashboard' : '/resume')"
          >
            我的
          </button>
          <button type="button" @click="handleLogout">退出</button>
        </span>
        <button v-else class="avatar-login" type="button" @click="openLogin">登录</button>
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
      <section class="login-modal-card" role="dialog" aria-modal="true" aria-label="登录">
        <button class="login-close-btn" type="button" aria-label="关闭登录窗口" @click="closeLogin">×</button>
        <LoginPanel @success="closeLogin" />
      </section>
    </div>
  </div>
</template>
