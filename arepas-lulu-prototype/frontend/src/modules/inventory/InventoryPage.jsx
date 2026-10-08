import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api/client.js'
import Toast from '../../components/Toast.jsx'

export default function InventoryPage() {
  const [ingredients, setIngredients] = useState([])
  const [products, setProducts] = useState([])
  const [form, setForm] = useState({ nombre: '', unidad: 'g', stockActual: '', stockMinimo: '' })
  const [productId, setProductId] = useState('')
  const [recipe, setRecipe] = useState([])
  const [ingredientId, setIngredientId] = useState('')
  const [recipeQty, setRecipeQty] = useState('')
  const [toast, setToast] = useState(null)

  const load = async () => { try { const [i, p] = await Promise.all([api.inventario.ingredientes(), api.productos.listar(false)]); setIngredients(i); setProducts(p.filter((x) => x.activo)) } catch (e) { setToast({ type: 'error', message: e.message }) } }
  useEffect(() => { load() }, [])
  useEffect(() => { if (!productId) return setRecipe([]); api.inventario.receta(productId).then(setRecipe).catch((e) => setToast({ type: 'error', message: e.message })) }, [productId])
  const alerts = useMemo(() => ingredients.filter((i) => Number(i.stockActual) <= Number(i.stockMinimo)), [ingredients])

  const create = async (e) => { e.preventDefault(); try { await api.inventario.crearIngrediente({ ...form, stockActual: Number(form.stockActual), stockMinimo: Number(form.stockMinimo || 0) }); setForm({ nombre: '', unidad: 'g', stockActual: '', stockMinimo: '' }); await load() } catch (err) { setToast({ type: 'error', message: err.message }) } }
  const adjust = async (item) => { const value = window.prompt(`Ajuste de stock para ${item.nombre}. Usa negativo para salida.`, '0'); if (value === null) return; try { await api.inventario.ajustarStock(item.id, Number(value)); await load() } catch (e) { setToast({ type: 'error', message: e.message }) } }
  const addRecipe = async () => { if (!productId || !ingredientId || !Number(recipeQty)) return; const next = [...recipe.filter((r) => String(r.ingredienteId) !== String(ingredientId)), { ingredienteId: Number(ingredientId), cantidad: Number(recipeQty) }]; try { const saved = await api.inventario.guardarReceta(productId, next.map((r) => ({ ingredienteId: r.ingredienteId, cantidad: Number(r.cantidad) }))); setRecipe(saved); setIngredientId(''); setRecipeQty(''); setToast({ type: 'success', message: 'Receta actualizada. El próximo pedido descontará estos insumos.' }) } catch (e) { setToast({ type: 'error', message: e.message }) } }

  return <>
    <header className="page-header"><div><span className="eyebrow">F-07 · Stock en tiempo real</span><h1>Inventario de ingredientes</h1><p>Controla existencias y define recetas para descontar insumos automáticamente al confirmar pedidos.</p></div><div className={`metric-card ${alerts.length ? 'danger' : ''}`}><span>Alertas de mínimo</span><strong>{alerts.length}</strong></div></header>
    <section className="inventory-layout">
      <form className="panel compact-form" onSubmit={create}><div className="panel-heading"><div><span className="eyebrow">Maestro</span><h2>Nuevo ingrediente</h2></div></div><label>Nombre<input required value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} /></label><div className="form-grid"><label>Unidad<select value={form.unidad} onChange={(e) => setForm({ ...form, unidad: e.target.value })}><option>g</option><option>kg</option><option>ml</option><option>unidad</option></select></label><label>Stock actual<input required type="number" step="0.001" min="0" value={form.stockActual} onChange={(e) => setForm({ ...form, stockActual: e.target.value })} /></label></div><label>Stock mínimo<input type="number" step="0.001" min="0" value={form.stockMinimo} onChange={(e) => setForm({ ...form, stockMinimo: e.target.value })} /></label><button className="secondary-button">Crear ingrediente</button></form>
      <section className="panel"><div className="panel-heading"><div><span className="eyebrow">Existencias</span><h2>Stock disponible</h2></div></div><div className="stock-grid">{ingredients.map((item) => { const low = Number(item.stockActual) <= Number(item.stockMinimo); return <article className={`stock-card ${low ? 'low' : ''}`} key={item.id}><div><span className={`badge ${low ? 'danger' : 'success'}`}>{low ? 'Stock bajo' : 'Disponible'}</span><h3>{item.nombre}</h3></div><strong>{item.stockActual} <small>{item.unidad}</small></strong><span>Mínimo {item.stockMinimo}</span><button className="text-button" onClick={() => adjust(item)}>Ajustar stock</button></article> })}</div></section>
    </section>
    <section className="panel recipe-panel"><div className="panel-heading"><div><span className="eyebrow">Consumo automático</span><h2>Recetas por producto</h2></div></div><div className="recipe-controls"><label>Producto<select value={productId} onChange={(e) => setProductId(e.target.value)}><option value="">Seleccionar…</option>{products.map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>)}</select></label><label>Ingrediente<select value={ingredientId} onChange={(e) => setIngredientId(e.target.value)}><option value="">Seleccionar…</option>{ingredients.map((i) => <option key={i.id} value={i.id}>{i.nombre}</option>)}</select></label><label>Cantidad por unidad<input type="number" step="0.001" min="0.001" value={recipeQty} onChange={(e) => setRecipeQty(e.target.value)} /></label><button className="primary-button" onClick={addRecipe}>Agregar / actualizar</button></div><div className="recipe-list">{recipe.map((r) => <div key={r.ingredienteId}><strong>{r.ingredienteNombre}</strong><span>{r.cantidad} {r.unidad} por producto</span></div>)}{productId && !recipe.length && <div className="empty-state">Este producto todavía no tiene receta; no descontará ingredientes.</div>}</div></section>
    <Toast {...toast} onClose={() => setToast(null)} />
  </>
}
