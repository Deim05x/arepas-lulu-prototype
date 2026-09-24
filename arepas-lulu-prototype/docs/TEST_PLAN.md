# Plan de pruebas — prototipo F-01 / F-02

## F-01 Administrar catálogo

| Caso | Dado | Cuando | Entonces |
|---|---|---|---|
| CAT-01 Alta | Datos válidos | POST producto | HTTP 201 y producto visible |
| CAT-02 Precio | Producto activo | PUT con precio nuevo | Catálogo refleja precio nuevo |
| CAT-03 Disponibilidad | Producto activo | PATCH disponible=false | Deja de aparecer en productos ordenables |
| CAT-04 Protección | Producto no disponible | POST pedido manual con su ID | API responde 409 |
| CAT-05 Baja lógica | Producto activo | DELETE | Conserva registro pero queda inactivo |
| CAT-06 Sincronización | Dos clientes web abiertos | Se cambia catálogo | Cliente conectado recibe SSE y refresca |

## F-02 Pedido por mesa

| Caso | Dado | Cuando | Entonces |
|---|---|---|---|
| PED-01 Estado local | Mesa e ítems | Se agregan productos | Nada se persiste antes de confirmar |
| PED-02 Resumen | Pedido local | Revisar | Muestra cantidades, notas y total estimado |
| PED-03 Persistencia | Resumen válido | Confirmar | HTTP 201, ID único y ENVIADO_COCINA |
| PED-04 Precio confiable | Cliente intenta manipular precio | Enviar payload | API ignora precio cliente; usa catálogo |
| PED-05 Consulta ID | Pedido existente | GET /pedidos/{id} | Devuelve detalle completo |
| PED-06 Consulta mesa | Pedidos existentes | GET ?mesa=N | Devuelve historial de la mesa |

## Prueba de humo automatizable

Con el stack levantado:

```bash
./scripts/smoke-test.sh
```

La prueba crea/actualiza un producto, crea un pedido, consulta el historial y comprueba que un producto agotado sea rechazado por el backend.

En Windows PowerShell puede ejecutarse el equivalente:

```powershell
.\scripts\smoke-test.ps1
```

## Registro de ejecución inicial

- Backend: suite Maven ejecutada con Java 21; resultado **OK**.
- Frontend: `npm.cmd run build`; resultado **OK**.
- Docker Compose y smoke test HTTP: **pendientes en este entorno**, porque Docker y MariaDB no están disponibles.
- Detalle de evidencias y nombres de capturas: `docs/DELIVERY_EVIDENCE.md`.
