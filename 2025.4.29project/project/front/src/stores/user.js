import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref(JSON.parse(localStorage.getItem('user') || '{}'))
  const token = ref(localStorage.getItem('token') || '')

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  // 方法
  const initUserInfo = () => {
    userInfo.value = JSON.parse(localStorage.getItem('user') || '{}')
    token.value = localStorage.getItem('token') || ''
  }

  const updateUserInfo = (info) => {
    userInfo.value = {
      ...userInfo.value,
      ...info
    }
    localStorage.setItem('user', JSON.stringify(userInfo.value))
  }

  const updateAvatar = (avatarUrl) => {
    userInfo.value.userPic = avatarUrl
    localStorage.setItem('user', JSON.stringify(userInfo.value))
  }

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearUserInfo = () => {
    userInfo.value = {}
    token.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    initUserInfo,
    updateUserInfo,
    updateAvatar,
    setToken,
    clearUserInfo
  }
})