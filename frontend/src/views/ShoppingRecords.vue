<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const error = ref('')
const dateRange = ref([])
const orderNo = ref('')
const selected = ref(new Set())

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const params = {}
    if (dateRange.value?.length === 2) {
      const [s, e] = dateRange.value
      const fmt = (d, end = false) => {
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        return `${y}-${m}-${day} ${end ? '23:59:59' : '00:00:00'}`
      }
      params.start = fmt(s)
      params.end = fmt(e, true)
    }
    if (orderNo.value) params.orderNo = orderNo.value
    const { data } = await http.get('/api/shopping-records', { params })
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

const toggleSelect = (id, checked) => {
  if (checked) selected.value.add(id)
  else selected.value.delete(id)
}

const removeItem = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' })
  } catch (e) { return }
  try {
    const { data } = await http.delete(`/api/shopping-records/${id}`)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      await load()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

const search = async () => { await load() }

const removeBatch = async () => {
  if (!selected.value.size) { ElMessage.error('请选择记录'); return }
  try {
    await ElMessageBox.confirm(`确认删除选中的${selected.value.size}条记录？`, '提示', { type: 'warning' })
  } catch (e) { return }
  try {
    const ids = Array.from(selected.value)
    const { data } = await http.delete('/api/shopping-records/batch', { data: ids })
    if (data && data.code === 1) {
      ElMessage.success('批量删除成功')
      selected.value.clear()
      await load()
    } else {
      ElMessage.error(data?.msg || '批量删除失败')
    }
  } catch (e) { ElMessage.error('请求失败') }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="filters">
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" format="YYYY-MM-DD" />
      <el-input v-model="orderNo" placeholder="订单号" class="order-input" />
      <el-button class="btn" type="primary" @click="search">查询</el-button>
      <el-button class="btn danger" type="danger" @click="removeBatch">批量删除</el-button>
    </div>
    <h2>购物记录</h2>
    <div class="list">
      <div v-for="(r, index) in list" :key="r.recordId" class="row">
        <input type="checkbox" :checked="selected.has(r.recordId)" @change="toggleSelect(r.recordId, $event.target.checked)" />
        <span class="index-num">#{{ index + 1 }}</span>
        <img :src="r.productImage || 'https://via.placeholder.com/80x80?text=Img'" />
        <div class="info">
          <div class="name">{{ r.productName }}</div>
          <div class="meta">订单号 {{ r.orderNo }}</div>
        </div>
        <div class="price">¥{{ r.price }}</div>
        <div class="qty">x{{ r.quantity }}</div>
        <div class="subtotal">小计 ¥{{ r.subtotal }}</div>
        <button class="btn gray" @click="removeItem(r.recordId)">删除</button>
      </div>
    </div>
    <div v-if="!loading && list.length===0" class="empty">暂无记录</div>
    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<style scoped>
.filters{display:flex;gap:8px;align-items:center;margin-bottom:10px}
.order-input{max-width:220px}
.list{display:flex;flex-direction:column;gap:8px}
.row{display:grid;grid-template-columns:24px 30px 80px 1fr 120px 80px 160px 100px;gap:10px;align-items:center;padding:12px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.index-num{font-weight:bold;color:#6b7280;min-width:30px}
.row img{width:80px;height:80px;object-fit:cover;border-radius:8px;background:#f3f4f6}
.name{font-weight:600}
.meta{color:#6b7280;font-size:12px}
.price{color:#ef4444;font-weight:600}
.qty{color:#374151}
.subtotal{color:#111827;font-weight:600}
.empty{padding:24px;text-align:center;color:#999}
.error{padding:12px;color:#d33}
.btn{padding:8px 12px;border:none;border-radius:10px;background:#e5e7eb;color:#111827;cursor:pointer}
.btn.danger{background:#ef4444;color:#fff}
</style>
