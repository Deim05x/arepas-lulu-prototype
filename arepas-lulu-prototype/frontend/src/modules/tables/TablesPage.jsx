import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api/client.js'
import Toast from '../../components/Toast.jsx'

const labels = { DISPONIBLE: 'Disponible', OCUPADA: 'Ocupada', RESERVADA: 'Reservada', INACTIVA: 'Inactiva' }

export default function TablesPage() {
  const [tables, setTables] = useState([])
  const [toast, setToast] = useState(null)
  const load = async () => { try { setTables(await api.mesas.listar()) } catch (e) { setToast({ type: 'error', message: e.message }) } }
  useEffect(() => { load() }, [])
  const counts = useMemo(() => Object.fromEntries(['DISPONIBLE','OCUPADA','RESERVADA','INACTIVA'].map((s) => [s, tables.filter((t) => t.estado === s).length])), [tables])
  const change = async (table, state) => { const reference = state === 'RESERVADA' ? (window.prompt('Nombre o referencia de la reserva', table.referencia || '') || null) : null; try { await api.mesas.estado(table.numero, { estado: state, referencia: reference }); await load() } catch (e) { setToast({ type: 'error', message: e.message }) } }

  return <>
    <header className="page-header"><div><span className="eyebrow">F-08 · Sala</span><h1>Ocupación y asignación de mesas</h1><p>Mapa operativo de mesas: disponibles, ocupadas, reservadas o fuera de servicio.</p></div><div className="metric-card accent"><span>Disponibles</span><strong>{counts.DISPONIBLE || 0}</strong></div></header>
    <section className="metrics-grid four"><div className="mini-stat"><span>Disponibles</span><b>{counts.DISPONIBLE || 0}</b></div><div className="mini-stat occupied"><span>Ocupadas</span><b>{counts.OCUPADA || 0}</b></div><div className="mini-stat reserved"><span>Reservadas</span><b>{counts.RESERVADA || 0}</b></div><div className="mini-stat muted"><span>Inactivas</span><b>{counts.INACTIVA || 0}</b></div></section>
    <section className="table-map">{tables.map((table) => <article className={`table-card ${table.estado.toLowerCase()}`} key={table.numero}><div className="table-circle"><span>Mesa</span><strong>{table.numero}</strong><small>{table.capacidad} personas</small></div><span className={`badge table-status ${table.estado.toLowerCase()}`}>{labels[table.estado]}</span>{table.referencia && <p className="reservation-ref">{table.referencia}</p>}<div className="table-actions"><button onClick={() => change(table, 'DISPONIBLE')}>Liberar</button><button onClick={() => change(table, 'RESERVADA')}>Reservar</button><button onClick={() => change(table, 'INACTIVA')}>Inactivar</button></div></article>)}</section>
    <div className="context-note">Las mesas pasan automáticamente a <strong>OCUPADA</strong> cuando F-02 registra un pedido y vuelven a <strong>DISPONIBLE</strong> cuando F-04 procesa el pago.</div>
    <Toast {...toast} onClose={() => setToast(null)} />
  </>
}
