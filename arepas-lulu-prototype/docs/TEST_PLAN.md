# Plan de prueba funcional F-01 a F-08

## Flujo integrado recomendado

1. F-01: crear/editar un producto y dejarlo disponible.
2. F-07: crear ingrediente y asociarlo a una receta del producto.
3. F-08: verificar una mesa `DISPONIBLE`.
4. F-02: crear pedido para esa mesa; verificar que pasa a `OCUPADA` y disminuye stock si hay receta.
5. F-03: mover `ENVIADO_COCINA → EN_PREPARACION → LISTO → SERVIDO`.
6. F-04: pagar; verificar factura, estado `PAGADO` y mesa `DISPONIBLE`.
7. F-05: consultar caja y marcar facturas del día como reportadas DIAN.
8. F-06: crear cliente, generar domicilio con productos y avanzar `PENDIENTE → DESPACHADO → ENTREGADO`.

## Casos negativos clave

- Producto inactivo/no disponible → pedido rechazado.
- Receta con stock insuficiente → pedido rechazado sin descuento parcial.
- Transición de cocina inválida → HTTP 409.
- Pago antes de `LISTO/SERVIDO` → HTTP 409.
- Segundo pago del mismo pedido → HTTP 409.
- Efectivo recibido menor al total → HTTP 409.
- Ajuste de inventario que deja stock negativo → HTTP 409.
- Mesa `INACTIVA` → no puede ocuparse mediante pedido.

## Automatización

```bash
cd backend && mvn test
cd ../frontend && npm run build
```

Además del test automatizado, la demo integrada anterior funciona como prueba de aceptación del prototipo completo.
