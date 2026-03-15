<script setup>
import { ref, onMounted, nextTick } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'
const list = ref([])
const loading = ref(false)
const error = ref('')
const form = ref({ contactName: '', contactPhone: '', address: '', building: '', room: '', longitude: null, latitude: null })
const editingId = ref(null)
const editForm = ref({ contactName: '', contactPhone: '', address: '', building: '', room: '', longitude: null, latitude: null })
const editVisible = ref(false)
const amapKey = import.meta.env.VITE_AMAP_KEY || 'YOUR_AMAP_KEY_HERE'
const showMap = ref(false)
const mapInited = ref(false)
const mapInstance = ref(null)
const mapMarker = ref(null)
const geocoderRef = ref(null)

const loadAmapScript = () => {
  return new Promise((resolve, reject) => {
    if (typeof window !== 'undefined' && window.AMap) {
      resolve(window.AMap)
      return
    }
    if (!amapKey || amapKey === 'YOUR_AMAP_KEY_HERE') {
      reject(new Error('未配置高德地图 Key'))
      return
    }
    const existed = document.querySelector('script[data-amap-sdk]')
    if (existed) {
      existed.addEventListener('load', () => resolve(window.AMap))
      existed.addEventListener('error', reject)
      return
    }
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${amapKey}&plugin=AMap.Geocoder,AMap.Geolocation`
    script.async = true
    script.defer = true
    script.setAttribute('data-amap-sdk', 'true')
    script.onload = () => resolve(window.AMap)
    script.onerror = reject
    document.head.appendChild(script)
  })
}

const openMapPicker = async () => {
  showMap.value = true
  await nextTick()
  if (mapInited.value) return
  try {
    const AMap = await loadAmapScript()
    const container = document.getElementById('address-map-container')
    if (!container) return
    mapInstance.value = new AMap.Map('address-map-container', {
      zoom: 16,
      center: [109.0, 34.0]
    })
    geocoderRef.value = new AMap.Geocoder()
    mapInstance.value.on('click', (e) => {
      const lnglat = [e.lnglat.lng, e.lnglat.lat]
      if (!mapMarker.value) {
        mapMarker.value = new AMap.Marker({ position: lnglat, map: mapInstance.value })
      } else {
        mapMarker.value.setPosition(lnglat)
      }
      form.value.longitude = e.lnglat.lng
      form.value.latitude = e.lnglat.lat
      if (geocoderRef.value) {
        geocoderRef.value.getAddress(lnglat, (status, result) => {
          if (status === 'complete' && result.regeocode) {
            form.value.address = result.regeocode.formattedAddress || ''
          }
        })
      }
    })
    mapInited.value = true
  } catch (e) {
    ElMessage.error('加载地图失败，请检查高德 Key 或网络')
  }
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get('/addresses')
    if (data && data.code === 1) list.value = data.data || []
    else {
      error.value = data?.msg || '加载失败'
      ElMessage.error(error.value)
    }
  } catch (e) {
    error.value = '请求失败'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}
const save = async () => {
  try {
    if (!editingId.value) return
    const res = await http.put(`/addresses/${editingId.value}`, editForm.value)
    const data = res.data
    if (data && data.code === 1) {
      editForm.value = { contactName: '', contactPhone: '', address: '', building: '', room: '' }
      editingId.value = null
      editVisible.value = false
      await load()
      ElMessage.success('更新成功')
    } else {
      error.value = data?.msg || '更新失败'
      ElMessage.error(error.value)
    }
  } catch (e) { 
    error.value = '请求失败'
    ElMessage.error(error.value)
  }
}
const create = async () => {
  try {
    const res = await http.post('/addresses', form.value)
    const data = res.data
    if (data && data.code === 1) {
      form.value = { contactName: '', contactPhone: '', address: '', building: '', room: '' }
      await load()
      ElMessage.success('创建成功')
    } else {
      error.value = data?.msg || '创建失败'
      ElMessage.error(error.value)
    }
  } catch (e) {
    error.value = '请求失败'
    ElMessage.error(error.value)
  }
}
const locate = () => {
  if (!navigator.geolocation) {
    ElMessage.error('当前浏览器不支持定位')
    return
  }
  navigator.geolocation.getCurrentPosition(async (pos) => {
    try {
      const { longitude, latitude } = pos.coords
      if (!amapKey || amapKey === 'YOUR_AMAP_KEY_HERE') {
        form.value.address = `${longitude.toFixed(6)},${latitude.toFixed(6)}`
        ElMessage.info('已填入经纬度，请完善详细地址')
        return
      }
      const resp = await fetch(`https://restapi.amap.com/v3/geocode/regeo?key=${amapKey}&location=${longitude},${latitude}&radius=1000&extensions=base`)
      const data = await resp.json()
      if (data.status === '1' && data.regeocode && data.regeocode.formatted_address) {
        form.value.address = data.regeocode.formatted_address
        form.value.longitude = longitude
        form.value.latitude = latitude
        ElMessage.success('已根据定位填入地址，请确认后保存')
      } else {
        ElMessage.error('定位解析失败，请手动填写地址')
      }
    } catch (e) {
      ElMessage.error('获取定位失败，请检查网络或稍后重试')
    }
  }, () => {
    ElMessage.error('无法获取定位，请检查浏览器权限设置')
  })
}
const editItem = (a) => {
  editingId.value = a.addressId
  editForm.value = {
    contactName: a.contactName || '',
    contactPhone: a.contactPhone || '',
    address: a.address || '',
    building: a.building || '',
    room: a.room || '',
    longitude: a.longitude ?? null,
    latitude: a.latitude ?? null
  }
  editVisible.value = true
}
const cancelEdit = () => {
  editingId.value = null
  editForm.value = { contactName: '', contactPhone: '', address: '', building: '', room: '', longitude: null, latitude: null }
  editVisible.value = false
}
const removeItem = async (id) => {
  if (!window.confirm('确认删除该地址？')) return
  try {
    await http.delete(`/addresses/${id}`)
    await load()
    ElMessage.success('删除成功')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}
const setDefault = async (id) => {
  try {
    await http.patch(`/addresses/${id}/default`)
    await load()
    ElMessage.success('已设为默认')
  } catch (e) {
    ElMessage.error('设置失败')
  }
}
onMounted(load)
</script>

<template>
  <div>
    <div class="form">
      <h2>新增地址</h2>
      <div class="grid">
        <el-input v-model="form.contactName" placeholder="收货人" />
        <el-input v-model="form.contactPhone" placeholder="联系电话" />
        <el-input v-model="form.address" placeholder="详细地址" />
        <el-input v-model="form.building" placeholder="楼栋(可选)" />
        <el-input v-model="form.room" placeholder="房间号(可选)" />
      </div>
      <div class="row-actions">
        <button class="btn gray" @click="locate">一键定位</button>
        <button class="btn gray" @click="openMapPicker">地图选点</button>
        <button class="btn" @click="create">保存</button>
      </div>
    </div>
    <el-dialog v-model="editVisible" title="编辑地址" width="600px">
      <div class="grid">
        <el-input v-model="editForm.contactName" placeholder="收货人" />
        <el-input v-model="editForm.contactPhone" placeholder="联系电话" />
        <el-input v-model="editForm.address" placeholder="详细地址" />
        <el-input v-model="editForm.building" placeholder="楼栋(可选)" />
        <el-input v-model="editForm.room" placeholder="房间号(可选)" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn gray" @click="cancelEdit">取消</button>
          <button class="btn" @click="save">更新</button>
        </div>
      </template>
    </el-dialog>
    <h2>我的地址</h2>
    <div class="list">
      <div v-for="a in list" :key="a.addressId" class="item">
        <div class="info">
          <div class="name">{{ a.contactName }}（{{ a.contactPhone }}）</div>
          <div class="addr">{{ a.address }} {{ a.building }} {{ a.room }}</div>
        </div>
        <div class="ops">
          <span v-if="a.isDefault===1" class="badge">默认</span>
          <button @click="editItem(a)">编辑</button>
          <button @click="setDefault(a.addressId)">设为默认</button>
          <button @click="removeItem(a.addressId)">删除</button>
        </div>
      </div>
    </div>
    <div v-if="error" class="error">{{ error }}</div>
    <el-dialog v-model="showMap" title="选择地址" width="720px">
      <div id="address-map-container" class="map-container"></div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn gray" @click="showMap = false">关闭</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.form{width:100%;padding:16px;border:1px solid #e5e7eb;border-radius:12px;background:#fff;margin-bottom:12px}
.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:10px}
.grid input{padding:10px;border:1px solid #e5e7eb;border-radius:10px;background:#fff}
.row-actions{display:flex;gap:8px}
.btn{padding:10px 16px;border:none;border-radius:10px;background:#42b883;color:#fff;cursor:pointer}
.btn.gray{background:#e5e7eb;color:#111827}
.list{display:flex;flex-direction:column;gap:8px}
.item{display:flex;justify-content:space-between;align-items:center;padding:12px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.name{font-weight:600}
.addr{color:#6b7280}
.ops{display:flex;gap:8px;align-items:center}
.badge{background:#42b883;color:#fff;border-radius:999px;padding:4px 8px;font-size:12px}
.error{padding:12px;color:#d33}
.map-container{width:100%;height:420px;border-radius:12px;overflow:hidden}
</style>
