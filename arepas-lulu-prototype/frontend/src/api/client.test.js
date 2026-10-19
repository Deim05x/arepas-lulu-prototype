import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from './client.js'

afterEach(() => {
  vi.restoreAllMocks()
})

describe('API client', () => {
  it('lists products from the catalog endpoint', async () => {
    const products = [{ id: 1, nombre: 'Arepa de queso' }]
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => products,
    })

    await expect(api.productos.listar()).resolves.toEqual(products)
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/productos?soloDisponibles=false',
      expect.objectContaining({ headers: { 'Content-Type': 'application/json' } }),
    )
  })

  it('sends new orders as JSON to the orders endpoint', async () => {
    const order = { mesaNumero: 4, items: [{ productoId: 1, cantidad: 2 }] }
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 201,
      json: async () => ({ id: 10, ...order }),
    })

    await api.pedidos.crear(order)

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/pedidos',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(order),
      }),
    )
  })
})
