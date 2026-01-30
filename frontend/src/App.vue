<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from './services/http'
const route = useRoute()
const router = useRouter()

const userType = ref(Number(localStorage.getItem('userType') || 1))
const nickname = ref('')
const loggedIn = ref(false)

const tabs = computed(() => {
  if (userType.value === 2) { // 跑腿员
    return [
      { path: '/errands', label: '任务大厅' },
      { path: '/errands/runner', label: '我的任务' },
      { path: '/my', label: '我的' }
    ]
  }
  if (userType.value === 3) { // 商家
    return [
      { path: '/merchant/products', label: '商品管理' },
      { path: '/merchant/orders', label: '订单管理' },
      { path: '/my', label: '我的' }
    ]
  }
  // 普通用户 (默认为用户界面)
  return [
    { path: '/shop', label: '首页' },
    { path: '/cart', label: '购物车' },
    { path: '/errands', label: '跑腿' },
    { path: '/my', label: '我的' }
  ]
})

const isAdminRoute = computed(() => route.path.startsWith('/admin'))

const keyword = ref('')
const refreshAuth = async () => {
  const token = localStorage.getItem('token')
  loggedIn.value = !!token
  // 始终从本地同步角色与昵称，保证底部栏立即按角色渲染
  userType.value = Number(localStorage.getItem('userType') || 1)
  const n = localStorage.getItem('nickname') || ''
  nickname.value = n
  // 若缺少服务端信息，再尝试拉取
  if (token && !n) {
    try {
      const { data } = await http.get('/auth/profile')
      if (data && data.code === 1 && data.data) {
        nickname.value = String(data.data.nickname || '')
        localStorage.setItem('nickname', nickname.value)
        if (data.data.userType !== undefined) {
           userType.value = data.data.userType
           localStorage.setItem('userType', String(data.data.userType))
        }
      }
    } catch (e) {}
  }
}
onMounted(refreshAuth)
watch(() => route.path, refreshAuth)
const search = () => {
  router.push({ path: '/shop', query: { keyword: keyword.value || '' } })
}
const logout = async () => {
  try { await http.post('/auth/logout') } catch (e) {}
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('nickname')
  localStorage.removeItem('userType')
  loggedIn.value = false
  nickname.value = ''
  router.push('/login')
}
</script>

<template>
  <router-view v-if="isAdminRoute" />
  <div v-else class="mobile-layout">
    <header class="m-header">
      <div class="logo">校园购</div>
      <div class="user" v-if="loggedIn">
        <div class="avatar">{{ nickname?.[0] || 'U' }}</div>
      </div>
      <router-link v-else to="/login" class="login-link">登录</router-link>
    </header>
    <main class="content">
      <router-view />
    </main>
    <nav class="tabbar" v-if="route.path !== '/login'">
      <router-link v-for="t in tabs" :key="t.path" :to="t.path" class="tab" :class="{active: route.path===t.path}">
        <span>{{ t.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<style scoped>
.mobile-layout{display:flex;flex-direction:column;height:100vh}
.m-header{display:flex;align-items:center;gap:8px;padding:10px;border-bottom:1px solid #e5e7eb;background:#fff}
.logo{font-weight:700;color:#111827}
.search{display:none}
.user{display:flex;align-items:center;gap:8px}
.avatar{width:28px;height:28px;border-radius:50%;background:#42b883;color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700}
.login-link{border:1px solid #e5e7eb;border-radius:8px;padding:6px 10px}
.content{flex:1;overflow:auto;padding:12px;background:#f5f7fb}
.tabbar{display:flex;justify-content:space-around;align-items:center;border-top:1px solid #e5e7eb;background:#fff}
.tab{flex:1;text-align:center;padding:10px 0;color:#374151}
.tab.active{color:#ff1f2d;font-weight:600}
</style>
