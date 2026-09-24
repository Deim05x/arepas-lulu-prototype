# Arepas Lulú — Prototipo funcional F-01 + F-02

Prototipo alineado con el **Plan Vivo de Requisitos** de Software II.

## Alcance implementado

- **F-01 — Administrar catálogo de productos y precios**
  - Crear productos.
  - Consultar catálogo.
  - Editar nombre, categoría, descripción y precio.
  - Cambiar disponibilidad operativa.
  - Desactivar/ocultar productos mediante borrado lógico.
  - Los productos inactivos/no disponibles no pueden agregarse a nuevos pedidos.
- **F-02 — Registrar pedidos digitales por mesa**
  - Seleccionar mesa.
  - Agregar productos, cantidades y observaciones.
  - Mantener el pedido en estado local en React antes de enviarlo.
  - Mostrar resumen de confirmación.
  - Persistir el pedido como `ENVIADO_COCINA`.
  - Consultar pedidos registrados por identificador o mesa.

> Nota de alcance: la gestión detallada de inventario de ingredientes pertenece a **F-07**. En F-01 se maneja la disponibilidad operativa del producto, suficiente para impedir su selección cuando no está disponible.

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA / Hibernate
- Bean Validation
- Flyway
- MariaDB 12.3.3 LTS
- React 19.3
- Vite 8.3
- Nginx (imagen de producción del frontend)
- Docker Compose

## Patrones de diseño utilizados

- **MVC / Controller**: capa HTTP.
- **Service Layer**: casos de uso y reglas transaccionales.
- **Repository**: acceso a persistencia.
- **DTO**: desacopla API de entidades JPA.
- **Mapper**: transforma dominio ↔ DTO.
- **Factory Method / Factory**: construcción consistente de pedidos.
- **Strategy**: cálculo de precio de cada ítem (`PricingStrategy`).
- **Reducer (frontend)**: estado local del pedido antes de persistirlo.
- **Observer / Publish-Subscribe**: SSE notifica cambios del catálogo a clientes web conectados.
- **Dependency Injection**: composición de servicios y estrategias.

## Estructura

```text
arepas-lulu-prototype/
├── backend/                 # API Spring Boot
├── frontend/                # SPA React
├── docs/
│   ├── architecture/        # C4 y decisiones
│   └── traceability/        # BR → StR → SyR → SWR → implementación
├── docker-compose.yml
└── README.md
```

## Arranque recomendado con Docker

Requisitos: Docker Engine + Docker Compose.

```bash
docker compose up --build
```

Servicios:

- Web: http://localhost:3000
- API: http://localhost:8080/api
- MariaDB: localhost:3306

Para detener:

```bash
docker compose down
```

Para eliminar también los datos:

```bash
docker compose down -v
```

## Arranque para desarrollo local

### 1. Base de datos

```bash
docker compose up -d mariadb
```

### 2. Backend

Requisitos locales: Java 21 y Maven 3.6.3+.

```bash
cd backend
mvn spring-boot:run
```

### 3. Frontend

Requisitos locales: Node 22+ y npm.

```bash
cd frontend
npm install
npm run dev
```

La SPA de desarrollo usa `http://localhost:8080/api` como API por defecto.

### Ejecución local sin MariaDB

Para una demostración local rápida puede usarse H2 en memoria. Los datos se pierden al detener el backend:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

El perfil `local` conserva la misma API y reglas de negocio, pero no sustituye la validación final sobre MariaDB.

## Flujo de demostración

1. Abrir **Catálogo**.
2. Crear `Arepa Todo Terreno`, seleccionar categoría y precio.
3. Editar su precio y comprobar el cambio.
4. Marcar un producto como no disponible y verificar que deja de aparecer en el selector del pedido.
5. Abrir **Pedido por mesa**.
6. Seleccionar mesa, agregar dos o más productos, cantidades y observaciones.
7. Revisar el resumen local.
8. Confirmar el pedido.
9. Verificar que la API devuelve un ID único y estado `ENVIADO_COCINA`.
10. Consultar los pedidos de la mesa en el historial.

## Endpoints principales

### F-01 Catálogo

```text
GET    /api/productos
GET    /api/productos?soloDisponibles=true
GET    /api/productos/{id}
GET    /api/productos/stream        # SSE de cambios del catálogo
POST   /api/productos
PUT    /api/productos/{id}
PATCH  /api/productos/{id}/disponibilidad
DELETE /api/productos/{id}
```

### F-02 Pedidos

```text
POST /api/pedidos
GET  /api/pedidos/{id}
GET  /api/pedidos?mesa=4
GET  /api/pedidos
```

## Datos iniciales

Flyway crea varias arepas/bebidas de ejemplo para poder probar F-02 inmediatamente.

## Calidad y pruebas

El backend contiene pruebas unitarias para servicios y reglas centrales. El repositorio incluye además un workflow de CI para compilar backend y frontend en GitHub Actions.

## Arquitectura C4

Ver:

- `docs/architecture/C4.md`
- `docs/architecture/workspace.dsl`

## Trazabilidad

Ver `docs/traceability/TRACEABILITY.md` para la relación:

**F → BR → StR → SyR → SWR → componente → endpoint → tabla → prueba**.
