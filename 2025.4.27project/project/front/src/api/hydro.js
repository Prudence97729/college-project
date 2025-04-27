import axios from 'axios'

// 创建axios实例
const request = axios.create({
    baseURL: '/api',
    timeout: 5000
})

export const hydroApi = {
    // 获取水位数据 - 修改路径以匹配后端控制器
    getWaterLevelData(params) {
        return request({
            url: '/water-level/data', // 修改为正确的路径
            method: 'get',
            params
        })
    }
} 