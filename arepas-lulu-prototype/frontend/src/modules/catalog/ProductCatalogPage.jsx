import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api/client.js'
import { money, dateTime } from '../../utils/format.js'
import StatusPill from '../../components/StatusPill.jsx'
import Toast from '../../components/Toast.jsx'
import { useCatalogEvents } from '../../hooks/useCatalogEvents.js'

const emptyForm = {
  nombre: '',
  categoria: 'Arepas',
  descripcion: '',
  precio: '',
  disponible: true,
}

export default function ProductCatalogPage() {
  const [productos, setProductos] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [confirmingCreate, setConfirmingCreate] = useState(false)
  const [toast, setToast] = useState(null)
  const [filter, setFilter] = useState('')

  const load = async () => {
    setLoading(true)
    try {
      setProductos(await api.productos.listar(false))
    } catch (error) {
      setToast({ type: 'error', message: error.message })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])
  useCatalogEvents(load)

  const visibleProducts = useMemo(() => {
    const q = filter.toLowerCase().trim()
    if (!q) return productos
    return productos.filter((p) => `${p.nombre} ${p.categoria}`.toLowerCase().includes(q))
  }, [productos, filter])

  const handleSubmit = async (event) => {
    event.preventDefault()
    const price = Number(form.precio)
    if (!Number.isFinite(price) || price < 0 || price > 100000) {
      setToast({ type: 'error', message: 'El precio debe estar entre $0 y $100.000.' })
      return
    }

    if (!editingId) {
      setConfirmingCreate(true)
      return
    }

    setSaving(true)
    try {
      await api.productos.actualizar(editingId, { ...form, precio: price })
      setToast({ type: 'success', message: 'Producto actualizado correctamente.' })
      setForm(emptyForm)
      setEditingId(null)
      await load()
    } catch (error) {
      const fields = Object.values(error.fieldErrors || {})
      setToast({ type: 'error', message: fields[0] || error.message })
    } finally {
      setSaving(false)
    }
  }

  const createProduct = async () => {
    setSaving(true)
    try {
      await api.productos.crear({ ...form, precio: Number(form.precio) })
      setToast({ type: 'success', message: 'Producto creado correctamente.' })
      setForm(emptyForm)
      setConfirmingCreate(false)
      await load()
    } catch (error) {
      const fields = Object.values(error.fieldErrors || {})
      setToast({ type: 'error', message: fields[0] || error.message })
    } finally {
      setSaving(false)
    }
  }

  const edit = (product) => {
    setEditingId(product.id)
    setForm({
      nombre: product.nombre,
      categoria: product.categoria,
      descripcion: product.descripcion || '',
      precio: String(product.precio),
      disponible: product.disponible,
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const toggleAvailability = async (product) => {
    try {
      await api.productos.disponibilidad(product.id, !product.disponible)
      setToast({ type: 'success', message: `Disponibilidad de ${product.nombre} actualizada.` })
      await load()
    } catch (error) {
      setToast({ type: 'error', message: error.message })
    }
  }

  const deactivate = async (product) => {
    if (!window.confirm(`¿Desactivar "${product.nombre}"? El producto se conservará en el historial.`)) return
    try {
      await api.productos.desactivar(product.id)
      setToast({ type: 'success', message: 'Producto desactivado.' })
      await load()
    } catch (error) {
      setToast({ type: 'error', message: error.message })
    }
  }

  return (
    <>
      <header className="page-header">
        <div>
          <h1>Administrar catálogo</h1>
        </div>
        <div className="metric-card">
          <span>Productos activos</span>
          <strong>{productos.filter((p) => p.activo).length}</strong>
        </div>
      </header>

      <section className="two-column">
        <form className="panel form-panel" onSubmit={handleSubmit}>
          <div className="panel-heading">
            <div>
              <span className="eyebrow">{editingId ? 'Edición' : 'Nuevo registro'}</span>
              <h2>{editingId ? 'Editar producto' : 'Crear producto'}</h2>
            </div>
            {editingId && <button type="button" className="text-button" onClick={() => { setEditingId(null); setForm(emptyForm) }}>Cancelar</button>}
          </div>

          <label>
            Nombre
            <input required maxLength="120" value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} placeholder="Arepa Todo Terreno" />
          </label>
          <div className="form-grid">
            <label>
              Categoría
              <select value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })}>
                <option>Arepas</option>
                <option>Bebidas</option>
                <option>Complementos</option>
                <option>Postres</option>
              </select>
            </label>
            <label>
              Precio COP
              <input required type="number" min="0" max="100000" step="0.01" value={form.precio} onChange={(e) => setForm({ ...form, precio: e.target.value })} placeholder="18000" />
            </label>
          </div>
          <label>
            Descripción
            <textarea maxLength="500" value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} placeholder="Descripción breve del producto" rows="3" />
          </label>
          <label className="check-row">
            <input type="checkbox" checked={form.disponible} onChange={(e) => setForm({ ...form, disponible: e.target.checked })} />
            Disponible para nuevos pedidos
          </label>
          <button className="primary-button" disabled={saving}>{saving ? 'Guardando…' : editingId ? 'Guardar cambios' : 'Crear producto'}</button>
        </form>

        <section className="panel">
          <div className="panel-heading">
            <div>
              <span className="eyebrow">Catálogo vigente</span>
              <h2>Productos</h2>
            </div>
            <input className="search-input" value={filter} onChange={(e) => setFilter(e.target.value)} placeholder="Buscar…" />
          </div>

          {loading ? <div className="empty-state">Cargando catálogo…</div> : (
            <div className="product-list">
              {visibleProducts.map((product) => (
                <article className={`product-row ${!product.activo ? 'disabled' : ''}`} key={product.id}>
                  <div className="product-main">
                    <div className="row-title">
                      <strong>{product.nombre}</strong>
                      <StatusPill active={product.activo}>{product.activo ? 'Activo' : 'Inactivo'}</StatusPill>
                      {product.activo && <StatusPill active={product.disponible}>{product.disponible ? 'Disponible' : 'Agotado'}</StatusPill>}
                    </div>
                    <span>{product.categoria} · {money(product.precio)}</span>
                    <small>Actualizado {dateTime(product.updatedAt)}</small>
                  </div>
                  <div className="row-actions">
                    {product.activo && <>
                      <button className="secondary-button" onClick={() => edit(product)}>Editar</button>
                      <button className="secondary-button" onClick={() => toggleAvailability(product)}>{product.disponible ? 'Marcar agotado' : 'Habilitar'}</button>
                      <button className="danger-button" onClick={() => deactivate(product)}>Desactivar</button>
                    </>}
                  </div>
                </article>
              ))}
              {!visibleProducts.length && <div className="empty-state">No hay productos que coincidan.</div>}
            </div>
          )}
        </section>
      </section>

      {confirmingCreate && (
        <div className="modal-backdrop" onMouseDown={() => !saving && setConfirmingCreate(false)}>
          <section className="modal compact-modal" onMouseDown={(e) => e.stopPropagation()}>
            <span className="eyebrow">Confirmación</span>
            <h2>¿Crear este producto?</h2>
            <div className="confirmation-summary">
              <strong>{form.nombre}</strong>
              <span>{form.categoria} · {money(Number(form.precio))}</span>
            </div>
            <div className="modal-actions">
              <button className="secondary-button" disabled={saving} onClick={() => setConfirmingCreate(false)}>Cancelar</button>
              <button className="primary-button" disabled={saving} onClick={createProduct}>{saving ? 'Creando…' : 'Confirmar creación'}</button>
            </div>
          </section>
        </div>
      )}

      <Toast {...toast} onClose={() => setToast(null)} />
    </>
  )
}
