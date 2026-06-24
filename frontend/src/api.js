import { ref } from 'vue'

const API_BASE = import.meta.env.VITE_API_BASE || '/api'
const SESSION_KEY = 'campus-career-session'

function safeJsonParse(text) {
  try {
    return text ? JSON.parse(text) : null
  } catch {
    return { message: text }
  }
}

function readSession() {
  const raw = localStorage.getItem(SESSION_KEY)
  return raw ? JSON.parse(raw) : null
}

export const sessionState = ref(readSession())

export function getSession() {
  return sessionState.value
}

export function saveSession(session) {
  sessionState.value = session
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession() {
  sessionState.value = null
  localStorage.removeItem(SESSION_KEY)
}

export async function logout() {
  try {
    await api('/auth/logout', { method: 'POST', timeout: 2000, skipAuthClear: true })
  } catch {
    // Local logout should still work even if the backend session already expired.
  } finally {
    clearSession()
  }
}

export async function markNotificationRead(id) {
  return api(`/notifications/${id}/read`, { method: 'PATCH' })
}

export async function api(path, options = {}) {
  const session = getSession()
  const { timeout = 3000, skipAuthClear = false, ...fetchOptions } = options
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), timeout)
  const isFormData = fetchOptions.body instanceof FormData
  const headers = {
    ...(fetchOptions.headers || {})
  }
  if (!isFormData) {
    headers['Content-Type'] = headers['Content-Type'] || 'application/json'
  }
  if (session?.token) {
    headers['X-Token'] = session.token
  }

  try {
    const response = await fetch(`${API_BASE}${path}`, {
      ...fetchOptions,
      headers,
      signal: controller.signal
    })
    const text = await response.text()
    const data = safeJsonParse(text)
    if (!response.ok) {
      if (response.status === 401 && !skipAuthClear) {
        clearSession()
      }
      throw new Error(data?.message || '请求失败')
    }
    return data
  } catch (error) {
    if (error.name === 'AbortError') {
      throw new Error('请求超时，请确认后端已启动')
    }
    throw error
  } finally {
    window.clearTimeout(timeoutId)
  }
}
