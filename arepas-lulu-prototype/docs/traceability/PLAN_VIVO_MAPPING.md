# Correspondencia con el Plan Vivo de Requisitos

Este documento conserva el texto consolidado de las filas **F-01** y **F-02** de la plantilla actualizada y muestra cómo se aterriza en el prototipo.

## F-01 — Administrar catálogo de productos y precios

### Plan Vivo

- **BR:** Mantener consistencia en la oferta y asegurar que los cobros correspondan a los precios actualizados.
- **StR:** La administración necesita crear, editar y ocultar productos y modificar sus precios centralizadamente.
- **SyR:** El sistema debe proveer una interfaz para la gestión del maestro de artículos (CRUD) y sincronizar cambios en tiempo real.
- **SWR consolidado:** El backend debe exponer endpoints RESTful (POST, PUT, DELETE, GET) para la entidad Producto y validar stock.
- **Verificación:** Crear un producto “Arepa Todo Terreno”, asignarle precio y verificar que aparece en la interfaz de pedidos.
- **PBI/User Story:** US-01 — Administrar menú e inventario.
- **Jira:** SCRUM-6.

### Decisión del prototipo

El concepto de “validar stock” se implementa en este incremento como **disponibilidad operativa del producto** (`disponible`). No se descuenta materia prima ni receta porque ese comportamiento pertenece a F-07 “Controlar stock de ingredientes en tiempo real”.

La sincronización inmediata se implementa con **Server-Sent Events (SSE)**. Cuando F-01 cambia, los navegadores conectados reciben `catalog-changed` y vuelven a consultar el catálogo.

## F-02 — Registrar pedidos digitales por mesa

### Plan Vivo

- **BR:** Eliminar errores de escritura, omisiones en pedidos y reducir tiempos de espera.
- **StR:** El mesero necesita una interfaz ágil (móvil) para seleccionar productos, agregar observaciones y enviarlos a preparación.
- **SyR:** El sistema debe permitir abrir órdenes vinculadas a una mesa, agregar ítems y cambiar el estado a “Enviado a cocina”.
- **SWR consolidado:** La app cliente debe gestionar el estado localmente antes de enviar el payload JSON al servidor para persistir la transacción.
- **Verificación:** Registrar una orden compleja (ítems con notas especiales) y comprobar que llega íntegra a cocina.
- **PBI/User Story:** US-02 — Registrar pedido digital en mesa.
- **Jira:** SCRUM-5.

### Decisión del prototipo

React usa `useReducer` para mantener el pedido **solo en memoria del navegador** durante su construcción. El POST a `/api/pedidos` ocurre únicamente después del resumen de confirmación. El backend asigna el ID, recupera los precios vigentes del catálogo, valida que cada producto continúe activo/disponible y persiste el agregado con estado `ENVIADO_COCINA`.

## Cadena hasta código

```text
F-01 → BR-01 → StR-01 → SyR-01
     → SWR-01..05
     → ProductCatalogPage / ProductoController / ProductoService / ProductoRepository
     → productos

F-02 → BR-02 → StR-02 → SyR-02
     → SWR-06..11
     → OrderPage / PedidoController / PedidoService / PedidoFactory / PricingStrategy
     → pedidos + pedido_items
```
