<script setup>
import { ref } from 'vue'
import http from '../services/http'
import { useRouter } from 'vue-router'
import { User, Shop, Van, SetUp } from '@element-plus/icons-vue'

const router = useRouter()

// tabs: login / register
const activeTab = ref('login')

// 登录表单
const loginPhone = ref('')
const loginPassword = ref('')

// 注册表单
const regPhone = ref('')
const regPassword = ref('')
const regPassword2 = ref('')
const regNickname = ref('')

const loading = ref(false)
const message = ref('')

const doLogin = async () => {
  loading.value = true
  message.value = ''
  try {
    const { data } = await http.post('/auth/login', { phone: loginPhone.value, password: loginPassword.value })
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
    message.value = e?.response?.data?.msg || '请求失败'
  } finally {
    loading.value = false
  }
}

const doRegister = async () => {
  if (!regPhone.value || !regPassword.value || !regNickname.value) {
    message.value = '手机号、密码、昵称不能为空'
    return
  }
  if (regPassword.value !== regPassword2.value) {
    message.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  message.value = ''
  try {
    const payload = {
      phone: regPhone.value,
      password: regPassword.value,
      nickname: regNickname.value
    }
    const { data } = await http.post('/auth/register', payload)
    if (data && data.code === 1) {
      message.value = '注册成功，请使用手机号和密码登录'
      // 将注册信息填到登录表单，方便直接登录
      loginPhone.value = regPhone.value
      loginPassword.value = regPassword.value
      activeTab.value = 'login'
    } else {
      message.value = data && data.msg ? data.msg : '注册失败'
    }
  } catch (e) {
    message.value = e?.response?.data?.msg || '注册失败'
  } finally {
    loading.value = false
  }
}

// 快捷测试账号
const fillTest = (phone, pwd) => {
  activeTab.value = 'login'
  loginPhone.value = phone
  loginPassword.value = pwd
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2 class="login-title">校园购</h2>
        <p class="login-subtitle">欢迎回来，开始您的校园购物之旅</p>
      </div>
      
      <div class="tabs">
        <button 
          class="tab" 
          :class="{ active: activeTab === 'login' }" 
          @click="activeTab = 'login'"
        >
          登录
        </button>
        <button 
          class="tab" 
          :class="{ active: activeTab === 'register' }" 
          @click="activeTab = 'register'"
        >
          注册
        </button>
      </div>

      <div v-if="activeTab === 'login'" class="panel">
        <div class="field">
          <label class="field-label">手机号</label>
          <el-input 
            v-model="loginPhone" 
            placeholder="请输入手机号" 
            class="custom-input"
            prefix-icon="el-icon-mobile"
          />
        </div>
        <div class="field">
          <label class="field-label">密码</label>
          <el-input 
            v-model="loginPassword" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
            class="custom-input"
            prefix-icon="el-icon-lock"
          />
        </div>
        <el-button 
          type="primary" 
          class="login-btn" 
          :loading="loading" 
          @click="doLogin"
        >
          登录
        </el-button>
      </div>

      <div v-else class="panel">
        <div class="field">
          <label class="field-label">手机号</label>
          <el-input 
            v-model="regPhone" 
            placeholder="请输入手机号" 
            class="custom-input"
            prefix-icon="el-icon-mobile"
          />
        </div>
        <div class="field">
          <label class="field-label">昵称</label>
          <el-input 
            v-model="regNickname" 
            placeholder="请输入昵称" 
            class="custom-input"
            prefix-icon="el-icon-user"
          />
        </div>
        <div class="field">
          <label class="field-label">密码</label>
          <el-input 
            v-model="regPassword" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
            class="custom-input"
            prefix-icon="el-icon-lock"
          />
        </div>
        <div class="field">
          <label class="field-label">确认密码</label>
          <el-input 
            v-model="regPassword2" 
            type="password" 
            placeholder="请再次输入密码" 
            show-password 
            class="custom-input"
            prefix-icon="el-icon-lock"
          />
        </div>
        <el-button 
          type="primary" 
          class="login-btn" 
          :loading="loading" 
          @click="doRegister"
        >
          注册
        </el-button>
      </div>

      <div class="message" v-if="message" :class="{ error: message.includes('失败') }">
        {{ message }}
      </div>
    </div>

    <div class="login-footer">
      <div class="tip">测试账号可用数据库中的手机号，密码为 123456</div>
      <div class="shortcut">
        <button class="link-btn" @click="fillTest('13800000003','123456')">
          <el-icon class="icon"><User /></el-icon>
          张三(用户)
        </button>
        <button class="link-btn" @click="fillTest('13800000001','123456')">
          <el-icon class="icon"><Shop /></el-icon>
          超市(商家)
        </button>
        <button class="link-btn" @click="fillTest('13800000002','123456')">
          <el-icon class="icon"><Van /></el-icon>
          飞毛腿(跑腿)
        </button>
        <button class="link-btn" @click="fillTest('13800000000','123456')">
          <el-icon class="icon"><SetUp /></el-icon>
          管理员
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  margin: 0 auto;
  padding: 32px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 4px;
}

.tab {
  flex: 1;
  border: none;
  background: transparent;
  border-radius: 10px;
  padding: 12px 0;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  color: #666;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.tab::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.tab:hover::before {
  left: 100%;
}

.tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.panel {
  margin-top: 8px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.field-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.custom-input {
  border-radius: 10px !important;
  border: 1px solid #e5e7eb !important;
  transition: all 0.3s ease !important;
}

.custom-input:focus {
  border-color: #667eea !important;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
}

.login-btn {
  width: 100%;
  margin-top: 12px;
  padding: 14px 0 !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
  transition: all 0.3s ease !important;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4) !important;
}

.login-btn:active {
  transform: translateY(0);
}

.message {
  margin-top: 16px;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  background: #f0f9ff;
  color: #3b82f6;
  border: 1px solid #dbeafe;
}

.message.error {
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fee2e2;
}

.login-footer {
  margin-top: 24px;
  text-align: center;
  width: 100%;
  max-width: 420px;
}

.tip {
  text-align: center;
  margin-bottom: 16px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.shortcut {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.link-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.link-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.icon {
  font-size: 14px;
}

@media (max-width: 480px) {
  .login-card {
    padding: 24px;
    margin: 0 16px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .shortcut {
    gap: 8px;
  }
  
  .link-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
