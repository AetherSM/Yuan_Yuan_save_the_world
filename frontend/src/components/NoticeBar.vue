<template>
  <div v-if="notices.length > 0" class="notice-bar">
    <div class="notice-header">
      <el-icon><Notification /></el-icon>
      <span class="notice-title">最新公告</span>
    </div>
    <div class="notice-list">
      <div v-for="notice in notices" :key="notice.noticeId" class="notice-item" @click="showDetail(notice)">
        <span class="notice-dot"></span>
        <span class="notice-text">{{ notice.title }}</span>
        <span class="notice-time">{{ formatDate(notice.createTime) }}</span>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="currentNotice?.title" width="500px">
      <div class="notice-content">{{ currentNotice?.content }}</div>
      <div class="notice-footer">发布时间: {{ currentNotice?.createTime }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../services/http'
import { Notification } from '@element-plus/icons-vue'

const notices = ref([])
const dialogVisible = ref(false)
const currentNotice = ref(null)

onMounted(() => {
  fetchNotices()
})

const fetchNotices = async () => {
  const userType = localStorage.getItem('userType') || 1
  try {
    const { data } = await http.get('/api/notices/visible', { params: { userType } })
    if (data.code === 1) {
      notices.value = data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const showDetail = (notice) => {
  currentNotice.value = notice
  dialogVisible.value = true
}

const formatDate = (time) => {
  if (!time) return ''
  return time.split(' ')[0].substring(5) // MM-DD
}
</script>

<style scoped>
.notice-bar {
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.notice-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #409eff;
  font-weight: bold;
  font-size: 14px;
}
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 0;
  transition: color 0.2s;
}
.notice-item:hover {
  color: #409eff;
}
.notice-dot {
  width: 6px;
  height: 6px;
  background: #ff4d4f;
  border-radius: 50%;
}
.notice-text {
  flex: 1;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.notice-time {
  font-size: 12px;
  color: #999;
}
.notice-content {
  line-height: 1.6;
  white-space: pre-wrap;
  color: #444;
}
.notice-footer {
  margin-top: 20px;
  text-align: right;
  font-size: 12px;
  color: #999;
}
</style>
