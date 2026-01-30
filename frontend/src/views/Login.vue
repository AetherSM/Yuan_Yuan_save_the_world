<script setup>
import { ref } from 'vue'
import http from '../services/http'
import { useRouter } from 'vue-router'
const router = useRouter()
const phone = ref('')
const password = ref('')
const loading = ref(false)
const message = ref('')
const submit = async () => {
  loading.value = true
  message.value = ''
  try {
    const { data } = await http.post('/auth/login', { phone: phone.value, password: password.value })
    if (data && data.code === 1 && data.data && data.data.token) {
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('userId', String(data.data.user.userId))
      localStorage.setItem('nickname', String(data.data.user.nickname || ''))
      if (data.data.user.userType !== undefined && data.data.user.userType !== null) {
        localStorage.setItem('userType', String(data.data.user.userType))
      }
      
      const type = data.data.user.userType
      if (type === 0) {
        router.push('/admin')
      } else if (type === 2) {
        router.push('/errands')
      } else if (type === 3) {
        router.push('/merchant/orders')
      } else {
        router.push('/shop')
      }
    } else {
      message.value = data && data.msg ? data.msg : '登录失败'
    }
  } catch (e) {
    message.value = '请求失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="card">
    <h2>登录</h2>
    <div class="field">
      <label>手机号</label>
      <el-input v-model="phone" placeholder="请输入手机号" />
    </div>
    <div class="field">
      <label>密码</label>
      <el-input v-model="password" type="password" placeholder="请输入密码" show-password />
    </div>
    <el-button type="primary" class="btn" :loading="loading" @click="submit">登录</el-button>
    <div class="msg" v-if="message">{{ message }}</div>
  </div>
  <div class="tip">测试账号可用数据库中的手机号，密码为123456</div>
  <div class="shortcut">
    <button class="link" @click="phone='13800000003';password='123456'">张三(用户)</button>
    <button class="link" @click="phone='13800000001';password='123456'">超市(商家)</button>
    <button class="link" @click="phone='13800000002';password='123456'">飞毛腿(跑腿)</button>
    <button class="link" @click="phone='13800000000';password='123456'">管理员</button>
  </div>
  </template>

<style scoped>
.card{width:100%;margin:0;padding:24px;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.field{display:flex;flex-direction:column;gap:6px;margin-bottom:16px}
.btn{width:100%;margin-top:6px}
.msg{margin-top:12px;color:#d33}
.tip{text-align:left;margin-top:12px;color:#666}
.shortcut{display:flex;gap:8px;margin-top:8px}
.link{padding:6px 10px;border:1px solid #ddd;border-radius:6px;background:#f7f7f7;cursor:pointer}
</style>
