<script setup>
import { ref } from 'vue'

const form = ref({
  name: '',
  email: '',
  subject: '',
  message: ''
})

const submitted = ref(false)

function handleSubmit() {
  submitted.value = true
  window.setTimeout(() => {
    submitted.value = false
    form.value = { name: '', email: '', subject: '', message: '' }
  }, 3000)
}
</script>

<template>
  <div class="contact-page">
    <section class="contact-hero">
      <div class="contact-hero-inner">
        <span class="page-tag">Get in touch</span>
        <h1>联系</h1>
        <p>有问题或建议，欢迎随时联系我们。</p>
      </div>
    </section>

    <section class="contact-body">
      <div class="contact-grid">
        <div class="contact-info">
          <h2>联系方式</h2>
          <p class="contact-subtitle">我们期待听到你的声音</p>

          <div class="info-list">
            <div class="info-item">
              <div class="info-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
              </div>
              <div>
                <strong>电子邮箱</strong>
                <p>support@campus-job.cn</p>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
              </div>
              <div>
                <strong>服务热线</strong>
                <p>400-800-1234</p>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              </div>
              <div>
                <strong>办公地址</strong>
                <p>江苏省南京市江宁区大学城创新路 88 号</p>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </div>
              <div>
                <strong>工作时间</strong>
                <p>周一至周五 9:00 - 18:00</p>
              </div>
            </div>
          </div>
        </div>

        <form class="contact-form" @submit.prevent="handleSubmit">
          <h2>发送消息</h2>
          <p class="contact-subtitle">填写以下表单，我们会尽快回复。</p>

          <div class="form-row">
            <label>
              <span>姓名</span>
              <input v-model="form.name" type="text" placeholder="请输入你的姓名" required />
            </label>
            <label>
              <span>邮箱</span>
              <input v-model="form.email" type="email" placeholder="请输入你的邮箱" required />
            </label>
          </div>
          <label>
            <span>主题</span>
            <input v-model="form.subject" type="text" placeholder="请输入消息主题" required />
          </label>
          <label>
            <span>内容</span>
            <textarea v-model="form.message" rows="5" placeholder="请输入你想说的内容" required></textarea>
          </label>

          <button type="submit" class="submit-btn" :class="{ success: submitted }">
            {{ submitted ? '发送成功' : '发送消息' }}
          </button>
        </form>
      </div>
    </section>
  </div>
</template>
