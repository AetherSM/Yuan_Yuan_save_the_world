<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight } from '@element-plus/icons-vue'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const error = ref('')
const status = ref('')
const keyword = ref('')
const refundDialog = ref(false)
const refundForm = ref({ orderNo: '', orderType: 1, refundAmount: 0, reason: '' })
const refundSubmitting = ref(false)
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
    await ElMessageBox.confirm(`确定要批量删除这 ${selected.value.size} 条订单记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { data } = await http.post('/orders/batch-hide', Array.from(selected.value))
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

/** 订单内可搜索文本（含商品摘要，支持按商品名模糊） */
const orderSearchText = (o) =>
  [
    o.orderNo,
    o.contactName,
    o.contactPhone,
    o.deliveryAddress,
    o.itemSummary
  ]
    .map((x) => String(x || '').toLowerCase())
    .join(' ')

const displayedList = computed(() => {
  const kw = (keyword.value || '').trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter((o) => orderSearchText(o).includes(kw))
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
    if (status.value !== '' && status.value != null) params.status = status.value
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

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待支付', value: 1 },
  { label: '待发货', value: 2 },
  { label: '待收货', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
]

const statusText = (s) =>
  (
    {
      1: '待支付',
      2: '待发货',
      3: '待收货',
      4: '已完成',
      5: '已取消',
      6: '退款中',
      7: '已退款'
    }[s] || '未知'
  )

const statusTagType = (s) => {
  if (s === 4) return 'success'
  if (s === 5) return 'info'
  if (s === 1) return 'warning'
  if (s === 6 || s === 7) return 'danger'
  return 'primary'
}

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
    if (data && data.code === 1) {
      ElMessage.success('已取消')
      await load()
    } else {
      ElMessage.error(data?.msg || '取消失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}
const confirm = async (orderNo) => {
  try {
    const { data } = await http.post(`/orders/${orderNo}/confirm`)
    if (data && data.code === 1) {
      ElMessage.success('已确认收货')
      await load()
    } else {
      ElMessage.error(data?.msg || '确认失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

const viewDetail = (orderNo) => {
  router.push(`/order/${orderNo}`)
}

const hideOrder = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要删除这条订单记录吗？删除后将不再显示，但不影响商家处理。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { data } = await http.post(`/orders/${orderNo}/hide-from-my-list`)
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

const reviewVisible = ref(false)
const currentOrderNo = ref('')
const rating = ref(5)
const content = ref('')

const openReview = (orderNo) => {
  currentOrderNo.value = orderNo
  rating.value = 5
  content.value = ''
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  try {
    const body = {
      rating: rating.value,
      content: content.value,
      orderType: 1
    }
    const { data } = await http.post(`/orders/${currentOrderNo.value}/reviews`, body)
    if (data && data.code === 1) {
      ElMessage.success('评价成功')
      reviewVisible.value = false
      await load()
    } else {
      ElMessage.error(data?.msg || '评价失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}
</script>

<template>
  <div class="product-orders">
    <header class="po-header">
      <h2 class="po-title">我的订单</h2>
      <p class="po-sub">支持按订单号、商品名称、地址、联系人搜索</p>
    </header>

    <div class="toolbar-card">
      <div class="toolbar-row">
        <span class="filter-label">筛选</span>
        <el-select
          v-model="status"
          placeholder="订单状态"
          class="status-select"
          clearable
          @change="load"
        >
          <el-option v-for="opt in statusOptions" :key="String(opt.value)" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-button :icon="RefreshRight" @click="load" :loading="loading">刷新</el-button>
        <el-button v-if="selected.size > 0" type="danger" plain @click="batchHide">批量删除 ({{ selected.size }})</el-button>
      </div>
      <el-input
        v-model="keyword"
        class="search-input"
        clearable
        :prefix-icon="Search"
        placeholder="搜索订单号、商品名、地址、收货人、手机号"
      />
    </div>

    <el-skeleton v-if="loading" animated :rows="4" class="skeleton-block" />

    <template v-else>
      <div v-if="displayedList.length === 0 && list.length > 0" class="empty-filter">
        <el-empty description="没有符合搜索条件的订单" :image-size="72" />
      </div>
      <div v-for="o in displayedList" :key="o.orderId" class="order-card" :class="{ 'is-selected': selected.has(o.orderNo) }">
        <div class="card-top">
          <div class="order-no-line">
            <el-checkbox
              v-if="[4, 5, 6, 7].includes(o.orderStatus)"
              :model-value="selected.has(o.orderNo)"
              @change="toggleSelect(o.orderNo)"
              class="card-checkbox"
            />
            <span class="label-muted">订单号</span>
            <span class="mono">{{ o.orderNo }}</span>
            <el-button link type="primary" class="detail-link" @click="viewDetail(o.orderNo)">详情</el-button>
          </div>
          <el-tag :type="statusTagType(o.orderStatus)" effect="light" round>
            {{ statusText(o.orderStatus) }}
          </el-tag>
        </div>

        <div class="card-body">
          <div class="stat">
            <span class="stat-label">总金额</span>
            <span class="stat-value price">¥{{ o.totalAmount }}</span>
          </div>
          <div class="stat">
            <span class="stat-label">收货人</span>
            <span class="stat-value">{{ o.contactName }} · {{ o.contactPhone }}</span>
          </div>
          <div v-if="o.itemSummary" class="goods-block">
            <span class="stat-label">商品</span>
            <p class="goods-text">{{ o.itemSummary }}</p>
          </div>
          <div class="addr-block">
            <span class="stat-label">地址</span>
            <p class="addr-text">{{ o.deliveryAddress }}</p>
          </div>
        </div>

        <div class="card-actions" @click.stop>
          <el-button v-if="o.orderStatus === 1" size="small" @click="cancel(o.orderNo)">取消订单</el-button>
          <el-button v-if="o.orderStatus === 3" type="primary" size="small" @click="confirm(o.orderNo)">确认收货</el-button>
          <el-button v-if="o.orderStatus === 4" size="small" @click="openReview(o.orderNo)">评价商品</el-button>
          <el-button v-if="canRefund(o)" type="warning" size="small" plain @click="openRefund(o)">申请退款</el-button>
          <el-button type="danger" size="small" plain @click="openComplaint(o)">投诉</el-button>
          <el-button v-if="[4, 5, 6, 7].includes(o.orderStatus)" size="small" @click="hideOrder(o.orderNo)">删除记录</el-button>
        </div>
      </div>

      <div v-if="!loading && list.length === 0" class="empty">
        <el-empty description="暂无订单" />
      </div>
    </template>

    <el-alert v-if="error" type="error" :closable="false" :title="error" class="err-alert" />

    <el-dialog v-model="complaintDialog" title="发起投诉" width="420">
      <div class="complaint-form">
        <p class="dialog-line">订单号：{{ displayedList.find((it) => it.orderId === complaintForm.orderId)?.orderNo }}</p>
        <p>投诉原因：</p>
        <el-input v-model="complaintForm.reason" type="textarea" rows="4" placeholder="请详细描述您遇到的问题" />
      </div>
      <template #footer>
        <el-button @click="complaintDialog = false">取消</el-button>
        <el-button type="danger" @click="submitComplaint" :loading="complaintSubmitting">提交投诉</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refundDialog" title="申请退款" width="420">
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

    <el-dialog v-model="reviewVisible" title="评价商品" width="500px">
      <div class="review-form">
        <div class="form-item">
          <label>评分</label>
          <el-rate v-model="rating" :max="5" />
        </div>
        <div class="form-item">
          <label>评价内容</label>
          <el-input v-model="content" type="textarea" :rows="4" placeholder="请分享您的使用体验" />
        </div>
      </div>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.product-orders {
  width: 100%;
  box-sizing: border-box;
}

.po-header {
  margin-bottom: 16px;
}

.po-title {
  margin: 0 0 6px;
  font-size: 1.35rem;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.02em;
}

.po-sub {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.toolbar-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
  background: linear-gradient(180deg, #f9fafb 0%, #fff 100%);
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.status-select {
  width: 140px;
}

.search-input {
  width: 100%;
  max-width: 100%;
}

.skeleton-block {
  padding: 8px 0;
}

.order-card {
  margin-bottom: 14px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
  transition: all 0.2s ease;
}

.order-card.is-selected {
  border-color: #f87171;
  background-color: #fef2f2;
}

.card-checkbox {
  margin-right: 12px;
}

.order-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.08);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.order-no-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.label-muted {
  font-size: 12px;
  color: #9ca3af;
}

.mono {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  word-break: break-all;
}

.detail-link {
  flex-shrink: 0;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px 12px;
  font-size: 14px;
}

.stat-label {
  flex-shrink: 0;
  width: 52px;
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.stat-value {
  color: #374151;
}

.price {
  font-size: 18px;
  font-weight: 800;
  color: #dc2626;
}

.goods-block,
.addr-block {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.goods-text,
.addr-text {
  margin: 0;
  flex: 1;
  font-size: 14px;
  line-height: 1.5;
  color: #111827;
}

.addr-text {
  color: #4b5563;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.empty,
.empty-filter {
  padding: 32px 16px;
}

.err-alert {
  margin-top: 12px;
}

.complaint-form p,
.refund-form p {
  margin: 10px 0;
}

.dialog-line {
  color: #374151;
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}
</style>
