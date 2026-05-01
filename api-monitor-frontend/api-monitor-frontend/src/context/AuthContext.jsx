import { createContext, useContext, useState, useCallback } from 'react'
import authService from '../services/authService'

const AuthContext = createContext(null)


export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => authService.getCurrentUser())
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const login = useCallback(async (email, password) => {
    setLoading(true)
    setError(null)
    try {
      const data = await authService.login(email, password)
      setUser({
        id:       data.userId,
        username: data.username,
        email:    data.email,
        role:     data.role,
      })
      return true
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.message ||
        'Login failed. Please try again.'
      setError(message)
      return false
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    authService.logout()
    setUser(null)
    setError(null)
  }, [])

  const clearError = useCallback(() => setError(null), [])

  const isAuthenticated = !!user


  return (
    <AuthContext.Provider value={{ user, isAuthenticated, loading, error, login, logout, clearError }}>
      {children}
    </AuthContext.Provider>
  )
}


export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}