import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api/client.js'
import { money, dateTime } from '../../utils/format.js'
import Toast from '../../components/Toast.jsx'

export default function DeliveryPage() {
  const [clients, setClients] = useState([])
  const [deliveries, setDeliveries] = useState([])
  const [products, setProducts] = useState([])
  const [clientForm, setClientForm] = useState({ nombre: '', telefono: '', direccion: '' })
  const [clientId, setClientId] = useState('')
  const [address, setAddress] = useState('')
  const [items, setItems] = useState([])
  const [productId, setProductId] = useState('')
  const [qty, setQty] = useState(1)
  const [note, setNote] = useState('')
  const [toast, setToast] = useState(null)

  const load = async () => {
    try {
      const [c, d, p] = await Promise.all([api.clientes.listar(), api.domicilios.listar(), api.productos.listar(true)])
      setClients(c); setDeliveries(d); setProducts(p)
    } catch (e) { setToast({ type: 'error', message: e.message }) }
  }
  useEffect(() => { load() }, [])
  const total = useMemo(() => items.reduce((sum, i) => sum + Number(i.precio) * i.cantidad, 0), [items])

  const createClient = async (e) => {
    e.preventDefault()
    try { const c = await api.clientes.crear(clientForm); setClientForm({ nombre: '', telefono: '', direccion: '' }); setClientId(String(c.id)); setToast({ type: 'success', message: 'Cliente registrado.' }); await load() }
    catch (err) { setToast({ type: 'error', message: err.message }) }
  }

  const addItem = () => {
    const p = products.find((x) => String(x.id) === String(productId)); if (!p) return
    setItems((current) => [...current, { localId: crypto.randomUUID(), productoId: p.id, nombre: p.nombre, precio: p.precio, cantidad: Number(qty), observacion: note || null }])
    setProductId(''); setQty(1); setNote('')
  }

  const createDelivery = async () => {
    if (!clientId || !items.length) return setToast({ type: 'error', message: 'Selecciona un cliente y agrega productos.' })
    try {
      const result = await api.domicilios.crear({ clienteId: Number(clientId), direccion: address || null, items: items.map(({ productoId, cantidad, observacion }) => ({ productoId, cantidad, observacion })) })
      setToast({ type: 'success', message: `Domicilio creado con pedido #${result.pedido.id}.` }); setItems([]); setAddress(''); await load()
    } catch (e) { setToast({ type: 'error', message: e.message }) }
  }

  const advance = async (delivery) => {
    const next = delivery.estado === 'PENDIENTE' ? 'DESPACHADO' : delivery.estado === 'DESPACHADO' ? 'ENTREGADO' : null
    if (!next) return
    const rider = next === 'DESPACHADO' ? (window.prompt('Nombre del domiciliario', delivery.repartidor || '') || null) : delivery.repartidor
    try { await api.domicilios.estado(delivery.id, { estado: next, repartidor: rider }); await load() } catch (e) { setToast({ type: 'error', message: e.message }) }
  }

  return <>
    <header className="page-header"><div><span className="eyebrow">F-06 · Clientes y entrega</span><h1>Domicilios</h1><p>Registra clientes, arma pedidos de domicilio y controla el despacho hasta la entrega.</p></div><div className="metric-card accent"><span>Domicilios activos</span><strong>{deliveries.filter((d) => !['ENTREGADO','CANCELADO'].includes(d.estado)).length}</strong></div></header>
    <section className="delivery-grid">
      <form className="panel compact-form" onSubmit={createClient}><div className="panel-heading"><div><span className="eyebrow">CRM básico</span><h2>Nuevo cliente</h2></div></div><label>Nombre<input required value={clientForm.nombre} onChange={(e) => setClientForm({ ...clientForm, nombre: e.target.value })} /></label><label>Teléfono<input required value={clientForm.telefono} onChange={(e) => setClientForm({ ...clientForm, telefono: e.target.value })} /></label><label>Dirección<input value={clientForm.direccion} onChange={(e) => setClientForm({ ...clientForm, direccion: e.target.value })} /></label><button className="secondary-button">Guardar cliente</button></form>
      <section className="panel delivery-builder"><div className="panel-heading"><div><span className="eyebrow">Nueva entrega</span><h2>Armar domicilio</h2></div><strong>{money(total)}</strong></div><div className="form-grid"><label>Cliente<select value={clientId} onChange={(e) => setClientId(e.target.value)}><option value="">Seleccionar…</option>{clients.map((c) => <option key={c.id} value={c.id}>{c.nombre} · {c.telefono}</option>)}</select></label><label>Dirección alternativa<input value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Usa la del cliente si queda vacío" /></label></div><div className="item-composer"><label>Producto<select value={productId} onChange={(e) => setProductId(e.target.value)}><option value="">Seleccionar…</option>{products.map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>)}</select></label><label className="qty-field">Cantidad<input type="number" min="1" value={qty} onChange={(e) => setQty(e.target.value)} /></label><label className="note-field">Observación<input value={note} onChange={(e) => setNote(e.target.value)} /></label><button type="button" className="secondary-button" onClick={addItem}>Agregar</button></div><div className="mini-list">{items.map((i) => <div key={i.localId}><span>{i.cantidad}× {i.nombre}</span><button className="text-button danger-text" onClick={() => setItems(items.filter((x) => x.localId !== i.localId))}>Quitar</button></div>)}</div><button className="primary-button" onClick={createDelivery}>Crear domicilio y enviar a cocina</button></section>
    </section>
    <section className="panel"><div className="panel-heading"><div><span className="eyebrow">Seguimiento</span><h2>Despachos recientes</h2></div></div><div className="delivery-list">{deliveries.map((d) => <article className="delivery-card" key={d.id}><div><span className={`badge ${d.estado === 'ENTREGADO' ? 'success' : d.estado === 'DESPACHADO' ? 'info' : 'warning'}`}>{d.estado}</span><h3>{d.clienteNombre}</h3><p>{d.direccion}</p><small>Pedido #{d.pedidoId} · {dateTime(d.createdAt)}</small></div>{['PENDIENTE','DESPACHADO'].includes(d.estado) && <button className="secondary-button" onClick={() => advance(d)}>{d.estado === 'PENDIENTE' ? 'Despachar' : 'Confirmar entrega'}</button>}</article>)}</div></section>
    <Toast {...toast} onClose={() => setToast(null)} />
  </>
}
