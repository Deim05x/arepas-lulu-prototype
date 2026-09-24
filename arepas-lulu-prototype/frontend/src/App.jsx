import { useState } from 'react'
import ProductCatalogPage from './modules/catalog/ProductCatalogPage.jsx'
import OrderPage from './modules/orders/OrderPage.jsx'

const tabs = [
  { id: 'catalogo', label: 'Catálogo' },
  { id: 'pedidos', label: 'Pedido por mesa' },
]

export default function App() {
  const [tab, setTab] = useState('catalogo')

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark" aria-label="Arepa" role="img">
            <svg viewBox="0 0 64 64" aria-hidden="true">
              <path className="arepa-shadow" d="M11 34c0-13 9-23 21-23s21 10 21 23-9 21-21 21-21-8-21-21Z" />
              <path className="arepa-body" d="M8 29C8 17 18 8 32 8s24 9 24 21-10 22-24 22S8 41 8 29Z" />
              <path className="arepa-highlight" d="M18 22c4-6 10-9 17-9 6 0 11 2 15 6-5-2-10-3-15-3-6 0-12 2-17 6Z" />
              <circle className="arepa-detail" cx="24" cy="29" r="2" />
              <circle className="arepa-detail" cx="34" cy="23" r="1.7" />
              <circle className="arepa-detail" cx="40" cy="33" r="2" />
              <circle className="arepa-detail" cx="28" cy="39" r="1.5" />
            </svg>
          </div>
          <div>
            <strong>Arepas Lulú</strong>
            <span>Prototipo operativo</span>
          </div>
        </div>

        <nav className="nav-list" aria-label="Funcionalidades">
          {tabs.map((item) => (
            <button
              key={item.id}
              className={`nav-item ${tab === item.id ? 'active' : ''}`}
              onClick={() => setTab(item.id)}
            >
              <strong>{item.label}</strong>
            </button>
          ))}
        </nav>

      </aside>

      <main className="main-content">
        {tab === 'catalogo' ? <ProductCatalogPage /> : <OrderPage />}
      </main>
    </div>
  )
}
