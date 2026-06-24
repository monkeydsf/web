<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, saveSession } from '../../api'

const router = useRouter()
const loading = ref(false)
const message = ref('')

const form = reactive({ username: '', password: '' })

async function submit() {
  loading.value = true; message.value = ''
  try {
    const data = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username: form.username, password: form.password }),
      timeout: 5000
    })
    if (data.user.role !== 'ADMIN') {
      message.value = '该账号不是管理员'; return
    }
    saveSession(data)
    router.push('/admin/dashboard')
  } catch (error) {
    message.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-login-page">
    <div class="alp-card">
      <div class="alp-header">
        <span class="alp-logo">职策</span>
        <span class="alp-badge">管理后台</span>
      </div>
      <h1>管理员登录</h1>
      <p>请输入管理员账号密码</p>

      <form class="alp-form" @submit.prevent="submit">
        <div class="alp-field">
          <label>用户名</label>
          <input v-model="form.username" required placeholder="admin" />
        </div>
        <div class="alp-field">
          <label>密码</label>
          <input v-model="form.password" type="password" required placeholder="请输入密码" />
        </div>
        <button class="alp-submit" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <p v-if="message" class="alp-error">{{ message }}</p>

      <div class="alp-demo">
        <span>快速体验</span>
        <button type="button" @click="form.username = 'admin'; form.password = 'admin123'">填充演示账号</button>
      </div>

      <div class="alp-footer">
        <router-link to="/">← 返回首页</router-link>
      </div>
    </div>
  </div>
</template>
