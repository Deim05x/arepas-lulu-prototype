# Evidencias por SWR

| SWR | Evidencia de ejecución | Evidencia documental | Estado |
|---|---|---|---|
| SWR-01 | Alta de producto HTTP 201 y fila visible | `TRACEABILITY.md`, captura 01 | Verificado en servicio; HTTP pendiente |
| SWR-02 | Precio actualizado y usado en pedido | `TRACEABILITY.md`, capturas 01 y 05 | Verificado en integración; HTTP pendiente |
| SWR-03 | PATCH de disponibilidad | `TRACEABILITY.md`, captura 02 | Verificado en integración; HTTP pendiente |
| SWR-04 | Pedido de producto no disponible rechazado con 409 | `PrototypeFlowIntegrationTest`, smoke test | Verificado en integración; HTTP pendiente |
| SWR-05 | `updatedAt` actualizado en producto | `Producto`, captura 01 | Pendiente de captura explícita |
| SWR-06 | ID único del pedido | `PrototypeFlowIntegrationTest`, captura 05 | Verificado en integración; HTTP pendiente |
| SWR-07 | Pedido asociado a mesa 4 | `PrototypeFlowIntegrationTest`, captura 05 | Verificado en integración; HTTP pendiente |
| SWR-08 | Ítems, cantidades y observación persistidos | `PrototypeFlowIntegrationTest`, captura 05 | Verificado en integración; HTTP pendiente |
| SWR-09 | Resumen antes del POST | `OrderPage`, captura 04 | Pendiente de captura |
| SWR-10 | Estado `ENVIADO_COCINA` | `PrototypeFlowIntegrationTest`, captura 05 | Verificado en integración; HTTP pendiente |
| SWR-11 | Consulta por ID y por mesa | `PedidoController`, captura 06 | Verificado en integración; HTTP pendiente |

## Regla de evidencia

Una fila solo se marca como completamente cerrada cuando existe: prueba automatizada o manual reproducible, salida/resultado conservado y captura o referencia de código que permita auditar el SWR.