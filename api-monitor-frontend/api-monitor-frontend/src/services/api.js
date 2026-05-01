import axios from 'axios'

const BASE_URL = '/api/v1'


const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})


api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)


api.interceptors.response.use(
  (response) => response,
  (error) => {
  const isAuthEndpoint = error.config?.url?.includes('/auth/')
  
  if (error.response?.status === 401 && !isAuthEndpoint) {
    
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user')
    window.location.href = '/login'
  }
  return Promise.reject(error)
},
)

export default api