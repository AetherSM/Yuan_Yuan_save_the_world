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
const load = async () => {
  try {
    const { data } = await http.get('/api/cart')
    if (data && data.code === 1) items.value = data.data || []
    else ElMessage.error(data?.msg || '加载购物车失败')
  } catch (e) {
    ElMessage.error('请求失败')
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
  <div>
    <h2>购物车</h2>
    <div class="list">
      <div v-for="x in items" :key="x.product.productId" class="row">
        <div class="info">
          <div class="name">{{ x.product?.productName || '商品' }}</div>
          <div class="price">¥{{ x.product?.price || '-' }}</div>
          <div class="stock">库存：{{ x.product?.stock ?? '-' }}</div>
        </div>
        <div class="qty-box">
          <input type="number" min="1" :max="x.product?.stock ?? undefined" :value="x.cartItem.quantity" @change="changeQty(x.product.productId, Number($event.target.value), x.product?.stock)" />
        </div>
        <button class="btn gray" @click="removeItem(x.product.productId)">移除</button>
      </div>
    </div>
    <div class="summary">
      <div>合计：¥{{ total().toFixed(2) }}</div>
      <button class="btn" @click="openCheckout">去结算</button>
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
          <button class="btn" @click="checkout">支付</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.list{display:flex;flex-direction:column;gap:8px}
.row{display:flex;align-items:center;gap:12px;padding:12px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.info{flex:1}
.name{font-weight:600}
.price{color:#ef4444}
.stock{color:#6b7280;font-size:12px;margin-top:4px}
.qty-box input{width:80px;padding:8px;border:1px solid #e5e7eb;border-radius:10px}
.btn{padding:10px 16px;border:none;border-radius:10px;background:#42b883;color:#fff;cursor:pointer}
.btn.gray{background:#e5e7eb;color:#111827}
.summary{display:flex;justify-content:flex-end;gap:12px;align-items:center;margin-top:12px}
.addr-list{display:flex;flex-direction:column;gap:8px}
.addr-item{display:flex;gap:8px;align-items:center}
</style>
