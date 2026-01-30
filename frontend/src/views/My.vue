<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../services/http'
import ProductOrders from './ProductOrders.vue'
import MyOrders from './MyOrders.vue'
import ShoppingRecords from './ShoppingRecords.vue'
import Addresses from './Addresses.vue'
import Wallet from './Wallet.vue'
const route = useRoute()
const router = useRouter()
const activeTab = ref('orders')
const syncFromQuery = () => {
  const t = route.query.tab
  activeTab.value = (t === 'wallet') ? 'wallet' : (t === 'errands' ? 'errands' : (t === 'records' ? 'records' : (t === 'addresses' ? 'addresses' : 'orders')))
}
onMounted(syncFromQuery)
watch(() => route.query.tab, syncFromQuery)
const switchTab = (name) => {
  activeTab.value = name
  router.replace({ path: '/my', query: { tab: name } })
}
const logout = async () => {
  try { await http.post('/auth/logout') } catch (e) {}
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('nickname')
  localStorage.removeItem('userType')
  router.push('/login')
}
</script>

<template>
  <div class="my">
    <div class="tabs">
      <button class="tab" :class="{active: activeTab==='orders'}" @click="switchTab('orders')">订单</button>
      <button class="tab" :class="{active: activeTab==='errands'}" @click="switchTab('errands')">跑腿订单</button>
      <button class="tab" :class="{active: activeTab==='records'}" @click="switchTab('records')">购物记录</button>
      <button class="tab" :class="{active: activeTab==='addresses'}" @click="switchTab('addresses')">地址管理</button>
      <button class="tab" :class="{active: activeTab==='wallet'}" @click="switchTab('wallet')">钱包充值</button>
      <button class="tab danger" @click="logout">退出登录</button>
    </div>
    <div class="panel">
      <ProductOrders v-if="activeTab==='orders'" />
      <MyOrders v-else-if="activeTab==='errands'" />
      <ShoppingRecords v-else-if="activeTab==='records'" />
      <Addresses v-else-if="activeTab==='addresses'" />
      <Wallet v-else />
    </div>
  </div>
</template>

<style scoped>
.my{display:flex;flex-direction:column;gap:12px}
.tabs{display:flex;gap:8px;background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:8px}
.tab{flex:1;padding:10px;border:none;border-radius:8px;background:#f3f4f6;color:#111827;cursor:pointer}
.tab.active{background:#ff1f2d;color:#fff;font-weight:600}
.tab.danger{background:#fee2e2;color:#b91c1c}
.panel{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:12px}
</style>
