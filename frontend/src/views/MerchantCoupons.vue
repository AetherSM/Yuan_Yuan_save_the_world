<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const coupons = ref([])
const loading = ref(false)
const error = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)

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
  try {
    const { data } = await http.get('/api/coupons/list')
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
    const { data } = await http.post('/api/coupons/add', submitData)
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

onMounted(loadCoupons)
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
      <div v-for="coupon in coupons" :key="coupon.couponId" class="coupon-card">
        <div class="coupon-header">
          <div class="coupon-name">{{ coupon.name }}</div>
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