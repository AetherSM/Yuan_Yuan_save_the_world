<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '../services/http'
import { ElMessage, ElInputNumber, ElRate } from 'element-plus'
const route = useRoute()
const id = Number(route.params.id)
const product = ref(null)
const loading = ref(false)
const error = ref('')
const qty = ref(1)
const addresses = ref([])
const chooseVisible = ref(false)
const addressId = ref(null)
const creatingAddress = ref(false)
const newAddress = ref({ contactName: '', contactPhone: '', address: '', building: '', room: '' })
const reviews = ref([])
const replies = ref(new Map())
const replyContent = ref(new Map())
const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get(`/products/${id}`)
    if (data && data.code === 1) product.value = data.data
    else error.value = data?.msg || '加载失败'
  } catch (e) {
    error.value = '请求失败'
  } finally {
    loading.value = false
  }
}
onMounted(load)
onMounted(async () => {
  try {
    const { data } = await http.get(`/products/${id}/reviews`)
    if (data && data.code === 1) {
      reviews.value = (data.data || []).map(r => ({
        ...r,
        reviewerNickname: r.isAnonymous === 1 ? '匿名用户' : (r.reviewerNickname || '用户')
      }))
      for (const r of reviews.value) {
        await loadReplies(r.reviewId)
      }
    }
  } catch (e) {}
})
const addToCart = async () => {
  try {
    const { data } = await http.post('/api/cart/add', null, { params: { productId: id, quantity: qty.value } })
    if (data && data.code === 1) ElMessage.success('已加入购物车')
    else ElMessage.error(data?.msg || '加入购物车失败')
  } catch (e) {
    ElMessage.error('请求失败')
  }
}
const openBuy = async () => {
  try {
    const { data } = await http.get('/addresses')
    if (data && data.code === 1) {
      addresses.value = data.data || []
      addressId.value = addresses.value[0]?.addressId || null
      chooseVisible.value = true
      creatingAddress.value = addresses.value.length === 0
    }
  } catch (e) {}
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
const payNow = async () => {
  if (!addressId.value) { ElMessage.error('请选择地址'); return }
  try {
    const dto = {
      addressId: addressId.value,
      items: [{ productId: id, quantity: qty.value }],
      remark: ''
    }
    const { data } = await http.post('/orders', dto)
    if (data && data.code === 1 && data.data && data.data.orderNo) {
      const orderNo = data.data.orderNo
      await http.post(`/orders/${orderNo}/pay`)
      ElMessage.success('支付成功')
    } else {
      ElMessage.error(data?.msg || '下单失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  } finally {
    chooseVisible.value = false
  }
}

const loadReplies = async (reviewId) => {
  try {
    const { data } = await http.get(`/reviews/${reviewId}/replies`)
    if (data && data.code === 1) {
      replies.value.set(reviewId, data.data || [])
    }
  } catch (e) {}
}

const submitReply = async (reviewId) => {
  const content = replyContent.value.get(reviewId) || ''
  if (!content || content.trim().length === 0) { ElMessage.error('请输入回复内容'); return }
  try {
    const { data } = await http.post(`/reviews/${reviewId}/replies`, { content })
    if (data && data.code === 1) {
      ElMessage.success('回复成功')
      replyContent.value.set(reviewId, '')
      await loadReplies(reviewId)
    } else {
      ElMessage.error(data?.msg || '回复失败')
    }
  } catch (e) { ElMessage.error('请求失败') }
}
</script>

<template>
  <div v-if="product" class="detail">
    <div class="gallery">
      <img :src="product.mainImage || 'https://via.placeholder.com/600x360?text=Product'" alt="" />
    </div>
    <div class="panel">
      <h2 class="title">{{ product.productName }}</h2>
      <div class="price-panel">
        <div class="price-line">
          <span class="label">价格</span>
          <span class="current">¥{{ product.price }}</span>
          <span class="original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
        </div>
        <div class="benefits">
          <span class="tag red">自营</span>
          <span class="tag green">满减</span>
        </div>
      </div>
      <p class="desc">{{ product.description }}</p>
      <div class="meta">库存 {{ product.stock }} ｜ 销量 {{ product.salesCount }}</div>
      <div class="buy-area">
        <div class="qty-line">
          <span>数量</span>
          <el-input-number v-model="qty" :min="1" />
        </div>
        <div class="actions">
          <button class="btn cart" @click="addToCart">加入购物车</button>
          <button class="btn primary" @click="openBuy">立即购买</button>
        </div>
      </div>
    </div>
  </div>
  <div class="reviews">
    <h3 class="rv-title">用户评价</h3>
    <div v-if="reviews.length===0" class="rv-empty">暂无评价</div>
    <div v-for="rv in reviews" :key="rv.reviewId" class="rv-card">
      <div class="rv-head">
        <div class="rv-user">{{ rv.reviewerNickname }}</div>
        <el-rate :model-value="rv.rating" :max="5" disabled />
      </div>
      <div class="rv-content">{{ rv.content }}</div>
      <div class="rv-replies" v-if="(replies.get(rv.reviewId)||[]).length>0">
        <div class="rp-item" v-for="rp in replies.get(rv.reviewId)" :key="rp.replyId">
          <span class="rp-user">{{ rp.replierNickname || '用户' }}：</span>
          <span class="rp-text">{{ rp.content }}</span>
        </div>
      </div>
      <div class="rv-reply-box">
        <input class="reply-input" :value="replyContent.get(rv.reviewId)||''" @input="e => replyContent.set(rv.reviewId, e.target.value)" placeholder="回复TA..." />
        <button class="btn" @click="submitReply(rv.reviewId)">回复</button>
      </div>
    </div>
  </div>
  <div v-if="!loading && !error && !product" class="empty">暂无数据</div>
  <div v-if="error" class="error">{{ error }}</div>
  <el-dialog v-model="chooseVisible" title="选择收货地址" width="520px">
    <div class="addr-list">
      <label v-for="a in addresses" :key="a.addressId" class="addr-item">
        <input type="radio" :value="a.addressId" v-model="addressId" />
        <span>{{ a.contactName }}（{{ a.contactPhone }}）{{ a.address }} {{ a.building }} {{ a.room }}</span>
      </label>
      <div v-if="addresses.length === 0" class="addr-empty">暂无收货地址，请先填写一个。</div>
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
    <template #footer>
      <div class="dialog-footer">
        <button class="btn" @click="payNow">支付</button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.detail{display:grid;grid-template-columns:1fr 1fr;gap:16px;background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:16px}
.gallery img{width:100%;height:420px;object-fit:cover;background:#f3f4f6;border-radius:12px}
.panel{display:flex;flex-direction:column;gap:12px}
.title{font-weight:700;color:#111827}
.price-panel{border:1px solid #fee2e2;background:#fff1f2;border-radius:12px;padding:12px}
.price-line{display:flex;align-items:center;gap:12px}
.price-line .label{color:#9ca3af}
.current{color:#ef4444;font-weight:800;margin-right:8px;font-size:22px}
.original{color:#9ca3af;text-decoration:line-through}
.benefits{display:flex;gap:8px;margin-top:6px}
.tag{display:inline-block;padding:2px 6px;border-radius:6px;font-size:12px;border:1px solid #e5e7eb;color:#374151}
.tag.red{border-color:#ef4444;color:#ef4444}
.tag.green{border-color:#10b981;color:#10b981}
.desc{color:#374151}
.meta{color:#6b7280}
.buy-area{display:flex;flex-direction:column;gap:12px}
.qty-line{display:flex;align-items:center;gap:12px}
.actions{display:flex;gap:12px}
.btn{padding:10px 16px;border:none;border-radius:10px;background:#e5e7eb;color:#111827;cursor:pointer}
.btn.cart{background:#ffedd5;color:#b45309}
.btn.primary{background:#ff1f2d;color:#fff}
.empty{padding:24px;text-align:center;color:#999}
.error{padding:12px;color:#d33}
.addr-list{display:flex;flex-direction:column;gap:8px;margin-bottom:8px}
.addr-item{display:flex;gap:8px;align-items:center}
.addr-empty{font-size:13px;color:#9ca3af}
.new-addr{margin-top:8px;border-top:1px dashed #e5e7eb;padding-top:8px;display:flex;flex-direction:column;gap:6px}
.new-addr-header{display:flex;justify-content:space-between;align-items:center;font-size:14px;color:#374151}
.new-addr-form{display:flex;flex-direction:column;gap:6px;margin-top:4px}
.new-addr-actions{display:flex;justify-content:flex-end;margin-top:4px}
.reviews{margin-top:16px;background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:12px}
.rv-title{font-weight:700;margin-bottom:8px}
.rv-card{border-top:1px solid #f3f4f6;padding:10px 0}
.rv-card:first-child{border-top:none}
.rv-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:6px}
.rv-user{font-weight:600;color:#111827}
.rv-content{color:#374151;margin-bottom:8px}
.rv-replies{display:flex;flex-direction:column;gap:6px;margin-bottom:8px}
.rp-item{font-size:14px;color:#4b5563}
.rp-user{font-weight:600;color:#111827;margin-right:6px}
.rv-reply-box{display:flex;gap:8px;align-items:center}
.reply-input{flex:1;padding:8px;border:1px solid #e5e7eb;border-radius:8px}
</style>
