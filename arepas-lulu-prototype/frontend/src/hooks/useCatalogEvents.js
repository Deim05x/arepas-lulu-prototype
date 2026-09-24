import { useEffect, useRef } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export function useCatalogEvents(onChanged) {
  const callbackRef = useRef(onChanged)
  callbackRef.current = onChanged

  useEffect(() => {
    const source = new EventSource(`${API_URL}/productos/stream`)
    const handler = () => callbackRef.current()
    source.addEventListener('catalog-changed', handler)
    return () => {
      source.removeEventListener('catalog-changed', handler)
      source.close()
    }
  }, [])
}
