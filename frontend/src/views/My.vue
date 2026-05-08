<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../services/http'
import ProductOrders from './ProductOrders.vue'
import MyRefunds from './MyRefunds.vue'
import MyOrders from './MyOrders.vue'
import ShoppingRecords from './ShoppingRecords.vue'
import Addresses from './Addresses.vue'
import Wallet from './Wallet.vue'
import MerchantRefunds from './MerchantRefunds.vue'
import RunnerRefunds from './RunnerRefunds.vue'
import RoleApply from './RoleApply.vue'
import MyComplaints from './MyComplaints.vue'
import MerchantCoupons from './MerchantCoupons.vue'

const route = useRoute()
const router = useRouter()
const activeTab = ref('orders')
const passwordDialogVisible = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 用户信息
const userInfo = ref({
  userId: null,
  nickname: '',
  username: '',
  email: '',
  phone: '',
  userType: 1,
  balance: 0,
  createTime: ''
})

// 计算用户角色文本
const userRoleText = computed(() => {
  const type = userInfo.value.userType
  if (type === 0) return '管理员'
  if (type === 2) return '跑腿员'
  if (type === 3) return '商家'
  return '普通用户'
})

// 判断是否为商家
const isMerchant = computed(() => userInfo.value.userType === 3)

// 判断是否为跑腿员
const isRunner = computed(() => userInfo.value.userType === 2)

// 判断是否为管理员
const isAdmin = computed(() => userInfo.value.userType === 0)

// 获取用户类型标签样式
const roleTagType = computed(() => {
  const type = userInfo.value.userType
  if (type === 0) return 'danger'
  if (type === 2) return 'warning'
  if (type === 3) return 'success'
  return 'info'
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const { data } = await http.get('/auth/profile')
    if (data && data.code === 1 && data.data) {
      userInfo.value = data.data
      // 更新本地存储
      localStorage.setItem('nickname', data.data.nickname || '')
      localStorage.setItem('userType', String(data.data.userType || 1))
      if (data.data.userType === 0) {
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('nickname')
        localStorage.removeItem('userType')
        router.replace('/login')
        return
      }
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

const syncFromQuery = () => {
  const t = route.query.tab
  // 商家默认显示钱包
  if (isMerchant.value) {
    activeTab.value = t === 'refunds' ? 'refunds' : t === 'roles' ? 'roles' : t === 'coupons' ? 'coupons' : 'wallet'
  } else if (isAdmin.value) {
    activeTab.value = ''
  } else if (isRunner.value) {
    activeTab.value =
      t === 'refunds' ? 'refunds'
      : t === 'complaints' ? 'complaints'
      : t === 'roles' ? 'roles'
      : 'wallet'
  } else {
    activeTab.value =
      t === 'wallet' ? 'wallet'
      : t === 'errands' ? 'errands'
      : t === 'records' ? 'records'
      : t === 'addresses' ? 'addresses'
      : t === 'complaints' ? 'complaints'
      : t === 'roles' ? 'roles'
      : t === 'refunds' ? 'refunds'
      : 'orders'
  }
}

onMounted(() => {
  loadUserInfo()
  syncFromQuery()
})

watch(() => route.query.tab, syncFromQuery)

const switchTab = (name) => {
  activeTab.value = name
  router.replace({ path: '/my', query: { tab: name } })
}

const logout = async () => {
  try { await http.post('/auth/logout') } catch (e) {}
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('nickname')
  localStorage.removeItem('userType')
  router.push('/login')
}

const openPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

const submitPasswordChange = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value
  if (!oldPassword || !newPassword || !confirmPassword) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }
  if (newPassword.length < 6 || newPassword.length > 20) {
    ElMessage.warning('新密码长度必须在6-20位之间')
    return
  }
  if (newPassword !== confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (oldPassword === newPassword) {
    ElMessage.warning('新密码不能与原密码相同')
    return
  }

  try {
    const { data } = await http.put('/auth/password', {
      oldPassword,
      newPassword
    })
    if (data && data.code === 1) {
      ElMessage.success('密码修改成功，请使用新密码重新登录')
      passwordDialogVisible.value = false
      await logout()
    } else {
      ElMessage.error(data?.msg || '密码修改失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '密码修改失败')
  }
}
</script>

<template>
  <div class="my">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-header">
        <div class="avatar">{{ userInfo.nickname?.[0] || userInfo.username?.[0] || 'U' }}</div>
        <div class="user-meta">
          <div class="user-name-row">
            <span class="nickname">{{ userInfo.nickname || userInfo.username || '用户' }}</span>
            <el-tag :type="roleTagType" size="small" effect="plain">{{ userRoleText }}</el-tag>
          </div>
          <div class="user-id">ID: {{ userInfo.userId }}</div>
        </div>
        <el-button plain @click="openPasswordDialog">修改密码</el-button>
      </div>
      
      <div class="user-details">
        <div class="detail-item" v-if="userInfo.phone">
          <span class="label">手机号:</span>
          <span class="value">{{ userInfo.phone }}</span>
        </div>
        <div class="detail-item" v-if="userInfo.email">
          <span class="label">邮箱:</span>
          <span class="value">{{ userInfo.email }}</span>
        </div>
        <div class="detail-item">
          <span class="label">账户余额:</span>
          <span class="value balance">¥{{ userInfo.balance || 0 }}</span>
        </div>
        <div class="detail-item" v-if="userInfo.createTime">
          <span class="label">注册时间:</span>
          <span class="value">{{ new Date(userInfo.createTime).toLocaleDateString() }}</span>
        </div>
      </div>
    </div>

    <!-- 功能标签页 -->
    <div class="tabs">
      <!-- 普通用户功能 -->
      <template v-if="!isMerchant && !isRunner && !isAdmin">
        <button class="tab" :class="{active: activeTab==='orders'}" @click="switchTab('orders')">订单</button>
        <button class="tab" :class="{active: activeTab==='refunds'}" @click="switchTab('refunds')">我的退款</button>
        <button class="tab" :class="{active: activeTab==='errands'}" @click="switchTab('errands')">跑腿订单</button>
        <button class="tab" :class="{active: activeTab==='records'}" @click="switchTab('records')">购物记录</button>
        <button class="tab" :class="{active: activeTab==='addresses'}" @click="switchTab('addresses')">地址管理</button>
        <button class="tab" :class="{active: activeTab==='complaints'}" @click="switchTab('complaints')">我的投诉</button>
        <button class="tab" :class="{active: activeTab==='wallet'}" @click="switchTab('wallet')">钱包充值</button>
        <button class="tab" :class="{active: activeTab==='roles'}" @click="switchTab('roles')">角色申请</button>
      </template>
      
      <!-- 商家功能 -->
      <template v-if="isMerchant">
        <button class="tab" :class="{active: activeTab==='wallet'}" @click="switchTab('wallet')">金额流水</button>
        <button class="tab" :class="{active: activeTab==='coupons'}" @click="switchTab('coupons')">营销工具</button>
        <button class="tab" :class="{active: activeTab==='refunds'}" @click="switchTab('refunds')">退款管理</button>
        <button class="tab" :class="{active: activeTab==='roles'}" @click="switchTab('roles')">角色申请</button>
      </template>
      
      <!-- 跑腿员功能 -->
      <template v-if="isRunner">
        <button class="tab" :class="{active: activeTab==='wallet'}" @click="switchTab('wallet')">金额流水</button>
        <button class="tab" :class="{active: activeTab==='refunds'}" @click="switchTab('refunds')">退款处理</button>
        <button class="tab" :class="{active: activeTab==='complaints'}" @click="switchTab('complaints')">我的投诉</button>
        <button class="tab" :class="{active: activeTab==='roles'}" @click="switchTab('roles')">角色申请</button>
      </template>
      
      <button class="tab danger" @click="logout">退出登录</button>
    </div>
    
    <div class="panel">
      <ProductOrders v-if="activeTab==='orders' && !isMerchant && !isRunner && !isAdmin" />
      <MyRefunds v-else-if="activeTab==='refunds' && !isMerchant && !isRunner && !isAdmin" />
      <MyOrders v-else-if="activeTab==='errands' && !isMerchant && !isRunner && !isAdmin" />
      <ShoppingRecords v-else-if="activeTab==='records' && !isMerchant && !isRunner && !isAdmin" />
      <Addresses v-else-if="activeTab==='addresses' && !isMerchant && !isRunner && !isAdmin" />
      <MyComplaints v-else-if="activeTab==='complaints' && !isMerchant && !isRunner && !isAdmin" />
      <Wallet v-else-if="activeTab==='wallet'" />
      <MerchantCoupons v-else-if="activeTab==='coupons' && isMerchant" />
      <MerchantRefunds v-else-if="activeTab==='refunds' && isMerchant" />
      <RunnerRefunds v-else-if="activeTab==='refunds' && isRunner" />
      <MyComplaints v-else-if="activeTab==='complaints' && isRunner" />
      <RoleApply v-else-if="activeTab==='roles'" />
      <div v-else class="empty-tip">
        <el-empty description="请选择功能" />
      </div>
    </div>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px">
      <el-form label-width="90px">
        <el-form-item label="原密码" required>
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入6-20位新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPasswordChange">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.my {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 用户信息卡片 */
.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  color: #fff;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-meta {
  flex: 1;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.nickname {
  font-size: 20px;
  font-weight: 600;
}

.user-id {
  font-size: 13px;
  opacity: 0.9;
}

.user-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.label {
  opacity: 0.8;
  min-width: 70px;
}

.value {
  font-weight: 500;
}

.balance {
  font-size: 18px;
  font-weight: 700;
  color: #ffd700;
}

/* 标签页 */
.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
}

.tab {
  flex: 1;
  min-width: 100px;
  padding: 12px 16px;
  border: none;
  border-radius: 8px;
  background: #f3f4f6;
  color: #374151;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.tab:hover {
  background: #e5e7eb;
}

.tab.active {
  background: #ff1f2d;
  color: #fff;
  font-weight: 600;
}

.tab.danger {
  background: #fee2e2;
  color: #b91c1c;
}

.tab.danger:hover {
  background: #fecaca;
}

.panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  min-height: 300px;
}

.empty-tip {
  padding: 40px;
  text-align: center;
  color: #6b7280;
}
</style>
