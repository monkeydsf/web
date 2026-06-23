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
  { value: 'JOB_SEEKER', title: '求职者', desc: '浏览职位，投递简历' },
  { value: 'COMPANY', title: '企业用户', desc: '发布岗位，管理投递' },
  { value: 'ADMIN', title: '管理员', desc: '管理后台内容' }
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
  if (role === 'JOB_SEEKER') { form.username = 'student'; form.password = 'student123' }
  else if (role === 'COMPANY') { form.username = 'company'; form.password = 'company123' }
  else { form.username = 'admin'; form.password = 'admin123' }
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
  <div class="lpv2-panel">
    <div class="lpv2-panel-header">
      <h2>{{ mode === 'login' ? '登录账号' : '注册账号' }}</h2>
      <p>{{ mode === 'login' ? '欢迎回来，请登录以继续' : '创建新账号，开始求职之旅' }}</p>
    </div>

    <!-- Role tabs -->
    <div class="lpv2-roles">
      <button
        v-for="role in roleCards"
        :key="role.value"
        type="button"
        class="lpv2-role-btn"
        :class="{ active: selectedRole === role.value }"
        @click="selectedRole = role.value"
      >
        <strong>{{ role.title }}</strong>
        <span>{{ role.desc }}</span>
      </button>
    </div>

    <!-- Auth mode -->
    <div class="lpv2-mode">
      <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
      <button type="button" :disabled="!canRegister" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
    </div>
    <p v-if="!canRegister" class="lpv2-tip">管理员账号由系统初始化，不开放注册。</p>

    <!-- Form -->
    <form class="lpv2-form" @submit.prevent="submit">
      <div class="lpv2-field">
        <label>用户名</label>
        <input v-model="form.username" required placeholder="请输入用户名" />
      </div>
      <div class="lpv2-field">
        <label>密码</label>
        <input v-model="form.password" type="password" required minlength="6" placeholder="请输入密码" />
      </div>

      <template v-if="mode === 'register'">
        <div class="lpv2-field">
          <label>{{ selectedRole === 'COMPANY' ? '联系人' : '姓名' }}</label>
          <input v-model="form.fullName" required placeholder="请输入姓名" />
        </div>
        <div class="lpv2-field">
          <label>{{ selectedRole === 'COMPANY' ? '公司名称' : '专业' }}</label>
          <input v-model="form.major" placeholder="请输入专业" />
        </div>
        <div class="lpv2-row">
          <div class="lpv2-field">
            <label>邮箱</label>
            <input v-model="form.email" type="email" placeholder="请输入邮箱" />
          </div>
          <div class="lpv2-field">
            <label>手机号</label>
            <input v-model="form.phone" placeholder="请输入手机号" />
          </div>
        </div>
        <div v-if="selectedRole === 'JOB_SEEKER'" class="lpv2-field">
          <label>毕业年份</label>
          <input v-model="form.graduationYear" type="number" />
        </div>
      </template>

      <button class="lpv2-submit" type="submit" :disabled="loading">
        {{ loading ? '处理中...' : (mode === 'login' ? '登录' : '注册并登录') }}
      </button>
    </form>

    <p v-if="message" class="lpv2-message">{{ message }}</p>

    <!-- Demo accounts -->
    <div class="lpv2-demo">
      <span>快速体验</span>
      <div class="lpv2-demo-btns">
        <button type="button" @click="fillDemo('JOB_SEEKER')">求职者</button>
        <button type="button" @click="fillDemo('COMPANY')">企业用户</button>
        <button type="button" @click="fillDemo('ADMIN')">管理员</button>
      </div>
    </div>
  </div>
</template>
