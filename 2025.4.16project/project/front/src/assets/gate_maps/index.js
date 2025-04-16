import MaXiaHu from './MaXiaHu.vue'
import XiTangGang from './XiTangGang.vue'
import HouChangGang from './HouChangGang.vue'
import BeiXiangFu from './BeiXiangFu.vue'
import GangNanBang from './GangNanBang.vue'
import HongLingTang from './HongLingTang.vue'

export const gateMapComponents = {
  '马斜湖枢纽': MaXiaHu,
  '西塘港闸站': XiTangGang,
  '后场港闸': HouChangGang,
  '北祥符荡东口水闸': BeiXiangFu,
  '港南浜水闸': GangNanBang,
  '红菱塘水闸': HongLingTang
}

export const getGateMap = (gateName) => {
  for (const [key, component] of Object.entries(gateMapComponents)) {
    if (gateName?.includes(key)) {
      return component
    }
  }
  return null
} 