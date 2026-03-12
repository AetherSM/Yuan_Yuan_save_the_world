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
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default http
