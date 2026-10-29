# Refactorización de F-02 Registrar pedidos digitales por mesa

Este documento resume la intervención realizada sobre **F-02 Registrar pedidos digitales por mesa**. La refactorización mantiene el contrato REST y el esquema de base de datos, protege el descuento de inventario frente a solicitudes concurrentes y reduce el acoplamiento del módulo `order` con los DTO de entrada y con la entidad JPA de catálogo.

## Resultado

| Hallazgo | Intervención | Estado |
|---|---|---|
| H-01 Actualización perdida de inventario | Descuento SQL atómico condicionado por stock disponible | Verificado |
| H-03 Acoplamiento entre `order`, `catalog` y DTO | Nuevo valor interno `LineaPedido` y persistencia escalar de `productoId` | Verificado |
| H-04 Cobertura insuficiente | Pruebas HTTP y prueba concurrente de la última unidad | Verificado |
| H-02 Creación implícita de mesas | No se modificó porque requiere decisión de negocio | Pendiente |

El diagnóstico completo, con evidencia, impacto, prioridad y propuesta, está disponible en [README_DIAGNOSTICO_F02.md](README_DIAGNOSTICO_F02.md).

## Diseño antes y después

### Antes

```text
PedidoRequest
    └─ PedidoService
         └─ PedidoFactory.ItemConProducto
              ├─ PedidoItemRequest
              └─ Producto entidad JPA de Catalog

InventoryService
    ├─ SELECT stock_actual
    ├─ valida en memoria
    └─ UPDATE stock_actual = valor calculado
```

La fábrica conocía el DTO HTTP y la entidad JPA de otro módulo. El inventario ejecutaba una secuencia de lectura, validación y escritura que permitía que dos transacciones calcularan el descuento desde el mismo stock inicial.

### Después

```text
PedidoRequest + Producto
    └─ PedidoService adapta
         └─ LineaPedido valor interno
              └─ PedidoFactory
                   └─ PedidoItem productoId y fotografía comercial

InventoryService
    └─ UPDATE ingredientes
       SET stock_actual = stock_actual - requerido
       WHERE id = ingrediente AND stock_actual >= requerido
```

`PedidoService` continúa como coordinador transaccional. `LineaPedido` contiene únicamente los datos requeridos para construir el agregado: identificador y nombre del producto, precio vigente, cantidad y observación. `PedidoItem` conserva `productoId` como columna escalar y mantiene la fotografía histórica de nombre, precio y subtotal.

## Archivos principales

- `backend/src/main/java/com/arepaslulu/order/factory/LineaPedido.java`: valor interno independiente del transporte y de JPA.
- `backend/src/main/java/com/arepaslulu/order/factory/PedidoFactory.java`: construye el agregado desde `LineaPedido`.
- `backend/src/main/java/com/arepaslulu/order/domain/PedidoItem.java`: almacena `productoId` sin navegar a `Producto`.
- `backend/src/main/java/com/arepaslulu/order/service/PedidoService.java`: adapta request y catálogo al modelo interno.
- `backend/src/main/java/com/arepaslulu/inventory/repository/InventoryRepository.java`: descuento condicional atómico.
- `backend/src/main/java/com/arepaslulu/inventory/service/InventoryService.java`: comprueba las filas afectadas y conserva el rollback transaccional.
- `backend/src/test/java/com/arepaslulu/order/web/PedidoControllerIntegrationTest.java`: contrato HTTP de creación y consulta.
- `backend/src/test/java/com/arepaslulu/order/service/PedidoConcurrencyIntegrationTest.java`: competencia por la última unidad.

## Comportamiento conservado

- `POST /api/pedidos` continúa devolviendo HTTP 201.
- El backend continúa usando el precio vigente del catálogo.
- Los productos inactivos o no disponibles siguen siendo rechazados.
- El pedido conserva mesa, ítems, observaciones, total y estado `ENVIADO_COCINA`.
- `GET /api/pedidos/{id}` y `GET /api/pedidos?mesa=N` conservan sus respuestas.
- La creación del pedido, el consumo de inventario y la ocupación de mesa continúan en una misma transacción.
- No se requiere una migración Flyway: `pedido_items.producto_id` conserva nombre, tipo y restricción referencial.

## Comportamiento corregido

Cuando dos pedidos intentan consumir simultáneamente la última unidad, la base de datos decide el descuento de forma atómica. Solo uno puede descontar la unidad; el otro recibe `BusinessRuleException` y su transacción no persiste pedido, consumo parcial ni ocupación de mesa.

## Ejecutar las pruebas

Desde la carpeta `backend`:

```bash
mvn test
```

Resultado verificado el 29 de octubre de 2026:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Pruebas nuevas:

- `PedidoControllerIntegrationTest.creaYConsultaPedidoPorIdYPorMesa`
- `PedidoControllerIntegrationTest.rechazaPayloadSinItemsConHttp400`
- `PedidoControllerIntegrationTest.rechazaProductoNoDisponibleConHttp409`
- `PedidoConcurrencyIntegrationTest.soloUnPedidoConsumeLaUltimaUnidadDisponible`

## Decisión pendiente

`MesaService.asegurarMesa` todavía crea automáticamente una mesa inexistente con capacidad 4. No se cambió ese comportamiento porque no existe una regla confirmada que indique si el autoaprovisionamiento es válido. El equipo debe decidir si F-02 solo acepta mesas previamente registradas; después podrá implementarse el rechazo 404 o 409 con una prueba de rollback.

## Evidencia de Git

- Commit de implementación: `5af173e35d517ba585e575ffd5d30576d01425ae`
- Mensaje: `refactor: asegurar pedidos F-02 y documentar cambios`
- Repositorio: <https://github.com/Deim05x/arepas-lulu-prototype>

La versión detallada también se conserva en [`docs/refactorizacion/README.md`](docs/refactorizacion/README.md).
