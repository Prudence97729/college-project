import request from '@/utils/request'

export const adminApi = {
  // 管理员登录
  login(data) {
    return request({
      url: '/admin/login',
      method: 'post',
      data
    })
  },
  
  // 获取管理员信息
  getAdminInfo() {
    return request({
      url: '/admin/info',
      method: 'get'
    })
  },
  
  // 更新管理员个人资料
  updateProfile(data) {
    return request({
      url: '/admin/profile',
      method: 'put',
      data
    })
  },
  
  // 更新管理员密码
  updatePassword(data) {
    return request({
      url: '/admin/password',
      method: 'put',
      data
    })
  },
  
  // 获取系统统计数据
  getStatistics() {
    return request({
      url: '/admin/statistics',
      method: 'get'
    })
  },
  
  // 获取用户列表
  getUserList() {
    return request({
      url: '/admin/users',
      method: 'get'
    })
  },
  
  // 获取策略列表
  getStrategyList(params) {
    return request({
      url: '/strategies',
      method: 'get',
      params
    })
  },
  
  // 添加策略
  addStrategy(data) {
    return request({
      url: '/strategies',
      method: 'post',
      data
    })
  },
  
  // 更新策略
  updateStrategy(data) {
    return request({
      url: '/strategies',
      method: 'put',
      data
    })
  },
  
  // 删除策略
  deleteStrategy(id) {
    return request({
      url: `/strategies/${id}`,
      method: 'delete'
    })
  },
  
  // 获取登录日志
  getLoginLogs(params) {
    return request({
      url: '/admin/logs',
      method: 'get',
      params
    })
  },
  
  // 添加用户
  addUser(data) {
    return request({
      url: '/admin/users',
      method: 'post',
      data
    })
  },
  
  // 更新用户
  updateUser(data) {
    return request({
      url: `/admin/users/${data.id}`,
      method: 'put',
      data
    })
  },
  
  // 删除用户
  deleteUser(userId) {
    return request({
      url: `/admin/users/${userId}`,
      method: 'delete'
    })
  },
  
  // 获取最近活动
  getRecentActivities() {
    return request({
      url: '/admin/logs',
      method: 'get',
      params: {
        page: 1,
        size: 10,
        simplified: true
      }
    })
  }
}