package com.arepaslulu.order.dto;

import java.math.BigDecimal;

public record PedidoItemResponse(
        Long productoId,
        String productoNombre,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String observacion
) {
}
