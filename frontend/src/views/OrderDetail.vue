<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderNo = route.params.orderNo
const order = ref(null)
const items = ref([])
const loading = ref(true)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get(`/orders/${orderNo}`)
    if (data && data.code === 1) {
      order.value = data.data?.order
      items.value = data.data?.items || []
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

const pay = async () => {
  try {
    const { data } = await http.post(`/orders/${orderNo}/pay`)
    if (data && data.code === 1) {
      ElMessage.success('支付成功')
      await load()
    } else {
      ElMessage.error(data?.msg || '支付失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '支付失败')
  }
}

const cancel = async () => {
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

const confirm = async () => {
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

const goBack = () => {
  router.push({ path: '/my', query: { tab: 'orders' } })
}
</script>

<template>
  <div class="order-detail">
    <div class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>订单详情</h1>
    </div>

    <el-skeleton v-if="loading" animated :rows="8" class="card" />

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="order" class="card">
      <div class="section">
        <h2>订单信息</h2>
        <div class="info-row">
          <span class="label">订单号：</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">订单状态：</span>
          <span class="value status">{{ statusText(order.orderStatus) }}</span>
        </div>
        <div class="info-row">
          <span class="label">总金额：</span>
          <span class="value price">¥{{ order.totalAmount }}</span>
        </div>
        <div class="info-row">
          <span class="label">创建时间：</span>
          <span class="value">{{ new Date(order.createTime).toLocaleString() }}</span>
        </div>
      </div>

      <div class="section">
        <h2>收货信息</h2>
        <div class="info-row">
          <span class="label">收货人：</span>
          <span class="value">{{ order.contactName }}（{{ order.contactPhone }}）</span>
        </div>
        <div class="info-row">
          <span class="label">收货地址：</span>
          <span class="value">{{ order.deliveryAddress }}</span>
        </div>
      </div>

      <div class="section">
        <h2>商品信息</h2>
        <div v-for="item in items" :key="item.orderItemId" class="item">
          <div class="item-info">
            <div class="item-name">{{ item.productName }}</div>
            <div class="item-meta">
              <span class="item-price">¥{{ item.price }}</span>
              <span class="item-quantity">x{{ item.quantity }}</span>
            </div>
          </div>
          <div class="item-subtotal">¥{{ item.subtotal }}</div>
        </div>
      </div>

      <div class="section">
        <h2>操作</h2>
        <div class="actions">
          <button v-if="order.orderStatus === 1" class="btn primary" @click="pay">立即支付</button>
          <button v-if="order.orderStatus === 1" class="btn gray" @click="cancel">取消订单</button>
          <button v-if="order.orderStatus === 3" class="btn primary" @click="confirm">确认收货</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #374151;
}

h1 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}

.section {
  margin-bottom: 20px;
}

.section:last-child {
  margin-bottom: 0;
}

h2 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #111827;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  align-items: flex-start;
}

.label {
  min-width: 80px;
  color: #6b7280;
  font-size: 14px;
}

.value {
  flex: 1;
  font-size: 14px;
  color: #374151;
}

.status {
  font-weight: 600;
  color: #ff1f2d;
}

.price {
  font-weight: 600;
  color: #ff1f2d;
  font-size: 16px;
}

.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #f3f4f6;
  border-radius: 8px;
  margin-bottom: 8px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.item-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #6b7280;
}

.item-subtotal {
  font-weight: 600;
  color: #374151;
}

.actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn.primary {
  background: #42b883;
  color: #fff;
}

.btn.gray {
  background: #e5e7eb;
  color: #111827;
}

.error {
  color: #ef4444;
  text-align: center;
  padding: 24px;
}
</style>