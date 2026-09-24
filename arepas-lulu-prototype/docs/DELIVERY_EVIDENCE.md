# Paquete de entrega académica

## Estado de la primera ejecución

| Verificación | Resultado | Evidencia |
|---|---|---|
| Build frontend (`npm.cmd run build`) | OK | Salida Vite: 24 módulos transformados, artefacto generado |
| Pruebas backend (`mvn test`) | OK | `PrototypeFlowIntegrationTest` y suite completa sin fallos |
| Arranque Docker Compose | BLOQUEADO EN ENTORNO | Docker no está disponible en el `PATH` |
| Smoke test HTTP F-01/F-02 | PENDIENTE | Requiere Docker/MariaDB o una API ya levantada |

## Evidencias que deben adjuntarse

Guardar capturas en `docs/evidence/screenshots/` con estos nombres:

1. `01-catalogo-alta-precio.png`: F-01 con producto creado y precio visible.
2. `02-catalogo-disponibilidad.png`: producto marcado agotado y ausente del selector de pedido.
3. `03-pedido-captura-local.png`: F-02 con mesa, dos ítems y observación antes de confirmar.
4. `04-pedido-resumen.png`: modal de confirmación con cantidades, notas y total.
5. `05-pedido-enviado.png`: pedido creado con ID y estado `ENVIADO_COCINA`.
6. `06-historial-mesa.png`: consulta del historial de la mesa.

Cada captura debe incluir la URL, el estado visible y una breve nota en la bitácora de entrega.

## Ejecución reproducible

Desde la raíz del proyecto:

```bash
docker compose up --build
./scripts/smoke-test.sh
```

En Windows PowerShell:

```powershell
docker compose up --build
.\scripts\smoke-test.ps1
```

## Diagramas C4 finales

- Contexto, contenedores, componentes y decisiones: `docs/architecture/C4.md`.
- Modelo ejecutable Structurizr DSL: `docs/architecture/workspace.dsl`.

La versión entregable debe conservar ambos archivos junto con las exportaciones PNG/PDF generadas por la herramienta de diagramación.