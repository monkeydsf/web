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
  loading.value = true; message.value = ''
  try { backgrounds.value = await api('/backgrounds') } catch (e) { message.value = e.message || '背景图加载失败' } finally { loading.value = false }
}

async function uploadBackground(item, event) {
  const file = event.target.files?.[0]
  if (!file) return
  const formData = new FormData(); formData.append('file', file)
  uploadingKey.value = item.pageKey; message.value = ''
  try {
    await api(`/backgrounds/${item.pageKey}`, { method: 'POST', body: formData, timeout: 10000 })
    await loadBackgrounds(); message.value = `${item.pageName} 背景图已更新`
  } catch (e) { message.value = e.message || '上传失败' } finally { uploadingKey.value = ''; event.target.value = '' }
}

onMounted(loadBackgrounds)
</script>

<template>
  <div class="adm-page">
    <div class="adm-page-head">
      <div>
        <h2>背景管理</h2>
        <span>为每个页面上传背景图</span>
      </div>
      <button class="adm-btn-outline" @click="loadBackgrounds">刷新</button>
    </div>

    <div v-if="loading" class="adm-loading">
      <div v-for="n in 2" :key="n" class="adm-loading-row"><div class="adm-loading-avatar"></div><div class="adm-loading-lines"><span></span><span style="width:60%"></span></div></div>
    </div>

    <div v-else class="adm-bg-grid">
      <div v-for="item in backgrounds" :key="item.pageKey" class="adm-bg-card">
        <div class="adm-bg-preview">
          <img :src="imageUrl(item)" :alt="item.pageName" />
        </div>
        <div class="adm-bg-info">
          <strong>{{ item.pageName }}</strong>
          <span>{{ item.pageKey }}</span>
        </div>
        <label class="adm-upload-label">
          <input type="file" accept="image/*" :disabled="Boolean(uploadingKey)" @change="uploadBackground(item, $event)" />
          {{ uploadingKey === item.pageKey ? '上传中...' : '上传新背景' }}
        </label>
      </div>
    </div>

    <p v-if="message" class="adm-msg">{{ message }}</p>
  </div>
</template>
