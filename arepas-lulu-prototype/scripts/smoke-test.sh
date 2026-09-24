#!/usr/bin/env bash
set -euo pipefail

API="${API_URL:-http://localhost:8080/api}"

echo "[1/6] Creando producto de prueba F-01..."
PRODUCT=$(curl -fsS -X POST "$API/productos" \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Arepa Todo Terreno Smoke","categoria":"Arepas","descripcion":"Producto para prueba de humo","precio":18000,"disponible":true}')
PRODUCT_ID=$(printf '%s' "$PRODUCT" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
echo "Producto creado: $PRODUCT_ID"

echo "[2/6] Actualizando precio..."
curl -fsS -X PUT "$API/productos/$PRODUCT_ID" \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Arepa Todo Terreno Smoke","categoria":"Arepas","descripcion":"Producto para prueba de humo","precio":19000,"disponible":true}' >/dev/null

echo "[3/6] Creando pedido de mesa F-02..."
ORDER=$(curl -fsS -X POST "$API/pedidos" \
  -H 'Content-Type: application/json' \
  -d "{\"mesaNumero\":4,\"items\":[{\"productoId\":$PRODUCT_ID,\"cantidad\":2,\"observacion\":\"Sin cebolla\"}]}")
ORDER_ID=$(printf '%s' "$ORDER" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
STATE=$(printf '%s' "$ORDER" | python3 -c 'import json,sys; print(json.load(sys.stdin)["estado"])')
[ "$STATE" = "ENVIADO_COCINA" ] || { echo "Estado inesperado: $STATE"; exit 1; }
echo "Pedido creado: $ORDER_ID ($STATE)"

echo "[4/6] Consultando pedido por ID..."
curl -fsS "$API/pedidos/$ORDER_ID" >/dev/null

echo "[5/6] Consultando historial de mesa 4..."
curl -fsS "$API/pedidos?mesa=4" | python3 -c 'import json,sys; d=json.load(sys.stdin); assert len(d)>=1'

echo "[6/6] Marcando producto agotado y comprobando bloqueo..."
curl -fsS -X PATCH "$API/productos/$PRODUCT_ID/disponibilidad" \
  -H 'Content-Type: application/json' -d '{"disponible":false}' >/dev/null
HTTP=$(curl -sS -o /tmp/arepas_error.json -w '%{http_code}' -X POST "$API/pedidos" \
  -H 'Content-Type: application/json' \
  -d "{\"mesaNumero\":5,\"items\":[{\"productoId\":$PRODUCT_ID,\"cantidad\":1,\"observacion\":null}]}")
[ "$HTTP" = "409" ] || { echo "Se esperaba HTTP 409 y llegó $HTTP"; cat /tmp/arepas_error.json; exit 1; }

echo "OK: F-01 y F-02 superaron la prueba de humo."
