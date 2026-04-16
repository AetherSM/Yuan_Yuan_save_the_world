<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const userId = localStorage.getItem('userId')
    const { data } = await http.get('/api/complaints/my', { params: { userId } })
    if (data && data.code === 1) {
      list.value = data.data || []
    } else {
      error.value = data?.msg || '加载失败'
    }
  } catch (e) {
    error.value = '请求失败'
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待处理'
    case 1: return '处理中'
    case 2: return '已完成'
    default: return '未知'
  }
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'info'
    case 2: return 'success'
    default: return ''
  }
}

onMounted(load)
</script>

<template>
  <div class="complaints">
    <div class="header">
      <h3>我的投诉记录</h3>
      <el-button size="small" type="primary" plain @click="load" :loading="loading">刷新</el-button>
    </div>

    <div v-for="c in list" :key="c.complaintId" class="complaint-card">
      <div class="row header-row">
        <span class="order-info">订单 ID: {{ c.orderId }} ({{ c.orderType === 1 ? '商品' : '跑腿' }})</span>
        <el-tag :type="getStatusType(c.status)" size="small">{{ getStatusText(c.status) }}</el-tag>
      </div>
      <div class="reason">
        <strong>投诉原因：</strong>{{ c.reason }}
      </div>
      <div v-if="c.result" class="result">
        <strong>处理结果：</strong>{{ c.result }}
      </div>
      <div class="time">{{ new Date(c.createTime).toLocaleString() }}</div>
    </div>

    <div v-if="!loading && list.length === 0" class="empty">
      <el-empty description="暂无投诉记录" />
    </div>
  </div>
</template>

<style scoped>
.complaints {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.complaint-card {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-info {
  font-weight: 600;
  color: #374151;
}
.reason, .result {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
}
.result {
  padding: 8px;
  background: #f9fafb;
  border-radius: 8px;
  border-left: 4px solid #42b883;
}
.time {
  font-size: 12px;
  color: #9ca3af;
  align-self: flex-end;
}
.empty {
  padding: 40px 0;
}
</style>
