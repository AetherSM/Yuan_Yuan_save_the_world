<template>
  <div>
    <h1>跑腿任务管理</h1>
    <div class="filters">
      <el-input v-model="filters.userId" placeholder="用户ID" />
      <el-input v-model="filters.runnerId" placeholder="跑腿员ID" />
      <el-select v-model="filters.status" placeholder="状态">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="0" />
        <el-option label="待接单" value="1" />
        <el-option label="已接单" value="2" />
        <el-option label="配送中" value="3" />
        <el-option label="已完成" value="4" />
        <el-option label="已取消" value="5" />
        <el-option label="审核拒绝" value="6" />
      </el-select>
      <el-button type="primary" @click="loadErrands">搜索</el-button>
      <el-button type="info" @click="loadPendingErrands">查看待审核</el-button>
    </div>

    <el-table :data="errands" style="width: 100%">
      <el-table-column prop="orderId" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单编号" />
      <el-table-column prop="title" label="任务标题" />
      <el-table-column prop="userId" label="用户ID" />
      <el-table-column prop="runnerId" label="跑腿员ID" />
      <el-table-column prop="reward" label="赏金" />
      <el-table-column prop="orderStatus" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.orderStatus)">
            {{ statusMap[row.orderStatus] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="强制改状态" width="200">
        <template #default="{ row }">
          <el-select
            v-model="row._nextStatus"
            placeholder="选择状态"
            size="small"
            style="width: 120px"
          >
            <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
          <el-button size="small" type="primary" @click="forceUpdateStatus(row)" style="margin-left: 8px">更新</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approveErrand(row)" v-if="row.orderStatus === 0">审核通过</el-button>
          <el-button size="small" type="danger" @click="rejectErrand(row)" v-if="row.orderStatus === 0">审核拒绝</el-button>
          <el-button size="small" type="danger" plain @click="forceCancel(row)" v-if="row.orderStatus !== 5 && row.orderStatus !== 4">强制取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const errands = ref([])
const filters = ref({
  userId: '',
  runnerId: '',
  status: '',
})

const statusMap = {
  0: '待审核',
  1: '待接单',
  2: '已接单',
  3: '配送中',
  4: '已完成',
  5: '已取消',
  6: '审核拒绝',
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'info'
    case 2: return 'info'
    case 3: return 'info'
    case 4: return 'success'
    case 5: return 'danger'
    case 6: return 'danger'
    default: return ''
  }
}

const loadErrands = async () => {
  try {
    const params = {
      userId: filters.value.userId || null,
      runnerId: filters.value.runnerId || null,
      status: filters.value.status || null,
    }
    const { data } = await http.get('/admin/errands', { params })
    if (data.code === 1) {
      errands.value = (data.data || []).map(it => ({ ...it, _nextStatus: it.orderStatus }))
    } else {
      ElMessage.error(data.msg || '加载任务失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.message || error?.message || '请求失败')
  }
}

const loadPendingErrands = async () => {
  try {
    const { data } = await http.get('/admin/errands/pending')
    if (data.code === 1) {
      errands.value = (data.data || []).map(it => ({ ...it, _nextStatus: it.orderStatus }))
    } else {
      ElMessage.error(data.msg || '加载待审核任务失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.message || error?.message || '请求失败')
  }
}

const forceUpdateStatus = async (row) => {
  const nextStatus = Number(row._nextStatus)
  if (Number.isNaN(nextStatus)) {
    ElMessage.warning('请选择要更新的状态')
    return
  }
  try {
    await http.patch(`/admin/errands/${row.orderId}/status`, { status: nextStatus })
    ElMessage.success('状态更新成功')
    await loadErrands()
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

const forceCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确定强制取消订单 ${row.orderNo} 吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await http.patch(`/admin/errands/${row.orderId}/status`, { status: 5 })
    ElMessage.success('已强制取消')
    await loadErrands()
  } catch (e) {
    ElMessage.error('强制取消失败')
  }
}

const approveErrand = async (errand) => {
  try {
    await http.patch(`/admin/errands/${errand.orderId}/approve`)
    ElMessage.success('审核通过成功')
    await loadErrands()
  } catch (error) {
    ElMessage.error('审核操作失败')
  }
}

const rejectErrand = async (errand) => {
  try {
    await http.patch(`/admin/errands/${errand.orderId}/reject`)
    ElMessage.success('审核拒绝成功')
    await loadErrands()
  } catch (error) {
    ElMessage.error('审核操作失败')
  }
}

onMounted(loadErrands)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
</style>