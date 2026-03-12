<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const orderTypeFilter = ref(null)
const statusFilter = ref(null)

const orderTypeMap = { 1: '商品订单', 2: '跑腿订单' }
const statusMap = { 0: '待处理', 2: '已退款', 3: '已拒绝' }

const load = async () => {
  loading.value = true
  try {
    const params = {}
    if (orderTypeFilter.value != null) params.orderType = orderTypeFilter.value
    if (statusFilter.value != null) params.status = statusFilter.value
    const { data } = await http.get('/admin/refunds', { params })
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
    const { data } = await http.post(`/admin/refunds/${row.id}/approve`, { handleRemark: '' })
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
    const { data } = await http.post(`/admin/refunds/${row.id}/reject`, { handleRemark: value || '' })
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
  <div class="admin-refunds">
    <div class="filters">
      <el-select v-model="orderTypeFilter" placeholder="订单类型" clearable style="width: 140px" @change="load">
        <el-option label="商品订单" :value="1" />
        <el-option label="跑腿订单" :value="2" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="load">
        <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="Number(key)" />
      </el-select>
      <el-button type="primary" @click="load" :loading="loading">查询</el-button>
    </div>
    <el-table :data="list" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderType" label="订单类型" width="100">
        <template #default="{ row }">{{ orderTypeMap[row.orderType] }}</template>
      </el-table-column>
      <el-table-column prop="orderNo" label="订单号" width="170" />
      <el-table-column label="订单内容" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.orderTitle">{{ row.orderTitle }}</span>
          <span v-if="row.orderSummary" class="summary"> — {{ row.orderSummary }}</span>
          <span v-if="!row.orderTitle && !row.orderSummary" class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="applicantId" label="申请人ID" width="90" />
      <el-table-column prop="refundAmount" label="退款金额" width="100" />
      <el-table-column prop="reason" label="原因" min-width="100" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : row.status === 2 ? 'success' : 'info'">
            {{ statusMap[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button type="success" size="small" @click="approve(row)">同意</el-button>
            <el-button type="danger" size="small" plain @click="reject(row)">拒绝</el-button>
          </template>
          <span v-else class="muted">已处理</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.admin-refunds { padding: 0; }
.filters { display: flex; gap: 10px; margin-bottom: 16px; }
.summary { font-size: 13px; color: #666; }
.muted { font-size: 13px; color: #999; }
</style>
