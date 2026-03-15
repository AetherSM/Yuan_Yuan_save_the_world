<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const error = ref('')
const status = ref(undefined)

const statusMap = {
  1: '待支付',
  2: '待发货',
  3: '待收货',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款'
}

const load = async () => {
  loading.value = true
  try {
    const params = {
      status: status.value ?? null,
    }
    const { data } = await http.get('/orders/seller', { params })
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

const ship = async (orderNo) => {
  try {
    const { data } = await http.post(`/orders/${orderNo}/ship`)
    if (data && data.code === 1) {
      ElMessage.success('发货成功')
      await load()
    } else {
      ElMessage.error(data?.msg || '发货失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-card toolbar">
      <div class="toolbar-left">
        <div class="title">订单管理</div>
        <div class="muted">查看并处理店铺订单</div>
      </div>
      <div class="toolbar-right">
        <el-select v-model="status" placeholder="状态筛选" style="width:150px" clearable>
          <el-option v-for="(label, key) in statusMap" :key="key" :value="Number(key)" :label="label" />
        </el-select>
        <el-button type="info" plain @click="load" :loading="loading">刷新</el-button>
      </div>
    </div>

    <div v-if="error" class="page-card">
      <el-alert type="error" :closable="false" :title="error" />
    </div>

    <el-skeleton v-if="loading" animated :rows="6" class="page-card" />

    <div v-else-if="list.length === 0" class="page-card">
      <el-empty description="暂无订单" />
    </div>

    <div v-else class="list">
      <div v-for="item in list" :key="item.orderId" class="card">
        <div class="row header-row">
          <span>订单号: {{ item.orderNo }}</span>
          <el-tag :type="item.orderStatus===4 ? 'success' : item.orderStatus===5 ? 'info' : 'warning'">
            {{ statusMap[item.orderStatus] }}
          </el-tag>
        </div>
        <div class="row">
          <div class="amount">总额: ¥{{ item.totalAmount }}</div>
          <div class="time">{{ item.createTime }}</div>
        </div>
        <div class="address-box">
          <div>收货人: {{ item.contactName }} {{ item.contactPhone }}</div>
          <div>地址: {{ item.deliveryAddress }}</div>
        </div>
        <div class="ops">
          <el-button
            v-if="item.orderStatus === 2"
            type="primary"
            size="small"
            @click="ship(item.orderNo)"
          >
            确认发货
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.list{display:flex;flex-direction:column;gap:12px}
.card{padding:12px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.row{display:flex;justify-content:space-between;margin-bottom:8px}
.header-row{border-bottom:1px solid #f5f5f5;padding-bottom:8px;margin-bottom:8px;font-size:13px;color:#666}
.amount{font-weight:600;font-size:16px;color:#333}
.time{font-size:12px;color:#999}
.address-box{background:#f9f9f9;padding:8px;border-radius:4px;font-size:13px;color:#555;margin-bottom:8px}
.ops{text-align:right;border-top:1px solid #f5f5f5;padding-top:8px}
</style>
