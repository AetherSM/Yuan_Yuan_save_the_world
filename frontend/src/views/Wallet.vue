<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElTag } from 'element-plus'
import http from '../services/http'

const list = ref([])
const loading = ref(false)
const error = ref('')
const amount = ref(100)
const recharging = ref(false)

// 交易类型映射
const typeMap = {
  1: '充值',
  2: '支出',
  3: '提现',
  4: '订单收入',
  5: '订单退款'
}

const typeTagType = (t) => {
  if (t === 1) return 'success'
  if (t === 4) return 'success'
  if (t === 5) return 'warning'
  if (t === 2 || t === 3) return 'danger'
  return 'info'
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get('/wallet/transactions')
    if (data && data.code === 1) {
      list.value = data.data || []
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

const recharge = async () => {
  recharging.value = true
  try {
    const { data } = await http.post('/wallet/recharge', { amount: amount.value })
    if (data && data.code === 1) {
      await load()
    } else {
      error.value = data?.msg || '充值失败'
    }
  } catch (e) {
    error.value = '请求失败'
  } finally {
    recharging.value = false
  }
}
</script>

<template>
  <div>
    <div class="form">
      <h2>钱包充值</h2>
      <div class="form-row">
        <input v-model.number="amount" type="number" min="1" step="1" />
        <button class="btn" :disabled="recharging" @click="recharge">
          {{ recharging ? '充值中' : '充值' }}
        </button>
      </div>
    </div>

    <h2>钱包流水</h2>
    <div v-for="t in list" :key="t.transactionId" class="row">
      <div class="id">#{{ t.transactionId }}</div>
      <div class="type">
        <el-tag :type="typeTagType(t.transactionType)" size="small" effect="light">
          {{ typeMap[t.transactionType] || '其他' }}
        </el-tag>
      </div>
      <div class="amount" :class="{ negative: t.transactionType === 2 || t.transactionType === 3 }">
        {{ (t.transactionType === 2 || t.transactionType === 3) ? '-' : '+' }}¥{{ t.amount }}
      </div>
      <div class="balance">余额 {{ t.balanceAfter }}</div>
      <div class="desc">
        <span class="order" v-if="t.relatedOrderNo">订单号：{{ t.relatedOrderNo }} ｜ </span>
        <span>{{ t.description }}</span>
      </div>
    </div>
    <div v-if="!loading && list.length===0" class="empty">暂无数据</div>
    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<style scoped>
.form{
  width:100%;
  padding:16px;
  border:1px solid #e5e7eb;
  border-radius:12px;
  background:#fff;
  margin-bottom:12px
}
.form-row{
  display:flex;
  gap:8px;
  align-items:center
}
.form-row input{
  flex:1;
  padding:8px 10px;
  border-radius:10px;
  border:1px solid #e5e7eb;
}
.btn{
  padding:10px 16px;
  border:none;
  border-radius:10px;
  background:#42b883;
  color:#fff;
  cursor:pointer
}
.row{
  display:grid;
  grid-template-columns:80px 90px 130px 140px 1fr;
  gap:8px;
  padding:8px;
  border-bottom:1px solid #eee;
  background:#fff;
  align-items:center;
  font-size:14px;
}
.amount{
  font-weight:600;
  color:#16a34a;
}
.amount.negative{
  color:#ef4444;
}
.balance{
  color:#6b7280;
}
.desc{
  color:#374151;
}
.order{
  color:#6b7280;
}
.empty{
  padding:24px;
  text-align:center;
  color:#999
}
.error{
  padding:12px;
  color:#d33
}
</style>
