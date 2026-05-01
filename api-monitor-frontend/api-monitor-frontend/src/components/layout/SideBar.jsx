import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'


const IconGrid = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
    <rect x="1" y="1" width="6" height="6" rx="1" stroke="currentColor" strokeWidth="1.5"/>
    <rect x="9" y="1" width="6" height="6" rx="1" stroke="currentColor" strokeWidth="1.5"/>
    <rect x="1" y="9" width="6" height="6" rx="1" stroke="currentColor" strokeWidth="1.5"/>
    <rect x="9" y="9" width="6" height="6" rx="1" stroke="currentColor" strokeWidth="1.5"/>
  </svg>
)

const IconList = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
    <path d="M2 4h12M2 8h12M2 12h8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
  </svg>
)

const IconLogout = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
    <path d="M6 14H3a1 1 0 01-1-1V3a1 1 0 011-1h3" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
    <path d="M10 11l3-3-3-3M13 8H6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
)

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: IconGrid },
  { path: '/logs',      label: 'API Logs',  icon: IconList  },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <aside style={styles.sidebar}>
      {/* Logo */}
      <div style={styles.logo}>
        <div style={styles.logoMark}>
          <span style={styles.logoSignal} />
        </div>
        <div>
          <div style={styles.logoTitle}>API Monitor</div>
          <div style={styles.logoSub}>Control Panel</div>
        </div>
      </div>

      {/* Live indicator */}
      <div style={styles.liveRow}>
        <span style={styles.liveDot} />
        <span style={styles.liveText}>LIVE</span>
      </div>

      {/* Nav */}
      <nav style={styles.nav}>
        <div style={styles.navLabel}>NAVIGATION</div>
        {navItems.map(({ path, label, icon: Icon }) => (
          <NavLink
            key={path}
            to={path}
            style={({ isActive }) => ({
              ...styles.navItem,
              ...(isActive ? styles.navItemActive : {}),
            })}
          >
            {({ isActive }) => (
              <>
                <span style={{ color: isActive ? 'var(--green)' : 'inherit' }}>
                  <Icon />
                </span>
                {label}
                {isActive && <span style={styles.navActiveBar} />}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* User + logout */}
      <div style={styles.footer}>
        <div style={styles.userInfo}>
          <div style={styles.avatar}>
            {user?.username?.[0]?.toUpperCase() ?? 'A'}
          </div>
          <div style={styles.userDetails}>
            <div style={styles.userName}>{user?.username ?? 'Admin'}</div>
            <div style={styles.userRole}>{user?.role ?? 'ADMIN'}</div>
          </div>
        </div>
        <button style={styles.logoutBtn} onClick={handleLogout} title="Logout">
          <IconLogout />
        </button>
      </div>
    </aside>
  )
}

const styles = {
  sidebar: {
    width: 220,
    minWidth: 220,
    height: '100vh',
    background: 'var(--bg-surface)',
    borderRight: '1px solid var(--border)',
    display: 'flex',
    flexDirection: 'column',
    padding: '0',
    position: 'sticky',
    top: 0,
    overflow: 'hidden',
  },
  logo: {
    display: 'flex',
    alignItems: 'center',
    gap: 12,
    padding: '20px 20px 16px',
    borderBottom: '1px solid var(--border-subtle)',
  },
  logoMark: {
    width: 32,
    height: 32,
    background: 'var(--green-dim)',
    border: '1px solid rgba(0,232,122,0.3)',
    borderRadius: 8,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  logoSignal: {
    display: 'block',
    width: 10,
    height: 10,
    borderRadius: '50%',
    background: 'var(--green)',
    boxShadow: '0 0 8px var(--green)',
  },
  logoTitle: {
    fontFamily: 'var(--font-display)',
    fontSize: 13,
    fontWeight: 600,
    color: 'var(--text-primary)',
    letterSpacing: '0.02em',
  },
  logoSub: {
    fontSize: 10,
    color: 'var(--text-muted)',
    letterSpacing: '0.08em',
    textTransform: 'uppercase',
    marginTop: 1,
  },
  liveRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    padding: '10px 20px',
    borderBottom: '1px solid var(--border-subtle)',
  },
  liveDot: {
    display: 'block',
    width: 6,
    height: 6,
    borderRadius: '50%',
    background: 'var(--green)',
    boxShadow: '0 0 6px var(--green)',
    animation: 'pulse-dot 2s ease-in-out infinite',
  },
  liveText: {
    fontSize: 10,
    fontFamily: 'var(--font-display)',
    color: 'var(--green)',
    letterSpacing: '0.15em',
    fontWeight: 600,
  },
  nav: {
    flex: 1,
    padding: '16px 12px',
    display: 'flex',
    flexDirection: 'column',
    gap: 2,
  },
  navLabel: {
    fontSize: 9,
    fontFamily: 'var(--font-display)',
    color: 'var(--text-muted)',
    letterSpacing: '0.12em',
    padding: '0 8px 8px',
  },
  navItem: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '9px 10px',
    borderRadius: 6,
    fontSize: 13,
    fontWeight: 500,
    color: 'var(--text-secondary)',
    textDecoration: 'none',
    transition: 'all var(--transition)',
    position: 'relative',
    cursor: 'pointer',
  },
  navItemActive: {
    background: 'var(--green-dim)',
    color: 'var(--text-primary)',
  },
  navActiveBar: {
    position: 'absolute',
    right: 0,
    top: '50%',
    transform: 'translateY(-50%)',
    width: 3,
    height: 16,
    background: 'var(--green)',
    borderRadius: 2,
    boxShadow: '0 0 6px var(--green)',
  },
  footer: {
    padding: '12px 16px',
    borderTop: '1px solid var(--border)',
    display: 'flex',
    alignItems: 'center',
    gap: 10,
  },
  userInfo: {
    flex: 1,
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    minWidth: 0,
  },
  avatar: {
    width: 30,
    height: 30,
    borderRadius: '50%',
    background: 'var(--green-dim)',
    border: '1px solid rgba(0,232,122,0.3)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: 12,
    fontWeight: 700,
    fontFamily: 'var(--font-display)',
    color: 'var(--green)',
    flexShrink: 0,
  },
  userDetails: { minWidth: 0 },
  userName: {
    fontSize: 12,
    fontWeight: 600,
    color: 'var(--text-primary)',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  userRole: {
    fontSize: 10,
    color: 'var(--text-muted)',
    fontFamily: 'var(--font-display)',
    letterSpacing: '0.05em',
  },
  logoutBtn: {
    background: 'none',
    border: '1px solid var(--border)',
    borderRadius: 6,
    padding: '6px',
    color: 'var(--text-muted)',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    transition: 'all var(--transition)',
    flexShrink: 0,
  },
}