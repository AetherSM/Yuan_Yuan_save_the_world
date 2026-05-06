<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessage } from 'element-plus'
const router = useRouter()
const list = ref([])
const loading = ref(false)
const error = ref('')
const userType = Number(localStorage.getItem('userType') || 0)
const userId = Number(localStorage.getItem('userId') || 0)

const plazaStatusMap = {
  0: '待审核',
  1: '待接单',
  3: '待重新接单'
}
const sameUser = (item) => Number(item.userId) === userId
const plazaStatusLabel = (item) => {
  const s = item.orderStatus
  if (sameUser(item)) return `${plazaStatusMap[s] || statusLabel(s)} · 我发布的`
  return plazaStatusMap[s] || statusLabel(s)
}
const statusLabel = (s) => {
  const m = { 0: '待审核', 1: '待接单', 2: '已接单', 3: '配送中', 4: '已完成', 5: '已取消', 6: '审核拒绝', 7: '退款中', 8: '已退款' }
  return m[s] ?? `状态(${s})`
}
const plazaTagType = (s) => {
  if (s === 0) return 'warning'
  if (s === 3) return 'danger'
  return 'info'
}
const canTakePlaza = (item) => {
  if (userType !== 2) return false
  if (sameUser(item)) return false
  return item.orderStatus === 1 || item.orderStatus === 3
}
const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get('/api/errands/open')
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
const take = async (orderNo) => {
  try {
    const { data } = await http.post('/api/errands/take', null, { params: { orderNo, runnerId: userId } })
    if (data && data.code === 1) {
      await load()
      router.push('/errands/runner')
    } else {
      const msg = data?.msg || '接单失败'
      error.value = msg
      ElMessage.error(msg)
    }
  } catch (e) {
    error.value = '请求失败'
    ElMessage.error('请求失败')
  }
}
const goCreate = () => {
  router.push('/errands/create')
}
</script>

<template>
  <div class="page">
    <div class="page-card toolbar">
      <div class="toolbar-left">
        <div class="title">跑腿广场</div>
        <div class="muted">找人帮忙 / 接单赚钱</div>
      </div>
      <div class="toolbar-right">
        <el-button v-if="userType===1" type="danger" @click="goCreate">发布跑腿</el-button>
        <el-button type="info" plain @click="load" :loading="loading">刷新</el-button>
      </div>
    </div>

    <div v-if="error" class="page-card">
      <el-alert type="error" :closable="false" :title="error" />
    </div>

    <el-skeleton v-if="loading" animated :rows="6" class="page-card" />

    <div v-else-if="list.length===0" class="page-card">
      <el-empty description="暂无可接跑腿任务" />
    </div>

    <div v-else class="grid">
      <div v-for="item in list" :key="item.orderId" class="card">
        <div class="row">
          <div class="title2">{{ item.title }}</div>
          <el-tag :type="plazaTagType(item.orderStatus)" effect="plain">{{ plazaStatusLabel(item) }}</el-tag>
        </div>
        <div class="desc">{{ item.description }}</div>
        <div class="row meta">
          <div>赏金 <span class="money">¥{{ item.reward }}</span></div>
          <div>联系人 {{ item.contactName }}</div>
        </div>
        <div v-if="item.orderStatus === 0" class="hint">管理员审核通过后，跑腿员即可接单。</div>
        <div v-if="item.orderStatus === 3" class="hint warn">系统显示为配送中但未分配跑腿员，可由跑腿员重新接单以修正状态。</div>
        <div v-if="sameUser(item) && userType === 2" class="hint">您发布的任务，需由其他跑腿员接单。</div>
        <div class="ops">
          <el-button v-if="canTakePlaza(item)" type="primary" @click="take(item.orderNo)">接单</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:12px}
.card{padding:12px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.row{display:flex;justify-content:space-between;align-items:center;margin:6px 0}
.title2{font-weight:700;color:#111827}
.desc{color:#4b5563;font-size:14px;line-height:1.5;min-height:38px}
.meta{color:#6b7280;font-size:13px}
.money{color:#ef4444;font-weight:800}
.ops{display:flex;justify-content:flex-end;margin-top:10px}
.hint{font-size:12px;color:#6b7280;margin-top:6px;line-height:1.4}
.hint.warn{color:#b45309}
</style>
