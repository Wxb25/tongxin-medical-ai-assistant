import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('tx_token') || '')
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem('tx_user') ? JSON.parse(localStorage.getItem('tx_user')!) : null
  )

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('tx_token', t)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('tx_user', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('tx_token')
    localStorage.removeItem('tx_user')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
