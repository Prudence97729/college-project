import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // API基础URL
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 根据请求URL判断是普通用户请求还是管理员请求
    if (config.url.includes('/admin')) {  // 修改判断条件
      // 管理员请求，添加管理员token
      const adminToken = localStorage.getItem('adminToken')
      if (adminToken) {
        config.headers['Authorization'] = `Bearer ${adminToken}`  // 添加 Bearer 前缀
      }
    } else {
      // 普通用户请求，添加用户token和用户信息
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
      
      if (userStr) {
        try {
          const userData = JSON.parse(userStr)
          // 如果存储的是包含 user 属性的对象，则取 user 属性
          const userInfo = userData.user || userData
          config.headers['user'] = encodeURIComponent(JSON.stringify(userInfo))
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('响应拦截器收到原始数据:', response)
    const res = response.data
    
    // 如果响应成功
    if (res.code === 200) {
      console.log('响应成功，处理前的数据:', res)
      // 直接返回整个响应对象
      return res
    }
    
    // 如果响应失败
    console.error('响应失败:', res)
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      const status = error.response.status
      
      // 处理401未授权错误
      if (status === 401) {
        // 判断是管理员请求还是普通用户请求
        if (error.config.url.startsWith('/admin')) {
          // 清除管理员信息
          localStorage.removeItem('adminInfo')
          localStorage.removeItem('adminToken')
          ElMessage.error('管理员登录已过期，请重新登录')
          router.push('/admin/login')
        } else {
          // 清除用户信息
          localStorage.removeItem('user')
          localStorage.removeItem('token')
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
        }
      }
      
      // 处理403禁止访问错误
      else if (status === 403) {
        ElMessage.error('没有权限访问该资源')
      }
      
      // 处理404错误
      else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      }
      
      // 处理500服务器错误
      else if (status === 500) {
        ElMessage.error('服务器错误，请稍后重试')
      }
      
      // 其他错误
      else {
        ElMessage.error(error.response.data.message || '请求失败')
      }
    } else {
      // 网络错误
      ElMessage.error('网络错误，请检查您的网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default service 