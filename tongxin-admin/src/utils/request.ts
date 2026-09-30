import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const adminStore = useAdminStore()
  if (adminStore.token) {
    config.headers.Authorization = `Bearer ${adminStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (resp) => {
    const data = resp.data
    if (data.code === 200) {
      return data.data
    }
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message))
  },
  (error) => {
    if (error.response?.status === 401) {
      const adminStore = useAdminStore()
      adminStore.logout()
      window.location.href = '/login'
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
