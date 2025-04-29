import { defineStore } from 'pinia'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    adminInfo: JSON.parse(localStorage.getItem('adminInfo') || '{}'),
    adminToken: localStorage.getItem('adminToken') || ''
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.adminToken,
    getAdminId: (state) => state.adminInfo.id,
    getAdminName: (state) => state.adminInfo.username,
    getAdminNickname: (state) => state.adminInfo.nickname || state.adminInfo.username
  },
  
  actions: {
    // 更新管理员信息
    updateAdminInfo(adminInfo) {
      this.adminInfo = adminInfo
    },
    
    // 更新管理员头像
    updateAvatar(avatar) {
      this.adminInfo.avatar = avatar
    },
    
    // 设置管理员token
    setAdminToken(token) {
      this.adminToken = token
      localStorage.setItem('adminToken', token)
    },
    
    // 清除管理员信息
    clearAdminInfo() {
      this.adminInfo = {}
      this.adminToken = ''
      localStorage.removeItem('adminInfo')
      localStorage.removeItem('adminToken')
    },
    
    // 登录
    login(adminInfo, token) {
      this.adminInfo = adminInfo
      this.adminToken = token
      localStorage.setItem('adminInfo', JSON.stringify(adminInfo))
      localStorage.setItem('adminToken', token)
    }
  }
})