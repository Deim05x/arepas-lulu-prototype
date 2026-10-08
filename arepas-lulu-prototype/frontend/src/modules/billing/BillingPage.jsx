import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api/client.js'
import { money, dateTime } from '../../utils/format.js'
import Toast from '../../components/Toast.jsx'

export default function BillingPage() {
  const [orders, setOrders] = useState([])
  const [selected, setSelected] = useState('')
  const [method, setMethod] = useState('EFECTIVO')
  const [received, setReceived] = useState('')
  const [result, setResult] = useState(null)
  const [toast, setToast] = useState(null)

  const load = async () => {
    try { setOrders(await api.pedidos.listar()) } catch (error) { setToast({ type: 'error', message: error.message }) }
  }
  useEffect(() => { load() }, [])
  const eligible = useMemo(() => orders.filter((o) => ['LISTO', 'SERVIDO'].includes(o.estado)), [orders])
  const order = eligible.find((o) => String(o.id) === String(selected))

  const pay = async (event) => {
    event.preventDefault()
    if (!order) return setToast({ type: 'error', message: 'Selecciona una orden lista o servida.' })
    try {
      const response = await api.pagos.crear({ pedidoId: order.id, medio: method, montoRecibido: method === 'EFECTIVO' ? Number(received || order.total) : null })
      setResult(response)
      setSelected('')
      setReceived('')
      setToast({ type: 'success', message: `Pago registrado. Factura ${response.factura.numero}.` })
      await load()
    } catch (error) { setToast({ type: 'error', message: error.message }) }
  }

  return <>
    <header className="page-header"><div><span className="eyebrow">F-04 · Cierre transaccional</span><h1>Pagos y facturación</h1><p>Liquida órdenes, registra el medio de pago y genera el comprobante de venta.</p></div><div className="metric-card"><span>Por cobrar</span><strong>{eligible.length}</strong></div></header>
    <section className="two-column billing-layout">
      <form className="panel form-panel" onSubmit={pay}>
        <div className="panel-heading"><div><span className="eyebrow">Caja</span><h2>Procesar pago</h2></div></div>
        <label>Orden lista<select value={selected} onChange={(e) => setSelected(e.target.value)}><option value="">Seleccionar…</option>{eligible.map((o) => <option key={o.id} value={o.id}>#{o.id} · {o.tipoServicio === 'DOMICILIO' ? 'Domicilio' : `Mesa ${o.mesaNumero}`} · {money(o.total)}</option>)}</select></label>
        <label>Medio de pago<select value={method} onChange={(e) => setMethod(e.target.value)}><option>EFECTIVO</option><option>TARJETA</option><option>TRANSFERENCIA</option></select></label>
        {method === 'EFECTIVO' && <label>Efectivo recibido<input type="number" min="0" value={received} onChange={(e) => setReceived(e.target.value)} placeholder={order ? String(order.total) : '0'} /></label>}
        {order && <div className="checkout-summary"><span>Total a cobrar</span><strong>{money(order.total)}</strong><small>{order.items.length} ítem(s) · {order.estado}</small></div>}
        <button className="primary-button">Registrar pago y facturar</button>
      </form>
      <section className="panel">
        <div className="panel-heading"><div><span className="eyebrow">Comprobante</span><h2>Última transacción</h2></div></div>
        {result ? <div className="invoice-card"><div className="invoice-brand"><span>AREPAS LULÚ</span><strong>{result.factura.numero}</strong></div><div className="invoice-line"><span>Pedido</span><b>#{result.pedido.id}</b></div><div className="invoice-line"><span>Medio</span><b>{result.pago.medio}</b></div><div className="invoice-line"><span>Fecha</span><b>{dateTime(result.factura.createdAt)}</b></div><div className="invoice-total"><span>Total</span><strong>{money(result.factura.total)}</strong></div>{Number(result.pago.cambio) > 0 && <div className="invoice-line"><span>Cambio</span><b>{money(result.pago.cambio)}</b></div>}</div> : <div className="empty-state tall">La factura aparecerá aquí después de procesar un pago.</div>}
      </section>
    </section>
    <Toast {...toast} onClose={() => setToast(null)} />
  </>
}
