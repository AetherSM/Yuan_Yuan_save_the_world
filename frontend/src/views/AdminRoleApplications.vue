<template>
  <div>
    <h1>角色申请管理</h1>
    <div class="filters">
      <el-input v-model="filters.userId" placeholder="用户ID" style="width: 140px" />
      <el-select v-model="filters.targetType" placeholder="目标角色" style="width: 160px">
        <el-option :value="null" label="全部角色" />
        <el-option v-for="(label, key) in roleMap" :key="key" :label="label" :value="Number(key)" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" style="width: 140px">
        <el-option :value="null" label="全部状态" />
        <el-option :value="0" label="待审核" />
        <el-option :value="1" label="已通过" />
        <el-option :value="2" label="已拒绝" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
    </div>

    <el-table :data="list" style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="currentType" label="当前角色" width="110">
        <template #default="{ row }">
          {{ roleMap[row.currentType] || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="targetType" label="目标角色" width="110">
        <template #default="{ row }">
          {{ roleMap[row.targetType] || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="申请理由" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleRemark" label="审核备注" />
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            size="small"
            type="success"
            @click="approve(row)"
          >通过</el-button>
          <el-button
            v-if="row.status === 0"
            size="small"
            type="danger"
            @click="reject(row)"
          >拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const roleMap = {
  0: '管理员',
  1: '普通用户',
  2: '跑腿员',
  3: '商家',
}

const filters = ref({
  userId: '',
  targetType: null,
  status: 0,
})

const list = ref([])

const statusText = (s) => {
  if (s === 0) return '待审核'
  if (s === 1) return '已通过'
  if (s === 2) return '已拒绝'
  return '未知'
}

const statusTagType = (s) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  return ''
}

const load = async () => {
  try {
    const params = {
      userId: filters.value.userId ? Number(filters.value.userId) : null,
      targetType: filters.value.targetType ?? null,
      status: filters.value.status ?? null,
    }
    const { data } = await http.get('/admin/role-applications', { params })
    if (data.code === 1) {
      list.value = data.data || []
    } else {
      ElMessage.error(data.msg || '加载申请失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '加载申请失败')
  }
}

const approve = async (row) => {
  let remark = ''
  try {
    const res = await ElMessageBox.prompt('请输入审批备注（可选）', `通过申请 #${row.id}`, {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
    })
    remark = res?.value || ''
  } catch (e) {
    return
  }
  try {
    await http.post(`/admin/role-applications/${row.id}/approve`, { remark })
    ElMessage.success('已通过')
    await load()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '操作失败')
  }
}

const reject = async (row) => {
  let remark = ''
  try {
    const res = await ElMessageBox.prompt('请输入拒绝原因（可选）', `拒绝申请 #${row.id}`, {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
    })
    remark = res?.value || ''
  } catch (e) {
    return
  }
  try {
    await http.post(`/admin/role-applications/${row.id}/reject`, { remark })
    ElMessage.success('已拒绝')
    await load()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.filters{
  display:flex;
  flex-wrap:wrap;
  gap:10px;
  margin-bottom:12px;
}
</style>

