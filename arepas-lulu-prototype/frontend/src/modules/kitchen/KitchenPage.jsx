import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api/client.js'
import { money, dateTime } from '../../utils/format.js'
import Toast from '../../components/Toast.jsx'

const lanes = [
  { estado: 'ENVIADO_COCINA', title: 'Por preparar', subtitle: 'Comandas recién confirmadas', next: 'EN_PREPARACION', action: 'Iniciar preparación' },
  { estado: 'EN_PREPARACION', title: 'En preparación', subtitle: 'Trabajo activo de cocina', next: 'LISTO', action: 'Marcar listo' },
  { estado: 'LISTO', title: 'Listos', subtitle: 'Esperan ser servidos', next: 'SERVIDO', action: 'Marcar servido' },
]

export default function KitchenPage() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState(null)

  const load = async () => {
    setLoading(true)
    try { setOrders(await api.cocina.listar()) } catch (error) { setToast({ type: 'error', message: error.message }) }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const active = useMemo(() => orders.filter((o) => o.estado !== 'SERVIDO'), [orders])

  const move = async (order, next) => {
    try {
      await api.cocina.estado(order.id, next)
      setToast({ type: 'success', message: `Pedido #${order.id} actualizado a ${next.replaceAll('_', ' ')}.` })
      await load()
    } catch (error) { setToast({ type: 'error', message: error.message }) }
  }

  return <>
    <header className="page-header">
      <div><span className="eyebrow">F-03 · Kitchen Display System</span><h1>Cola de preparación</h1><p>Visualiza comandas y controla su avance hasta que estén listas para servir.</p></div>
      <div className="metric-card accent"><span>Pedidos activos</span><strong>{active.length}</strong></div>
    </header>

    {loading ? <div className="panel empty-state">Cargando cocina…</div> : (
      <section className="kanban-grid">
        {lanes.map((lane) => (
          <div className="kanban-lane" key={lane.estado}>
            <div className="lane-heading"><div><span className={`status-dot ${lane.estado.toLowerCase()}`} /><strong>{lane.title}</strong><small>{lane.subtitle}</small></div><b>{orders.filter((o) => o.estado === lane.estado).length}</b></div>
            <div className="lane-list">
              {orders.filter((o) => o.estado === lane.estado).map((order) => (
                <article className="kitchen-card" key={order.id}>
                  <div className="card-top"><div><span className="eyebrow">Pedido #{order.id}</span><h3>{order.tipoServicio === 'DOMICILIO' ? 'Domicilio' : `Mesa ${order.mesaNumero}`}</h3></div><strong>{money(order.total)}</strong></div>
                  <div className="ticket-items">{order.items.map((item) => <div key={`${order.id}-${item.productoId}`}><span>{item.cantidad}× {item.productoNombre}</span><small>{item.observacion || 'Sin observaciones'}</small></div>)}</div>
                  <div className="card-meta">{dateTime(order.createdAt)}</div>
                  <button className="primary-button full" onClick={() => move(order, lane.next)}>{lane.action}</button>
                </article>
              ))}
              {!orders.some((o) => o.estado === lane.estado) && <div className="lane-empty">Sin pedidos en esta etapa</div>}
            </div>
          </div>
        ))}
      </section>
    )}
    <Toast {...toast} onClose={() => setToast(null)} />
  </>
}
