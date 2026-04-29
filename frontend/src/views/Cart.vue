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
const creatingAddress = ref(false)
const newAddress = ref({ contactName: '', contactPhone: '', address: '', building: '', room: '' })
const availableCoupons = ref([])
const selectedCouponId = ref(null)
const pendingCouponId = ref(null)
const amountPreview = ref({ originalAmount: 0, discountAmount: 0, payAmount: 0 })
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
      creatingAddress.value = addresses.value.length === 0
      await loadAvailableCoupons()
      pendingCouponId.value = selectedCouponId.value
      await refreshAmountPreview()
    }
  } catch (e) {}
}
const buildOrderItems = () =>
  items.value
    .map(x => ({ productId: x.product?.productId, quantity: x.cartItem?.quantity || 1 }))
    .filter(x => x.productId)

const refreshAmountPreview = async () => {
  const payloadItems = buildOrderItems()
  if (!payloadItems.length) {
    amountPreview.value = { originalAmount: 0, discountAmount: 0, payAmount: 0 }
    return
  }
  try {
    const dto = { items: payloadItems, couponId: selectedCouponId.value || null }
    const { data } = await http.post('/orders/preview', dto)
    if (data && data.code === 1 && data.data) {
      amountPreview.value = data.data
    }
  } catch (e) {}
}

const loadAvailableCoupons = async () => {
  const payloadItems = buildOrderItems()
  availableCoupons.value = []
  selectedCouponId.value = null
  pendingCouponId.value = null
  if (!payloadItems.length) return
  try {
    const { data } = await http.post('/orders/coupons/available', { items: payloadItems })
    if (data && data.code === 1) {
      availableCoupons.value = data.data || []
    }
  } catch (e) {}
}

const applySelectedCoupon = async () => {
  selectedCouponId.value = pendingCouponId.value || null
  await refreshAmountPreview()
}

const couponLabel = (c) => {
  const typeText = c.type === 1 ? `满减${c.value}` : `${c.value}折`
  const discount = c.discountAmount ?? 0
  return `${c.name}（${typeText}，可减¥${discount}）`
}
const saveNewAddress = async () => {
  if (!newAddress.value.contactName || !newAddress.value.contactPhone || !newAddress.value.address) {
    ElMessage.error('请填写收货人、电话和详细地址')
    return
  }
  try {
    const res = await http.post('/addresses', newAddress.value)
    const data = res.data
    if (data && data.code === 1) {
      ElMessage.success('地址已保存')
      const addrRes = await http.get('/addresses')
      if (addrRes.data && addrRes.data.code === 1) {
        addresses.value = addrRes.data.data || []
        if (addresses.value.length) {
          addressId.value = addresses.value[addresses.value.length - 1].addressId
        }
      }
      newAddress.value = { contactName: '', contactPhone: '', address: '', building: '', room: '' }
      creatingAddress.value = false
    } else {
      ElMessage.error(data?.msg || '保存地址失败')
    }
  } catch (e) {
    ElMessage.error('保存地址失败')
  }
}
const checkout = async () => {
  if (!addressId.value) { ElMessage.error('请选择地址'); return }
  try {
    const payloadItems = buildOrderItems()
    const dto = { addressId: addressId.value, items: payloadItems, couponId: selectedCouponId.value || null }
    const { data } = await http.post('/orders', dto)
    if (data && data.code === 1 && data.data?.orderNo) {
      const orderNo = data.data.orderNo
      try {
        const payResponse = await http.post(`/orders/${orderNo}/pay`)
        if (payResponse && payResponse.data && payResponse.data.code === 1) {
          await http.post('/api/cart/clear')
          ElMessage.success('支付成功')
          checkoutVisible.value = false
          await load()
          router.push({ path: '/my', query: { tab: 'orders' } })
        } else {
          const errorMsg = payResponse?.data?.msg || '支付失败'
          ElMessage.error(errorMsg)
          // 支付失败后不跳转，让用户可以重新操作
          checkoutVisible.value = false
        }
      } catch (payError) {
        // 支付失败，显示后端返回的错误信息（如余额不足）
        const errorMsg = payError?.response?.data?.msg || payError?.message || '支付失败'
        ElMessage.error(errorMsg)
        // 支付失败后不跳转，让用户可以重新操作
        checkoutVisible.value = false
      }
    } else {
      ElMessage.error(data?.msg || '下单失败')
    }
  } catch (e) {
    const errorMsg = e?.response?.data?.msg || '请求失败'
    ElMessage.error(errorMsg)
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
        <div v-for="x in items" :key="x.product?.productId || x.cartItem?.productId" class="row">
          <div class="info">
            <div class="name">{{ x.product?.productName || '商品' }}</div>
            <div class="meta">
              <span class="price">¥{{ x.product?.price || '-' }}</span>
              <span class="stock">库存：{{ x.product?.stock ?? '-' }}</span>
            </div>
          </div>
          <div class="qty-box">
            <el-input-number
              :model-value="x.cartItem?.quantity || 1"
              :min="1"
              :max="x.product?.stock > 0 ? x.product.stock : undefined"
              size="small"
              @change="val => x.product && changeQty(x.product.productId, Number(val), x.product?.stock)"
            />
          </div>
          <el-button size="small" type="danger" text @click="x.product && removeItem(x.product.productId)">移除</el-button>
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
        <div v-if="addresses.length === 0" class="empty-tip">暂无收货地址，请先填写一个。</div>
      </div>
      <div class="new-addr">
        <div class="new-addr-header">
          <span>新增地址</span>
          <el-button type="primary" link size="small" @click="creatingAddress = !creatingAddress">
            {{ creatingAddress ? '收起' : '展开' }}
          </el-button>
        </div>
        <div v-if="creatingAddress" class="new-addr-form">
          <el-input v-model="newAddress.contactName" placeholder="收货人" />
          <el-input v-model="newAddress.contactPhone" placeholder="联系电话" />
          <el-input v-model="newAddress.address" placeholder="详细地址，如宿舍楼、校区等" />
          <el-input v-model="newAddress.building" placeholder="楼栋(可选)" />
          <el-input v-model="newAddress.room" placeholder="房间号(可选)" />
          <div class="new-addr-actions">
            <el-button type="primary" size="small" @click="saveNewAddress">保存并使用</el-button>
          </div>
        </div>
      </div>
      <div class="coupon-box">
        <div class="coupon-title">优惠券</div>
        <el-select
          v-model="pendingCouponId"
          clearable
          placeholder="不使用优惠券"
          style="width: 100%"
        >
          <el-option
            v-for="c in availableCoupons"
            :key="c.userCouponId || c.couponId"
            :label="couponLabel(c)"
            :value="c.couponId"
          />
        </el-select>
        <div class="coupon-actions">
          <el-button size="small" type="primary" @click="applySelectedCoupon">确定使用</el-button>
        </div>
      </div>
      <div class="amount-box">
        <div>商品金额：¥{{ Number(amountPreview.originalAmount || total()).toFixed(2) }}</div>
        <div>优惠金额：-¥{{ Number(amountPreview.discountAmount || 0).toFixed(2) }}</div>
        <div class="pay-amount">应付金额：¥{{ Number(amountPreview.payAmount || total()).toFixed(2) }}</div>
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
.addr-list{display:flex;flex-direction:column;gap:8px;margin-bottom:8px}
.addr-item{display:flex;gap:8px;align-items:center}
.empty-tip{font-size:13px;color:#9ca3af}
.new-addr{margin-top:8px;border-top:1px dashed #e5e7eb;padding-top:8px;display:flex;flex-direction:column;gap:6px}
.new-addr-header{display:flex;justify-content:space-between;align-items:center;font-size:14px;color:#374151}
.new-addr-form{display:flex;flex-direction:column;gap:6px;margin-top:4px}
.new-addr-actions{display:flex;justify-content:flex-end;margin-top:4px}
.coupon-box{margin-top:10px;border-top:1px dashed #e5e7eb;padding-top:10px}
.coupon-title{font-size:14px;color:#374151;margin-bottom:6px}
.coupon-actions{margin-top:8px;display:flex;justify-content:flex-end}
.amount-box{margin-top:10px;background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;padding:10px;color:#4b5563;display:flex;flex-direction:column;gap:4px}
.pay-amount{color:#ef4444;font-weight:700}
</style>
