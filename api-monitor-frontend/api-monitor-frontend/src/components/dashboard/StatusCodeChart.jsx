import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis,
  CartesianGrid, Tooltip, Cell,
} from 'recharts'


export default function StatusCodeChart({ data = [], loading = false, height = 220 }) {
  if (loading) return <div style={{ ...styles.skeleton, height }} />

  if (!data.length) {
    return (
      <div style={{ ...styles.empty, height }}>
        No status code data available
      </div>
    )
  }

  const getColor = (category) => {
    const map = {
      '2xx': 'var(--green)',
      '3xx': 'var(--blue)',
      '4xx': 'var(--amber)',
      '5xx': 'var(--red)',
    }
    return map[category] ?? 'var(--text-muted)'
  }

  const CustomTooltip = ({ active, payload }) => {
    if (!active || !payload?.length) return null
    const { statusCode, count, category } = payload[0].payload
    return (
      <div style={styles.tooltip}>
        <div style={{ ...styles.tooltipCode, color: getColor(category) }}>
          {statusCode}
        </div>
        <div style={styles.tooltipDetail}>
          <span style={styles.tooltipCat}>{category}</span>
          <span style={styles.tooltipCount}>{count?.toLocaleString()} requests</span>
        </div>
      </div>
    )
  }

  return (
    <ResponsiveContainer width="100%" height={height}>
      <BarChart data={data} margin={{ top: 4, right: 4, left: -20, bottom: 0 }} barSize={28}>
        <CartesianGrid strokeDasharray="3 3" vertical={false} />
        <XAxis
          dataKey="statusCode"
          tick={tickStyle}
          axisLine={false}
          tickLine={false}
        />
        <YAxis
          tick={tickStyle}
          axisLine={false}
          tickLine={false}
        />
        <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
        <Bar dataKey="count" name="Requests" radius={[4, 4, 0, 0]}>
          {data.map((entry, index) => (
            <Cell key={index} fill={getColor(entry.category)} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
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
  },
  tooltipCode: {
    fontFamily: 'var(--font-display)',
    fontSize: 20,
    fontWeight: 600,
    marginBottom: 4,
  },
  tooltipDetail: {
    display: 'flex',
    flexDirection: 'column',
    gap: 2,
  },
  tooltipCat: {
    fontSize: 10,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
    letterSpacing: '0.08em',
  },
  tooltipCount: {
    fontSize: 12,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-primary)',
  },
}