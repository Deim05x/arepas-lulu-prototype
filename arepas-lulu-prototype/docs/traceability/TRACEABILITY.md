# Trazabilidad del prototipo

La implementación conserva los identificadores usados en el Plan Vivo.

## F-01 — Administrar catálogo de productos y precios

| Nivel | Requisito | Implementación |
|---|---|---|
| BR-01 | Mantener consistencia en la oferta y asegurar cobros con precios actualizados. | Módulo `catalog` + persistencia `productos`. |
| StR-01 | Administración crea, edita, oculta productos y modifica precios centralmente. | `ProductCatalogPage` + `ProductoController` + `ProductoService`. |
| SyR-01 | Gestionar maestro de artículos y reflejar cambios en la interfaz. | API REST + React refresca catálogo después de mutaciones. |
| SWR-01 | Registrar producto con datos básicos. | `POST /api/productos`. |
| SWR-02 | Definir y actualizar precio vigente. | `POST/PUT /api/productos`; precio almacenado en `DECIMAL(12,2)`. |
| SWR-03 | Marcar producto disponible/no disponible. | `PATCH /api/productos/{id}/disponibilidad`. |
| SWR-04 | Impedir seleccionar productos no disponibles. | `GET ?soloDisponibles=true` + validación backend al crear pedido. |
| SWR-05 | Registrar última actualización. | `productos.updated_at` con callbacks JPA. |

### Verificación F-01

- Crear `Arepa Todo Terreno` y comprobar que aparece en catálogo.
- Cambiar su precio y comprobar el valor actualizado.
- Cambiar `disponible=false` y comprobar que no aparece como seleccionable en F-02.
- Intentar enviar manualmente un pedido con producto no disponible y comprobar rechazo `409`.

## F-02 — Registrar pedidos digitales por mesa

| Nivel | Requisito | Implementación |
|---|---|---|
| BR-02 | Eliminar errores de escritura/omisiones y reducir reprocesos. | Pedido estructurado y persistente. |
| StR-02 | Mesero selecciona productos, cantidades, observaciones y envía a preparación. | `OrderPage` con `useReducer`. |
| SyR-02 | Abrir orden por mesa, agregar ítems y dejarla `ENVIADO_COCINA`. | `PedidoService#create`. |
| SWR-06 | Generar identificador único. | PK autoincremental MariaDB. |
| SWR-07 | Asociar pedido a una mesa. | `pedidos.mesa_numero`. |
| SWR-08 | Registrar productos, cantidades y preferencias. | `pedido_items`. |
| SWR-09 | Mostrar resumen antes de enviar. | Paso de confirmación en React. |
| SWR-10 | Guardar pedido confirmado con estado. | `pedidos.estado = ENVIADO_COCINA`. |
| SWR-11 | Consultar pedido por mesa o ID. | `GET /api/pedidos`, `GET /{id}`, filtro `mesa`. |

### Verificación F-02

- Construir un pedido con al menos 2 ítems y una observación.
- Confirmar el resumen.
- Comprobar respuesta con ID, mesa, total y estado `ENVIADO_COCINA`.
- Consultar el pedido por ID.
- Consultar la mesa y comprobar que aparece en el historial.

## Matriz técnica

| SWR | Componente backend | Frontend | Endpoint | Persistencia | Prueba |
|---|---|---|---|---|---|
| SWR-01 | ProductoService | ProductForm | POST `/productos` | `productos` | Alta válida |
| SWR-02 | ProductoService | ProductForm | PUT `/productos/{id}` | `productos.precio` | Cambio de precio |
| SWR-03 | ProductoService | ProductTable | PATCH `/disponibilidad` | `productos.disponible` | Toggle disponibilidad |
| SWR-04 | PedidoService | OrderPage | GET disponibles + POST pedido | `productos` | Rechazo no disponible |
| SWR-05 | Producto | ProductTable | mutaciones producto | `updated_at` | Timestamp cambia |
| SWR-06 | PedidoRepository | OrderSuccess | POST `/pedidos` | `pedidos.id` | ID único |
| SWR-07 | PedidoService | OrderPage | POST `/pedidos` | `mesa_numero` | Asociación mesa |
| SWR-08 | PedidoFactory | OrderCart | POST `/pedidos` | `pedido_items` | Ítems y notas |
| SWR-09 | — | OrderSummary | antes de POST | estado local | Confirmación UI |
| SWR-10 | PedidoFactory | OrderSuccess | POST `/pedidos` | `estado` | Estado final |
| SWR-11 | PedidoQuery | OrderHistory | GET `/pedidos` | lectura | Consulta ID/mesa |
