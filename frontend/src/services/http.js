import axios from 'axios'

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
  res => res,
  err => {
    if (err?.response?.status === 401) {
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

export default http

export const getUserDetails = async (userId) => {
  try {
    const { data } = await http.get(`/admin/users?userId=${userId}`);
    if (data.code === 1 && data.data && data.data.length > 0) {
      return data.data[0];
    }
  } catch (error) {
    console.error(`Failed to fetch user details for ID ${userId}:`, error);
  }
  return null;
};

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
