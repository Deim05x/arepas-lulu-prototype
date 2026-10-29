# Diagnóstico y cierre de refactorización de F-02

## 1. Contexto

- **Proyecto:** Arepas Lulú.
- **Funcionalidad:** F-02 · Registrar pedidos digitales por mesa.
- **Arquitectura:** SPA React, API Spring Boot organizada como monolito modular y MariaDB/H2 con Flyway.
- **Trazabilidad:** F-02 → BR-02 → StR-02 → SyR-02 → SWR-06…SWR-11.
- **Alcance inspeccionado:** controlador, servicio, fábrica, agregado, repositorios, inventario, mesas y pruebas.

## 2. Arquitectura real

```text
OrderPage
  → POST /api/pedidos
  → PedidoController
  → PedidoService  @Transactional
      → ProductoRepository
      → InventoryService
      → PedidoFactory
      → PedidoRepository
      → MesaService
```

El controlador delega correctamente. `PedidoService` coordina el caso de uso y delimita la transacción que une validación de catálogo, consumo de inventario, persistencia del pedido y ocupación de mesa.

## 3. Hallazgos

### H-01 · riesgo de actualización perdida en inventario

- **Clasificación:** riesgo arquitectónico con posible defecto funcional concurrente.
- **Evidencia inicial:** `InventoryService.consumirParaPedido` leía `stock_actual`, validaba en memoria y posteriormente escribía un valor absoluto mediante `InventoryRepository.establecerStock`.
- **Impacto:** dos pedidos simultáneos podían aprobar el consumo desde el mismo stock inicial y aceptar más unidades de las disponibles.
- **Prioridad:** 1.
- **Intervención:** `descontarStockSiDisponible` ejecuta un `UPDATE` atómico con la condición `stock_actual >= cantidad` y devuelve las filas afectadas.
- **Verificación:** `PedidoConcurrencyIntegrationTest` confirma que, con stock inicial 1 y dos pedidos simultáneos, uno tiene éxito, otro es rechazado y el stock final es 0.
- **Estado:** verificado.

### H-02 · creación implícita de una mesa inexistente

- **Clasificación:** riesgo de regla de negocio y separación de responsabilidades.
- **Evidencia:** `MesaService.asegurarMesa` crea cualquier número positivo que no exista y asigna capacidad 4.
- **Impacto:** un error de digitación puede crear una mesa y asociar el pedido a ella.
- **Prioridad:** 3.
- **Acción:** no se modificó. Requiere confirmar si el autoaprovisionamiento forma parte del comportamiento esperado.
- **Estado:** pendiente de decisión de negocio.

### H-03 · acoplamiento entre Order, Catalog y DTO

- **Clasificación:** deuda técnica de arquitectura.
- **Evidencia inicial:** `PedidoFactory.ItemConProducto` dependía de `PedidoItemRequest` y `Producto`; `PedidoItem` mantenía una asociación JPA directa con `Producto`.
- **Impacto:** los cambios del contrato HTTP o de la entidad de catálogo se propagaban a la fábrica y al agregado.
- **Prioridad:** 4.
- **Intervención:** se creó `LineaPedido`; `PedidoService` realiza la adaptación y `PedidoItem` persiste `productoId` como escalar junto a la fotografía comercial.
- **Verificación:** las pruebas de fábrica, flujo completo, F-01/F-02 y HTTP conservan ID, nombre, precio, subtotal y total.
- **Estado:** verificado.

### H-04 · pruebas y trazabilidad incompletas

- **Clasificación:** deuda técnica de calidad.
- **Evidencia inicial:** `PrototypeFlowIntegrationTest` creaba pedidos a nivel de servicio, pero no verificaba el controlador, los códigos HTTP ni las consultas por ID y mesa declaradas en SWR-11.
- **Impacto:** una refactorización podía romper el contrato REST sin que la suite lo detectara.
- **Prioridad:** 2.
- **Intervención:** se agregaron tres pruebas de integración HTTP y una prueba concurrente.
- **Verificación:** 10 pruebas ejecutadas, 0 fallos, 0 errores y 0 omitidas.
- **Estado:** verificado.

## 4. Priorización

| ID | Impacto | Riesgo | Esfuerzo | Prioridad | Estado final |
|---|---:|---:|---:|---:|---|
| H-01 | 3 | 3 | 2 | 1 | Implementado y verificado |
| H-04 | 3 | 2 | 1 | 2 | Implementado y verificado |
| H-02 | 2 | 2 | 1 | 3 | Pendiente de decisión |
| H-03 | 2 | 2 | 3 | 4 | Implementado y verificado |

## 5. Intervención

La refactorización conserva los endpoints, los DTO de respuesta y las tablas existentes. No añade una migración de base de datos. El cambio se concentra en dos decisiones:

1. Mover la invariante de stock al `UPDATE` de la base de datos para que la comprobación y el descuento sean una sola operación atómica.
2. Introducir un valor interno para que la fábrica y el agregado no dependan del DTO HTTP ni de la entidad JPA de catálogo.

## 6. Verificación

Comando ejecutado:

```bash
cd backend
mvn test
```

Resultado:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Cobertura funcional conservada:

- creación del pedido con estado `ENVIADO_COCINA`;
- cálculo autoritativo de precio y total;
- persistencia de ítems y observaciones;
- rechazo de productos no disponibles;
- consumo de receta y ocupación de mesa;
- consulta HTTP por ID y por mesa;
- validación HTTP 400 y regla de negocio HTTP 409;
- exclusión mutua efectiva sobre la última unidad disponible.

## 7. Cierre

La intervención mejora consistencia y modularidad sin cambiar el contrato observable de F-02. La principal deuda pendiente es decidir si una orden puede crear automáticamente una mesa. En el proceso de desarrollo, una revisión de invariantes acompañada de pruebas concurrentes y de contrato HTTP habría detectado los riesgos antes de la entrega.

## 8. Git

El informe queda preparado para versionarse junto con la intervención. Los hashes de commit deben agregarse después de que el equipo revise y confirme el cambio.
