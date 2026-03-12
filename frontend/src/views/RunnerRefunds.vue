<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const statusFilter = ref(0)

const statusMap = { 0: '待处理', 2: '已退款', 3: '已拒绝' }

const load = async () => {
  loading.value = true
  try {
    const params = statusFilter.value != null ? { status: statusFilter.value } : {}
    const { data } = await http.get('/refunds/runner', { params })
    if (data && data.code === 1) {
      list.value = data.data || []
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const approve = async (row) => {
  try {
    await ElMessageBox.confirm('确定同意该退款申请？退款将打入用户余额。', '确认')
    const { data } = await http.post(`/refunds/${row.id}/approve`, { handleRemark: '' })
    if (data && data.code === 1) {
      ElMessage.success('已同意退款')
      load()
    } else {
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const reject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝退款', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '选填'
    })
    const { data } = await http.post(`/refunds/${row.id}/reject`, { handleRemark: value || '' })
    if (data && data.code === 1) {
      ElMessage.success('已拒绝')
      load()
    } else {
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-card toolbar">
      <div class="toolbar-left">
        <div class="title">退款处理</div>
        <div class="muted">处理我接单的跑腿订单退款申请</div>
      </div>
      <div class="toolbar-right">
        <el-select v-model="statusFilter" placeholder="状态" style="width: 120px" @change="load">
          <el-option label="待处理" :value="0" />
          <el-option label="已退款" :value="2" />
          <el-option label="已拒绝" :value="3" />
        </el-select>
        <el-button type="primary" @click="load" :loading="loading">刷新</el-button>
      </div>
    </div>
    <div v-if="loading" class="page-card"><el-skeleton :rows="4" /></div>
    <div v-else-if="list.length === 0" class="page-card"><el-empty description="暂无退款申请" /></div>
    <div v-else class="list">
      <div v-for="r in list" :key="r.id" class="card">
        <div class="row">
          <span>订单号：{{ r.orderNo }}</span>
          <el-tag :type="r.status === 0 ? 'warning' : r.status === 2 ? 'success' : 'info'">
            {{ statusMap[r.status] }}
          </el-tag>
        </div>
        <div class="row order-content" v-if="r.orderTitle || r.orderSummary">
          <span class="label">订单内容：</span>
          <span>{{ r.orderTitle }}</span>
          <span v-if="r.orderSummary" class="summary"> — {{ r.orderSummary }}</span>
        </div>
        <div class="row">退款金额：¥{{ r.refundAmount }}</div>
        <div class="row" v-if="r.reason">原因：{{ r.reason }}</div>
        <div class="row muted">申请时间：{{ r.createTime }}</div>
        <div v-if="r.status === 0" class="ops">
          <el-button type="success" size="small" @click="approve(r)">同意退款</el-button>
          <el-button type="danger" size="small" plain @click="reject(r)">拒绝</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 0; }
.toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.toolbar-left .title { font-weight: 600; }
.muted { font-size: 13px; color: #666; }
.list { display: flex; flex-direction: column; gap: 12px; }
.card { padding: 16px; border: 1px solid #eee; border-radius: 8px; background: #fff; }
.row { margin: 6px 0; }
.order-content .label { color: #6b7280; margin-right: 6px; }
.order-content .summary { color: #374151; font-size: 14px; }
.ops { margin-top: 12px; display: flex; gap: 8px; }
</style>
