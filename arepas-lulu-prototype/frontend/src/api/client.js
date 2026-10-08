const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options,
    })
  } catch (error) {
    if (error instanceof TypeError) {
      const connectionError = new Error('No se pudo conectar con la API. Inicia el backend y vuelve a intentarlo.')
      connectionError.status = 0
      throw connectionError
    }
    throw error
  }
  if (response.status === 204) return null
  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    const error = new Error(payload?.message || `Error HTTP ${response.status}`)
    error.status = response.status
    error.fieldErrors = payload?.fieldErrors || {}
    throw error
  }
  return payload
}

export const api = {
  productos: {
    listar: (soloDisponibles = false) => request(`/productos?soloDisponibles=${soloDisponibles}`),
    crear: (data) => request('/productos', { method: 'POST', body: JSON.stringify(data) }),
    actualizar: (id, data) => request(`/productos/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    disponibilidad: (id, disponible) => request(`/productos/${id}/disponibilidad`, { method: 'PATCH', body: JSON.stringify({ disponible }) }),
    desactivar: (id) => request(`/productos/${id}`, { method: 'DELETE' }),
  },
  pedidos: {
    crear: (data) => request('/pedidos', { method: 'POST', body: JSON.stringify(data) }),
    listar: (mesa) => request(`/pedidos${mesa ? `?mesa=${mesa}` : ''}`),
    obtener: (id) => request(`/pedidos/${id}`),
  },
  cocina: {
    listar: () => request('/cocina/pedidos'),
    estado: (id, estado) => request(`/cocina/pedidos/${id}/estado`, { method: 'PATCH', body: JSON.stringify({ estado }) }),
  },
  pagos: {
    crear: (data) => request('/pagos', { method: 'POST', body: JSON.stringify(data) }),
    factura: (pedidoId) => request(`/facturas/pedido/${pedidoId}`),
  },
  caja: {
    resumen: (desde, hasta = desde) => request(`/caja/resumen?desde=${desde}&hasta=${hasta}`),
    dian: (fecha) => request(`/caja/dian?fecha=${fecha}`, { method: 'POST' }),
  },
  clientes: {
    listar: () => request('/clientes'),
    crear: (data) => request('/clientes', { method: 'POST', body: JSON.stringify(data) }),
  },
  domicilios: {
    listar: () => request('/domicilios'),
    crear: (data) => request('/domicilios', { method: 'POST', body: JSON.stringify(data) }),
    estado: (id, data) => request(`/domicilios/${id}/estado`, { method: 'PATCH', body: JSON.stringify(data) }),
  },
  inventario: {
    ingredientes: () => request('/inventario/ingredientes'),
    crearIngrediente: (data) => request('/inventario/ingredientes', { method: 'POST', body: JSON.stringify(data) }),
    ajustarStock: (id, delta) => request(`/inventario/ingredientes/${id}/stock`, { method: 'PATCH', body: JSON.stringify({ delta }) }),
    receta: (productoId) => request(`/inventario/recetas/${productoId}`),
    guardarReceta: (productoId, items) => request(`/inventario/recetas/${productoId}`, { method: 'PUT', body: JSON.stringify(items) }),
  },
  mesas: {
    listar: () => request('/mesas'),
    estado: (numero, data) => request(`/mesas/${numero}`, { method: 'PATCH', body: JSON.stringify(data) }),
  },
}
