<script setup>
import { ref, onMounted, computed } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'
import NoticeBar from '../components/NoticeBar.vue'

const list = ref([])
const loading = ref(false)
const error = ref('')
const runnerId = Number(localStorage.getItem('userId') || 0)
const activeTab = ref('ongoing') // ongoing, history
const tabOptions = [
  { label: '进行中', value: 'ongoing' },
  { label: '跑腿记录', value: 'history' },
]

const statusMap = {
  1: '待接单',
  2: '已接单',
  3: '配送中',
  4: '已完成',
  5: '已取消'
}

const displayList = computed(() => {
  if (activeTab.value === 'ongoing') {
    return list.value.filter(i => i.orderStatus === 2 || i.orderStatus === 3)
  } else {
    return list.value.filter(i => i.orderStatus === 4 || i.orderStatus === 5)
  }
})

const tagType = (s) => {
  if (s === 4) return 'success'
  if (s === 5) return 'danger'
  if (s === 2 || s === 3) return 'info'
  return 'warning'
}

const load = async () => {
  loading.value = true
  try {
    const { data } = await http.get('/api/errands/runner', { params: { runnerId } })
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

const complete = async (orderNo) => {
  try {
    const { data } = await http.post('/api/errands/complete', null, { params: { orderNo } })
    if (data && data.code === 1) {
      ElMessage.success('已确认送达')
      await load()
    } else {
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

const startDelivery = async (orderNo) => {
  try {
    const { data } = await http.post('/api/errands/start-delivery', null, { params: { orderNo } })
    if (data && data.code === 1) {
      ElMessage.success('已开始配送')
      await load()
    } else {
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <NoticeBar />
    <div class="page-card toolbar">
      <div class="toolbar-left">
        <div class="title">我的跑腿任务</div>
        <div class="muted">进行中 / 历史记录</div>
      </div>
      <div class="toolbar-right">
        <el-button type="info" plain @click="load" :loading="loading">刷新</el-button>
      </div>
    </div>

    <div class="page-card">
      <el-segmented v-model="activeTab" :options="tabOptions" />
    </div>

    <div v-if="error" class="page-card">
      <el-alert type="error" :closable="false" :title="error" />
    </div>

    <el-skeleton v-if="loading" animated :rows="6" class="page-card" />

    <div v-else-if="displayList.length === 0" class="page-card">
      <el-empty description="暂无任务" />
    </div>

    <div v-else class="grid">
      <div v-for="item in displayList" :key="item.orderId" class="card">
        <div class="row">
          <div class="title2">{{ item.title }}</div>
          <el-tag :type="tagType(item.orderStatus)">{{ statusMap[item.orderStatus] }}</el-tag>
        </div>
        <div class="desc">{{ item.description }}</div>
        <div class="row info">
          <span>赏金: <span class="money">¥{{ item.reward }}</span></span>
          <span>{{ item.contactName }} {{ item.contactPhone }}</span>
        </div>
        <div class="row info" v-if="item.pickupCode">
          <span>取件码: <b>{{ item.pickupCode }}</b></span>
        </div>
        <div class="addresses">
          <div><b>取</b>：{{ item.pickupAddress }}</div>
          <div><b>送</b>：{{ item.deliveryAddress }}</div>
        </div>
        <div class="ops">
          <el-button v-if="item.orderStatus === 2" type="info" @click="startDelivery(item.orderNo)">
            开始配送
          </el-button>
          <el-button v-if="item.orderStatus === 2 || item.orderStatus === 3" type="primary" @click="complete(item.orderNo)">
            确认送达
          </el-button>
          <el-tag v-if="item.orderStatus === 4" type="success">已完成</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid{display:flex;flex-direction:column;gap:12px}
.card{padding:12px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.row{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px}
.title2{font-weight:800;font-size:16px;color:#111827}
.desc{color:#4b5563;font-size:14px;margin-bottom:8px;line-height:1.5}
.info{color:#888;font-size:13px;margin-bottom:8px}
.money{color:#ef4444;font-weight:800}
.addresses{font-size:13px;color:#374151;background:#f9fafb;padding:10px;border-radius:10px;margin-bottom:8px;border:1px dashed #e5e7eb}
.ops{text-align:right;margin-top:8px}
</style>
