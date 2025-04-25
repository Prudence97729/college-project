/**
 * 格式化时间
 * @param {Date|string} time 时间对象或时间字符串
 * @returns {string} 格式化后的时间字符串
 */
export const formatTime = (time) => {
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化水位数据
 * @param {number} value 水位值
 * @returns {string} 格式化后的水位值
 */
export const formatWaterLevel = (value) => {
  return value.toFixed(2) + ' m'
} 