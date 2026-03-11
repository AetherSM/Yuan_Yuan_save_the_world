<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../services/http'
const router = useRouter()
const list = ref([])
const loading = ref(false)
const error = ref('')
const userType = Number(localStorage.getItem('userType') || 0)
const userId = Number(localStorage.getItem('userId') || 0)
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
    }
    else error.value = data?.msg || '接单失败'
  } catch (e) { error.value = '请求失败' }
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
          <el-tag type="warning">待接单</el-tag>
        </div>
        <div class="desc">{{ item.description }}</div>
        <div class="row meta">
          <div>赏金 <span class="money">¥{{ item.reward }}</span></div>
          <div>联系人 {{ item.contact_name || item.contactName }}</div>
        </div>
        <div class="ops">
          <el-button v-if="userType===2" type="primary" @click="take(item.orderNo)">接单</el-button>
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
</style>
