<template>
  <div>
    <h1>订单管理</h1>

    <el-tabs v-model="activeTab" class="tabs">
      <el-tab-pane label="商品订单" name="product">
        <div class="filters">
          <el-input v-model="pFilters.orderNo" placeholder="订单号" style="width: 220px" />
          <el-input v-model="pFilters.userQuery" placeholder="用户昵称/手机号" style="width: 140px" />
          <el-input v-model="pFilters.sellerQuery" placeholder="商家昵称/手机号" style="width: 140px" />
          <el-select v-model="pFilters.status" placeholder="状态" style="width: 160px" clearable>
            <el-option v-for="(label, key) in productStatusMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
          <el-button type="primary" @click="loadProductOrders">搜索</el-button>
          <el-button @click="exportProductCsv">导出CSV</el-button>
          <el-button @click="exportProductXlsx">导出Excel</el-button>
        </div>

        <el-table :data="productOrders" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="170" />
          <el-table-column label="用户" width="150">
            <template #default="{ row }">
              {{ row.user?.nickname }} ({{ row.user?.phone }})
            </template>
          </el-table-column>
          <el-table-column label="商家" width="150">
            <template #default="{ row }">
              {{ row.seller?.nickname }} ({{ row.seller?.phone }})
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="金额" width="110" />
          <el-table-column prop="orderStatus" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="productStatusTagType(row.orderStatus)">
                {{ productStatusMap[row.orderStatus] || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="强制改状态" width="240">
            <template #default="{ row }">
              <el-select v-model="row._nextStatus" size="small" style="width: 130px">
                <el-option v-for="(label, key) in productStatusMap" :key="key" :label="label" :value="Number(key)" />
              </el-select>
              <el-button size="small" type="primary" @click="forceUpdateProductStatus(row)" style="margin-left: 8px">
                更新
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button
                size="small"
                type="danger"
                plain
                v-if="row.orderStatus !== 5 && row.orderStatus !== 4"
                @click="forceCancelProduct(row)"
              >强制取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="跑腿订单" name="errand">
        <div class="filters">
          <el-input v-model="eFilters.userQuery" placeholder="用户昵称/手机号" style="width: 140px" />
          <el-input v-model="eFilters.runnerQuery" placeholder="跑腿员昵称/手机号" style="width: 140px" />
          <el-select v-model="eFilters.status" placeholder="状态" style="width: 160px" clearable>
            <el-option v-for="(label, key) in errandStatusMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
          <el-button type="primary" @click="loadErrandOrders">搜索</el-button>
          <el-button @click="exportErrandCsv">导出CSV</el-button>
          <el-button @click="exportErrandXlsx">导出Excel</el-button>
        </div>

        <el-table :data="errandOrders" style="width: 100%">
          <el-table-column prop="orderNo" label="订单编号" width="160" />
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column label="用户" width="150">
            <template #default="{ row }">
              {{ row.user?.nickname }} ({{ row.user?.phone }})
            </template>
          </el-table-column>
          <el-table-column label="跑腿员" width="150">
            <template #default="{ row }">
              {{ row.runner?.nickname }} ({{ row.runner?.phone }})
            </template>
          </el-table-column>
          <el-table-column prop="reward" label="赏金" width="110" />
          <el-table-column prop="orderStatus" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="errandStatusTagType(row.orderStatus)">
                {{ errandStatusMap[row.orderStatus] || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="强制改状态" width="240">
            <template #default="{ row }">
              <el-select v-model="row._nextStatus" size="small" style="width: 130px">
                <el-option v-for="(label, key) in errandStatusMap" :key="key" :label="label" :value="Number(key)" />
              </el-select>
              <el-button size="small" type="primary" @click="forceUpdateErrandStatus(row)" style="margin-left: 8px">
                更新
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button
                size="small"
                type="danger"
                plain
                v-if="row.orderStatus !== 5 && row.orderStatus !== 4"
                @click="forceCancelErrand(row)"
              >强制取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http, { getUserDetails, searchUserIds } from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'

const activeTab = ref('product')

const productOrders = ref([])
const errandOrders = ref([])

const pFilters = ref({ orderNo: '', userQuery: '', sellerQuery: '', status: undefined })
const eFilters = ref({ userQuery: '', runnerQuery: '', status: undefined })

const productStatusMap = {
  1: '待支付',
  2: '待发货',
  3: '待收货',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款',
}

const errandStatusMap = {
  0: '待审核',
  1: '待接单',
  2: '已接单',
  3: '配送中',
  4: '已完成',
  5: '已取消',
  6: '审核拒绝',
  7: '退款中',
  8: '已退款',
}

const productStatusTagType = (s) => {
  if (s === 4) return 'success'
  if (s === 5) return 'danger'
  if (s === 1) return 'warning'
  return 'info'
}

const errandStatusTagType = (s) => {
  if (s === 4) return 'success'
  if (s === 5 || s === 6) return 'danger'
  if (s === 0) return 'warning'
  return 'info'
}

const loadProductOrders = async () => {
  try {
    let userIds = null;
    if (pFilters.value.userQuery) {
      userIds = await searchUserIds(pFilters.value.userQuery);
      if (userIds.length === 0) {
        productOrders.value = [];
        return;
      }
    }

    let sellerIds = null;
    if (pFilters.value.sellerQuery) {
      sellerIds = await searchUserIds(pFilters.value.sellerQuery);
      if (sellerIds.length === 0) {
        productOrders.value = [];
        return;
      }
    }

    const params = {
      orderNo: pFilters.value.orderNo || null,
      userId: userIds ? userIds.join(',') : null,
      sellerId: sellerIds ? sellerIds.join(',') : null,
      status: pFilters.value.status ?? null,
    }
    const { data } = await http.get('/admin/orders/products', { params })
    if (data.code === 1) {
      const fetchedOrders = await Promise.all((data.data || []).map(async it => {
        const user = it.userId ? await getUserDetails(it.userId) : null;
        const seller = it.sellerId ? await getUserDetails(it.sellerId) : null;
        return { ...it, _nextStatus: it.orderStatus, user, seller };
      }));
      productOrders.value = fetchedOrders;
    } else {
      ElMessage.error(data.msg || '加载订单失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

const loadErrandOrders = async () => {
  try {
    let userIds = null;
    if (eFilters.value.userQuery) {
      userIds = await searchUserIds(eFilters.value.userQuery);
      if (userIds.length === 0) {
        errandOrders.value = [];
        return;
      }
    }

    let runnerIds = null;
    if (eFilters.value.runnerQuery) {
      runnerIds = await searchUserIds(eFilters.value.runnerQuery);
      if (runnerIds.length === 0) {
        errandOrders.value = [];
        return;
      }
    }

    const params = {
      userId: userIds ? userIds.join(',') : null,
      runnerId: runnerIds ? runnerIds.join(',') : null,
      status: eFilters.value.status ?? null,
    }
    const { data } = await http.get('/admin/errands', { params })
    if (data.code === 1) {
      const fetchedErrands = await Promise.all((data.data || []).map(async it => {
        const user = it.userId ? await getUserDetails(it.userId) : null;
        const runner = it.runnerId ? await getUserDetails(it.runnerId) : null;
        return { ...it, _nextStatus: it.orderStatus, user, runner };
      }));
      errandOrders.value = fetchedErrands;
    } else {
      ElMessage.error(data.msg || '加载订单失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

const forceUpdateProductStatus = async (row) => {
  const nextStatus = Number(row._nextStatus)
  if (!nextStatus) {
    ElMessage.warning('请选择要更新的状态')
    return
  }
  try {
    await http.patch(`/admin/orders/products/${row.orderNo}/status`, { status: nextStatus })
    ElMessage.success('状态更新成功')
    await loadProductOrders()
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

const forceCancelProduct = async (row) => {
  let reason = ''
  try {
    const res = await ElMessageBox.prompt('请输入取消原因（可选）', `强制取消 ${row.orderNo}`, {
      confirmButtonText: '确定取消',
      cancelButtonText: '返回',
      inputPlaceholder: '如：违规订单/用户申请/异常支付等',
    })
    reason = res?.value || ''
  } catch (e) {
    return
  }
  try {
    await http.patch(`/admin/orders/products/${row.orderNo}/status`, { status: 5, cancelReason: reason || null })
    ElMessage.success('已强制取消')
    await loadProductOrders()
  } catch (e) {
    ElMessage.error('强制取消失败')
  }
}

const forceUpdateErrandStatus = async (row) => {
  const nextStatus = Number(row._nextStatus)
  if (Number.isNaN(nextStatus)) {
    ElMessage.warning('请选择要更新的状态')
    return
  }
  try {
    await http.patch(`/admin/errands/${row.orderId}/status`, { status: nextStatus })
    ElMessage.success('状态更新成功')
    await loadErrandOrders()
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

const forceCancelErrand = async (row) => {
  try {
    await ElMessageBox.confirm(`确定强制取消订单 ${row.orderNo} 吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await http.patch(`/admin/errands/${row.orderId}/status`, { status: 5 })
    ElMessage.success('已强制取消')
    await loadErrandOrders()
  } catch (e) {
    ElMessage.error('强制取消失败')
  }
}

const downloadText = (filename, text) => {
  const blob = new Blob([text], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

const toCsv = (rows, columns) => {
  const esc = (v) => {
    const s = v === null || v === undefined ? '' : String(v)
    if (/[",\n]/.test(s)) return `"${s.replace(/"/g, '""')}"`
    return s
  }
  const head = columns.map(c => esc(c.title)).join(',')
  const body = rows.map(r => columns.map(c => esc(typeof c.value === 'function' ? c.value(r) : r[c.value])).join(',')).join('\n')
  return `${head}\n${body}\n`
}

const exportProductCsv = () => {
  const cols = [
    { title: '订单号', value: 'orderNo' },
    { title: '用户ID', value: 'userId' },
    { title: '商家ID', value: 'sellerId' },
    { title: '金额', value: 'totalAmount' },
    { title: '状态', value: (r) => productStatusMap[r.orderStatus] || r.orderStatus },
    { title: '创建时间', value: 'createTime' },
  ]
  downloadText(`商品订单_${Date.now()}.csv`, toCsv(productOrders.value, cols))
}

const exportErrandCsv = () => {
  const cols = [
    { title: '订单编号', value: 'orderNo' },
    { title: '标题', value: 'title' },
    { title: '用户ID', value: 'userId' },
    { title: '跑腿员ID', value: 'runnerId' },
    { title: '赏金', value: 'reward' },
    { title: '状态', value: (r) => errandStatusMap[r.orderStatus] || r.orderStatus },
    { title: '创建时间', value: 'createTime' },
  ]
  downloadText(`跑腿订单_${Date.now()}.csv`, toCsv(errandOrders.value, cols))
}

const exportProductXlsx = () => {
  const data = productOrders.value.map(r => ({
    订单号: r.orderNo,
    用户ID: r.userId,
    商家ID: r.sellerId,
    金额: r.totalAmount,
    状态: productStatusMap[r.orderStatus] || r.orderStatus,
    创建时间: r.createTime,
  }))
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '商品订单')
  XLSX.writeFile(wb, `商品订单_${Date.now()}.xlsx`)
}

const exportErrandXlsx = () => {
  const data = errandOrders.value.map(r => ({
    订单编号: r.orderNo,
    标题: r.title,
    用户ID: r.userId,
    跑腿员ID: r.runnerId,
    赏金: r.reward,
    状态: errandStatusMap[r.orderStatus] || r.orderStatus,
    创建时间: r.createTime,
  }))
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '跑腿订单')
  XLSX.writeFile(wb, `跑腿订单_${Date.now()}.xlsx`)
}

onMounted(async () => {
  await loadProductOrders()
})
</script>

<style scoped>
.filters{
  display:flex;
  flex-wrap:wrap;
  gap:10px;
  margin-bottom:14px;
}
.tabs :deep(.el-tabs__header){margin-bottom:12px}
</style>

