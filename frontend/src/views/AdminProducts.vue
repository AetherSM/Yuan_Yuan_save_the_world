<template>
  <div>
    <h1>商品管理</h1>
    <div class="filters">
      <el-input v-model="filters.keyword" placeholder="商品名称" />
      <el-input v-model="filters.sellerId" placeholder="商家ID" />
      <el-button type="primary" @click="loadProducts">搜索</el-button>
    </div>

    <el-table :data="products" style="width: 100%">
      <el-table-column prop="productId" label="ID" width="80" />
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="sellerId" label="商家ID" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="stock" label="库存" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ statusMap[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="updateStatus(row, 0)" v-if="row.status === 1">强制下架</el-button>
          <el-button size="small" type="success" @click="updateStatus(row, 1)" v-if="row.status === 0">重新上架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const products = ref([])
const filters = ref({
  keyword: '',
  sellerId: null,
})

const statusMap = {
  1: '上架',
  0: '下架',
}

const loadProducts = async () => {
  try {
    const params = {
      keyword: filters.value.keyword || null,
      sellerId: filters.value.sellerId || null,
    }
    const { data } = await http.get('/admin/products', { params })
    if (data.code === 1) {
      products.value = data.data
    } else {
      ElMessage.error(data.msg || '加载商品失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  }
}

const updateStatus = async (product, status) => {
  try {
    await http.patch(`/admin/products/${product.productId}/status`, { status })
    ElMessage.success('操作成功')
    await loadProducts()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(loadProducts)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>
