<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">管理后台</div>
      <nav class="menu">
        <router-link to="/admin" class="menu-item">概览</router-link>
        <router-link to="/admin/users" class="menu-item">用户管理</router-link>
        <router-link to="/admin/products" class="menu-item">商品管理</router-link>
        <router-link to="/admin/orders" class="menu-item">订单管理</router-link>
        <router-link to="/admin/refunds" class="menu-item">退款管理</router-link>
        <router-link to="/admin/errands" class="menu-item">跑腿任务管理</router-link>
        <router-link to="/admin/complaints" class="menu-item">投诉管理</router-link>
        <router-link to="/admin/role-applications" class="menu-item">角色申请管理</router-link>
        <!-- 其他菜单项 -->
      </nav>
    </aside>
    <main class="main-content">
      <header class="topbar">
        <div class="topbar-left">
          <div class="crumb">{{ pageTitle }}</div>
        </div>
        <div class="topbar-right">
          <div class="who">{{ nickname || '管理员' }}</div>
          <el-button size="small" type="danger" plain @click="logout">退出登录</el-button>
        </div>
      </header>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../services/http'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const nickname = computed(() => localStorage.getItem('nickname') || '')

const pageTitle = computed(() => {
  const p = route.path || ''
  if (p.startsWith('/admin/users')) return '用户管理'
  if (p.startsWith('/admin/products')) return '商品管理'
  if (p.startsWith('/admin/orders')) return '订单管理'
  if (p.startsWith('/admin/refunds')) return '退款管理'
  if (p.startsWith('/admin/errands')) return '跑腿任务管理'
  if (p.startsWith('/admin/complaints')) return '投诉管理'
  if (p.startsWith('/admin/role-applications')) return '角色申请管理'
  return '概览'
})

const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try { await http.post('/auth/logout') } catch (e) {}
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('nickname')
  localStorage.removeItem('userType')
  router.replace('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 200px;
  background-color: #333;
  color: #fff;
  padding: 20px;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
}
.menu-item {
  display: block;
  color: #fff;
  text-decoration: none;
  padding: 10px 0;
  margin-bottom: 10px;
}
.menu-item.router-link-active {
  background-color: #555;
}
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.topbar{
  position: sticky;
  top: 0;
  z-index: 10;
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:12px;
  padding:12px 14px;
  margin:-20px -20px 16px;
  background:#fff;
  border-bottom:1px solid #e5e7eb;
}
.crumb{font-size:14px;color:#111827;font-weight:600}
.topbar-right{display:flex;align-items:center;gap:10px}
.who{font-size:13px;color:#6b7280}
</style>
