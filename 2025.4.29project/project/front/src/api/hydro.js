import axios from 'axios'

// 创建axios实例,这里应该调用request.js方法里面的request，这样返回的数据就不用res.data.data了，但考虑已经部分模块已经写好了，就暂时不改了
const request = axios.create({
    baseURL: '/api',
    timeout: 5000
})

export const hydroApi = {
    // 获取全部测站水位数据 - 修改路径以匹配后端控制器
    getWaterLevelData(params) {
        return request({
            url: '/water-level/data', // 修改为正确的路径
            method: 'get',
            params
        })
    }
} 

export const envWaterApi = {
    // 获取大舜or横港水位数据，希望得到 时间localDateTime+水位数据
    getWaterLevelData(params) {
        return request({
            url: '/water-level/env', // 修改为正确的路径
            method: 'get',
            params
        })
    }
} 