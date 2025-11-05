import axios, { AxiosError, type AxiosInstance, type AxiosRequestConfig } from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const api: AxiosInstance = axios.create({
  baseURL: API_URL,
  withCredentials: true,
})

let isRefreshing = false
const failedQueue: {
  resolve: (value?: unknown) => void
  reject: (reason?: any) => void
  config: AxiosRequestConfig
}[] = []

function processQueue(error: any, token: string | null = null) {
  failedQueue.forEach(({ resolve, reject, config }) => {
    if (error) reject(error)
    else resolve(token)
  })
  failedQueue.length = 0
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config

    if (!error.response || (originalRequest as any)?._retry) {
      return Promise.reject(error)
    }

    if (error.response.status === 401) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject, config: originalRequest! })
        })
          .then(() => api(originalRequest!))
          .catch((err) => Promise.reject(err))
      }

      (originalRequest as any)._retry = true
      isRefreshing = true

      try {
        await axios.post(`${API_URL}/v1/auth/refresh-token`, {}, { withCredentials: true })
        processQueue(null)
        return api(originalRequest!)
      } catch (refreshError) {
        processQueue(refreshError)
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default api
