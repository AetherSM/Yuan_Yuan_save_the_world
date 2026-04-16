<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const error = ref('')
const status = ref('') // '': 全部, 1-待支付,2-待发货,3-待收货,4-已完成,5-已取消,6-退款中,7-已退款
const keyword = ref('')
const refundDialog = ref(false)
const refundForm = ref({ orderNo: '', orderType: 1, refundAmount: 0, reason: '' })
const refundSubmitting = ref(false)
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

const complaintDialog = ref(false)
const complaintForm = ref({ orderId: null, orderType: 1, reason: '' })
const complaintSubmitting = ref(false)

const openComplaint = (o) => {
  complaintForm.value = { orderId: o.orderId, orderType: 1, reason: '' }
  complaintDialog.value = true
}

const submitComplaint = async () => {
  if (!complaintForm.value.reason.trim()) {
    ElMessage.warning('请填写投诉原因')
    return
  }
  complaintSubmitting.value = true
  try {
    const userId = localStorage.getItem('userId')
    const { data } = await http.post('/api/complaints/submit', {
      ...complaintForm.value,
      complainantId: userId
    })
    if (data && data.code === 1) {
      ElMessage.success('投诉已提交')
      complaintDialog.value = false
    } else {
      ElMessage.error(data?.msg || '提交失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  } finally {
    complaintSubmitting.value = false
  }
}

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
  5: '已取消',
  6: '退款中',
  7: '已退款'
}[s] || '未知')
const canRefund = (o) => [2, 3, 4].includes(o.orderStatus)
const openRefund = (o) => {
  refundForm.value = { orderNo: o.orderNo, orderType: 1, refundAmount: Number(o.totalAmount) || 0, reason: '' }
  refundDialog.value = true
}
const submitRefund = async () => {
  if (!refundForm.value.orderNo || !refundForm.value.refundAmount || refundForm.value.refundAmount <= 0) {
    ElMessage.warning('请填写退款金额')
    return
  }
  refundSubmitting.value = true
  try {
    const { data } = await http.post('/refunds/apply', refundForm.value)
    if (data && data.code === 1) {
      ElMessage.success('退款申请已提交')
      refundDialog.value = false
      await load()
    } else {
      ElMessage.error(data?.msg || '申请失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '申请失败')
  } finally {
    refundSubmitting.value = false
  }
}
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

const viewDetail = (orderNo) => {
  router.push(`/order/${orderNo}`)
}
</script>

<template>
  <div>
    <div class="toolbar">
      <label>筛选：</label>
      <select v-model="status" @change="load">
        <option value="">全部</option>
        <option :value="1">待支付</option>
        <option :value="2">待发货</option>
        <option :value="3">待收货</option>
        <option :value="4">已完成</option>
        <option :value="5">已取消</option>
      </select>
      <button class="btn gray" @click="load">刷新</button>
      <input class="search" v-model="keyword" placeholder="搜索订单号/地址/联系人" />
    </div>
    <div v-for="o in displayedList" :key="o.orderId" class="card" @click="viewDetail(o.orderNo)" style="cursor: pointer;">
      <div class="row">
        <div class="title">订单号 {{ o.orderNo }}</div>
        <div class="status">{{ statusText(o.orderStatus) }}</div>
      </div>
      <div class="row info">
        <div>总金额 ¥{{ o.totalAmount }}</div>
        <div>收货人 {{ o.contactName }}（{{ o.contactPhone }}）</div>
      </div>
      <div v-if="o.itemSummary" class="items">商品：{{ o.itemSummary }}</div>
      <div class="desc">地址：{{ o.deliveryAddress }}</div>
      <div class="ops" @click.stop>
        <button v-if="o.orderStatus===1" class="btn gray" @click="cancel(o.orderNo)">取消订单</button>
        <button v-if="o.orderStatus===3" class="btn" @click="confirm(o.orderNo)">确认收货</button>
        <button v-if="canRefund(o)" class="btn warn" @click="openRefund(o)">申请退款</button>
        <button class="btn danger" @click="openComplaint(o)">投诉</button>
      </div>
    </div>
    <el-dialog v-model="complaintDialog" title="发起投诉" width="400">
      <div class="complaint-form">
        <p>订单号：{{ displayedList.find(it => it.orderId === complaintForm.orderId)?.orderNo }}</p>
        <p>投诉原因：</p>
        <el-input v-model="complaintForm.reason" type="textarea" rows="4" placeholder="请详细描述您遇到的问题" />
      </div>
      <template #footer>
        <el-button @click="complaintDialog = false">取消</el-button>
        <el-button type="danger" @click="submitComplaint" :loading="complaintSubmitting">提交投诉</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="refundDialog" title="申请退款" width="400">
      <div class="refund-form">
        <p>订单号：{{ refundForm.orderNo }}</p>
        <p>退款金额：<el-input-number v-model="refundForm.refundAmount" :min="0.01" :precision="2" size="small" /></p>
        <p>退款原因：<el-input v-model="refundForm.reason" type="textarea" rows="2" placeholder="选填" /></p>
      </div>
      <template #footer>
        <el-button @click="refundDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRefund" :loading="refundSubmitting">提交申请</el-button>
      </template>
    </el-dialog>
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
.items{color:#374151;font-size:14px;margin:6px 0}
.desc{color:#6b7280;font-size:14px}
.btn{padding:8px 12px;border:none;border-radius:10px;background:#42b883;color:#fff;cursor:pointer}
.btn.gray{background:#e5e7eb;color:#111827}
.btn.warn{background:#f59e0b;color:#fff}
.btn.danger{background:#ef4444;color:#fff}
.ops{display:flex;gap:8px;margin-top:8px}
.refund-form p{margin:10px 0}
.empty{padding:24px;text-align:center;color:#999}
.error{padding:12px;color:#d33}
</style>
