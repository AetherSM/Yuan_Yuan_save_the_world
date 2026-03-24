<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessage } from 'element-plus'
import NoticeBar from '../components/NoticeBar.vue'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const error = ref('')
const filters = ref({
  keyword: '',
  status: undefined,
})
const sellerId = Number(localStorage.getItem('userId') || 0)

const load = async () => {
  loading.value = true
  try {
    const params = {
      sellerId,
      keyword: filters.value.keyword || null,
      status: filters.value.status ?? null,
    }
    const { data } = await http.get('/products', { params })
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

const goCreate = () => {
  router.push('/merchant/product/create')
}

const goEdit = (id) => {
  router.push({ path: '/merchant/product/create', query: { id } })
}

const toggleStatus = async (item) => {
  try {
    const newStatus = item.status === 1 ? 0 : 1
    const { data } = await http.patch(`/products/${item.productId}/status`, { status: newStatus })
    if (data && data.code === 1) {
      ElMessage.success('状态已更新')
      item.status = newStatus
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
        <div class="title">商品管理</div>
        <div class="muted">管理店铺商品的上架、下架与库存</div>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="goCreate">发布新商品</el-button>
        <el-button type="info" plain @click="load" :loading="loading">刷新</el-button>
      </div>
    </div>

    <div class="page-card toolbar">
      <div class="toolbar-left">
        <el-input v-model="filters.keyword" placeholder="搜索商品名称" style="width:220px" clearable />
        <el-select v-model="filters.status" placeholder="状态" style="width:140px" clearable>
          <el-option :value="1" label="上架中" />
          <el-option :value="0" label="已下架" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" link @click="load">应用筛选</el-button>
      </div>
    </div>

    <div v-if="error" class="page-card">
      <el-alert type="error" :closable="false" :title="error" />
    </div>

    <el-skeleton v-if="loading" animated :rows="6" class="page-card" />

    <div v-else-if="list.length === 0" class="page-card">
      <el-empty description="暂无商品" />
    </div>

    <div v-else class="page-card">
      <el-table :data="list" stripe style="width: 100%">
        <el-table-column prop="productId" label="ID" width="70" />
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="salesCount" label="销量" width="90" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status===1 ? 'success' : 'info'">
              {{ row.status===1 ? '上架中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="goEdit(row.productId)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status===1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status===1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.toolbar{margin-bottom:8px}
</style>
