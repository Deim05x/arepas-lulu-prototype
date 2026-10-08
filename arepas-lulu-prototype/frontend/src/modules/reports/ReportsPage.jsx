import { useEffect, useState } from 'react'
import { api } from '../../api/client.js'
import { money, dateTime } from '../../utils/format.js'
import Toast from '../../components/Toast.jsx'

const today = () => new Date().toISOString().slice(0, 10)

export default function ReportsPage() {
  const [date, setDate] = useState(today())
  const [data, setData] = useState(null)
  const [toast, setToast] = useState(null)
  const load = async () => { try { setData(await api.caja.resumen(date)) } catch (e) { setToast({ type: 'error', message: e.message }) } }
  useEffect(() => { load() }, [date])
  const report = async () => { try { const r = await api.caja.dian(date); setToast({ type: 'success', message: `${r.reportadas} factura(s) marcadas como reportadas a DIAN.` }); await load() } catch (e) { setToast({ type: 'error', message: e.message }) } }

  return <>
    <header className="page-header"><div><span className="eyebrow">F-05 · Control administrativo</span><h1>Caja y facturación DIAN</h1><p>Consolida ventas por fecha, medios de pago y estado de reporte fiscal.</p></div><label className="date-filter">Fecha<input type="date" value={date} onChange={(e) => setDate(e.target.value)} /></label></header>
    <section className="metrics-grid">
      <div className="stat-card"><span>Ventas</span><strong>{money(data?.total || 0)}</strong><small>{data?.transacciones || 0} transacciones</small></div>
      <div className="stat-card"><span>Efectivo</span><strong>{data?.efectivo || 0}</strong><small>pagos registrados</small></div>
      <div className="stat-card"><span>Tarjeta / transferencia</span><strong>{(data?.tarjeta || 0) + (data?.transferencia || 0)}</strong><small>pagos electrónicos</small></div>
      <div className={`stat-card ${data?.pendientesDian ? 'warning' : 'success'}`}><span>Pendientes DIAN</span><strong>{data?.pendientesDian || 0}</strong><button className="text-button" onClick={report}>Reportar jornada</button></div>
    </section>
    <section className="panel">
      <div className="panel-heading"><div><span className="eyebrow">Libro de ventas</span><h2>Facturas del día</h2></div><button className="secondary-button" onClick={load}>Actualizar</button></div>
      <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Factura</th><th>Pedido</th><th>Total</th><th>Hora</th><th>DIAN</th></tr></thead><tbody>{data?.facturas?.map((f) => <tr key={f.id}><td><strong>{f.numero}</strong></td><td>#{f.pedidoId}</td><td>{money(f.total)}</td><td>{dateTime(f.createdAt)}</td><td><span className={`badge ${f.reportadaDian ? 'success' : 'warning'}`}>{f.reportadaDian ? 'Reportada' : 'Pendiente'}</span></td></tr>)}{!data?.facturas?.length && <tr><td colSpan="5" className="empty-cell">Sin facturas para la fecha seleccionada.</td></tr>}</tbody></table></div>
    </section>
    <Toast {...toast} onClose={() => setToast(null)} />
  </>
}
