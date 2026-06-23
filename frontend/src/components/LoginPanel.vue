<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, saveSession } from '../api'

const emit = defineEmits(['success'])
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const message = ref('')
const mode = ref('login')
const selectedRole = ref(route.query.role === 'COMPANY' || route.query.role === 'ADMIN' ? route.query.role : 'JOB_SEEKER')

const form = reactive({
  username: '',
  password: '',
  fullName: '',
  email: '',
  phone: '',
  major: '',
  graduationYear: 2026
})

const roleCards = [
  { value: 'JOB_SEEKER', title: '求职者', desc: '浏览职位，投递简历，查看投递结果。' },
  { value: 'COMPANY', title: '公司人员', desc: '发布岗位，管理投递，查看招聘数据。' },
  { value: 'ADMIN', title: '管理员', desc: '进入后台管理路由，维护全站内容。' }
]

const canRegister = computed(() => selectedRole.value !== 'ADMIN')

watch(
  () => route.query.role,
  (role) => {
    if (role === 'COMPANY' || role === 'ADMIN' || role === 'JOB_SEEKER') {
      selectedRole.value = role
    }
  },
  { immediate: true }
)

function fillDemo(role) {
  selectedRole.value = role
  mode.value = 'login'
  if (role === 'JOB_SEEKER') {
    form.username = 'student'
    form.password = 'student123'
  } else if (role === 'COMPANY') {
    form.username = 'company'
    form.password = 'company123'
  } else {
    form.username = 'admin'
    form.password = 'admin123'
  }
}

function roleMatches(userRole) {
  if (selectedRole.value === 'JOB_SEEKER') return userRole === 'JOB_SEEKER' || userRole === 'STUDENT'
  return userRole === selectedRole.value
}

async function submit() {
  loading.value = true
  message.value = ''
  try {
    const path = mode.value === 'login' ? '/auth/login' : '/auth/register'
    const body = mode.value === 'login'
      ? { username: form.username, password: form.password }
      : {
          username: form.username,
          password: form.password,
          fullName: form.fullName,
          email: form.email,
          phone: form.phone,
          major: form.major,
          graduationYear: Number(form.graduationYear),
          role: selectedRole.value
        }
    const data = await api(path, {
      method: 'POST',
      body: JSON.stringify(body),
      timeout: 5000
    })
    if (!roleMatches(data.user.role)) {
      message.value = '账号角色与当前选择不一致，请切换角色后再登录。'
      return
    }
    saveSession(data)
    emit('success')
    const redirect = route.query.redirect
    if (typeof redirect === 'string' && redirect && redirect !== '/login') {
      router.push(redirect)
      return
    }
    router.push(data.user.role === 'JOB_SEEKER' || data.user.role === 'STUDENT' ? '/jobs' : '/admin/dashboard')
  } catch (error) {
    message.value = error.message || '操作失败，请确认后端已启动。'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-panel">
    <div class="login-intro">
      <span class="page-tag">Account</span>
      <h1>登录职策</h1>
      <p>求职者进入前台，企业人员和管理员进入后台。</p>
      <div class="demo-list">
        <button type="button" @click="fillDemo('JOB_SEEKER')">求职者演示</button>
        <button type="button" @click="fillDemo('COMPANY')">公司人员演示</button>
        <button type="button" @click="fillDemo('ADMIN')">管理员演示</button>
      </div>
    </div>

    <div class="login-card">
      <div class="role-switch">
        <button
          v-for="role in roleCards"
          :key="role.value"
          type="button"
          :class="{ active: selectedRole === role.value }"
          @click="selectedRole = role.value"
        >
          <strong>{{ role.title }}</strong>
          <span>{{ role.desc }}</span>
        </button>
      </div>

      <div class="auth-mode">
        <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button type="button" :disabled="!canRegister" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>
      <p v-if="!canRegister" class="login-tip">管理员账号由系统初始化，不开放注册。</p>

      <form class="login-form" @submit.prevent="submit">
        <label>
          <span>用户名</span>
          <input v-model="form.username" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" type="password" required minlength="6" />
        </label>

        <template v-if="mode === 'register'">
          <label>
            <span>{{ selectedRole === 'COMPANY' ? '联系人' : '姓名' }}</span>
            <input v-model="form.fullName" required />
          </label>
          <label>
            <span>{{ selectedRole === 'COMPANY' ? '公司名称' : '专业' }}</span>
            <input v-model="form.major" />
          </label>
          <label>
            <span>邮箱</span>
            <input v-model="form.email" type="email" />
          </label>
          <label>
            <span>手机号</span>
            <input v-model="form.phone" />
          </label>
          <label v-if="selectedRole === 'JOB_SEEKER'">
            <span>毕业年份</span>
            <input v-model="form.graduationYear" type="number" />
          </label>
        </template>

        <button class="login-submit" :disabled="loading">{{ mode === 'login' ? '登录' : '注册并登录' }}</button>
      </form>
      <p v-if="message" class="login-message">{{ message }}</p>
    </div>
  </div>
</template>
