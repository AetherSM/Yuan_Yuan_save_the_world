<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const statusFilter = ref(null)
const statusMap = { 0: '待处理', 2: '已退款', 3: '已拒绝' }
const orderTypeMap = { 1: '商品订单', 2: '跑腿订单' }

const load = async () => {
  loading.value = true
  try {
    const params = statusFilter.value != null ? { status: statusFilter.value } : {}
    const { data } = await http.get('/refunds/my', { params })
    if (data && data.code === 1) {
      list.value = data.data || []
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const statusTagType = (s) => {
  if (s === 0) return 'warning'
  if (s === 2) return 'success'
  return 'info'
}

onMounted(load)
</script>

<template>
  <div class="my-refunds">
    <div class="toolbar">
      <span class="title">我的退款</span>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="load">
        <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="Number(key)" />
      </el-select>
      <el-button type="primary" @click="load" :loading="loading">刷新</el-button>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无退款记录</div>
    <div v-else class="list">
      <div v-for="r in list" :key="r.id" class="card">
        <div class="row">
          <span class="order-no">{{ orderTypeMap[r.orderType] }} {{ r.orderNo }}</span>
          <el-tag :type="statusTagType(r.status)">{{ statusMap[r.status] }}</el-tag>
        </div>
        <div class="row title-row" v-if="r.orderTitle">
          <span class="label">订单内容：</span>
          <span class="order-title">{{ r.orderTitle }}</span>
        </div>
        <div class="row summary-row" v-if="r.orderSummary">
          <span class="label">商品/描述：</span>
          <span class="order-summary">{{ r.orderSummary }}</span>
        </div>
        <div class="row">退款金额：¥{{ r.refundAmount }}</div>
        <div class="row" v-if="r.reason">原因：{{ r.reason }}</div>
        <div class="row muted">申请时间：{{ r.createTime }}</div>
        <div v-if="r.handleTime" class="row muted">处理时间：{{ r.handleTime }}</div>
        <div v-if="r.handleRemark" class="row muted">处理备注：{{ r.handleRemark }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-refunds { padding: 0; }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.title { font-weight: 600; margin-right: 12px; }
.loading, .empty { padding: 24px; color: #666; }
.list { display: flex; flex-direction: column; gap: 12px; }
.card { padding: 12px; border: 1px solid #eee; border-radius: 8px; background: #fff; }
.row { margin: 6px 0; }
.order-no { font-weight: 500; }
.title-row .order-title { font-weight: 600; color: #111; }
.summary-row .order-summary { color: #374151; font-size: 14px; }
.label { color: #6b7280; margin-right: 6px; min-width: 72px; }
.muted { font-size: 13px; color: #666; }
</style>
