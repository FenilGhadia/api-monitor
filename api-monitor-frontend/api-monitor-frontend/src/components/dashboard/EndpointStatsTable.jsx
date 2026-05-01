
export default function EndpointStatsTable({ data = [], loading = false }) {
  if (loading) {
    return (
      <div style={styles.skeletonWrap}>
        {[...Array(5)].map((_, i) => (
          <div key={i} style={{ ...styles.skeletonRow, opacity: 1 - i * 0.15 }} />
        ))}
      </div>
    )
  }

  if (!data.length) {
    return (
      <div style={styles.empty}>No endpoint data for this window</div>
    )
  }

  const maxRequests = Math.max(...data.map((d) => d.totalRequests), 1)

  return (
    <div style={styles.table}>
      {/* Header */}
      <div style={{ ...styles.row, ...styles.headerRow }}>
        <span style={{ ...styles.cell, flex: 3 }}>Endpoint</span>
        <span style={{ ...styles.cell, flex: 2 }}>Requests</span>
        <span style={{ ...styles.cell, flex: 1, textAlign: 'right' }}>Avg Time</span>
        <span style={{ ...styles.cell, flex: 1, textAlign: 'right' }}>Error Rate</span>
      </div>

      {/* Data rows */}
      {data.map((row, i) => {
        const barWidth = (row.totalRequests / maxRequests) * 100
        const errorColor = row.errorRate > 10
          ? 'var(--red)'
          : row.errorRate > 5
            ? 'var(--amber)'
            : 'var(--green)'

        return (
          <div key={i} style={styles.row}>
            {/* Endpoint */}
            <span style={{ ...styles.cell, flex: 3 }}>
              <span style={styles.endpointText}>{row.endpoint}</span>
            </span>

            {/* Request count + bar */}
            <span style={{ ...styles.cell, flex: 2 }}>
              <span style={styles.countValue}>
                {row.totalRequests?.toLocaleString()}
              </span>
              <div style={styles.barTrack}>
                <div style={{ ...styles.barFill, width: `${barWidth}%` }} />
              </div>
            </span>

            {/* Avg response time */}
            <span style={{ ...styles.cell, flex: 1, textAlign: 'right' }}>
              <span style={styles.monoValue}>
                {row.avgResponseTimeMs != null
                  ? `${Math.round(row.avgResponseTimeMs)}ms`
                  : '—'}
              </span>
            </span>

            {/* Error rate */}
            <span style={{ ...styles.cell, flex: 1, textAlign: 'right' }}>
              <span style={{ ...styles.monoValue, color: errorColor }}>
                {row.errorRate != null ? `${row.errorRate.toFixed(1)}%` : '—'}
              </span>
            </span>
          </div>
        )
      })}
    </div>
  )
}

const styles = {
  table: {
    display: 'flex',
    flexDirection: 'column',
    gap: 1,
  },
  row: {
    display: 'flex',
    alignItems: 'center',
    gap: 12,
    padding: '10px 0',
    borderBottom: '1px solid var(--border-subtle)',
  },
  headerRow: {
    borderBottom: '1px solid var(--border)',
    paddingBottom: 8,
  },
  cell: {
    fontSize: 12,
    color: 'var(--text-secondary)',
    fontFamily: 'var(--font-display)',
    letterSpacing: '0.04em',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  endpointText: {
    fontFamily: 'var(--font-display)',
    fontSize: 11,
    color: 'var(--text-primary)',
    display: 'block',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  countValue: {
    fontFamily: 'var(--font-display)',
    fontSize: 12,
    color: 'var(--text-primary)',
    display: 'block',
    marginBottom: 4,
  },
  barTrack: {
    height: 3,
    background: 'var(--bg-hover)',
    borderRadius: 2,
    overflow: 'hidden',
  },
  barFill: {
    height: '100%',
    background: 'var(--green)',
    borderRadius: 2,
    opacity: 0.6,
    transition: 'width var(--transition-slow)',
  },
  monoValue: {
    fontFamily: 'var(--font-display)',
    fontSize: 11,
    color: 'var(--text-secondary)',
  },
  skeletonWrap: { display: 'flex', flexDirection: 'column', gap: 8 },
  skeletonRow: {
    height: 36,
    borderRadius: 4,
    background: 'linear-gradient(90deg, var(--bg-elevated) 25%, var(--bg-hover) 50%, var(--bg-elevated) 75%)',
    backgroundSize: '200% 100%',
    animation: 'shimmer 1.4s infinite',
  },
  empty: {
    fontSize: 12,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
    padding: '24px 0',
    textAlign: 'center',
  },
}