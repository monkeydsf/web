<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'

const API_ORIGIN = 'http://localhost:8087'
const backgrounds = ref([])
const loading = ref(true)
const message = ref('')
const uploadingKey = ref('')

function imageUrl(item) {
  if (!item.imageUrl) return ''
  return item.imageUrl.startsWith('http') ? item.imageUrl : `${API_ORIGIN}${item.imageUrl}`
}

async function loadBackgrounds() {
  loading.value = true
  message.value = ''
  try {
    backgrounds.value = await api('/backgrounds')
  } catch (error) {
    message.value = error.message || '背景图加载失败'
  } finally {
    loading.value = false
  }
}

async function uploadBackground(item, event) {
  const file = event.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  uploadingKey.value = item.pageKey
  message.value = ''
  try {
    await api(`/backgrounds/${item.pageKey}`, {
      method: 'POST',
      body: formData,
      timeout: 10000
    })
    await loadBackgrounds()
    message.value = `${item.pageName} 背景图已更新`
  } catch (error) {
    message.value = error.message || '上传失败'
  } finally {
    uploadingKey.value = ''
    event.target.value = ''
  }
}

onMounted(loadBackgrounds)
</script>

<template>
  <section class="admin-panel">
    <div class="admin-panel-head">
      <div>
        <h2>背景管理</h2>
        <p>默认背景图存放在后端服务，管理员可以为每个页面上传新背景图。</p>
      </div>
      <button class="admin-logout" @click="loadBackgrounds">刷新</button>
    </div>

    <p v-if="loading" class="notice">加载中...</p>

    <div v-else class="background-grid">
      <article v-for="item in backgrounds" :key="item.pageKey" class="background-card">
        <div class="background-preview">
          <img :src="imageUrl(item)" :alt="item.pageName" />
        </div>
        <div>
          <h3>{{ item.pageName }}</h3>
          <p>页面标识：{{ item.pageKey }}</p>
          <p>当前地址：{{ item.imageUrl }}</p>
        </div>
        <label class="background-upload">
          <span>{{ uploadingKey === item.pageKey ? '上传中...' : '上传新背景图' }}</span>
          <input
            type="file"
            accept="image/png,image/jpeg,image/webp,image/gif,image/svg+xml"
            :disabled="Boolean(uploadingKey)"
            @change="uploadBackground(item, $event)"
          />
        </label>
      </article>
    </div>

    <p v-if="message" class="notice">{{ message }}</p>
  </section>
</template>
