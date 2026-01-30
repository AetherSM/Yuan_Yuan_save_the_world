<script setup>
import { ref, onMounted, computed } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'
const list = ref([])
const loading = ref(false)
const error = ref('')
const status = ref(null) // 1-待支付,2-待发货,3-待收货,4-已完成,5-已取消
const keyword = ref('')
const displayedList = computed(() => {
  const kw = (keyword.value || '').trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter(o => {
    const s = [
      o.orderNo,
      o.contactName,
      o.contactPhone,
      o.deliveryAddress
    ].map(x => String(x || '').toLowerCase()).join(' ')
    return s.includes(kw)
  })
})
const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const params = {}
    if (status.value) params.status = status.value
    const { data } = await http.get('/orders', { params })
    if (data && data.code === 1) {
      list.value = data.data || []
    } else {
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
onMounted(load)
const statusText = (s) => ({
  1: '待支付',
  2: '待发货',
  3: '待收货',
  4: '已完成',
  5: '已取消'
}[s] || '未知')
const cancel = async (orderNo) => {
  try {
    const { data } = await http.post(`/orders/${orderNo}/cancel`, { cancelReason: '用户取消' })
    if (data && data.code === 1) { ElMessage.success('已取消'); await load() }
    else { ElMessage.error(data?.msg || '取消失败') }
  } catch (e) { ElMessage.error('请求失败') }
}
const confirm = async (orderNo) => {
  try {
    const { data } = await http.post(`/orders/${orderNo}/confirm`)
    if (data && data.code === 1) { ElMessage.success('已确认收货'); await load() }
    else { ElMessage.error(data?.msg || '确认失败') }
  } catch (e) { ElMessage.error('请求失败') }
}
</script>

<template>
  <div>
    <div class="toolbar">
      <label>筛选：</label>
      <select v-model="status" @change="load">
        <option :value="null">全部</option>
        <option :value="1">待支付</option>
        <option :value="2">待发货</option>
        <option :value="3">待收货</option>
        <option :value="4">已完成</option>
        <option :value="5">已取消</option>
      </select>
      <button class="btn gray" @click="load">刷新</button>
      <input class="search" v-model="keyword" placeholder="搜索订单号/地址/联系人" />
    </div>
    <div v-for="o in displayedList" :key="o.orderId" class="card">
      <div class="row">
        <div class="title">订单号 {{ o.orderNo }}</div>
        <div class="status">{{ statusText(o.orderStatus) }}</div>
      </div>
      <div class="row info">
        <div>总金额 ¥{{ o.totalAmount }}</div>
        <div>收货人 {{ o.contactName }}（{{ o.contactPhone }}）</div>
      </div>
      <div class="desc">地址：{{ o.deliveryAddress }}</div>
      <div class="ops">
        <button v-if="o.orderStatus===1" class="btn gray" @click="cancel(o.orderNo)">取消订单</button>
        <button v-if="o.orderStatus===3" class="btn" @click="confirm(o.orderNo)">确认收货</button>
      </div>
    </div>
    <div v-if="!loading && list.length===0" class="empty">暂无订单</div>
    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<style scoped>
.toolbar{display:flex;gap:8px;align-items:center;margin-bottom:12px}
.search{flex:1;max-width:280px;border:1px solid #e5e7eb;border-radius:8px;padding:6px}
.card{padding:12px;border:1px solid #eee;border-radius:8px;background:#fff;margin-bottom:10px}
.row{display:flex;justify-content:space-between;align-items:center;margin:6px 0}
.title{font-weight:600}
.status{color:#999}
.info{color:#374151}
.desc{color:#6b7280;font-size:14px}
.btn{padding:8px 12px;border:none;border-radius:10px;background:#42b883;color:#fff;cursor:pointer}
.btn.gray{background:#e5e7eb;color:#111827}
.ops{display:flex;gap:8px;margin-top:8px}
.empty{padding:24px;text-align:center;color:#999}
.error{padding:12px;color:#d33}
</style>
