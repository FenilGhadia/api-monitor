import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'


export default function AppLayout() {
  return (
    <div style={styles.shell}>
      <Sidebar />
      <main style={styles.main}>
        <Outlet />
      </main>
    </div>
  )
}

const styles = {
  shell: {
    display: 'flex',
    height: '100vh',
    overflow: 'hidden',
    background: 'var(--bg-base)',
  },
  main: {
    flex: 1,
    overflow: 'auto',
    padding: '32px',
    minWidth: 0,
  },
}