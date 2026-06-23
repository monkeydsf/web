<script setup>
import { useRouter } from 'vue-router'
import { getSession } from '../api'
import LoginPanel from '../components/LoginPanel.vue'

const router = useRouter()

function goHome() {
  if (getSession()) {
    const role = getSession()?.user?.role
    router.push(role === 'JOB_SEEKER' || role === 'STUDENT' ? '/jobs' : '/admin/dashboard')
  } else {
    router.push('/')
  }
}
</script>

<template>
  <div class="login-page-v2">
    <div class="lpv2-left">
      <div class="lpv2-brand">
        <span class="lpv2-logo">职策</span>
      </div>
      <h1>校园求职<br/>从这里开始</h1>
      <p>覆盖实习与校招岗位，AI 辅助简历优化与面试准备</p>
      <div class="lpv2-features">
        <div class="lpv2-feat">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
          <span>海量岗位</span>
        </div>
        <div class="lpv2-feat">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
          <span>AI 辅助</span>
        </div>
        <div class="lpv2-feat">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <span>精准匹配</span>
        </div>
      </div>
    </div>
    <div class="lpv2-right">
      <LoginPanel @success="goHome" />
    </div>
  </div>
</template>
