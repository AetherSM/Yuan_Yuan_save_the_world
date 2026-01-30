<template>
  <div>
    <h1>用户管理</h1>
    <div class="filters">
      <el-input v-model="filters.phone" placeholder="手机号" />
      <el-input v-model="filters.nickname" placeholder="昵称" />
      <el-select v-model="filters.userType" placeholder="角色">
        <el-option label="全部角色" :value="null" />
        <el-option label="管理员" :value="0" />
        <el-option label="普通用户" :value="1" />
        <el-option label="跑腿员" :value="2" />
        <el-option label="商家" :value="3" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态">
        <el-option label="全部状态" :value="null" />
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadUsers">搜索</el-button>
    </div>

    <el-table :data="users" style="width: 100%">
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="userType" label="角色">
        <template #default="{ row }">
          {{ userTypeMap[row.userType] || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ statusMap[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="openEditModal(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editModalVisible" title="编辑用户">
      <el-form label-width="80px">
        <el-form-item label="角色">
          <el-select v-model="currentUser.userType">
            <el-option label="管理员" :value="0" />
            <el-option label="普通用户" :value="1" />
            <el-option label="跑腿员" :value="2" />
            <el-option label="商家" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentUser.status">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editModalVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'

const users = ref([])
const filters = ref({
  phone: '',
  nickname: '',
  userType: null,
  status: null,
})

const userTypeMap = {
  0: '管理员',
  1: '普通用户',
  2: '跑腿员',
  3: '商家',
}

const statusMap = {
  1: '正常',
  0: '禁用',
}

const editModalVisible = ref(false)
const currentUser = ref({})

const loadUsers = async () => {
  try {
    const { data } = await http.get('/admin/users', { params: filters.value })
    if (data.code === 1) {
      users.value = data.data
    } else {
      ElMessage.error(data.msg || '加载用户失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  }
}

const openEditModal = (user) => {
  currentUser.value = { ...user }
  editModalVisible.value = true
}

const saveUser = async () => {
  try {
    await http.patch(`/admin/users/${currentUser.value.userId}/status`, { status: currentUser.value.status })
    await http.patch(`/admin/users/${currentUser.value.userId}/type`, { userType: currentUser.value.userType })
    ElMessage.success('保存成功')
    editModalVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>
