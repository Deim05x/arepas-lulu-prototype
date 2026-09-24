import { useEffect, useMemo, useReducer, useState } from 'react'
import { api } from '../../api/client.js'
import { money, dateTime } from '../../utils/format.js'
import Toast from '../../components/Toast.jsx'
import { useCatalogEvents } from '../../hooks/useCatalogEvents.js'
import { initialOrder, orderReducer } from './orderReducer.js'

export default function OrderPage() {
  const [order, dispatch] = useReducer(orderReducer, initialOrder)
  const [products, setProducts] = useState([])
  const [history, setHistory] = useState([])
  const [selectedProduct, setSelectedProduct] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [note, setNote] = useState('')
  const [reviewing, setReviewing] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [created, setCreated] = useState(null)
  const [toast, setToast] = useState(null)

  const loadProducts = async () => {
    try {
      const current = await api.productos.listar(true)
      setProducts(current)
      dispatch({ type: 'SYNC_CATALOG', products: current })
    } catch (error) {
      setToast({ type: 'error', message: error.message })
    }
  }

  const loadHistory = async (mesa = order.mesaNumero) => {
    if (!mesa) { setHistory([]); return }
    try {
      setHistory(await api.pedidos.listar(mesa))
    } catch (error) {
      setToast({ type: 'error', message: error.message })
    }
  }

  useEffect(() => { loadProducts() }, [])
  useCatalogEvents(loadProducts)
  useEffect(() => {
    const timer = setTimeout(() => loadHistory(order.mesaNumero), 250)
    return () => clearTimeout(timer)
  }, [order.mesaNumero])

  const total = useMemo(() => order.items.reduce((sum, item) => sum + Number(item.precio) * item.cantidad, 0), [order.items])
  const hasUnavailable = order.items.some((item) => item.unavailable)

  const openReview = () => {
    if (!order.mesaNumero) {
      setToast({ type: 'error', message: 'Escribe el número de mesa para continuar.' })
      return
    }
    if (!order.items.length) {
      setToast({ type: 'error', message: 'Agrega al menos un producto al pedido.' })
      return
    }
    if (hasUnavailable) {
      setToast({ type: 'error', message: 'Quita los productos que ya no están disponibles.' })
      return
    }
    setReviewing(true)
  }

  const addItem = () => {
    const product = products.find((p) => String(p.id) === String(selectedProduct))
    if (!product) {
      setToast({ type: 'error', message: 'Selecciona un producto disponible.' })
      return
    }
    dispatch({
      type: 'ADD_ITEM',
      item: {
        localId: crypto.randomUUID(),
        productoId: product.id,
        nombre: product.nombre,
        precio: product.precio,
        cantidad: Number(quantity),
        observacion: note.trim(),
        unavailable: false,
      },
    })
    setSelectedProduct('')
    setQuantity(1)
    setNote('')
  }

  const submit = async () => {
    if (!order.mesaNumero || order.items.length === 0) return
    setSubmitting(true)
    try {
      const payload = {
        mesaNumero: Number(order.mesaNumero),
        items: order.items.map((item) => ({
          productoId: item.productoId,
          cantidad: item.cantidad,
          observacion: item.observacion || null,
        })),
      }
      const result = await api.pedidos.crear(payload)
      setCreated(result)
      setToast({ type: 'success', message: `Pedido #${result.id} enviado a cocina.` })
      setReviewing(false)
      dispatch({ type: 'CLEAR_ITEMS' })
      await loadProducts()
      await loadHistory(result.mesaNumero)
    } catch (error) {
      setToast({ type: 'error', message: error.message })
      await loadProducts()
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <header className="page-header">
        <div>
          <h1>Pedido digital por mesa</h1>
        </div>
        <div className="metric-card accent">
          <span>Total local</span>
          <strong>{money(total)}</strong>
        </div>
      </header>

      <section className="order-layout">
        <div className="panel order-builder">
          <div className="panel-heading">
            <div>
              <span className="eyebrow">Nueva orden</span>
              <h2>Armar pedido</h2>
            </div>
          </div>

          <label>
            Número de mesa
            <input type="number" min="1" value={order.mesaNumero} onChange={(e) => dispatch({ type: 'SET_MESA', value: e.target.value })} placeholder="Ej. 4" />
          </label>

          <div className="item-composer">
            <label>
              Producto disponible
              <select value={selectedProduct} onChange={(e) => setSelectedProduct(e.target.value)}>
                <option value="">Seleccionar…</option>
                {products.map((p) => <option value={p.id} key={p.id}>{p.nombre} · {money(p.precio)}</option>)}
              </select>
            </label>
            <label className="qty-field">
              Cantidad
              <input type="number" min="1" max="20" value={quantity} onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))} />
            </label>
            <label className="note-field">
              Observación
              <input maxLength="300" value={note} onChange={(e) => setNote(e.target.value)} placeholder="Ej. sin cebolla" />
            </label>
            <button type="button" className="secondary-button add-button" onClick={addItem}>Agregar</button>
          </div>

          <div className="cart-list">
            {order.items.map((item) => (
              <article className="cart-row" key={item.localId}>
                <div>
                  <strong>{item.cantidad} × {item.nombre}</strong>
                  <span>{item.observacion || 'Sin observaciones'}</span>
                  {item.unavailable && <span className="unavailable-warning">Producto no disponible: quítalo antes de confirmar.</span>}
                </div>
                <div className="cart-price">
                  <strong>{money(Number(item.precio) * item.cantidad)}</strong>
                  <button className="text-button danger-text" onClick={() => dispatch({ type: 'REMOVE_ITEM', localId: item.localId })}>Quitar</button>
                </div>
              </article>
            ))}
            {!order.items.length && <div className="empty-state">El pedido todavía está vacío.</div>}
          </div>

          <div className="order-total">
            <span>Total estimado</span>
            <strong>{money(total)}</strong>
          </div>

          <button className="primary-button" onClick={openReview}>Revisar y confirmar</button>
        </div>

        <aside className="panel history-panel">
          <div className="panel-heading">
            <div>
              <h2>Pedidos de la mesa</h2>
            </div>
          </div>
          {!order.mesaNumero && !created && <div className="empty-state">Escribe una mesa para consultar su historial.</div>}
          {created && (
            <div className="success-card">
              <span>Último pedido</span>
              <strong>#{created.id}</strong>
              <small>Mesa {created.mesaNumero} · {created.estado}</small>
            </div>
          )}
          <div className="history-list">
            {history.map((pedido) => (
              <article key={pedido.id} className="history-row">
                <div>
                  <strong>Pedido #{pedido.id}</strong>
                  <span>{pedido.items.length} ítem(s) · {dateTime(pedido.createdAt)}</span>
                </div>
                <div>
                  <strong>{money(pedido.total)}</strong>
                  <span className="state-label">{pedido.estado}</span>
                </div>
              </article>
            ))}
          </div>
        </aside>
      </section>

      {reviewing && (
        <div className="modal-backdrop" onMouseDown={() => !submitting && setReviewing(false)}>
          <section className="modal" onMouseDown={(e) => e.stopPropagation()}>
            <span className="eyebrow">Resumen del pedido</span>
            <h2>Revisar mesa {order.mesaNumero}</h2>
            <div className="review-list">
              {order.items.map((item) => (
                <div key={item.localId} className="review-row">
                  <div><strong>{item.cantidad} × {item.nombre}</strong><span>{item.observacion || 'Sin observaciones'}</span></div>
                  <strong>{money(Number(item.precio) * item.cantidad)}</strong>
                </div>
              ))}
            </div>
            <div className="order-total"><span>Total estimado</span><strong>{money(total)}</strong></div>
            <div className="modal-actions">
              <button className="secondary-button" disabled={submitting} onClick={() => setReviewing(false)}>Volver</button>
              <button className="primary-button" disabled={submitting} onClick={submit}>{submitting ? 'Enviando…' : 'Confirmar y enviar a cocina'}</button>
            </div>
          </section>
        </div>
      )}

      <Toast {...toast} onClose={() => setToast(null)} />
    </>
  )
}
