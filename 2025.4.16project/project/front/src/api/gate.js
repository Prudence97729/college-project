import request from '@/utils/request'

export const gateApi = {
    // 获取水闸列表
    getGateList() {
        return request({
            url: '/gates',
            method: 'get'
        })
    },

    // 获取单个水闸详情
    getGateDetail(id) {
        return request({
            url: `/gates/${id}`,
            method: 'get'
        })
    },

    // 更新水闸状态
    updateGateStatus(id, status) {
        return request({
            url: `/gates/${id}/status`,
            method: 'put',
            params: { status }
        })
    }
}
