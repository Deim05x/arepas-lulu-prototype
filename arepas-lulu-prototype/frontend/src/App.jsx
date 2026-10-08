import { useState } from 'react'
import ProductCatalogPage from './modules/catalog/ProductCatalogPage.jsx'
import OrderPage from './modules/orders/OrderPage.jsx'
import KitchenPage from './modules/kitchen/KitchenPage.jsx'
import BillingPage from './modules/billing/BillingPage.jsx'
import ReportsPage from './modules/reports/ReportsPage.jsx'
import DeliveryPage from './modules/delivery/DeliveryPage.jsx'
import InventoryPage from './modules/inventory/InventoryPage.jsx'
import TablesPage from './modules/tables/TablesPage.jsx'

const tabs = [
  { id: 'catalogo', code: 'F-01', label: 'Catálogo', icon: '◫' },
  { id: 'pedidos', code: 'F-02', label: 'Pedidos por mesa', icon: '✎' },
  { id: 'cocina', code: 'F-03', label: 'Cocina · KDS', icon: '♨' },
  { id: 'facturacion', code: 'F-04', label: 'Pagos y factura', icon: '$' },
  { id: 'reportes', code: 'F-05', label: 'Caja y DIAN', icon: '▥' },
  { id: 'domicilios', code: 'F-06', label: 'Domicilios', icon: '⌂' },
  { id: 'inventario', code: 'F-07', label: 'Inventario', icon: '▦' },
  { id: 'mesas', code: 'F-08', label: 'Mesas', icon: '◎' },
]

function Page({ tab }) {
  switch (tab) {
    case 'catalogo': return <ProductCatalogPage />
    case 'pedidos': return <OrderPage />
    case 'cocina': return <KitchenPage />
    case 'facturacion': return <BillingPage />
    case 'reportes': return <ReportsPage />
    case 'domicilios': return <DeliveryPage />
    case 'inventario': return <InventoryPage />
    case 'mesas': return <TablesPage />
    default: return <ProductCatalogPage />
  }
}

export default function App() {
  const [tab, setTab] = useState('catalogo')
  const active = tabs.find((item) => item.id === tab)

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
              <circle className="arepa-detail" cx="40" cy="33" r="2" />
            </svg>
          </div>
          <div><strong>Arepas Lulú</strong><span>Operación integral</span></div>
        </div>

        <div className="nav-caption">Plan Vivo · 8 funcionalidades</div>
        <nav className="nav-list" aria-label="Funcionalidades">
          {tabs.map((item) => (
            <button key={item.id} className={`nav-item ${tab === item.id ? 'active' : ''}`} onClick={() => setTab(item.id)}>
              <span className="nav-icon">{item.icon}</span>
              <span className="nav-copy"><small>{item.code}</small><strong>{item.label}</strong></span>
            </button>
          ))}
        </nav>
        <div className="sidebar-footer"><span className="live-dot" /> Spring Boot · MariaDB · React</div>
      </aside>

      <main className="main-content">
        <div className="topbar">
          <div><span className="breadcrumb">Arepas Lulú / {active?.code}</span></div>
          <div className="architecture-chip">C4 · Monolito modular</div>
        </div>
        <Page tab={tab} />
      </main>
    </div>
  )
}
