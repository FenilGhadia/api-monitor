import api from './api'



const authService = {
  
async login(email, password) {
  const response = await api.post('/auth/login', { email, password })
  const envelope = response.data  // { success, message, data }

 
  if (!envelope.success || !envelope.data) {
    throw new Error(envelope.message || 'Login failed')
  }

  const data = envelope.data

  
  if (!data.access_token || !data.refresh_token) {
    throw new Error('Invalid token response from server')
  }

  localStorage.setItem('access_token', data.access_token)
  localStorage.setItem('refresh_token', data.refresh_token)
  localStorage.setItem('user', JSON.stringify({
    id:       data.userId,
    username: data.username,
    email:    data.email,
    role:     data.role,
  }))

  return data
},

  
  logout() {
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user')
  },

  
  getCurrentUser() {
    try {
      const stored = localStorage.getItem('user')
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  },

  
  isAuthenticated() {
    return !!localStorage.getItem('access_token')
  },

  
async refresh() {
  const refreshToken = localStorage.getItem('refresh_token')
  if (!refreshToken) throw new Error('No refresh token available')

  const response = await api.post(
    '/auth/refresh',
    {},
    { headers: { Authorization: `Bearer ${refreshToken}` } }
  )
  
  const envelope = response.data
  if (!envelope.success || !envelope.data) {
    throw new Error(envelope.message || 'Token refresh failed')
  }

  const data = envelope.data
  localStorage.setItem('access_token', data.access_token)

  
  if (data.userId) {
    localStorage.setItem('user', JSON.stringify({
      id:       data.userId,
      username: data.username,
      email:    data.email,
      role:     data.role,
    }))
  }

  return data
},
}

export default authService