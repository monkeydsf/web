<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { getSession, logout as logoutSession } from '../../api'

const router = useRouter()
const session = computed(() => getSession())
const roleName = computed(() => session.value?.user?.role === 'ADMIN' ? '管理员' : '企业用户')
const menu = [
  { label: '概览', path: '/admin/dashboard' },
  { label: '岗位管理', path: '/admin/jobs' },
  { label: '投递管理', path: '/admin/applications' },
  { label: '背景管理', path: '/admin/backgrounds' },
  { label: '举报处理', path: '/admin/reports' }
]

async function logout() {
  await logoutSession()
  router.push('/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <span class="logo-symbol">职</span>
        <div>
          <strong>职策</strong>
          <small>{{ roleName }}</small>
        </div>
      </div>

      <nav class="admin-menu">
        <RouterLink v-for="item in menu" :key="item.path" :to="item.path" class="admin-menu-item">
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="admin-userbox">
        <span>{{ session?.user?.fullName || session?.user?.username }}</span>
        <small>{{ session?.user?.role }}</small>
      </div>

      <button class="admin-logout" @click="logout">退出登录</button>
    </aside>

    <section class="admin-content">
      <header class="admin-topbar">
        <div>
          <span class="page-tag">Admin console</span>
          <h1>后台管理</h1>
        </div>
        <div class="admin-topbar-meta">
          <span>{{ session?.user?.username }}</span>
          <strong>{{ roleName }}</strong>
        </div>
      </header>

      <RouterView />
    </section>
  </div>
</template>
