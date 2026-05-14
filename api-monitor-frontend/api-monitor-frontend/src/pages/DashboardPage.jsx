import { useState, useEffect } from 'react'
import { useAnalytics } from '../hooks/useAnalytics'
import MetricCard from '../components/dashboard/MetricCard'
import AlertBanner from '../components/dashboard/AlertBanner'
import TimeSeriesChart from '../components/dashboard/TimeSeriesChart'
import StatusCodeChart from '../components/dashboard/StatusCodeChart'
import EndpointStatsTable from '../components/dashboard/EndpointStatsTable'
import { PageHeader, ErrorMessage } from '../components/common'


const WINDOWS = [
  { label: '1h',  hours: 1   },
  { label: '6h',  hours: 6   },
  { label: '24h', hours: 24  },
  { label: '7d',  hours: 168 },
]


function useRelativeTime(date) {
  const [label, setLabel] = useState('')

  useEffect(() => {
    if (!date) { setLabel(''); return }

    const tick = () => {
      const secs = Math.floor((Date.now() - date.getTime()) / 1000)
      if (secs < 60) setLabel(`${secs}s ago`)
      else           setLabel(`${Math.floor(secs / 60)}m ago`)
    }

    tick()
    const id = setInterval(tick, 1000)
    return () => clearInterval(id)
  }, [date])

  return label
}

export default function DashboardPage() {
  const [windowHours, setWindowHours] = useState(24)
  const [alertDismissed, setAlertDismissed] = useState(false)

  const to   = new Date()
  const from = new Date(Date.now() - windowHours * 60 * 60 * 1000)

  const {
    summary,
    timeSeries,
    statusCodes,
    endpointStats,
    loading,
    error,
    refresh,
    lastUpdated,
  } = useAnalytics(from, to, 60_000)

  const relativeTime = useRelativeTime(lastUpdated)
  const showAlert = !alertDismissed && summary?.alertTriggered

  const fmt = {
    requests: (n) => n != null ? n.toLocaleString() : '—',
    avgTime:  (n) => n != null ? Math.round(n).toString() : '—',
    errorRate:(n) => n != null ? n.toFixed(1) : '—',
    slow:     (n) => n != null ? n.toLocaleString() : '—',
  }

  return (
    <div style={styles.page}>
      <PageHeader title="Dashboard" subtitle="Real-time API performance monitoring">
        <div style={styles.windowSelector}>
          {WINDOWS.map((w) => (
            <button
              key={w.label}
              style={{
                ...styles.windowBtn,
                ...(windowHours === w.hours ? styles.windowBtnActive : {}),
              }}
              onClick={() => { setWindowHours(w.hours); setAlertDismissed(false) }}
            >
              {w.label}
            </button>
          ))}
        </div>

        <button style={styles.refreshBtn} onClick={refresh} title="Refresh">
          <RefreshIcon spinning={loading} />
        </button>

        {lastUpdated && (
          <span style={styles.lastUpdated}>Updated {relativeTime}</span>
        )}
      </PageHeader>

      {showAlert && (
        <AlertBanner
          message={summary.alertMessage}
          onClose={() => setAlertDismissed(true)}
        />
      )}

      {error && <ErrorMessage message={error} onRetry={refresh} />}

      <div style={styles.metricsGrid}>
        <MetricCard
          label="Total Requests"
          value={fmt.requests(summary?.totalRequests)}
          accent="var(--green)"
          icon="◈"
          sublabel={`Last ${windowHours < 24 ? windowHours + 'h' : windowHours / 24 + 'd'}`}
          loading={loading && !summary}
        />
        <MetricCard
          label="Avg Response Time"
          value={fmt.avgTime(summary?.avgResponseTimeMs)}
          unit="ms"
          accent="var(--blue)"
          icon="◎"
          sublabel={summary?.avgResponseTimeMs > 1000 ? '⚠ Above 1s threshold' : 'Within normal range'}
          loading={loading && !summary}
        />
        <MetricCard
          label="Error Rate"
          value={fmt.errorRate(summary?.errorRate)}
          unit="%"
          accent={
            summary?.errorRate > 10 ? 'var(--red)'
            : summary?.errorRate > 5 ? 'var(--amber)'
            : 'var(--green)'
          }
          icon="◬"
          sublabel={`${fmt.requests(summary?.errorCount)} error responses`}
          loading={loading && !summary}
        />
        <MetricCard
          label="Slow Requests"
          value={fmt.slow(summary?.slowRequestCount)}
          accent="var(--amber)"
          icon="◑"
          sublabel="Response time > 1000ms"
          loading={loading && !summary}
        />
      </div>

      <div style={styles.chartsRow}>
        <ChartCard title="Request Volume" subtitle="Requests & errors over time" flex={3}>
          <TimeSeriesChart
            data={timeSeries}
            loading={loading && !timeSeries.length}
            height={240}
          />
        </ChartCard>
        <ChartCard title="Status Codes" subtitle="Response code distribution" flex={2}>
          <StatusCodeChart
            data={statusCodes}
            loading={loading && !statusCodes.length}
            height={240}
          />
        </ChartCard>
      </div>

      <ChartCard title="Top Endpoints" subtitle="Most active endpoints ranked by request count">
        <EndpointStatsTable
          data={endpointStats}
          loading={loading && !endpointStats.length}
        />
      </ChartCard>
    </div>
  )
}

function ChartCard({ title, subtitle, children, flex }) {
  return (
    <div style={{ ...styles.chartCard, ...(flex ? { flex } : {}) }}>
      <div style={styles.chartHeader}>
        <div>
          <div style={styles.chartTitle}>{title}</div>
          {subtitle && <div style={styles.chartSubtitle}>{subtitle}</div>}
        </div>
      </div>
      <div style={styles.chartBody}>{children}</div>
    </div>
  )
}

function RefreshIcon({ spinning }) {
  return (
    <svg
      width="14" height="14" viewBox="0 0 14 14" fill="none"
      style={{ animation: spinning ? 'spin 0.8s linear infinite' : 'none' }}
    >
      <path
        d="M13 7A6 6 0 1 1 7 1a6 6 0 0 1 4.243 1.757L13 1v4H9l1.5-1.5"
        stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"
      />
    </svg>
  )
}

const styles = {
  page: {
    maxWidth: 1200,
    margin: '0 auto',
    animation: 'fadeIn 0.3s ease forwards',
  },
  windowSelector: {
    display: 'flex',
    gap: 2,
    background: 'var(--bg-surface)',
    border: '1px solid var(--border)',
    borderRadius: 8,
    padding: 3,
  },
  windowBtn: {
    background: 'none',
    border: 'none',
    borderRadius: 5,
    color: 'var(--text-muted)',
    fontSize: 11,
    fontFamily: 'var(--font-display)',
    fontWeight: 600,
    padding: '4px 10px',
    cursor: 'pointer',
    letterSpacing: '0.04em',
    transition: 'all var(--transition)',
  },
  windowBtnActive: {
    background: 'var(--green-dim)',
    color: 'var(--green)',
    border: '1px solid rgba(0,232,122,0.25)',
  },
  refreshBtn: {
    background: 'var(--bg-surface)',
    border: '1px solid var(--border)',
    borderRadius: 7,
    color: 'var(--text-secondary)',
    padding: '6px 8px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    transition: 'all var(--transition)',
  },
  lastUpdated: {
    fontSize: 10,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
    letterSpacing: '0.04em',
  },
  metricsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: 14,
    marginBottom: 20,
  },
  chartsRow: {
    display: 'flex',
    gap: 14,
    marginBottom: 20,
    flexWrap: 'wrap',
  },
  chartCard: {
    background: 'var(--bg-surface)',
    border: '1px solid var(--border)',
    borderRadius: 10,
    overflow: 'hidden',
    flex: 1,
    minWidth: 280,
    animation: 'fadeIn 0.35s ease forwards',
  },
  chartHeader: {
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    padding: '16px 20px 12px',
    borderBottom: '1px solid var(--border-subtle)',
  },
  chartTitle: {
    fontSize: 13,
    fontWeight: 600,
    color: 'var(--text-primary)',
    fontFamily: 'var(--font-body)',
  },
  chartSubtitle: {
    fontSize: 11,
    color: 'var(--text-muted)',
    marginTop: 2,
    fontFamily: 'var(--font-display)',
  },
  chartBody: {
    padding: '16px 20px 20px',
  },
}