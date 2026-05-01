
export default function MetricCard({ label, value, unit, accent = 'var(--green)', icon, sublabel, loading }) {
  return (
    <div style={{ ...styles.card }}>
      {/* Top row */}
      <div style={styles.topRow}>
        <span style={styles.label}>{label}</span>
        {icon && <span style={{ ...styles.icon, color: accent }}>{icon}</span>}
      </div>

      {/* Value */}
      <div style={styles.valueRow}>
        {loading ? (
          <div style={styles.skeleton} />
        ) : (
          <>
            <span style={{ ...styles.value, color: accent }}>{value ?? '—'}</span>
            {unit && <span style={styles.unit}>{unit}</span>}
          </>
        )}
      </div>

      {/* Sublabel */}
      {sublabel && !loading && (
        <div style={styles.sublabel}>{sublabel}</div>
      )}

      {/* Bottom accent line */}
      <div style={{ ...styles.accentBar, background: accent }} />
    </div>
  )
}

const styles = {
  card: {
    background: 'var(--bg-surface)',
    border: '1px solid var(--border)',
    borderRadius: 10,
    padding: '20px 22px 16px',
    position: 'relative',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
    gap: 8,
    transition: 'border-color var(--transition)',
    animation: 'fadeIn 0.3s ease forwards',
  },
  topRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  label: {
    fontSize: 11,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
    letterSpacing: '0.08em',
    textTransform: 'uppercase',
  },
  icon: {
    fontSize: 16,
    opacity: 0.7,
  },
  valueRow: {
    display: 'flex',
    alignItems: 'baseline',
    gap: 5,
    minHeight: 38,
  },
  value: {
    fontSize: 32,
    fontFamily: 'var(--font-display)',
    fontWeight: 600,
    letterSpacing: '-0.02em',
    lineHeight: 1,
  },
  unit: {
    fontSize: 14,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-secondary)',
    fontWeight: 400,
  },
  sublabel: {
    fontSize: 11,
    color: 'var(--text-secondary)',
    fontFamily: 'var(--font-display)',
  },
  skeleton: {
    height: 32,
    width: 120,
    borderRadius: 4,
    background: 'linear-gradient(90deg, var(--bg-elevated) 25%, var(--bg-hover) 50%, var(--bg-elevated) 75%)',
    backgroundSize: '200% 100%',
    animation: 'shimmer 1.4s infinite',
  },
  accentBar: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: 2,
    opacity: 0.6,
  },
}