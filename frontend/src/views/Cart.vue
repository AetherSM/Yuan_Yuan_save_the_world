<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessage } from 'element-plus'
const router = useRouter()
const items = ref([]) // { cartItem: {...}, product: {...} }
const addresses = ref([])
const addressId = ref(null)
const checkoutVisible = ref(false)
const loading = ref(false)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get('/api/cart')
    if (data && data.code === 1) items.value = data.data || []
    else error.value = data?.msg || '加载购物车失败'
  } catch (e) {
    error.value = '请求失败'
  } finally {
    loading.value = false
  }
}
onMounted(load)
const changeQty = async (pid, q, stock) => {
  try {
    let next = Math.max(1, q)
    if (typeof stock === 'number' && stock >= 0 && next > stock) {
      next = stock
      ElMessage.error('超过库存，已调整为最大可购数量')
    }
    const { data } = await http.put(`/api/cart/${pid}`, null, { params: { quantity: next } })
    if (data && data.code === 1) await load()
    else ElMessage.error(data?.msg || '更新数量失败')
  } catch (e) { ElMessage.error('请求失败') }
}
const removeItem = async (pid) => {
  try {
    const { data } = await http.delete(`/api/cart/${pid}`)
    if (data && data.code === 1) await load()
    else ElMessage.error(data?.msg || '移除失败')
  } catch (e) { ElMessage.error('请求失败') }
}
const openCheckout = async () => {
  if (!items.value.length) { ElMessage.error('购物车为空'); return }
  try {
    const { data } = await http.get('/addresses')
    if (data && data.code === 1) {
      addresses.value = data.data || []
      addressId.value = addresses.value[0]?.addressId || null
      checkoutVisible.value = true
    }
  } catch (e) {}
}
const checkout = async () => {
  if (!addressId.value) { ElMessage.error('请选择地址'); return }
  try {
    const payloadItems = items.value.map(x => ({ productId: x.product.productId, quantity: x.cartItem.quantity }))
    const dto = { addressId: addressId.value, items: payloadItems }
    const { data } = await http.post('/orders', dto)
    if (data && data.code === 1 && data.data?.orderNo) {
      const orderNo = data.data.orderNo
      await http.post(`/orders/${orderNo}/pay`)
      await http.post('/api/cart/clear')
      ElMessage.success('支付成功')
      checkoutVisible.value = false
      await load()
      router.push({ path: '/my', query: { tab: 'orders' } })
    } else {
      ElMessage.error(data?.msg || '下单失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}
const total = () => items.value.reduce((sum, x) => {
  const price = x.product?.price || 0
  const qty = x.cartItem?.quantity || 0
  return sum + Number(price) * qty
}, 0)
</script>

<template>
  <div class="page">
    <div class="page-card toolbar">
      <div class="toolbar-left">
        <div class="title">购物车</div>
        <div class="muted">查看已加入的商品并一键结算</div>
      </div>
      <div class="toolbar-right">
        <el-button type="info" plain @click="load" :loading="loading">刷新</el-button>
      </div>
    </div>

    <div v-if="error" class="page-card">
      <el-alert type="error" :closable="false" :title="error" />
    </div>

    <el-skeleton v-if="loading" animated :rows="4" class="page-card" />

    <div v-else-if="items.length === 0" class="page-card">
      <el-empty description="购物车还是空的，去逛逛吧" />
    </div>

    <div v-else class="page-card">
      <div class="list">
        <div v-for="x in items" :key="x.product.productId" class="row">
          <div class="info">
            <div class="name">{{ x.product?.productName || '商品' }}</div>
            <div class="meta">
              <span class="price">¥{{ x.product?.price || '-' }}</span>
              <span class="stock">库存：{{ x.product?.stock ?? '-' }}</span>
            </div>
          </div>
          <div class="qty-box">
            <el-input-number
              :model-value="x.cartItem.quantity"
              :min="1"
              :max="x.product?.stock ?? undefined"
              size="small"
              @change="val => changeQty(x.product.productId, Number(val), x.product?.stock)"
            />
          </div>
          <el-button size="small" type="danger" text @click="removeItem(x.product.productId)">移除</el-button>
        </div>
      </div>
      <div class="summary">
        <div>合计：<span class="sum">¥{{ total().toFixed(2) }}</span></div>
        <el-button type="primary" @click="openCheckout">去结算</el-button>
      </div>
    </div>

    <el-dialog v-model="checkoutVisible" title="选择收货地址" width="520px">
      <div class="addr-list">
        <label v-for="a in addresses" :key="a.addressId" class="addr-item">
          <input type="radio" :value="a.addressId" v-model="addressId" />
          <span>{{ a.contactName }}（{{ a.contactPhone }}）{{ a.address }} {{ a.building }} {{ a.room }}</span>
        </label>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="checkout">支付</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.list{display:flex;flex-direction:column;gap:8px}
.row{display:flex;align-items:center;gap:12px;margin-bottom:8px}
.info{flex:1}
.name{font-weight:600;color:#111827}
.meta{color:#6b7280;font-size:12px;margin-top:4px;display:flex;gap:10px;align-items:center}
.price{color:#ef4444;font-weight:600}
.stock{color:#9ca3af}
.summary{display:flex;justify-content:flex-end;gap:12px;align-items:center;margin-top:16px;border-top:1px solid #f3f4f6;padding-top:10px}
.sum{color:#ef4444;font-weight:800}
.addr-list{display:flex;flex-direction:column;gap:8px}
.addr-item{display:flex;gap:8px;align-items:center}
</style>
