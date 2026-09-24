export const initialOrder = {
  mesaNumero: '',
  items: [],
}

export function orderReducer(state, action) {
  switch (action.type) {
    case 'SET_MESA':
      return { ...state, mesaNumero: action.value }
    case 'ADD_ITEM':
      return { ...state, items: [...state.items, action.item] }
    case 'UPDATE_ITEM':
      return {
        ...state,
        items: state.items.map((item) => item.localId === action.localId ? { ...item, ...action.patch } : item),
      }
    case 'SYNC_CATALOG': {
      const byId = new Map(action.products.map((product) => [String(product.id), product]))
      return {
        ...state,
        items: state.items.map((item) => {
          const product = byId.get(String(item.productoId))
          return product
            ? { ...item, nombre: product.nombre, precio: product.precio, unavailable: false }
            : { ...item, unavailable: true }
        }),
      }
    }
    case 'REMOVE_ITEM':
      return { ...state, items: state.items.filter((item) => item.localId !== action.localId) }
    case 'CLEAR_ITEMS':
      return { ...state, items: [] }
    case 'RESET':
      return initialOrder
    default:
      return state
  }
}
