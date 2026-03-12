<script setup>
import { ref, onMounted, computed } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'
const orders = ref([])
const loading = ref(false)
const error = ref('')
const userId = localStorage.getItem('userId')
const status = ref(null) // null: 全部, 4: 已完成, 5: 已取消
const keyword = ref('')
const displayed = computed(() => {
  const kw = (keyword.value || '').trim().toLowerCase()
  const src = orders.value
  if (!kw) return src
  return src.filter(item => {
    const s = [
      item.orderNo,
      item.title,
      item.pickupAddress,
      item.deliveryAddress,
      item.contactName,
      item.contactPhone,
      item.pickupCode
    ].map(x => String(x || '').toLowerCase()).join(' ')
    return s.includes(kw)
  })
})
const reviewVisible = ref(false)
const currentOrderNo = ref('')
const rating = ref(5)
const content = ref('')
const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const params = { userId }
    if (status.value) params.status = status.value
    const { data } = await http.get('/api/errands/my', { params })
    if (data && data.code === 1) {
      orders.value = data.data || []
    } else {
      error.value = data && data.msg ? data.msg : '加载失败'
    }
  } catch (e) {
    error.value = '请求失败'
  } finally {
    loading.value = false
  }
}
onMounted(load)
const complete = async (orderNo) => {
  try {
    const { data } = await http.post('/api/errands/complete', null, { params: { orderNo } })
    if (data && data.code === 1) { ElMessage.success('已完成'); await load() }
    else { error.value = data?.msg || '完成失败'; ElMessage.error(error.value) }
  } catch (e) { error.value = '请求失败'; ElMessage.error(error.value) }
}
const openReview = (orderNo) => {
  currentOrderNo.value = orderNo
  rating.value = 5
  content.value = ''
  reviewVisible.value = true
}
const submitReview = async () => {
  try {
    const body = { rating: rating.value, content: content.value, orderType: 2 }
    const { data } = await http.post(`/orders/${currentOrderNo.value}/reviews`, body)
    if (data && data.code === 1) { ElMessage.success('评价成功'); reviewVisible.value = false }
    else { ElMessage.error(data?.msg || '评价失败') }
  } catch (e) { ElMessage.error('请求失败') }
}

const statusMap = {
  1: '待接单',
  2: '已接单',
  3: '配送中',
  4: '已完成',
  5: '已取消',
  7: '退款中',
  8: '已退款'
}
const canRefundErrand = (item) => [2, 3, 4].includes(item.orderStatus)
const refundDialog = ref(false)
const refundForm = ref({ orderNo: '', orderType: 2, refundAmount: 0, reason: '' })
const refundSubmitting = ref(false)
const openRefund = (item) => {
  refundForm.value = { orderNo: item.orderNo, orderType: 2, refundAmount: Number(item.totalAmount) || Number(item.reward) || 0, reason: '' }
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
</script>

<template>
  <div>
    <h2>我的订单</h2>
    <div class="toolbar">
      <label>筛选：</label>
      <select v-model="status" @change="load">
        <option :value="null">全部</option>
        <option :value="1">进行中</option>
        <option :value="4">历史-已完成</option>
        <option :value="5">历史-已取消</option>
      </select>
      <button class="btn gray" @click="load">刷新</button>
      <input class="search" v-model="keyword" placeholder="搜索订单号/标题/地址/联系人" />
    </div>
    <div v-for="item in displayed" :key="item.orderId" class="card">
      <div class="row">
        <div class="title">{{ item.title }}</div>
        <div class="status" :class="{'done': item.orderStatus===4}">{{ statusMap[item.orderStatus] }}</div>
      </div>
      <div class="desc">{{ item.description }}</div>
      <div class="row info">
        <span>赏金: ¥{{ item.reward }}</span>
        <span v-if="item.pickupCode">取件码: {{ item.pickupCode }}</span>
      </div>
      <div class="ops">
        <button v-if="item.orderStatus === 2 || item.orderStatus === 3" class="btn" @click="complete(item.orderNo)">确认完成</button>
        <button v-if="item.orderStatus === 4" class="btn gray" @click="openReview(item.orderNo)">评价</button>
        <button v-if="canRefundErrand(item)" class="btn warn" @click="openRefund(item)">申请退款</button>
      </div>
    </div>
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
    <div v-if="!loading && orders.length===0" class="empty">暂无数据</div>
    <div v-if="error" class="error">{{ error }}</div>
    <el-dialog v-model="reviewVisible" title="订单评价" width="520px">
      <div class="review-body">
        <div class="row">
          <span>评分：</span>
          <el-rate v-model="rating" :max="5" />
        </div>
        <div class="row">
          <span>内容：</span>
          <el-input v-model="content" type="textarea" :rows="3" placeholder="请输入评价内容" />
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn" @click="submitReview">提交评价</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.card{padding:12px;border:1px solid #eee;border-radius:8px;background:#fff;margin-bottom:10px}
.row{display:flex;justify-content:space-between;align-items:center;margin:6px 0}
.title{font-weight:600}
.status{color:#999}
.desc{color:#666;font-size:14px}
.toolbar{display:flex;gap:8px;align-items:center;margin-bottom:12px}
.search{flex:1;max-width:320px;border:1px solid #e5e7eb;border-radius:8px;padding:6px}
.ops{display:flex;gap:8px;margin-top:8px}
.btn{padding:8px 12px;border:none;border-radius:10px;background:#42b883;color:#fff;cursor:pointer}
.btn.gray{background:#e5e7eb;color:#111827}
.btn.warn{background:#f59e0b;color:#fff}
.refund-form p{margin:10px 0}
.empty{padding:24px;text-align:center;color:#999}
.error{padding:12px;color:#d33}
.review-body{display:flex;flex-direction:column;gap:12px}
</style>
