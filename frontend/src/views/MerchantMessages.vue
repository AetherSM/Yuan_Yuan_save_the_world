<template>
  <div class="merchant-messages">
    <div class="sidebar">
      <div class="header">消息中心</div>
      <div class="session-list">
        <div 
          v-for="s in sessions" 
          :key="s.userId" 
          class="session-item" 
          :class="{ active: currentUserId === s.userId }"
          @click="selectSession(s)"
        >
          <img :src="s.avatar || '/default-avatar.png'" class="avatar">
          <div class="info">
            <div class="name-time">
              <span class="name">{{ s.nickname || s.username }}</span>
              <span class="time">{{ formatTime(s.lastTime) }}</span>
            </div>
            <div class="last-msg">
              <span class="text">{{ s.lastMsg }}</span>
              <span v-if="s.unreadCount > 0" class="unread">{{ s.unreadCount }}</span>
            </div>
          </div>
        </div>
        <div v-if="sessions.length === 0" class="empty">暂无消息</div>
      </div>
    </div>

    <div class="main">
      <template v-if="currentUserId">
        <div class="chat-header">{{ currentUsername }}</div>
        <el-scrollbar ref="scrollbarRef" class="chat-body">
          <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="{ mine: m.senderId === merchantId }">
            <div class="msg-content">{{ m.content }}</div>
            <div class="msg-time">{{ formatTime(m.createTime) }}</div>
          </div>
        </el-scrollbar>
        <div class="chat-footer">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息内容..."
            @keyup.enter.ctrl="send"
          />
          <div class="ops">
            <span class="tip">Ctrl + Enter 发送</span>
            <el-button type="primary" @click="send" :disabled="!newMessage.trim()">发送</el-button>
          </div>
        </div>
      </template>
      <div v-else class="welcome">
        <el-icon :size="64" color="#d1d5db"><ChatDotRound /></el-icon>
        <p>请选择左侧会话开始沟通</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import http from '../services/http'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'

const merchantId = Number(localStorage.getItem('userId'))
const sessions = ref([])
const messages = ref([])
const currentUserId = ref(null)
const currentUsername = ref('')
const newMessage = ref('')
const scrollbarRef = ref(null)
let socket = null

const initWebSocket = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  socket = new WebSocket(`${protocol}//localhost:8080/ws/${merchantId}`)
  
  socket.onmessage = (event) => {
    if (event.data === 'NEW_MSG') {
      // 收到推送，立刻刷新列表和当前会话
      loadSessions()
      if (currentUserId.value) loadMessages()
    }
  }

  socket.onclose = () => {
    console.log('WebSocket closed, retrying...')
    setTimeout(initWebSocket, 3000)
  }
}

const loadSessions = async () => {
  try {
    const { data } = await http.get('/common/chat/sessions', { params: { merchantId } })
    if (data?.code === 1) {
      sessions.value = data.data || []
    }
  } catch (e) {}
}

const selectSession = async (s) => {
  currentUserId.value = s.userId
  currentUsername.value = s.nickname || s.username
  await loadMessages()
  await markRead()
  s.unreadCount = 0
  scrollToBottom()
}

const loadMessages = async () => {
  if (!currentUserId.value) return
  try {
    const { data } = await http.get('/common/chat/history', { 
      params: { userId1: merchantId, userId2: currentUserId.value } 
    })
    if (data?.code === 1) {
      messages.value = data.data || []
    }
  } catch (e) {}
}

const markRead = async () => {
  try {
    await http.post('/common/chat/read', null, { 
      params: { senderId: currentUserId.value, receiverId: merchantId } 
    })
  } catch (e) {}
}

const send = async () => {
  if (!newMessage.value.trim()) return
  try {
    const { data } = await http.post('/common/chat/send', null, {
      params: {
        senderId: merchantId,
        receiverId: currentUserId.value,
        content: newMessage.value.trim()
      }
    })
    if (data?.code === 1) {
      messages.value.push({
        senderId: merchantId,
        content: newMessage.value.trim(),
        createTime: new Date().toISOString()
      })
      newMessage.value = ''
      scrollToBottom()
      loadSessions() // 刷新列表最后一条消息
    }
  } catch (e) {
    ElMessage.error('发送失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      scrollbarRef.value.setScrollTop(99999)
    }
  })
}

const formatTime = (t) => {
  if (!t) return ''
  const date = new Date(t)
  return date.toLocaleString('zh-CN', { hour12: false, month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadSessions()
  initWebSocket()
})

onBeforeUnmount(() => {
  if (socket) socket.close()
})
</script>

<style scoped>
.merchant-messages{display:flex;height:calc(100vh - 120px);background:#fff;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden}
.sidebar{width:300px;border-right:1px solid #e5e7eb;display:flex;flex-direction:column}
.sidebar .header{padding:16px;font-weight:700;border-bottom:1px solid #f3f4f6;font-size:18px}
.session-list{flex:1;overflow-y:auto}
.session-item{display:flex;gap:12px;padding:12px;cursor:pointer;transition:all 0.2s}
.session-item:hover{background:#f9fafb}
.session-item.active{background:#f0f9ff}
.avatar{width:44px;height:44px;border-radius:50%;object-fit:cover}
.info{flex:1;min-width:0}
.name-time{display:flex;justify-content:space-between;align-items:center;margin-bottom:4px}
.name{font-weight:600;color:#111827}
.time{font-size:12px;color:#9ca3af}
.last-msg{display:flex;justify-content:space-between;align-items:center}
.text{font-size:13px;color:#6b7280;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.unread{background:#ef4444;color:#fff;font-size:11px;padding:1px 6px;border-radius:10px}
.empty{padding:40px;text-align:center;color:#9ca3af}

.main{flex:1;display:flex;flex-direction:column;background:#f9fafb}
.chat-header{padding:16px;background:#fff;border-bottom:1px solid #e5e7eb;font-weight:600}
.chat-body{flex:1;padding:16px}
.msg-row{display:flex;flex-direction:column;align-items:flex-start;margin-bottom:16px}
.msg-row.mine{align-items:flex-end}
.msg-content{max-width:70%;padding:10px 14px;background:#fff;border:1px solid #e5e7eb;border-radius:12px;font-size:14px;color:#1f2937}
.mine .msg-content{background:#3b82f6;color:#fff;border-color:#3b82f6}
.msg-time{font-size:11px;color:#9ca3af;margin-top:4px}
.chat-footer{padding:16px;background:#fff;border-top:1px solid #e5e7eb}
.ops{display:flex;justify-content:space-between;align-items:center;margin-top:8px}
.tip{font-size:12px;color:#9ca3af}
.welcome{flex:1;display:flex;flex-direction:column;justify-content:center;align-items:center;color:#9ca3af}
</style>
