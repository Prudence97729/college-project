import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gateApi } from '@/api/gate'

export function useGateOperation() {
  const loading = ref(false)
  const gates = ref([])
  
  // 获取水闸列表
  const fetchGates = async () => {
    loading.value = true
    try {
      const res = await gateApi.getGateList()
      if (res.code === 200) {
        gates.value = res.data || []
      } else {
        ElMessage.error(res.message || '获取水闸列表失败')
      }
    } catch (error) {
      console.error('获取水闸列表失败:', error)
      ElMessage.error('获取水闸列表失败')
    } finally {
      loading.value = false
    }
  }

  // 更新水闸状态
  const updateGateStatus = async (gate) => {
    const newStatus = gate.status === 'open' ? 'closed' : 'open'
    const actionText = newStatus === 'open' ? '开启' : '关闭'
    
    try {
      await ElMessageBox.confirm(
        `确定要${actionText}水闸 "${gate.gateName}" 吗？`,
        '操作确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      loading.value = true
      const res = await gateApi.updateGateStatus(gate.id, newStatus)
      
      if (res.code === 200) {
        gate.status = newStatus
        ElMessage.success(`${actionText}水闸成功`)
        return true
      } else {
        ElMessage.error(res.message || `${actionText}水闸失败`)
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('操作失败:', error)
        ElMessage.error('操作失败，请重试')
      }
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    gates,
    fetchGates,
    updateGateStatus
  }
} 