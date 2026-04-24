<template>
  <div>
    <h1>管理概览</h1>

    <el-row :gutter="16" class="cards">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="card">
          <div class="card-inner" @click="go('/admin/products')">
            <div class="card-title">待审核商品</div>
            <div class="card-value">{{ pendingProducts }}</div>
            <div class="card-tip">点击进入商品管理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="card">
          <div class="card-inner" @click="go('/admin/errands')">
            <div class="card-title">待审核跑腿</div>
            <div class="card-value">{{ pendingErrands }}</div>
            <div class="card-tip">点击进入跑腿任务管理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="card">
          <div class="card-inner" @click="go('/admin/complaints')">
            <div class="card-title">待处理投诉</div>
            <div class="card-value">{{ pendingComplaints }}</div>
            <div class="card-tip">点击进入投诉管理</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>待处理投诉趋势（按日期）</span>
              <el-date-picker
                v-model="complaintDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                @change="refresh"
              />
            </div>
          </template>
          <div ref="complaintChartEl" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="chart-header">跑腿状态分布</div>
          </template>
          <div ref="errandChartEl" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="chart-header">按日期的待处理投诉</div>
          </template>
          <el-table :data="pendingByDate" size="small" style="width: 100%">
            <el-table-column prop="date" label="日期" width="140" />
            <el-table-column prop="count" label="待处理数量" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <div class="actions">
      <el-button type="primary" @click="refresh" :loading="loading">刷新数据</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const router = useRouter()

const loading = ref(false)
const pendingProducts = ref(0)
const pendingErrands = ref(0)
const pendingComplaints = ref(0)
const pendingByDate = ref([])
const complaintDateRange = ref([]) // 用于日期选择器

const go = (path) => router.push(path)

const complaintChartEl = ref(null)
const errandChartEl = ref(null)
let complaintChart = null
let errandChart = null

const renderComplaintChart = (complaints) => {
  if (!complaintChartEl.value) return
  complaintChart ??= echarts.init(complaintChartEl.value)
  const map = new Map()
  for (const item of (Array.isArray(complaints) ? complaints : [])) {
    const t = item.createTime || ''
    const d = t.split(' ')[0] || '未知'
    map.set(d, (map.get(d) || 0) + 1)
  }
  const rows = Array.from(map.entries()).sort((a, b) => a[0] > b[0] ? 1 : -1)
  const labels = rows.map(([d]) => d)
  const values = rows.map(([, c]) => c)
  complaintChart.setOption({
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: values, smooth: true, itemStyle: { color: '#409EFF' } }],
  })
}

const renderErrandChart = (list) => {
  if (!errandChartEl.value) return
  errandChart ??= echarts.init(errandChartEl.value)
  const map = new Map()
  for (const it of (Array.isArray(list) ? list : [])) {
    const s = Number(it.orderStatus)
    map.set(s, (map.get(s) || 0) + 1)
  }
  const labels = ['待审核', '待接单', '已接单', '配送中', '已完成', '已取消', '审核拒绝']
  const values = [0, 1, 2, 3, 4, 5, 6].map(k => map.get(k) || 0)
  errandChart.setOption({
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: values, itemStyle: { color: '#67C23A' } }],
  })
}

const refresh = async () => {
  loading.value = true
  try {
    const complaintParams = { status: 0 };
    if (complaintDateRange.value && complaintDateRange.value.length === 2) {
      complaintParams.startTime = complaintDateRange.value[0];
      complaintParams.endTime = complaintDateRange.value[1];
    }

    const results = await Promise.allSettled([
      http.get('/admin/products/pending'),
      http.get('/admin/errands/pending'),
      http.get('/admin/complaints', { params: complaintParams }),
      http.get('/admin/complaints/stats'),
      http.get('/admin/errands'),
    ])

    const val = (i) => (results[i]?.status === 'fulfilled' ? results[i].value : null)
    const errMsg = (i) => {
      const r = results[i]
      if (!r || r.status !== 'rejected') return null
      const e = r.reason
      return e?.response?.data?.msg || e?.response?.data?.message || e?.message || '请求失败'
    }

    const p = val(0)
    const e = val(1)
    const c = val(2)
    const cs = val(3)
    const allErrands = val(4)

    if (p) pendingProducts.value = Array.isArray(p?.data?.data) ? p.data.data.length : 0
    if (e) pendingErrands.value = Array.isArray(e?.data?.data) ? e.data.data.length : 0
    const complaints = Array.isArray(c?.data?.data) ? c.data.data : []
    if (c) pendingComplaints.value = complaints.length

    // 统计按日期的待处理投诉
    const map = new Map()
    for (const item of complaints) {
      const t = item.createTime || ''
      const d = t.split(' ')[0] || '未知'
      map.set(d, (map.get(d) || 0) + 1)
    }
    pendingByDate.value = Array.from(map.entries())
      .sort((a, b) => a[0] < b[0] ? 1 : -1) // 最近在上
      .map(([date, count]) => ({ date, count }))

    await nextTick()
    if (complaints) renderComplaintChart(complaints)
    if (allErrands) renderErrandChart(allErrands?.data?.data)

    const msgs = [errMsg(0), errMsg(1), errMsg(2), errMsg(3), errMsg(4)].filter(Boolean)
    if (msgs.length) {
      ElMessage.error(`概览部分数据加载失败：${msgs[0]}`)
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.msg || err?.message || '加载概览数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(refresh)

const resize = () => {
  complaintChart?.resize()
  errandChart?.resize()
}
window.addEventListener('resize', resize)
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  complaintChart?.dispose()
  errandChart?.dispose()
  complaintChart = null
  errandChart = null
})
</script>

<style scoped>
.cards{margin-bottom:16px}
.card-inner{cursor:pointer}
.card-title{color:#6b7280;font-size:13px;margin-bottom:6px}
.card-value{font-size:34px;font-weight:800;color:#111827;line-height:1}
.card-tip{color:#9ca3af;font-size:12px;margin-top:10px}
.charts{margin-bottom:16px}
.chart-header{font-size:14px;font-weight:600;color:#111827}
.chart{height:280px}
.actions{display:flex;justify-content:flex-end;margin-top:8px}
</style>
