<script setup>
import { ref } from 'vue'
import { api } from '../api'

const form = ref({
  name: '',
  email: '',
  subject: '',
  message: ''
})

const loading = ref(false)
const submitted = ref(false)
const error = ref('')

async function handleSubmit() {
  loading.value = true
  error.value = ''
  try {
    await api('/contacts', {
      method: 'POST',
      body: JSON.stringify(form.value),
      timeout: 5000
    })
    submitted.value = true
    form.value = { name: '', email: '', subject: '', message: '' }
    window.setTimeout(() => { submitted.value = false }, 2500)
  } catch (err) {
    error.value = err.message || '提交失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="contact-page">
    <section class="contact-hero">
      <div class="contact-hero-inner">
        <span class="page-tag">Get in touch</span>
        <h1>联系</h1>
        <p>有问题或建议，欢迎联系我们。</p>
      </div>
    </section>

    <section class="contact-body">
      <div class="contact-grid">
        <div class="contact-info">
          <h2>联系方式</h2>
          <p class="contact-subtitle">我们期待听到你的声音</p>
          <div class="info-list">
            <div class="info-item"><div class="info-icon">✉</div><div><strong>电子邮箱</strong><p>support@campus-job.cn</p></div></div>
            <div class="info-item"><div class="info-icon">☎</div><div><strong>服务热线</strong><p>400-800-1234</p></div></div>
            <div class="info-item"><div class="info-icon">⌂</div><div><strong>办公地址</strong><p>江苏省南京市江宁区大学城创新路88号</p></div></div>
          </div>
        </div>

        <form class="contact-form" @submit.prevent="handleSubmit">
          <h2>发送消息</h2>
          <p class="contact-subtitle">填写以下表单，我们会尽快回复。</p>
          <div class="form-row">
            <label>
              <span>姓名</span>
              <input v-model="form.name" type="text" required />
            </label>
            <label>
              <span>邮箱</span>
              <input v-model="form.email" type="email" required />
            </label>
          </div>
          <label>
            <span>主题</span>
            <input v-model="form.subject" type="text" required />
          </label>
          <label>
            <span>内容</span>
            <textarea v-model="form.message" rows="5" required></textarea>
          </label>
          <button type="submit" class="submit-btn" :disabled="loading" :class="{ success: submitted }">
            {{ loading ? '提交中...' : (submitted ? '已提交' : '发送消息') }}
          </button>
          <p v-if="error" class="contact-error">{{ error }}</p>
        </form>
      </div>
    </section>
  </div>
</template>
