export function LoadingSpinner({ size = 32, text = 'Loading...' }) {
  return (
    <div style={styles.spinnerWrap}>
      <div style={{ ...styles.spinner, width: size, height: size }} />
      {text && <span style={styles.spinnerText}>{text}</span>}
    </div>
  )
}


export function ErrorMessage({ message, onRetry }) {
  return (
    <div style={styles.error}>
      <span style={styles.errorIcon}>⚠</span>
      <span style={styles.errorText}>{message || 'Something went wrong'}</span>
      {onRetry && (
        <button style={styles.retryBtn} onClick={onRetry}>
          Retry
        </button>
      )}
    </div>
  )
}


export function EmptyState({ message = 'No data available' }) {
  return (
    <div style={styles.empty}>
      <div style={styles.emptyIcon}>◻</div>
      <div style={styles.emptyText}>{message}</div>
    </div>
  )
}


export function PageHeader({ title, subtitle, children }) {
  return (
    <div style={styles.header}>
      <div>
        <h1 style={styles.title}>{title}</h1>
        {subtitle && <p style={styles.subtitle}>{subtitle}</p>}
      </div>
      {children && <div style={styles.headerActions}>{children}</div>}
    </div>
  )
}


export function Badge({ children, type = 'neutral' }) {
  const colors = {
    success: { bg: 'var(--green-dim)',   color: 'var(--green)',  border: 'rgba(0,232,122,0.25)' },
    error:   { bg: 'var(--red-dim)',     color: 'var(--red)',    border: 'rgba(255,64,64,0.25)' },
    warning: { bg: 'var(--amber-dim)',   color: 'var(--amber)',  border: 'rgba(255,176,32,0.25)' },
    info:    { bg: 'var(--blue-dim)',    color: 'var(--blue)',   border: 'rgba(61,158,255,0.25)' },
    neutral: { bg: 'var(--bg-elevated)', color: 'var(--text-secondary)', border: 'var(--border)' },
  }
  const c = colors[type] ?? colors.neutral
  return (
    <span style={{
      ...styles.badge,
      background: c.bg,
      color: c.color,
      border: `1px solid ${c.border}`,
    }}>
      {children}
    </span>
  )
}

const styles = {
  // Spinner
  spinnerWrap: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
    padding: '48px',
    color: 'var(--text-muted)',
  },
  spinner: {
    borderRadius: '50%',
    border: '2px solid var(--border)',
    borderTopColor: 'var(--green)',
    animation: 'spin 0.7s linear infinite',
  },
  spinnerText: {
    fontSize: 12,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
    letterSpacing: '0.05em',
  },

  // Error
  error: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '14px 16px',
    background: 'var(--red-dim)',
    border: '1px solid rgba(255,64,64,0.25)',
    borderRadius: 8,
    fontSize: 13,
  },
  errorIcon: { color: 'var(--red)', fontSize: 14 },
  errorText: { color: 'var(--text-primary)', flex: 1 },
  retryBtn: {
    background: 'none',
    border: '1px solid rgba(255,64,64,0.4)',
    borderRadius: 5,
    color: 'var(--red)',
    fontSize: 11,
    fontFamily: 'var(--font-display)',
    padding: '3px 10px',
    cursor: 'pointer',
    letterSpacing: '0.04em',
  },

  // Empty
  empty: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: 8,
    padding: '48px',
    color: 'var(--text-muted)',
  },
  emptyIcon: { fontSize: 28, opacity: 0.3 },
  emptyText: { fontSize: 13, fontFamily: 'var(--font-display)' },

  // Page header
  header: {
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    marginBottom: 28,
    gap: 16,
  },
  title: {
    fontSize: 22,
    fontWeight: 700,
    color: 'var(--text-primary)',
    fontFamily: 'var(--font-body)',
    letterSpacing: '-0.02em',
  },
  subtitle: {
    fontSize: 13,
    color: 'var(--text-secondary)',
    marginTop: 4,
  },
  headerActions: {
    display: 'flex',
    gap: 10,
    alignItems: 'center',
    flexShrink: 0,
  },

  // Badge
  badge: {
    display: 'inline-flex',
    alignItems: 'center',
    borderRadius: 4,
    padding: '2px 7px',
    fontSize: 11,
    fontFamily: 'var(--font-display)',
    fontWeight: 500,
    letterSpacing: '0.04em',
  },
}