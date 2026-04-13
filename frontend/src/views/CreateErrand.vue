<script setup>
import { ref, onMounted, watch } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'
const title = ref('')
const description = ref('')
const pickupAddress = ref('')
const deliveryAddress = ref('')
const contactName = ref('')
const contactPhone = ref('')
const reward = ref(5)
const errandType = ref(1)
const userId = Number(localStorage.getItem('userId') || 0)
const message = ref('')
const loading = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const estimating = ref(false)
const estimatedDistance = ref('')
const amapKey = import.meta.env.VITE_AMAP_KEY || 'YOUR_AMAP_KEY_HERE'
const amapSecurityJsCode = import.meta.env.VITE_AMAP_SECURITY_JS_CODE || ''
const pickupLongitude = ref(null)
const pickupLatitude = ref(null)
const deliveryLongitude = ref(null)
const deliveryLatitude = ref(null)
const showPickupMap = ref(false)
const draftKey = 'errandDraft'
onMounted(() => {
  try {
    const raw = localStorage.getItem(draftKey)
    if (raw) {
      const d = JSON.parse(raw)
      if (d.title != null) title.value = d.title
      if (d.description != null) description.value = d.description
      if (d.pickupAddress != null) pickupAddress.value = d.pickupAddress
      if (d.deliveryAddress != null) deliveryAddress.value = d.deliveryAddress
      if (d.contactName != null) contactName.value = d.contactName
      if (d.contactPhone != null) contactPhone.value = d.contactPhone
      if (d.reward != null) reward.value = d.reward
      if (d.errandType != null) errandType.value = d.errandType
      if (d.selectedAddressId != null) selectedAddressId.value = d.selectedAddressId
      if (d.pickupLongitude != null) pickupLongitude.value = d.pickupLongitude
      if (d.pickupLatitude != null) pickupLatitude.value = d.pickupLatitude
      if (d.deliveryLongitude != null) deliveryLongitude.value = d.deliveryLongitude
      if (d.deliveryLatitude != null) deliveryLatitude.value = d.deliveryLatitude
      if (d.estimatedDistance != null) estimatedDistance.value = d.estimatedDistance
    }
  } catch (e) {}
})
const saveDraft = () => {
  const d = {
    title: title.value,
    description: description.value,
    pickupAddress: pickupAddress.value,
    deliveryAddress: deliveryAddress.value,
    contactName: contactName.value,
    contactPhone: contactPhone.value,
    reward: reward.value,
    errandType: errandType.value,
    selectedAddressId: selectedAddressId.value,
    pickupLongitude: pickupLongitude.value,
    pickupLatitude: pickupLatitude.value,
    deliveryLongitude: deliveryLongitude.value,
    deliveryLatitude: deliveryLatitude.value,
    estimatedDistance: estimatedDistance.value
  }
  try { localStorage.setItem(draftKey, JSON.stringify(d)) } catch (e) {}
}
watch([title, description, pickupAddress, deliveryAddress, contactName, contactPhone, reward, errandType, selectedAddressId, pickupLongitude, pickupLatitude, deliveryLongitude, deliveryLatitude, estimatedDistance], saveDraft)
const formatAddress = (item) => [item.address, item.building, item.room].filter(Boolean).join(' ')
const loadAddresses = async () => {
  try {
    const { data } = await http.get('/addresses')
    if (data && data.code === 1) {
      addresses.value = data.data || []
      if (selectedAddressId.value) {
        applyAddress(selectedAddressId.value)
        return
      }
      const def = addresses.value.find(item => item.isDefault === 1) || addresses.value[0]
      if (def) {
        selectedAddressId.value = def.addressId
        applyAddress(def.addressId)
      }
    }
  } catch (e) {}
}
const applyAddress = (id) => {
  const item = addresses.value.find(addr => addr.addressId === id)
  if (!item) return
  deliveryAddress.value = formatAddress(item)
  contactName.value = item.contactName || contactName.value
  contactPhone.value = item.contactPhone || contactPhone.value
  deliveryLongitude.value = item.longitude ?? null
  deliveryLatitude.value = item.latitude ?? null
}
const ensureAmapKey = () => {
  if (!amapKey || amapKey === 'YOUR_AMAP_KEY_HERE') {
    ElMessage.error('未配置地图 Key，暂时无法智能定位和估价')
    return false
  }
  return true
}
const ensureAmapSecurity = () => {
  if (typeof window !== 'undefined' && amapSecurityJsCode) {
    window['_AMapSecurityConfig'] = { securityJsCode: amapSecurityJsCode }
  }
}
const loadAmapScript = () => {
  return new Promise((resolve, reject) => {
    ensureAmapSecurity()
    if (typeof window !== 'undefined' && window['AMap']) {
      resolve(window['AMap'])
      return
    }
    if (!ensureAmapKey()) {
      reject(new Error('未配置地图 Key'))
      return
    }
    const existed = document.querySelector('script[data-amap-sdk]')
    if (existed) {
      existed.addEventListener('load', () => resolve(window['AMap']))
      existed.addEventListener('error', reject)
      return
    }
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${amapKey}&plugin=AMap.Geocoder`
    script.async = true
    script.defer = true
    script.setAttribute('data-amap-sdk', 'true')
    script.onload = () => resolve(window['AMap'])
    script.onerror = reject
    document.head.appendChild(script)
  })
}
const reverseGeocodeBySdk = async (longitude, latitude) => {
  const AMap = await loadAmapScript()
  return new Promise((resolve, reject) => {
    const geocoder = new AMap.Geocoder()
    geocoder.getAddress([longitude, latitude], (status, result) => {
      const formatted = result?.regeocode?.formattedAddress || result?.regeocode?.formatted_address || ''
      if (status === 'complete' && formatted) resolve(formatted)
      else reject(new Error('逆地理解析失败'))
    })
  })
}
const geocodeAddressBySdk = async (address) => {
  const AMap = await loadAmapScript()
  return new Promise((resolve, reject) => {
    const geocoder = new AMap.Geocoder()
    geocoder.getLocation(address, (status, result) => {
      const location = result?.geocodes?.[0]?.location
      const lng = typeof location?.getLng === 'function' ? location.getLng() : Number(location?.lng)
      const lat = typeof location?.getLat === 'function' ? location.getLat() : Number(location?.lat)
      if (status === 'complete' && !Number.isNaN(lng) && !Number.isNaN(lat)) {
        resolve({ longitude: lng, latitude: lat })
      } else {
        reject(new Error('地址解析失败'))
      }
    })
  })
}
const openPickupMap = async () => {
  showPickupMap.value = true
  try {
    const AMap = await loadAmapScript()
    setTimeout(() => {
      const container = document.getElementById('pickup-map-container')
      if (!container) return
      const center = (pickupLongitude.value != null && pickupLatitude.value != null)
        ? [Number(pickupLongitude.value), Number(pickupLatitude.value)]
        : [117.120497, 36.650997]
      const map = new AMap.Map('pickup-map-container', {
        zoom: 16,
        center
      })
      const marker = new AMap.Marker({ position: center, map })
      const geocoder = new AMap.Geocoder()
      map.on('click', async (e) => {
        const lnglat = [e.lnglat.lng, e.lnglat.lat]
        marker.setPosition(lnglat)
        pickupLongitude.value = e.lnglat.lng
        pickupLatitude.value = e.lnglat.lat
        try {
          pickupAddress.value = await reverseGeocodeBySdk(e.lnglat.lng, e.lnglat.lat)
          ElMessage.success('已从地图选择取件地址')
          showPickupMap.value = false
        } catch (e) {
          geocoder.getAddress(lnglat, (status, result) => {
            const formatted = result?.regeocode?.formattedAddress || result?.regeocode?.formatted_address || ''
            if (status === 'complete' && formatted) {
              pickupAddress.value = formatted
              ElMessage.success('已从地图选择取件地址')
              showPickupMap.value = false
            } else {
              ElMessage.error('地址解析失败，请检查地图配置或网络')
            }
          })
        }
      })
    }, 0)
  } catch (e) {
    ElMessage.error('地图加载失败，请检查 Key、安全密钥或网络')
  }
}
const calcDistanceKm = (lng1, lat1, lng2, lat2) => {
  const toRad = (deg) => deg * Math.PI / 180
  const R = 6371
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(a))
}
const estimateReward = async () => {
  if (!pickupAddress.value || !deliveryAddress.value) {
    ElMessage.error('请先填写取件地址并选择送达地址')
    return
  }
  if (!ensureAmapKey()) return
  estimating.value = true
  try {
    const pickupPoint = (pickupLongitude.value != null && pickupLatitude.value != null)
      ? { longitude: Number(pickupLongitude.value), latitude: Number(pickupLatitude.value) }
      : await geocodeAddressBySdk(pickupAddress.value)
    const deliveryPoint = (deliveryLongitude.value != null && deliveryLatitude.value != null)
      ? { longitude: Number(deliveryLongitude.value), latitude: Number(deliveryLatitude.value) }
      : await geocodeAddressBySdk(deliveryAddress.value)
    pickupLongitude.value = pickupPoint.longitude
    pickupLatitude.value = pickupPoint.latitude
    deliveryLongitude.value = deliveryPoint.longitude
    deliveryLatitude.value = deliveryPoint.latitude
    const km = calcDistanceKm(pickupPoint.longitude, pickupPoint.latitude, deliveryPoint.longitude, deliveryPoint.latitude)
    const suggested = Math.max(5, Math.ceil(km * 2 + 3))
    estimatedDistance.value = `${km.toFixed(2)} km`
    reward.value = suggested
    ElMessage.success(`已按距离估价，建议赏金 ${suggested} 元`)
  } catch (e) {
    ElMessage.error('估价失败，请检查地址是否足够准确')
  } finally {
    estimating.value = false
  }
}
const locatePickup = () => {
  if (!navigator.geolocation) {
    ElMessage.error('当前浏览器不支持定位')
    return
  }
  if (!ensureAmapKey()) return
  navigator.geolocation.getCurrentPosition(async (pos) => {
    try {
      const { longitude, latitude } = pos.coords
      pickupLongitude.value = longitude
      pickupLatitude.value = latitude
      pickupAddress.value = await reverseGeocodeBySdk(longitude, latitude)
      ElMessage.success('已定位取件地址')
    } catch (e) {
      ElMessage.error('定位成功但地址解析失败，请改用地图选点')
    }
  }, () => {
    ElMessage.error('无法获取定位，请检查浏览器权限')
  })
}
const submit = async () => {
  loading.value = true
  message.value = ''
  try {
    if (!title.value || !pickupAddress.value || !deliveryAddress.value || !contactName.value || !contactPhone.value) {
      message.value = '请完整填写信息'
      return
    }
    const body = {
      title: title.value,
      description: description.value,
      pickupAddress: pickupAddress.value,
      deliveryAddress: deliveryAddress.value,
      contactName: contactName.value,
      contactPhone: contactPhone.value,
      reward: reward.value,
      errandType: errandType.value,
      userId: userId,
      tip: 0,
      totalAmount: reward.value
    }
    const { data } = await http.post('/api/errands/create', body)
    if (data && data.code === 1) {
      message.value = '订单创建成功'
      title.value = ''
      description.value = ''
      pickupAddress.value = ''
      deliveryAddress.value = ''
      contactName.value = ''
      contactPhone.value = ''
      reward.value = 5
      errandType.value = 1
      selectedAddressId.value = null
      estimatedDistance.value = ''
      pickupLongitude.value = null
      pickupLatitude.value = null
      deliveryLongitude.value = null
      deliveryLatitude.value = null
      try { localStorage.removeItem(draftKey) } catch (e) {}
    } else {
      message.value = data && data.msg ? data.msg : '创建失败'
    }
  } catch (e) {
    message.value = '请求失败'
  } finally {
    loading.value = false
  }
}
onMounted(loadAddresses)
</script>

<template>
  <div class="form">
    <h2>发布跑腿</h2>
    <div class="tips">送达地址可直接从“我的地址”中选择，系统会根据定位信息智能估算赏金。</div>
    <div class="row">
      <label>标题</label>
      <el-input v-model="title" placeholder="请输入标题" />
    </div>
    <div class="row">
      <label>描述</label>
      <el-input v-model="description" type="textarea" :rows="4" placeholder="请输入任务描述" />
    </div>
    <div class="row">
      <label>取件地址</label>
      <div class="inline-row">
        <el-input v-model="pickupAddress" placeholder="请输入取件地址" />
        <button class="btn gray mini" type="button" @click="locatePickup">定位</button>
        <button class="btn gray mini" type="button" @click="openPickupMap">地图选点</button>
      </div>
    </div>
    <div class="row">
      <label>送达地址</label>
      <el-select v-model="selectedAddressId" placeholder="从我的地址中选择" @change="applyAddress" clearable>
        <el-option
          v-for="item in addresses"
          :key="item.addressId"
          :label="`${item.contactName} - ${formatAddress(item)}`"
          :value="item.addressId"
        />
      </el-select>
      <el-input v-model="deliveryAddress" placeholder="选择地址后会自动带入，也可手动调整" />
    </div>
    <div class="row">
      <label>联系人</label>
      <el-input v-model="contactName" placeholder="请输入联系人姓名" />
    </div>
    <div class="row">
      <label>联系电话</label>
      <el-input v-model="contactPhone" placeholder="请输入联系电话" />
    </div>
    <div class="row">
      <label>类型</label>
      <el-select v-model="errandType" placeholder="请选择类型">
        <el-option :value="1" label="取快递" />
        <el-option :value="2" label="送外卖" />
        <el-option :value="3" label="代买" />
        <el-option :value="4" label="其他" />
      </el-select>
    </div>
    <div class="row">
      <label>赏金</label>
      <div class="reward-row">
        <el-input-number v-model="reward" :min="0" :step="1" />
        <button class="btn gray mini" type="button" :disabled="estimating" @click="estimateReward">
          {{ estimating ? '估价中...' : '智能估价' }}
        </button>
      </div>
      <div v-if="estimatedDistance" class="sub-tip">预估距离：{{ estimatedDistance }}，当前已自动回填建议赏金。</div>
    </div>
    <button class="btn" :disabled="loading" @click="submit">发布</button>
    <div class="msg" v-if="message">{{ message }}</div>
    <el-dialog v-model="showPickupMap" title="选择取件地址" width="720px">
      <div id="pickup-map-container" class="map-container"></div>
      <div class="sub-tip">点击地图位置后自动回填标准地址，不再显示纯坐标。</div>
    </el-dialog>
  </div>
</template>

<style scoped>
.form{width:100%;padding:16px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.row{display:flex;flex-direction:column;gap:6px;margin-bottom:12px}
.tips{margin-bottom:12px;padding:10px 12px;border-radius:10px;background:#f0fdf4;color:#166534;font-size:13px}
.inline-row{display:flex;gap:8px;align-items:center}
.reward-row{display:flex;gap:8px;align-items:center}
.btn{margin-top:6px;padding:8px 12px;border:none;border-radius:10px;background:#42b883;color:#fff;cursor:pointer}
.btn.gray{background:#e5e7eb;color:#111827}
.btn.mini{margin-top:0;white-space:nowrap}
.btn[disabled]{opacity:0.6;cursor:not-allowed}
.msg{margin-top:12px;color:#42b883}
.sub-tip{font-size:13px;color:#6b7280}
.map-container{width:100%;height:420px;border-radius:12px;overflow:hidden;margin-bottom:8px}
</style>
