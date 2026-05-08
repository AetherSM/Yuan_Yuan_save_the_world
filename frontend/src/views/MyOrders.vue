<script setup>
import { ref, onMounted, computed } from 'vue'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'
const orders = ref([])
const loading = ref(false)
const error = ref('')
const userId = localStorage.getItem('userId')
const status = ref('') // '': 全部, -1: 进行中, 4: 已完成, 5: 已取消
const keyword = ref('')
const selected = ref(new Set())

const toggleSelect = (orderNo) => {
  if (selected.value.has(orderNo)) {
    selected.value.delete(orderNo)
  } else {
    selected.value.add(orderNo)
  }
}

const batchHide = async () => {
  if (selected.value.size === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要批量删除这 ${selected.value.size} 条跑腿订单记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { data } = await http.post('/api/errands/batch-hide', Array.from(selected.value))
    if (data && data.code === 1) {
      ElMessage.success('批量删除成功')
      selected.value.clear()
      await load()
    } else {
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('请求失败')
  }
}

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

const complaintDialog = ref(false)
const complaintForm = ref({ orderId: null, orderType: 2, reason: '' })
const complaintSubmitting = ref(false)

const openComplaint = (item) => {
  complaintForm.value = { orderId: item.orderId, orderType: 2, reason: '' }
  complaintDialog.value = true
}

const submitComplaint = async () => {
  if (!complaintForm.value.reason.trim()) {
    ElMessage.warning('请填写举报/投诉原因')
    return
  }
  complaintSubmitting.value = true
  try {
    const { data } = await http.post('/api/complaints/submit', {
      ...complaintForm.value,
      complainantId: userId
    })
    if (data && data.code === 1) {
      ElMessage.success('举报/投诉已提交')
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
    const params = { userId }
    if (status.value !== '' && status.value != null) params.status = status.value
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
    const { data } = await http.post(`/api/errands/complete?orderNo=${orderNo}`)
    if (data && data.code === 1) {
      ElMessage.success('订单已确认完成')
      await load()
    } else {
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
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
  0: '待审核',
  1: '待接单',
  2: '已接单',
  3: '配送中',
  4: '已完成',
  5: '已取消',
  6: '审核拒绝',
  7: '退款中',
  8: '已退款'
}
const statusLabel = (s) => statusMap[s] ?? `未知状态(${s ?? '-'})`
const statusTagType = (s) => {
  if (s === 4) return 'success'
  if (s === 5 || s === 6) return 'danger'
  if (s === 0) return 'warning'
  if (s === 7 || s === 8) return 'info'
  return 'primary'
}
const hideOrder = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要删除这条跑腿订单记录吗？删除后将不再显示，但不影响跑腿员侧。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { data } = await http.post(`/api/errands/${orderNo}/hide-from-my-list`)
    if (data && data.code === 1) {
      ElMessage.success('已从列表移除')
      await load()
    } else {
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('请求失败')
    }
  }
}

const canRefundErrand = (item) => [2, 3, 4].includes(item.orderStatus)
/** 无人接单/未履约前不展示投诉（待审核、待接单、已取消、审核拒绝无投诉入口） */
const canComplaintErrand = (item) => [2, 3, 4, 7, 8].includes(item.orderStatus)
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
        <option value="">全部</option>
        <option :value="-1">进行中</option>
        <option :value="4">历史-已完成</option>
        <option :value="5">历史-已取消</option>
      </select>
      <button class="btn gray" @click="load">刷新</button>
      <button v-if="selected.size > 0" class="btn danger" @click="batchHide">批量删除 ({{ selected.size }})</button>
      <input class="search" v-model="keyword" placeholder="搜索订单号/标题/地址/联系人" />
    </div>
    <div v-for="item in displayed" :key="item.orderId" class="card" :class="{ 'is-selected': selected.has(item.orderNo) }">
      <div class="row head">
        <div class="title">
          <el-checkbox
            v-if="[4, 5, 6, 8].includes(item.orderStatus)"
            :model-value="selected.has(item.orderNo)"
            @change="toggleSelect(item.orderNo)"
            style="margin-right: 8px"
          />
          {{ item.title }}
        </div>
        <el-tag :type="statusTagType(item.orderStatus)" effect="dark" size="small">{{ statusLabel(item.orderStatus) }}</el-tag>
      </div>
      <div class="desc">{{ item.description }}</div>
      <div class="row info">
        <span>赏金: ¥{{ item.reward }}</span>
        <span v-if="item.pickupCode">取件码: {{ item.pickupCode }}</span>
      </div>
      <div v-if="item.runnerId && (item.runnerNickname || item.runnerPhone)" class="runner meta">
        <span class="label">接单跑腿员：</span>
        <span>{{ item.runnerNickname || '跑腿员' }} {{ item.runnerPhone || '' }}</span>
      </div>
      <div v-if="item.acceptedAt" class="meta">接单时间：{{ item.acceptedAt }}</div>
      <div v-if="item.completedAt" class="meta">完成时间：{{ item.completedAt }}</div>
      <div class="ops">
        <button v-if="item.orderStatus === 2 || item.orderStatus === 3" class="btn" @click="complete(item.orderNo)">确认完成</button>
        <button v-if="item.orderStatus === 4" class="btn gray" @click="openReview(item.orderNo)">评价</button>
        <button v-if="canRefundErrand(item)" class="btn warn" @click="openRefund(item)">申请退款</button>
        <button v-if="canComplaintErrand(item)" class="btn danger" @click="openComplaint(item)">举报/投诉</button>
        <button v-if="[4, 5, 6, 8].includes(item.orderStatus)" class="btn gray" @click="hideOrder(item.orderNo)">删除记录</button>
      </div>
    </div>
    <el-dialog v-model="complaintDialog" title="发起举报/投诉" width="400">
      <div class="complaint-form">
        <p>订单：{{ displayed.find(it => it.orderId === complaintForm.orderId)?.title }}</p>
        <p>举报/投诉原因：</p>
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
.card{padding:12px;border:1px solid #eee;border-radius:8px;background:#fff;margin-bottom:10px;transition:all 0.2s ease}
.card.is-selected { border-color: #ef4444; background-color: #fef2f2; }
.row{display:flex;justify-content:space-between;align-items:center;margin:6px 0}
.row.head{align-items:flex-start;gap:10px}
.title{font-weight:600;flex:1;min-width:0}
.desc{color:#666;font-size:14px}
.meta{font-size:13px;color:#555;margin-top:4px}
.runner{font-weight:500}
.label{color:#888}
.toolbar{display:flex;gap:8px;align-items:center;margin-bottom:12px}
.search{flex:1;max-width:320px;border:1px solid #e5e7eb;border-radius:8px;padding:6px}
.ops{display:flex;gap:8px;margin-top:8px}
.btn{padding:8px 12px;border:none;border-radius:10px;background:#42b883;color:#fff;cursor:pointer}
.btn.gray{background:#e5e7eb;color:#111827}
.btn.warn{background:#f59e0b;color:#fff}
.btn.danger{background:#ef4444;color:#fff}
.refund-form p{margin:10px 0}
.empty{padding:24px;text-align:center;color:#999}
.error{padding:12px;color:#d33}
.review-body{display:flex;flex-direction:column;gap:12px}
</style>
