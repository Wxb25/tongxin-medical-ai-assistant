import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AdminInfo } from '@/types'
import request from '@/utils/request'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref<AdminInfo | null>(
    localStorage.getItem('admin_info') ? JSON.parse(localStorage.getItem('admin_info')!) : null
  )

  const login = async (username: string, password: string) => {
    const res: any = await request.post('/admin/login', { username, password })
    token.value = res.token
    adminInfo.value = res.userInfo
    localStorage.setItem('admin_token', res.token)
    localStorage.setItem('admin_info', JSON.stringify(res.userInfo))
  }

  const logout = () => {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
  }

  return { token, adminInfo, login, logout }
})
