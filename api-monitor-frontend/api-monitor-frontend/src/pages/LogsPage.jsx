import { useLogs } from '../hooks/useLogs'
import LogsFilter from '../components/logs/LogsFilter'
import LogsTable from '../components/logs/LogsTable'
import { PageHeader, ErrorMessage } from '../components/common'

export default function LogsPage() {
  const {
    page,
    loading,
    error,
    currentPage,
    setPage,
    applyFilters,
    refresh,
  } = useLogs(20)

  
  const countLabel = (() => {
    if (!page) return null
    if (page.totalElements === 0) return 'No entries'
    const start = page.number * page.size + 1
    const end   = Math.min((page.number + 1) * page.size, page.totalElements)
    return `Showing ${start}–${end} of ${page.totalElements.toLocaleString()}`
  })()

  return (
    <div style={styles.page}>
      {/* ── Header ─────────────────────────────────────── */}
      <PageHeader
        title="API Logs"
        subtitle="Searchable request history with filtering"
      >
        <button style={styles.refreshBtn} onClick={refresh} title="Refresh logs">
          <RefreshIcon />
          <span>Refresh</span>
        </button>
        {countLabel && (
          <div style={styles.countPill}>
            <span style={styles.countDot} />
            {countLabel}
          </div>
        )}
      </PageHeader>

      {/* ── Error state ─────────────────────────────────── */}
      {error && (
        <div style={{ marginBottom: 16 }}>
          <ErrorMessage message={error} onRetry={refresh} />
        </div>
      )}

      {/* ── Filter bar ──────────────────────────────────── */}
      <LogsFilter onFilter={applyFilters} loading={loading} />

      {/* ── Logs table ──────────────────────────────────── */}
      <LogsTable
        data={page}
        loading={loading}
        onPageChange={setPage}
      />
    </div>
  )
}

function RefreshIcon() {
  return (
    <svg width="12" height="12" viewBox="0 0 14 14" fill="none">
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
  refreshBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    background: 'var(--bg-surface)',
    border: '1px solid var(--border)',
    borderRadius: 7,
    color: 'var(--text-secondary)',
    fontSize: 12,
    fontFamily: 'var(--font-display)',
    padding: '6px 12px',
    cursor: 'pointer',
    letterSpacing: '0.04em',
    transition: 'all var(--transition)',
  },
  countPill: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    background: 'var(--bg-surface)',
    border: '1px solid var(--border)',
    borderRadius: 7,
    color: 'var(--text-secondary)',
    fontSize: 11,
    fontFamily: 'var(--font-display)',
    padding: '6px 12px',
    letterSpacing: '0.04em',
  },
  countDot: {
    display: 'block',
    width: 5,
    height: 5,
    borderRadius: '50%',
    background: 'var(--green)',
    boxShadow: '0 0 4px var(--green)',
  },
}