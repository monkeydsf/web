<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, saveSession } from '../api'

const props = defineProps({ role: { type: String, default: '' } })
const emit = defineEmits(['success'])
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const message = ref('')
const mode = ref('login')
const selectedRole = ref(props.role || 'JOB_SEEKER')

const form = reactive({
  username: '', password: '', fullName: '', email: '', phone: '', major: '', graduationYear: 2026
})

const isJobSeeker = computed(() => selectedRole.value === 'JOB_SEEKER')
const isAdmin = computed(() => selectedRole.value === 'ADMIN')

function fillDemo() {
  mode.value = 'login'
  if (isAdmin.value) { form.username = 'admin'; form.password = 'admin123' }
  else if (isJobSeeker.value) { form.username = 'student'; form.password = 'student123' }
  else { form.username = 'company'; form.password = 'company123' }
}

async function submit() {
  loading.value = true; message.value = ''
  try {
    const path = mode.value === 'login' ? '/auth/login' : '/auth/register'
    const body = mode.value === 'login'
      ? { username: form.username, password: form.password }
      : {
          username: form.username, password: form.password, fullName: form.fullName,
          email: form.email, phone: form.phone, major: form.major,
          graduationYear: Number(form.graduationYear), role: selectedRole.value
        }
    const data = await api(path, { method: 'POST', body: JSON.stringify(body), timeout: 5000 })
    const userRole = data.user.role
    if (isAdmin.value && userRole !== 'ADMIN') {
      message.value = '该账号不是管理员'; return
    }
    if (isJobSeeker.value && userRole !== 'JOB_SEEKER' && userRole !== 'STUDENT') {
      message.value = '该账号不是求职者，请选择企业登录'; return
    }
    if (!isJobSeeker.value && !isAdmin.value && userRole !== 'COMPANY') {
      message.value = '该账号不是企业用户，请选择求职者登录'; return
    }
    saveSession(data); emit('success')
    const redirect = route.query.redirect
    if (typeof redirect === 'string' && redirect && redirect !== '/login') { router.push(redirect); return }
    if (isAdmin.value) router.push('/admin/dashboard')
    else if (isJobSeeker.value) router.push('/jobs')
    else router.push('/company/dashboard')
  } catch (error) { message.value = error.message || '操作失败' } finally { loading.value = false }
}
</script>

<template>
  <div class="lpv2-panel">
    <div class="lpv2-panel-header">
      <h2>{{ isAdmin ? '管理员登录' : isJobSeeker ? '求职者登录' : '企业登录' }}</h2>
      <p>{{ isAdmin ? '登录后台管理系统' : isJobSeeker ? '登录后浏览职位、投递简历' : '登录后发布岗位、管理招聘' }}</p>
    </div>

    <div class="lpv2-mode">
      <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
      <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
    </div>

    <form class="lpv2-form" @submit.prevent="submit">
      <div class="lpv2-field"><label>用户名</label><input v-model="form.username" required placeholder="请输入用户名" /></div>
      <div class="lpv2-field"><label>密码</label><input v-model="form.password" type="password" required minlength="6" placeholder="请输入密码" /></div>
      <template v-if="mode === 'register'">
        <div class="lpv2-field"><label>{{ isJobSeeker ? '姓名' : '联系人' }}</label><input v-model="form.fullName" required placeholder="请输入姓名" /></div>
        <div class="lpv2-field"><label>{{ isJobSeeker ? '专业' : '公司名称' }}</label><input v-model="form.major" placeholder="请输入" /></div>
        <div class="lpv2-row">
          <div class="lpv2-field"><label>邮箱</label><input v-model="form.email" type="email" placeholder="请输入邮箱" /></div>
          <div class="lpv2-field"><label>手机号</label><input v-model="form.phone" placeholder="请输入手机号" /></div>
        </div>
        <div v-if="isJobSeeker" class="lpv2-field"><label>毕业年份</label><input v-model="form.graduationYear" type="number" /></div>
      </template>
      <button class="lpv2-submit" type="submit" :disabled="loading">{{ loading ? '处理中...' : (mode === 'login' ? '登录' : '注册并登录') }}</button>
    </form>
    <p v-if="message" class="lpv2-message">{{ message }}</p>
    <div class="lpv2-demo">
      <span>快速体验</span>
      <button type="button" class="lpv2-demo-single" @click="fillDemo">{{ isJobSeeker ? '求职者演示' : '企业演示' }}</button>
    </div>
  </div>
</template>
