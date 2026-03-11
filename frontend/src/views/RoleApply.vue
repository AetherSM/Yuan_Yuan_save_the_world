<template>
  <div>
    <h2>角色申请</h2>
    <el-form label-width="80px" class="form">
      <el-form-item label="当前角色">
        <span>{{ currentRoleText }}</span>
      </el-form-item>
      <el-form-item label="目标角色">
        <el-select v-model="targetType" placeholder="请选择目标角色">
          <el-option v-for="(label, key) in roleMap" :key="key" :label="label" :value="Number(key)" />
        </el-select>
      </el-form-item>
      <el-form-item label="申请理由">
        <el-input
          v-model="reason"
          type="textarea"
          rows="3"
          maxlength="200"
          show-word-limit
          placeholder="请说明你为什么需要该角色，例如：我是校内商家 / 具备跑腿经验等"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit" :loading="submitting">提交申请</el-button>
      </el-form-item>
    </el-form>

    <h3 style="margin-top:16px;">我的申请记录</h3>
    <el-table :data="list" size="small" style="width:100%;margin-top:8px;">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="currentType" label="当前角色">
        <template #default="{ row }">
          {{ roleMap[row.currentType] || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="targetType" label="目标角色">
        <template #default="{ row }">
          {{ roleMap[row.targetType] || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleRemark" label="审核备注" />
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const roleMap = {
  0: '管理员',
  1: '普通用户',
  2: '跑腿员',
  3: '商家',
}

const currentType = ref(Number(localStorage.getItem('userType') || 1))
const currentRoleText = ref(roleMap[currentType.value] || '普通用户')

const targetType = ref(null)
const reason = ref('')
const submitting = ref(false)
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

const loadList = async () => {
  try {
    const { data } = await http.get('/role-applications/mine')
    if (data.code === 1) {
      list.value = data.data || []
    } else {
      ElMessage.error(data.msg || '加载申请记录失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '加载申请记录失败')
  }
}

const submit = async () => {
  if (targetType.value == null) {
    ElMessage.warning('请选择目标角色')
    return
  }
  if (!reason.value.trim()) {
    ElMessage.warning('请填写申请理由')
    return
  }
  submitting.value = true
  try {
    const { data } = await http.post('/role-applications', {
      targetType: targetType.value,
      reason: reason.value.trim(),
    })
    if (data.code === 1) {
      ElMessage.success('申请已提交，请等待管理员审核')
      reason.value = ''
      await loadList()
    } else {
      ElMessage.error(data.msg || '提交失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.form{
  max-width:520px;
  margin-bottom:12px;
}
</style>

