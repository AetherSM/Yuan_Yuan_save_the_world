<template>
  <div class="admin-notices">
    <div class="header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="openDialog()">发布公告</el-button>
    </div>

    <el-table :data="notices" style="width: 100%" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="visibility" label="可见范围" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.visibility === 0">所有人</el-tag>
          <el-tag v-else-if="row.visibility === 1" type="success">按类型</el-tag>
          <el-tag v-else type="warning">按用户ID</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.noticeId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 发布/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.noticeId ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" rows="5" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="可见范围">
          <el-radio-group v-model="form.visibility">
            <el-radio :label="0">所有人</el-radio>
            <el-radio :label="1">按用户类型</el-radio>
            <el-radio :label="2">按指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="form.visibility === 1" label="用户类型">
          <el-checkbox-group v-model="selectedTypes">
            <el-checkbox label="1">普通用户</el-checkbox>
            <el-checkbox label="2">跑腿员</el-checkbox>
            <el-checkbox label="3">商家</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item v-if="form.visibility === 2" label="用户ID">
          <el-input v-model="form.targetUsers" placeholder='请输入用户ID数组,如 [1, 2]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import http from '../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const notices = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)

const form = ref({
  noticeId: null,
  title: '',
  content: '',
  visibility: 0,
  targetTypes: '',
  targetUsers: ''
})

const selectedTypes = ref([])

onMounted(() => {
  fetchNotices()
})

const fetchNotices = async () => {
  loading.value = true
  try {
    const { data } = await http.get('/admin/notices/all')
    if (data.code === 1) {
      notices.value = data.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  if (row) {
    form.value = { ...row }
    selectedTypes.value = row.targetTypes ? row.targetTypes.split(',') : []
  } else {
    form.value = {
      noticeId: null,
      title: '',
      content: '',
      visibility: 0,
      targetTypes: '',
      targetUsers: ''
    }
    selectedTypes.value = []
  }
  dialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此公告吗?', '警告', { type: 'warning' }).then(async () => {
    try {
      const { data } = await http.delete(`/admin/notices/${id}`)
      if (data.code === 1) {
        ElMessage.success('删除成功')
        fetchNotices()
      }
    } catch (e) {
      console.error(e)
    }
  })
}

const submitForm = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('标题和内容不能为空')
    return
  }

  if (form.value.visibility === 1) {
    form.value.targetTypes = selectedTypes.value.join(',')
  }

  submitting.value = true
  try {
    const url = form.value.noticeId ? '/admin/notices/update' : '/admin/notices/publish'
    const method = form.value.noticeId ? 'put' : 'post'
    const { data } = await http[method](url, form.value)
    if (data.code === 1) {
      ElMessage.success(form.value.noticeId ? '更新成功' : '发布成功')
      dialogVisible.value = false
      fetchNotices()
    } else {
      ElMessage.error(data.msg || '操作失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('服务器错误')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
