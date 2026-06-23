<script setup>
import { useRouter } from 'vue-router'
import LoginPanel from '../components/LoginPanel.vue'

const router = useRouter()
</script>

<template>
  <div class="login-floating-page">
    <div class="login-modal-card">
      <button class="login-close-btn" type="button" aria-label="关闭登录窗口" @click="router.push('/')">×</button>
      <LoginPanel />
    </div>
  </div>
</template>
