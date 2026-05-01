import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const { login, loading, error, clearError, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const [localErr, setLocalErr] = useState('')

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) navigate('/dashboard', { replace: true })
  }, [isAuthenticated, navigate])

  // Sync auth context error to local state
  useEffect(() => {
    if (error) setLocalErr(error)
  }, [error])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLocalErr('')
    clearError()

    if (!email || !password) {
      setLocalErr('Email and password are required')
      return
    }
    const ok = await login(email, password)
    if (ok) navigate('/dashboard', { replace: true })
  }

  return (
    <div style={styles.page}>
      {/* Background glow */}
      <div style={styles.bgGlow} />

      <div style={styles.card}>
        {/* Header */}
        <div style={styles.header}>
          <div style={styles.logoRow}>
            <div style={styles.logoMark}>
              <span style={styles.logoDot} />
            </div>
            <span style={styles.logoText}>API Monitor</span>
          </div>
          <div style={styles.liveIndicator}>
            <span style={styles.liveDot} />
            <span style={styles.liveLabel}>SYSTEM ONLINE</span>
          </div>
        </div>

        <h1 style={styles.title}>Control Panel Access</h1>
        <p style={styles.subtitle}>Enter your credentials to access the monitoring dashboard</p>

        {/* Error banner */}
        {localErr && (
          <div style={styles.errorBanner}>
            <span>⚠</span> {localErr}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.field}>
            <label style={styles.label} htmlFor="email">EMAIL ADDRESS</label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => { setEmail(e.target.value); setLocalErr('') }}
              placeholder="admin@company.com"
              style={styles.input}
              disabled={loading}
            />
          </div>

          <div style={styles.field}>
            <label style={styles.label} htmlFor="password">PASSWORD</label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => { setPassword(e.target.value); setLocalErr('') }}
              placeholder="••••••••"
              style={styles.input}
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            style={{ ...styles.submitBtn, opacity: loading ? 0.6 : 1 }}
            disabled={loading}
          >
            {loading ? (
              <span style={styles.btnContent}>
                <span style={styles.btnSpinner} />
                Authenticating...
              </span>
            ) : (
              <span style={styles.btnContent}>
                Access Dashboard →
              </span>
            )}
          </button>
        </form>

        {/* Footer */}
        <div style={styles.footer}>
          <span style={styles.footerText}>
            Default admin: admin@test.com
          </span>
        </div>
      </div>
    </div>
  )
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'var(--bg-base)',
    padding: '24px',
    position: 'relative',
    overflow: 'hidden',
  },
  bgGlow: {
    position: 'absolute',
    top: '20%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 600,
    height: 600,
    borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(0,232,122,0.06) 0%, transparent 70%)',
    pointerEvents: 'none',
  },
  card: {
    background: 'var(--bg-surface)',
    border: '1px solid var(--border)',
    borderRadius: 16,
    padding: '40px',
    width: '100%',
    maxWidth: 420,
    position: 'relative',
    animation: 'fadeIn 0.4s ease forwards',
    boxShadow: '0 24px 64px rgba(0,0,0,0.6)',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 28,
  },
  logoRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
  },
  logoMark: {
    width: 28,
    height: 28,
    borderRadius: 7,
    background: 'var(--green-dim)',
    border: '1px solid rgba(0,232,122,0.3)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoDot: {
    display: 'block',
    width: 8,
    height: 8,
    borderRadius: '50%',
    background: 'var(--green)',
    boxShadow: '0 0 8px var(--green)',
  },
  logoText: {
    fontFamily: 'var(--font-display)',
    fontSize: 13,
    fontWeight: 600,
    color: 'var(--text-primary)',
    letterSpacing: '0.02em',
  },
  liveIndicator: {
    display: 'flex',
    alignItems: 'center',
    gap: 5,
  },
  liveDot: {
    display: 'block',
    width: 5,
    height: 5,
    borderRadius: '50%',
    background: 'var(--green)',
    boxShadow: '0 0 5px var(--green)',
    animation: 'pulse-dot 2s ease-in-out infinite',
  },
  liveLabel: {
    fontSize: 9,
    fontFamily: 'var(--font-display)',
    color: 'var(--green)',
    letterSpacing: '0.12em',
    fontWeight: 600,
  },
  title: {
    fontSize: 22,
    fontWeight: 700,
    color: 'var(--text-primary)',
    letterSpacing: '-0.02em',
    marginBottom: 6,
  },
  subtitle: {
    fontSize: 13,
    color: 'var(--text-secondary)',
    lineHeight: 1.5,
    marginBottom: 24,
  },
  errorBanner: {
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    padding: '10px 14px',
    background: 'var(--red-dim)',
    border: '1px solid rgba(255,64,64,0.25)',
    borderRadius: 8,
    fontSize: 12,
    fontFamily: 'var(--font-display)',
    color: 'var(--red)',
    marginBottom: 18,
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: 18,
  },
  field: {
    display: 'flex',
    flexDirection: 'column',
    gap: 7,
  },
  label: {
    fontSize: 10,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
    letterSpacing: '0.10em',
    fontWeight: 600,
  },
  input: {
    background: 'var(--bg-input)',
    border: '1px solid var(--border)',
    borderRadius: 8,
    color: 'var(--text-primary)',
    fontSize: 13,
    fontFamily: 'var(--font-body)',
    padding: '11px 14px',
    outline: 'none',
    transition: 'border-color var(--transition)',
    width: '100%',
  },
  submitBtn: {
    background: 'var(--green)',
    border: 'none',
    borderRadius: 8,
    color: 'var(--text-inverse)',
    fontSize: 13,
    fontFamily: 'var(--font-display)',
    fontWeight: 700,
    padding: '13px',
    cursor: 'pointer',
    letterSpacing: '0.04em',
    transition: 'all var(--transition)',
    marginTop: 4,
    boxShadow: '0 4px 16px rgba(0,232,122,0.25)',
    width: '100%',
  },
  btnContent: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  btnSpinner: {
    display: 'inline-block',
    width: 12,
    height: 12,
    borderRadius: '50%',
    border: '2px solid rgba(0,0,0,0.2)',
    borderTopColor: 'var(--text-inverse)',
    animation: 'spin 0.7s linear infinite',
  },
  footer: {
    marginTop: 24,
    paddingTop: 16,
    borderTop: '1px solid var(--border-subtle)',
    textAlign: 'center',
  },
  footerText: {
    fontSize: 11,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
  },
}