export default function StatusPill({ active, children }) {
  return <span className={`status-pill ${active ? 'ok' : 'muted'}`}>{children}</span>
}
