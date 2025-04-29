import axios from 'axios'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const request = axios.create({
    baseURL: '/api',  // 修改这里，使用相对路径
    timeout: 5000  // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = token
        }
        
        // 从localStorage获取用户信息并进行编码
        const userStr = localStorage.getItem('user')
        if (userStr) {
            config.headers['user'] = encodeURIComponent(userStr)
        }
        
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 用户相关的api接口
export const userApi = {
    // 登录
    async login(data) {
        const res = await request({
            url: '/user/login',
            method: 'post',
            data
        })
        if (res.code === 200) {
            const userStore = useUserStore()
            userStore.setToken(res.data.token)
            userStore.updateUserInfo(res.data.user)
        }
        return res
    },

    // 注册
    register(data) {
        return request({
            url: '/user/register',
            method: 'post',
            data
        })
    },

    // 获取用户信息
    getUserInfo() {
        return request({
            url: '/user/info',
            method: 'get'
        })
    },

    // 更新用户信息
    updateProfile(data) {
        return request({
            url: '/user/profile',
            method: 'put',
            data: {
                id: data.id,
                nickname: data.nickname,
                email: data.email
            }
        })
    },

    // 修改密码
    updatePassword(data) {
        return request({
            url: '/user/password',
            method: 'put',
            data
        })
    },

    // 更新头像
    updateAvatar(formData) {
        return request({
            url: '/user/avatar',
            method: 'post',
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            data: formData
        })
    }
} 