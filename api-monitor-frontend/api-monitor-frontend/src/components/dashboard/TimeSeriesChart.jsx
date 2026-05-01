import {
  ResponsiveContainer, AreaChart, Area, XAxis, YAxis,
  CartesianGrid, Tooltip, Legend,
} from 'recharts'
import { format, parseISO } from 'date-fns'


export default function TimeSeriesChart({ data = [], loading = false, height = 260 }) {
  if (loading) {
    return <div style={{ ...styles.skeleton, height }} />
  }

  if (!data.length) {
    return (
      <div style={{ ...styles.empty, height }}>
        <span>No time-series data for selected window</span>
      </div>
    )
  }

  
  const formatted = data.map((d) => ({
    ...d,
    label: formatBucket(d.bucket),
  }))

  const CustomTooltip = ({ active, payload, label }) => {
    if (!active || !payload?.length) return null
    return (
      <div style={styles.tooltip}>
        <div style={styles.tooltipTitle}>{label}</div>
        {payload.map((p) => (
          <div key={p.name} style={styles.tooltipRow}>
            <span style={{ ...styles.tooltipDot, background: p.color }} />
            <span style={styles.tooltipLabel}>{p.name}</span>
            <span style={styles.tooltipValue}>{p.value?.toLocaleString()}</span>
          </div>
        ))}
      </div>
    )
  }

  return (
    <ResponsiveContainer width="100%" height={height}>
      <AreaChart data={formatted} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
        <defs>
          <linearGradient id="gradRequests" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%"  stopColor="var(--green)" stopOpacity={0.25} />
            <stop offset="95%" stopColor="var(--green)" stopOpacity={0} />
          </linearGradient>
          <linearGradient id="gradErrors" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%"  stopColor="var(--red)" stopOpacity={0.25} />
            <stop offset="95%" stopColor="var(--red)" stopOpacity={0} />
          </linearGradient>
        </defs>

        <CartesianGrid strokeDasharray="3 3" vertical={false} />

        <XAxis
          dataKey="label"
          tick={tickStyle}
          axisLine={false}
          tickLine={false}
          interval="preserveStartEnd"
        />
        <YAxis
          tick={tickStyle}
          axisLine={false}
          tickLine={false}
        />

        <Tooltip content={<CustomTooltip />} />

        <Legend
          wrapperStyle={{ fontSize: 11, fontFamily: 'var(--font-display)', paddingTop: 12 }}
        />

        <Area
          type="monotone"
          dataKey="requestCount"
          name="Requests"
          stroke="var(--green)"
          strokeWidth={1.5}
          fill="url(#gradRequests)"
          dot={false}
          activeDot={{ r: 4, fill: 'var(--green)' }}
        />
        <Area
          type="monotone"
          dataKey="errorCount"
          name="Errors"
          stroke="var(--red)"
          strokeWidth={1.5}
          fill="url(#gradErrors)"
          dot={false}
          activeDot={{ r: 4, fill: 'var(--red)' }}
        />
      </AreaChart>
    </ResponsiveContainer>
  )
}


function formatBucket(bucket) {
  if (!bucket) return ''
  try {
    const d = typeof bucket === 'string' ? parseISO(bucket) : new Date(bucket)
    return format(d, 'HH:mm')
  } catch {
    return String(bucket).slice(11, 16)
  }
}

const tickStyle = {
  fill: 'var(--text-muted)',
  fontSize: 10,
  fontFamily: 'var(--font-display)',
}

const styles = {
  skeleton: {
    borderRadius: 8,
    background: 'linear-gradient(90deg, var(--bg-elevated) 25%, var(--bg-hover) 50%, var(--bg-elevated) 75%)',
    backgroundSize: '200% 100%',
    animation: 'shimmer 1.4s infinite',
  },
  empty: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: 12,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
    border: '1px dashed var(--border)',
    borderRadius: 8,
  },
  tooltip: {
    background: 'var(--bg-elevated)',
    border: '1px solid var(--border)',
    borderRadius: 8,
    padding: '10px 14px',
    fontSize: 12,
    minWidth: 160,
  },
  tooltipTitle: {
    fontFamily: 'var(--font-display)',
    color: 'var(--text-secondary)',
    marginBottom: 8,
    fontSize: 11,
  },
  tooltipRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    marginBottom: 4,
  },
  tooltipDot: {
    width: 6, height: 6,
    borderRadius: '50%',
    flexShrink: 0,
  },
  tooltipLabel: {
    flex: 1,
    color: 'var(--text-secondary)',
    fontFamily: 'var(--font-display)',
  },
  tooltipValue: {
    color: 'var(--text-primary)',
    fontFamily: 'var(--font-display)',
    fontWeight: 600,
  },
}