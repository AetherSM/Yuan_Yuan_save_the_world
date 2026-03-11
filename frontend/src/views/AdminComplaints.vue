<template>
  <div>
    <h1>投诉管理</h1>
    <div class="filters">
      <el-select v-model="filters.status" placeholder="状态">
        <el-option label="全部" value="" />
        <el-option label="待处理" value="0" />
        <el-option label="处理中" value="1" />
        <el-option label="已完成" value="2" />
      </el-select>
      <el-select v-model="filters.orderType" placeholder="订单类型">
        <el-option label="全部" value="" />
        <el-option label="商品订单" value="1" />
        <el-option label="跑腿订单" value="2" />
      </el-select>
      <el-button type="primary" @click="loadComplaints">搜索</el-button>
      <el-button type="info" @click="loadStats">查看统计</el-button>
    </div>

    <div v-if="stats.length > 0" class="stats">
      <h3>投诉统计</h3>
      <el-row :gutter="20">
        <el-col :span="8" v-for="stat in stats" :key="stat.status">
          <el-card>
            <div class="stat-item">
              <div class="stat-label">{{ getStatusText(stat.status) }}</div>
              <div class="stat-value">{{ stat.count }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-table :data="complaints" style="width: 100%">
      <el-table-column prop="complaintId" label="ID" width="80" />
      <el-table-column prop="orderId" label="订单ID" />
      <el-table-column prop="orderType" label="订单类型">
        <template #default="{ row }">
          {{ row.orderType === 1 ? '商品订单' : '跑腿订单' }}
        </template>
      </el-table-column>
      <el-table-column prop="complainantId" label="投诉人ID" />
      <el-table-column prop="reason" label="投诉原因" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleComplaint(row)" v-if="row.status !== 2">处理</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      title="处理投诉"
      width="500px"
    >
      <div v-if="currentComplaint">
        <el-form label-width="80px">
          <el-form-item label="投诉ID">
            {{ currentComplaint.complaintId }}
          </el-form-item>
          <el-form-item label="订单ID">
            {{ currentComplaint.orderId }}
          </el-form-item>
          <el-form-item label="订单类型">
            {{ currentComplaint.orderType === 1 ? '商品订单' : '跑腿订单' }}
          </el-form-item>
          <el-form-item label="投诉人ID">
            {{ currentComplaint.complainantId }}
          </el-form-item>
          <el-form-item label="投诉原因">
            {{ currentComplaint.reason }}
          </el-form-item>
          <el-form-item label="处理结果">
            <el-input
              v-model="handleResult"
              type="textarea"
              rows="4"
              placeholder="请输入处理结果"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHandle">提交处理</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const complaints = ref([])
const stats = ref([])
const filters = ref({
  status: '',
  orderType: '',
})

const dialogVisible = ref(false)
const currentComplaint = ref(null)
const handleResult = ref('')

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待处理'
    case 1: return '处理中'
    case 2: return '已完成'
    default: return '未知'
  }
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'info'
    case 2: return 'success'
    default: return ''
  }
}

const loadComplaints = async () => {
  try {
    const params = {
      status: filters.value.status || null,
      orderType: filters.value.orderType || null,
    }
    const { data } = await http.get('/admin/complaints', { params })
    if (data.code === 1) {
      complaints.value = data.data
    } else {
      ElMessage.error(data.msg || '加载投诉失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  }
}

const loadStats = async () => {
  try {
    const { data } = await http.get('/admin/complaints/stats')
    if (data.code === 1) {
      stats.value = data.data
    } else {
      ElMessage.error(data.msg || '加载统计失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  }
}

const handleComplaint = (complaint) => {
  currentComplaint.value = complaint
  handleResult.value = ''
  dialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleResult.value.trim()) {
    ElMessage.warning('请输入处理结果')
    return
  }
  
  try {
    await http.post(`/admin/complaints/${currentComplaint.value.complaintId}/resolve`, null, {
      params: { result: handleResult.value }
    })
    ElMessage.success('处理成功')
    dialogVisible.value = false
    await loadComplaints()
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

onMounted(() => {
  loadComplaints()
  loadStats()
})
</script>

<style scoped>
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stats {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.dialog-footer {
  text-align: right;
}
</style>