// src/api/strategy.js
import request from '@/utils/request'

export function getStrategies() {
  return request({
    url: '/strategies',
    method: 'get'
  })
}

export function simulateStrategy(id) {
  return request({
    url: `/strategies/${id}/simulate`,
    method: 'post'
  })
}