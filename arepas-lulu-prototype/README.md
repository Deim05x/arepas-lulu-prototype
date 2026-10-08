# Arepas Lulú · sistema operativo integral

Prototipo académico de Software II alineado con el **Plan Vivo de Requisitos**. La solución implementa las ocho funcionalidades vigentes mediante una SPA React, una API Spring Boot y MariaDB, documentadas con C4 y trazabilidad requisito → componente → endpoint → persistencia.

## Funcionalidades implementadas

| ID | Funcionalidad | Módulo |
|---|---|---|
| F-01 | Administrar catálogo de productos y precios | `catalog` |
| F-02 | Registrar pedidos digitales por mesa | `order` |
| F-03 | Gestionar cola de preparación (KDS) | `kitchen` |
| F-04 | Liquidar órdenes, procesar pagos y facturar | `billing` |
| F-05 | Cuadrar caja y reportar facturación DIAN | `reporting` |
| F-06 | Gestionar pedidos a domicilio y clientes | `delivery` |
| F-07 | Controlar stock de ingredientes en tiempo real | `inventory` |
| F-08 | Monitorear ocupación y asignación de mesas | `table` |

## Flujo integrado

1. F-01 mantiene productos, precios y disponibilidad.
2. F-07 mantiene ingredientes y recetas por producto.
3. F-02 confirma un pedido; el backend valida catálogo, descuenta ingredientes de la receta y ocupa la mesa.
4. F-03 mueve el pedido por `ENVIADO_COCINA → EN_PREPARACION → LISTO → SERVIDO`.
5. F-04 registra un único pago, genera factura y libera la mesa.
6. F-05 consolida caja por fecha y marca facturas como reportadas a DIAN (simulación académica, no integración real con el servicio DIAN).
7. F-06 reutiliza el mismo motor de pedidos para `DOMICILIO` y agrega cliente, dirección, despacho y entrega.
8. F-08 muestra el mapa de mesas y permite reservas/inhabilitación, además de los cambios automáticos por pedido/pago.

## Arquitectura

- **Frontend:** React + Vite, SPA modular por funcionalidad.
- **Backend:** Spring Boot / Java 21, monolito modular organizado por feature.
- **Persistencia:** MariaDB + JPA para catálogo/pedidos y Repository con `JdbcTemplate` para módulos operativos.
- **Migraciones:** Flyway.
- **Comunicación:** REST/JSON; SSE para refresco del catálogo.
- **Patrones:** Controller, Service Layer, Repository, DTO/Mapper, Factory, Strategy, Reducer, Observer/Pub-Sub, Dependency Injection.
- **Documentación:** C4 niveles Contexto, Contenedores y Componentes.

Ver `docs/architecture/C4.md` y `docs/architecture/workspace.dsl`.

## Ejecutar con Docker

```bash
cd arepas-lulu-prototype
docker compose up --build
```

- Web: http://localhost:3000
- API: http://localhost:8080/api
- MariaDB: localhost:3306

La migración `V3__full_operations.sql` crea pagos, facturas, clientes, domicilios, ingredientes, recetas y mesas, además de extender `pedidos` con `tipo_servicio`.

## Ejecución local

```bash
# terminal 1
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# terminal 2
cd frontend
npm install
npm run dev
```

El perfil local usa H2 en modo MySQL y ejecuta las mismas migraciones Flyway para conservar el esquema funcional completo.

## Endpoints principales

- `/api/productos` · F-01
- `/api/pedidos` · F-02
- `/api/cocina/pedidos` · F-03
- `/api/pagos` y `/api/facturas` · F-04
- `/api/caja/resumen` y `/api/caja/dian` · F-05
- `/api/clientes` y `/api/domicilios` · F-06
- `/api/inventario/*` · F-07
- `/api/mesas` · F-08

## Reglas integradas destacadas

- El precio final lo calcula el backend desde el catálogo vigente.
- Un producto inactivo o no disponible no puede pedirse.
- Si existe receta, F-07 valida y descuenta ingredientes dentro de la misma transacción del pedido.
- Cocina aplica transiciones de estado controladas.
- Solo se paga una orden lista/servida y no puede pagarse dos veces.
- El pago crea factura y la mesa se libera automáticamente.
- El reporte DIAN del prototipo **marca** facturas reportadas; no envía documentos a un proveedor externo real.

## Verificación

```bash
cd backend && mvn test
cd ../frontend && npm run build
```

La trazabilidad detallada está en `docs/traceability/TRACEABILITY.md`.
