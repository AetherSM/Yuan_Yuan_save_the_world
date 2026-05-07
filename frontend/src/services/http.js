import axios from 'axios'
import { ElMessageBox } from 'element-plus'

const http = axios.create({
  baseURL: '',
  timeout: 10000
})

http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['token'] = token
  }
  return config
})

http.interceptors.response.use(
  res => {
    // 处理后端返回的封禁错误码 (虽然拦截器返回了 403，但某些接口可能返回 code: 0)
    if (res.data && res.data.code === 0 && res.data.msg === '账号已被封禁，请联系管理员') {
      handleBannedUser()
    }
    return res
  },
  err => {
    if (err?.response?.status === 403 && err.response.data?.msg === '账号已被封禁，请联系管理员') {
      handleBannedUser()
    } else if (err?.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('nickname')
      localStorage.removeItem('userType')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

function handleBannedUser() {
  ElMessageBox.alert('账号已被封禁，请联系管理员', '账号状态异常', {
    confirmButtonText: '确定',
    type: 'error',
    callback: () => {
      localStorage.clear()
      window.location.href = '/login'
    }
  })
}

export default http

export const getUserDetails = async (userId) => {
  const id = userId == null || userId === '' ? null : Number(userId)
  if (id == null || Number.isNaN(id)) return null
  try {
    const { data } = await http.get('/admin/users', { params: { userId: id } })
    if (data.code === 1 && data.data && data.data.length > 0) {
      return data.data[0]
    }
  } catch (error) {
    console.error(`Failed to fetch user details for ID ${userId}:`, error)
  }
  return null
}

export const searchUserIds = async (query) => {
  try {
    const params = {};
    if (query.match(/^\d+$/)) { // 假设是手机号
      params.phone = query;
    } else { // 假设是昵称
      params.nickname = query;
    }
    const { data } = await http.get('/admin/users', { params });
    if (data.code === 1 && data.data) {
      return data.data.map(user => user.userId);
    }
  } catch (error) {
    console.error(`Failed to search users for query "${query}":`, error);
  }
  return [];
};
