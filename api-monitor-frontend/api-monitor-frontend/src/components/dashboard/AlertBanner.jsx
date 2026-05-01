
export default function AlertBanner({ message, onClose }) {
  if (!message) return null

  return (
    <div style={styles.banner}>
      <div style={styles.left}>
        <span style={styles.icon}>▲</span>
        <div>
          <div style={styles.title}>Performance Alert</div>
          <div style={styles.message}>{message}</div>
        </div>
      </div>
      {onClose && (
        <button style={styles.close} onClick={onClose}>✕</button>
      )}
    </div>
  )
}

const styles = {
  banner: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 16,
    padding: '14px 18px',
    background: 'var(--amber-dim)',
    border: '1px solid rgba(255,176,32,0.3)',
    borderRadius: 10,
    marginBottom: 24,
    animation: 'fadeIn 0.3s ease forwards',
  },
  left: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: 12,
  },
  icon: {
    fontSize: 14,
    color: 'var(--amber)',
    flexShrink: 0,
    marginTop: 1,
  },
  title: {
    fontSize: 12,
    fontFamily: 'var(--font-display)',
    fontWeight: 600,
    color: 'var(--amber)',
    letterSpacing: '0.05em',
    textTransform: 'uppercase',
    marginBottom: 2,
  },
  message: {
    fontSize: 13,
    color: 'var(--text-primary)',
    lineHeight: 1.5,
  },
  close: {
    background: 'none',
    border: 'none',
    color: 'var(--text-muted)',
    cursor: 'pointer',
    fontSize: 14,
    padding: '4px 6px',
    borderRadius: 4,
    flexShrink: 0,
  },
}