const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
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
    disponibilidad: (id, disponible) => request(`/productos/${id}/disponibilidad`, {
      method: 'PATCH', body: JSON.stringify({ disponible }),
    }),
    desactivar: (id) => request(`/productos/${id}`, { method: 'DELETE' }),
  },
  pedidos: {
    crear: (data) => request('/pedidos', { method: 'POST', body: JSON.stringify(data) }),
    listar: (mesa) => request(`/pedidos${mesa ? `?mesa=${mesa}` : ''}`),
    obtener: (id) => request(`/pedidos/${id}`),
  },
}
