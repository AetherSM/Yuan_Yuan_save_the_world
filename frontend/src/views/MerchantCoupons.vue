<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const coupons = ref([])
const loading = ref(false)
const error = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const distributeVisible = ref(false)
const issuanceVisible = ref(false)
const issuanceList = ref([])
const userOptions = ref([])
const userSearchLoading = ref(false)
const distributeForm = ref({
  couponId: null,
  userIds: []
})

// 表单数据
const form = ref({
  couponId: null,
  name: '',
  type: 1,
  value: 0,
  minSpend: 0,
  totalCount: null,
  startTime: '',
  endTime: '',
  status: 1
})

const typeOptions = [
  { label: '满减券', value: 1 },
  { label: '折扣券', value: 2 }
]

const statusOptions = [
  { label: '无效', value: 0 },
  { label: '有效', value: 1 }
]

// 加载优惠券列表
const loadCoupons = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get('/api/coupons/merchant/list')
    if (data && data.code === 1) {
      coupons.value = data.data || []
    } else {
      error.value = data?.msg || '加载失败'
    }
  } catch (e) {
    error.value = '请求失败'
  } finally {
    loading.value = false
  }
}

// 远程搜索用户
const searchUsers = async (query) => {
  const q = (query || '').trim()
  if (!q) {
    userOptions.value = []
    return
  }
  userSearchLoading.value = true
  try {
    const { data } = await http.get('/auth/query', { params: { keyword: q } })
    if (data && data.code === 1) {
      userOptions.value = (data.data || []).slice(0, 50)
    } else {
      userOptions.value = []
    }
  } catch (e) {
    userOptions.value = []
  } finally {
    userSearchLoading.value = false
  }
}

// 打开创建对话框
const openCreateDialog = () => {
  isEdit.value = false
  form.value = {
    couponId: null,
    name: '',
    type: 1,
    value: 0,
    minSpend: 0,
    totalCount: null,
    startTime: '',
    endTime: '',
    status: 1
  }
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (coupon) => {
  isEdit.value = true
  form.value = {
    couponId: coupon.couponId,
    name: coupon.name,
    type: coupon.type,
    value: coupon.value,
    minSpend: coupon.minSpend,
    totalCount: coupon.totalCount,
    startTime: coupon.startTime,
    endTime: coupon.endTime,
    status: coupon.status
  }
  dialogVisible.value = true
}

// 格式化时间为后端期望的格式
const formatDateTime = (date) => {
  if (!date) return null
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 提交表单
const submitForm = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入优惠券名称')
    return
  }
  if (form.value.value <= 0) {
    ElMessage.warning('面值必须大于0')
    return
  }
  if (form.value.minSpend < 0) {
    ElMessage.warning('最低消费不能为负数')
    return
  }
  if (!form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  if (new Date(form.value.startTime) >= new Date(form.value.endTime)) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }

  try {
    const submitData = {
      ...form.value,
      startTime: formatDateTime(form.value.startTime),
      endTime: formatDateTime(form.value.endTime)
    }
    const { data } = await http.post('/api/coupons/merchant/save', submitData)
    if (data && data.code === 1) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      await loadCoupons()
    } else {
      ElMessage.error(data?.msg || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

const handleDelete = async (coupon) => {
  try {
    await ElMessageBox.confirm('确定要删除这张优惠券吗？删除后用户已领取的券仍可使用，但不再发放。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { data } = await http.delete(`/api/coupons/${coupon.couponId}`)
    if (data && data.code === 1) {
      ElMessage.success('删除成功')
      await loadCoupons()
    } else {
      ElMessage.error(data?.msg || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('请求失败')
    }
  }
}

const openIssuanceDialog = async (coupon) => {
  try {
    const { data } = await http.get(`/api/coupons/${coupon.couponId}/issuance`)
    if (data && data.code === 1) {
      issuanceList.value = data.data || []
      issuanceVisible.value = true
    } else {
      ElMessage.error(data?.msg || '获取发放详情失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

const openDistributeDialog = (coupon) => {
  distributeForm.value = {
    couponId: coupon.couponId,
    userIds: []
  }
  userOptions.value = []
  distributeVisible.value = true
}

const submitDistribute = async () => {
  if (!distributeForm.value.userIds.length) {
    ElMessage.warning('请选择至少一个用户')
    return
  }
  try {
    const { data } = await http.post('/api/coupons/merchant/distribute', {
      couponId: distributeForm.value.couponId,
      userIds: distributeForm.value.userIds
    })
    if (data && data.code === 1) {
      ElMessage.success('发放成功')
      distributeVisible.value = false
      await loadCoupons()
    } else {
      ElMessage.error(data?.msg || '发放失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
}

// 获取类型文本
const getTypeText = (type) => {
  const option = typeOptions.find(opt => opt.value === type)
  return option ? option.label : '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  return status === 1 ? 'success' : 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return status === 1 ? '有效' : '无效'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadCoupons()
})
</script>

<template>
  <div class="page">
    <div class="page-card toolbar">
      <div class="toolbar-left">
        <div class="title">优惠券管理</div>
        <div class="muted">创建和管理店铺优惠券</div>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="openCreateDialog">创建优惠券</el-button>
        <el-button type="info" plain @click="loadCoupons" :loading="loading">刷新</el-button>
      </div>
    </div>

    <div v-if="error" class="page-card">
      <el-alert type="error" :closable="false" :title="error" />
    </div>

    <el-skeleton v-if="loading" animated :rows="6" class="page-card" />

    <div v-else-if="coupons.length === 0" class="page-card">
      <el-empty description="暂无优惠券" />
    </div>

    <div v-else class="coupons-grid">
      <div v-for="(coupon, index) in coupons" :key="coupon.couponId" class="coupon-card">
        <div class="coupon-header">
          <div class="coupon-name">#{{ index + 1 }} {{ coupon.name }}</div>
          <el-tag :type="getStatusType(coupon.status)">{{ getStatusText(coupon.status) }}</el-tag>
        </div>
        
        <div class="coupon-body">
          <div class="coupon-type">{{ getTypeText(coupon.type) }}</div>
          <div class="coupon-value">
            <span v-if="coupon.type === 1">¥{{ coupon.value }}</span>
            <span v-else>{{ coupon.value }}折</span>
          </div>
          <div class="coupon-condition">满 ¥{{ coupon.minSpend }} 可用</div>
        </div>
        
        <div class="coupon-info">
          <div class="info-item">
            <span class="label">总量:</span>
            <span class="value">{{ coupon.totalCount || '不限' }}</span>
          </div>
          <div class="info-item">
            <span class="label">已领:</span>
            <span class="value">{{ coupon.receivedCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="label">剩余:</span>
            <span class="value">{{ coupon.totalCount ? coupon.totalCount - (coupon.receivedCount || 0) : '不限' }}</span>
          </div>
        </div>
        
        <div class="coupon-time">
          <div>开始: {{ formatDate(coupon.startTime) }}</div>
          <div>结束: {{ formatDate(coupon.endTime) }}</div>
        </div>
        
        <div class="coupon-actions">
          <el-button size="small" @click="openEditDialog(coupon)">编辑</el-button>
          <el-button size="small" type="primary" plain @click="openDistributeDialog(coupon)">发放</el-button>
          <el-button size="small" type="info" plain @click="openIssuanceDialog(coupon)">记录</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(coupon)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '创建优惠券'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="优惠券名称" required>
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        
        <el-form-item label="优惠券类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="面值" required>
          <el-input-number v-model="form.value" :min="0.01" :step="0.01" style="width: 100%" />
          <div class="form-tip">{{ form.type === 1 ? '满减金额（元）' : '折扣率（如8.5折填8.5）' }}</div>
        </el-form-item>
        
        <el-form-item label="最低消费" required>
          <el-input-number v-model="form.minSpend" :min="0" :step="0.01" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="发行总量">
          <el-input-number v-model="form.totalCount" :min="1" :step="1" style="width: 100%" />
          <div class="form-tip">留空表示不限量</div>
        </el-form-item>
        
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">{{ isEdit ? '保存' : '创建' }}</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="distributeVisible" title="定向发放优惠券" width="520px">
      <el-form :model="distributeForm" label-width="100px">
        <el-form-item label="优惠券ID">
          <el-input :model-value="distributeForm.couponId" disabled />
        </el-form-item>
        <el-form-item label="选择用户" required>
          <el-select
            v-model="distributeForm.userIds"
            multiple
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            placeholder="输入手机号/昵称搜索并选择用户"
            style="width: 100%"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="`${u.nickname || '用户'}（${u.phone || '无手机号'}）ID:${u.userId}`"
              :value="u.userId"
            />
          </el-select>
          <div class="form-tip">支持按手机号或昵称搜索；可多选。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="distributeVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDistribute">确认发放</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发放详情对话框 -->
    <el-dialog v-model="issuanceVisible" title="发放记录" width="700px">
      <el-table :data="issuanceList" stripe style="width: 100%" max-height="400">
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="createTime" label="领取时间">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="使用状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已使用' : '未使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usedTime" label="使用时间">
          <template #default="{ row }">
            {{ row.usedTime ? formatDate(row.usedTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="usedOrderId" label="关联订单" width="120">
          <template #default="{ row }">
            {{ row.usedOrderId || '-' }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="issuanceVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 20px; }
.page-card { 
  background: #fff; 
  border-radius: 12px; 
  padding: 20px; 
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.title { font-size: 18px; font-weight: 600; }
.muted { color: #6b7280; font-size: 14px; }

.coupons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.coupon-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  border: 1px solid #e5e7eb;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e5e7eb;
}

.coupon-name { font-weight: 600; font-size: 16px; }

.coupon-body {
  text-align: center;
  padding: 16px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  margin-bottom: 12px;
  color: #fff;
}

.coupon-type { font-size: 14px; opacity: 0.9; margin-bottom: 4px; }
.coupon-value { font-size: 28px; font-weight: 700; margin-bottom: 4px; }
.coupon-condition { font-size: 13px; opacity: 0.9; }

.coupon-info {
  display: flex;
  justify-content: space-around;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item { text-align: center; }
.info-item .label { color: #6b7280; font-size: 12px; display: block; }
.info-item .value { color: #333; font-weight: 600; font-size: 14px; }

.coupon-time {
  padding: 12px 0;
  font-size: 13px;
  color: #6b7280;
  border-bottom: 1px solid #f0f0f0;
}

.coupon-time div { margin-bottom: 4px; }

.coupon-actions {
  padding-top: 12px;
  text-align: right;
}

.form-tip {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.dialog-footer { text-align: right; }
</style>
