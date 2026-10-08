# Trazabilidad de requisitos → implementación

La fuente de alcance es el Plan Vivo actualizado con F-01 a F-08. Este archivo no reemplaza el Excel: documenta dónde se materializa cada funcionalidad en código.

## F-01 · Administrar catálogo de productos y precios

Cadena refinada existente: **F-01 → BR-01 → StR-01 → SyR-01 → SWR-01…SWR-05**.

- Frontend: `frontend/src/modules/catalog/ProductCatalogPage.jsx`
- Backend: `catalog/web/ProductoController.java` → `catalog/service/ProductoService.java` → `ProductoRepository`
- Persistencia: `productos`
- Endpoints: `GET/POST/PUT /api/productos`, `PATCH /disponibilidad`, `DELETE`
- Integración: F-02 usa precio/disponibilidad autoritativos; SSE refresca catálogo.

## F-02 · Registrar pedidos digitales por mesa

Cadena refinada existente: **F-02 → BR-02 → StR-02 → SyR-02 → SWR-06…SWR-11**.

- Frontend: `frontend/src/modules/orders/OrderPage.jsx` + `orderReducer.js`
- Backend: `order/web/PedidoController.java` → `PedidoService` → `PedidoFactory/PricingStrategy` → `PedidoRepository`
- Persistencia: `pedidos`, `pedido_items`
- Endpoint: `POST/GET /api/pedidos`
- Integración nueva: consume F-07 por receta y ocupa F-08 para pedidos `MESA`.

## F-03 · Gestionar cola de preparación (KDS)

- Frontend: `frontend/src/modules/kitchen/KitchenPage.jsx`
- Backend: `kitchen/web/CocinaController.java` → `CocinaService` → `PedidoRepository`
- Endpoint: `GET /api/cocina/pedidos`, `PATCH /api/cocina/pedidos/{id}/estado`
- Estados controlados: `ENVIADO_COCINA → EN_PREPARACION → LISTO → SERVIDO` y cancelación antes de servir.
- Persistencia: columna `pedidos.estado`.

## F-04 · Liquidar órdenes, procesar pagos y facturar

- Frontend: `frontend/src/modules/billing/BillingPage.jsx`
- Backend: `billing/web/BillingController.java` → `BillingService` → `BillingRepository`
- Endpoints: `POST /api/pagos`, `GET /api/facturas/pedido/{pedidoId}`
- Reglas: orden debe estar `LISTO/SERVIDO`, un solo pago por pedido, cambio en efectivo, factura única.
- Persistencia: `pagos`, `facturas`; pedido pasa a `PAGADO`; F-08 libera mesa.

## F-05 · Cuadrar caja y reportar facturación DIAN

- Frontend: `frontend/src/modules/reports/ReportsPage.jsx`
- Backend: `reporting/web/ReporteController.java` → `ReporteService` → `BillingRepository`
- Endpoints: `GET /api/caja/resumen`, `POST /api/caja/dian`
- Persistencia: agrega `pagos/facturas`; actualiza `facturas.reportada_dian`.
- Alcance: el reporte DIAN es una **simulación académica de estado**, no una integración real con el proveedor/servicio de facturación electrónica.

## F-06 · Gestionar pedidos a domicilio y clientes

- Frontend: `frontend/src/modules/delivery/DeliveryPage.jsx`
- Backend: `delivery/web/DeliveryController.java` → `DeliveryService` → `DeliveryRepository`
- Endpoints: `/api/clientes`, `/api/domicilios`, `/api/domicilios/{id}/estado`
- Persistencia: `clientes`, `domicilios` + pedido reutilizado con `tipo_servicio=DOMICILIO`.
- Integración: crear domicilio invoca F-02 para mantener una sola lógica de precios, disponibilidad e inventario.

## F-07 · Controlar stock de ingredientes en tiempo real

- Frontend: `frontend/src/modules/inventory/InventoryPage.jsx`
- Backend: `inventory/web/InventoryController.java` → `InventoryService` → `InventoryRepository`
- Endpoints: ingredientes, ajustes y recetas bajo `/api/inventario`.
- Persistencia: `ingredientes`, `recetas`.
- Integración: dentro de la transacción de F-02 se calculan requerimientos `cantidad receta × cantidad pedida`; si falta stock se rechaza el pedido y si alcanza se descuenta.

## F-08 · Monitorear ocupación y asignación de mesas

- Frontend: `frontend/src/modules/tables/TablesPage.jsx`
- Backend: `table/web/MesaController.java` → `MesaService` → `MesaRepository`
- Endpoint: `GET/PATCH /api/mesas`.
- Estados: `DISPONIBLE`, `OCUPADA`, `RESERVADA`, `INACTIVA`.
- Integración: F-02 ocupa automáticamente y F-04 libera automáticamente.

## Vista transversal

```text
F-01 catálogo ─┐
F-07 inventario ├─> F-02 pedido ─> F-03 cocina ─> F-04 pago/factura ─> F-05 caja/DIAN
F-08 mesas ────┘       ↑                         │
                        └──── F-06 domicilio ─────┘
```

La arquitectura evita duplicar reglas: catálogo e inventario se validan en servidor, domicilio reutiliza `PedidoService` y pago/factura usa el mismo agregado `Pedido` que cocina.
